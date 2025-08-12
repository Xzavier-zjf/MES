<template>
  <!-- 页面访问监控组件 -->
  <PageAccessMonitor />
  
  <!-- 主要内容区域 -->
  <router-view></router-view>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import PageAccessMonitor from '@/components/PageAccessMonitor.vue'

const appStore = useAppStore()
const authStore = useAuthStore()

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth()
  
  // 如果已登录，初始化数据
  if (authStore.isAuthenticated) {
    appStore.fetchAllData()
  }
})
</script>

<style>
/* 可选样式 */
body {
  margin: 0;
  font-family: 'Segoe UI', sans-serif;
}
</style>
