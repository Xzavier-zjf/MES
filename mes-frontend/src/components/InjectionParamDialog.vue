<template>
  <el-dialog
    :title="`任务 ${task?.taskId} 工艺参数录入`"
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    width="500px"
  >
    <el-form :model="form" label-width="120px">
      <el-form-item label="注塑压力 (MPa)">
        <el-input-number v-model="form.pressure" :min="0" />
      </el-form-item>
      <el-form-item label="注塑速度 (mm/s)">
        <el-input-number v-model="form.speed" :min="0" />
      </el-form-item>
      <el-form-item label="冷却时间 (s)">
        <el-input-number v-model="form.coolingTime" :min="0" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
const props = defineProps({
  visible: Boolean,
  task: Object,
})
const emit = defineEmits(['update:visible', 'submit'])

const form = ref({
  pressure: 0,
  speed: 0,
  coolingTime: 0,
})

watch(() => props.task, (val) => {
  if (val) {
    // 可扩展：根据 task 预加载参数
    form.value = { pressure: 0, speed: 0, coolingTime: 0 }
  }
})

const submit = () => {
  emit('submit', { ...form.value, taskId: props.task?.taskId })
}
</script>
