<template>
  <el-card class="table-card">
    <el-table
      :data="pagedTasks"
      border
      style="width: 100%"
      highlight-current-row
      @row-click="onRowClick"
      size="large"
    >
      <el-table-column prop="planCode" label="计划编号" width="180" />
      <el-table-column prop="taskCode" label="任务编号" width="180" sortable />
      <el-table-column prop="productName" label="产品名称" width="180" sortable />
      <el-table-column prop="processType" label="工序类型" width="120" />
      <el-table-column prop="deviceCode" label="设备编号" width="130" />
      <el-table-column label="任务数量" width="120" align="center">
        <template #default="{ row }">
          <el-tag >{{ row.quantity }} 件</el-tag>
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
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button size="small" plain icon="Edit" type="primary" @click.stop="onEdit(row)">编辑</el-button>
          <el-button size="small" plain icon="Delete" type="danger" @click.stop="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="tasks.length"
        :page-size="pageSize.value"
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="onPageChange"
        @size-change="handleSizeChange"
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
  },
})


const emit = defineEmits(['edit', 'delete'])

const pageSize = ref(10)
const currentPage = ref(1)

const pagedTasks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  const result = props.tasks.slice(start, end)
  
  return result
})

function onPageChange(page) {
  currentPage.value = page
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
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
    emit('delete', row)
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
