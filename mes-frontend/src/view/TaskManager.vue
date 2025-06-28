<template>
  <div class="task-manager-page">
    <HeaderSection 
    :totalTasks="totalTasks"
  :inProgressTasks="inProgressTasks"
  :completedTasks="completedTasks"
  :pendingTasks="pendingTasks"/>

    <TaskFilter
      v-model:filters="filters"
      @filter="filterTasks"
      @reset="resetFilters"
      @create="openCreateDialog"
    />

    <TaskTable
      :tasks="tasks"
      @edit="editTask"
      @delete="deleteTask"
    />

    <TaskDialog
      :visible="dialogVisible"
      :modelValue="form"
      :planOptions="planOptions"
      :processTypes="processTypes"
      :deviceOptions="deviceOptions"
      :isEditing="isEditing"
      @update:visible="val => dialogVisible = val"
      @submit="submitTask"
    />
  </div>
</template>

<script setup>
import { ref, computed,onMounted } from 'vue'
import HeaderSection from '@/components/HeaderSection.vue'
import TaskFilter from '@/components/TaskFilter.vue'
import TaskTable from '@/components/TaskTable.vue'
import TaskDialog from '@/components/TaskDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const filters = ref({
  planCode: '',
  processType: '',
  status: ''
})

const tasks = ref([])
const planOptions = ref([])
const planMap = ref({})
const reversePlanMap = ref({})
const processTypes = ['注塑', '印刷', '组装', '质检', '包装']
const deviceOptions = ref([])
const deviceMap = ref({})

// 对话框状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const form = ref({
  taskId: '',
  planCode: '',
  processType: '',
  deviceCode: '',
  quantity: 100,
  status: '待下发'
})

// 清空筛选并重新加载任务
const resetFilters = async () => {
  filters.value = { planCode: '', processType: '', status: '' }
  await filterTasks()
}
// 计算统计数据
const totalTasks = computed(() => tasks.value.length)
const inProgressTasks = computed(() => tasks.value.filter(t => t.status === '进行中').length)
const completedTasks = computed(() => tasks.value.filter(t => t.status === '已完成').length)
const pendingTasks = computed(() => tasks.value.filter(t => t.status === '待下发').length)
// 获取任务列表
const filterTasks = async () => {
  try {
    const params = new URLSearchParams()
    if (filters.value.planCode) params.append('planCode', filters.value.planCode.trim())
    if (filters.value.processType) params.append('processType', filters.value.processType.trim())
    if (filters.value.status) params.append('status', filters.value.status.trim())
    params.append('page', '0')
    params.append('size', '100')

    const res = await fetch(`http://localhost:8080/api/v1/production/tasks?${params.toString()}`)
    if (!res.ok) throw new Error(`服务器异常: ${res.status}`)

    const data = await res.json()
    tasks.value = data.content.map(task => {
      const planCode = reversePlanMap.value[task.planId] || ''
      const deviceCode = Object.keys(deviceMap.value).find(code => deviceMap.value[code] === task.deviceId) || ''
      return {
        ...task,
        taskId: `TASK-${String(task.id).padStart(4, '0')}`,
        planCode,
        deviceCode
      }
    })
  } catch (err) {
    console.error('filterTasks 错误:', err)
    ElMessage.error(`获取任务失败: ${err.message}`)
  }
}

// 打开创建弹窗
const openCreateDialog = () => {
  isEditing.value = false
  dialogVisible.value = true
  form.value = {
    taskId: '',
    planCode: '',
    processType: '',
    deviceCode: '',
    quantity: 100,
    status: '待下发'
  }
}

// 编辑任务：打开弹窗并填充数据
const editTask = (task) => {
  form.value = {
    taskId: task.taskId || '',
    planCode: task.planCode || '',
    processType: task.processType || '',
    deviceCode: task.deviceCode || '',
    quantity: task.quantity || 1,
    status: task.status || '待下发'
  }
  isEditing.value = true
  dialogVisible.value = true
}

// 提交任务（创建或更新）
const submitTask = async (data) => {
  try {
    const planId = planMap.value[data.planCode]
    const deviceId = deviceMap.value[data.deviceCode]
    if (!planId || !deviceId) return ElMessage.error('请选择有效的计划或设备编号')

    let res
    if (isEditing.value) {
      // 编辑更新任务（假设用PUT）
      const taskIdNum = parseInt(data.taskId?.replace('TASK-', '')) || 0
      if (!taskIdNum) return ElMessage.error('任务ID无效，无法更新')

      res = await fetch(`http://localhost:8080/api/v1/production/tasks/${taskIdNum}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          planId,
          deviceId,
          processType: data.processType,
          quantity: data.quantity,
          status: data.status
        })
      })
    } else {
      // 新建任务
      res = await fetch(`http://localhost:8080/api/v1/production/tasks?planId=${planId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          deviceId,
          processType: data.processType,
          quantity: data.quantity,
          status: data.status
        })
      })
    }

    if (!res.ok) throw new Error(isEditing.value ? '更新失败' : '创建失败')

    const task = await res.json()

    if (isEditing.value) {
      // 更新tasks列表中对应任务
      const index = tasks.value.findIndex(t => t.taskId === data.taskId)
      if (index >= 0) {
        tasks.value[index] = {
          ...task,
          taskId: data.taskId,
          planCode: data.planCode,
          deviceCode: data.deviceCode,
          status: data.status
        }
      }
      ElMessage.success('任务更新成功')
    } else {
      tasks.value.push({
        ...task,
        taskId: `TASK-${String(task.id).padStart(4, '0')}`,
        planCode: data.planCode,
        deviceCode: data.deviceCode,
        status: data.status || '待下发'
      })
      ElMessage.success('任务创建成功')
    }

    dialogVisible.value = false
  } catch (err) {
    console.error(err)
    ElMessage.error(isEditing.value ? '任务更新失败' : '任务提交失败')
  }
}

// 删除任务
const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务 ${task.taskId} 吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const id = parseInt(task.taskId.replace('TASK-', ''))
    const res = await fetch(`http://localhost:8080/api/v1/production/tasks/${id}`, { method: 'DELETE' })
    if (!res.ok) throw new Error('删除失败')

    tasks.value = tasks.value.filter(t => t.taskId !== task.taskId)
    ElMessage.success('任务删除成功')
  } catch (err) {
    if (err !== 'cancel') {
      console.error('删除失败:', err)
      ElMessage.error('删除失败')
    }
  }
}

// 载入计划数据
const loadPlans = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/v1/production/plans?page=0&size=100')
    if (!res.ok) throw new Error('获取计划失败')
    const data = await res.json()

    planMap.value = {}
    reversePlanMap.value = {}
    planOptions.value = []

    data.content.forEach(plan => {
      const cleanCode = plan.planCode.replace(/[^\w-]/g, '')
      planMap.value[cleanCode] = plan.id
      reversePlanMap.value[plan.id] = plan.planCode
      planOptions.value.push({ label: plan.planCode, value: cleanCode })
    })
  } catch (err) {
    console.error(err)
    ElMessage.error(`加载计划失败: ${err.message}`)
  }
}

// 载入设备数据
const loadDevices = async () => {
  try {
    const res = await fetch('http://localhost:8080/api/v1/equipment/devices?page=0&size=100')
    if (!res.ok) throw new Error('获取设备失败')
    const data = await res.json()

    deviceMap.value = {}
    deviceOptions.value = []

    data.content.forEach(device => {
      deviceMap.value[device.deviceCode] = device.id
      deviceOptions.value.push(device.deviceCode)
    })
  } catch (err) {
    console.error(err)
    ElMessage.error(`加载设备失败: ${err.message}`)
  }
}

onMounted(async () => {
  try {
    await loadPlans()
    await loadDevices()
    await filterTasks()
  } catch (err) {
    console.error('初始化失败:', err)
    ElMessage.error(`初始化失败: ${err.message}`)
  }
})
</script>
