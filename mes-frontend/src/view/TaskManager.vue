<template>
  <div class="task-manager-page">
    <!-- 头部信息统计卡片 -->
    <HeaderSection />

    <!-- 筛选区域 -->
    <FilterSection
      :filters="filters"
      @filter="filterTasks"
      @reset="resetFilters"
      @create="openCreateDialog"
    />

    <!-- 表格区域 -->
    <TaskTable
      :tasks="filteredTasks"
      @edit="editTask"
      @delete="deleteTask"
    />

    <!-- 弹窗区域 -->
    <TaskDialog
      v-model:visible="dialogVisible"
      :form="form"
      :is-editing="isEditing"
      :plan-options="planOptions"
      :process-types="processTypes"
      :device-options="deviceOptions"
      @submit="submitTask"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import HeaderSection from '@/components/HeaderSection.vue'
import FilterSection from '@/components/TaskFilter.vue'
import TaskTable from '@/components/TaskTable.vue'
import TaskDialog from '@/components/TaskDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const filters = ref({
  planCode: '',
  processType: '',
  status: ''
})

const tasks = ref([
  { taskId: 'T001', planCode: 'P20240601', processType: '注塑', deviceCode: 'D001', quantity: 100, status: '进行中', description: 'A型零件注塑生产' },
  { taskId: 'T002', planCode: 'P20240602', processType: '印刷', deviceCode: 'D002', quantity: 200, status: '待下发', description: '产品外壳图案印刷' },
  { taskId: 'T003', planCode: 'P20240601', processType: '注塑', deviceCode: 'D003', quantity: 150, status: '已完成', description: 'B型零件注塑生产' },
  { taskId: 'T004', planCode: 'P20240603', processType: '组装', deviceCode: 'D004', quantity: 80, status: '进行中', description: '最终产品组装' },
  { taskId: 'T005', planCode: 'P20240604', processType: '质检', deviceCode: 'D005', quantity: 120, status: '待下发', description: '成品质量检查' },
  { taskId: 'T006', planCode: 'P20240602', processType: '印刷', deviceCode: 'D002', quantity: 180, status: '已完成', description: '包装盒印刷' },
])

const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    return (
      (!filters.value.planCode || task.planCode.includes(filters.value.planCode)) &&
      (!filters.value.processType || task.processType === filters.value.processType) &&
      (!filters.value.status || task.status === filters.value.status)
    )
  })
})

const planOptions = ['P20240601', 'P20240602', 'P20240603', 'P20240604', 'P20240605']
const processTypes = ['注塑', '印刷', '组装', '质检', '包装']
const deviceOptions = ['D001', 'D002', 'D003', 'D004', 'D005', 'D006']

const dialogVisible = ref(false)
const isEditing = ref(false)
const form = ref({
  taskId: '',
  planCode: '',
  processType: '',
  deviceCode: '',
  quantity: 100,
  status: '待下发',
  description: ''
})

const resetFilters = () => {
  filters.value = { planCode: '', processType: '', status: '' }
}

const filterTasks = () => {
  // 由于使用 computed，会自动更新
}

const openCreateDialog = () => {
  isEditing.value = false
  dialogVisible.value = true
  form.value = { taskId: '', planCode: '', processType: '', deviceCode: '', quantity: 100, status: '待下发', description: '' }
}

const submitTask = (data) => {
  if (isEditing.value) {
    const index = tasks.value.findIndex(t => t.taskId === data.taskId)
    if (index !== -1) tasks.value[index] = { ...data }
    ElMessage.success('任务更新成功')
  } else {
    const newTask = { ...data, taskId: `T${Math.floor(1000 + Math.random() * 9000)}` }
    tasks.value.push(newTask)
    ElMessage.success('任务创建成功')
  }
  dialogVisible.value = false
}

const editTask = (task) => {
  form.value = { ...task }
  isEditing.value = true
  dialogVisible.value = true
}

const deleteTask = (task) => {
  ElMessageBox.confirm(`确定要删除任务 ${task.taskId} 吗？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    tasks.value = tasks.value.filter(t => t.taskId !== task.taskId)
    ElMessage.success('任务删除成功')
  }).catch(() => {})
}
</script>

<style scoped>
@import '@/assets/task-manager.css';
</style>
