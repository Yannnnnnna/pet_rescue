<template>
  <div class="dashboard-container">
    <div class="header-section">
      <h2 class="welcome-title">
        <span>你好，管理员</span>
        <span class="sub-title">欢迎回到控制台</span>
      </h2>
      <div class="current-date">{{ currentDate }}</div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-cards">
      <div class="stat-card rescue-card" v-loading="loading">
        <div class="icon-wrapper">
          <el-icon><FirstAidKit /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">宠物总数</div>
          <div class="stat-value">
            <span ref="rescueCountRef">{{ dashboardData.totalRescueCount }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card adoption-card" v-loading="loading">
        <div class="icon-wrapper">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">领养总数</div>
          <div class="stat-value">
            <span ref="adoptionCountRef">{{ dashboardData.totalAdoptionCount }}</span>
          </div>
        </div>
      </div>

      <div class="stat-card rate-card" v-loading="loading">
        <div class="icon-wrapper">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">领养率</div>
          <div class="stat-value">
            <span ref="adoptionRateRef">{{ Number(dashboardData.adoptionRate).toFixed(1) }}</span>%
          </div>
        </div>
      </div>

      <div class="stat-card audit-card" v-loading="loading">
        <div class="icon-wrapper">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">待审核申请</div>
          <div class="stat-value">
            <span ref="pendingAuditRef">{{ dashboardData.pendingAuditCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card trend-chart">
        <div class="chart-header">
          <h3>近7天数据趋势</h3>
        </div>
        <div class="chart-body" ref="trendChartRef"></div>
      </div>

      <div class="chart-card breed-chart">
        <div class="chart-header">
          <h3>热门品种分布</h3>
        </div>
        <div class="chart-body" ref="breedChartRef"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, nextTick } from 'vue'
import { FirstAidKit, House, TrendCharts, Bell } from '@element-plus/icons-vue'
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

// 日期显示
const currentDate = ref('')
const updateDate = () => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
}

// 数字滚动动画
const animateValue = (obj, start, end, duration) => {
  if (!obj) return
  let startTimestamp = null
  const step = (timestamp) => {
    if (!startTimestamp) startTimestamp = timestamp
    const progress = Math.min((timestamp - startTimestamp) / duration, 1)
    obj.innerHTML = Math.floor(progress * (end - start) + start)
    if (progress < 1) {
      window.requestAnimationFrame(step)
    } else {
      obj.innerHTML = end
    }
  }
  window.requestAnimationFrame(step)
}

// 初始化图表
const initCharts = (breedData, datesData, rescueData, adoptionData) => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['救助数量', '领养申请数量'],
        bottom: '0%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: datesData || [],
        axisLine: { lineStyle: { color: '#909399' } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } }
      },
      series: [
        {
          name: '救助数量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: rescueData || [],
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64,158,255,0.3)' },
              { offset: 1, color: 'rgba(64,158,255,0.05)' }
            ])
          },
          lineStyle: {
            width: 3,
            color: '#409EFF'
          }
        },
        {
          name: '领养申请数量',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: adoptionData || [],
          itemStyle: {
            color: '#67C23A'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103,194,58,0.3)' },
              { offset: 1, color: 'rgba(103,194,58,0.05)' }
            ])
          },
          lineStyle: {
            width: 3,
            color: '#67C23A'
          }
        }
      ]
    }
    trendChart.setOption(option)
  }

  if (breedChartRef.value) {
    breedChart = echarts.init(breedChartRef.value)
    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        bottom: '0%',
        left: 'center',
        icon: 'circle'
      },
      series: [
        {
          name: '品种分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
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
              fontSize: 20,
              fontWeight: 'bold'
            },
            scale: true,
            scaleSize: 10
          },
          labelLine: {
            show: false
          },
          data: breedData || []
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
      
      // Ensure arrays are not null
      if (!dashboardData.dates) dashboardData.dates = []
      if (!dashboardData.dailyRescueData) dashboardData.dailyRescueData = []
      if (!dashboardData.dailyAdoptionData) dashboardData.dailyAdoptionData = []
      if (!dashboardData.breedDistribution) dashboardData.breedDistribution = []
      
      // Using JSON.parse(JSON.stringify()) to remove proxy for echarts
      const breedData = JSON.parse(JSON.stringify(dashboardData.breedDistribution))
      const datesData = JSON.parse(JSON.stringify(dashboardData.dates))
      const rescueData = JSON.parse(JSON.stringify(dashboardData.dailyRescueData))
      const adoptionData = JSON.parse(JSON.stringify(dashboardData.dailyAdoptionData))

      // 触发图表更新
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
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px); /* 减去头部高度，如果有的话 */
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  display: flex;
  flex-direction: column;
}

.sub-title {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  margin-top: 4px;
}

.current-date {
  font-size: 14px;
  color: #606266;
  background: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-card::after {
  content: '';
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.1;
  transition: all 0.3s ease;
}

.stat-card:hover::after {
  transform: scale(1.2);
}

.rescue-card .icon-wrapper { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
.rescue-card::after { background: #409EFF; }

.adoption-card .icon-wrapper { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
.adoption-card::after { background: #67C23A; }

.rate-card .icon-wrapper { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
.rate-card::after { background: #E6A23C; }

.audit-card .icon-wrapper { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
.audit-card::after { background: #F56C6C; }

.icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.chart-header {
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-body {
  height: 350px;
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
}
</style>
