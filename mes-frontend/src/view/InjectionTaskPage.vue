<template>
  <div class="injection-page">
 <HeaderSection 
    title=" 🛠&nbsp注塑工艺参数"
    subtitle="
      管理注塑工艺参数，为注塑工艺提供参数。
      "
    :showStats="false"
    :value1="totalTasks"
    :value2="inProgressTasks"
    :value3="completedTasks"
    :value4="pendingTasks"/>


    <!-- 过滤器组件 -->
    <el-card shadow="hover" class="table-card" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="计划编号">
          <el-input v-model="filters.planId" placeholder="请输入计划编号" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="filters.taskId" placeholder="请输入任务编号" />
        </el-form-item>

        <!-- <el-form-item label="工序类型">
          <el-select v-model="filters.processType" placeholder="请选择工序类型" clearable>
            <el-option label="注塑" value="注塑" />
          </el-select>
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" @click="submitFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="filteredTasks" border style="width: 100%">
        <el-table-column prop="planId" label="计划编号" width="180">
          <template #default="{row}">
            {{ planMap[row.planId] || row.planId }}
          </template>
        </el-table-column>
        <el-table-column prop="taskId" label="任务编号">
          <template #default="{row}">
            {{ taskMap[row.taskId] || row.taskId }}
          </template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备编号">
          <template #default="{row}">
            {{ deviceMap[row.deviceId] || row.deviceId }}
          </template>
        </el-table-column>
        <el-table-column label="任务数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag >{{ row.quantity }} 件</el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="getProgress(row.status)" :color="progressColor(row.status)" />
          </template>
        </el-table-column> -->
        <el-table-column prop="pressure" label="注塑压力 (MPa)" />
        <el-table-column prop="injectionSpeed" label="注塑速度 (mm/s)" />
        <el-table-column prop="holdTime" label="保压时间 (s)" />
        <el-table-column prop="coolingTime" label="冷却时间 (s)" />
        <el-table-column prop="moldTemperature" label="模具温度 (℃)" />
        <el-table-column prop="materialTemperature" label="料筒温度 (℃)" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">录入工艺</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页组件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      />
    </el-card>

    <!-- 参数录入弹窗 -->
    <el-dialog v-model="dialogVisible" title="录入注塑工艺参数" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="注塑压力">
          <el-input-number v-model="form.pressure" :min="0" />
        </el-form-item>
        <el-form-item label="注塑速度">
          <el-input-number v-model="form.injectionSpeed" :min="0" />
        </el-form-item>
        <el-form-item label="冷却时间">
          <el-input-number v-model="form.coolingTime" :min="0" />
        </el-form-item>
        <el-form-item label="保压时间">
          <el-input-number v-model="form.holdTime" :min="0" />
        </el-form-item>
        <el-form-item label="模具温度">
          <el-input-number v-model="form.moldTemperature" :min="0" />
        </el-form-item>
        <el-form-item label="料筒温度">
          <el-input-number v-model="form.materialTemperature" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitParams">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import HeaderSection from '@/components/HeaderSection.vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { getInjectionParams, updateInjectionParam } from '@/api/injection'
import { getPlans } from '@/api/plans'
import { getTasks } from '@/api/tasks'
import { getDevices } from '@/api/devices'
import { useAppStore } from '@/stores'

const route = useRoute()
const appStore = useAppStore()

// 任务列表
const tasks = ref([])
// 当前页码
const currentPage = ref(1)
// 每页显示数量
const pageSize = ref(20)
// 总记录数
const total = ref(0)

// 过滤条件
const filters = ref({
  taskId: '',
  planId: '',
  processType: ''
})

// 过滤后的任务列表
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const taskIdStr = task.taskId != null ? String(task.taskId) : '';
    const planIdStr = task.planId != null ? String(task.planId) : '';
    
    // 计划编号筛选 - 支持模糊匹配
    const planMatch = !filters.value.planId || 
      planIdStr.toLowerCase().includes(filters.value.planId.toLowerCase()) ||
      (planMap.value[task.planId] && planMap.value[task.planId].toLowerCase().includes(filters.value.planId.toLowerCase()));
    
    // 任务编号筛选 - 支持模糊匹配
    const taskMatch = !filters.value.taskId || 
      taskIdStr.toLowerCase().includes(filters.value.taskId.toLowerCase()) ||
      (taskMap.value[task.taskId] && taskMap.value[task.taskId].toLowerCase().includes(filters.value.taskId.toLowerCase()));
    
    return planMatch && taskMatch;
  })
})

const dialogVisible = ref(false)
const form = ref({})

const navItems = [
  { name: '首页', path: '/home' },
  { name: '生产计划管理', path: '/plan' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑参数', path: '/injection' },
  { name: '图案参数', path: '/pattern' },
]

const openDialog = (task) => {
  form.value = { ...task }
  dialogVisible.value = true
}

// 更新注塑参数
const updateInjectionParamLocal = async (id, updateDTO) => {
  try {
    const response = await updateInjectionParam(id, updateDTO)
    return response
  } catch (error) {
    console.error('更新注塑参数失败:', error)
    throw error
  }
}

const submitParams = async () => {
  if (!form.value.id) {
    console.error('缺少参数 ID，无法更新')
    dialogVisible.value = false
    return
  }
  try {
    // 提取需要更新的数据
    const updateDTO = {
      id: form.value.id,
      planId: form.value.planId,
      taskId: form.value.taskId,
      deviceId: form.value.deviceId,
      quantity: form.value.quantity, // 确保包含quantity字段
      pressure: form.value.pressure,
      injectionSpeed: form.value.injectionSpeed,
      coolingTime: form.value.coolingTime,
      holdTime: form.value.holdTime,
      moldTemperature: form.value.moldTemperature,
      materialTemperature: form.value.materialTemperature
    }
    // 调用更新接口
    const updatedData = await updateInjectionParamLocal(form.value.id, updateDTO)
    // 更新本地任务列表
    const index = tasks.value.findIndex(t => t.id === form.value.id)
    if (index !== -1) {
      tasks.value[index] = { ...tasks.value[index], ...updatedData }
    }
    dialogVisible.value = false
  } catch (error) {
    // 可添加错误提示
  }
}

// 提交过滤
const submitFilter = () => {
  console.log('执行筛选，条件:', filters.value)
  // 过滤逻辑已在 computed 中实现，这里可以添加额外的筛选逻辑
}

// 重置过滤
const resetFilter = () => {
  filters.value = {
    taskId: '',
    planId: '',
    processType: ''
  }
  console.log('重置筛选条件')
}

// 获取注塑参数列表
const fetchInjectionParams = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'id,desc'
    }
    const response = await getInjectionParams(params)
    tasks.value = response.content
    total.value = response.totalElements
  } catch (error) {
    console.error('获取注塑参数列表失败:', error)
  }
}

// 页码变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchInjectionParams()
}

// 每页数量变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchInjectionParams()
}

// 组件挂载时获取数据
// 添加映射关系
const planMap = ref({})
const taskMap = ref({})
const deviceMap = ref({})

// 获取计划映射
const loadPlanMap = async () => {
  try {
    const response = await getPlans(0, 1000)
    planMap.value = {}
    response.content.forEach(plan => {
      planMap.value[plan.id] = plan.planCode
    })
  } catch (error) {
    console.error('获取计划映射失败:', error)
  }
}

// 获取任务映射
const loadTaskMap = async () => {
  try {
    const response = await getTasks({ page: 0, size: 1000 })
    taskMap.value = {}
    response.content.forEach(task => {
      taskMap.value[task.id] = task.taskCode
    })
  } catch (error) {
    console.error('获取任务映射失败:', error)
  }
}

// 获取设备映射
const loadDeviceMap = async () => {
  try {
    const response = await getDevices({ page: 0, size: 1000 })
    deviceMap.value = {}
    response.content.forEach(device => {
      deviceMap.value[device.id] = device.deviceCode
    })
  } catch (error) {
    console.error('获取设备映射失败:', error)
  }
}

// 在onMounted中添加这些调用
onMounted(async () => {
  await fetchInjectionParams()
  await loadPlanMap()
  await loadTaskMap()
  await loadDeviceMap()
  
  // 启动自动刷新
  appStore.startAutoRefresh(30000) // 30秒刷新一次
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})
</script>

<style scoped>
.injection-page {
  padding: 20px;
  background: #f5f7fa;
}

/* 顶部卡片 */
.header-card {
  margin-bottom: 20px;
  padding: 16px 40px 20px 40px;
  border-radius: 52px;
  background-color: #ecf5ff;
  border-left: 5px solid #409EFF;
}

/* 顶部卡片内容布局，左右标题和导航 */
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 左侧标题 */
.left-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}
.left-title .icon {
  font-size: 26px;
}

/* 右侧导航 */
.nav-buttons {
  display: flex;
  gap: 24px;
}
.nav-link {
  font-size: 16px;
  color: #666;
  font-weight: 500;
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color 0.3s, color 0.3s;
  cursor: pointer;
}
.nav-link:hover {
  background-color: #e0e7ff;
  color: #2563eb;
}
.nav-link.active {
  background-color: #2563eb;
  color: #fff;
  font-weight: 700;
}

/* 说明文字 */
.header-desc {
  margin-top: 8px;
  font-size: 14px;
  color: #555;
  text-align: center;
}

/* 表格卡片 */
.table-card {
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}

/* 过滤表单 */
.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 分页组件 */
.el-pagination {
  margin-top: 16px;
  text-align: right;
}
</style>