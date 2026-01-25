import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')

  const login = async (loginForm) => {
    try {
      const res = await request.post('/sys-user/admin/login', loginForm)
      // Assuming the response structure is { code: 200, data: 'token_string', msg: '...' }
      // Adjust based on actual API response if needed
      if (res.code === 200) {
        const tokenValue = res.data
        token.value = tokenValue
        localStorage.setItem('token', tokenValue)
        return true
      }
      return false
    } catch (error) {
      console.error('Login error:', error)
      throw error
    }
  }

  const logout = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  return {
    token,
    login,
    logout
  }
})
