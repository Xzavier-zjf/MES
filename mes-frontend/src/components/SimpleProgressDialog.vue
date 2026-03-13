<template>
  <el-dialog
    :model-value="visible"
    :title="`${plan?.planCode} - 基础进度信息`"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-if="plan" class="simple-progress">
      <el-descriptions title="计划信息" :column="2" border>
        <el-descriptions-item label="计划编号">{{ plan.planCode }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ plan.productName }}</el-descriptions-item>
        <el-descriptions-item label="计划总量">{{ plan.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="计划状态">
          <el-tag :type="getStatusType(plan.status)">{{ plan.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已分配任务">{{ totalAllocated }}</el-descriptions-item>
        <el-descriptions-item label="已完成数量">{{ totalCompleted }}</el-descriptions-item>
        <el-descriptions-item label="进行中数量">{{ totalInProgress }}</el-descriptions-item>
        <el-descriptions-item label="实际剩余">{{ actualRemaining }}</el-descriptions-item>
      </el-descriptions>

      <div class="progress-section" style="margin-top: 20px;">
        <h4>完成进度</h4>
        <el-progress 
          :percentage="completionPercent" 
          :color="getProgressColor(completionPercent)"
          :stroke-width="8"
        />
        
        <h4 style="margin-top: 20px;">分配进度</h4>
        <el-progress 
          :percentage="allocationPercent" 
          :color="getAllocationColor(allocationPercent)"
          :stroke-width="8"
        />
      </div>

      <div class="task-list" style="margin-top: 20px;" v-if="relatedTasks.length > 0">
        <h4>相关任务</h4>
        <el-table :data="relatedTasks" size="small" border>
          <el-table-column prop="taskCode" label="任务编号" width="150" />
          <el-table-column prop="processType" label="工序类型" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

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

const relatedTasks = computed(() => {
  if (!props.plan || !props.tasks) return []
  return props.tasks.filter(task => task.planId === props.plan.id)
})

const totalAllocated = computed(() => {
  return relatedTasks.value.reduce((sum, task) => sum + (task.quantity || 0), 0)
})

const totalCompleted = computed(() => {
  return relatedTasks.value
    .filter(task => task.status === '已完成')
    .reduce((sum, task) => sum + (task.quantity || 0), 0)
})

const totalInProgress = computed(() => {
  return relatedTasks.value
    .filter(task => task.status === '进行中')
    .reduce((sum, task) => sum + (task.quantity || 0), 0)
})

const actualRemaining = computed(() => {
  if (!props.plan) return 0
  return Math.max(0, props.plan.totalQuantity - totalCompleted.value)
})

const completionPercent = computed(() => {
  if (!props.plan || props.plan.totalQuantity === 0) return 0
  return Math.round((totalCompleted.value / props.plan.totalQuantity) * 100)
})

const allocationPercent = computed(() => {
  if (!props.plan || props.plan.totalQuantity === 0) return 0
  return Math.round((totalAllocated.value / props.plan.totalQuantity) * 100)
})

const getStatusType = (status) => {
  switch (status) {
    case '已完成': return 'success'
    case '进行中': return 'primary'
    case '待下发': return 'warning'
    default: return 'info'
  }
}

const getProgressColor = (percent) => {
  if (percent >= 100) return '#67C23A'
  if (percent >= 80) return '#409EFF'
  if (percent >= 50) return '#E6A23C'
  return '#F56C6C'
}

const getAllocationColor = (percent) => {
  if (percent >= 100) return '#67C23A'
  if (percent >= 80) return '#409EFF'
  return '#E6A23C'
}
</script>

<style scoped>
.simple-progress {
  max-height: 500px;
  overflow-y: auto;
}

.progress-section h4 {
  margin: 10px 0;
  color: #409EFF;
}

.task-list h4 {
  margin: 10px 0;
  color: #409EFF;
}
</style>