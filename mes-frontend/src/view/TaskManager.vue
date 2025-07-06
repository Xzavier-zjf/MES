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
import { getTasks, createTask, updateTask, deleteTask } from '@/api/tasks'
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
        productName: plan.productName // 添加productionName到选项
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

    // 只在新建任务时检查设备状态
    if (!isEditing.value) {
      // 1️⃣ 获取设备状态
      const device = await getDeviceById(deviceId)

      // 2️⃣ 校验设备是否空闲
      if (device.status !== '空闲') {
        return ElMessage.error(`设备当前状态为 ${device.status}，无法分配任务`)
      }

      // 3️⃣ 校验设备类型与工序类型是否匹配
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


onMounted(async () => {
  try {
    await loadPlans()
    await loadDevices()
    await filterTasks()
    
    // 启动自动刷新
    appStore.startAutoRefresh(30000) // 30秒刷新一次
  } catch (err) {
    console.error('初始化失败:', err)
    ElMessage.error(`初始化失败: ${err.message}`)
  }
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})
</script>
<style scoped>
.task-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>