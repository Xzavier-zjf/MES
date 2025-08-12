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
          <el-progress :percentage="getProgress(row)" :color="progressColor(row.status)" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="150" align="center">
        <template #default="{ row }">
          <div class="status-container">
            <span :class="`status-badge status-${getStatusClass(row.status)}`">
              {{ row.status }}
            </span>
            <el-tooltip 
              v-if="needsStatusUpdate(row)" 
              :content="`建议更新为: ${getSuggestedStatus(row)}`" 
              placement="top"
            >
              <el-button 
                size="small" 
                type="warning" 
                icon="Warning"
                circle
                @click.stop="$emit('suggest-status-update', row)"
                style="margin-left: 5px;"
              />
            </el-tooltip>
          </div>
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


const emit = defineEmits(['edit', 'delete', 'suggest-status-update'])

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

function getProgress(row) {
  if (!row.quantity || row.quantity === 0) return 0
  
  // 特殊处理：如果状态是已完成，进度应该是100%
  if (row.status === '已完成') {
    return 100
  }
  
  // 如果有实际完成数量，使用实际进度
  if (row.completedQuantity !== undefined && row.completedQuantity >= 0) {
    const actualProgress = Math.min(100, Math.round((row.completedQuantity / row.quantity) * 100))
    
    // 如果实际进度是100%但状态不是已完成，显示99%以示区别
    if (actualProgress === 100 && row.status !== '已完成') {
      return 99
    }
    
    return actualProgress
  }
  
  // 基于状态的基础进度（当没有完成数量数据时）
  const baseProgress = {
    '待下发': 0,
    '进行中': 50,
    '已完成': 100,
    '已暂停': 30
  }[row.status] || 0
  
  return baseProgress
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

// 计算建议的状态
function getSuggestedStatus(row) {
  const { status, quantity, completedQuantity = 0 } = row
  
  // 完成数量达到任务数量，建议设为已完成
  if (completedQuantity >= quantity && quantity > 0) {
    return '已完成'
  }
  
  // 有生产进度但未完成，建议设为进行中
  if (completedQuantity > 0 && completedQuantity < quantity && status !== '已暂停') {
    return '进行中'
  }
  
  // 没有生产进度
  if (completedQuantity === 0) {
    return status === '已暂停' ? '已暂停' : '待下发'
  }
  
  return status
}

// 检查状态是否需要更新
function needsStatusUpdate(row) {
  const suggestedStatus = getSuggestedStatus(row)
  return suggestedStatus !== row.status
}

// 获取状态更新原因
function getStatusUpdateReason(row) {
  const suggestedStatus = getSuggestedStatus(row)
  const { quantity, completedQuantity = 0 } = row
  
  if (suggestedStatus === '已完成') {
    return `完成数量 ${completedQuantity} 已达到任务数量 ${quantity}`
  }
  
  if (suggestedStatus === '进行中') {
    return `已有生产进度 ${completedQuantity}/${quantity}`
  }
  
  if (suggestedStatus === '待下发') {
    return '尚未开始生产'
  }
  
  return '基于当前进度的建议'
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

.status-container {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
