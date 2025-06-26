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
        />
        <el-select
          v-model="filter.priority"
          placeholder="优先级"
          size="small"
          clearable
          style="width: 120px"
        >
          <el-option label="高" value="高" />
          <el-option label="中" value="中" />
          <el-option label="低" value="低" />
        </el-select>
        <el-date-picker
          v-model="filter.date"
          type="date"
          placeholder="创建时间"
          size="small"
        />
        <el-button type="primary" size="small" icon="Plus" @click="openDialog">
          添加计划
        </el-button>
      </div>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" shadow="always">
      <el-table :data="filteredPlans" style="width: 100%" border>
        <el-table-column prop="plan_code" label="计划编号" width="150" />
        <el-table-column prop="product_name" label="产品名称" width="200" />
        <el-table-column prop="total_quantity" label="生产数量" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="priorityTagType(scope.row.priority)">
              {{ scope.row.priority }}
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
        <el-table-column prop="create_time" label="创建时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="editPlan(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deletePlan(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEditMode ? '编辑计划' : '新增计划'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划编号">
          <el-input v-model="form.plan_code" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.product_name" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.total_quantity" :min="1" />
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
            <el-option label="草稿" value="草稿" />
            <el-option label="已下发" value="已下发" />
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
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'

const route = useRoute()

const dialogVisible = ref(false)
const isEditMode = ref(false)

const form = ref({
  plan_code: '',
  product_name: '',
  total_quantity: 0,
  status: '',
  priority: '',
})

const filter = ref({
  keyword: '',
  priority: '',
  date: null,
})

const plans = ref([
  {
    plan_code: 'P20240601',
    product_name: '手机壳A款',
    total_quantity: 1000,
    status: '已下发',
    priority: '高',
    create_time: '2025-06-18 10:30',
  },
  {
    plan_code: 'P20240602',
    product_name: '手机壳B款',
    total_quantity: 500,
    status: '草稿',
    priority: '中',
    create_time: '2025-06-17 16:00',
  },
])

const navItems = [
  { name: '首页', path: '/home' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑参数', path: '/injection' },
   { name: '图案参数', path: '/pattern' },
]

const filteredPlans = computed(() => {
  return plans.value.filter(plan => {
    const keywordMatch = !filter.value.keyword ||
      plan.plan_code.includes(filter.value.keyword) ||
      plan.product_name.includes(filter.value.keyword)

    const priorityMatch = !filter.value.priority || plan.priority === filter.value.priority

    const dateMatch = !filter.value.date ||
      dayjs(plan.create_time).format('YYYY-MM-DD') === dayjs(filter.value.date).format('YYYY-MM-DD')

    return keywordMatch && priorityMatch && dateMatch
  })
})

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
    case '高': return 'danger'
    case '中': return 'warning'
    case '低': return 'info'
    default: return ''
  }
}

const openDialog = () => {
  isEditMode.value = false
  form.value = {
    plan_code: '',
    product_name: '',
    total_quantity: 0,
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
const submitForm = () => {
  if (
    form.value.plan_code &&
    form.value.product_name &&
    form.value.total_quantity &&
    form.value.status &&
    form.value.priority
  ) {
    if (isEditMode.value) {
      const idx = plans.value.findIndex(p => p.plan_code === form.value.plan_code)
      if (idx !== -1) plans.value[idx] = { ...form.value }
    } else {
      plans.value.push({
        ...form.value,
        create_time: dayjs().format('YYYY-MM-DD HH:mm')
      })
    }
    dialogVisible.value = false
  } else {
    alert('请填写完整表单信息！')
  }
}
const deletePlan = (row) => {
  plans.value = plans.value.filter(p => p.plan_code !== row.plan_code)
}
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
