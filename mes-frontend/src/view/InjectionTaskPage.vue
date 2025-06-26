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

    <!-- 任务列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="tasks" border style="width: 100%">
        <el-table-column prop="taskId" label="任务编号" width="120" />
        <el-table-column prop="planCode" label="计划编号" />
        <el-table-column prop="processType" label="工序类型" />
        <el-table-column prop="deviceCode" label="设备编号" />
        <el-table-column prop="pressure" label="注塑压力 (MPa)" />
        <el-table-column prop="speed" label="注塑速度 (mm/s)" />
        <el-table-column prop="coolingTime" label="冷却时间 (s)" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">录入工艺</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 参数录入弹窗 -->
    <el-dialog v-model="dialogVisible" title="录入注塑工艺参数" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="注塑压力">
          <el-input-number v-model="form.pressure" :min="0" />
        </el-form-item>
        <el-form-item label="注塑速度">
          <el-input-number v-model="form.speed" :min="0" />
        </el-form-item>
        <el-form-item label="冷却时间">
          <el-input-number v-model="form.coolingTime" :min="0" />
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
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const tasks = ref([
  { taskId: 'T001', planCode: 'P20240601', processType: '注塑', deviceCode: 'D001', pressure: 100, speed: 60, coolingTime: 15 },
  { taskId: 'T002', planCode: 'P20240602', processType: '注塑', deviceCode: 'D002', pressure: 90, speed: 55, coolingTime: 12 },
])

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

const submitParams = () => {
  const index = tasks.value.findIndex(t => t.taskId === form.value.taskId)
  if (index !== -1) {
    tasks.value[index] = { ...form.value }
  }
  dialogVisible.value = false
}
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
</style>
