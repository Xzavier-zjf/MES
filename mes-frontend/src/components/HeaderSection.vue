<template>
  <div class="header fade-in">
    <!-- 顶部标题 + 导航按钮：一行水平分布 -->
    <div class="header-top-row">
      <div class="header-title">
        <i class="fas fa-tasks"></i>
        <span>任务管理系统</span>
      </div>
      <div class="nav-buttons">
        <button class="nav-btn" @click="go('/home')">首页</button>
         <button class="nav-btn" @click="go('/plan')">生产计划管理</button>
        <button class="nav-btn" @click="go('/task')">任务管理</button>
        <button class="nav-btn" @click="go('/device')">设备监控</button>
        <button class="nav-btn" @click="go('/injection')">注塑参数</button>
        <button class="nav-btn" @click="go('/pattern')">图案参数</button>
      </div>
    </div>

    <!-- 副标题 -->
    <p class="header-subtitle">
      高效管理生产任务，实时监控任务状态。通过本系统您可以创建、分配、跟踪和完成生产任务，
      确保生产计划按时完成。系统提供全面的数据统计和可视化分析，帮助您优化生产流程。
    </p>

    <!-- 统计卡片 -->
     <div class="stats-container">
      <div class="stat-card fade-in delay-1">
        <div class="stat-value">{{ props.totalTasks }}</div>
        <div class="stat-label">总任务数</div>
      </div>
      <div class="stat-card fade-in delay-2">
        <div class="stat-value">{{ props.inProgressTasks }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card fade-in delay-3">
        <div class="stat-value">{{ props.completedTasks }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card fade-in delay-4">
        <div class="stat-value">{{ props.pendingTasks }}</div>
        <div class="stat-label">待下发</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const go = (path) => {
  console.log('点击跳转到:', path)
  router.push(path)
}

// 保留props，设置默认值，防止未传数据报错
const props = defineProps({
  totalTasks: {
    type: Number,
    default: 0
  },
  inProgressTasks: {
    type: Number,
    default: 0
  },
  completedTasks: {
    type: Number,
    default: 0
  },
  pendingTasks: {
    type: Number,
    default: 0
  },
})
</script>

<style scoped>
@import '@/assets/task-manager.css';

/* 新增样式 */

.header-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.header-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  gap: 10px;
}

.nav-buttons {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 999;
}

.nav-btn {
  background-color: #25c0eb;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  position: relative;
  z-index: 999;
}


.nav-btn:hover {
  background-color: #1e40af;
}
</style>
