<template>
  <div class="stats-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">数据看板</h1>
        <p class="page-description">会议室使用情况概览与分析</p>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="default"
          @change="loadData"
        />
      </div>
    </div>
    
    <!-- 概览指标 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon blue">
          <el-icon :size="20"><Calendar /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ overviewData.totalBookings }}</div>
          <div class="metric-label">总预约数</div>
        </div>
      </div>
      
      <div class="metric-card">
        <div class="metric-icon green">
          <el-icon :size="20"><OfficeBuilding /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ overviewData.totalRooms }}</div>
          <div class="metric-label">会议室总数</div>
        </div>
      </div>
      
      <div class="metric-card">
        <div class="metric-icon orange">
          <el-icon :size="20"><TrendCharts /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ overviewData.avgUsageRate }}%</div>
          <div class="metric-label">平均使用率</div>
        </div>
      </div>
      
      <div class="metric-card">
        <div class="metric-icon purple">
          <el-icon :size="20"><User /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ overviewData.activeUsers }}</div>
          <div class="metric-label">活跃用户</div>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card large">
        <div class="card-header">
          <h3 class="card-title">预约趋势</h3>
        </div>
        <div class="card-body">
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </div>
      
      <div class="chart-card">
        <div class="card-header">
          <h3 class="card-title">热门会议室 Top5</h3>
        </div>
        <div class="card-body">
          <div ref="hotChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>
    
    <!-- 使用率详情 -->
    <div class="table-card">
      <div class="card-header">
        <h3 class="card-title">会议室使用率详情</h3>
      </div>
      <div class="card-body">
        <el-table :data="usageData" v-loading="loading" stripe>
          <el-table-column prop="roomName" label="会议室" min-width="120" />
          <el-table-column prop="bookingCount" label="预约次数" width="100" />
          <el-table-column prop="totalDuration" label="使用时长(分钟)" width="130" />
          <el-table-column prop="avgDuration" label="平均时长(分钟)" width="130" />
          <el-table-column label="使用率" width="200">
            <template #default="{ row }">
              <div class="usage-bar">
                <div class="usage-progress" :style="{ width: `${row.usageRate || 0}%` }"></div>
                <span class="usage-text">{{ row.usageRate || 0 }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStatsOverview, getUsageStats, getHotRooms, getBookingTrend } from '@/api'
import type { StatsInfo } from '@/types'

const loading = ref(false)
const trendChartRef = ref<HTMLElement>()
const hotChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let hotChart: echarts.ECharts | null = null

const dateRange = ref<[string, string] | null>(null)

const overviewData = reactive({
  totalBookings: 0,
  totalRooms: 0,
  avgUsageRate: 0,
  activeUsers: 0
})

const usageData = ref<StatsInfo[]>([])
const hotData = ref<StatsInfo[]>([])

const buildParams = () => dateRange.value
  ? { startDate: dateRange.value[0], endDate: dateRange.value[1] }
  : undefined

const loadData = async () => {
  await Promise.all([loadOverview(), loadUsageData(), loadHotData(), loadTrendData()])
}

const loadOverview = async () => {
  const res = await getStatsOverview(buildParams())
  overviewData.totalRooms = res.data.totalRooms ?? 0
  overviewData.totalBookings = res.data.totalBookings ?? 0
  overviewData.activeUsers = res.data.activeUsers ?? 0
  overviewData.avgUsageRate = res.data.avgUsageRate ?? 0
}

const loadUsageData = async () => {
  loading.value = true
  try {
    const res = await getUsageStats(buildParams())
    usageData.value = res.data
  } finally {
    loading.value = false
  }
}

const loadHotData = async () => {
  try {
    const res = await getHotRooms(buildParams())
    hotData.value = res.data

    await nextTick()
    renderHotChart()
  } catch (error) {
    // ignore
  }
}

const loadTrendData = async () => {
  try {
    const res = await getBookingTrend(buildParams())

    await nextTick()
    renderTrendChart(res.data)
  } catch (error) {
    // ignore
  }
}

const renderTrendChart = (data?: any) => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  const dates = data?.dates || []
  const values = data?.values || []
  
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E8E5E0',
      textStyle: {
        color: '#2D3748'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data?.dates || dates,
      axisLine: {
        lineStyle: {
          color: '#E8E5E0'
        }
      },
      axisLabel: {
        color: '#718096'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#E8E5E0'
        }
      },
      axisLabel: {
        color: '#718096'
      },
      splitLine: {
        lineStyle: {
          color: '#F0EDE8'
        }
      }
    },
    series: [{
      name: '预约数',
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: {
        color: '#5B8FB9'
      },
      lineStyle: {
        width: 3
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(91, 143, 185, 0.2)' },
          { offset: 1, color: 'rgba(91, 143, 185, 0.02)' }
        ])
      }
    }]
  }
  
  trendChart.setOption(option)
}

const renderHotChart = () => {
  if (!hotChartRef.value) return
  
  if (!hotChart) {
    hotChart = echarts.init(hotChartRef.value)
  }
  
  const names = hotData.value.map(item => item.roomName || '未知')
  const values = hotData.value.map(item => item.bookingCount || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E8E5E0',
      textStyle: {
        color: '#2D3748'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#E8E5E0'
        }
      },
      axisLabel: {
        color: '#718096'
      },
      splitLine: {
        lineStyle: {
          color: '#F0EDE8'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: names.reverse(),
      axisLine: {
        lineStyle: {
          color: '#E8E5E0'
        }
      },
      axisLabel: {
        color: '#718096'
      }
    },
    series: [{
      name: '预约次数',
      type: 'bar',
      data: values.reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#5B8FB9' },
          { offset: 1, color: '#7C9A72' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: '60%'
    }]
  }
  
  hotChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  hotChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  hotChart?.dispose()
})
</script>

<style lang="scss" scoped>
.stats-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  
  .header-content {
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #2D3748;
      margin-bottom: 4px;
    }
    
    .page-description {
      font-size: 14px;
      color: #718096;
    }
  }
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.metric-card {
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: box-shadow 0.2s ease;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.blue {
      background: #EBF5FF;
      color: #5B8FB9;
    }
    
    &.green {
      background: #F0F9EB;
      color: #7C9A72;
    }
    
    &.orange {
      background: #FDF6EC;
      color: #D4845A;
    }
    
    &.purple {
      background: #F3E8FF;
      color: #9F7AEA;
    }
  }
  
  .metric-content {
    .metric-value {
      font-size: 28px;
      font-weight: 700;
      color: #2D3748;
      line-height: 1.2;
    }
    
    .metric-label {
      font-size: 13px;
      color: #718096;
      margin-top: 2px;
    }
  }
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
  border-radius: 12px;
  overflow: hidden;
  
  &.large {
    grid-column: span 1;
  }
  
  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #F0EDE8;
    
    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #2D3748;
    }
  }
  
  .card-body {
    padding: 20px;
    
    .chart-container {
      height: 300px;
    }
  }
}

.table-card {
  background: #FFFFFF;
  border: 1px solid #F0EDE8;
  border-radius: 12px;
  overflow: hidden;
  
  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #F0EDE8;
    
    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #2D3748;
    }
  }
  
  .card-body {
    padding: 20px;
  }
}

.usage-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .usage-progress {
    flex: 1;
    height: 6px;
    background: #F0EDE8;
    border-radius: 3px;
    overflow: hidden;
    
    &::after {
      content: '';
      display: block;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, #5B8FB9, #7C9A72);
      border-radius: 3px;
    }
  }
  
  .usage-text {
    font-size: 13px;
    font-weight: 500;
    color: #4A5568;
    min-width: 40px;
  }
}

// 响应式调整
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
