<template>
  <div class="injection-page">
    <!-- 顶部标题和导航栏合并在一个卡片 -->
    <el-card class="header-card" shadow="hover">
      <div class="header-content">
        <!-- 左侧标题 -->
        <div class="left-title">
          <span class="icon">🛠</span>
          <span class="text">注塑工艺参数记录</span>
        </div>

        <!-- 右侧导航按钮 -->
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

    <!-- 过滤器组件 -->
    <el-card shadow="hover" class="table-card" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="计划编号">
          <el-input v-model="filters.planCode" placeholder="请输入计划编号" />
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
        <el-table-column prop="planCode" label="计划编号" />
        <el-table-column prop="taskId" label="任务编号" width="120" />
        <!-- <el-table-column prop="processType" label="工序类型" /> -->
        <el-table-column prop="deviceId" label="设备编号" />
        <el-table-column prop="pressure" label="注塑压力 (MPa)" />
        <el-table-column prop="injectionSpeed" label="注塑速度 (mm/s)" />
        <el-table-column prop="holdTime" label="保压时间 (s)" />
        <el-table-column prop="coolingTime" label="冷却时间 (s)" />
        <el-table-column prop="holdTime" label="模具温度 (℃)" />
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
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()

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
  planCode: '',
  processType: ''
})

// 过滤后的任务列表
const filteredTasks = computed(() => {
  return tasks.value.filter(task => {
    const taskIdStr = task.taskId != null ? String(task.taskId) : '';
    const planCodeStr = task.planCode != null ? String(task.planCode) : '';
    return (
      taskIdStr.includes(filters.value.taskId) &&
      planCodeStr.includes(filters.value.planCode) &&
      (filters.value.processType === '' || task.processType === filters.value.processType)
    )
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
const updateInjectionParam = async (id, updateDTO) => {
  try {
    const response = await axios.put(`http://localhost:8080/api/v1/process/injection-params/${id}`, updateDTO)
    return response.data
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
      planCode: form.value.planCode,
      taskId: form.value.taskId,
      deviceId: form.value.deviceId,
      pressure: form.value.pressure,
      injectionSpeed: form.value.injectionSpeed,
      coolingTime: form.value.coolingTime,
      holdTime: form.value.holdTime,
      moldTemperature: form.value.moldTemperature,
      materialTemperature: form.value.materialTemperature
    }
    // 调用更新接口
    const updatedData = await updateInjectionParam(form.value.id, updateDTO)
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
  // 过滤逻辑已在 computed 中实现，无需额外操作
}

// 重置过滤
const resetFilter = () => {
  filters.value = {
    taskId: '',
    planCode: '',
    processType: ''
  }
}

// 获取注塑参数列表
const fetchInjectionParams = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/process/injection-params/all', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        sort: 'id,desc'
      }
    })
    tasks.value = response.data.content
    total.value = response.data.totalElements
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
onMounted(() => {
  fetchInjectionParams()
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