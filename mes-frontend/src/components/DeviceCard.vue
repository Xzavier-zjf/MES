<template>
  <el-card :class="statusClass" shadow="always" @click="handleClick" style="cursor:pointer;">
    <div class="card-header">
      <h3>{{ device.deviceCode }} · {{ device.name }}</h3>
      <el-tag :type="tagType">{{ device.status }}</el-tag>
    </div>
    <ul class="metrics">
      <li>使用时长：{{ device.runtimeMinutes }} h</li>
      <li>开模次数：{{ device.openCloseTimes }}</li>
      <li>压力：{{ device.injectionPressure }} Mpa</li>
      <li>注塑时间：{{ device.injectionTime }} s</li>
    </ul>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ device: Object })
const emit = defineEmits(['view'])

const handleClick = () => {
  emit('view', props.device)
}

const statusClass = computed(() => ({
  'status-running': props.device.status === '运行中',
  'status-idle': props.device.status === '空闲',
  'status-error': props.device.status === '故障',
}))

const tagType = computed(() => 
  props.device.status === '运行中' ? 'success'
  : props.device.status === '故障' ? 'danger'
  : 'info'
)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.metrics {
  list-style: none;
  padding: 0;
  margin: 0;
  line-height: 1.8;
}
.status-running {
  border-left: 6px solid #67c23a;
}
.status-idle {
  border-left: 6px solid #909399;
}
.status-error {
  border-left: 6px solid #f56c6c;
}
</style>
