import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  // 认证状态
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const userInfo = computed(() => user.value)

  // 登录方法
  const login = (tokenValue, userInfo = null) => {
    token.value = tokenValue
    user.value = userInfo
    
    // 持久化存储
    localStorage.setItem('token', tokenValue)
    if (userInfo) {
      localStorage.setItem('user', JSON.stringify(userInfo))
    }
  }

  // 登出方法
  const logout = () => {
    token.value = null
    user.value = null
    
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  // 检查token有效性
  const checkTokenValidity = () => {
    if (!token.value) return false
    
    try {
      // 解析JWT token的payload部分
      const payload = JSON.parse(atob(token.value.split('.')[1]))
      const currentTime = Date.now() / 1000
      
      // 检查是否过期
      if (payload.exp && payload.exp < currentTime) {
        logout()
        return false
      }
      
      return true
    } catch (error) {
      console.error('Token解析失败:', error)
      logout()
      return false
    }
  }

  // 初始化时检查token
  const initAuth = () => {
    if (token.value && !checkTokenValidity()) {
      logout()
    }
  }

  return {
    // 状态
    token,
    user,
    isLoading,
    
    // 计算属性
    isAuthenticated,
    userInfo,
    
    // 方法
    login,
    logout,
    checkTokenValidity,
    initAuth
  }
})