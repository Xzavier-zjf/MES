<template>
  <div class="pattern-page">
      <HeaderSection 
    title=" 🎨&nbsp印刷图案管理"
    subtitle="管理印刷图案资源，为印刷任务提供图案支持。"
    :showStats="false"  
    :value1="totalTasks"
    :value2="inProgressTasks"
    :value3="completedTasks"
    :value4="pendingTasks"/>

    <!-- 筛选表单 -->
  
      <el-form :model="filters" inline>
        <el-form-item label="设备编号">
          <el-input v-model="filters.deviceId" placeholder="请输入设备编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="图案编号">
          <el-input v-model="filters.patternCode" placeholder="请输入图案编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="适用机型">
          <el-select v-model="filters.machineModel" placeholder="请选择机型" clearable style="width: 150px;">
            <el-option label="印刷机 A" value="A" />
            <el-option label="印刷机 B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="filterPatterns">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <!-- <el-button type="success" @click="openDialog">上传图案</el-button> -->
        </el-form-item>
      </el-form>


      <!-- 图案表格展示 -->
      <el-card class="table-card" shadow="hover">
        <el-table :data="filteredPatterns" border style="width: 100%">
          <el-table-column prop="planId" label="计划编号">
            <template #default="{row}">
              {{ planMap[row.planId] || row.planId }}
            </template>
          </el-table-column>
          <el-table-column prop="taskId" label="任务编号" width="120">
            <template #default="{row}">
              {{ taskMap[row.taskId] || row.taskId }}
            </template>
          </el-table-column>
          <el-table-column prop="deviceId" label="设备编号">
            <template #default="{row}">
              {{ deviceMap[row.deviceId] || row.deviceId }}
            </template>
          </el-table-column>
          <el-table-column label="任务数量" width="120" align="center">
            <template #default="{ row }">
              <el-tag >{{ row.quantity }} 件</el-tag>
            </template>
          </el-table-column>
          <!-- <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <el-progress :percentage="getProgress(row.status)" :color="progressColor(row.status)" />
            </template>
          </el-table-column> -->
          <el-table-column prop="patternCode" label="图案编号" width="120" />
          <el-table-column prop="patternName" label="图案名称" />
          <el-table-column label="预览图" width="120">
            <template #default="{ row }">
              <el-image 
                :src="row.imageUrl ? `http://localhost:7000${row.imageUrl}` : ''" 
                fit="cover" 
                style="width: 60px; height: 60px"
                :preview-src-list="row.imageUrl ? [`http://localhost:7000${row.imageUrl}`] : []"
              />
            </template>
          </el-table-column>
          <el-table-column prop="machineModel" label="适用机型" width="120" />
          <el-table-column prop="defaultPrintSpeed" label="印刷速度(次/小时)" />
          <el-table-column prop="defaultPressure" label="印刷压力(kg/cm²)" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editPattern(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="confirmDelete(row)">删除</el-button>
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

    <!-- 图案上传弹窗 -->
    <el-dialog v-model="dialogVisible" title="上传 / 编辑图案" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划编号">
          <el-input v-model="form.planId" :disabled="true" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="form.taskId" :disabled="true" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="form.deviceId" :disabled="true" />
        </el-form-item>
        <el-form-item label="图案编号">
          <el-input v-model="form.patternCode" />
        </el-form-item>
        <el-form-item label="图案名称">
          <el-input v-model="form.patternName" />
        </el-form-item>
        <el-form-item label="适用机型">
          <el-select v-model="form.machineModel">
            <el-option label="印刷机 A" value="A" />
            <el-option label="印刷机 B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="印刷速度">
          <el-input-number v-model="form.defaultPrintSpeed" :min="0" />
        </el-form-item>
        <el-form-item label="印刷压力">
          <el-input-number v-model="form.defaultPressure" :min="0" />
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
import HeaderSection from '@/components/HeaderSection.vue'
import TopNavBar from '@/components/NavBar.vue'
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessageBox } from 'element-plus'

const dialogVisible = ref(false)
const filters = ref({ patternCode: '', machine: '' })
const patterns = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 过滤后的图案列表
const filteredPatterns = computed(() => {
  return patterns.value.filter(p =>
    (!filters.value.patternCode || p.patternCode.includes(filters.value.patternCode)) &&
    (!filters.value.machineModel || p.machineModel === filters.value.machineModel) &&
    (!filters.value.deviceId || p.deviceId.includes(filters.value.deviceId))
  )
})

// 表单数据
const form = ref({ 
  patternCode: '', 
  patternName: '', 
  machineModel: '', 
  image: '',
})

// 筛选图案
const filterPatterns = () => {}
// 重置筛选条件
const resetFilters = () => { 
  filters.value = { deviceId: '', patternCode: '', machineModel: '' } 
}
// 打开弹窗
const openDialog = () => {
  dialogVisible.value = true
  form.value = {}
}

// 编辑图案
const editPattern = (row) => {
  dialogVisible.value = true;
  form.value = { 
    ...row,
    // 确保包含所有字段
    id: row.id,
    patternCode: row.patternCode,
    patternName: row.patternName,
    machineModel: row.machineModel,
    defaultPrintSpeed: row.defaultPrintSpeed,
    defaultPressure: row.defaultPressure,
    imageUrl: row.imageUrl,
    planId: row.planId,
    taskId: row.taskId,
    deviceId: row.deviceId
  };
}

// 更新印刷图案
const updatePrintPattern = async (id, formData) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/api/v1/process/print-patterns/${id}`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('更新印刷图案失败:', error);
    throw error;
  }
};

// 修改保存图案方法
const savePattern = async () => {
  try {
    const formData = new FormData();
    formData.append('patternCode', form.value.patternCode);
    formData.append('patternName', form.value.patternName);
    formData.append('machineModel', form.value.machineModel);
    formData.append('defaultPrintSpeed', form.value.defaultPrintSpeed);
    formData.append('defaultPressure', form.value.defaultPressure);
    formData.append('planId', form.value.planId);
    formData.append('taskId', form.value.taskId);
    formData.append('deviceId', form.value.deviceId);
    formData.append('imageFile', form.value.imageFile);


    if (form.value.id) {
      // 编辑逻辑
      const updatedData = await updatePrintPattern(form.value.id, formData);
      const index = patterns.value.findIndex(p => p.id === form.value.id);
      if (index !== -1) {
        patterns.value[index] = { ...patterns.value[index], ...updatedData };
      }
    } else {
      // 新增逻辑
      const response = await axios.post(
        'http://localhost:8080/api/v1/process/print-patterns',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );
      patterns.value.unshift(response.data);
      total.value++;
    }
    dialogVisible.value = false;
  } catch (error) {
    console.error('保存失败:', error);
  }
};

// 修改图片上传处理方法
const handleImageChange = (file) => {
  form.value.image = URL.createObjectURL(file.raw);
  form.value.imageFile = file.raw; // 保存原始文件对象
}

// 获取印刷图案列表
const fetchPrintPatterns = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/process/print-patterns/all', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        sort: 'id,desc'
      }
    })
    patterns.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取印刷图案列表失败:', error)
  }
}

// 页码变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPrintPatterns()
}

// 每页数量变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPrintPatterns()
}

// 删除印刷图案
const deletePrintPattern = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/v1/process/print-patterns/${id}`);

  } catch (error) {
    console.error('删除印刷图案失败:', error);
    throw error;
  }
};

// 确认删除
const confirmDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该图案吗？此操作不可逆！',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    await deletePrintPattern(row.id);
    // 更新本地图案列表
    const index = patterns.value.findIndex(p => p.id === row.id);
    if (index !== -1) {
      patterns.value.splice(index, 1);
      total.value--;
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 可添加错误提示
    }
  }
};
// 组件挂载时获取数据
// 添加映射关系
const planMap = ref({})
const taskMap = ref({})
const deviceMap = ref({})

// 获取计划映射
const loadPlanMap = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/production/plans?page=0&size=1000')
    planMap.value = {}
    response.data.content.forEach(plan => {
      planMap.value[plan.id] = plan.planCode
    })
  } catch (error) {
    console.error('获取计划映射失败:', error)
  }
}

// 获取任务映射
const loadTaskMap = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/production/tasks?page=0&size=1000')
    taskMap.value = {}
    response.data.content.forEach(task => {
      taskMap.value[task.id] = task.taskCode
    })
  } catch (error) {
    console.error('获取任务映射失败:', error)
  }
}

// 获取设备映射
const loadDeviceMap = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/equipment/devices?page=0&size=1000')
    deviceMap.value = {}
    response.data.content.forEach(device => {
      deviceMap.value[device.id] = device.deviceCode
    })
  } catch (error) {
    console.error('获取设备映射失败:', error)
  }
}

// 在onMounted中添加这些调用
onMounted(() => {
  fetchPrintPatterns()
  loadPlanMap()
  loadTaskMap()
  loadDeviceMap()
})
</script>

<style scoped>
.pattern-page {
  padding: 20px;
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

.el-pagination {
  margin-top: 16px;
  text-align: right;
}
</style>