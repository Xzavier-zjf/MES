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
          <el-input v-model="filters.taskCode" placeholder="请输入任务编号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格展示 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="filteredTasks" border style="width: 100%">
        <el-table-column prop="planCode" label="计划编号" />
        <el-table-column prop="taskId" label="任务编号" width="120" />
        <el-table-column prop="deviceCode" label="设备编号" />
        <el-table-column prop="pressure" label="注塑压力 (MPa)" />
        <el-table-column prop="injectionSpeed" label="注塑速度 (mm/s)" />
        <el-table-column prop="holdTime" label="保压时间 (s)" />
        <el-table-column prop="coolingTime" label="冷却时间 (s)" />
        <el-table-column prop="moldTemperature" label="模具温度 (℃)" />
        <el-table-column prop="materialTemperature" label="料筒温度 (℃)" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">
              {{ row.pressure ? '修改工艺' : '录入工艺' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 工艺参数录入弹窗 -->
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

// 列表数据
const tasks = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 过滤器
const filters = ref({
  taskCode: '',
  planCode: ''
})

// 过滤 & 分页
const filteredTasks = computed(() => {
  return tasks.value
    .filter(task => {
      const taskCodeStr = task.taskCode ? String(task.taskCode) : ''
      const planCodeStr = task.planCode ? String(task.planCode) : ''
      return (
        taskCodeStr.includes(filters.value.taskCode) &&
        planCodeStr.includes(filters.value.planCode)
      )
    })
    .slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
})

// 表单弹窗相关
const dialogVisible = ref(false)
const form = ref({})

// 跳转导航栏
const navItems = [
  { name: '首页', path: '/home' },
  { name: '生产计划管理', path: '/plan' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑参数', path: '/injection' },
  { name: '图案参数', path: '/pattern' }
]

// 打开弹窗
const openDialog = (task) => {
  form.value = {
    ...task,
    pressure: task.pressure || 0,
    injectionSpeed: task.injectionSpeed || 0,
    coolingTime: task.coolingTime || 0,
    holdTime: task.holdTime || 0,
    moldTemperature: task.moldTemperature || 0,
    materialTemperature: task.materialTemperature || 0
  }
  dialogVisible.value = true
}

// 创建参数
const createInjectionParam = async (dto) => {
  const res = await axios.post(`http://localhost:8080/api/v1/process/injection-params`, dto)
  return res.data
}

// 更新参数
const updateInjectionParam = async (id, dto) => {
  const res = await axios.put(`http://localhost:8080/api/v1/process/injection-params/${id}`, dto)
  return res.data
}

// 提交参数
const submitParams = async () => {
  try {
    const dto = {
      planCode: form.value.planCode,
      taskId: form.value.taskCode,
      deviceId: form.value.deviceCode,
      pressure: form.value.pressure,
      injectionSpeed: form.value.injectionSpeed,
      coolingTime: form.value.coolingTime,
      holdTime: form.value.holdTime,
      moldTemperature: form.value.moldTemperature,
      materialTemperature: form.value.materialTemperature
    }

    let result
    if (form.value.id) {
      result = await updateInjectionParam(form.value.id, dto)
    } else {
      result = await createInjectionParam(dto)
    }

    // 更新前端数据（可选刷新方式）
    await fetchInjectionTasks()
    dialogVisible.value = false
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 筛选操作
const submitFilter = () => {}
const resetFilter = () => {
  filters.value = { taskCode: '', planCode: '' }
}

// 从后端拉取所有注塑任务（已+未）
const fetchInjectionTasks = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/v1/process/all');
    console.log('res.data:', res.data);

    // 如果是分页响应结构
    if (res.data.content) {
      tasks.value = res.data.content.map(task => ({
        ...task,
        taskId: `TASK-${String(task.taskCode).padStart(4, '0')}`
      }));
      total.value = res.data.totalElements;
    } else if (Array.isArray(res.data)) {
      tasks.value = res.data.map(task => ({
        ...task,
        taskId: `TASK-${String(task.taskCode).padStart(4, '0')}`
      }));
      total.value = res.data.length;
    } else {
      console.warn('Unexpected response data format', res.data);
      tasks.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('fetchInjectionTasks error:', error);
  }
};


// 分页处理
const handleCurrentChange = (val) => { currentPage.value = val }
const handleSizeChange = (val) => { pageSize.value = val }

onMounted(() => {
  fetchInjectionTasks()
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