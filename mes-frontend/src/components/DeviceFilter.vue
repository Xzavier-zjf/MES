<template>
  <el-form :inline="true" :model="filters" class="filter-form">
    <el-form-item label="设备类型">
      <el-select v-model="filters.type" placeholder="请选择类型" clearable>
        <el-option label="注塑机" value="注塑机" />
        <el-option label="压机" value="压机" />
      </el-select>
    </el-form-item>
    <el-form-item label="状态">
      <el-select v-model="filters.status" placeholder="请选择状态" clearable>
        <el-option label="运行中" value="运行中" />
        <el-option label="故障" value="故障" />
        <el-option label="空闲" value="空闲" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="$emit('filter')">筛选</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
defineProps({ filters: Object })
</script>

<style scoped>
.filter-form {
  margin-bottom: 20px;
}
</style>
