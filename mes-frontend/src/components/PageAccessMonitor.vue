<template>
  <!-- 这是一个无界面的监控组件 -->
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 页面访问日志
const logPageAccess = (path, isAuthorized) => {
  const logData = {
    path,
    timestamp: new Date().toISOString(),
    user: authStore.userInfo?.username || 'anonymous',
    isAuthorized,
    userAgent: navigator.userAgent,
    referrer: document.referrer
  }
  
  // 只在开发环境或未授权访问时记录日志
  if (import.meta.env.DEV || !isAuthorized) {
    console.log('页面访问日志:', logData)
  }
  
  // 这里可以发送到后端进行记录
  // sendAccessLog(logData)
}

// 检测非法访问尝试
const detectUnauthorizedAccess = () => {
  const currentPath = route.path
  const isProtectedRoute = route.meta.requiresAuth !== false
  
  if (isProtectedRoute && !authStore.isAuthenticated) {
    logPageAccess(currentPath, false)
    ElMessage.warning('检测到未授权访问尝试')
    return false
  }
  
  logPageAccess(currentPath, true)
  return true
}

// 监听路由变化
watch(
  () => route.path,
  (newPath) => {
    detectUnauthorizedAccess()
  },
  { immediate: true }
)

// 监听浏览器前进后退
const handlePopState = (event) => {
  setTimeout(() => {
    detectUnauthorizedAccess()
  }, 0)
}

// 监听页面可见性变化
const handleVisibilityChange = () => {
  if (!document.hidden) {
    // 页面重新可见时检查认证状态
    if (!authStore.checkTokenValidity()) {
      ElMessage.error('登录状态已失效')
      router.push('/login')
    }
  }
}

onMounted(() => {
  window.addEventListener('popstate', handlePopState)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  window.removeEventListener('popstate', handlePopState)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>