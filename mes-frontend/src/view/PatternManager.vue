<template>
  <div class="pattern-page">
    <!-- 顶部导航 -->
    <TopNavBar />

    <!-- 页面标题卡片 -->
    <el-card class="title-card" shadow="hover">
      <h2>🎨 印刷图案管理</h2>
      <p>管理印刷图案资源，为印刷任务提供图案支持。</p>
    </el-card>

    <!-- 筛选表单 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="图案编号">
          <el-input v-model="filters.code" placeholder="请输入图案编号" clearable />
        </el-form-item>
        <el-form-item label="适用机型">
          <el-select v-model="filters.machine" placeholder="请选择机型" clearable>
            <el-option label="印刷机 A" value="A" />
            <el-option label="印刷机 B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="filterPatterns">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="openDialog">上传图案</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 图案表格展示 -->
    <el-card class="table-card" shadow="hover">
      <el-table :data="filteredPatterns" border stripe>
        <el-table-column prop="code" label="图案编号" width="120" />
        <el-table-column prop="name" label="图案名称" />
        <el-table-column label="预览图" width="120">
          <template #default="{ row }">
            <el-image :src="row.image" fit="cover" style="width: 60px; height: 60px" />
          </template>
        </el-table-column>
        <el-table-column prop="machine" label="适用机型" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="editPattern(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 图案上传弹窗 -->
    <el-dialog v-model="dialogVisible" title="上传 / 编辑图案" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="图案编号">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="图案名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="适用机型">
          <el-select v-model="form.machine">
            <el-option label="印刷机 A" value="A" />
            <el-option label="印刷机 B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="图案图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="1"
            :on-change="handleImageChange"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePattern">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import TopNavBar from '@/components/NavBar.vue'
import { ref, computed } from 'vue'

const dialogVisible = ref(false)
const filters = ref({ code: '', machine: '' })
const patterns = ref([
  { code: 'P001', name: '波纹线条', machine: 'A', image: 'https://via.placeholder.com/60x60' },
  { code: 'P002', name: '科技图案', machine: 'B', image: 'https://via.placeholder.com/60x60' },
])
const form = ref({ code: '', name: '', machine: '', image: '' })

const filteredPatterns = computed(() => {
  return patterns.value.filter(p =>
    (!filters.value.code || p.code.includes(filters.value.code)) &&
    (!filters.value.machine || p.machine === filters.value.machine)
  )
})

const filterPatterns = () => {}
const resetFilters = () => { filters.value = { code: '', machine: '' } }
const openDialog = () => {
  dialogVisible.value = true
  form.value = { code: '', name: '', machine: '', image: '' }
}
const editPattern = (row) => {
  dialogVisible.value = true
  form.value = { ...row }
}
const savePattern = () => { dialogVisible.value = false }
const handleImageChange = (file) => {
  form.value.image = URL.createObjectURL(file.raw)
}
</script>

<style scoped>
.pattern-page {
  padding: 24px;
  background-color: #f6f8fa;
  min-height: 100vh;
}

.title-card {
  margin-bottom: 20px;
  background-color: #f0f9ff;
  border-left: 5px solid #409EFF;
}

.title-card h2 {
  margin-bottom: 6px;
  font-weight: bold;
  color: #1f2d3d;
}

.filter-card {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 16px;
}

.table-card {
  background-color: #fff;
  padding: 16px;
}
</style>
