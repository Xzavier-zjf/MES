<template>
  <el-dialog
    :title="isEditing ? '编辑任务' : '新建任务'"
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    width="500px"
    :before-close="handleClose"
  >
    <el-form
      ref="taskFormRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      label-position="left"
      status-icon
    >
      <el-form-item label="计划编号" prop="planCode">
       <el-select v-model="form.planCode" placeholder="请选择计划编号" filterable>
  <el-option
    v-for="plan in planOptions"
    :key="plan.value"
    :label="plan.label"
    :value="plan.value"
  />
</el-select>

      </el-form-item>

      <el-form-item label="工序类型" prop="processType">
        <el-select v-model="form.processType" placeholder="请选择工序类型">
          <el-option
            v-for="type in processTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="设备编号" prop="deviceCode">
        <el-select v-model="form.deviceCode" placeholder="请选择设备编号" filterable>
          <el-option
            v-for="device in deviceOptions"
            :key="device"
            :label="device"
            :value="device"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="任务数量" prop="quantity">
        <el-input-number
          v-model="form.quantity"
          :min="1"
          :max="10000"
          style="width: 100%"
        />
      </el-form-item>

      
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="submitForm">
        {{ isEditing ? '更新' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  modelValue: Object,
  planOptions: {
    type: Array,
    default: () => []
  },
  processTypes: {
    type: Array,
    default: () => []
  },
  deviceOptions: {
    type: Array,
    default: () => []
  },
  isEditing: Boolean,
})

const emit = defineEmits(['update:visible', 'submit'])

const taskFormRef = ref(null)

const form = reactive({
  taskId: '',
  planCode: '',
  processType: '',
  deviceCode: '',
  quantity: 1,

})

const rules = {
  planCode: [{ required: true, message: '请选择计划编号', trigger: 'change' }],
  processType: [{ required: true, message: '请选择工序类型', trigger: 'change' }],
  deviceCode: [{ required: true, message: '请选择设备编号', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入任务数量', trigger: 'blur' }],
  
}

// 监听外部传入 modelValue（用于编辑）
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      Object.assign(form, newVal)
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

function resetForm() {
  if (taskFormRef.value) {
    taskFormRef.value.resetFields()
  }
  Object.assign(form, {
    taskId: '',
    planCode: '',
    processType: '',
    deviceCode: '',
    quantity: 1,
   
  })
}

function handleClose() {
  emit('update:visible', false)
  resetForm()
}

function submitForm() {
  taskFormRef.value.validate((valid) => {
    if (valid) {
      emit('submit', { ...form })
    } else {
      ElMessage.error('请完整填写表单')
    }
  })
}
</script>

<style scoped>
.el-dialog {
  border-radius: 16px;
  overflow: hidden;
}
.el-dialog__header {
  background: linear-gradient(90deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  font-weight: 600;
  padding: 16px 24px;
}
.el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #eee;
}
</style>
