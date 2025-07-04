<template>
  <el-dialog
    :model-value="visible"
    title="设备详情"
    width="500px"
    :before-close="handleClose"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-descriptions :column="1" border>
      <el-descriptions-item label="设备编号">{{ device?.deviceCode }}</el-descriptions-item>
      <el-descriptions-item label="设备类型">{{ device?.name }}</el-descriptions-item>
      <el-descriptions-item label="使用时长">{{ device?.runtimeMinutes }} 小时</el-descriptions-item>
      <el-descriptions-item label="注塑压力">{{ device?.injectionPressure }} MPa</el-descriptions-item>
      <el-descriptions-item label="开启次数">{{ device?.openCloseTimes }}</el-descriptions-item>
      <el-descriptions-item label="注塑时间">{{ device?.injectionTime }} 秒</el-descriptions-item>
    </el-descriptions>

    <div style="margin-top: 16px">
      <el-form label-width="100px">
        <el-form-item label="修改状态">
          <el-select v-model="editableStatus" placeholder="请选择设备状态">
            <el-option label="运行中" value="运行中" />
            <el-option label="故障" value="故障" />
            <el-option label="空闲" value="空闲" />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" @click="saveStatus">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'  // 添加这行导入

const props = defineProps({
  visible: Boolean,
  device: Object
})

const emit = defineEmits(['update:visible', 'updateStatus'])

const editableStatus = ref('')

watch(() => props.device, (newVal) => {
  if (newVal) {
    editableStatus.value = newVal.status
  }
}, { immediate: true })

const handleClose = () => {
  emit('update:visible', false)
}

const saveStatus = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/equipment/devices/${props.device.id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        status: editableStatus.value
      })
    })

    if (!response.ok) {
      throw new Error('更新状态失败')
    }

    emit('updateStatus', editableStatus.value)
    emit('update:visible', false)
    ElMessage.success('状态更新成功')  // 确保这里使用了导入的ElMessage
    emit('filterDevices')
  } catch (error) {
    console.error('更新状态错误:', error)
    ElMessage.error('状态更新失败')  // 确保这里使用了导入的ElMessage
  }
}
</script>
