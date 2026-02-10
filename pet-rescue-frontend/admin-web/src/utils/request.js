import axios from 'axios'
import { ElMessage } from 'element-plus'
import JSONBig from 'json-bigint'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000, // 请求超时时间
  transformResponse: [function (data) {
    try {
      // 如果是字符串，尝试解析
      // 使用 storeAsString: true 将大数字处理为字符串，避免精度丢失和 BigInt 序列化问题
      return JSONBig({ storeAsString: true }).parse(data)
    } catch (err) {
      // 解析失败则返回原数据
      return data
    }
  }]
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      // 将 token 添加到请求头
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    // 请求错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 响应成功处理
    const res = response.data
    
    // 如果后端返回的状态码不是成功状态，可以在这里统一处理
    // 这里假设后端返回的数据格式为 { code: 200, data: ..., msg: ... }
    if (res.code && res.code !== 200) {
      if (res.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        router.push('/login')
        return Promise.reject(new Error('Unauthorized'))
      }
      ElMessage.error(res.msg || res.message || '请求失败')
      return Promise.reject(new Error(res.msg || res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 响应错误处理
    console.error('响应错误:', error)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 清除 token 并跳转到登录页
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败: ${status}`)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
