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
      // 确保token是字符串类型
      const tokenString = typeof token.value === 'string' ? token.value : String(token.value)
      
      // 检查token格式（JWT应该有3个部分，用.分隔）
      const tokenParts = tokenString.split('.')
      if (tokenParts.length !== 3) {
        // 对于非JWT token（如简单字符串token），直接认为有效，不输出错误日志
        return true
      }
      
      // 解析JWT token的payload部分
      const payload = JSON.parse(atob(tokenParts[1]))
      const currentTime = Date.now() / 1000
      
      // 检查是否过期
      if (payload.exp && payload.exp < currentTime) {
        logout()
        return false
      }
      
      return true
    } catch (error) {
      // 对于解析失败的token，不自动登出，可能是非JWT格式的token
      // 静默处理，不输出错误日志
      return true
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