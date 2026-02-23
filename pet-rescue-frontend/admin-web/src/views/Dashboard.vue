<template>
  <div class="dashboard-container">
    <div class="header-section">
      <div class="welcome-info">
        <h2 class="welcome-title">你好，管理员</h2>
        <p class="welcome-subtitle">欢迎回到控制台，今天是 {{ currentDate }}</p>
      </div>
      <div class="date-badge">
        <el-icon class="date-icon"><Calendar /></el-icon>
        <span>{{ todayDate }}</span>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card rescue-card" v-loading="loading">
        <div class="stat-header">
          <div class="stat-icon-wrapper blue">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="stat-trend" v-if="dashboardData.totalRescueCount > 0">
            <el-icon><TrendCharts /></el-icon>
            <span>+12%</span>
          </div>
        </div>
        <div class="stat-content">
          <p class="stat-label">宠物总数</p>
          <h3 class="stat-value">{{ dashboardData.totalRescueCount }}</h3>
        </div>
      </div>

      <div class="stat-card adoption-card" v-loading="loading">
        <div class="stat-header">
          <div class="stat-icon-wrapper emerald">
            <el-icon><House /></el-icon>
          </div>
          <div class="stat-trend emerald">
            <el-icon><TrendCharts /></el-icon>
            <span>+5%</span>
          </div>
        </div>
        <div class="stat-content">
          <p class="stat-label">领养总数</p>
          <h3 class="stat-value">{{ dashboardData.totalAdoptionCount }}</h3>
        </div>
      </div>

      <div class="stat-card rate-card" v-loading="loading">
        <div class="stat-header">
          <div class="stat-icon-wrapper yellow">
            <el-icon><DataLine /></el-icon>
          </div>
        </div>
        <div class="stat-content">
          <p class="stat-label">领养率</p>
          <h3 class="stat-value">{{ Number(dashboardData.adoptionRate).toFixed(1) }}%</h3>
        </div>
      </div>

      <div class="stat-card audit-card" v-loading="loading">
        <div class="stat-header">
          <div class="stat-icon-wrapper rose">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-trend rose" v-if="dashboardData.pendingAuditCount > 0">
            <span>Need Action</span>
          </div>
        </div>
        <div class="stat-content">
          <p class="stat-label">待审核申请</p>
          <h3 class="stat-value">{{ dashboardData.pendingAuditCount }}</h3>
        </div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card trend-chart">
        <div class="chart-header">
          <div class="chart-title-bar blue"></div>
          <h3>近7天数据趋势</h3>
        </div>
        <div class="chart-body" ref="trendChartRef"></div>
      </div>

      <div class="chart-card breed-chart">
        <div class="chart-header">
          <div class="chart-title-bar yellow"></div>
          <h3>热门品种分布</h3>
        </div>
        <div class="chart-body" ref="breedChartRef"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, nextTick } from 'vue'
import { FirstAidKit, House, TrendCharts, Bell, Calendar, DataLine } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardData } from '../api/user'
import { ElMessage } from 'element-plus'

const loading = ref(true)
const trendChartRef = ref(null)
const breedChartRef = ref(null)
let trendChart = null
let breedChart = null

const dashboardData = reactive({
  totalRescueCount: 0,
  totalAdoptionCount: 0,
  adoptionRate: 0,
  pendingAuditCount: 0,
  breedDistribution: [],
  dates: [],
  dailyRescueData: [],
  dailyAdoptionData: []
})

const currentDate = ref('')
const todayDate = ref('')

const updateDate = () => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
  
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  todayDate.value = `${year}-${month}-${day}`
}

const initCharts = (breedData, datesData, rescueData, adoptionData) => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        textStyle: {
          color: '#334155'
        }
      },
      legend: {
        data: ['救助数量', '领养申请数量'],
        bottom: '0%',
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: {
          color: '#64748b'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '12%',
        top: '8%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: datesData || [],
        axisLine: { 
          show: false
        },
        axisTick: { show: false },
        axisLabel: {
          color: '#94a3b8',
          fontSize: 12
        }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { 
          lineStyle: { 
            type: 'dashed', 
            color: '#f1f5f9' 
          } 
        },
        axisLabel: {
          color: '#94a3b8',
          fontSize: 12
        }
      },
      series: [
        {
          name: '救助数量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: rescueData || [],
          itemStyle: {
            color: '#3b82f6'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59, 130, 246, 0.15)' },
              { offset: 1, color: 'rgba(59, 130, 246, 0)' }
            ])
          },
          lineStyle: {
            width: 3,
            color: '#3b82f6'
          }
        },
        {
          name: '领养申请数量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: adoptionData || [],
          itemStyle: {
            color: '#10b981'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(16, 185, 129, 0.15)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0)' }
            ])
          },
          lineStyle: {
            width: 3,
            color: '#10b981'
          }
        }
      ]
    }
    trendChart.setOption(option)
  }

  if (breedChartRef.value) {
    breedChart = echarts.init(breedChartRef.value)
    const colors = ['#10b981', '#fbbf24', '#3b82f6', '#f472b6', '#a78bfa', '#64748b']
    const option = {
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e2e8f0',
        borderWidth: 1,
        textStyle: {
          color: '#334155'
        }
      },
      legend: {
        bottom: '0%',
        left: 'center',
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: {
          color: '#64748b',
          fontSize: 12
        }
      },
      series: [
        {
          name: '品种分布',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold',
              color: '#334155'
            },
            scale: true,
            scaleSize: 8
          },
          labelLine: {
            show: false
          },
          data: breedData || [],
          color: colors
        }
      ]
    }
    breedChart.setOption(option)
  }
}

const fetchData = async () => {
  try {
    loading.value = true
    const res = await getDashboardData()
    if (res.code === 200) {
      Object.assign(dashboardData, res.data)
      
      if (!dashboardData.dates) dashboardData.dates = []
      if (!dashboardData.dailyRescueData) dashboardData.dailyRescueData = []
      if (!dashboardData.dailyAdoptionData) dashboardData.dailyAdoptionData = []
      if (!dashboardData.breedDistribution) dashboardData.breedDistribution = []
      
      const breedData = JSON.parse(JSON.stringify(dashboardData.breedDistribution))
      const datesData = JSON.parse(JSON.stringify(dashboardData.dates))
      const rescueData = JSON.parse(JSON.stringify(dashboardData.dailyRescueData))
      const adoptionData = JSON.parse(JSON.stringify(dashboardData.dailyAdoptionData))

      nextTick(() => {
        initCharts(breedData, datesData, rescueData, adoptionData)
      })
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  trendChart?.resize()
  breedChart?.resize()
}

onMounted(() => {
  updateDate()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  breedChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.98);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-info {
  display: flex;
  flex-direction: column;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.welcome-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 4px 0 0 0;
}

.date-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 8px 16px;
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

.date-icon {
  color: #10b981;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-wrapper.blue {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-icon-wrapper.emerald {
  background: #ecfdf5;
  color: #10b981;
}

.stat-icon-wrapper.yellow {
  background: #fefce8;
  color: #eab308;
}

.stat-icon-wrapper.rose {
  background: #fff1f2;
  color: #f43f5e;
}

.stat-icon-wrapper .el-icon {
  font-size: 24px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #3b82f6;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 20px;
}

.stat-trend.emerald {
  color: #10b981;
  background: #ecfdf5;
}

.stat-trend.rose {
  color: #f43f5e;
  background: #fff1f2;
}

.stat-content {
  margin-top: 8px;
}

.stat-label {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  margin: 0 0 4px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.chart-title-bar {
  width: 4px;
  height: 24px;
  border-radius: 2px;
}

.chart-title-bar.blue {
  background: #3b82f6;
}

.chart-title-bar.yellow {
  background: #eab308;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.chart-body {
  height: 300px;
  width: 100%;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
