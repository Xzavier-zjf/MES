<template>
  <div class="quantity-status">
    <div class="status-item">
      <span class="label">计划总量:</span>
      <span class="value total">{{ planTotal }}</span>
    </div>
    <div class="status-item">
      <span class="label">已分配:</span>
      <span class="value allocated">{{ allocated }}</span>
    </div>
    <div class="status-item">
      <span class="label">剩余:</span>
      <span class="value remaining" :class="remainingClass">{{ remaining }}</span>
    </div>
    <div class="progress-bar">
      <div 
        class="progress-fill" 
        :style="{ width: progressPercent + '%' }"
        :class="progressClass"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  planTotal: {
    type: Number,
    default: 0
  },
  allocated: {
    type: Number,
    default: 0
  }
})

const remaining = computed(() => props.planTotal - props.allocated)
const progressPercent = computed(() => {
  if (props.planTotal === 0) return 0
  return Math.min((props.allocated / props.planTotal) * 100, 100)
})

const remainingClass = computed(() => {
  if (remaining.value < 0) return 'over'
  if (remaining.value === 0) return 'complete'
  return 'normal'
})

const progressClass = computed(() => {
  if (props.allocated > props.planTotal) return 'over'
  if (props.allocated === props.planTotal) return 'complete'
  return 'normal'
})
</script>

<style scoped>
.quantity-status {
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f9f9f9;
  font-size: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.label {
  color: #666;
}

.value {
  font-weight: bold;
}

.value.total {
  color: #409eff;
}

.value.allocated {
  color: #67c23a;
}

.value.remaining.normal {
  color: #e6a23c;
}

.value.remaining.complete {
  color: #67c23a;
}

.value.remaining.over {
  color: #f56c6c;
}

.progress-bar {
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
  margin-top: 8px;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-fill.normal {
  background: #409eff;
}

.progress-fill.complete {
  background: #67c23a;
}

.progress-fill.over {
  background: #f56c6c;
}
</style>