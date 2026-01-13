/**
 * request 使用示例
 * 这个文件仅作为参考，可以删除
 */

import request from './request'

// ========== 基础用法 ==========

// 1. GET 请求（方式一：使用 params）
request.get('/api/users', { params: { id: 1 } })
	.then(res => {
		console.log('GET 请求成功:', res)
	})
	.catch(err => {
		console.error('GET 请求失败:', err)
	})

// 2. GET 请求（方式二：直接传参）
request.get('/api/users', { id: 1 })
	.then(res => {
		console.log('GET 请求成功:', res)
	})

// 3. POST 请求
request.post('/api/users', { name: 'John', age: 25 })
	.then(res => {
		console.log('POST 请求成功:', res)
	})

// 4. PUT 请求
request.put('/api/users/1', { name: 'John Updated' })
	.then(res => {
		console.log('PUT 请求成功:', res)
	})

// 5. DELETE 请求
request.delete('/api/users/1')
	.then(res => {
		console.log('DELETE 请求成功:', res)
	})

// 6. 使用 config 对象（类似 axios）
request({
	url: '/api/users',
	method: 'POST',
	data: { name: 'John' },
	headers: {
		'Custom-Header': 'value'
	},
	timeout: 5000
})
	.then(res => {
		console.log('请求成功:', res)
	})

// ========== 创建实例 ==========

// 创建带默认配置的实例
const api = request.create({
	baseURL: 'https://api.example.com',
	timeout: 5000,
	headers: {
		'X-Custom-Header': 'value'
	}
})

// 使用实例
api.get('/users')
api.post('/users', { name: 'John' })

// ========== 在 Vue 组件中使用 ==========

// 在 setup 中使用
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

export default {
	setup() {
		const users = ref([])
		
		onMounted(async () => {
			try {
				const res = await request.get('/api/users')
				users.value = res.data
			} catch (error) {
				console.error('获取用户列表失败:', error)
			}
		})
		
		return {
			users
		}
	}
}

// 在 Options API 中使用
export default {
	data() {
		return {
			users: []
		}
	},
	async mounted() {
		try {
			const res = await request.get('/api/users')
			this.users = res.data
		} catch (error) {
			console.error('获取用户列表失败:', error)
		}
	}
}

// ========== 使用 async/await ==========

async function fetchUsers() {
	try {
		const response = await request.get('/api/users')
		console.log('用户列表:', response.data)
		return response.data
	} catch (error) {
		console.error('获取用户列表失败:', error)
		throw error
	}
}

// ========== 并发请求 ==========

// 使用 Promise.all
Promise.all([
	request.get('/api/users'),
	request.get('/api/posts')
])
	.then(([users, posts]) => {
		console.log('用户:', users)
		console.log('文章:', posts)
	})
	.catch(error => {
		console.error('请求失败:', error)
	})