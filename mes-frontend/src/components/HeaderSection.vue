<template>
  <div class="header fade-in">
    <!-- 顶部标题 + 导航按钮：一行水平分布 -->
    <div class="header-top-row">
      <div class="header-title">
        <i class="fas fa-tasks"></i>
        <span>{{ title }}</span>
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
      {{ subtitle }}
    </p>

    <!-- 统计卡片 -->
    <div class="stats-container" v-if="showStats">
      <div class="stat-card fade-in delay-1">
        <div class="stat-value">{{ props.value1 }}</div>
        <div class="stat-label">{{ props.card1 }}</div>
      </div>
      <div class="stat-card fade-in delay-2">
        <div class="stat-value">{{ props.value2 }}</div>
        <div class="stat-label">{{ props.card2 }}</div>
      </div>
      <div class="stat-card fade-in delay-3">
        <div class="stat-value">{{ props.value3 }}</div>
        <div class="stat-label">{{ props.card3 }}</div>
      </div>
      <div class="stat-card fade-in delay-4">
        <div class="stat-value">{{ props.value4 }}</div>
        <div class="stat-label">{{ props.card4 }}</div>
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

// 定义props
const props = defineProps({
  title: {
    type: String,
    default: '生产管理系统'
  },
  subtitle: {
    type: String,
    default: '高效管理生产流程，实时监控系统状态'
  },
  showStats: {
    type: Boolean,
    default: false
  },
  value1: {
    type: Number,
    default: 0
  },
  card1: {
    type: String,
    default: '总任务数'
  },
  value2: {
    type: Number,
    default: 0
  },
  card2: {
    type: String,
    default: '进行中'
  },
  value3: {
    type: Number,
    default: 0
  },
  card3: {
    type: String,
    default: '已完成'
  },
  value4: {
    type: Number,
    default: 0
  },
  card4: {
    type: String,
    default: '未完成'
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
