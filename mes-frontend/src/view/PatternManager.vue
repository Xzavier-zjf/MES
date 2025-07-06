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
    <el-card shadow="hover" class="table-card" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="计划编号">
          <el-input v-model="filters.planId" placeholder="请输入计划编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="filters.taskId" placeholder="请输入任务编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="filters.deviceId" placeholder="请输入设备编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="图案编号">
          <el-input v-model="filters.patternCode" placeholder="请输入图案编号" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="图案名称">
          <el-input v-model="filters.patternName" placeholder="请输入图案名称" clearable style="width: 150px;" />
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
          <el-button type="success" @click="openDialog">新增图案</el-button>
        </el-form-item>
      </el-form>
    </el-card>


      <!-- 图案表格展示 -->
      <el-card class="table-card" shadow="hover">
        <!-- 调试信息区域 -->
        <div style="background: #f0f9ff; padding: 12px; margin-bottom: 16px; border-radius: 6px; font-size: 12px;">
          <strong>调试信息:</strong> 
          图案总数: {{ patterns.length }} | 
          筛选后: {{ filteredPatterns.length }} | 
          当前页: {{ currentPage }} | 
          每页数量: {{ pageSize }}
          <br>
          <span v-if="patterns.length > 0">
            第一个图案: ID={{ patterns[0].id }}, 编号={{ patterns[0].patternCode }}, 名称={{ patterns[0].patternName }}
          </span>
        </div>
        
        <!-- 筛选统计信息 -->
        <div class="filter-stats" v-if="hasActiveFilters">
          <el-tag type="info" size="small">
            筛选结果: {{ filteredPatterns.length }} 条记录
          </el-tag>
          <el-tag type="warning" size="small" style="margin-left: 10px;">
            总记录: {{ patterns.length }} 条
          </el-tag>
        </div>
        
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
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="editPattern(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="confirmDelete(row)">删除</el-button>
              <el-button size="small" type="info" @click="debugRow(row)">调试</el-button>
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
      <!-- 调试信息 -->
      <div v-if="isEdit" style="background: #f0f9ff; padding: 8px; margin-bottom: 16px; border-radius: 4px; font-size: 12px;">
        <strong>调试信息:</strong> 编辑模式 - ID: {{ form.id }} | 图案编号: {{ form.patternCode }} | 图案名称: {{ form.patternName }}
      </div>
      
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划编号">
          <el-select v-model="form.planId" placeholder="请选择计划" clearable>
            <el-option 
              v-for="plan in availablePlans" 
              :key="plan.id" 
              :label="plan.planCode" 
              :value="plan.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务编号">
          <el-select v-model="form.taskId" placeholder="请选择任务" clearable>
            <el-option 
              v-for="task in availableTasks" 
              :key="task.id" 
              :label="task.taskCode" 
              :value="task.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备编号">
          <el-select v-model="form.deviceId" placeholder="请选择设备" clearable>
            <el-option 
              v-for="device in availableDevices" 
              :key="device.id" 
              :label="device.deviceCode" 
              :value="device.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图案编号">
          <el-input v-model="form.patternCode" placeholder="请输入图案编号" />
        </el-form-item>
        <el-form-item label="图案名称">
          <el-input v-model="form.patternName" placeholder="请输入图案名称" />
        </el-form-item>
        <el-form-item label="适用机型">
          <el-select v-model="form.machineModel" placeholder="请选择机型">
            <el-option label="印刷机 A" value="A" />
            <el-option label="印刷机 B" value="B" />
          </el-select>
        </el-form-item>
        <el-form-item label="印刷速度">
          <el-input-number v-model="form.defaultPrintSpeed" :min="0" placeholder="印刷速度(次/小时)" />
        </el-form-item>
        <el-form-item label="印刷压力">
          <el-input-number v-model="form.defaultPressure" :min="0" placeholder="印刷压力(kg/cm²)" />
        </el-form-item>
        <el-form-item label="图案图片">
          <div class="image-upload-container">
            <!-- 上传方式选择 -->
            <el-radio-group v-model="imageUploadType" style="margin-bottom: 12px;">
              <el-radio label="url">图片URL</el-radio>
              <el-radio label="file">文件上传</el-radio>
              <el-radio label="path">文件路径</el-radio>
            </el-radio-group>
            
            <!-- URL输入 -->
            <div v-if="imageUploadType === 'url'" class="upload-section">
              <div class="url-input-group">
                <el-input 
                  v-model="form.imageUrl" 
                  placeholder="请输入图片URL，如: https://example.com/image.jpg" 
                  clearable
                />
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="validateImage(form.imageUrl)"
                  :disabled="!form.imageUrl"
                >
                  验证
                </el-button>
              </div>
              <div class="upload-tip">支持http/https链接的图片URL</div>
            </div>
            
            <!-- 文件上传 -->
            <div v-if="imageUploadType === 'file'" class="upload-section">
              <el-upload
                ref="uploadRef"
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="1"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :before-upload="beforeFileUpload"
                accept="image/*"
              >
                <template #default>
                  <div class="upload-trigger">
                    <i class="el-icon-plus"></i>
                    <div class="upload-text">点击上传</div>
                  </div>
                </template>
                <template #file="{ file }">
                  <div class="upload-preview">
                    <img :src="file.url" class="preview-image" />
                    <div class="file-info">
                      <span>{{ file.name }}</span>
                      <span class="file-size">{{ formatFileSize(file.size) }}</span>
                    </div>
                  </div>
                </template>
              </el-upload>
              <div class="upload-tip">支持JPG、PNG、GIF等图片格式，最大5MB</div>
            </div>
            
            <!-- 文件路径 -->
            <div v-if="imageUploadType === 'path'" class="upload-section">
              <el-input 
                v-model="form.imagePath" 
                placeholder="请输入服务器图片路径，如: /uploads/patterns/image.jpg" 
                clearable
              />
              <div class="upload-tip">输入服务器上的图片文件路径</div>
            </div>
            
            <!-- 图片预览 -->
            <div v-if="previewImageUrl" class="image-preview">
              <div class="preview-header">
                <span>图片预览</span>
                <el-button type="text" size="small" @click="clearImage">清除</el-button>
              </div>
              <img :src="previewImageUrl" class="preview-img" @error="handleImageError" />
            </div>
          </div>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getPrintPatterns, updatePrintPattern, deletePrintPattern, createPrintPattern } from '@/api/pattern'
import { getPlans } from '@/api/plans'
import { getTasks } from '@/api/tasks'
import { getDevices } from '@/api/devices'
import { uploadImage, getImageUrl, validateImageUrl } from '@/api/upload'
import { useAppStore } from '@/stores'

const appStore = useAppStore()

const dialogVisible = ref(false)
const isEdit = ref(false)
const filters = ref({ 
  planId: '', 
  taskId: '', 
  deviceId: '', 
  patternCode: '', 
  patternName: '', 
  machineModel: '' 
})
const patterns = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 检查是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return Object.values(filters.value).some(value => value && value.toString().trim() !== '');
})

// 过滤后的图案列表
const filteredPatterns = computed(() => {
  return patterns.value.filter(p => {
    // 计划编号筛选 - 支持模糊匹配
    const planMatch = !filters.value.planId || 
      (p.planId && p.planId.toString().toLowerCase().includes(filters.value.planId.toLowerCase())) ||
      (planMap.value[p.planId] && planMap.value[p.planId].toLowerCase().includes(filters.value.planId.toLowerCase()));
    
    // 任务编号筛选 - 支持模糊匹配
    const taskMatch = !filters.value.taskId || 
      (p.taskId && p.taskId.toString().toLowerCase().includes(filters.value.taskId.toLowerCase())) ||
      (taskMap.value[p.taskId] && taskMap.value[p.taskId].toLowerCase().includes(filters.value.taskId.toLowerCase()));
    
    // 设备编号筛选 - 支持模糊匹配
    const deviceMatch = !filters.value.deviceId || 
      (p.deviceId && p.deviceId.toString().toLowerCase().includes(filters.value.deviceId.toLowerCase())) ||
      (deviceMap.value[p.deviceId] && deviceMap.value[p.deviceId].toLowerCase().includes(filters.value.deviceId.toLowerCase()));
    
    // 图案编号筛选 - 支持模糊匹配
    const patternCodeMatch = !filters.value.patternCode || 
      (p.patternCode && p.patternCode.toLowerCase().includes(filters.value.patternCode.toLowerCase()));
    
    // 图案名称筛选 - 支持模糊匹配
    const patternNameMatch = !filters.value.patternName || 
      (p.patternName && p.patternName.toLowerCase().includes(filters.value.patternName.toLowerCase()));
    
    // 适用机型筛选 - 精确匹配
    const machineModelMatch = !filters.value.machineModel || 
      p.machineModel === filters.value.machineModel;
    
    return planMatch && taskMatch && deviceMatch && patternCodeMatch && patternNameMatch && machineModelMatch;
  })
})

// 表单数据
const form = ref({ 
  patternCode: '', 
  patternName: '', 
  machineModel: '', 
  imageUrl: '',
  imagePath: ''
})

// 图片上传相关
const imageUploadType = ref('url')
const previewImageUrl = ref('')
const uploadRef = ref(null)

// 筛选图案
const filterPatterns = () => {
  console.log('执行筛选，条件:', filters.value)
  console.log('筛选结果数量:', filteredPatterns.value.length)
  // 筛选逻辑已在 computed 中实现，这里可以添加额外的筛选逻辑
}

// 重置筛选条件
const resetFilters = () => { 
  filters.value = { 
    planId: '', 
    taskId: '', 
    deviceId: '', 
    patternCode: '', 
    patternName: '', 
    machineModel: '' 
  } 
  console.log('重置筛选条件')
}
// 打开弹窗
const openDialog = () => {
  dialogVisible.value = true
  isEdit.value = false
  
  // 重置表单数据
  form.value = {
    id: null,
    patternCode: '',
    patternName: '',
    machineModel: '',
    defaultPrintSpeed: 0,
    defaultPressure: 0,
    planId: '',
    taskId: '',
    deviceId: '',
    imageUrl: '',
    imagePath: ''
  }
  
  // 重置上传相关状态
  imageUploadType.value = 'url'
  previewImageUrl.value = ''
  
  // 清除上传组件
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  
  console.log('新增图案，初始化表单:', form.value)
}

// 监听图片URL变化
watch(() => form.value.imageUrl, (newUrl) => {
  if (imageUploadType.value === 'url' && newUrl) {
    updatePreviewImage();
  }
})

// 监听图片路径变化
watch(() => form.value.imagePath, (newPath) => {
  if (imageUploadType.value === 'path' && newPath) {
    updatePreviewImage();
  }
})

// 监听上传方式变化
watch(imageUploadType, (newType) => {
  if (newType === 'url') {
    form.value.imagePath = '';
  } else if (newType === 'path') {
    form.value.imageUrl = '';
  } else if (newType === 'file') {
    form.value.imageUrl = '';
    form.value.imagePath = '';
  }
  previewImageUrl.value = '';
})

// 编辑图案
const editPattern = (row) => {
  console.log('编辑图案数据:', row)
  
  // 验证行数据
  if (!row || !row.id) {
    console.error('编辑图案失败：无效的行数据', row)
    ElMessage.error('编辑失败：数据无效')
    return
  }
  
  dialogVisible.value = true;
  isEdit.value = true;
  
  // 重置表单数据
  form.value = {
    id: row.id,
    patternCode: row.patternCode || '',
    patternName: row.patternName || '',
    machineModel: row.machineModel || '',
    defaultPrintSpeed: row.defaultPrintSpeed || 0,
    defaultPressure: row.defaultPressure || 0,
    planId: row.planId || '',
    taskId: row.taskId || '',
    deviceId: row.deviceId || '',
    imageUrl: row.imageUrl || '',
    imagePath: row.imageUrl || ''
  };
  
  // 根据现有数据判断上传方式
  if (row.imageUrl && row.imageUrl.startsWith('http')) {
    imageUploadType.value = 'url'
  } else if (row.imageUrl && row.imageUrl.startsWith('/')) {
    imageUploadType.value = 'path'
  } else {
    imageUploadType.value = 'url'
  }
  
  // 更新预览图片
  updatePreviewImage()
  console.log('编辑图案，表单数据:', form.value)
  console.log('编辑图案，上传方式:', imageUploadType.value)
}

// 更新印刷图案
const updatePrintPatternLocal = async (id, formData) => {
  try {
    const response = await updatePrintPattern(id, formData);
    return response;
  } catch (error) {
    console.error('更新印刷图案失败:', error);
    throw error;
  }
};

// 修改保存图案方法
const savePattern = async () => {
  try {
    console.log('保存图案数据:', form.value)
    
    // 验证必填字段
    if (!form.value.patternCode || !form.value.patternName) {
      alert('请填写图案编号和图案名称')
      return
    }
    
    // 根据上传方式确定图片字段
    let imageField = '';
    if (imageUploadType.value === 'url') {
      imageField = form.value.imageUrl || '';
    } else if (imageUploadType.value === 'path') {
      imageField = form.value.imagePath || '';
    } else if (imageUploadType.value === 'file') {
      imageField = form.value.imageUrl || '';
    }
    
    // 构建请求数据对象
    const requestData = {
      patternCode: form.value.patternCode || '',
      patternName: form.value.patternName || '',
      machineModel: form.value.machineModel || '',
      defaultPrintSpeed: form.value.defaultPrintSpeed || 0,
      defaultPressure: form.value.defaultPressure || 0,
      planId: form.value.planId || '',
      taskId: form.value.taskId || '',
      deviceId: form.value.deviceId || '',
      imageUrl: imageField
    }

    // 调试：打印请求数据
    console.log('请求数据:', requestData);

    if (form.value.id) {
      // 编辑逻辑
      console.log('执行编辑操作，ID:', form.value.id)
      const updatedData = await updatePrintPatternLocal(form.value.id, requestData);
      const index = patterns.value.findIndex(p => p.id === form.value.id);
      if (index !== -1) {
        patterns.value[index] = { ...patterns.value[index], ...updatedData };
      }
      console.log('编辑成功')
    } else {
      // 新增逻辑
      console.log('执行新增操作')
      const response = await createPrintPattern(requestData);
      patterns.value.unshift(response);
      total.value++;
      console.log('新增成功')
    }
    dialogVisible.value = false;
  } catch (error) {
    console.error('保存失败:', error);
    alert('保存失败: ' + (error.message || '未知错误'));
  }
};

// 图片处理相关方法
const handleImageUrlChange = (url) => {
  form.value.imageUrl = url;
  updatePreviewImage();
}

// 文件上传处理
const handleFileChange = async (file) => {
  console.log('文件上传:', file);
  try {
    if (file.raw) {
      // 显示上传进度
      ElMessage.info('正在上传图片...');
      
      // 上传到服务器
      const uploadResult = await uploadImage(file.raw);
      console.log('上传结果:', uploadResult);
      
      // 设置图片URL
      form.value.imageUrl = uploadResult.url || uploadResult.path;
      previewImageUrl.value = getImageUrl(form.value.imageUrl);
      
      ElMessage.success('图片上传成功');
    }
  } catch (error) {
    console.error('文件上传失败:', error);
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'));
  }
}

// 文件移除处理
const handleFileRemove = () => {
  form.value.imageUrl = '';
  previewImageUrl.value = '';
}

// 文件上传前验证
const beforeFileUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return false;
  }
  return true;
}

// 更新预览图片
const updatePreviewImage = () => {
  if (imageUploadType.value === 'url' && form.value.imageUrl) {
    previewImageUrl.value = form.value.imageUrl;
  } else if (imageUploadType.value === 'path' && form.value.imagePath) {
    // 使用API获取完整的图片URL
    previewImageUrl.value = getImageUrl(form.value.imagePath);
  }
}

// 清除图片
const clearImage = () => {
  form.value.imageUrl = '';
  form.value.imagePath = '';
  previewImageUrl.value = '';
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

// 图片加载错误处理
const handleImageError = () => {
  ElMessage.warning('图片加载失败，请检查URL是否正确');
  previewImageUrl.value = '';
}

// 验证图片URL
const validateImage = async (url) => {
  if (!url) return false;
  
  try {
    const isValid = await validateImageUrl(url);
    if (!isValid) {
      ElMessage.warning('图片URL无效，请检查链接是否正确');
    }
    return isValid;
  } catch (error) {
    console.error('图片验证失败:', error);
    ElMessage.warning('图片验证失败');
    return false;
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// 获取印刷图案列表
const fetchPrintPatterns = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'id,desc'
    }
    console.log('获取图案列表，参数:', params)
    const response = await getPrintPatterns(params)
    console.log('获取图案列表响应:', response)
    patterns.value = response.content || []
    total.value = response.totalElements || 0
    console.log('图案列表数据:', patterns.value)
  } catch (error) {
    console.error('获取印刷图案列表失败:', error)
    patterns.value = []
    total.value = 0
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
const deletePrintPatternLocal = async (id) => {
  try {
    await deletePrintPattern(id);
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
    await deletePrintPatternLocal(row.id);
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

// 可用的计划、任务、设备数据
const availablePlans = ref([])
const availableTasks = ref([])
const availableDevices = ref([])

// 获取计划映射和可用计划
const loadPlanMap = async () => {
  try {
    const response = await getPlans(0, 1000)
    planMap.value = {}
    availablePlans.value = response.content || []
    response.content.forEach(plan => {
      planMap.value[plan.id] = plan.planCode
    })
  } catch (error) {
    console.error('获取计划映射失败:', error)
  }
}

// 获取任务映射和可用任务
const loadTaskMap = async () => {
  try {
    const response = await getTasks({ page: 0, size: 1000 })
    taskMap.value = {}
    availableTasks.value = response.content || []
    response.content.forEach(task => {
      taskMap.value[task.id] = task.taskCode
    })
  } catch (error) {
    console.error('获取任务映射失败:', error)
  }
}

// 获取设备映射和可用设备
const loadDeviceMap = async () => {
  try {
    const response = await getDevices({ page: 0, size: 1000 })
    deviceMap.value = {}
    availableDevices.value = response.content || []
    response.content.forEach(device => {
      deviceMap.value[device.id] = device.deviceCode
    })
  } catch (error) {
    console.error('获取设备映射失败:', error)
  }
}

// 数据检查函数
const checkDataIntegrity = () => {
  console.log('=== 数据完整性检查 ===')
  console.log('图案列表数量:', patterns.value.length)
  console.log('可用计划数量:', availablePlans.value.length)
  console.log('可用任务数量:', availableTasks.value.length)
  console.log('可用设备数量:', availableDevices.value.length)
  console.log('计划映射:', planMap.value)
  console.log('任务映射:', taskMap.value)
  console.log('设备映射:', deviceMap.value)
  
  if (patterns.value.length > 0) {
    console.log('第一个图案数据:', patterns.value[0])
  }
}

// 调试行数据
const debugRow = (row) => {
  console.log('=== 调试行数据 ===')
  console.log('完整行数据:', row)
  console.log('行数据类型:', typeof row)
  console.log('行数据键:', Object.keys(row))
  console.log('ID:', row.id)
  console.log('图案编号:', row.patternCode)
  console.log('图案名称:', row.patternName)
  console.log('适用机型:', row.machineModel)
  console.log('印刷速度:', row.defaultPrintSpeed)
  console.log('印刷压力:', row.defaultPressure)
  console.log('计划ID:', row.planId)
  console.log('任务ID:', row.taskId)
  console.log('设备ID:', row.deviceId)
  console.log('图片URL:', row.imageUrl)
  
  // 显示在页面上
  alert(`调试信息:\nID: ${row.id}\n图案编号: ${row.patternCode}\n图案名称: ${row.patternName}\n适用机型: ${row.machineModel}`)
}

// 在onMounted中添加这些调用
onMounted(async () => {
  await fetchPrintPatterns()
  await loadPlanMap()
  await loadTaskMap()
  await loadDeviceMap()
  
  // 检查数据完整性
  checkDataIntegrity()
  
  // 启动自动刷新
  appStore.startAutoRefresh(30000) // 30秒刷新一次
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})
</script>

<style scoped>
.pattern-page {
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
  flex-wrap: wrap;
}

/* 筛选统计信息 */
.filter-stats {
  margin-bottom: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

/* 分页组件 */
.el-pagination {
  margin-top: 16px;
  text-align: right;
}

/* 图片上传组件样式 */
.image-upload-container {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  background: #fafafa;
}

.upload-section {
  margin-top: 12px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-trigger:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.upload-text {
  margin-top: 8px;
  color: #606266;
  font-size: 14px;
}

.upload-preview {
  position: relative;
  border-radius: 6px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100px;
  object-fit: cover;
}

.file-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4px 8px;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
}

.file-size {
  color: #ccc;
}

.image-preview {
  margin-top: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
  background: white;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-size: 14px;
  font-weight: 500;
}

.preview-img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  display: block;
}

/* URL输入组样式 */
.url-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.url-input-group .el-input {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }
  
  .form-row {
    flex-direction: column;
  }
  
  .image-upload-container {
    padding: 12px;
  }
  
  .url-input-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>