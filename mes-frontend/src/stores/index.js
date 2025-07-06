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
  const inProgressTasks = computed(() => tasks.value.filter(t => t.status === '进行中').length)
  const completedTasks = computed(() => tasks.value.filter(t => t.status === '已完成').length)
  
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
  
  return {
    plans,
    tasks,
    devices,
    totalPlans,
    inProgressPlans,
    pendingPlans,
    completedPlans,
    inProgressTasks,
    completedTasks,
    totalDevices,
    pendingDevices,
    runningDevices,
    idlingDevices,
    updatePlans,
    updateTasks,
    devicesName,
    runtimeMinutes,
    updateDevices,
    fetchAllData
  }
})
