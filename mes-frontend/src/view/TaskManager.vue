<template>
  <div class="task-manager">
    <HeaderSection 
    title="任务管理"
    subtitle="可查看跟踪生产任务执行状态，能及时调整、分配资源，确保任务按时按质完成。
      "
    :showStats="true"
    :value1="totalTasks"
    :value2="inProgressTasks"
    :value3="completedTasks"
    :value4="pendingTasks"/>

    <TaskFilter
      v-model:filters="filters"
      @filter="filterTasks"
      @reset="resetFilters"
      @create="openCreateDialog"
    />

    <TaskTable
      :tasks="tasks"
      @edit="editTask"
      @delete="handleDeleteTask"
      @suggest-status-update="handleStatusSuggestion"
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import HeaderSection from '@/components/HeaderSection.vue'
import TaskFilter from '@/components/TaskFilter.vue'
import TaskTable from '@/components/TaskTable.vue'
import TaskDialog from '@/components/TaskDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasks, createTask, updateTask, deleteTask, updateTaskStatus } from '@/api/tasks'
import { getPlans } from '@/api/plans'
import { getDevices, updateDeviceStatus, getDeviceById } from '@/api/devices'
import { useAppStore } from '@/stores'

const filters = ref({
  planCode: '',
  processType: '',
  status: ''
})

const tasks = ref([])
const planOptions = ref([])
const planMap = ref({})
const reversePlanMap = ref({})
const appStore = useAppStore()

const processTypes = ['注塑', '印刷', '组装', '质检', '包装']
const deviceOptions = ref([])
const deviceMap = ref({})


// 对话框状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const form = ref({
  id: '',  // 修改taskId为id
  taskCode: '', // 新增taskCode字段
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

// 使用store的统计数据
const totalTasks = computed(() => appStore.totalTasks)
const inProgressTasks = computed(() => appStore.inProgressTasks)
const completedTasks = computed(() => appStore.completedTasks)
const pendingTasks = computed(() => appStore.pendingTasks)
// 获取任务列表
const filterTasks = async () => {
  try {
    const params = {
      planCode: filters.value.planCode?.trim(),
      processType: filters.value.processType?.trim(),
      status: filters.value.status?.trim(),
      page: 0,
      size: 100
    }

    const data = await getTasks(params)
    
    if (!data || !data.content) {
      console.warn('任务数据格式异常:', data)
      tasks.value = []
      return
    }
    
    tasks.value = data.content.map(task => {
      const planCode = reversePlanMap.value[task.planId] || ''
      const deviceCode = Object.keys(deviceMap.value).find(code => deviceMap.value[code] === task.deviceId) || ''
      const planOption = planOptions.value.find(p => p.value === reversePlanMap.value[task.planId])
      return {
        ...task,
        id: task.id,
        taskCode: task.taskCode,
        planCode,
        deviceCode,
        productName: planOption?.productName || ''
      }
    })
  } catch (err) {
    console.error('filterTasks 错误:', err)
    ElMessage.error(`获取任务失败: ${err.message}`)
    tasks.value = []
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
    id: task.id,  // 确保传递id字段
    taskCode: task.taskCode || '',
    planCode: task.planCode || '',
    processType: task.processType || '',
    deviceCode: task.deviceCode || '',
    quantity: task.quantity || 1,
    status: task.status || '待下发'
  }
  isEditing.value = true
  dialogVisible.value = true
}


// 删除任务
const handleDeleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要删除任务 ${task.taskCode} 吗？`, '删除确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await deleteTask(task.id)
    tasks.value = tasks.value.filter(t => t.id !== task.id)
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
    const data = await getPlans(0, 100)

    planMap.value = {}
    reversePlanMap.value = {}
    planOptions.value = []

    data.content.forEach(plan => {
      const cleanCode = plan.planCode.replace(/[^\w-]/g, '')
      planMap.value[cleanCode] = plan.id
      reversePlanMap.value[plan.id] = plan.planCode
      planOptions.value.push({ 
        label: plan.planCode, 
        value: cleanCode,
        productName: plan.productName, // 添加productName到选项
        totalQuantity: plan.totalQuantity, // ✅ 添加总数量信息
        id: plan.id // ✅ 添加计划ID
      })
    })
  } catch (err) {
    console.error(err)
    ElMessage.error(`加载计划失败: ${err.message}`)
  }
}

// 载入设备数据
const loadDevices = async () => {
  try {
    const data = await getDevices({ page: 0, size: 100 })

    deviceMap.value = {}
    deviceOptions.value = []

    data.content.forEach(device => {
      deviceMap.value[device.deviceCode] = device.id
      // 编辑时显示所有设备，新建时只显示空闲设备
      deviceOptions.value.push(device.deviceCode)
    })
  } catch (err) {
    console.error(err)
    ElMessage.error(`加载设备失败: ${err.message}`)
  }
}
//设备状态转换

// 在提交任务时检查设备状态
const submitTask = async (data) => {
  try {
    const planId = planMap.value[data.planCode]
    const deviceId = deviceMap.value[data.deviceCode]

    if (!planId || !deviceId) {
      return ElMessage.error('请选择有效的计划或设备编号')
    }

    // 只在新建任务时检查设备状态和数量
    if (!isEditing.value) {
      // 1️⃣ 验证任务数量是否合理
      const relatedPlan = planOptions.value.find(p => p.value === data.planCode)
      if (relatedPlan && relatedPlan.totalQuantity) {
        const relatedTasks = tasks.value.filter(t => t.planCode === data.planCode)
        const currentTaskTotal = relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
        const afterAddTotal = currentTaskTotal + data.quantity
        
        if (afterAddTotal > relatedPlan.totalQuantity) {
          return ElMessage.error(
            `任务数量超限！计划总数量: ${relatedPlan.totalQuantity}，` +
            `已分配: ${currentTaskTotal}，本次添加: ${data.quantity}，` +
            `可用数量: ${relatedPlan.totalQuantity - currentTaskTotal}`
          )
        }
        
        // 提示剩余可分配数量
        const remaining = relatedPlan.totalQuantity - afterAddTotal
        if (remaining > 0) {
          console.log(`任务创建后，计划 ${data.planCode} 还有 ${remaining} 个未分配`)
        }
      }

      // 2️⃣ 获取设备状态
      const device = await getDeviceById(deviceId)

      // 3️⃣ 校验设备是否空闲
      if (device.status !== '空闲') {
        return ElMessage.error(`设备当前状态为 ${device.status}，无法分配任务`)
      }

      // 4️⃣ 校验设备类型与工序类型是否匹配
      const processToDeviceType = {
        '注塑': 'INJ',
        '印刷': 'PRT',
        '组装': 'ASM',
        '质检': 'QC',
        '包装': 'PKG'
      }
      const expectedPrefix = processToDeviceType[data.processType]
      if (expectedPrefix && !device.deviceCode.startsWith(expectedPrefix)) {
        return ElMessage.error(`设备类型不匹配，${data.processType} 工序需要使用 ${expectedPrefix} 开头的设备`)
      }
    }

    if (isEditing.value) {
      // ✅ 编辑任务
      if (!data.id) return ElMessage.error('任务ID无效，无法更新')

      const taskData = {
        id: data.id,
        taskCode: data.taskCode,
        planId,
        deviceId,
        processType: data.processType,
        quantity: data.quantity,
        status: data.status
      }
      // 可选：打印payload便于调试
      console.log('编辑任务payload:', taskData)
      try {
        await updateTask(data.id, taskData)
        ElMessage.success('任务更新成功')
        dialogVisible.value = false
        await filterTasks()
      } catch (err) {
        ElMessage.error('任务更新失败: ' + (err.message || '未知错误'))
        console.error('任务更新失败详细信息:', err)
      }
    } else {
      // ✅ 新建任务
      const taskData = {
        taskCode: data.taskCode,
        planId,
        deviceId,
        processType: data.processType,
        quantity: data.quantity,
        status: data.status
      }
      // 可选：打印payload便于调试
      console.log('新建任务payload:', taskData)
      try {
        const task = await createTask(taskData)
        tasks.value.push({
          ...task,
          taskCode: data.taskCode,
          planCode: data.planCode,
          deviceCode: data.deviceCode,
          status: data.status || '待下发'
        })
        // ✅ 4️⃣ 创建成功后更新设备状态为“运行中”
        try {
          await updateDeviceStatus(deviceId, '运行中')
          ElMessage.success('任务创建成功，设备状态已更新为运行中')
          // 更新本地设备列表中的设备状态
          const deviceIndex = deviceOptions.value.findIndex(d => d.value === deviceId)
          if (deviceIndex !== -1) {
            deviceOptions.value[deviceIndex].status = '运行中'
          }
        } catch (error) {
          ElMessage.warning('任务创建成功，但设备状态更新失败，请手动检查设备状态')
          console.error('设备状态更新失败:', error)
        }
        dialogVisible.value = false
        await filterTasks()
      } catch (err) {
        ElMessage.error('任务提交失败: ' + (err.message || '未知错误'))
        console.error('任务提交失败详细信息:', err)
      }
    }

  } catch (err) {
    console.error(err)
    ElMessage.error(isEditing.value ? '任务更新失败' : '任务提交失败')
  }
}

// 处理任务数量更新事件
const handleTaskQuantityUpdate = async (event) => {
  const { taskId, newQuantity, source } = event.detail
  
  console.log('任务管理页面接收到数量更新事件:', { taskId, newQuantity, source })
  
  try {
    // 更新本地任务数据
    const taskIndex = tasks.value.findIndex(t => t.id === taskId)
    if (taskIndex !== -1) {
      tasks.value[taskIndex].quantity = newQuantity
      console.log('本地任务数据已更新:', tasks.value[taskIndex])
      
      // 检查是否需要状态更新
      await checkTaskStatusAfterUpdate(tasks.value[taskIndex])
    }
    
    // 如果需要，重新获取任务列表以确保数据一致性
    if (source === 'injection-params') {
      // 延迟一点时间再刷新，确保后端数据已更新
      setTimeout(async () => {
        await filterTasks()
        console.log('任务列表已刷新')
      }, 1000)
    }
    
  } catch (error) {
    console.error('处理任务数量更新失败:', error)
  }
}

// 计算建议的状态
const getSuggestedStatus = (task) => {
  const { status, quantity, completedQuantity = 0 } = task
  
  if (completedQuantity >= quantity && quantity > 0) {
    return '已完成'
  }
  
  if (completedQuantity > 0 && completedQuantity < quantity && status !== '已暂停') {
    return '进行中'
  }
  
  if (completedQuantity === 0) {
    return status === '已暂停' ? '已暂停' : '待下发'
  }
  
  return status
}

// 获取状态更新原因
const getStatusUpdateReason = (task, suggestedStatus) => {
  const { quantity, completedQuantity = 0 } = task
  
  if (suggestedStatus === '已完成') {
    return `完成数量 ${completedQuantity} 已达到任务数量 ${quantity}`
  }
  
  if (suggestedStatus === '进行中') {
    return `已有生产进度 ${completedQuantity}/${quantity}`
  }
  
  if (suggestedStatus === '待下发') {
    return '尚未开始生产'
  }
  
  return '基于当前进度的建议'
}

// 处理状态建议
const handleStatusSuggestion = async (task) => {
  const suggestedStatus = getSuggestedStatus(task)
  const reason = getStatusUpdateReason(task, suggestedStatus)
  
  try {
    const result = await ElMessageBox.confirm(
      `建议将任务 ${task.taskCode} 的状态从 "${task.status}" 更新为 "${suggestedStatus}"？\n\n` +
      `原因：${reason}`,
      '状态更新建议',
      {
        confirmButtonText: '更新状态',
        cancelButtonText: '保持现状',
        type: 'info'
      }
    )
    
    if (result) {
      await updateTaskStatus(task.id, suggestedStatus, 'user-accepted-suggestion')
      await filterTasks() // 刷新任务列表
      ElMessage.success('任务状态更新成功')
      
      // 发送状态更新事件
      window.dispatchEvent(new CustomEvent('task-status-updated', {
        detail: { 
          taskId: task.id, 
          newStatus: suggestedStatus, 
          oldStatus: task.status,
          timestamp: Date.now(),
          source: 'task-manager'
        }
      }))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态更新失败:', error)
      ElMessage.error('状态更新失败: ' + error.message)
    }
  }
}

// 数量更新后检查状态
const checkTaskStatusAfterUpdate = async (task) => {
  const suggestedStatus = getSuggestedStatus(task)
  
  // 自动更新的条件：完成数量达到100%时自动设为已完成
  if (suggestedStatus === '已完成' && task.completedQuantity >= task.quantity) {
    try {
      await updateTaskStatus(task.id, suggestedStatus, 'auto-update-on-completion')
      ElMessage.success(`任务 ${task.taskCode} 已自动设为已完成状态`)
      
      // 发送状态更新事件
      window.dispatchEvent(new CustomEvent('task-status-updated', {
        detail: { 
          taskId: task.id, 
          newStatus: suggestedStatus, 
          oldStatus: task.status,
          timestamp: Date.now(),
          source: 'auto-update'
        }
      }))
      
      // 刷新任务列表
      setTimeout(() => filterTasks(), 500)
    } catch (error) {
      console.error('自动状态更新失败:', error)
    }
  }
}


onMounted(async () => {
  try {
    await loadPlans()
    await loadDevices()
    await filterTasks()
    
    // 启动自动刷新
    appStore.startAutoRefresh(30000) // 30秒刷新一次
    
    // 监听任务数量更新事件
    window.addEventListener('task-quantity-updated', handleTaskQuantityUpdate)
  } catch (err) {
    console.error('初始化失败:', err)
    ElMessage.error(`初始化失败: ${err.message}`)
  }
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
  
  // 移除事件监听
  window.removeEventListener('task-quantity-updated', handleTaskQuantityUpdate)
})
</script>
<style scoped>
.task-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>