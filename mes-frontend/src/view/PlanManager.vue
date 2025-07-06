<template>
  <div class="plan-manager">
    <HeaderSection 
    title="生产计划管理"
    subtitle="用于制定手机壳生产计划，依订单、库存等安排任务，保障生产顺利推进。
      "
    :showStats="true"
    :card1="props.card1"
    :card4="props.card4"
    :value1="totalPlans"
    :value2="inProgress"
    :value3="completed"
    :value4="pending"/>

    <!-- 中间过滤器区域 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-container">
        <el-input
          v-model="filter.keyword"
          placeholder="输入计划编号或产品名称"
          clearable
          style="width: 220px"
        />
        <el-select
          v-model="filter.priority"
          placeholder="优先级"
          clearable
          style="width: 120px"
        >
            <el-option label="低" value="3" />
          <el-option label="中" value="2" />
          <el-option label="高" value="1  " />
        </el-select>
        <el-select
          v-model="filter.status"
          placeholder="状态"
          clearable
          style="width: 120px"
        >
            <el-option label="待下发" value="待下发" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
        <el-button type="primary" @click="openDialog">添加计划</el-button>
      </div>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="filteredPlans" style="width: 100%" border size="large">
        <el-table-column prop="planCode" label="计划编号" width="180" />
        <el-table-column prop="productName" label="产品名称" width="200" />
        <el-table-column prop="totalQuantity" label="生产数量" width="120" />
        <el-table-column label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="priorityTagType(scope.row.priority)">
              {{ scope.row.priorityText }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="editPlan(scope.row)" >编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDeletePlan(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="size"
        :current-page="page"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes="[5, 10, 20, 50]"
      />
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
            <el-option label="高" value="1" />
            <el-option label="中" value="2" />
            <el-option label="低" value="3" />
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
  </div>
</template>

<script setup>
import HeaderSection from '@/components/HeaderSection.vue'
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getPlans, createPlan, updatePlan, deletePlan } from '@/api/plans'


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
// 计算统计数据
const totalPlans = computed(() => plans.value.length)
const inProgress = computed(() => plans.value.filter(t => t.status === '进行中').length)
const completed = computed(() => plans.value.filter(t => t.status === '已完成').length)
const pending = computed(() => plans.value.filter(t => t.status === '待下发').length)

const dialogVisible = ref(false)
const isEditMode = ref(false)
const plans = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

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

onMounted(() => {
  fetchPlans()
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
  form.value = {
    id: row.id,
    planCode: row.planCode,
    productName: row.productName,
    totalQuantity: row.totalQuantity,
    priority: reversePriorityMap[row.priority] || '',
    status: row.status,
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (
    form.value.planCode &&
    form.value.productName &&
    form.value.totalQuantity &&
    form.value.status &&
    form.value.priority
  ) {
    try {
      const payload = {
        planCode: form.value.planCode,
        productName: form.value.productName,
        totalQuantity: Number(form.value.totalQuantity),
        status: form.value.status,
        priority: priorityMap[form.value.priority], // 文字转数字
      }

      if (isEditMode.value) {
        await updatePlan(form.value.id, payload)
      } else {
        await createPlan(payload)
      }

      dialogVisible.value = false
      fetchPlans()
    } catch (err) {
      alert('提交失败，请检查后端接口')
      console.error(err)
    }
  } else {
    alert('请填写完整表单信息！')
  }
}

const handleDeletePlan = async (row) => {
  if (!confirm(`确定要删除计划 ${row.planCode} 吗？`)) return

  try {
    await deletePlan(row.id)
    fetchPlans()
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
