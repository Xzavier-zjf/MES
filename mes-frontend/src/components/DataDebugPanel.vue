<template>
  <el-card class="debug-panel" v-if="showDebug">
    <template #header>
      <div class="debug-header">
        <span>数据调试面板</span>
        <el-button size="small" @click="showDebug = false">关闭</el-button>
      </div>
    </template>
    
    <div class="debug-content">
      <h4>计划数据 ({{ plans.length }})</h4>
      <el-table :data="plans.slice(0, 3)" size="small" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="planCode" label="计划编号" width="150" />
        <el-table-column prop="totalQuantity" label="总量" width="80" />
        <el-table-column prop="status" label="状态" width="80" />
      </el-table>
      
      <h4 style="margin-top: 20px;">任务数据 ({{ tasks.length }})</h4>
      <el-table :data="tasks.slice(0, 5)" size="small" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="planId" label="计划ID" width="80" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="status" label="状态" width="80" />
      </el-table>
      
      <h4 style="margin-top: 20px;">分配统计</h4>
      <div v-for="plan in plans.slice(0, 3)" :key="plan.id" class="allocation-info">
        <strong>{{ plan.planCode }}:</strong>
        总量 {{ plan.totalQuantity }}, 
        已分配 {{ getTaskAllocation(plan.id) }}, 
        剩余 {{ plan.totalQuantity - getTaskAllocation(plan.id) }}
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  plans: {
    type: Array,
    default: () => []
  },
  tasks: {
    type: Array,
    default: () => []
  }
})

const showDebug = ref(true)

const getTaskAllocation = (planId) => {
  const relatedTasks = props.tasks.filter(task => task.planId === planId)
  return relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
}
</script>

<style scoped>
.debug-panel {
  margin-bottom: 20px;
  border: 2px solid #f0f0f0;
}

.debug-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.debug-content {
  max-height: 400px;
  overflow-y: auto;
}

.allocation-info {
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

h4 {
  color: #409eff;
  margin: 10px 0;
}
</style>