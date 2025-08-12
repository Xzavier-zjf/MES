<template>
  <div class="injection-page">
 <HeaderSection 
    title=" 🛠&nbsp注塑工艺参数"
    subtitle="
      管理注塑工艺参数，为注塑工艺提供参数。
      "
    :showStats="true"
    :card1="'注塑参数总数'"
    :card2="'已配置参数'"
    :card3="'待配置参数'"
    :card4="'异常参数'"
    :value1="totalInjectionParams"
    :value2="configuredParams"
    :value3="pendingParams"
    :value4="errorParams"/>


    <!-- 过滤器组件 -->
    <el-card shadow="hover" class="table-card" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="计划编号">
          <el-input v-model="filters.planId" placeholder="请输入计划编号" />
        </el-form-item>
        <el-form-item label="任务编号">
          <el-input v-model="filters.taskId" placeholder="请输入任务编号" />
        </el-form-item>

        <!-- <el-form-item label="工序类型">
          <el-select v-model="filters.processType" placeholder="请选择工序类型" clearable>
            <el-option label="注塑" value="注塑" />
          </el-select>
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" @click="submitFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="success" @click="openCreateDialog">新增注塑工艺</el-button>
          <el-button type="info" @click="validateQuantityLogic">验证数量逻辑</el-button>
          <el-button type="warning" @click="refreshAllData">刷新数据</el-button>
          <el-button type="danger" @click="checkDataConsistency">检查数据一致性</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table :data="filteredTasks" border style="width: 100%">
        <el-table-column prop="planId" label="计划编号" width="180">
          <template #default="{row}">
            {{ planMap[row.planId] || row.planId }}
          </template>
        </el-table-column>
        <el-table-column prop="taskId" label="任务编号">
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
            <el-tag :type="getQuantityTagType(row)">
              {{ getTaskQuantityDisplay(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="getProgress(row.status)" :color="progressColor(row.status)" />
          </template>
        </el-table-column> -->
        <el-table-column prop="pressure" label="注塑压力 (MPa)" />
        <el-table-column prop="injectionSpeed" label="注塑速度 (mm/s)" />
        <el-table-column prop="holdTime" label="保压时间 (s)" />
        <el-table-column prop="coolingTime" label="冷却时间 (s)" />
        <el-table-column prop="moldTemperature" label="模具温度 (℃)" />
        <el-table-column prop="materialTemperature" label="料筒温度 (℃)" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDialog(row)">录入工艺</el-button>
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

    <!-- 参数录入弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isCreateMode ? '新增注塑工艺参数' : '编辑注塑工艺参数'" width="600px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <!-- 基础信息 - 仅在新增模式显示 -->
        <div v-if="isCreateMode" class="form-section">
          <h4 class="section-title">基础信息</h4>
          <el-form-item label="关联计划" prop="planId">
            <el-select v-model="form.planId" placeholder="请选择生产计划" clearable filterable>
              <el-option 
                v-for="plan in availablePlans" 
                :key="plan.id" 
                :label="`${plan.planCode} - ${plan.productName}`" 
                :value="plan.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联任务" prop="taskId">
            <el-select v-model="form.taskId" placeholder="请选择注塑任务" clearable filterable @change="onTaskChange">
              <el-option 
                v-for="task in availableInjectionTasks" 
                :key="task.id" 
                :label="`${task.taskCode} - 数量: ${task.quantity}`" 
                :value="task.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联设备" prop="deviceId">
            <el-select v-model="form.deviceId" placeholder="请选择注塑设备" clearable filterable>
              <el-option 
                v-for="device in availableInjectionDevices" 
                :key="device.id" 
                :label="`${device.deviceCode} - ${device.name || '注塑设备'}`" 
                :value="device.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="生产数量" prop="quantity">
            <el-input-number 
              v-model="form.quantity" 
              :min="1" 
              :max="getMaxQuantityForTask()" 
              placeholder="请输入生产数量" 
            />
            <div v-if="getSelectedTaskQuantity() > 0" class="quantity-hint">
              <span class="hint-text">
                任务总数量: {{ getSelectedTaskQuantity() }} 件
                <el-tag v-if="form.quantity > getSelectedTaskQuantity()" type="danger" size="small">
                  超出任务数量
                </el-tag>
                <el-tag v-else-if="form.quantity === getSelectedTaskQuantity()" type="success" size="small">
                  数量匹配
                </el-tag>
              </span>
            </div>
          </el-form-item>
        </div>

        <!-- 任务信息 - 仅在编辑模式显示 -->
        <div v-if="!isCreateMode" class="form-section">
          <h4 class="section-title">任务信息</h4>
          <el-form-item label="关联任务">
            <el-input :value="getTaskDisplayName()" readonly />
          </el-form-item>
          <el-form-item label="任务数量" prop="taskQuantity">
            <el-input-number 
              v-model="form.taskQuantity" 
              :min="1" 
              :max="50000"
              :disabled="!hasTaskUpdatePermission"
              placeholder="任务数量"
            />
            <div class="quantity-info">
              <span class="info-text">
                当前工艺参数数量: {{ form.quantity }}
                <el-button 
                  type="text" 
                  size="small" 
                  @click="syncQuantityFromTask"
                  v-if="form.taskQuantity !== form.quantity"
                >
                  同步任务数量
                </el-button>
              </span>
            </div>
          </el-form-item>
          <el-form-item label="数量更新">
            <el-checkbox v-model="form.updateTaskQuantity" :disabled="!hasTaskUpdatePermission">
              同时更新任务数量
            </el-checkbox>
            <div class="update-hint" v-if="form.updateTaskQuantity">
              <el-alert
                title="注意：更新任务数量可能影响其他关联的工艺参数"
                type="warning"
                :closable="false"
                show-icon
                size="small"
              />
            </div>
          </el-form-item>
        </div>

        <!-- 工艺参数 -->
        <div class="form-section">
          <h4 class="section-title">工艺参数</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="注塑压力" prop="pressure">
                <el-input-number 
                  v-model="form.pressure" 
                  :min="0" 
                  :max="1000" 
                  :precision="1"
                  placeholder="MPa"
                />
                <span class="unit-text">MPa</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="注塑速度" prop="injectionSpeed">
                <el-input-number 
                  v-model="form.injectionSpeed" 
                  :min="0" 
                  :max="500" 
                  :precision="1"
                  placeholder="mm/s"
                />
                <span class="unit-text">mm/s</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="保压时间" prop="holdTime">
                <el-input-number 
                  v-model="form.holdTime" 
                  :min="0" 
                  :max="60" 
                  :precision="1"
                  placeholder="秒"
                />
                <span class="unit-text">秒</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="冷却时间" prop="coolingTime">
                <el-input-number 
                  v-model="form.coolingTime" 
                  :min="0" 
                  :max="300" 
                  :precision="1"
                  placeholder="秒"
                />
                <span class="unit-text">秒</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="模具温度" prop="moldTemperature">
                <el-input-number 
                  v-model="form.moldTemperature" 
                  :min="0" 
                  :max="500" 
                  :precision="1"
                  placeholder="℃"
                />
                <span class="unit-text">℃</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="料筒温度" prop="materialTemperature">
                <el-input-number 
                  v-model="form.materialTemperature" 
                  :min="0" 
                  :max="400" 
                  :precision="1"
                  placeholder="℃"
                />
                <span class="unit-text">℃</span>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 备注信息 - 暂时移除，因为后端DTO不支持 -->
        <!-- <div class="form-section">
          <el-form-item label="备注信息">
            <el-input 
              v-model="form.remarks" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入工艺备注信息（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div> -->
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="submitParams" :loading="submitLoading">
          {{ isCreateMode ? '创建工艺参数' : '更新工艺参数' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import HeaderSection from '@/components/HeaderSection.vue'
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getInjectionParams, updateInjectionParam, createInjectionParam } from '@/api/injection'
import { getPlans } from '@/api/plans'
import { getTasks, updateTaskQuantity } from '@/api/tasks'
import { getDevices } from '@/api/devices'
import { useAppStore } from '@/stores'
import { validateInjectionQuantityLogic, generateQuantityLogicReport } from '@/utils/quantityLogicValidator'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'

const route = useRoute()
const appStore = useAppStore()

// 注塑参数列表
const injectionParams = ref([])
// 当前页码
const currentPage = ref(1)
// 每页显示数量
const pageSize = ref(20)
// 总记录数
const total = ref(0)

// ✅ 注塑参数统计
const totalInjectionParams = computed(() => injectionParams.value.length)
const configuredParams = computed(() => 
  injectionParams.value.filter(p => 
    p.pressure && p.injectionSpeed && p.holdTime && p.coolingTime && 
    p.moldTemperature && p.materialTemperature
  ).length
)
const pendingParams = computed(() => 
  injectionParams.value.filter(p => 
    !p.pressure || !p.injectionSpeed || !p.holdTime || !p.coolingTime || 
    !p.moldTemperature || !p.materialTemperature
  ).length
)
const errorParams = computed(() => 
  injectionParams.value.filter(p => 
    (p.pressure && p.pressure < 0) || 
    (p.injectionSpeed && p.injectionSpeed < 0) ||
    (p.moldTemperature && (p.moldTemperature < 0 || p.moldTemperature > 500))
  ).length
)

// 过滤条件
const filters = ref({
  taskId: '',
  planId: '',
  processType: ''
})

// 过滤后的注塑参数列表
const filteredTasks = computed(() => {
  return injectionParams.value.filter(param => {
    const taskIdStr = param.taskId != null ? String(param.taskId) : '';
    const planIdStr = param.planId != null ? String(param.planId) : '';
    
    // 计划编号筛选 - 支持模糊匹配
    const planMatch = !filters.value.planId || 
      planIdStr.toLowerCase().includes(filters.value.planId.toLowerCase()) ||
      (planMap.value[param.planId] && planMap.value[param.planId].toLowerCase().includes(filters.value.planId.toLowerCase()));
    
    // 任务编号筛选 - 支持模糊匹配
    const taskMatch = !filters.value.taskId || 
      taskIdStr.toLowerCase().includes(filters.value.taskId.toLowerCase()) ||
      (taskMap.value[param.taskId] && taskMap.value[param.taskId].toLowerCase().includes(filters.value.taskId.toLowerCase()));
    
    return planMatch && taskMatch;
  })
})

const dialogVisible = ref(false)
const isCreateMode = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const form = ref({
  planId: '',
  taskId: '',
  deviceId: '',
  quantity: 1,
  pressure: null,
  injectionSpeed: null,
  holdTime: null,
  coolingTime: null,
  moldTemperature: null,
  materialTemperature: null,
  // 任务数量相关字段
  taskQuantity: 1,
  originalTaskQuantity: 1,
  updateTaskQuantity: false
})

// 可用的计划、任务、设备数据
const availablePlans = ref([])
const availableInjectionTasks = ref([])
const availableInjectionDevices = ref([])

// 任务详细信息映射（包含数量等信息）
const taskDetailsMap = ref({})

// 表单验证规则
const formRules = {
  planId: [{ required: true, message: '请选择生产计划', trigger: 'change' }],
  taskId: [{ required: true, message: '请选择注塑任务', trigger: 'change' }],
  deviceId: [{ required: true, message: '请选择注塑设备', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入生产数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '生产数量必须大于0', trigger: 'blur' }
  ],
  pressure: [
    { required: true, message: '请输入注塑压力', trigger: 'blur' },
    { type: 'number', min: 0, max: 1000, message: '注塑压力范围: 0-1000 MPa', trigger: 'blur' }
  ],
  injectionSpeed: [
    { required: true, message: '请输入注塑速度', trigger: 'blur' },
    { type: 'number', min: 0, max: 500, message: '注塑速度范围: 0-500 mm/s', trigger: 'blur' }
  ],
  holdTime: [
    { required: true, message: '请输入保压时间', trigger: 'blur' },
    { type: 'number', min: 0, max: 60, message: '保压时间范围: 0-60 秒', trigger: 'blur' }
  ],
  coolingTime: [
    { required: true, message: '请输入冷却时间', trigger: 'blur' },
    { type: 'number', min: 0, max: 300, message: '冷却时间范围: 0-300 秒', trigger: 'blur' }
  ],
  moldTemperature: [
    { required: true, message: '请输入模具温度', trigger: 'blur' },
    { type: 'number', min: 0, max: 500, message: '模具温度范围: 0-500 ℃', trigger: 'blur' }
  ],
  materialTemperature: [
    { required: true, message: '请输入料筒温度', trigger: 'blur' },
    { type: 'number', min: 0, max: 400, message: '料筒温度范围: 0-400 ℃', trigger: 'blur' }
  ]
}

const navItems = [
  { name: '首页', path: '/home' },
  { name: '生产计划管理', path: '/plan' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑参数', path: '/injection' },
  { name: '图案参数', path: '/pattern' },
]

// 打开编辑对话框
const openDialog = (task) => {
  isCreateMode.value = false
  form.value = { 
    ...task,
    taskQuantity: 0,
    originalTaskQuantity: 0,
    updateTaskQuantity: false
  }
  
  // 获取任务详细信息
  const taskDetails = taskDetailsMap.value[task.taskId]
  if (taskDetails) {
    form.value.taskQuantity = taskDetails.quantity || 0
    form.value.originalTaskQuantity = taskDetails.quantity || 0
  }
  
  dialogVisible.value = true
}

// 打开新增对话框
const openCreateDialog = () => {
  isCreateMode.value = true
  form.value = {
    planId: '',
    taskId: '',
    deviceId: '',
    quantity: 1,
    pressure: null,
    injectionSpeed: null,
    holdTime: null,
    coolingTime: null,
    moldTemperature: null,
    materialTemperature: null,
    taskQuantity: 1,
    originalTaskQuantity: 1,
    updateTaskQuantity: false
  }
  dialogVisible.value = true
}

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 更新注塑参数
const updateInjectionParamLocal = async (id, updateDTO) => {
  try {
    const response = await updateInjectionParam(id, updateDTO)
    return response
  } catch (error) {
    console.error('更新注塑参数失败:', error)
    throw error
  }
}

const submitParams = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    submitLoading.value = true
    
    // 确保数据类型正确，后端期望 Float 类型
    const paramData = {
      planId: form.value.planId ? String(form.value.planId) : null,
      taskId: form.value.taskId ? String(form.value.taskId) : null,
      deviceId: form.value.deviceId ? String(form.value.deviceId) : null,
      quantity: parseInt(form.value.quantity) || 0,
      pressure: parseFloat(form.value.pressure) || null,
      injectionSpeed: parseFloat(form.value.injectionSpeed) || null,
      coolingTime: parseFloat(form.value.coolingTime) || null,
      holdTime: parseFloat(form.value.holdTime) || null,
      moldTemperature: parseFloat(form.value.moldTemperature) || null,
      materialTemperature: parseFloat(form.value.materialTemperature) || null
    }
    
    console.log('准备发送的数据:', paramData)
    
    if (isCreateMode.value) {
      // 新增模式
      console.log('创建注塑工艺参数:', paramData)
      
      // 验证必填字段
      if (!paramData.planId || !paramData.taskId || !paramData.deviceId) {
        ElMessage.error('请完整填写计划、任务和设备信息')
        return
      }
      
      if (paramData.quantity <= 0) {
        ElMessage.error('生产数量必须大于0')
        return
      }
      
      // 验证数量是否合理
      const selectedTaskQuantity = getSelectedTaskQuantity()
      if (selectedTaskQuantity > 0 && paramData.quantity > selectedTaskQuantity) {
        const confirmResult = await ElMessageBox.confirm(
          `输入的生产数量 ${paramData.quantity} 超出了任务数量 ${selectedTaskQuantity}，是否继续？`,
          '数量超出警告',
          {
            confirmButtonText: '继续创建',
            cancelButtonText: '重新输入',
            type: 'warning'
          }
        ).catch(() => false)
        
        if (!confirmResult) {
          return
        }
      }
      
      // 验证工艺参数
      if (!paramData.pressure || !paramData.injectionSpeed || !paramData.holdTime || 
          !paramData.coolingTime || !paramData.moldTemperature || !paramData.materialTemperature) {
        ElMessage.error('请完整填写所有工艺参数')
        return
      }
      
      const newParam = await createInjectionParam(paramData)
      injectionParams.value.unshift(newParam)
      ElMessage.success('注塑工艺参数创建成功')
    } else {
      // 编辑模式
      if (!form.value.id) {
        ElMessage.error('缺少参数 ID，无法更新')
        return
      }
      
      // 检查是否需要更新任务数量
      const needUpdateTask = form.value.updateTaskQuantity && 
                            form.value.taskQuantity !== form.value.originalTaskQuantity &&
                            hasTaskUpdatePermission.value
      
      if (needUpdateTask) {
        // 确认更新任务数量
        const confirmResult = await ElMessageBox.confirm(
          `确定要将任务数量从 ${form.value.originalTaskQuantity} 更新为 ${form.value.taskQuantity} 吗？\n\n注意：这可能影响其他关联的工艺参数。`,
          '确认更新任务数量',
          {
            confirmButtonText: '确认更新',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: false
          }
        ).catch(() => false)
        
        if (!confirmResult) {
          return
        }
        
        try {
          // 先更新任务数量
          await updateTaskQuantity(form.value.taskId, form.value.taskQuantity)
          
          // 更新本地任务数据 - 使用响应式更新
          if (taskDetailsMap.value[form.value.taskId]) {
            // 创建新对象以触发响应式更新
            taskDetailsMap.value = {
              ...taskDetailsMap.value,
              [form.value.taskId]: {
                ...taskDetailsMap.value[form.value.taskId],
                quantity: form.value.taskQuantity
              }
            }
          }
          
          console.log('任务数量更新成功:', form.value.taskId, form.value.taskQuantity)
          
          // 重新加载任务详细信息以确保数据同步
          await loadTaskDetailsMap()
          
          // 强制触发响应式更新
          await nextTick()
          
          // 验证数据同步
          const isSynced = verifyDataSync(form.value.taskId, form.value.taskQuantity)
          if (!isSynced) {
            console.warn('数据同步验证失败，尝试再次刷新')
            await loadTaskDetailsMap()
            await nextTick()
          }
          
          // 触发全局数据同步
          await triggerGlobalDataSync(form.value.taskId, form.value.taskQuantity)
        } catch (error) {
          console.error('更新任务数量失败:', error)
          ElMessage.error('更新任务数量失败: ' + error.message)
          return
        }
      }
      
      // 更新工艺参数
      console.log('更新注塑工艺参数:', { id: form.value.id, data: paramData })
      const updatedData = await updateInjectionParamLocal(form.value.id, paramData)
      
      // 更新本地注塑参数列表
      const index = injectionParams.value.findIndex(t => t.id === form.value.id)
      if (index !== -1) {
        injectionParams.value[index] = { ...injectionParams.value[index], ...updatedData }
      }
      
      const successMessage = needUpdateTask ? 
        '工艺参数和任务数量更新成功' : 
        '注塑工艺参数更新成功'
      ElMessage.success(successMessage)
      
      // 如果更新了任务数量，刷新注塑参数列表以显示最新数据
      if (needUpdateTask) {
        await fetchInjectionParams()
      }
    }
    
    closeDialog()
    
  } catch (error) {
    console.error('提交失败:', error)
    
    // 提供更详细的错误信息
    let errorMessage = '操作失败'
    if (error.message) {
      if (error.message.includes('该任务已存在工艺参数')) {
        errorMessage = '该任务已存在工艺参数，请选择其他任务或编辑现有参数'
      } else if (error.message.includes('关联的任务不存在')) {
        errorMessage = '选择的任务不存在，请重新选择任务'
      } else if (error.message.includes('请求参数错误')) {
        errorMessage = '请求参数错误，请检查输入的数据格式'
      } else {
        errorMessage = error.message
      }
    }
    
    ElMessage.error(errorMessage)
  } finally {
    submitLoading.value = false
  }
}

// 提交过滤
const submitFilter = () => {
  console.log('执行筛选，条件:', filters.value)
  // 过滤逻辑已在 computed 中实现，这里可以添加额外的筛选逻辑
}

// 重置过滤
const resetFilter = () => {
  filters.value = {
    taskId: '',
    planId: '',
    processType: ''
  }
  console.log('重置筛选条件')
}

// 获取注塑参数列表
const fetchInjectionParams = async () => {
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'id,desc'
    }
    const response = await getInjectionParams(params)
    injectionParams.value = response.content
    total.value = response.totalElements
  } catch (error) {
    console.error('获取注塑参数列表失败:', error)
  }
}

// 页码变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchInjectionParams()
}

// 每页数量变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchInjectionParams()
}

// 组件挂载时获取数据
// 添加映射关系
const planMap = ref({})
const taskMap = ref({})
const deviceMap = ref({})

// 获取计划映射
const loadPlanMap = async () => {
  try {
    const response = await getPlans(0, 1000)
    planMap.value = {}
    response.content.forEach(plan => {
      planMap.value[plan.id] = plan.planCode
    })
  } catch (error) {
    console.error('获取计划映射失败:', error)
  }
}

// 获取任务映射
const loadTaskMap = async () => {
  try {
    const response = await getTasks({ page: 0, size: 1000 })
    taskMap.value = {}
    response.content.forEach(task => {
      taskMap.value[task.id] = task.taskCode
    })
  } catch (error) {
    console.error('获取任务映射失败:', error)
  }
}

// 获取设备映射
const loadDeviceMap = async () => {
  try {
    const response = await getDevices({ page: 0, size: 1000 })
    deviceMap.value = {}
    response.content.forEach(device => {
      deviceMap.value[device.id] = device.deviceCode
    })
  } catch (error) {
    console.error('获取设备映射失败:', error)
  }
}

// 获取任务详细信息映射
const loadTaskDetailsMap = async () => {
  try {
    const response = await getTasks({ page: 0, size: 1000 })
    taskDetailsMap.value = {}
    response.content.forEach(task => {
      taskDetailsMap.value[task.id] = task
    })
  } catch (error) {
    console.error('获取任务详细信息失败:', error)
  }
}

// 获取任务数量显示
const getTaskQuantityDisplay = (row) => {
  const taskDetails = taskDetailsMap.value[row.taskId]
  const paramQuantity = row.quantity || 0
  const taskQuantity = taskDetails?.quantity || 0
  
  // 添加调试信息
  console.log('显示任务数量:', {
    taskId: row.taskId,
    paramQuantity,
    taskQuantity,
    taskDetails: taskDetails ? '存在' : '不存在'
  })
  
  if (taskQuantity > 0) {
    if (paramQuantity === taskQuantity) {
      return `${paramQuantity} 件`
    } else {
      const difference = Math.abs(paramQuantity - taskQuantity)
      const isSerious = difference > taskQuantity * 0.5 // 差异超过50%
      
      return `${paramQuantity}/${taskQuantity} 件${isSerious ? ' ⚠️' : ''}`
    }
  }
  return `${paramQuantity} 件`
}

// 获取数量标签类型
const getQuantityTagType = (row) => {
  const taskDetails = taskDetailsMap.value[row.taskId]
  const paramQuantity = row.quantity || 0
  const taskQuantity = taskDetails?.quantity || 0
  
  if (taskQuantity > 0) {
    const difference = Math.abs(paramQuantity - taskQuantity)
    
    if (paramQuantity === taskQuantity) {
      return 'success' // 数量匹配
    } else if (difference > taskQuantity * 0.5) {
      return 'danger' // 差异超过50%，严重不一致
    } else if (paramQuantity > taskQuantity) {
      return 'warning' // 超出任务数量
    } else {
      return 'info' // 少于任务数量
    }
  }
  return '' // 默认样式
}

// 任务变化时的处理
const onTaskChange = (taskId) => {
  if (taskId) {
    const selectedTask = availableInjectionTasks.value.find(task => task.id === taskId)
    if (selectedTask && selectedTask.quantity) {
      // 自动设置为任务的数量
      form.value.quantity = selectedTask.quantity
    }
  }
}

// 获取选中任务的数量
const getSelectedTaskQuantity = () => {
  if (form.value.taskId) {
    const selectedTask = availableInjectionTasks.value.find(task => task.id === form.value.taskId)
    return selectedTask?.quantity || 0
  }
  return 0
}

// 获取任务的最大数量限制
const getMaxQuantityForTask = () => {
  const taskQuantity = getSelectedTaskQuantity()
  return taskQuantity > 0 ? Math.max(taskQuantity * 1.1, 10000) : 10000 // 允许超出10%或最大10000
}

// 权限控制
const hasTaskUpdatePermission = computed(() => {
  // 这里可以根据实际的权限系统进行判断
  // 暂时返回 true，后续可以接入权限系统
  return true
})

// 获取任务显示名称
const getTaskDisplayName = () => {
  const taskDetails = taskDetailsMap.value[form.value.taskId]
  if (taskDetails) {
    return `${taskDetails.taskCode || form.value.taskId} - ${taskDetails.productName || '注塑任务'}`
  }
  return form.value.taskId || '未知任务'
}

// 同步任务数量到工艺参数数量
const syncQuantityFromTask = () => {
  form.value.quantity = form.value.taskQuantity
}

// 刷新所有数据
const refreshAllData = async () => {
  try {
    ElMessage.info('正在刷新数据...')
    await Promise.all([
      fetchInjectionParams(),
      loadTaskDetailsMap(),
      loadPlanMap(),
      loadTaskMap(),
      loadDeviceMap()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('数据刷新失败')
  }
}

// 检查数据一致性
const checkDataConsistency = async () => {
  try {
    ElMessage.info('正在检查数据一致性...')
    
    let inconsistencies = []
    let totalChecked = 0
    
    // 检查每个工艺参数与对应任务的数量一致性
    injectionParams.value.forEach(param => {
      const taskDetails = taskDetailsMap.value[param.taskId]
      totalChecked++
      
      if (!taskDetails) {
        inconsistencies.push({
          type: '任务不存在',
          paramId: param.id,
          taskId: param.taskId,
          paramQuantity: param.quantity,
          taskQuantity: 'N/A'
        })
      } else if (taskDetails.quantity !== param.quantity) {
        const difference = Math.abs(taskDetails.quantity - param.quantity)
        const isSerious = difference > taskDetails.quantity * 0.5
        
        inconsistencies.push({
          type: isSerious ? '严重不一致' : '数量不一致',
          paramId: param.id,
          taskId: param.taskId,
          paramQuantity: param.quantity,
          taskQuantity: taskDetails.quantity,
          difference: difference,
          isSerious: isSerious
        })
      }
    })
    
    // 显示检查结果
    if (inconsistencies.length === 0) {
      ElNotification({
        title: '数据一致性检查',
        message: `✅ 检查完成！所有 ${totalChecked} 项数据都是一致的`,
        type: 'success',
        duration: 3000
      })
    } else {
      const seriousCount = inconsistencies.filter(i => i.isSerious).length
      
      ElNotification({
        title: '发现数据不一致',
        message: `⚠️ 发现 ${inconsistencies.length} 项不一致，其中 ${seriousCount} 项严重不一致`,
        type: 'warning',
        duration: 5000
      })
      
      // 输出详细信息到控制台
      console.group('数据一致性检查结果')
      console.log('总检查项:', totalChecked)
      console.log('不一致项:', inconsistencies.length)
      console.log('严重不一致项:', seriousCount)
      console.table(inconsistencies)
      console.groupEnd()
      
      // 显示修复建议
      if (seriousCount > 0) {
        setTimeout(() => {
          ElMessageBox.confirm(
            `发现 ${seriousCount} 项严重数据不一致，建议立即修复。是否查看详细信息？`,
            '数据不一致警告',
            {
              confirmButtonText: '查看详情',
              cancelButtonText: '稍后处理',
              type: 'warning'
            }
          ).then(() => {
            // 可以打开详细的数据一致性检查页面
            window.open('./test-data-consistency-simple.html', '_blank')
          }).catch(() => {
            // 用户选择稍后处理
          })
        }, 1000)
      }
    }
    
  } catch (error) {
    console.error('数据一致性检查失败:', error)
    ElMessage.error('数据一致性检查失败')
  }
}

// 验证数据同步状态
const verifyDataSync = (taskId, expectedQuantity) => {
  const taskDetails = taskDetailsMap.value[taskId]
  const actualQuantity = taskDetails?.quantity
  
  console.log('数据同步验证:', {
    taskId,
    expectedQuantity,
    actualQuantity,
    isSync: actualQuantity === expectedQuantity,
    taskDetails: taskDetails ? '存在' : '不存在'
  })
  
  return actualQuantity === expectedQuantity
}

// 触发全局数据同步
const triggerGlobalDataSync = async (taskId, newQuantity) => {
  try {
    // 更新全局 store 中的任务数据
    await appStore.refreshTasks()
    
    // 发送跨页面同步事件
    window.dispatchEvent(new CustomEvent('task-quantity-updated', {
      detail: { 
        taskId, 
        newQuantity, 
        timestamp: Date.now(),
        source: 'injection-params'
      }
    }))
    
    console.log('全局数据同步触发成功:', { taskId, newQuantity })
  } catch (error) {
    console.error('全局数据同步失败:', error)
  }
}

// 加载可用的计划数据
const loadAvailablePlans = async () => {
  try {
    const response = await getPlans(0, 1000)
    availablePlans.value = response.content || []
  } catch (error) {
    console.error('获取可用计划失败:', error)
    availablePlans.value = []
  }
}

// 加载可用的注塑任务
const loadAvailableInjectionTasks = async () => {
  try {
    const response = await getTasks({ page: 0, size: 1000, processType: '注塑' })
    availableInjectionTasks.value = response.content || []
  } catch (error) {
    console.error('获取可用注塑任务失败:', error)
    availableInjectionTasks.value = []
  }
}

// 加载可用的注塑设备
const loadAvailableInjectionDevices = async () => {
  try {
    const response = await getDevices({ page: 0, size: 1000 })
    // 筛选注塑设备（设备编号以INJ开头或名称包含注塑）
    availableInjectionDevices.value = (response.content || []).filter(device => 
      device.deviceCode?.startsWith('INJ') || 
      device.name?.includes('注塑') ||
      device.type === '注塑'
    )
  } catch (error) {
    console.error('获取可用注塑设备失败:', error)
    availableInjectionDevices.value = []
  }
}

// ✅ 验证数量逻辑
const validateQuantityLogic = async () => {
  try {
    // 获取所有必要数据
    const [tasksResponse, plansResponse] = await Promise.all([
      getTasks({ page: 0, size: 1000 }),
      getPlans(0, 1000)
    ])
    
    const allTasks = tasksResponse.content || []
    const allPlans = plansResponse.content || []
    
    // 执行验证
    const validationResult = validateInjectionQuantityLogic(
      injectionParams.value, 
      allTasks, 
      allPlans
    )
    
    console.log('注塑参数数量逻辑验证结果:', validationResult)
    
    if (validationResult.isValid) {
      ElNotification({
        title: '验证通过',
        message: `数量逻辑验证通过！共验证 ${validationResult.summary.totalParams} 个参数`,
        type: 'success',
        duration: 3000
      })
    } else {
      ElNotification({
        title: '发现问题',
        message: `发现 ${validationResult.issues.length} 个严重问题，${validationResult.warnings.length} 个警告`,
        type: 'warning',
        duration: 5000
      })
    }
    
    // 输出详细报告到控制台
    const report = generateQuantityLogicReport(validationResult, { summary: {}, issues: [], warnings: [] })
    console.log('详细验证报告:')
    console.log(report)
    
  } catch (error) {
    console.error('验证失败:', error)
    ElMessage.error('验证过程中发生错误')
  }
}

// 在onMounted中添加这些调用
onMounted(async () => {
  await Promise.all([
    fetchInjectionParams(),
    loadPlanMap(),
    loadTaskMap(),
    loadDeviceMap(),
    loadTaskDetailsMap(),
    loadAvailablePlans(),
    loadAvailableInjectionTasks(),
    loadAvailableInjectionDevices()
  ])
  
  // 启动自动刷新
  appStore.startAutoRefresh(30000) // 30秒刷新一次
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})
</script>

<style scoped>
.injection-page {
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
}

/* 分页组件 */
.el-pagination {
  margin-top: 16px;
  text-align: right;
}

/* 表单样式 */
.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}

.unit-text {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

/* 表单项样式优化 */
.el-form-item {
  margin-bottom: 16px;
}

.el-input-number {
  width: 100%;
}

/* 对话框样式 */
.el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
}

/* 数量提示样式 */
.quantity-hint {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  border-radius: 4px;
  font-size: 12px;
}

.hint-text {
  color: #0369a1;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 任务数量信息样式 */
.quantity-info {
  margin-top: 8px;
  padding: 6px 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 12px;
}

.info-text {
  color: #475569;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 更新提示样式 */
.update-hint {
  margin-top: 8px;
}

.update-hint .el-alert {
  padding: 8px 12px;
}
</style>