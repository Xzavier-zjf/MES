<template>
  <el-card class="table-card">
    <el-table
      :data="pagedTasks"
      border
      style="width: 100%"
      highlight-current-row
      @row-click="onRowClick"
    >
      <el-table-column prop="taskId" label="任务编号" width="140" sortable />
      <el-table-column prop="planCode" label="计划编号" width="140" />
      <el-table-column prop="processType" label="工序类型" width="120" />
      <el-table-column prop="deviceCode" label="设备编号" width="130" />
      <el-table-column label="任务数量" width="120" align="center">
        <template #default="{ row }">
          <el-tag type="info">{{ row.quantity }} 件</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="进度" width="150">
        <template #default="{ row }">
          <el-progress :percentage="getProgress(row.status)" :color="progressColor(row.status)" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <span :class="`status-badge status-${getStatusClass(row.status)}`">{{ row.status }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button size="small" icon="Edit" @click.stop="onEdit(row)">编辑</el-button>
          <el-button size="small" icon="Delete" type="danger" @click.stop="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
     <el-pagination
  background
  layout="prev, pager, next"
  :total="tasks.length"
  :page-size="pageSize"
  v-model:current-page="currentPage"
  @current-change="onPageChange"
/>

    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['edit', 'delete'])

const pageSize = 10
const currentPage = ref(1)

const pagedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return props.tasks.slice(start, start + pageSize)
})

function onPageChange(page) {
  currentPage.value = page
}

function getStatusClass(status) {
  const map = {
    '待下发': 'pending',
    '进行中': 'in-progress',
    '已完成': 'completed',
    '已暂停': 'paused'
  }
  return map[status] || 'pending'
}

function getProgress(status) {
  const map = {
    '待下发': 0,
    '进行中': 50,
    '已完成': 100,
    '已暂停': 30
  }
  return map[status] || 0
}

function progressColor(status) {
  const map = {
    '待下发': '#e2e8f0',
    '进行中': '#3b82f6',
    '已完成': '#10b981',
    '已暂停': '#f59e0b'
  }
  return map[status] || '#e2e8f0'
}

function onEdit(row) {
  emit('edit', row)
}

function onDelete(row) {
  ElMessageBox.confirm(`确定要删除任务 ${row.taskId} 吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    emit('delete', row)
    ElMessage.success('任务删除成功')
  }).catch(() => {})
}

function onRowClick(row) {
  // 这里可以做点击行的操作，比如详情展示等
}
</script>

<style scoped>
.table-card {
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  border: none;
  background: white;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.table-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-in-progress {
  background: #dbeafe;
  color: #2563eb;
}

.status-completed {
  background: #dcfce7;
  color: #16a34a;
}

.status-paused {
  background: #fde68a;
  color: #b45309;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
}
</style>
