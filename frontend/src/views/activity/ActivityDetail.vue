<template>
  <div class="activity-detail">
    <div class="page-header">
      <h2>活动详情</h2>
      <el-button type="primary" @click="handleBack">返回列表</el-button>
    </div>
    
    <div class="detail-card" v-if="!loading">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>{{ activity.title }}</h3>
            <div class="status-tag">
              <el-tag :type="getStatusTag(activity.status)">{{ getStatusName(activity.status) }}</el-tag>
            </div>
          </div>
        </template>
        
        <div class="detail-content">
          <div class="info-row">
            <span class="label">活动类型：</span>
            <el-tag :type="getTypeTag(activity.type)">{{ getTypeName(activity.type) }}</el-tag>
          </div>
          
          <div class="info-row">
            <span class="label">主办方：</span>
            <span class="value">{{ activity.organizer }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">活动地点：</span>
            <span class="value">{{ activity.location }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">开始时间：</span>
            <span class="value">{{ formatDateTime(activity.startTime) }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">结束时间：</span>
            <span class="value">{{ formatDateTime(activity.endTime) }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">报名截止时间：</span>
            <span class="value">{{ formatDateTime(activity.registrationDeadline) }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">参与人数：</span>
            <span class="value">{{ activity.currentParticipants }} / {{ activity.maxParticipants }}</span>
          </div>
          
          <!-- <div class="info-row">
            <span class="label">创建者ID：</span>
            <span class="value">{{ activity.creatorId }}</span>
          </div> -->
          
          <div class="info-row description">
            <span class="label">活动描述：</span>
            <div class="value">{{ activity.description }}</div>
          </div>
        </div>
        
        <div class="card-footer">
            <!-- 只有学生角色才显示报名相关按钮 -->
            <!-- 学生已报名且报名截止时间未到，显示取消报名按钮 -->
            <el-button v-if="userRole === 'STUDENT' && hasRegistered && activity && activity.registrationDeadline && new Date(activity.registrationDeadline) > new Date() && activity.status !== 'COMPLETED'" type="info" @click="handleCancelJoin" :loading="joinLoading">取消报名</el-button>
            <!-- 学生未报名且满足报名条件，显示立即报名按钮 -->
            <el-button v-else-if="userRole === 'STUDENT' && canJoin(activity)" type="primary" @click="handleJoin" :loading="joinLoading">立即报名</el-button>
            <!-- 学生已报名且活动已完成，显示生成参与证明按钮 -->
            <el-button v-if="userRole === 'STUDENT' && hasRegistered && activity && activity.status === 'COMPLETED'" type="success" @click="handleGenerateCertificate" :loading="joinLoading">生成参赛证明</el-button>
            <!-- 活动已完成，显示下载参赛人员名单按钮 -->
              <template v-if="userRole !== 'STUDENT' && activity && activity.status === 'COMPLETED'">
                <el-button type="success" @click="handleDownloadParticipants('excel')">下载参赛名单(Excel)</el-button>
                <el-button type="primary" @click="handleDownloadParticipants('pdf')">下载参赛名单(PDF)</el-button>
              </template>
          </div>
      </el-card>
    </div>
    
    <div v-else class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityById, joinActivity, cancelJoinActivity, generateCertificate, downloadParticipants } from '@/api/activity'

export default {
  name: 'ActivityDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const loading = ref(true)
    const joinLoading = ref(false)
    const activity = ref({})
    const hasRegistered = ref(false)
    
    const userRole = computed(() => store.getters.userRole)
    const userId = computed(() => store.getters.userId || 3) // 直接从store获取userId，避免硬编码，保留默认值作为后备
    
    const hasPermission = (roles) => {
      return roles.includes(userRole.value)
    }
    
    const getTypeName = (type) => {
      const typeMap = {
        'LECTURE': '讲座',
        'COMPETITION': '赛事',
        'TRAINING': '培训'
      }
      return typeMap[type] || type
    }
    
    const getTypeTag = (type) => {
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
    
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-'
      // 处理日期时间格式，确保正确显示
      const date = new Date(dateTime)
      if (isNaN(date.getTime())) return dateTime
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    // 判断当前用户是否已报名活动
    const isUserRegistered = () => {
      // 添加详细调试信息
      console.log('判断报名状态 - 活动ID:', activity.value.id, '用户ID:', userId.value)
      
      // 确保活动对象存在
      if (!activity.value || !activity.value.id) {
        console.log('活动对象不存在或ID无效')
        return false
      }
      
      // 优先使用后端直接返回的报名状态标志
      if (activity.value.hasRegistered !== undefined) {
        console.log('使用后端返回的hasRegistered字段:', activity.value.hasRegistered)
        return activity.value.hasRegistered
      }
      
      // 1. 检查是否有registeredUsers数组包含当前用户ID
      if (activity.value.registeredUsers && Array.isArray(activity.value.registeredUsers)) {
        const result = activity.value.registeredUsers.includes(userId.value)
        console.log('通过registeredUsers检查结果:', result)
        return result
      }
      
      // 2. 检查是否有registrations数组，包含当前用户的报名记录
      if (activity.value.registrations && Array.isArray(activity.value.registrations)) {
        const result = activity.value.registrations.some(reg => reg.userId === userId.value)
        console.log('通过registrations检查结果:', result)
        return result
      }
      
      // 默认返回false，确保未报名用户不显示已报名状态
      return false
    }
    
    const canJoin = (activity) => {
      // 使用hasRegistered状态变量，而不是直接调用函数，确保UI一致性
      return userRole.value === 'STUDENT' && 
             activity.status === 'PUBLISHED' && 
             new Date(activity.registrationDeadline) > new Date() &&
             !hasRegistered.value
    }
    
    const loadActivityData = async () => {
      loading.value = true
      try {
        const activityId = route.params.id
        console.log('加载活动详情 - 活动ID:', activityId)
        console.log('当前登录用户ID:', userId.value, '角色:', userRole.value)
        
        const response = await getActivityById(activityId)
        
        if (response.code === 200) {
          activity.value = response.data || {}
          // 添加详细日志以便调试
          console.log('活动数据:', activity.value)
          console.log('用户角色:', userRole.value)
          console.log('用户ID:', userId.value)
          console.log('活动ID:', activity.value.id)
          
          // 明确计算并记录报名状态
          const registeredStatus = isUserRegistered()
          console.log('用户报名状态:', registeredStatus)
          
          // 确保hasRegistered的值与计算结果一致
          hasRegistered.value = registeredStatus
          
          console.log('报名截止时间:', activity.value.registrationDeadline)
          console.log('是否已过截止时间:', new Date(activity.value.registrationDeadline) <= new Date())
          console.log('canJoin返回结果:', canJoin(activity.value))
        } else {
          ElMessage.error(response.message || '加载活动详情失败')
          // 如果加载失败，跳转回列表页
          router.push('/dashboard/activities')
        }
      } catch (error) {
        console.error('加载活动详情失败:', error)
        ElMessage.error('加载活动详情失败')
        router.push('/dashboard/activities')
      } finally {
        loading.value = false
      }
    }
    
    const handleBack = () => {
      router.push('/dashboard/activities')
    }
    
    const handleJoin = async () => {
      joinLoading.value = true
      try {
        // 先进行基本验证
        if (!activity.value.id) {
          ElMessage.error('活动信息错误，无法报名')
          return
        }
        
        // 检查报名截止时间
        if (new Date(activity.value.registrationDeadline) <= new Date()) {
          ElMessage.warning('报名已截止')
          return
        }
        
        // 检查是否已达到最大参与人数
        if (activity.value.currentParticipants >= activity.value.maxParticipants) {
          ElMessage.warning('活动人数已满')
          return
        }
        
        // 再次确认用户未报名，防止重复报名
        if (hasRegistered.value) {
          ElMessage.warning('您已报名该活动')
          return
        }
        
        const response = await joinActivity(activity.value.id)
        
        if (response.code === 200 || response.success) {
          ElMessage.success(response.message || '报名成功')
          // 立即更新本地状态，提升用户体验
          hasRegistered.value = true
          // 重新加载活动数据以更新参与人数和其他信息
          setTimeout(() => {
            loadActivityData()
          }, 500)
        } else {
          ElMessage.error(response.message || '报名失败')
        }
      } catch (error) {
        console.error('报名失败:', error)
        // 从错误响应中提取消息
        const errorMsg = error.response?.data?.message || error.message || '报名失败，请稍后重试'
        ElMessage.error(errorMsg)
      } finally {
        joinLoading.value = false
      }
    }
    
    const handleCancelJoin = async () => {
      joinLoading.value = true
      try {
        // 先进行基本验证
        if (!activity.value.id) {
          ElMessage.error('活动信息错误，无法取消报名')
          return
        }
        
        // 再次确认用户已报名
        if (!hasRegistered.value) {
          ElMessage.warning('您尚未报名该活动')
          return
        }
        
        const response = await cancelJoinActivity(activity.value.id)
        
        if (response.code === 200 || response.success) {
          ElMessage.success(response.message || '取消报名成功')
          // 立即更新本地状态，提升用户体验
          hasRegistered.value = false
          // 重新加载活动数据以更新参与人数和其他信息
          setTimeout(() => {
            loadActivityData()
          }, 500)
        } else {
          ElMessage.error(response.message || '取消报名失败')
        }
      } catch (error) {
        console.error('取消报名失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '取消报名失败，请稍后重试'
        ElMessage.error(errorMsg)
      } finally {
        joinLoading.value = false
      }
    }
    
    const handleGenerateCertificate = async () => {
      joinLoading.value = true
      try {
        // 先进行基本验证
        if (!activity.value.id) {
          ElMessage.error('活动信息错误，无法生成参与证明')
          return
        }
        
        // 检查用户是否已报名
        if (!hasRegistered.value) {
          ElMessage.warning('您尚未报名该活动，无法生成参与证明')
          return
        }
        
        // 检查活动是否已完成
        if (activity.value.status !== 'COMPLETED') {
          ElMessage.warning('活动尚未完成，无法生成参与证明')
          return
        }
        
        // 调用生成证明接口
        const response = await generateCertificate(activity.value.id)
        
        if (response.code === 200 || response.success) {
          // 跳转到证书查看页面
          router.push(`/dashboard/activities/${activity.value.id}/certificate`)
        } else {
          ElMessage.error(response.message || '参与证明生成失败')
        }
      } catch (error) {
        console.error('生成参与证明失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '生成参与证明失败，请稍后重试'
        ElMessage.error(errorMsg)
      } finally {
        joinLoading.value = false
      }
    }
    
    const handleDownloadParticipants = async (type) => {
      try {
        // 调用API下载参与名单
        const response = await downloadParticipants(activity.value.id, type)
        
        // 确保获取正确的响应数据
        const blobData = response.data || response
        
        // 创建blob对象并下载
        const blob = new Blob([blobData], {
          type: type === 'excel' ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'application/pdf'
        })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `活动参与者名单_${activity.value.id}_${new Date().getTime()}.${type === 'excel' ? 'xlsx' : 'pdf'}`
        document.body.appendChild(link)
        link.click()
        
        // 清理
        setTimeout(() => {
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
        }, 100)
        
        ElMessage.success('下载成功')
      } catch (error) {
        console.error('下载失败:', error)
        // 尝试从错误响应中获取更多信息
        const errorMsg = error.response?.data?.message || error.message || '下载失败，请稍后重试'
        ElMessage.error(errorMsg)
      }
    }
    
    onMounted(() => {
      loadActivityData()
    })
    
    return {
      loading,
      joinLoading,
      activity,
      hasRegistered,
      userRole,
      userId,
      isUserRegistered,
      getTypeName,
      getTypeTag,
      getStatusName,
      getStatusTag,
      formatDateTime,
      canJoin,
      handleBack,
      handleJoin,
      handleCancelJoin,
      handleGenerateCertificate,
      handleDownloadParticipants,
    }
  }
}
</script>

<style scoped>
.activity-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 1.5rem;
}

.detail-content {
  margin-top: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.info-row.description {
  align-items: flex-start;
}

.label {
  width: 120px;
  font-weight: 500;
  color: #606266;
}

.value {
  flex: 1;
  color: #303133;
}

.description .value {
  white-space: pre-wrap;
  line-height: 1.8;
}

.loading-container {
  margin-top: 20px;
}

.card-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>