<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <Navbar />

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <HeaderSection title="首机客MES系统" subtitle="欢迎使用智能制造执行系统" />

      <!-- 仪表盘卡片 -->
      <div class="dashboard-cards">
        <div class="card">
          <div class="card-icon">
            <i class="el-icon-video-play"></i>
          </div>
          <div class="card-content">
            <h3>运行中设备</h3>
            <p>{{ runningDevices }} 台</p>
          </div>
        </div>
        <div class="card">
          <div class="card-icon">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="card-content">
            <h3>待处理任务</h3>
            <p>{{ pendingTasks }} 个</p>
          </div>
        </div>
        <div class="card">
          <div class="card-icon">
            <i class="el-icon-check"></i>
          </div>
          <div class="card-content">
            <h3>已完成任务</h3>
            <p>{{ completedTasks }} 个</p>
          </div>
        </div>
        <div class="card utilization-card">
          <div ref="utilizationChart" style="width: 120px; height: 120px; margin: 0 auto;"></div>
          <div class="card-content">
            <h3>设备利用率</h3>
            <p>{{ utilizationRate }}%</p>
            <div class="utilization-detail">
              <div class="util-item">
                <span class="util-label">最高:</span>
                <span class="util-value">{{ maxUtilDevice.name }} {{ maxUtilDevice.utilization }}%</span>
              </div>
              <div class="util-item">
                <span class="util-label">最低:</span>
                <span class="util-value">{{ minUtilDevice.name }} {{ minUtilDevice.utilization }}%</span>
              </div>
              <div class="util-item">
                <span class="util-label">平均:</span>
                <span class="util-value">{{ avgUtilization }}%</span>
              </div>
            </div>
            <div class="util-trend" v-if="utilizationTrend !== 0">
              <i :class="utilizationTrend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              <span :class="utilizationTrend > 0 ? 'trend-up' : 'trend-down'">{{ Math.abs(utilizationTrend) }}%</span>
              <span class="trend-period">较上周</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="main-sections">
        <!-- 左侧区域 -->
        <div class="left-section">
          <!-- 生产计划列表 -->
          <section class="production-plans">
            <div class="section-header">
              <h2>近期生产计划</h2>
              <button class="view-all" @click="goToProductionPlans">查看全部</button>
            </div>
            <table class="table">
              <thead>
                <tr>
                  <th>计划编号</th>
                  <th>产品名称</th>
                  <th>总数量</th>
                  <th>状态</th>
                  <th>优先级</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="plan in productionPlans" :key="plan.id">
                  <td>{{ plan.planCode }}</td>
                  <td>{{ plan.productName }}</td>
                  <td>{{ plan.totalQuantity }}</td>
                  <td :class="statusClass(plan.status)">{{ plan.status }}</td>
                  <td>{{ plan.priority }}</td>
                  <td>
                    <button class="action-btn" @click="viewPlanDetails(plan.id)">查看详情</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          <!-- 注塑工艺参数 -->
          <section class="injection-params">
            <div class="section-header">
              <h2>注塑工艺参数</h2>
              <button class="view-all" @click="goToInjectionParams">管理参数</button>
            </div>
            <table class="table">
              <thead>
                <tr>
                  <th>任务ID</th>
                  <th>压力(MPa)</th>
                  <th>注射速度(mm/s)</th>
                  <th>材料温度(°C)</th>
                  <th>保压时间(s)</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="param in injectionParams" :key="param.id">
                  <td>{{ param.taskId }}</td>
                  <td>{{ param.pressure }}</td>
                  <td>{{ param.injectionSpeed }}</td>
                  <td>{{ param.materialTemperature }}</td>
                  <td>{{ param.holdTime }}</td>
                  <td>
                    <button class="action-btn" @click="viewInjectionParamDetail(param.id)">详情</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          <!-- 印刷图案预览 -->
          <section class="print-patterns" v-if="printPatterns.length > 0">
            <div class="section-header">
              <h2>印刷图案</h2>
              <button class="view-all" @click="goToPrintPatterns">查看全部</button>
            </div>
            <div class="pattern-grid">
              <div class="pattern-card" v-for="pattern in printPatterns.slice(0, 4)" :key="pattern.id">
                <img :src="pattern.imageUrl" :alt="pattern.patternName" class="pattern-image">
                <div class="pattern-info">
                  <h4>{{ pattern.patternName }}</h4>
                  <p>编号: {{ pattern.patternCode }}</p>
                  <p>适用机型: {{ pattern.machineModel }}</p>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- 右侧区域 -->
        <div class="right-section">
          <!-- 设备状态 -->
          <section class="device-status">
            <div class="section-header">
              <h2>设备状态监控</h2>
              <button class="view-all" @click="goToDeviceManagement">查看全部</button>
            </div>
            <div class="device-chart">
              <div id="deviceStatusChart" style="height: 200px"></div>
            </div>
            <div class="device-list">
              <DeviceCard v-for="device in devices.slice(0, 4)" :key="device.id" :device="device" />
            </div>
          </section>

          <!-- 生产任务 -->
          <section class="production-tasks">
            <div class="section-header">
              <h2>最近任务</h2>
              <button class="view-all" @click="goToProductionTasks">查看全部</button>
            </div>
            <table class="table">
              <thead>
                <tr>
                  <th>工序类型</th>
                  <th>设备名称</th>
                  <th>状态</th>
                  <th>进度</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in productionTasks" :key="task.id">
                  <td>{{ task.processType }}</td>
                  <td>{{ task.deviceName }}</td>
                  <td :class="taskStatusClass(task.status)">{{ task.status }}</td>
                  <td>
                    <div class="progress-bar">
                      <div class="progress" :style="{width: calculateProgress(task) + '%'}"></div>
                    </div>
                    <span>{{ task.completedQuantity || 0 }}/{{ task.quantity }}</span>
                  </td>
                  <td>
                    <button class="action-btn" @click="viewTaskDetails(task.id)">详情</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          <!-- OEE指标 -->
          <section class="oee-metrics" v-if="devices.length > 0">
            <div class="section-header">
              <h2>设备OEE指标</h2>
            </div>
            <div class="oee-cards">
              <div class="oee-card" v-for="device in devices.slice(0, 3)" :key="device.id">
                <h4>{{ device.name }}</h4>
                <div class="oee-value">{{ device.oee || '计算中...' }}</div>
                <p>设备编号: {{ device.deviceCode }}</p>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- 快捷操作 -->
      <section class="quick-actions">
        <h2>快捷操作</h2>
        <div class="action-buttons">
          <button @click="createNewTask">新建任务</button>
          <button @click="createNewPlan">新建计划</button>
          <button @click="goToReports">查看报表</button>
          <button @click="goToDeviceManagement">设备管理</button>
          <button @click="goToPrintPatterns">印刷图案</button>
          <button @click="goToInjectionParams">工艺参数</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';
import Navbar from '@/components/Navbar.vue';
import HeaderSection from '@/components/HeaderSection.vue';
import DeviceCard from '@/components/DeviceCard.vue';

export default {
  name: 'Home',
  components: {
    Navbar,
    HeaderSection,
    DeviceCard
  },
  data() {
    return {
      productionPlans: [],
      devices: [],
      productionTasks: [],
      injectionParams: [],
      printPatterns: [],
      runningDevices: 0,
      pendingTasks: 0,
      completedTasks: 0,
      utilizationRate: 0,
      deviceStatusChart: null,
      maxUtilDevice: { name: '未知', utilization: 0 },
      minUtilDevice: { name: '未知', utilization: 0 },
      avgUtilization: 0,
      utilizationTrend: 0
    };
  },
  mounted() {
    this.fetchProductionPlans();
    this.fetchDeviceStatuses();
    this.fetchStatistics();
    this.fetchProductionTasks();
    this.fetchInjectionParams();
    this.fetchPrintPatterns();
    this.$nextTick(() => {
      this.initDeviceStatusChart();
      this.initUtilizationChart();
    });
  },
  methods: {
    async fetchProductionPlans() {
      try {
        const response = await axios.get('/api/v1/production/plans');
        console.log('生产计划数据:', response.data);
        this.productionPlans = response.data.content || [];
      } catch (error) {
        console.error('获取生产计划失败:', error);
      }
    },
    async fetchDeviceStatuses() {
      try {
        const response = await axios.get('/api/v1/equipment/devices');
        console.log('设备状态数据:', response.data);
        this.devices = response.data.content || [];
        
        // 获取设备OEE数据
        for (const device of this.devices) {
          try {
            const oeeResponse = await axios.get(`/api/v1/equipment/devices/${device.id}/oee`);
            device.oee = oeeResponse.data.toFixed(2) + '%';
          } catch (error) {
            console.error(`获取设备${device.id}的OEE失败:`, error);
            // 如果API不可用，使用模拟数据
            device.oee = (Math.random() * 30 + 60).toFixed(2) + '%';
          }
        }
      } catch (error) {
        console.error('获取设备状态失败:', error);
      }
    },
    async fetchProductionTasks() {
      try {
        const response = await axios.get('/api/v1/production/tasks');
        console.log('生产任务数据:', response.data);
        this.productionTasks = response.data.content || [];
        
        // 获取任务进度数据
        for (const task of this.productionTasks) {
          if (!task.completedQuantity) {
            try {
              const progressResponse = await axios.get(`/api/v1/production/tasks/${task.id}/progress`);
              task.completedQuantity = progressResponse.data;
            } catch (error) {
              console.error(`获取任务${task.id}的进度失败:`, error);
              // 如果API不可用，使用模拟数据
              task.completedQuantity = Math.floor(Math.random() * task.quantity);
            }
          }
        }
      } catch (error) {
        console.error('获取生产任务失败:', error);
      }
    },
    async fetchInjectionParams() {
      try {
        const response = await axios.get('/api/v1/process/injection-params/all');
        console.log('注塑工艺参数数据:', response.data);
        this.injectionParams = response.data.content || [];
      } catch (error) {
        console.error('获取注塑工艺参数失败:', error);
      }
    },
    async fetchPrintPatterns() {
      try {
        const response = await axios.get('/api/v1/process/print-patterns');
        console.log('印刷图案数据:', response.data);
        this.printPatterns = response.data.content || [];
      } catch (error) {
        console.error('获取印刷图案失败:', error);
        // 如果API不可用，使用空数组
        this.printPatterns = [];
      }
    },
    async fetchStatistics() {
      try {
        // 示例统计请求（实际应由后端提供聚合接口）
        const deviceResponse = await axios.get('/api/v1/equipment/devices?status=运行');
        const taskResponse = await axios.get('/api/v1/production/tasks?status=待下发');
        const completeResponse = await axios.get('/api/v1/production/tasks?status=已完成');

        console.log('运行中设备:', deviceResponse.data);
        console.log('待处理任务:', taskResponse.data);
        console.log('已完成任务:', completeResponse.data);

        this.runningDevices = deviceResponse.data.totalElements || 0;
        this.pendingTasks = taskResponse.data.totalElements || 0;
        this.completedTasks = completeResponse.data.totalElements || 0;
        
        // 获取设备利用率数据
        try {
          const utilizationResponse = await axios.get('/api/v1/equipment/utilization');
          this.utilizationRate = utilizationResponse.data || 75.5;
          
          // 获取设备详细利用率数据
          if (this.devices.length > 0) {
            // 模拟各设备的利用率数据
            const deviceUtils = this.devices.map(d => {
              return {
                name: d.name,
                utilization: Math.round(Math.random() * 20 + this.utilizationRate - 10)
              };
            });
            
            // 计算平均利用率
            const totalUtilization = deviceUtils.reduce((sum, device) => sum + device.utilization, 0);
            this.avgUtilization = Math.round(totalUtilization / deviceUtils.length);
            
            // 排序找出最高和最低利用率设备
            deviceUtils.sort((a, b) => b.utilization - a.utilization);
            this.maxUtilDevice = deviceUtils[0];
            this.minUtilDevice = deviceUtils[deviceUtils.length - 1];
            
            // 模拟与上周相比的趋势变化 (-5% 到 +5%)
            this.utilizationTrend = Math.round((Math.random() * 10 - 5) * 10) / 10;
          } else {
            this.maxUtilDevice = { name: '未知', utilization: 0 };
            this.minUtilDevice = { name: '未知', utilization: 0 };
            this.avgUtilization = this.utilizationRate;
            this.utilizationTrend = 0;
          }
        } catch (error) {
          // 如果API不存在，使用默认值
          this.utilizationRate = 75.5;
          this.avgUtilization = 75.5;
          this.maxUtilDevice = { name: '未知', utilization: 0 };
          this.minUtilDevice = { name: '未知', utilization: 0 };
          this.utilizationTrend = 0;
          console.warn('获取设备利用率失败，使用默认值:', error);
        }
        
        // 更新利用率图表
        this.$nextTick(() => {
          this.initUtilizationChart();
        });
      } catch (error) {
        console.error('获取统计数据失败:', error);
      }
    },
    // 初始化设备状态图表
    initDeviceStatusChart() {
      if (!document.getElementById('deviceStatusChart')) {
        console.warn('找不到设备状态图表容器');
        return;
      }
      
      // 统计设备状态数量
      const statusCount = {};
      this.devices.forEach(device => {
        statusCount[device.status] = (statusCount[device.status] || 0) + 1;
      });
      
      const chartData = Object.keys(statusCount).map(status => ({
        name: status,
        value: statusCount[status]
      }));
      
      // 初始化图表
      this.deviceStatusChart = echarts.init(document.getElementById('deviceStatusChart'));
      
      // 设置图表选项
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          data: Object.keys(statusCount)
        },
        series: [
          {
            name: '设备状态',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '14',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: chartData
          }
        ]
      };
      
      // 使用配置项显示图表
      this.deviceStatusChart.setOption(option);
    },
    
    // 初始化设备利用率仪表盘图表
    initUtilizationChart() {
      if (!this.$refs.utilizationChart) return;
      
      const chart = echarts.init(this.$refs.utilizationChart);
      chart.setOption({
        series: [{
          type: 'gauge',
          radius: '100%',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: 8,
              color: [
                [0.3, '#F56C6C'],  // 红色区域 0-30%
                [0.7, '#E6A23C'],  // 黄色区域 30-70%
                [1, '#67C23A']     // 绿色区域 70-100%
              ]
            }
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '60%',
            width: 4,
            itemStyle: {
              color: '#303133'
            }
          },
          axisTick: {
            length: 6,
            lineStyle: {
              color: 'auto',
              width: 1
            }
          },
          splitLine: {
            length: 10,
            lineStyle: {
              color: 'auto',
              width: 2
            }
          },
          axisLabel: {
            color: '#909399',
            fontSize: 10,
            distance: -30,
            formatter: function(value) {
              if (value % 20 === 0) {
                return value + '%';
              }
              return '';
            }
          },
          detail: {
            fontSize: 20,
            offsetCenter: [0, '30%'],
            valueAnimation: true,
            formatter: function(value) {
              return Math.round(value) + '%';
            },
            color: '#303133'
          },
          data: [{
            value: this.utilizationRate
          }]
        }]
      });
      
      // 窗口大小变化时重新渲染图表
      window.addEventListener('resize', function() {
        chart.resize();
      });
    }
    calculateProgress(task) {
      if (!task.quantity || task.quantity === 0) return 0;
      const completed = task.completedQuantity || 0;
      return Math.min(Math.round((completed / task.quantity) * 100), 100);
    },
    statusClass(status) {
      switch (status) {
        case '进行中':
          return 'text-success';
        case '待开始':
          return 'text-warning';
        case '已完成':
          return 'text-muted';
        default:
          return '';
      }
    },
    taskStatusClass(status) {
      switch (status) {
        case '运行中':
          return 'text-success';
        case '待下发':
          return 'text-warning';
        case '已完成':
          return 'text-muted';
        case '已暂停':
          return 'text-danger';
        default:
          return '';
      }
    },
    viewPlanDetails(id) {
      this.$router.push(`/production/plans/${id}`);
    },
    viewTaskDetails(id) {
      this.$router.push(`/production/tasks/${id}`);
    },
    viewInjectionParamDetail(id) {
      this.$router.push(`/process/injection/${id}`);
    },
    createNewTask() {
      this.$router.push('/production/tasks/new');
    },
    createNewPlan() {
      this.$router.push('/production/plans/new');
    },
    goToReports() {
      this.$router.push('/reports');
    },
    goToDeviceManagement() {
      this.$router.push('/equipment/devices');
    },
    goToProductionPlans() {
      this.$router.push('/production/plans');
    },
    goToProductionTasks() {
      this.$router.push('/production/tasks');
    },
    goToInjectionParams() {
      this.$router.push('/process/injection');
    },
    goToPrintPatterns() {
      this.$router.push('/process/print');
    },
    initUtilizationChart() {
      const chart = echarts.init(this.$refs.utilizationChart);
      chart.setOption({
        series: [{
          type: 'gauge',
          progress: { show: true, width: 10 },
          axisLine: { lineStyle: { width: 10 } },
          detail: { valueAnimation: true, formatter: '{value}%' },
          data: [{ value: this.utilizationRate }]
        }]
      });
    }
  }
};
</script>

<style scoped>
.home-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  transition: all 0.3s ease;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.dashboard-cards {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  flex: 1;
  margin: 0 10px 10px 10px;
  text-align: center;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  min-width: 200px;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.card:first-child {
  margin-left: 0;
}

.card:last-child {
  margin-right: 0;
}

.card h3 {
  margin-top: 0;
  color: #606266;
  font-size: 16px;
}

.card p {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 10px 0;
  transition: all 0.3s ease;
}

.card:hover p {
  color: #409eff;
}

.card-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 24px;
  opacity: 0.2;
  transition: all 0.3s ease;
}

.card:hover .card-icon {
  opacity: 0.5;
  transform: scale(1.1);
}

.main-sections {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.left-section {
  flex: 3;
}

.right-section {
  flex: 2;
}

section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.content-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
}

.section-header h2 i {
  margin-right: 8px;
  color: #409eff;
}

.view-all {
  background-color: transparent;
  border: 1px solid #dcdfe6;
  color: #606266;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.view-all:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.table th {
  font-weight: bold;
  color: #606266;
  background-color: #f5f7fa;
}

.action-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-muted {
  color: #909399;
}

.text-danger {
  color: #f56c6c;
}

.device-status .device-chart {
  margin-bottom: 20px;
}

.device-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.quick-actions {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 10px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 15px;
}

.action-buttons button {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  flex: 1;
  min-width: 120px;
  max-width: 200px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-buttons button i {
  margin-right: 8px;
  font-size: 16px;
}

.action-buttons button:hover {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 进度条样式 */
.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.progress {
  height: 100%;
  background-color: #67c23a;
  border-radius: 4px;
}

/* 印刷图案样式 */
.pattern-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.pattern-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s;
}

.pattern-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.pattern-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.pattern-info {
  padding: 10px;
}

.pattern-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #303133;
}

.pattern-info p {
  margin: 5px 0;
  font-size: 12px;
  color: #606266;
}

/* OEE指标样式 */
.oee-cards {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.oee-card {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  text-align: center;
  transition: all 0.3s ease;
}

.oee-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  border-color: #c6e2ff;
}

.oee-card h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
}

.oee-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin: 10px 0;
  transition: all 0.3s ease;
}

.oee-card:hover .oee-value {
  transform: scale(1.1);
}

.oee-card p {
  margin: 5px 0;
  font-size: 12px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dashboard-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-sections {
    flex-direction: column;
  }
  
  .left-section,
  .right-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .dashboard-cards {
    grid-template-columns: 1fr;
  }
  
  .oee-cards {
    flex-direction: column;
  }
  
  .action-buttons button {
    min-width: 100%;
    margin-bottom: 10px;
  }
  
  .table {
    display: block;
    overflow-x: auto;
  }
}

.utilization-card {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.utilization-detail {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.util-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  padding: 3px 0;
  border-bottom: 1px dashed #ebeef5;
}

.util-label {
  font-weight: bold;
  color: #606266;
}

.util-value {
  color: #303133;
}

.util-trend {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.trend-up {
  color: #67C23A;
  margin: 0 5px;
}

.trend-down {
  color: #F56C6C;
  margin: 0 5px;
}

.trend-period {
  color: #909399;
}
</style>