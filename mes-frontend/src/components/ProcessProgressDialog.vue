<template>
  <el-dialog :model-value="visible" :title="`${plan?.planCode} - 工序进度详情`" width="800px"
    @update:model-value="$emit('update:visible', $event)" @close="$emit('update:visible', false)">
    <div v-if="plan && allocation" class="progress-content">
      <!-- 总体概览 -->
      <el-card class="overview-card" shadow="never">
        <template #header>
          <span>总体概览</span>
        </template>
        <div class="overview-stats">
          <div class="stat-item">
            <div class="stat-value">{{ allocation.totalQuantity }}</div>
            <div class="stat-label">计划总量</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ allocation.totalCompleted }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ allocation.totalInProgress }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ allocation.actualRemaining }}</div>
            <div class="stat-label">实际剩余</div>
          </div>
        </div>
      </el-card>

      <!-- 工序详情 -->
      <el-card class="process-card" shadow="never">
        <template #header>
          <span>各工序进度</span>
        </template>
        <div class="process-list">
          <div v-for="processType in processFlow" :key="processType" class="process-item">
            <div class="process-header">
              <span class="process-name">{{ processType }}</span>
              <el-tag type="info" size="small">
                待开发
              </el-tag>
            </div>

            <div class="process-stats">
              <div class="stat-row">
                <span>已分配: 0</span>
                <span>已完成: 0</span>
              </div>
              <div class="stat-row">
                <span>进行中: 0</span>
                <span>完成率: 0%</span>
              </div>
            </div>

            <!-- 工序进度条 -->
            <div class="process-progress">
              <div class="progress-bar">
                <div class="progress-fill" style="width: 0%; background: #e4e7ed;"></div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 推荐操作 -->
      <el-card class="recommendations-card" shadow="never">
        <template #header>
          <span>推荐操作</span>
        </template>
        <div class="recommendations-list">
          <div class="recommendation-item">
            <div class="rec-content">
              <div class="rec-message">详细工序分析功能正在开发中...</div>
              <div class="rec-priority">请使用基础进度信息</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
      <el-button type="primary" @click="exportReport">导出报告</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

// 临时简化版本，避免复杂依赖
const PROCESS_FLOW = ['注塑', '印刷', '组装', '质检', '包装']

// 简化的计算函数
function calculateDetailedAllocation(plan, tasks) {
  if (!plan || !tasks) return null

  const relatedTasks = tasks.filter(task => task.planId === plan.id)
  const totalAllocated = relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
  const totalCompleted = relatedTasks.filter(t => t.status === '已完成').reduce((sum, task) => sum + (task.quantity || 0), 0)
  const totalInProgress = relatedTasks.filter(t => t.status === '进行中').reduce((sum, task) => sum + (task.quantity || 0), 0)

  return {
    totalQuantity: plan.totalQuantity,
    totalAllocated,
    totalCompleted,
    totalInProgress,
    actualRemaining: plan.totalQuantity - totalCompleted,
    processSummary: {},
    flowProgress: {}
  }
}

function generateFlowReport(plan, tasks) {
  return `简化报告 - ${plan.planCode}\n总量: ${plan.totalQuantity}`
}

function getRecommendedActions(plan, tasks) {
  return []
}

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  plan: {
    type: Object,
    default: null
  },
  tasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible'])

const processFlow = PROCESS_FLOW

const allocation = computed(() => {
  if (!props.plan || !props.tasks) return null
  return calculateDetailedAllocation(props.plan, props.tasks)
})

const exportReport = () => {
  if (!props.plan) return

  const report = `简化报告 - ${props.plan.planCode}\n总量: ${props.plan.totalQuantity}\n状态: ${props.plan.status}`

  // 创建并下载文本文件
  const blob = new Blob([report], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${props.plan.planCode}_进度报告.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('报告已导出')
}
</script>

<style scoped>
.progress-content {
  max-height: 600px;
  overflow-y: auto;
}

.overview-card {
  margin-bottom: 16px;
}

.overview-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.process-card {
  margin-bottom: 16px;
}

.process-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.process-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.process-item.bottleneck {
  border-color: #F56C6C;
  background-color: #fef0f0;
}

.process-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.process-name {
  font-weight: bold;
  font-size: 14px;
}

.bottleneck-badge {
  color: #F56C6C;
  font-size: 12px;
}

.process-stats {
  margin-bottom: 8px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.process-progress {
  width: 100%;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.recommendations-card {
  margin-bottom: 16px;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.recommendation-item.priority-high {
  background-color: #fef0f0;
  border-left: 4px solid #F56C6C;
}

.recommendation-item.priority-medium {
  background-color: #fdf6ec;
  border-left: 4px solid #E6A23C;
}

.recommendation-item.priority-low {
  background-color: #f0f9ff;
  border-left: 4px solid #409EFF;
}

.rec-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.rec-content {
  flex: 1;
}

.rec-message {
  font-size: 14px;
  margin-bottom: 4px;
}

.rec-priority {
  font-size: 12px;
  color: #666;
}
</style>