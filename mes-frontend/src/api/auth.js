import request from './request'

// 用户登录
export const login = async (username, password) => {
  try {
    const response = await request.post('/auth/login', {
      username,
      password
    })
    
    // 根据后端返回格式调整
    if (typeof response === 'string') {
      return { token: response }
    }
    
    return response
  } catch (error) {
    throw new Error(error.response?.data?.message || '登录失败')
  }
}

// 用户注册
export const register = async (userData) => {
  try {
    const response = await request.post('/auth/register', userData)
    return response
  } catch (error) {
    throw new Error(error.response?.data?.message || '注册失败')
  }
}

// 获取用户信息
export const getUserInfo = async () => {
  try {
    const response = await request.get('/auth/userinfo')
    return response
  } catch (error) {
    throw new Error(error.response?.data?.message || '获取用户信息失败')
  }
}

// 刷新token
export const refreshToken = async () => {
  try {
    const response = await request.post('/auth/refresh')
    return response
  } catch (error) {
    throw new Error(error.response?.data?.message || '刷新token失败')
  }
}

// 用户登出
export const logout = async () => {
  try {
    const response = await request.post('/auth/logout')
    return response
  } catch (error) {
    // 登出失败也不抛出错误，因为前端可以直接清除本地状态
    console.warn('服务端登出失败:', error)
    return null
  }
}
