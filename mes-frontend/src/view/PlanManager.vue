<template>
  <div class="plan-manager">
    <!-- 顶部标题和导航栏合并在一个卡片 -->
    <el-card shadow="hover" class="header-card">
      <div class="header-content">
        <!-- 左侧标题 -->
        <div class="left-title">
          <span class="icon">📄</span>
          <span class="text">生产计划管理</span>
        </div>

        <!-- 中间导航按钮 -->
        <nav class="nav-buttons">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            :class="{ active: $route.path === item.path }"
          >
            {{ item.name }}
          </router-link>
        </nav>
      </div>
    </el-card>

    <!-- 中间过滤器区域 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-container">
        <el-input
          v-model="filter.keyword"
          placeholder="输入计划编号或产品名称"
          size="small"
          clearable
          style="width: 220px"
          @input="fetchPlans"
        />
        <el-select
          v-model="filter.status"
          placeholder="状态"
          size="small"
          clearable
          style="width: 120px"
          @change="fetchPlans"
        >
          <el-option label="草稿" value="草稿" />
          <el-option label="已下发" value="已下发" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
        <el-button type="primary" size="small" icon="Plus" @click="openDialog">
          添加计划
        </el-button>
      </div>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="filteredPlans" style="width: 100%" border>
        <el-table-column prop="planCode" label="计划编号" width="150" />
        <el-table-column prop="productName" label="产品名称" width="200" />
        <el-table-column prop="totalQuantity" label="生产数量" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="priorityTagType(scope.row.priority)">
              {{ convertPriority(scope.row.priority) }}
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
            <el-button size="small" type="primary" plain @click="editPlan(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deletePlan(scope.row)">删除</el-button>
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
            <el-option label="草稿" value="草稿" />
            <el-option label="已下发" value="已下发" />
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
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'

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
  priority: '',
})

const filter = ref({
  keyword: '',
  status: '',
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
    let query = `?page=${page.value - 1}&size=${size.value}`
    if (filter.value.status) {
      query += `&status=${filter.value.status}`
    }
    if (filter.value.keyword) {
      // 后端如果支持模糊搜索，可以加 keyword 参数，否则去掉或改成对应参数名
      query += `&keyword=${encodeURIComponent(filter.value.keyword)}`
    }
    const res = await fetch(`http://localhost:8080/api/v1/production/plans${query}`)
    const data = await res.json()

    // 赋值时转换优先级和时间格式，方便展示
    plans.value = data.content.map(item => ({
      ...item,
      priority: item.priority,
      createTime: item.createTime,
    }))
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
    priority: '',
  }
  dialogVisible.value = true
}

const editPlan = (row) => {
  isEditMode.value = true
  form.value = { ...row }
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
      const response = await fetch('http://localhost:8080/api/v1/production/plans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form.value)
      })

      if (!response.ok) throw new Error('添加失败')

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

const deletePlan = (row) => {
  // 这里最好调用后端删除接口，删除成功后刷新列表
  plans.value = plans.value.filter(p => p.planCode !== row.planCode)
}

const statusTagType = (status) => {
  switch (status) {
    case '草稿': return 'info'
    case '已下发': return 'success'
    case '进行中': return 'warning'
    case '已完成': return 'primary'
    default: return ''
  }
}

const priorityTagType = (priority) => {
  switch (priority) {
    case '1': return 'success'
    case '2': return 'warning'
    case '3': return 'info'
    default: return ''
  }
}

const convertPriority = (val) => {
  switch (val) {
    case '1': return '高'
    case '2': return '中'
    case '3': return '低'
    default: return val
  }
}

const formatDate = (dateStr) => {
  return dateStr ? dayjs(dateStr).format('YYYY-MM-DD HH:mm') : ''
}

const filteredPlans = computed(() => {
  // 关键字过滤：计划编号和产品名称
  if (!filter.value.keyword) return plans.value
  const kw = filter.value.keyword.toLowerCase()
  return plans.value.filter(plan => 
    (plan.planCode && plan.planCode.toLowerCase().includes(kw)) ||
    (plan.productName && plan.productName.toLowerCase().includes(kw))
  )
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
