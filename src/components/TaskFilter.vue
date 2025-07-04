<template>
  <div class="task-filter">
    <el-form :inline="true" :model="localFilters" class="filter-form">
      <el-form-item label="计划编号">
        <el-input
          v-model="localFilters.planCode"
          placeholder="输入计划编号"
          prefix-icon="Search"
          style="width: 200px"
        />
      </el-form-item>

      <el-form-item label="工序类型">
        <el-select 
          v-model="localFilters.processType" 
          placeholder="请选择工序类型" 
          clearable
          style="width: 200px" 
        >
          <el-option label="注塑" value="注塑" />
          <el-option label="印刷" value="印刷" />
          <el-option label="组装" value="组装" />
          <el-option label="质检" value="质检" />
          <el-option label="包装" value="包装" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select 
          v-model="localFilters.status" 
          placeholder="请选择状态" 
          clearable
          style="width: 200px"  
        >
          <el-option label="待下发" value="待下发" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
      </el-form-item>

      <el-form-item class="action-buttons">
        <el-button type="primary" @click="$emit('filter')">筛选</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
        <el-button type="success" @click="$emit('create')">新建任务</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  filters: Object,
})

const emit = defineEmits(['update:filters', 'filter', 'reset', 'create'])

const localFilters = reactive({ ...props.filters })

// 监听父组件filters变化，更新localFilters
watch(
  () => props.filters,
  (newVal) => {
    Object.assign(localFilters, newVal)
  },
  { deep: true }
)

// localFilters变化，发射事件给父组件更新filters
watch(
  localFilters,
  (newVal) => {
    emit('update:filters', { ...newVal })
  },
  { deep: true }
)
</script>

<style scoped>
.task-filter {
  margin-bottom: 20px;
  background-color: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;  /* 增加间距 */
  align-items: flex-start;
}

/* 确保按钮组换行后仍能对齐 */
.action-buttons {
  margin-left: auto;
}

/* 响应式调整：小屏幕时垂直排列 */
@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-buttons {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>