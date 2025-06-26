<template>
  <div class="device-monitor">
    <!-- 顶部信息 + 导航按钮 -->
    <el-card class="header-card" shadow="hover">
      <div class="header-flex">
        <!-- 左边标题和描述 -->
        <div class="header-left">
          <h2 class="title">📊 设备状态监控中心</h2>
          <p class="subtitle">实时掌握车间设备运行状态与关键指标</p>
        </div>



        <!-- 右边导航按钮 -->
         <div class="nav-row">

          <button class="nav-btn" @click="go('/home')">首页</button>
          <button class="nav-btn" @click="go('/plan')">生产计划管理</button>
          <button class="nav-btn" @click="go('/task')">任务管理</button>
          <button class="nav-btn" @click="go('/injection')">注塑参数</button>
          <button class="nav-btn" @click="go('/pattern')">图案参数</button>
        </div>
      </div>
    </el-card>


    <!-- 筛选组件 -->
    <DeviceFilter
      :filters="filters"
      @filter="filterDevices"
      @reset="resetFilters"
    />


    <!-- 设备展示卡片 -->
    <div class="device-grid">
      <DeviceCard
        v-for="device in filteredDevices"
        :key="device.id"
        :device="device"
        @view="viewDetail"
      />
    </div>

    <!-- 设备详情弹窗 -->
    <DeviceDetailDialog

  v-model:visible="detailVisible"
  :device="selectedDevice"
  @updateStatus="updateDeviceStatus"
/>


    <!-- 新增设备弹窗 -->
    <el-dialog
      title="新增设备"
      v-model="addDeviceDialogVisible"
      width="400px"
      :before-close="handleBeforeClose"
    >
      <el-form :model="newDevice" label-width="100px" ref="addDeviceForm">
        <el-form-item
          label="设备编号"
          :rules="[{ required: true, message: '请输入设备编号', trigger: 'blur' }]"
        >
          <el-input v-model="newDevice.id" autocomplete="off" />
        </el-form-item>
        <el-form-item
          label="设备类型"
          :rules="[{ required: true, message: '请输入设备类型', trigger: 'blur' }]"
        >
          <el-input v-model="newDevice.type" autocomplete="off" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="newDevice.status" placeholder="请选择">
            <el-option label="运行中" value="运行中" />
            <el-option label="故障" value="故障" />
            <el-option label="空闲" value="空闲" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用时长（小时）">
          <el-input-number v-model="newDevice.usageHours" :min="0" />
        </el-form-item>
        <el-form-item label="注塑压力 (MPa)">
          <el-input-number v-model="newDevice.pressure" :min="0" />
        </el-form-item>
        <el-form-item label="开启次数">
          <el-input-number v-model="newDevice.openTimes" :min="0" />
        </el-form-item>
        <el-form-item label="注塑时间 (秒)">
          <el-input-number v-model="newDevice.injectionTime" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeAddDeviceDialog">取消</el-button>
        <el-button type="primary" @click="addDevice">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import DeviceFilter from '@/components/DeviceFilter.vue'
import DeviceCard from '@/components/DeviceCard.vue'
import DeviceDetailDialog from '@/components/DeviceDetailDialog.vue'

const router = useRouter()
const go = (path) => {
  router.push(path)
}

const devices = ref([
  { id: 'D001', type: '注塑机', status: '运行中', usageHours: 200, pressure: 120, openTimes: 5000, injectionTime: 6.5 },
  { id: 'D002', type: '压机', status: '故障', usageHours: 80, pressure: 95, openTimes: 1200, injectionTime: 5.2 },
  { id: 'D003', type: '注塑机', status: '空闲', usageHours: 0, pressure: 0, openTimes: 0, injectionTime: 0 },
])

const filters = ref({ type: '', status: '' })

const filteredDevices = computed(() =>
  devices.value.filter(d =>
    (!filters.value.type || d.type === filters.value.type) &&
    (!filters.value.status || d.status === filters.value.status)
  )
)

const detailVisible = ref(false)
const selectedDevice = ref(null)

const viewDetail = (device) => {
  selectedDevice.value = device
  detailVisible.value = true
}

const filterDevices = () => {}
const resetFilters = () => {
  filters.value = { type: '', status: '' }
}

// 新增设备弹窗相关
const addDeviceDialogVisible = ref(false)
const newDevice = ref({
  id: '',
  type: '',
  status: '空闲',
  usageHours: 0,
  pressure: 0,
  openTimes: 0,
  injectionTime: 0,
})

const openAddDeviceDialog = () => {
  addDeviceDialogVisible.value = true
}

const closeAddDeviceDialog = () => {
  addDeviceDialogVisible.value = false
}

const handleBeforeClose = (done) => {
  done()
}

const addDevice = () => {
  if (!newDevice.value.id || !newDevice.value.type) {
    alert('设备编号和设备类型不能为空！')
    return
  }
  const exists = devices.value.some(d => d.id === newDevice.value.id)
  if (exists) {
    alert('设备编号已存在，请更换！')
    return
  }

  devices.value.push({ ...newDevice.value })
  closeAddDeviceDialog()
  newDevice.value = {
    id: '',
    type: '',
    status: '空闲',
    usageHours: 0,
    pressure: 0,
    openTimes: 0,
    injectionTime: 0,
  }
}
const updateDeviceStatus = (newStatus) => {
  if (selectedDevice.value) {
    // 找到该设备并更新其状态
    const index = devices.value.findIndex(d => d.id === selectedDevice.value.id)
    if (index !== -1) {
      devices.value[index].status = newStatus
      selectedDevice.value.status = newStatus // 保持详情数据一致
    }
  }
}


</script>

<style scoped>
.device-monitor {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部整体布局为左右结构 */
.header-card {
  margin-bottom: 20px;
  border-radius: 12px;
  color: white;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  max-width: 70%;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.subtitle {
  margin-top: 6px;
  font-size: 14px;
  opacity: 0.95;
}


/* 顶部导航按钮水平排布 */

.nav-row {
  display: flex;
  gap: 12px;
}

.nav-btn {
  background-color: #ffffff22;
  color: #fff;
  border: 1px solid #ffffff33;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.nav-btn:hover {
  background-color: #ffffff44;
}


.add-device-btn {
  font-weight: 700;
  background-color: #4caf50aa;
  border-color: #4caf50cc;
}

.add-device-btn:hover {
  background-color: #4caf50dd;
}

/* 筛选组件和新增设备按钮同一行 */
.filter-add-wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}


.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

</style>