import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 生产计划数据
  const plans = ref([])
  const totalPlans = computed(() => plans.value.length)
  const inProgressPlans = computed(() => plans.value.filter(p => p.status === '进行中').length)
  const pendingPlans = computed(() => plans.value.filter(p => p.status === '待下发').length)
  
  // 任务数据
  const tasks = ref([])
  const inProgressTasks = computed(() => tasks.value.filter(t => t.status === '进行中').length)
  const completedTasks = computed(() => tasks.value.filter(t => t.status === '已完成').length)
  
  // 设备数据
  const devices = ref([])
  const pendingDevices = computed(() => devices.value.filter(d => d.status === '故障').length)
  const runningDevices = computed(() => devices.value.filter(d => d.status === '运行中').length)
  
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
      const [plansRes, tasksRes, devicesRes] = await Promise.all([
        fetch('http://localhost:8080/api/v1/production/plans'),
        fetch('http://localhost:8080/api/v1/production/tasks'),
        fetch('http://localhost:8080/api/v1/equipment/devices')
      ])
      
      if (!plansRes.ok || !tasksRes.ok || !devicesRes.ok) {
        throw new Error('获取数据失败')
      }
      
      const plans = await plansRes.json()
      const tasks = await tasksRes.json()
      const devices = await devicesRes.json()
      
      // 更新store状态
      updatePlans(plans.content || plans)
      updateTasks(tasks.content || tasks)
      updateDevices(devices.content || devices)
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
    inProgressTasks,
    completedTasks,
    pendingDevices,
    runningDevices,
    updatePlans,
    updateTasks,

    updateDevices,
    fetchAllData
  }
})
