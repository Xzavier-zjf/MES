import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPlans } from '@/api/plans'
import { getTasks } from '@/api/tasks'
import { getDevices } from '@/api/devices'

export const useAppStore = defineStore('app', () => {
  // 生产计划数据
  const plans = ref([])
  const totalPlans = computed(() => plans.value.length)
  const inProgressPlans = computed(() => plans.value.filter(p => p.status === '进行中').length)
  const pendingPlans = computed(() => plans.value.filter(p => p.status === '待下发').length)
  const completedPlans = computed(() => plans.value.filter(p => p.status === '已完成').length)

  // 任务数据
  const tasks = ref([])
  const totalTasks = computed(() => tasks.value.length)
  const inProgressTasks = computed(() => tasks.value.filter(t => t.status === '进行中').length)
  const completedTasks = computed(() => tasks.value.filter(t => t.status === '已完成').length)
  const pendingTasks = computed(() => tasks.value.filter(t => t.status === '待下发').length)
  
  // 基于数量的任务统计
  const totalTaskQuantity = computed(() => 
    tasks.value.reduce((sum, task) => sum + (task.quantity || 0), 0)
  )
  const completedTaskQuantity = computed(() => 
    tasks.value
      .filter(t => t.status === '已完成')
      .reduce((sum, task) => sum + (task.quantity || 0), 0)
  )
  const inProgressTaskQuantity = computed(() => 
    tasks.value
      .filter(t => t.status === '进行中')
      .reduce((sum, task) => sum + (task.quantity || 0), 0)
  )
  const pendingTaskQuantity = computed(() => 
    tasks.value
      .filter(t => t.status === '待下发')
      .reduce((sum, task) => sum + (task.quantity || 0), 0)
  )
  
  // 设备数据
  const devices = ref([])
  const totalDevices = computed(() => devices.value.length)
  const pendingDevices = computed(() => devices.value.filter(d => d.status === '故障').length)
  const runningDevices = computed(() => devices.value.filter(d => d.status === '运行中').length)
  const idlingDevices = computed(() => devices.value.filter(d => d.status === '空闲').length)
  const devicesName = computed(() => devices.value.map(d => d.name))  // 新增设备名称列表
  const runtimeMinutes = computed(() => devices.value.map(d => d.runtimeMinutes))  // 新增设备运行时间（分钟）

  // 更新方法
  const updatePlans = (newPlans) => {
    plans.value = newPlans
  }
  
  const updateTasks = (newTasks) => {
    tasks.value = newTasks
  }
  
  const updateDevices = (newDevices) => {
    devices.value = newDevices
  }
  
  // 实时更新方法
  const refreshPlans = async () => {
    try {
      const data = await getPlans(0, 1000)
      updatePlans(data.content || data)
    } catch (error) {
      console.error('刷新计划数据失败:', error)
    }
  }
  
  const refreshTasks = async () => {
    try {
      const data = await getTasks({ page: 0, size: 1000 })
      updateTasks(data.content || data)
    } catch (error) {
      console.error('刷新任务数据失败:', error)
    }
  }
  
  const refreshDevices = async () => {
    try {
      const data = await getDevices({ page: 0, size: 1000 })
      updateDevices(data.content || data)
    } catch (error) {
      console.error('刷新设备数据失败:', error)
    }
  }
  
  const refreshDeviceStats = async () => {
    try {
      const data = await getDevices({ page: 0, size: 1000 })
      updateDevices(data.content || data)
    } catch (error) {
      console.error('刷新设备统计数据失败:', error)
    }
  }
  
  const fetchAllData = async () => {
    try {
      // 并行获取所有数据
      const [plansData, tasksData, devicesData] = await Promise.all([
        getPlans(0, 1000),
        getTasks({ page: 0, size: 1000 }),
        getDevices({ page: 0, size: 1000 })
      ])
      
      // 更新store状态
      updatePlans(plansData.content || plansData)
      updateTasks(tasksData.content || tasksData)
      updateDevices(devicesData.content || devicesData)
    } catch (error) {
      console.error('获取数据失败:', error)
    }
  }
  
  // 定时刷新机制
  let refreshInterval = null
  
  const startAutoRefresh = (intervalMs = 30000) => { // 默认30秒刷新一次
    if (refreshInterval) {
      clearInterval(refreshInterval)
    }
    refreshInterval = setInterval(() => {
      fetchAllData()
    }, intervalMs)
  }
  
  const stopAutoRefresh = () => {
    if (refreshInterval) {
      clearInterval(refreshInterval)
      refreshInterval = null
    }
  }
  
  return {
    // 数据
    plans,
    tasks,
    devices,
    
    // 计划统计
    totalPlans,
    inProgressPlans,
    pendingPlans,
    completedPlans,
    
    // 任务统计
    totalTasks,
    inProgressTasks,
    completedTasks,
    pendingTasks,
    
    // 基于数量的任务统计
    totalTaskQuantity,
    completedTaskQuantity,
    inProgressTaskQuantity,
    pendingTaskQuantity,
    
    // 设备统计
    totalDevices,
    pendingDevices,
    runningDevices,
    idlingDevices,
    devicesName,
    runtimeMinutes,
    
    // 更新方法
    updatePlans,
    updateTasks,
    updateDevices,
    refreshPlans,
    refreshTasks,
    refreshDevices,
    refreshDeviceStats,
    fetchAllData,
    
    // 自动刷新
    startAutoRefresh,
    stopAutoRefresh
  }
})
