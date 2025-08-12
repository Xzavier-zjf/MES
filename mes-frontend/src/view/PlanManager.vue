<template>
  <div class="plan-manager">
    <HeaderSection title="生产计划管理" subtitle="用于制定手机壳生产计划，依订单、库存等安排任务，保障生产顺利推进。
      " :showStats="true" :card1="props.card1" :card4="props.card4" :value1="totalPlans" :value2="inProgress"
      :value3="completed" :value4="pending" />

    <!-- 调试面板 -->
    <DataDebugPanel :plans="plans" :tasks="tasks" v-if="showDebugPanel" />

    <!-- 中间过滤器区域 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-container">
        <el-input v-model="filter.keyword" placeholder="输入计划编号或产品名称" clearable style="width: 220px" />
        <el-select v-model="filter.priority" placeholder="优先级" clearable style="width: 120px">
          <el-option label="低" value="3" />
          <el-option label="中" value="2" />
          <el-option label="高" value="1" />
        </el-select>
        <el-select v-model="filter.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待下发" value="待下发" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
        <el-button type="primary" @click="openDialog">添加计划</el-button>
        <el-button type="info" @click="checkDataConsistency">数据一致性检查</el-button>
        <el-button type="success" @click="showDebugPanel = !showDebugPanel">
          {{ showDebugPanel ? '隐藏' : '显示' }}调试信息
        </el-button>
        <el-button type="warning" @click="showSmartAllocation">智能分配建议</el-button>
      </div>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="filteredPlans" style="width: 100%" border size="large">
        <el-table-column prop="planCode" label="计划编号" width="180" />
        <el-table-column prop="productName" label="产品名称" width="200" />
        <el-table-column prop="totalQuantity" label="生产数量" width="120" />
        <el-table-column label="生产进度" width="220">
          <template #default="scope">
            <div style="font-size: 11px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span style="color: #409EFF;">总量: {{ scope.row.totalQuantity }}</span>
                <el-button 
                  size="small" 
                  type="text" 
                  @click="showDetailedProgress(scope.row)"
                  style="padding: 0; font-size: 10px;"
                >
                  详情
                </el-button>
              </div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span style="color: #67C23A;">已完成: {{ getCompletedQuantity(scope.row.id) }}</span>
                <span style="color: #E6A23C;">进行中: {{ getInProgressQuantity(scope.row.id) }}</span>
              </div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                <span style="color: #909399;">已分配: {{ getTaskAllocation(scope.row.id) }}</span>
                <span :style="{ color: getRemainingColor(scope.row.totalQuantity, getCompletedQuantity(scope.row.id)) }">
                  实际剩余: {{ getActualRemaining(scope.row.id, scope.row.totalQuantity) }}
                </span>
              </div>
              <!-- 完成进度条 -->
              <div style="width: 100%; height: 3px; background: #f0f0f0; border-radius: 2px; overflow: hidden; margin-bottom: 2px;">
                <div 
                  :style="{ 
                    width: getCompletionPercent(scope.row.totalQuantity, getCompletedQuantity(scope.row.id)) + '%',
                    height: '100%',
                    background: '#67C23A',
                    transition: 'width 0.3s ease'
                  }"
                ></div>
              </div>
              <!-- 分配进度条 -->
              <div style="width: 100%; height: 2px; background: #f0f0f0; border-radius: 1px; overflow: hidden;">
                <div 
                  :style="{ 
                    width: getProgressPercent(scope.row.totalQuantity, getTaskAllocation(scope.row.id)) + '%',
                    height: '100%',
                    background: getProgressColor(scope.row.totalQuantity, getTaskAllocation(scope.row.id)),
                    transition: 'width 0.3s ease'
                  }"
                ></div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="priorityTagType(scope.row.priority)">
              {{ scope.row.priorityText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <div>
              <el-tag :type="statusTagType(scope.row.status)" style="margin-bottom: 4px;">
                {{ scope.row.status }}
              </el-tag>
              <br>
              <el-tag size="small"
                :type="getAllocationStatusType(scope.row.totalQuantity, getTaskAllocation(scope.row.id))">
                {{ getAllocationStatusText(scope.row.totalQuantity, getTaskAllocation(scope.row.id)) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="editPlan(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDeletePlan(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination style="margin-top: 20px; text-align: right;" background
        layout="total, sizes, prev, pager, next, jumper" :total="total" :page-size="size" :current-page="page"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" :page-sizes="[5, 10, 20, 50]" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEditMode ? '编辑计划' : '新增计划'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划编号">
          <el-input v-model="form.planCode" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.totalQuantity" :min="1" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" placeholder="请选择">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="待下发" value="待下发" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">
          {{ isEditMode ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 简单进度详情弹窗 -->
    <SimpleProgressDialog
      v-model:visible="progressDialogVisible"
      :plan="selectedPlan"
      :tasks="tasks"
    />
  </div>
</template>

<script setup>
import HeaderSection from '@/components/HeaderSection.vue'
import DataDebugPanel from '@/components/DataDebugPanel.vue'
import SimpleProgressDialog from '@/components/SimpleProgressDialog.vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'
import { getPlans, createPlan, updatePlan, deletePlan } from '@/api/plans'
import { getTasks } from '@/api/tasks'
import { useAppStore } from '@/stores'
import { checkPlanTaskConsistency, generateConsistencyReport } from '@/utils/dataConsistencyCheck'
// import { calculateDetailedAllocation, generateFlowReport, getRecommendedActions } from '@/utils/productionFlowCalculator'
// import { generateAllocationSuggestions, validateAllocationSuggestions } from '@/utils/taskAllocationSuggestion'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'


// 优先级映射：文字 <-> 数字
const priorityMap = {
  高: 1,
  中: 2,
  低: 3,
}
const reversePriorityMap = {
  1: '高',
  2: '中',
  3: '低',
}

const props = defineProps({
  card1: {
    type: String,
    default: '总计划数'
  },
  card4: {
    type: String,
    default: '待下发'
  }
})
const appStore = useAppStore()

// 使用store的统计数据
const totalPlans = computed(() => appStore.totalPlans)
const inProgress = computed(() => appStore.inProgressPlans)
const completed = computed(() => appStore.completedPlans)
const pending = computed(() => appStore.pendingPlans)

const dialogVisible = ref(false)
const isEditMode = ref(false)
const plans = ref([])
const tasks = ref([]) // ✅ 添加任务数据
const page = ref(1)
const size = ref(10)
const total = ref(0)
const showDebugPanel = ref(false) // ✅ 调试面板开关
const progressDialogVisible = ref(false) // ✅ 进度详情弹窗
const selectedPlan = ref(null) // ✅ 选中的计划

const form = ref({
  planCode: '',
  productName: '',
  totalQuantity: 0,
  status: '',
  priority: '', // 文字，如“高”
})

const filter = ref({
  keyword: '',
  status: '',
  priority: '' // 新增优先级筛选
})

const navItems = [
  { name: '首页', path: '/home' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑参数', path: '/injection' },
  { name: '图案参数', path: '/pattern' },
]

const fetchPlans = async () => {
  try {
    const data = await getPlans(page.value - 1, size.value)

    plans.value = data.content.map(item => {
      const priorityNum = Number(item.priority)
      return {
        ...item,
        priority: priorityNum,
        priorityText: reversePriorityMap[priorityNum] || `未知(${item.priority})`,
        createTime: item.createTime,
      }
    })
    total.value = data.totalElements
  } catch (err) {
    console.error('加载计划失败', err)
  }
}

// ✅ 获取任务数据
const fetchTasks = async () => {
  try {
    const data = await getTasks({ page: 0, size: 1000 })
    tasks.value = data.content || []
    console.log('任务数据加载完成:', {
      taskCount: tasks.value.length,
      sampleTasks: tasks.value.slice(0, 3).map(t => ({
        id: t.id,
        planId: t.planId,
        quantity: t.quantity,
        status: t.status
      }))
    })
  } catch (err) {
    console.error('加载任务失败', err)
    tasks.value = []
  }
}

onMounted(async () => {
  await Promise.all([fetchPlans(), fetchTasks()]) // ✅ 同时获取计划和任务数据

  // ✅ 自动进行数据一致性检查
  setTimeout(() => {
    if (plans.value.length > 0 && tasks.value.length > 0) {
      const checkResult = checkPlanTaskConsistency(plans.value, tasks.value)
      if (!checkResult.isConsistent) {
        console.warn('发现数据一致性问题:', checkResult)
        ElMessage.warning(`发现 ${checkResult.inconsistencies.length} 个数据一致性问题，请检查`)
      }
    }
  }, 1000)

  // 启动自动刷新
  appStore.startAutoRefresh(30000) // 30秒刷新一次
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})

const handleSizeChange = (val) => {
  size.value = val
  page.value = 1
  fetchPlans()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchPlans()
}

const openDialog = () => {
  isEditMode.value = false
  form.value = {
    planCode: '',
    productName: '',
    totalQuantity: 0,
    status: '',
    priority: '', // 文字，如“高”
  }
  dialogVisible.value = true
}

const editPlan = (row) => {
  isEditMode.value = true
  console.log('编辑计划数据:', row)
  console.log('优先级值:', row.priority, '类型:', typeof row.priority)

  // 确保优先级正确转换
  const priorityNum = Number(row.priority)
  const priorityText = reversePriorityMap[priorityNum]

  console.log('转换后的优先级:', { num: priorityNum, text: priorityText })

  form.value = {
    id: row.id,
    planCode: row.planCode,
    productName: row.productName,
    totalQuantity: row.totalQuantity,
    priority: priorityText || '',
    status: row.status,
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  console.log('提交表单 - 当前模式:', isEditMode.value ? '编辑' : '新建')
  console.log('表单数据:', form.value)

  if (
    form.value.planCode &&
    form.value.productName &&
    form.value.totalQuantity &&
    form.value.status &&
    form.value.priority
  ) {
    try {
      // 检查id
      if (isEditMode.value && !form.value.id) {
        alert('编辑模式下缺少计划ID，无法提交')
        return
      }

      // 确保优先级正确转换
      const priorityText = form.value.priority

      if (!priorityText) {
        throw new Error('请选择优先级')
      }

      const priorityNum = priorityMap[priorityText]

      console.log('优先级转换详情:', {
        formValue: priorityText,
        mappedValue: priorityNum,
        priorityMap: priorityMap
      })

      if (priorityNum === undefined) {
        throw new Error(`无效的优先级值: ${priorityText}`)
      }

      const payload = {
        planCode: form.value.planCode,
        productName: form.value.productName,
        totalQuantity: Number(form.value.totalQuantity),
        status: form.value.status,
        priority: priorityNum, // 确保是数字
      }

      console.log('提交计划payload:', payload)

      if (isEditMode.value) {
        console.log('执行更新操作，ID:', form.value.id)
        await updatePlan(form.value.id, payload)
      } else {
        console.log('执行创建操作')
        await createPlan(payload)
      }

      dialogVisible.value = false
      await Promise.all([fetchPlans(), fetchTasks()]) // ✅ 同时刷新计划和任务数据
      console.log('操作成功完成')
    } catch (err) {
      console.error('提交失败详细信息:', err)
      alert('提交失败，请检查后端接口: ' + (err.message || '未知错误'))
    }
  } else {
    console.log('表单验证失败，缺少字段:', {
      planCode: !!form.value.planCode,
      productName: !!form.value.productName,
      totalQuantity: !!form.value.totalQuantity,
      status: !!form.value.status,
      priority: !!form.value.priority
    })
    alert('请填写完整表单信息！')
  }
}

const handleDeletePlan = async (row) => {
  if (!confirm(`确定要删除计划 ${row.planCode} 吗？`)) return

  try {
    await deletePlan(row.id)
    await Promise.all([fetchPlans(), fetchTasks()]) // ✅ 同时刷新计划和任务数据
  } catch (err) {
    console.error('删除失败', err)
    alert('删除失败，请检查后端接口或网络')
  }
}

const statusTagType = (status) => {
  switch (status) {
    case '待下发':
      return 'warning'   // 给“待下发”一个醒目的颜色（也可以换）
    case '进行中':
      return 'primary'
    case '已完成':
      return 'success'
    default:
      return ''
  }
}


const priorityTagType = (priority) => {
  // priority 这里是数字
  switch (priority) {
    case 1:
      return 'success' // 高-绿色
    case 2:
      return 'warning' // 中-黄色
    case 3:
      return 'info' // 低-蓝色
    default:
      return ''
  }
}

const formatDate = (dateStr) => {
  return dateStr ? dayjs(dateStr).format('YYYY-MM-DD HH:mm') : ''
}

// ✅ 获取剩余数量的颜色
const getRemainingColor = (total, allocated) => {
  const remaining = total - allocated
  if (remaining < 0) return '#F56C6C' // 红色：超额分配
  if (remaining === 0) return '#67C23A' // 绿色：完全分配
  return '#E6A23C' // 橙色：部分分配
}

// ✅ 获取进度百分比
const getProgressPercent = (total, allocated) => {
  if (total === 0) return 0
  return Math.min((allocated / total) * 100, 100)
}

// ✅ 获取进度条颜色
const getProgressColor = (total, allocated) => {
  const percent = allocated / total
  if (percent > 1) return '#F56C6C' // 红色：超额
  if (percent === 1) return '#67C23A' // 绿色：完成
  if (percent >= 0.8) return '#409EFF' // 蓝色：接近完成
  return '#E6A23C' // 橙色：进行中
}

// ✅ 获取分配状态文本
const getAllocationStatusText = (total, allocated) => {
  if (allocated === 0) return '未分配'
  if (allocated > total) return '超额分配'
  if (allocated === total) return '完全分配'
  return '部分分配'
}

// ✅ 获取分配状态标签类型
const getAllocationStatusType = (total, allocated) => {
  if (allocated === 0) return 'info'
  if (allocated > total) return 'danger'
  if (allocated === total) return 'success'
  return 'warning'
}

// ✅ 数据一致性检查
const checkDataConsistency = () => {
  if (plans.value.length === 0 || tasks.value.length === 0) {
    ElMessage.warning('请先加载计划和任务数据')
    return
  }

  const checkResult = checkPlanTaskConsistency(plans.value, tasks.value)
  const report = generateConsistencyReport(checkResult)

  console.log('数据一致性检查结果:', checkResult)

  if (checkResult.isConsistent) {
    ElNotification({
      title: '数据一致性检查',
      message: `数据一致性良好！健康评分: ${checkResult.summary.healthScore}/100`,
      type: 'success',
      duration: 3000
    })
  } else {
    ElNotification({
      title: '发现数据不一致',
      message: `发现 ${checkResult.inconsistencies.length} 个严重问题，${checkResult.warnings.length} 个警告`,
      type: 'warning',
      duration: 5000
    })

    // 在控制台输出详细报告
    console.log(report)

    // 高亮显示有问题的计划
    checkResult.inconsistencies.forEach(issue => {
      console.warn(`❌ ${issue.message}`)
    })

    checkResult.warnings.forEach(warning => {
      console.warn(`⚠️ ${warning.message}`)
    })
  }
}

// ✅ 修复任务分配计算函数
const getTaskAllocation = (planId) => {
  if (!tasks.value || tasks.value.length === 0) {
    return 0
  }

  // 根据planId找到相关任务，累计数量
  const relatedTasks = tasks.value.filter(task => task.planId === planId)
  const totalAllocated = relatedTasks.reduce((sum, task) => {
    return sum + (task.quantity || 0)
  }, 0)

  console.log(`计划 ${planId} 的任务分配:`, {
    relatedTasks: relatedTasks.length,
    totalAllocated,
    taskDetails: relatedTasks.map(t => ({ id: t.id, quantity: t.quantity, status: t.status }))
  })

  return totalAllocated
}

// ✅ 获取已完成数量（考虑工序进度）
const getCompletedQuantity = (planId) => {
  if (!tasks.value || tasks.value.length === 0) {
    return 0
  }
  
  const relatedTasks = tasks.value.filter(task => task.planId === planId && task.status === '已完成')
  return relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
}

// ✅ 获取进行中数量
const getInProgressQuantity = (planId) => {
  if (!tasks.value || tasks.value.length === 0) {
    return 0
  }
  
  const relatedTasks = tasks.value.filter(task => task.planId === planId && task.status === '进行中')
  return relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
}

// ✅ 获取完成百分比
const getCompletionPercent = (total, completed) => {
  if (total === 0) return 0
  return Math.min((completed / total) * 100, 100)
}

// ✅ 显示详细进度（简化版本）
const showDetailedProgress = (plan) => {
  selectedPlan.value = plan
  progressDialogVisible.value = true
  
  console.log('显示计划详情:', plan.planCode)
}

// ✅ 获取实际剩余数量（考虑已完成的工序）
const getActualRemaining = (planId, totalQuantity) => {
  const completedQuantity = getCompletedQuantity(planId)
  return Math.max(0, totalQuantity - completedQuantity)
}

// ✅ 显示智能分配建议（临时简化版本）
const showSmartAllocation = async () => {
  ElMessage.info('智能分配建议功能正在开发中，请手动创建任务')
}

const filteredPlans = computed(() => {
  let result = [...plans.value]

  // 关键词筛选
  if (filter.value.keyword) {
    const kw = filter.value.keyword.toLowerCase()
    result = result.filter(plan =>
      (plan.planCode && plan.planCode.toLowerCase().includes(kw)) ||
      (plan.productName && plan.productName.toLowerCase().includes(kw))
    )
  }

  // 状态筛选
  if (filter.value.status) {
    result = result.filter(plan => plan.status === filter.value.status)
  }

  // 优先级筛选
  if (filter.value.priority) {
    result = result.filter(plan => plan.priority === Number(filter.value.priority))
  }

  return result
})
</script>

<style scoped>
.plan-manager {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部卡片内容，左右布局，中间导航居中 */
.header-card {
  margin-bottom: 12px;
  border-radius: 52px;
  padding: 15px 40px;
}

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

/* 中间导航 */
.nav-buttons {
  display: flex;
  gap: 24px;
  flex: 1;
  justify-content: center;
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

/* 过滤器和表格部分 */
.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.table-card {
  border-radius: 12px;
}
</style>
