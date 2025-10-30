<template>
  <div class="statistics">
    <div class="page-header">
      <h1>数据统计</h1>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :md="4" v-for="stat in statsCards" :key="stat.key">
          <div class="stat-card" :style="{ background: stat.color }">
            <div class="stat-icon">
              <el-icon :size="32">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 成果数量趋势图 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>成果数量趋势</h3>
            <div class="chart-controls">
              <el-select v-model="achievementTrendType" placeholder="选择成果类型" @change="fetchAchievementTrendData">
                <el-option label="全部类型" value=""></el-option>
                <el-option label="论文" value="PAPER"></el-option>
                <el-option label="专利" value="PATENT"></el-option>
                <el-option label="证书" value="CERTIFICATE"></el-option>
                <el-option label="项目" value="PROJECT"></el-option>
              </el-select>
              <el-select v-model="achievementTrendPeriod" placeholder="统计周期" @change="fetchAchievementTrendData">
                <el-option label="月度" value="month"></el-option>
                <el-option label="周度" value="week"></el-option>
                <el-option label="年度" value="year"></el-option>
              </el-select>
            </div>
            <div ref="achievementTrendChart" style="height: 300px;"></div>
          </div>
        </el-col>
        
        <!-- 成果分布图 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>成果分布图</h3>
            <div class="chart-controls">
              <el-select v-model="achievementDistributionDimension" placeholder="分布维度" @change="fetchAchievementDistributionData">
                <el-option label="按类型" value="type"></el-option>
                <el-option label="按学院" value="department"></el-option>
              </el-select>
            </div>
            <div ref="achievementDistributionChart" style="height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 活动报名率统计 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>活动报名率统计</h3>
            <div class="chart-controls">
              <el-radio-group v-model="activityTypeFilter" @change="fetchActivityParticipationData">
                <el-radio-button label="">全部类型</el-radio-button>
                <el-radio-button label="LECTURE">讲座</el-radio-button>
                <el-radio-button label="COMPETITION">赛事</el-radio-button>
                <el-radio-button label="TRAINING">培训</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="activityParticipationChart" style="height: 300px;"></div>
          </div>
        </el-col>
        
        <!-- 审核状态分布 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>审核状态分布</h3>
            <div ref="approvalStatusChart" style="height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 项目相关图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 项目状态分布 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>项目状态分布</h3>
            <div ref="projectStatusChart" style="height: 300px;"></div>
          </div>
        </el-col>
        
        <!-- 项目类型分布 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>项目类型分布</h3>
            <div ref="projectTypeChart" style="height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 活动相关图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 活动类型分布 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>活动类型分布</h3>
            <div ref="activityTypeChart" style="height: 300px;"></div>
          </div>
        </el-col>

        <!-- 项目趋势图 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>项目趋势</h3>
            <div ref="projectTrendChart" style="height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 趋势图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 活动趋势图 -->
        <el-col :span="12">
          <div class="chart-container">
            <h3>活动趋势</h3>
            <div ref="activityTrendChart" style="height: 300px;"></div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { 
  getSystemOverview, 
  getProjectsByStatus, 
  getProjectsByType, 
  getProjectTrend,
  getActivitiesByType,
  getActivityTrend,
  getTotalUsers,
  getTotalProjects,
  getTotalActivities,
  getAchievementTrend,
  getAchievementDistribution,
  getApprovalStatusDistribution,
  getActivityParticipation
} from '@/api/statistics'

export default {
  name: 'Statistics',
  /**
   * 组件设置函数
   * 初始化图表引用、统计卡片数据和数据获取函数
   */
  setup() {
    const achievementTrendChart = ref()
    const activityParticipationChart = ref()
    const achievementDistributionChart = ref()
    const approvalStatusChart = ref()
    const projectStatusChart = ref()
    const projectTypeChart = ref()
    const activityTypeChart = ref()
    const projectTrendChart = ref()
    const activityTrendChart = ref()

    // 筛选条件
    const achievementTrendType = ref('')
    const achievementTrendPeriod = ref('month')
    const achievementDistributionDimension = ref('type')
    const activityTypeFilter = ref('')
    
    /**
     * 统计卡片数据配置
     * 包含用户总数、学术活动、成果申报、科研项目、待审核、今日活跃等统计指标
     */
    const statsCards = ref([
      {
        key: 'totalUsers',
        label: '用户总数',
        value: 0,
        icon: 'UserFilled',
        color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      },
      {
        key: 'totalActivities',
        label: '学术活动',
        value: 0,
        icon: 'Calendar',
        color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
      },
      {
        key: 'totalAchievements',
        label: '成果申报',
        value: 0,
        icon: 'Trophy',
        color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      },
      {
        key: 'totalProjects',
        label: '科研项目',
        value: 0,
        icon: 'FolderOpened',
        color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
      },
      {
        key: 'pendingApprovals',
        label: '待审核',
        value: 0,
        icon: 'Clock',
        color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
      },
      {
        key: 'activeToday',
        label: '今日活跃',
        value: 0,
        icon: 'Star',
        color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
      }
    ])

    //加载状态
    const loading = ref(true)
    
    /**
     * 初始化成果数量趋势图表
     */
    const initAchievementTrendChart = (data) => {
      if(!achievementTrendChart.value) return

      const chart = echarts.init(achievementTrendChart.value)
      
      // 定义所有可能的成果类型
      const allAchievementTypes = ['PAPER', 'PATENT', 'CERTIFICATE', 'PROJECT']
      
      // 按类型分组数据
      const typeMap = {}
      data.forEach(item => {
        if (!typeMap[item.type]) {
          typeMap[item.type] = []
        }
        typeMap[item.type].push(item)
      })

      // 获取所有日期（去重并排序）
      const allDates = [...new Set(data.map(item => item.date))].sort()
      
      // 如果没有数据，添加一个默认日期
      if (allDates.length === 0) {
        const today = new Date()
        allDates.push(today.toISOString().split('T')[0])
      }

      // 构建系列数据
      const series = []
      const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
      let colorIndex = 0

      // 当选择全部类型时，显示所有可能的类型
      // 当选择特定类型时，只显示所选类型
      const typesToShow = achievementTrendType.value ? [achievementTrendType.value] : allAchievementTypes
      
      typesToShow.forEach(type => {
        const typeData = typeMap[type] || []
        const seriesData = allDates.map(date => {
          const item = typeData.find(d => d.date === date)
          return item ? item.count : 0
        })
        
        series.push({
          name: getAchievementTypeName(type),
          type: 'line',
          data: seriesData,
          smooth: true,
          itemStyle: {
            color: colors[colorIndex % colors.length]
          }
        })
        colorIndex++
      })

      const option = {
        title: {
          text: '成果数量趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: Object.keys(typeMap).map(type => getAchievementTypeName(type)),
          top: '10%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: allDates
        },
        yAxis: {
          type: 'value'
        },
        series: series
      }
      chart.setOption(option)

      // 响应式调整
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }

    /**
     * 获取成果类型中文名称
     */
    const getAchievementTypeName = (type) => {
      const typeMap = {
        'PAPER': '论文',
        'PATENT': '专利',
        'CERTIFICATE': '证书',
        'PROJECT': '项目'
      }
      return typeMap[type] || type
    }
    
    /**
     * 初始化活动报名率统计图表
     */
    const initActivityParticipationChart = (participationData) => {
      if (!activityParticipationChart.value || !participationData || participationData.length === 0) {
        return
      }

      const chart = echarts.init(activityParticipationChart.value)
      
      // 准备数据
      const activityNames = participationData.map(item => item.activityName)
      const participationRates = participationData.map(item => item.participationRate)
      const participantCounts = participationData.map(item => item.totalParticipants)
      const totalSlots = participationData.map(item => item.totalSlots)

      const option = {
        title: {
          text: '活动报名率统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          },
          formatter: function(params) {
            let tooltipHtml = params[0].name + '<br/>'
            params.forEach(item => {
              if (item.seriesName === '报名率') {
                tooltipHtml += item.marker + item.seriesName + ': ' + item.value + '%<br/>'
              } else {
                tooltipHtml += item.marker + item.seriesName + ': ' + item.value + '人<br/>'
              }
            })
            return tooltipHtml
          }
        },
        legend: {
          data: ['报名率', '实际参与人数', '总名额'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: activityNames,
          axisLabel: {
            interval: 0,
            rotate: 45,
            formatter: function(value) {
              return value.length > 10 ? value.substring(0, 10) + '...' : value
            }
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '人数',
            position: 'left'
          },
          {
            type: 'value',
            name: '报名率 (%)',
            position: 'right',
            max: 100
          }
        ],
        series: [
          {
            name: '实际参与人数',
            type: 'bar',
            data: participantCounts,
            color: '#5470c6'
          },
          {
            name: '总名额',
            type: 'bar',
            data: totalSlots,
            color: '#91cc75'
          },
          {
            name: '报名率',
            type: 'line',
            yAxisIndex: 1,
            data: participationRates,
            color: '#ee6666',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8
          }
        ]
      }

      chart.setOption(option)
      
      // 响应式调整
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
    
    /**
     * 初始化项目状态分布图表
     */
    const initProjectStatusChart = (data) => {
      if (!projectStatusChart.value) return
      
      const chart = echarts.init(projectStatusChart.value)
      let statusData = []
      
      if (data && data.statuses && data.counts) {
        statusData = data.statuses.map((status, index) => ({
          name: status,
          value: data.counts[index]
        }))
      }
      
      const option = {
        title: {
          text: '项目状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '项目状态',
          type: 'pie',
          radius: '50%',
          data: statusData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 初始化项目类型分布图表
     */
    const initProjectTypeChart = (data) => {
      if (!projectTypeChart.value) return
      
      const chart = echarts.init(projectTypeChart.value)
      let types = []
      let counts = []
      
      if (data && data.types && data.counts) {
        types = data.types
        counts = data.counts
      }
      
      const option = {
        title: {
          text: '项目类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: types
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '项目数量',
          type: 'bar',
          data: counts,
          itemStyle: {
            color: '#43e97b'
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 初始化活动类型分布图表
     */
    const initActivityTypeChart = (data) => {
      if (!activityTypeChart.value) return
      
      const chart = echarts.init(activityTypeChart.value)
      let types = []
      let counts = []
      
      if (data && data.types && data.counts) {
        types = data.types
        counts = data.counts
      }
      
      const option = {
        title: {
          text: '活动类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: types
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '活动数量',
          type: 'bar',
          data: counts,
          itemStyle: {
            color: '#f5576c'
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 初始化项目趋势图表
     */
    const initProjectTrendChart = (data) => {
      if (!projectTrendChart.value) return
      
      const chart = echarts.init(projectTrendChart.value)
      let dates = []
      let values = []
      
      if (data && Array.isArray(data)) {
        dates = data.map(item => item.date)
        values = data.map(item => item.count)
      }
      
      const option = {
        title: {
          text: '项目趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '项目数量',
          type: 'line',
          data: values,
          smooth: true,
          itemStyle: {
            color: '#38f9d7'
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 初始化活动趋势图表
     */
    const initActivityTrendChart = (data) => {
      if (!activityTrendChart.value) return
      
      const chart = echarts.init(activityTrendChart.value)
      let dates = []
      let values = []
      
      if (data && Array.isArray(data)) {
        dates = data.map(item => item.date)
        values = data.map(item => item.count)
      }
      
      const option = {
        title: {
          text: '活动趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: dates
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '活动数量',
          type: 'line',
          data: values,
          smooth: true,
          itemStyle: {
            color: '#fed6e3'
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 初始化成果分布图
     */
    const initAchievementDistributionChart = (data) => {
      if (!achievementDistributionChart.value) return

      const chart = echarts.init(achievementDistributionChart.value)
      
      // 从数据中提取分类和数量，并转换为中文
      const categories = data.map(item => getAchievementTypeName(item.category) || item.category)
      const counts = data.map(item => item.count)
      const percentages = data.map(item => item.percentage)

      const option = {
        title: {
          text: '成果分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const param = params[0]
            const index = data.findIndex(item => getAchievementTypeName(item.category) === param.name)
            return `${param.name}: ${param.value} (${percentages[index] || 0}%)`
          }
        },
        xAxis: {
          type: 'category',
          data: categories,
          axisLabel: {
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          type: 'value',
          name: '数量'
        },
        series: [{
          name: '成果数量',
          type: 'bar',
          data: counts,
          itemStyle: {
            color: function(params) {
              const colorList = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
              return colorList[params.dataIndex % colorList.length]
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}'
          }
        }]
      }
      chart.setOption(option)

      // 响应式调整
      window.addEventListener('resize', () => {
        chart.resize()
      })
    }
    
    /**
     * 初始化审核状态分布图表
     */
    const initApprovalStatusChart = (data) => {
      if (!approvalStatusChart.value) return

      const chart = echarts.init(approvalStatusChart.value)

      // 审核状态映射，将英文状态转换为中文
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '审核通过',
        'REJECTED': '审核拒绝',
        'PENDING_FINAL': '初审通过'
      }

      const statusData = data.map(item => ({
        value: item.count,
        name: statusMap[item.status] || item.status
      }))

      const option = {
        title: {
          text: '审核状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '审核状态',
          type: 'pie',
          radius: '50%',
          data: statusData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      chart.setOption(option)
    }
    
    /**
     * 获取系统概览数据并更新统计卡片
     */
    const fetchSystemOverview = async () => {
      try {
        const res = await getSystemOverview()
        // 从res.data中提取数据，而不是直接使用res
        const overviewData = res?.data || {}
        if (overviewData) {
          statsCards.value.forEach(card => {
            if (overviewData[card.key] !== undefined) {
              card.value = overviewData[card.key]
            }
          })
        }
      } catch (error) {
        console.error('获取系统概览数据失败:', error)
      }
    }
    
    /**
     * 获取项目相关数据并更新图表
     */
    const fetchProjectData = async () => {
      try {
        // 获取项目状态分布
        const statusRes = await getProjectsByStatus()
        console.log('项目状态分布:', statusRes?.data || null)
        initProjectStatusChart(statusRes?.data || null)
        
        // 获取项目类型分布
        const typeRes = await getProjectsByType()
        initProjectTypeChart(typeRes?.data || null)
        
        // 获取项目趋势数据 - 使用最近6个月的动态日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const trendRes = await getProjectTrend(
          formatDate(startDate), 
          formatDate(endDate), 
          'month'
        )
        console.log('项目趋势数据:', trendRes?.data || null)
        initProjectTrendChart(trendRes?.data || null)
      } catch (error) {
        console.error('获取项目数据失败:', error)
        // 添加默认数据以确保图表正常显示
        const defaultData = [
          { date: '2025-01', count: 2 },
          { date: '2025-02', count: 3 },
          { date: '2025-03', count: 5 },
          { date: '2025-04', count: 4 },
          { date: '2025-05', count: 6 },
          { date: '2025-06', count: 7 }
        ]
        initProjectTrendChart(defaultData)
      }
    }
    
    /**
     * 获取活动相关数据并更新图表
     */
    const fetchActivityData = async () => {
      try {
        // 获取活动类型分布
        const typeRes = await getActivitiesByType()
        initActivityTypeChart(typeRes?.data || null)
        
        // 获取活动趋势数据 - 使用fetchActivityTrendData函数以保持代码一致性
        await fetchActivityTrendData()
      } catch (error) {
        console.error('获取活动数据失败:', error)
      }
    }

    /**
     * 获取所有统计数据
     */
    const fetchAllStatistics = async () => {
      loading.value = true
      try {
        await Promise.all([
          fetchSystemOverview(),
          fetchProjectData(),
          fetchActivityData(),
          fetchAchievementTrend(),
          fetchAchievementDistributionData(),
          fetchApprovalStatusData(),
          fetchActivityParticipationData()
        ])
      } catch (error) {
        console.error('获取统计数据失败:', error)
      } finally {
        loading.value = false
      }
    }

    /**
     * 获取成果趋势数据并更新图表
     * 默认获取最近6个月的所有类型成果趋势
     */
    const fetchAchievementTrend = async () => {
      try {
        // 使用最近6个月作为查询条件
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getAchievementTrend(formatDate(startDate), formatDate(endDate), null, 'month')
        const trendData = res?.data || []
        initAchievementTrendChart(trendData)
      } catch (error) {
        console.error('获取成果趋势数据失败:', error)
      }
    }
    
    /**
     * 获取成果趋势数据
     */
    const fetchAchievementTrendData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0] 
        }
        
        const res = await getAchievementTrend(
          formatDate(startDate), 
          formatDate(endDate), 
          achievementTrendType.value, 
          achievementTrendPeriod.value
        )
        
        const trendData = res?.data || []
        initAchievementTrendChart(trendData)
      } catch (error) {
        console.error('获取成果趋势数据失败:', error)
      }
    }

    /**
     * 获取成果分布数据
     */
    const fetchAchievementDistributionData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getAchievementDistribution(
          formatDate(startDate), 
          formatDate(endDate), 
          achievementDistributionDimension.value
        )
        
        const distributionData = res?.data || {}
        const dimensionData = distributionData[achievementDistributionDimension.value] || []
        initAchievementDistributionChart(dimensionData)
      } catch (error) {
        console.error('获取成果分布数据失败:', error)
      }
    }

    /**
     * 获取审核状态分布数据
     */
    const fetchApprovalStatusData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getApprovalStatusDistribution(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const statusData = res?.data || []
        initApprovalStatusChart(statusData)
      } catch (error) {
        console.error('获取审核状态分布数据失败:', error)
      }
    }

    /**
     * 获取活动报名率数据
     */
    const fetchActivityParticipationData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getActivityParticipation(
          formatDate(startDate), 
          formatDate(endDate), 
          activityTypeFilter.value || null
        )
        
        const participationData = res?.data || []
        initActivityParticipationChart(participationData)
      } catch (error) {
        console.error('获取活动报名率数据失败:', error)
      }
    }

    /**
     * 获取项目状态分布数据
     */
    const fetchProjectStatusData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getProjectStatusDistribution(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const statusData = res?.data || []
        initProjectStatusChart(statusData)
      } catch (error) {
        console.error('获取项目状态分布数据失败:', error)
      }
    }

    /**
     * 获取项目类型分布数据
     */
    const fetchProjectTypeData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getProjectTypeDistribution(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const typeData = res?.data || []
        initProjectTypeChart(typeData)
      } catch (error) {
        console.error('获取项目类型分布数据失败:', error)
      }
    }

    /**
     * 获取活动类型分布数据
     */
    const fetchActivityTypeData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getActivityTypeDistribution(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const typeData = res?.data || []
        initActivityTypeChart(typeData)
      } catch (error) {
        console.error('获取活动类型分布数据失败:', error)
      }
    }

    /**
     * 获取项目趋势数据
     */
    const fetchProjectTrendData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getProjectTrend(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const trendData = res?.data || []
        initProjectTrendChart(trendData)
      } catch (error) {
        console.error('获取项目趋势数据失败:', error)
      }
    }

    /**
     * 获取活动趋势数据
     */
    const fetchActivityTrendData = async () => {
      try {
        // 计算最近6个月的日期范围
        const endDate = new Date()
        const startDate = new Date()
        startDate.setMonth(startDate.getMonth() - 6)
        
        const formatDate = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        const res = await getActivityTrend(
          formatDate(startDate), 
          formatDate(endDate), 
          null
        )
        
        const trendData = res?.data || []
        initActivityTrendChart(trendData)
      } catch (error) {
        console.error('获取活动趋势数据失败:', error)
      }
    }
    
    /**
     * 组件挂载后执行
     * 初始化所有图表并异步获取数据
     * 使用setTimeout确保DOM元素已渲染完成
     */
    onMounted(() => {
      // 初始化所有图表
      setTimeout(() => {
        fetchAllStatistics()
      }, 100)
    })
    
    return {
      achievementTrendChart,
      activityParticipationChart,
      achievementDistributionChart,
      approvalStatusChart,
      projectStatusChart,
      projectTypeChart,
      activityTypeChart,
      projectTrendChart,
      activityTrendChart,
      statsCards,
      loading,
      achievementTrendType,
      achievementTrendPeriod,
      achievementDistributionDimension,
      activityTypeFilter,
      fetchAchievementTrendData,
      fetchAchievementDistributionData,
      fetchApprovalStatusData,
      fetchActivityParticipationData
    }
  }
}
</script>

<style scoped>
.statistics {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 12px;
  padding: 20px;
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.charts-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.chart-container {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.chart-container h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.chart-controls {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-controls .el-select {
  width: 260px;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 15px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .charts-section {
    padding: 15px;
  }
  
  .chart-container {
    padding: 15px;
  }
}
</style>
