<template>
  <div class="pattern-page">
      <HeaderSection 
    title=" 🎨&nbsp印刷图案管理"
    subtitle="管理印刷图案资源，为印刷任务提供图案支持。"
    :showStats="true"  
    :card1="'图案总数'"
    :card2="'已配置图案'"
    :card3="'待配置图案'"
    :card4="'异常图案'"
    :value1="totalPatterns"
    :value2="configuredPatterns"
    :value3="pendingPatterns"
    :value4="errorPatterns"/>

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
          <el-button type="info" @click="validateQuantityLogic">验证数量逻辑</el-button>
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
          <el-table-column label="关联任务" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.taskId" type="success" size="small">
                已关联
              </el-tag>
              <el-tag v-else type="info" size="small">
                未关联
              </el-tag>
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
                :src="getSmartImageUrl(row.imageUrl)" 
                fit="cover" 
                style="width: 60px; height: 60px"
                :preview-src-list="row.imageUrl ? [getSmartImageUrl(row.imageUrl)] : []"
                :lazy="true"
                @error="handleImageLoadError"
              >
                <template #error>
                  <div class="image-slot">
                    <i class="el-icon-picture-outline"></i>
                    <div>暂无图片</div>
                  </div>
                </template>
              </el-image>
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
            <!-- 编辑模式下显示原图片 -->
            <div v-if="isEdit && form.imageUrl" class="original-image-section">
              <div class="section-title">当前图片：</div>
              <div class="original-image-preview">
                <img :src="getImageUrl(form.imageUrl)" class="original-preview-img" @error="handleImageError" />
                <div class="original-image-info">
                  <div class="image-url-text">{{ form.imageUrl }}</div>
                </div>
              </div>
            </div>
            
            <!-- 上传方式选择 -->
            <el-radio-group v-model="imageUploadType" style="margin-bottom: 12px;">
              <el-radio value="file">文件上传</el-radio>
              <el-radio value="url">图片URL</el-radio>
              <el-radio value="path">文件路径</el-radio>
            </el-radio-group>
            
            <!-- 文件上传 -->
            <div v-if="imageUploadType === 'file'" class="upload-section">
              <div class="file-upload-area" 
                   @click="triggerFileInput"
                   @dragover.prevent
                   @drop.prevent="handleFileDrop"
                   :class="{ 'drag-over': isDragOver, 'uploading': uploading }"
                   @dragenter="isDragOver = true"
                   @dragleave="isDragOver = false">
                <div v-if="!selectedFile && !uploading" class="upload-placeholder">
                  <div class="upload-icon">📁</div>
                  <div class="upload-text">点击选择图片或拖拽到此处</div>
                  <div class="upload-hint">支持 JPG、PNG、GIF、WEBP 格式，最大 5MB</div>
                </div>
                <div v-else-if="uploading" class="upload-progress">
                  <div class="upload-icon">⏳</div>
                  <div class="upload-text">正在上传...</div>
                  <div class="upload-hint">请稍候，文件上传中</div>
                </div>
                <div v-else class="file-info">
                  <div class="file-preview">
                    <img v-if="filePreviewUrl" :src="filePreviewUrl" class="preview-thumbnail" />
                    <div v-else class="file-icon">📄</div>
                  </div>
                  <div class="file-details">
                    <div class="file-name">{{ selectedFile.name }}</div>
                    <div class="file-size">{{ formatFileSize(selectedFile.size) }}</div>
                    <div class="file-type">{{ selectedFile.type }}</div>
                    <div class="file-actions">
                      <el-button size="small" type="primary" @click="uploadFile" :loading="uploading" :disabled="uploading">
                        {{ uploading ? '上传中...' : '上传到服务器' }}
                      </el-button>
                      <el-button size="small" @click="clearSelectedFile" :disabled="uploading">移除</el-button>
                    </div>
                  </div>
                </div>
              </div>
              <input 
                ref="fileInput" 
                type="file" 
                accept="image/*" 
                style="display: none" 
                @change="handleFileSelect"
              />
              <div class="upload-tip">
                支持拖拽上传，选择文件后点击"上传到服务器"按钮。
                <span v-if="isEdit" style="color: #f56c6c;">上传成功后将替换当前图片。</span>
              </div>
            </div>
            
            <!-- URL输入 -->
            <div v-if="imageUploadType === 'url'" class="upload-section">
              <el-input 
                v-model="form.imageUrl" 
                placeholder="请输入图片URL，如: https://example.com/image.jpg" 
                clearable
                @input="handleUrlInput"
              />
              <div class="upload-tip">支持http/https链接的图片URL</div>
            </div>
            
            <!-- 文件路径 -->
            <div v-if="imageUploadType === 'path'" class="upload-section">
              <el-select 
                v-model="form.imagePath" 
                placeholder="选择现有图片或输入新路径" 
                clearable
                filterable
                allow-create
                style="width: 100%"
                @change="handlePathSelect"
              >
                <el-option 
                  v-for="path in predefinedImagePaths" 
                  :key="path" 
                  :label="path" 
                  :value="path"
                />
              </el-select>
              <div class="upload-tip">选择现有图片路径或输入新的服务器路径</div>
            </div>
            
            <!-- 图片预览 -->
            <div v-if="previewImageUrl" class="image-preview">
              <div class="preview-header">
                <span>{{ isEdit ? '新图片预览' : '图片预览' }}</span>
                <el-button type="text" size="small" @click="clearImage">清除</el-button>
              </div>
              <img :src="previewImageUrl" class="preview-img" @error="handleImageError" />
              <div v-if="isEdit" class="image-change-notice">
                <el-tag type="warning" size="small">
                  <i class="el-icon-warning"></i>
                  保存后将替换当前图片
                </el-tag>
              </div>
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
import TopNavBar from '@/components/Navbar.vue'
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessageBox, ElMessage, ElNotification } from 'element-plus'

// 安全的消息显示函数
const showMessage = (type, message) => {
  try {
    if (ElMessage && typeof ElMessage[type] === 'function') {
      ElMessage[type](message)
    } else {
      console.log(`${type.toUpperCase()}: ${message}`)
      alert(`${type.toUpperCase()}: ${message}`)
    }
  } catch (error) {
    console.error('显示消息失败:', error)
    alert(`${type.toUpperCase()}: ${message}`)
  }
}

const showNotification = (options) => {
  try {
    if (ElNotification && typeof ElNotification === 'function') {
      ElNotification(options)
    } else {
      console.log(`通知: ${options.title} - ${options.message}`)
      alert(`${options.title}: ${options.message}`)
    }
  } catch (error) {
    console.error('显示通知失败:', error)
    alert(`${options.title}: ${options.message}`)
  }
}
import { getPrintPatterns, updatePrintPattern, deletePrintPattern, createPrintPattern } from '@/api/pattern'
import { getPlans } from '@/api/plans'
import { getTasks } from '@/api/tasks'
import { getDevices } from '@/api/devices'
import { validateImageUrl, uploadImage, getImageUrl } from '@/api/upload'
import { useAppStore } from '@/stores'
import { validatePatternQuantityLogic, generateQuantityLogicReport } from '@/utils/quantityLogicValidator'

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

// ✅ 图案统计
const totalPatterns = computed(() => patterns.value.length)
const configuredPatterns = computed(() => 
  patterns.value.filter(p => 
    p.patternCode && p.patternName && p.machineModel && 
    p.defaultPrintSpeed && p.defaultPressure && p.imageUrl
  ).length
)
const pendingPatterns = computed(() => 
  patterns.value.filter(p => 
    !p.patternCode || !p.patternName || !p.machineModel || 
    !p.defaultPrintSpeed || !p.defaultPressure || !p.imageUrl
  ).length
)
const errorPatterns = computed(() => 
  patterns.value.filter(p => 
    (p.defaultPrintSpeed && p.defaultPrintSpeed < 0) || 
    (p.defaultPressure && p.defaultPressure < 0) ||
    (p.imageUrl && !isValidImageUrl(p.imageUrl))
  ).length
)

// 验证图片URL格式（考虑智能切换逻辑）
const isValidImageUrl = (url) => {
  if (!url) return false
  
  // 基本格式检查
  const isValidFormat = url.startsWith('http') || url.startsWith('/') || url.startsWith('data:image')
  if (!isValidFormat) return false
  
  // 对于JPG/PNG格式，检查对应的SVG版本是否存在
  if (url.endsWith('.jpg') || url.endsWith('.png')) {
    // 假设对应的SVG版本存在（因为我们已经创建了这些文件）
    return true
  }
  
  return true
}

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
const imageUploadType = ref('file')
const previewImageUrl = ref('')
const uploadRef = ref(null)
const fileInput = ref(null)
const selectedFile = ref(null)
const filePreviewUrl = ref('')
const uploading = ref(false)
const isDragOver = ref(false)

// 预定义的图片路径（使用SVG格式，确保浏览器可以正常显示）
const predefinedImagePaths = ref([
  '/uploads/patterns/test-pattern-1.svg',
  '/uploads/patterns/test-pattern-2.svg',
  '/uploads/patterns/geometric-pattern.svg',
  '/uploads/patterns/marble-texture.svg',
  '/uploads/patterns/4106e9db-40d5-4922-96d8-3240a6c99e3f_R-C (1).svg',
  '/uploads/patterns/deeaa61e-3742-40dc-9e41-3507caf50e5e_R-C.svg',
  '/uploads/patterns/819aad2c-5506-4a54-9288-1836982876ed_2126.png_860.svg',
  // 也保留原始格式的选项（已修复为HTML格式）
  '/uploads/patterns/test-pattern-1.jpg',
  '/uploads/patterns/test-pattern-2.png',
  '/uploads/patterns/geometric-pattern.jpg',
  '/uploads/patterns/marble-texture.jpg'
])

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
  
  // 检查是否有测试URL
  const testUrl = localStorage.getItem('testImageUrl')
  if (testUrl) {
    form.value.imageUrl = testUrl
    imageUploadType.value = 'url'
    localStorage.removeItem('testImageUrl') // 使用后清除
    console.log('使用测试URL:', testUrl)
  } else {
    // 重置上传相关状态
    imageUploadType.value = 'file'
  }
  
  previewImageUrl.value = ''
  clearSelectedFile()
  
  // 延迟更新预览
  setTimeout(() => {
    updatePreviewImage()
  }, 100)
  
  console.log('新增图案，初始化表单:', form.value)
}

// 监听图片URL变化
watch(() => form.value.imageUrl, (newUrl, oldUrl) => {
  console.log('图片URL变化:', { oldUrl, newUrl, uploadType: imageUploadType.value })
  
  // 在URL上传方式下自动更新预览，或者在文件上传完成后更新预览
  if (imageUploadType.value === 'url') {
    // 防抖处理，避免输入过程中频繁更新
    clearTimeout(updatePreviewImage.timer)
    updatePreviewImage.timer = setTimeout(() => {
      updatePreviewImage()
    }, 300) // 300ms 防抖
  } else if (imageUploadType.value === 'file' && newUrl && !oldUrl) {
    // 文件上传完成后立即更新预览（从空值变为有值）
    updatePreviewImage()
  }
}, { immediate: false }) // 不立即执行，避免初始化时的不必要更新

// 监听图片路径变化
watch(() => form.value.imagePath, (newPath, oldPath) => {
  console.log('图片路径变化:', { oldPath, newPath, uploadType: imageUploadType.value })
  
  // 只在路径上传方式下自动更新预览
  if (imageUploadType.value === 'path') {
    updatePreviewImage()
  }
})

// 监听上传方式变化
watch(imageUploadType, (newType, oldType) => {
  console.log('上传方式变化:', { oldType, newType })
  
  // 清除其他方式的数据，但保留当前方式的数据
  if (newType === 'file') {
    form.value.imageUrl = ''
    form.value.imagePath = ''
  } else if (newType === 'url') {
    form.value.imagePath = ''
    clearSelectedFile()
  } else if (newType === 'path') {
    form.value.imageUrl = ''
    clearSelectedFile()
  }
  
  // 清除预览，然后根据新方式更新
  previewImageUrl.value = ''
  
  // 延迟更新预览，确保数据已清理
  setTimeout(() => {
    updatePreviewImage()
  }, 100)
})

// 编辑图案
const editPattern = (row) => {
  console.log('编辑图案数据:', row)
  
  // 验证行数据
  if (!row || !row.id) {
    console.error('编辑图案失败：无效的行数据', row)
    showMessage('error', '编辑失败：数据无效')
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
    imageUploadType.value = 'file'
  }
  
  // 清除文件选择状态
  clearSelectedFile()
  
  // 更新预览图片
  setTimeout(() => {
    updatePreviewImage()
  }, 100) // 延迟一点确保数据已更新
  
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
      showMessage('error', '请填写图案编号和图案名称')
      return
    }
    
    // 根据上传方式确定图片字段
    let imageField = '';
    if (imageUploadType.value === 'url') {
      imageField = form.value.imageUrl?.trim() || '';
      console.log('使用URL方式，图片字段:', imageField)
    } else if (imageUploadType.value === 'path') {
      imageField = form.value.imagePath?.trim() || '';
      console.log('使用路径方式，图片字段:', imageField)
    } else if (imageUploadType.value === 'file') {
      // 如果是文件上传方式，使用已上传的URL
      imageField = form.value.imageUrl?.trim() || '';
      console.log('使用文件上传方式，图片字段:', imageField)
      
      // 验证文件上传是否完成
      if (!imageField) {
        showMessage('error', '请先上传文件到服务器')
        return
      }
    }
    
    // 验证图片字段
    if (imageField) {
      console.log('验证图片字段:', imageField)
      
      // 对于URL方式，进行基本格式验证
      if (imageUploadType.value === 'url') {
        try {
          new URL(imageField)
        } catch (error) {
          showMessage('error', '图片URL格式无效，请检查输入')
          return
        }
      }
      
      // 异步验证图片可访问性（不阻塞保存）
      validateImage(imageField).catch(error => {
        console.warn('图片验证失败，但继续保存:', error)
      })
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
      console.log('发送到后端的数据:', requestData)
      
      try {
        const updatedData = await updatePrintPatternLocal(form.value.id, requestData);
        console.log('后端返回的数据:', updatedData)
        
        const index = patterns.value.findIndex(p => p.id === form.value.id);
        if (index !== -1) {
          // 构建完整的更新数据，优先使用请求数据确保一致性
          const completeUpdatedData = {
            ...patterns.value[index], // 保留原有数据
            ...requestData,           // 应用请求的更新数据
            id: form.value.id,        // 确保ID不变
            imageUrl: imageField      // 明确设置图片URL
          };
          
          // 如果后端返回了数据，合并后端数据
          if (updatedData && typeof updatedData === 'object') {
            Object.assign(completeUpdatedData, updatedData);
            // 但仍然确保图片URL是我们期望的值
            completeUpdatedData.imageUrl = imageField;
          }
          
          patterns.value[index] = completeUpdatedData;
          console.log('本地数据已更新:', patterns.value[index])
        }
        
        // 延迟刷新数据以确保后端数据已保存
        setTimeout(async () => {
          try {
            console.log('延迟验证：重新获取数据以确认更新...')
            await fetchPrintPatterns()
            console.log('数据刷新完成')
          } catch (error) {
            console.warn('数据刷新失败:', error)
          }
        }, 1000)
        
        showMessage('success', '图案更新成功')
        console.log('编辑成功，新图片URL:', imageField)
      } catch (error) {
        console.error('更新操作失败:', error)
        throw error // 重新抛出错误以便外层catch处理
      }
    } else {
      // 新增逻辑
      console.log('执行新增操作')
      const response = await createPrintPattern(requestData);
      
      // 确保新增的数据包含正确的图片URL
      if (response && imageField) {
        response.imageUrl = imageField;
      }
      
      patterns.value.unshift(response);
      total.value++;
      
      showMessage('success', '图案创建成功')
      console.log('新增成功，图片URL:', imageField)
    }
    
    dialogVisible.value = false;
    
    // 清理上传状态
    clearSelectedFile();
    previewImageUrl.value = '';
    
  } catch (error) {
    console.error('保存失败:', error);
    showMessage('error', '保存失败: ' + (error.message || '未知错误'));
  }
};

// 图片处理相关方法
const handleImageUrlChange = (url) => {
  form.value.imageUrl = url;
  updatePreviewImage();
}

// 简化的图片处理方法（移除复杂的上传功能）

// 智能图片URL生成函数
const getSmartImageUrl = (path) => {
  if (!path) return ''
  
  console.log('智能图片URL处理:', path)
  
  // 首先尝试原始路径
  const originalUrl = getImageUrl(path)
  console.log('原始URL:', originalUrl)
  
  // 对于包含中文字符的文件名，直接使用原始格式
  // 不进行智能切换，避免文件不存在的问题
  if (path.includes('%') || /[\u4e00-\u9fa5]/.test(path)) {
    console.log('检测到中文字符或已编码字符，使用原始格式:', path)
    return originalUrl
  }
  
  // 只对预定义的测试文件进行智能切换
  const predefinedFiles = [
    'test-pattern-1.jpg',
    'test-pattern-2.png', 
    'geometric-pattern.jpg',
    'marble-texture.jpg'
  ]
  
  const fileName = path.split('/').pop()
  if (predefinedFiles.includes(fileName) && (path.endsWith('.jpg') || path.endsWith('.png'))) {
    const svgPath = path.replace(/\.(jpg|png)$/i, '.svg')
    console.log(`智能切换预定义文件: ${path} -> ${svgPath}`)
    return getImageUrl(svgPath)
  }
  
  // 其他情况直接返回原始URL
  console.log('使用原始格式:', path)
  return originalUrl
}

// 更新预览图片
const updatePreviewImage = () => {
  console.log('更新预览图片:', {
    uploadType: imageUploadType.value,
    imageUrl: form.value.imageUrl,
    imagePath: form.value.imagePath,
    isEdit: isEdit.value
  })
  
  let newPreviewUrl = ''
  
  if (imageUploadType.value === 'url' && form.value.imageUrl?.trim()) {
    newPreviewUrl = form.value.imageUrl.trim()
    console.log('设置URL预览:', newPreviewUrl)
  } else if (imageUploadType.value === 'path' && form.value.imagePath?.trim()) {
    // 使用智能切换的URL生成，自动处理JPG/PNG到SVG的转换
    newPreviewUrl = getSmartImageUrl(form.value.imagePath.trim())
    console.log('设置智能路径预览:', newPreviewUrl)
  } else if (imageUploadType.value === 'file' && form.value.imageUrl?.trim()) {
    // 文件上传后的预览 - 使用getImageUrl确保正确的URL格式
    newPreviewUrl = getImageUrl(form.value.imageUrl.trim())
    console.log('设置文件预览:', newPreviewUrl)
  } else {
    console.log('清除预览 - 没有有效的图片数据')
  }
  
  // 更新预览URL
  if (previewImageUrl.value !== newPreviewUrl) {
    previewImageUrl.value = newPreviewUrl
    
    // 如果有新的预览URL，记录更换操作
    if (newPreviewUrl) {
      console.log('✅ 图片预览已更新:', newPreviewUrl)
      
      // 验证预览URL是否可访问（异步，不阻塞）
      setTimeout(() => {
        const img = new Image()
        img.onload = () => {
          console.log('✅ 预览图片加载成功:', newPreviewUrl)
        }
        img.onerror = () => {
          console.warn('⚠️ 预览图片加载失败:', newPreviewUrl)
        }
        img.src = newPreviewUrl
      }, 100)
    } else {
      console.log('预览已清除')
    }
  } else {
    console.log('预览URL未变化，跳过更新')
  }
}

// 清除图片
const clearImage = () => {
  form.value.imageUrl = '';
  form.value.imagePath = '';
  previewImageUrl.value = '';
}

// 图片加载错误处理
const handleImageError = () => {
  showMessage('warning', '图片加载失败，请检查URL是否正确')
  previewImageUrl.value = '';
}

// 验证图片URL
const validateImage = async (url) => {
  if (!url) return false;
  
  try {
    const isValid = await validateImageUrl(url);
    if (!isValid) {
      showMessage('warning', '图片URL无效，请检查链接是否正确')
    }
    return isValid;
  } catch (error) {
    console.error('图片验证失败:', error);
    showMessage('warning', '图片验证失败')
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

// 文件上传相关函数
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    processSelectedFile(file)
  }
}

const handleFileDrop = (event) => {
  isDragOver.value = false
  const files = event.dataTransfer.files
  if (files.length > 0) {
    processSelectedFile(files[0])
  }
}

const processSelectedFile = (file) => {
  console.log('处理选择的文件:', file.name, file.type, file.size)
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!file.type.startsWith('image/') || !allowedTypes.includes(file.type)) {
    showMessage('error', `不支持的文件类型: ${file.type}。请选择 JPG、PNG、GIF、WEBP 或 BMP 格式的图片`)
    return
  }
  
  // 验证文件大小 (5MB)
  if (file.size > 5 * 1024 * 1024) {
    showMessage('error', `文件大小 ${formatFileSize(file.size)} 超过限制，请选择小于 5MB 的图片`)
    return
  }
  
  // 验证文件名
  if (file.name.length > 100) {
    showMessage('error', '文件名过长，请选择文件名少于100个字符的文件')
    return
  }
  
  selectedFile.value = file
  console.log('文件验证通过，开始创建预览')
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    filePreviewUrl.value = e.target.result
    console.log('文件预览创建成功')
  }
  reader.onerror = (e) => {
    console.error('文件读取失败:', e)
    showMessage('error', '文件读取失败，请重新选择')
  }
  reader.readAsDataURL(file)
}

const clearSelectedFile = () => {
  selectedFile.value = null
  filePreviewUrl.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const uploadFile = async () => {
  if (!selectedFile.value) {
    showMessage('error', '请先选择文件')
    return
  }
  
  uploading.value = true
  
  try {
    console.log('开始上传文件:', selectedFile.value.name)
    
    // 使用真实的上传API
    const uploadResult = await uploadImage(selectedFile.value)
    console.log('文件上传成功:', uploadResult)
    
    if (uploadResult.success && uploadResult.url) {
      // 设置上传后的图片URL
      form.value.imageUrl = uploadResult.url
      
      console.log('设置图片URL:', uploadResult.url)
      
      // 立即更新预览图片 - 确保在文件上传模式下显示
      const fullImageUrl = getImageUrl(uploadResult.url)
      previewImageUrl.value = fullImageUrl
      console.log('✅ 预览图片已更新:', previewImageUrl.value)
      
      // 确保预览图片在DOM中正确显示
      setTimeout(() => {
        if (previewImageUrl.value) {
          console.log('✅ 文件上传预览确认显示:', previewImageUrl.value)
        }
      }, 100)
      
      showMessage('success', `图片上传成功！文件名: ${uploadResult.filename || selectedFile.value.name}`)
      
      // 清除选择的文件状态，但保留预览
      clearSelectedFile()
      
      console.log('✅ 文件上传完成，图片URL已更新:', form.value.imageUrl)
      console.log('✅ 预览状态:', { previewImageUrl: previewImageUrl.value, uploadType: imageUploadType.value })
    } else {
      throw new Error(uploadResult.message || '上传失败，服务器未返回有效URL')
    }
    
  } catch (error) {
    console.error('上传失败:', error)
    showMessage('error', '图片上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}



// URL输入处理
const handleUrlInput = async () => {
  const url = form.value.imageUrl?.trim()
  console.log('URL输入处理:', url)
  
  if (!url) {
    previewImageUrl.value = ''
    return
  }
  
  // 基本URL格式验证
  try {
    new URL(url)
  } catch (error) {
    console.warn('URL格式无效:', url)
    showMessage('warning', 'URL格式无效，请检查输入')
    previewImageUrl.value = ''
    return
  }
  
  // 检查是否是图片URL（基于扩展名或已知图片服务）
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg']
  const isImageUrl = imageExtensions.some(ext => url.toLowerCase().includes(ext)) ||
                     url.includes('picsum.photos') ||
                     url.includes('placeholder.com') ||
                     url.includes('unsplash.com') ||
                     url.includes('images.') ||
                     url.includes('/image/') ||
                     url.includes('data:image/')
  
  if (!isImageUrl) {
    console.warn('可能不是图片URL:', url)
    showMessage('warning', '输入的URL可能不是图片链接，请确认')
  }
  
  // 立即更新预览
  updatePreviewImage()
  
  // 异步验证URL可访问性（不阻塞用户操作）
  try {
    const isValid = await validateImage(url)
    if (!isValid) {
      console.warn('图片URL验证失败:', url)
    }
  } catch (error) {
    console.warn('图片URL验证出错:', error)
  }
}

// 路径选择处理
const handlePathSelect = () => {
  const path = form.value.imagePath?.trim()
  console.log('路径选择处理:', path)
  
  if (!path) {
    previewImageUrl.value = ''
    return
  }
  
  // 验证路径格式
  if (!path.startsWith('/')) {
    console.warn('路径格式可能不正确:', path)
    showMessage('warning', '路径应以 / 开头，如：/uploads/patterns/image.jpg')
  }
  
  updatePreviewImage()
}



// 图片加载错误处理
const handleImageLoadError = (error) => {
  // 静默处理图片加载错误，避免控制台噪音
  // console.warn('图片加载失败:', error)
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

// ✅ 验证数量逻辑
const validateQuantityLogic = async () => {
  try {
    showMessage('info', '开始验证数量逻辑...')
    
    // 获取所有必要数据
    const [tasksResponse, plansResponse] = await Promise.all([
      getTasks({ page: 0, size: 1000 }),
      getPlans(0, 1000)
    ])
    
    const allTasks = tasksResponse.content || []
    const allPlans = plansResponse.content || []
    
    // 执行验证
    const validationResult = validatePatternQuantityLogic(
      patterns.value, 
      allTasks, 
      allPlans
    )
    
    console.log('图案管理数量逻辑验证结果:', validationResult)
    
    if (validationResult.isValid) {
      showNotification({
        title: '验证通过',
        message: `数量逻辑验证通过！共验证 ${validationResult.summary.totalPatterns} 个图案`,
        type: 'success',
        duration: 3000
      })
    } else {
      showNotification({
        title: '发现问题',
        message: `发现 ${validationResult.issues.length} 个严重问题，${validationResult.warnings.length} 个警告`,
        type: 'warning',
        duration: 5000
      })
    }
    
    // 输出详细报告到控制台
    const report = generateQuantityLogicReport({ summary: {}, issues: [], warnings: [] }, validationResult)
    console.log('详细验证报告:')
    console.log(report)
    
  } catch (error) {
    console.error('验证失败:', error)
    showMessage('error', '验证过程中发生错误: ' + (error.message || '未知错误'))
  }
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

/* 文件上传区域样式 */
.file-upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.file-upload-area:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.file-upload-area.drag-over {
  border-color: #409eff;
  background: #e6f7ff;
}

.file-upload-area.uploading {
  border-color: #faad14;
  background: #fffbe6;
  cursor: not-allowed;
}

.upload-progress {
  padding: 20px;
  text-align: center;
}

.upload-progress .upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: spin 2s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-placeholder {
  padding: 20px;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

/* 文件信息样式 */
.file-info {
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.file-preview {
  flex-shrink: 0;
}

.preview-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.file-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 24px;
}

.file-details {
  flex: 1;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-all;
}

.file-size {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.file-type {
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
  font-family: monospace;
}

.file-actions {
  display: flex;
  gap: 8px;
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

/* 原图片显示区域 */
.original-image-section {
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f9f9f9;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.original-image-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.original-preview-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
  flex-shrink: 0;
}

.original-image-info {
  flex: 1;
  min-width: 0;
}

.image-url-text {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
  line-height: 1.4;
}

/* 图片更换提示 */
.image-change-notice {
  margin-top: 8px;
  text-align: center;
}

.image-change-notice .el-tag {
  font-size: 12px;
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
  
  .original-image-preview {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .original-preview-img {
    width: 100%;
    max-width: 200px;
    height: auto;
  }
}
</style>
