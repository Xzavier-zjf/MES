<template>
  <div class="navbar-container">
    <el-menu
      mode="horizontal"
      router
      :default-active="$route.path"
      background-color="transparent"
      text-color="#374151"
      active-text-color="#ffffff"
      class="custom-navbar"
    >
      <el-menu-item index="/home">首页</el-menu-item>
      <el-menu-item index="/plan">生产计划管理</el-menu-item>
      <el-menu-item index="/task">任务管理</el-menu-item>
      <el-menu-item index="/device">设备监控</el-menu-item>
      <el-menu-item index="/injection">注塑工艺参数</el-menu-item>
      <el-menu-item index="/pattern">图案工艺参数</el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const $route = useRoute()
</script>

<style scoped>
.navbar-container {
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid #e5e7eb;
}

/* 导航栏基础样式 */
.custom-navbar {
  padding: 0 40px;
  font-size: 15px;
  height: 60px;
  align-items: center;
}

/* 所有按钮统一圆角/过渡 */
::v-deep(.el-menu-item) {
  margin: 8px;
  padding: 10px 20px !important;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 500;
}

/* hover 效果更柔和 */
::v-deep(.el-menu-item:hover) {
  background: rgba(96, 165, 250, 0.15);
  color: #2563eb !important;
}

/* 选中项样式 */
::v-deep(.el-menu-item.is-active) {
  background: linear-gradient(to right, #3b82f6, #60a5fa);
  color: white !important;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}
</style>
