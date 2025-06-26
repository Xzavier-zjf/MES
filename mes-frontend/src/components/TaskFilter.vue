<template>
  <div class="task-filter">
    <el-form :inline="true" :model="filters" class="filter-form">
      <el-form-item label="计划编号">
        <el-input
          v-model="filters.planCode"
          placeholder="输入计划编号"
          prefix-icon="Search"
        />
      </el-form-item>

      <el-form-item label="工序类型">
        <el-select v-model="filters.processType" placeholder="请选择工序类型" clearable>
          <el-option label="注塑" value="注塑" />
          <el-option label="印刷" value="印刷" />
          <el-option label="组装" value="组装" />
          <el-option label="质检" value="质检" />
          <el-option label="包装" value="包装" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="请选择状态" clearable>
          <el-option label="待下发" value="待下发" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="$emit('filter')">筛选</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
        <el-button type="success" @click="$emit('create')">新建任务</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
const props = defineProps({
  filters: Object,
})
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
  gap: 12px;
}
</style>
