<template>
  <div class="home-container">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h1>欢迎使用学术活动与科研成果管理系统</h1>
      <p>Welcome to Academic Activity & Research Achievement Management System</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.key">
          <div class="stat-card" :style="{ background: stat.color }">
            <div class="stat-icon">
              <el-icon :size="32">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">
                <template v-if="loading">
                  <el-skeleton :rows="1" animated />
                </template>
                <template v-else>
                  {{ stat.value }}
                </template>
              </div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷操作</h2>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="8" :md="6" v-for="action in quickActions" :key="action.key">
          <el-card 
            class="action-card" 
            :body-style="{ padding: '20px' }"
            @click="handleAction(action)"
          >
            <div class="action-content">
              <el-icon :size="24" class="action-icon">
                <component :is="action.icon" />
              </el-icon>
              <div class="action-title">{{ action.title }}</div>
              <div class="action-desc">{{ action.description }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 最近活动 -->
    <div class="recent-section">
      <h2>最近活动</h2>
      <el-card>
        <el-table :data="recentActivities" style="width: 100%">
          <el-table-column prop="title" label="活动标题" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="scope">
              <el-tag :type="getActivityTypeTag(scope.row.type)">
                {{ getActivityTypeName(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ getStatusName(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { getSystemOverview, getTotalParticipants, getTotalUsers } from '@/api/statistics'
import { getAchievementList } from '@/api/achievement'
import { getActivityList } from '@/api/activity'
import { getAllProjects } from '@/api/project'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    
    const userRole = computed(() => store.getters.userRole)
    
    // 定义所有可能的统计卡片
    const allStats = [
      {
        key: 'activities',
        label: '学术活动',
        value: 0,
        icon: 'Calendar',
        color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      },
      {
        key: 'achievements',
        label: '成果申报',
        value: 0,
        icon: 'Trophy',
        color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
      },
      {
        key: 'projects',
        label: '科研项目',
        value: 0,
        icon: 'FolderOpened',
        color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      },
      {
        key: 'users',
        label: '用户总数',
        value: 0,
        icon: 'UserFilled',
        color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
      }
    ]
    
    // 根据用户角色决定显示的统计卡片
    const stats = computed(() => {
      // 如果是学生用户，只显示学术活动和成果申报
      if (userRole.value === 'STUDENT') {
        return allStats.filter(stat => 
          stat.key === 'activities' || stat.key === 'achievements'
        )
      }
      // 其他角色显示所有统计卡片
      return allStats
    })
    
    const quickActions = computed(() => {
      const actions = []
      
      if (userRole.value === 'STUDENT') {
        actions.push(
          {
            key: 'join-activity',
            title: '参与活动',
            description: '查看并报名学术活动',
            icon: 'Calendar',
            route: '/dashboard/activities'
          },
          {
            key: 'submit-achievement',
            title: '申报成果',
            description: '提交科研成果',
            icon: 'Trophy',
            route: '/dashboard/achievements/create'
          }
        )
      }
      
      if (userRole.value === 'TEACHER' || userRole.value === 'ADMIN') {
        actions.push(
          {
            key: 'create-activity',
            title: '发布活动',
            description: '创建新的学术活动',
            icon: 'Calendar',
            route: '/dashboard/activities/create'
          },
          {
            key: 'manage-projects',
            title: '项目管理',
            description: '管理科研项目',
            icon: 'FolderOpened',
            route: '/dashboard/projects'
          }
        )
      }
      
      if (userRole.value === 'ADMIN') {
        actions.push(
          {
            key: 'view-statistics',
            title: '数据统计',
            description: '查看系统统计数据',
            icon: 'DataAnalysis',
            route: '/dashboard/statistics'
          },
          {
            key: 'manage-users',
            title: '用户管理',
            description: '管理系统用户',
            icon: 'UserFilled',
            route: '/dashboard/users'
          }
        )
      }
      
      return actions
    })
    
    const recentActivities = ref([])
    
    const handleAction = (action) => {
      router.push(action.route)
    }
    
    const getActivityTypeName = (type) => {
      const typeMap = {
        'LECTURE': '讲座',
        'COMPETITION': '赛事',
        'TRAINING': '培训'
      }
      return typeMap[type] || type
    }
    
    const getActivityTypeTag = (type) => {
      const tagMap = {
        'LECTURE': 'primary',
        'COMPETITION': 'success',
        'TRAINING': 'warning'
      }
      return tagMap[type] || 'info'
    }
    
    const getStatusName = (status) => {
      const statusMap = {
        'DRAFT': '草稿',
        'PUBLISHED': '已发布',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成'
      }
      return statusMap[status] || status
    }
    
    const getStatusTag = (status) => {
      const tagMap = {
        'DRAFT': 'info',
        'PUBLISHED': 'success',
        'CANCELLED': 'danger',
        'COMPLETED': 'warning'
      }
      return tagMap[status] || 'info'
    }
    
    const loadDashboardData = async () => {
      loading.value = true
      try {
        console.log('开始加载首页统计数据')
        
        // 分别获取各类统计数据
        // 1. 获取学术活动总数
        try {
          const activitiesResponse = await getActivityList()
          console.log('获取学术活动数据:', activitiesResponse)
          const activityStat = stats.value.find(stat => stat.key === 'activities')
          if (activityStat) {
            // 从响应对象的data字段中提取实际数值
            activityStat.value = activitiesResponse?.data?.length || activitiesResponse?.length || 0
          }
        } catch (error) {
          console.error('获取学术活动总数失败:', error)
        }
        
        // 2. 获取成果申报总数
        try {
          const achievementsResponse = await getAchievementList({ pageSize: 1 })
          console.log('获取成果申报数据:', achievementsResponse)
          const achievementStat = stats.value.find(stat => stat.key === 'achievements')
          if (achievementStat) {
            achievementStat.value = achievementsResponse?.data?.length || achievementsResponse?.length || 0
          }
        } catch (error) {
          console.error('获取成果申报总数失败:', error)
        }
        
        // 3. 获取科研项目总数
        try {
          const projectsResponse = await getAllProjects()
          console.log('获取科研项目总数:', projectsResponse)
          const projectStat = stats.value.find(stat => stat.key === 'projects')
          if (projectStat) {
            // 从响应对象的data字段中提取实际数值
            projectStat.value = projectsResponse?.data?.length || projectsResponse?.length || 0
          }
        } catch (error) {
          console.error('获取科研项目总数失败:', error)
        }
        
        // 4. 获取用户总数
        try {
          const usersCount = await getTotalUsers()
          console.log('获取用户总数:', usersCount)
          const userStat = stats.value.find(stat => stat.key === 'users')
          if (userStat) {
            userStat.value = typeof usersCount === 'number' ? usersCount : 
                               (usersCount?.data || usersCount?.data?.total || usersCount?.total || usersCount?.count || 0)
          }
        } catch (error) {
          console.error('获取用户总数失败:', error)
        }
        
        // 获取最近活动
        try {
          const activitiesResponse = await getActivityList({
            page: 1,
            pageSize: 5,
            sortBy: 'startTime',
            order: 'desc'
          })
          console.log('获取最近活动:', activitiesResponse)
          if (activitiesResponse) {
            recentActivities.value = activitiesResponse?.data || activitiesResponse?.data?.list || activitiesResponse?.list || []
          }
        } catch (error) {
          console.error('获取最近活动失败:', error)
        }
        
        console.log('首页统计数据加载完成:', stats.value)
      } catch (error) {
        console.error('加载首页数据失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    onMounted(() => {
      loadDashboardData()
    })
    
    return {
      stats,
      quickActions,
      recentActivities,
      loading,
      handleAction,
      getActivityTypeName,
      getActivityTypeTag,
      getStatusName,
      getStatusTag
    }
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.welcome-section h1 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: 600;
}

.welcome-section p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.stats-section {
  margin-bottom: 40px;
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

.quick-actions {
  margin-bottom: 40px;
}

.quick-actions h2 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 20px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-content {
  text-align: center;
}

.action-icon {
  color: #409eff;
  margin-bottom: 10px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.action-desc {
  font-size: 12px;
  color: #7f8c8d;
}

.recent-section h2 {
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 20px;
}

@media (max-width: 768px) {
  .welcome-section h1 {
    font-size: 24px;
  }
  
  .welcome-section p {
    font-size: 14px;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-number {
    font-size: 20px;
  }
}
</style>
