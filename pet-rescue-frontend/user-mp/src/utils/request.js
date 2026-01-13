/**
 * 封装 uni.request，使其用法类似 axios
 */

// 请求基础配置
// 注意：微信小程序不支持 Node.js 原生模块，这里直接使用 Vite 环境变量
const baseURL = import.meta.env.VITE_APP_BASE_URL || 'http://localhost:8080'

// 请求拦截器
const requestInterceptor = (config) => {
	// 可以在这里添加 token 等
	const token = uni.getStorageSync('token')
	if (token) {
		config.header = config.header || {}
		config.header.Authorization = `Bearer ${token}`
	}
	
	// 设置默认 header
	config.header = {
		'Content-Type': 'application/json',
		...config.header
	}
	
	return config
}

// 响应拦截器
const responseInterceptor = (response) => {
	const { statusCode, data } = response
	
	// HTTP 状态码处理
	if (statusCode >= 200 && statusCode < 300) {
		// 可以根据后端返回的数据结构进行调整
		// 假设后端返回格式为 { code: 200, data: {}, message: '' }
		if (data.code === 200 || data.code === 0) {
			return Promise.resolve(data)
		} else {
			// 业务错误
			uni.showToast({
				title: data.message || '请求失败',
				icon: 'none'
			})
			return Promise.reject(new Error(data.message || '请求失败'))
		}
	} else {
		// HTTP 错误
		uni.showToast({
			title: `请求失败: ${statusCode}`,
			icon: 'none'
		})
		return Promise.reject(new Error(`HTTP Error: ${statusCode}`))
	}
}

// 错误处理
const errorHandler = (error) => {
	console.error('Request Error:', error)
	
	// 网络错误
	if (error.errMsg && error.errMsg.includes('fail')) {
		uni.showToast({
			title: '网络连接失败',
			icon: 'none'
		})
	}
	
	return Promise.reject(error)
}

// 核心请求方法
const request = (config) => {
	// 处理 config 参数，支持 axios 风格的传参
	let url = ''
	let options = {}
	
	if (typeof config === 'string') {
		// request(url, config) 或 request(url)
		url = config
		options = arguments[1] || {}
	} else {
		// request({ url, method, data, ... })
		url = config.url
		options = { ...config }
		delete options.url
	}
	
	// 处理 GET 请求的 params（查询字符串）
	if (options.method === 'GET' || !options.method) {
		const params = options.params || {}
		if (Object.keys(params).length > 0) {
			const queryString = Object.keys(params)
				.map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
				.join('&')
			url += (url.includes('?') ? '&' : '?') + queryString
		}
	}
	
	// 处理完整 URL
	if (!url.startsWith('http://') && !url.startsWith('https://')) {
		// 简单的字符串拼接，避免使用 path 或 url 模块
		const prefix = baseURL.endsWith('/') ? baseURL.slice(0, -1) : baseURL
		const path = url.startsWith('/') ? url : '/' + url
		url = prefix + path
	}
	
	// 请求拦截
	const finalConfig = requestInterceptor({
		url,
		method: options.method || 'GET',
		data: options.data || {},
		header: options.headers || options.header || {},
		timeout: options.timeout || 10000,
		...options
	})
	
	return new Promise((resolve, reject) => {
		uni.request({
			...finalConfig,
			success: (res) => {
				responseInterceptor(res)
					.then(resolve)
					.catch(reject)
			},
			fail: (err) => {
				errorHandler(err)
					.then(resolve)
					.catch(reject)
			}
		})
	})
}

// 封装常用方法
const createRequestMethod = (method) => {
	return (url, dataOrConfig, config = {}) => {
		// 处理 GET 请求的 params
		if (method.toUpperCase() === 'GET') {
			// GET 请求：第二个参数可能是 params 对象或 config 对象
			if (dataOrConfig && typeof dataOrConfig === 'object' && !dataOrConfig.params && !dataOrConfig.method) {
				// 如果第二个参数是普通对象，当作 params 处理
				return request({
					url,
					method: 'GET',
					params: dataOrConfig,
					...config
				})
			} else {
				// 如果第二个参数是 config 对象
				return request({
					url,
					method: 'GET',
					...dataOrConfig,
					...config
				})
			}
		} else {
			// POST/PUT/DELETE 等请求：第二个参数是 data
			return request({
				url,
				method: method.toUpperCase(),
				data: dataOrConfig,
				...config
			})
		}
	}
}

// 导出 axios 风格的 API
const http = {
	request,
	get: createRequestMethod('get'),
	post: createRequestMethod('post'),
	put: createRequestMethod('put'),
	delete: createRequestMethod('delete'),
	patch: createRequestMethod('patch'),
	
	// 支持 axios 的别名方法
	head: createRequestMethod('head'),
	options: createRequestMethod('options'),
	
	// 创建实例（类似 axios.create）
	create(config) {
		const instance = { ...http }
		const originalRequest = instance.request
		
		instance.request = (configOrUrl, requestConfig) => {
			let mergedConfig = {}
			
			if (typeof configOrUrl === 'string') {
				mergedConfig = {
					...config,
					url: configOrUrl,
					...requestConfig
				}
			} else {
				mergedConfig = {
					...config,
					...configOrUrl
				}
			}
			
			return originalRequest(mergedConfig)
		}
		
		// 重新绑定方法
		['get', 'post', 'put', 'delete', 'patch', 'head', 'options'].forEach(method => {
			instance[method] = (url, data, requestConfig = {}) => {
				return instance.request({
					url,
					method: method.toUpperCase(),
					data,
					...config,
					...requestConfig
				})
			}
		})
		
		return instance
	},
	
	// 设置默认配置
	defaults: {
		baseURL: baseURL,
		timeout: 10000,
		headers: {
			'Content-Type': 'application/json'
		}
	},
	
	// 拦截器（类似 axios 的拦截器）
	interceptors: {
		request: {
			use: (fulfilled, rejected) => {
				const originalInterceptor = requestInterceptor
				requestInterceptor = (config) => {
					try {
						return fulfilled ? fulfilled(config) : config
					} catch (error) {
						return rejected ? rejected(error) : Promise.reject(error)
					}
				}
			}
		},
		response: {
			use: (fulfilled, rejected) => {
				const originalInterceptor = responseInterceptor
				responseInterceptor = (response) => {
					try {
						return fulfilled ? fulfilled(response) : response
					} catch (error) {
						return rejected ? rejected(error) : Promise.reject(error)
					}
				}
			}
		}
	}
}

export default http