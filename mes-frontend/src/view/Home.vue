<template>
  <div class="dashboard">
    <HeaderSection 
    title="欢迎使用MES手机壳生产管理平台"
    subtitle=" 当前环境：注塑车间 · 实时监控与管理中心"
    :showStats="false"
    />

    <!-- 四个统计卡片 -->
    <div class="stats-container">
      <el-card class="stat-card" shadow="always" v-for="stat in stats" :key="stat.label">
        <div class="stat-content">
          <el-icon :size="28" :style="{ color: stat.color }">
            <component :is="stat.icon" />
          </el-icon>
          <div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts">
      <el-card shadow="hover" class="chart-card" v-if="showChart">
        <v-chart
          :option="planTrendOption"
          autoresize
          style="width: 100%; height: 300px;"
        />
      </el-card>
      <el-card shadow="hover" class="chart-card" v-if="showChart">
        <v-chart
          :option="deviceStatusOption"
          autoresize
          style="width: 100%; height: 300px;"
        />
      </el-card>

      <!-- 新增设备情况 -->
      <el-card shadow="hover" class="chart-card" v-if="showChart">
        <v-chart
          :option="deviceUsageOption"
          autoresize
          style="width: 100%; height: 300px;"
        />
      </el-card>

      <!-- 新增生产完成情况 -->
      <el-card shadow="hover" class="chart-card" v-if="showChart">
        <v-chart
          :option="productionCompletionOption"
          autoresize
          style="width: 100%; height: 300px;"
        />
      </el-card>
    </div>

 
  </div>
</template>

<script setup>
import HeaderSection from '@/components/HeaderSection.vue'
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Setting, Finished, Warning } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import VChart from 'vue-echarts'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { useAppStore } from '@/stores'

const appStore = useAppStore()

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

const router = useRouter()
const showChart = ref(false) // 控制图表加载

const stats = computed(() => [
  { 
    label: '今日计划数', 
    value: appStore.totalPlans, 
    icon: Document, 
    color: '#409EFF' 
  },
  { 
    label: '进行中任务', 
    value: appStore.inProgressTasks, 
    icon: Setting, 
    color: '#67C23A' 
  },
  { 
    label: '已完成任务', 
    value: appStore.completedTasks, 
    icon: Finished, 
    color: '#E6A23C' 
  },
  { 
    label: '异常设备', 
    value: appStore.pendingDevices, 
    icon: Warning, 
    color: '#F56C6C' 
  }
])

const navItems = ref([
  { name: '生产计划管理', path: '/plan' },
  { name: '任务管理', path: '/task' },
  { name: '设备监控', path: '/device' },
  { name: '注塑工艺参数', path: '/injection' },
  { name: '图案管理', path: '/pattern' },
])

const planTrendOption = ref({
  title: { text: '计划下发趋势', left: 'center' },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#E4E7ED',
    borderWidth: 1,
    textStyle: {
      color: '#333'
    },
    formatter: function(params) {
      return `
        <div style="padding: 8px;">
          <div style="font-weight: bold; margin-bottom: 8px; color: #333;">
            ${params[0].axisValue}
          </div>
          <div style="margin-bottom: 4px;">
            <span style="color: #666;">计划数：</span>
            <span style="font-weight: bold; color: #409EFF;">${params[0].data} 个</span>
          </div>
          <div>
            <span style="color: #666;">趋势：</span>
            <span style="font-weight: bold; color: #67C23A;">${params[0].data > 3 ? '上升' : params[0].data < 3 ? '下降' : '平稳'}</span>
          </div>
        </div>
      `
    }
  },
  xAxis: { 
    type: 'category', 
    data: ['6月14日', '6月15日', '6月16日', '6月17日', '6月18日'],
    axisLabel: {
      color: '#666'
    }
  },
  yAxis: { 
    type: 'value',
    axisLabel: {
      color: '#666'
    }
  },
  series: [
    { 
      data: [3, 5, 2, 4, 6], 
      type: 'line', 
      smooth: true, 
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ]
        }
      },
      itemStyle: {
        color: '#409EFF',
        borderWidth: 2,
        borderColor: '#fff'
      },
      lineStyle: {
        color: '#409EFF',
        width: 3
      }
    }
  ]
})

const deviceStatusOption = computed(() => {
  const totalDevices = appStore.totalDevices
  const runningDevices = appStore.runningDevices
  const pendingDevices = appStore.pendingDevices
  const idlingDevices = appStore.idlingDevices
  
  return {
    title: { text: '设备状态分布', left: 'center' },
    tooltip: { 
      trigger: 'item',
      formatter: function(params) {
        const percent = totalDevices > 0 ? ((params.value / totalDevices) * 100).toFixed(1) : 0
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 8px; color: #333;">
              ${params.name}
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #666;">数量：</span>
              <span style="font-weight: bold; color: #409EFF;">${params.value} 台</span>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #666;">占比：</span>
              <span style="font-weight: bold; color: #67C23A;">${percent}%</span>
            </div>
            <div>
              <span style="color: #666;">总计：</span>
              <span style="font-weight: bold; color: #E6A23C;">${totalDevices} 台</span>
            </div>
          </div>
        `
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      }
    },
    legend: { 
      bottom: 10,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '状态',
        type: 'pie',
        radius: '60%',
        itemStyle: {
          borderRadius: 4,
          borderWidth: 2,
          borderColor: '#fff'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data: [
          { 
            value: runningDevices, 
            name: '运行中',
            itemStyle: { color: '#67C23A' }
          },
          { 
            value: pendingDevices, 
            name: '故障',
            itemStyle: { color: '#F56C6C' }
          },
          { 
            value: idlingDevices, 
            name: '空闲',
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }
    ]
  }
})

// 新增设备使用情况图表（柱状图示例）
const deviceUsageOption = ref({
  title: { text: '设备运行情况', left: 'center' },
  tooltip: { 
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#E4E7ED',
    borderWidth: 1,
    textStyle: {
      color: '#333'
    },
    formatter: function(params) {
      return `
        <div style="padding: 8px;">
          <div style="font-weight: bold; margin-bottom: 8px; color: #333;">
            ${params[0].name}
          </div>
          <div style="margin-bottom: 4px;">
            <span style="color: #666;">运行时间：</span>
            <span style="font-weight: bold; color: #409EFF;">${params[0].data} 小时</span>
          </div>
          <div>
            <span style="color: #666;">状态：</span>
            <span style="font-weight: bold; color: #67C23A;">${params[0].data > 100 ? '高效运行' : params[0].data > 80 ? '正常运行' : '待优化'}</span>
          </div>
        </div>
      `
    }
  },
  xAxis: {
    type: 'category',
    data: ['设备A', '设备B', '设备C', '设备D', '设备E'],
    axisLabel: {
      color: '#666'
    }
  },
  yAxis: {
    type: 'value',
    name: '运行小时',
    axisLabel: {
      color: '#666'
    }
  },
  series: [
    {
      name: '运行小时',
      type: 'bar',
      data: [120, 98, 135, 80, 90],
      itemStyle: { 
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#67C23A' }
          ]
        },
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 新增生产完成情况（环形图示例）
const productionCompletionOption = computed(() => {
  const totalTasks = appStore.totalTasks
  const completedTasks = appStore.completedTasks
  const inProgressTasks = appStore.inProgressTasks
  const pendingTasks = appStore.pendingTasks
  
  // 计算百分比
  const completedPercent = totalTasks > 0 ? ((completedTasks / totalTasks) * 100).toFixed(1) : 0
  const inProgressPercent = totalTasks > 0 ? ((inProgressTasks / totalTasks) * 100).toFixed(1) : 0
  const pendingPercent = totalTasks > 0 ? ((pendingTasks / totalTasks) * 100).toFixed(1) : 0
  
  return {
    title: { text: '生产完成情况', left: 'center' },
    tooltip: { 
      trigger: 'item',
      formatter: function(params) {
        const percent = totalTasks > 0 ? ((params.value / totalTasks) * 100).toFixed(1) : 0
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 8px; color: #333;">
              ${params.name}
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #666;">数量：</span>
              <span style="font-weight: bold; color: #409EFF;">${params.value} 个</span>
            </div>
            <div style="margin-bottom: 4px;">
              <span style="color: #666;">占比：</span>
              <span style="font-weight: bold; color: #67C23A;">${percent}%</span>
            </div>
            <div>
              <span style="color: #666;">总计：</span>
              <span style="font-weight: bold; color: #E6A23C;">${totalTasks} 个</span>
            </div>
          </div>
        `
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: {
        color: '#333'
      }
    },
    legend: { 
      bottom: 10,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '完成情况',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: 'center',
          formatter: function() {
            if (totalTasks === 0) return '暂无数据'
            return `${completedPercent}%\n已完成`
          },
          fontSize: 16,
          fontWeight: 'bold',
          color: '#409EFF',
          lineHeight: 20
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: { show: false },
        itemStyle: {
          borderRadius: 4,
          borderWidth: 2,
          borderColor: '#fff'
        },
        data: [
          { 
            value: completedTasks, 
            name: '已完成',
            itemStyle: { color: '#67C23A' }
          },
          { 
            value: inProgressTasks, 
            name: '进行中',
            itemStyle: { color: '#409EFF' }
          },
          { 
            value: pendingTasks, 
            name: '待下发',
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }
    ]
  }
})

const navigate = (path) => {
  router.push(path)
}

onMounted(() => {
  nextTick(() => {
    showChart.value = true
    // 加载所有数据
    appStore.fetchAllData()
    // 启动自动刷新
    appStore.startAutoRefresh(30000) // 30秒刷新一次
  })
})

onUnmounted(() => {
  // 停止自动刷新
  appStore.stopAutoRefresh()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f5f7fa;
}

.welcome-card {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fffbe6;
  border-left: 5px solid #f7ba2a;
}

.welcome-text h2 {
  margin: 0 0 10px;
  font-weight: bold;
}

.stats-container {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.stat-card {
  flex: 1;
  padding: 16px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
}

.stat-label {
  color: #888;
  font-size: 14px;
}
.charts {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  justify-content: space-between;
}

.chart-card {
  width: 48%;
  height: 300px; /* 这里高度改为300，与图表保持一致 */
  padding: 8px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.chart-card .el-card__body {
  padding: 0 !important;
  height: 100%;
}

.chart-card v-chart {
  width: 100% !important;
  height: 100% !important; /* 100%撑满父容器 */
}
.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}


.nav-card {
  padding: 20px;
}

.nav-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
}
</style>
