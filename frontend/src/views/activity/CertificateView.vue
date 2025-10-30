<template>
  <div class="certificate-view">
    <div class="page-header">
      <h2>电子参赛证明</h2>
      <el-button type="primary" @click="handleBack">返回活动详情</el-button>
    </div>
    
    <div class="certificate-container" v-if="!loading">
      <div class="certificate-card">
        <div class="certificate-header">
          <h1>电子参赛证明</h1>
          <p class="subtitle">Certificate of Participation</p>
        </div>
        
        <div class="certificate-content">
          <p class="present-to">兹证明</p>
          <p class="student-name">{{ certificateData.studentName }}</p>
          <p class="message">同学于 {{ formatDate(certificateData.activityDate) }} 参加了</p>
          <p class="activity-title">{{ certificateData.activityTitle }}</p>
          <p class="activity-type">{{ certificateData.activityType }}</p>
          <p class="footer-message">特发此证，以资鼓励</p>
        </div>
        
        <div class="certificate-footer">
          <div class="signature-area">
            <p>主办方：{{ certificateData.organizer }}</p>
            <p class="signature-date">日期：{{ formatDate(certificateData.issueDate) }}</p>
          </div>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button @click="handlePrint">打印证书</el-button>
      </div>
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
import { ElMessage } from 'element-plus'
import { generateCertificate, getActivityById } from '@/api/activity'

export default {
  name: 'CertificateView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const loading = ref(true)
    const activityDetail = ref({})
    const certificateData = ref({
      studentName: '',
      activityTitle: '',
      activityType: '',
      activityDate: '',
      organizer: '',
      issueDate: new Date().toISOString(),
      certificateUrl: ''
    })
    
    const user = computed(() => store.getters.user || {})
    const userRole = computed(() => store.getters.userRole)
    const userId = computed(() => store.getters.userId)
    
    const formatDate = (dateTime) => {
      if (!dateTime) return '-'  
      const date = new Date(dateTime)
      if (isNaN(date.getTime())) return dateTime
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
    
    const getActivityTypeName = (type) => {
      const typeMap = {
        'LECTURE': '讲座',
        'COMPETITION': '赛事',
        'TRAINING': '培训'
      }
      return typeMap[type] || type
    }
    
    const loadActivityDetail = async (activityId) => {
      try {
        const response = await getActivityById(activityId)
        if (response.code === 200) {
          activityDetail.value = response.data || {}
          return true
        }
        return false
      } catch (error) {
        console.error('加载活动详情失败:', error)
        return false
      }
    }
    
    const loadCertificateData = async () => {
      loading.value = true
      try {
        const activityId = route.params.activityId
        console.log('加载证书数据 - 活动ID:', activityId)
        
        // 先加载活动详情，获取真实的活动信息
        await loadActivityDetail(activityId)
        
        // 调用生成证明接口
        const response = await generateCertificate(activityId)
        console.log('store的数据：', store.getters.user)
        // 获取真实的学生姓名（从store的user对象中）
        const realStudentName = user.value?.realName || user.value?.username || user.value?.nickname || '学生姓名'
        
        if (response.code === 200 || response.success) {
          // 更新证书数据，优先使用响应中的数据，其次使用活动详情数据，最后使用默认值
          certificateData.value = {
            ...certificateData.value,
            certificateUrl: response.data || '',
            studentName: response.studentName || response.data?.studentName || realStudentName,
            activityTitle: response.activityTitle || response.data?.activityTitle || activityDetail.value.title || '活动名称',
            activityType: response.activityType || response.data?.activityType || getActivityTypeName(activityDetail.value.type),
            activityDate: response.activityDate || response.data?.activityDate || activityDetail.value.startTime || new Date().toISOString(),
            organizer: response.organizer || response.data?.organizer || activityDetail.value.organizer || '主办方',
            issueDate: response.issueDate || response.data?.issueDate || new Date().toISOString()
          }
          console.log('证书数据:', certificateData.value)
        } else {
          // 即使接口返回失败，也使用活动详情和用户信息填充证书
          certificateData.value = {
            ...certificateData.value,
            studentName: realStudentName,
            activityTitle: activityDetail.value.title || '活动名称',
            activityType: getActivityTypeName(activityDetail.value.type),
            activityDate: activityDetail.value.startTime || new Date().toISOString(),
            organizer: activityDetail.value.organizer || '主办方'
          }
          console.log('使用本地数据填充证书:', certificateData.value)
        }
      } catch (error) {
        console.error('加载证书失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '加载证书失败，请稍后重试'
        ElMessage.error(errorMsg)
        
        // 错误情况下也尝试使用本地数据
        const realStudentName = user.value?.name || user.value?.username || user.value?.nickname || '学生姓名'
        certificateData.value = {
          ...certificateData.value,
          studentName: realStudentName,
          activityTitle: activityDetail.value.title || '活动名称',
          activityType: getActivityTypeName(activityDetail.value.type),
          activityDate: activityDetail.value.startTime || new Date().toISOString(),
          organizer: activityDetail.value.organizer || '主办方'
        }
      } finally {
        loading.value = false
      }
    }
    
    const handleBack = () => {
      const activityId = route.params.activityId
      router.push(`/dashboard/activities/${activityId}`)
    }
    
    const handlePrint = () => {
      window.print()
    }
    
    onMounted(() => {
      loadCertificateData()
    })
    
    return {
      loading,
      certificateData,
      formatDate,
      handleBack,
      handlePrint
    }
  }
}
</script>

<style scoped>
.certificate-view {
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

.certificate-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.certificate-card {
  width: 80%;
  max-width: 800px;
  background: white;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.certificate-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.certificate-header h1 {
  font-size: 2.5rem;
  color: #303133;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.certificate-header .subtitle {
  font-size: 1.2rem;
  color: #606266;
  margin: 0;
  font-style: italic;
}

.certificate-content {
  text-align: center;
  margin-bottom: 40px;
}

.certificate-content p {
  margin: 20px 0;
  line-height: 1.8;
}

.present-to {
  font-size: 1.3rem;
  color: #303133;
}

.student-name {
  font-size: 2rem;
  font-weight: bold;
  color: #303133;
  margin: 30px 0;
}

.message {
  font-size: 1.3rem;
  color: #303133;
}

.activity-title {
  font-size: 1.8rem;
  font-weight: bold;
  color: #303133;
  margin: 30px 0;
}

.activity-type {
  font-size: 1.3rem;
  color: #303133;
}

.footer-message {
  font-size: 1.3rem;
  color: #303133;
  margin-top: 40px;
}

.certificate-footer {
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.signature-area p {
  margin: 10px 0;
  font-size: 1.1rem;
  color: #606266;
}

.signature-date {
  font-style: italic;
}

.action-buttons {
  display: flex;
  gap: 20px;
}

.loading-container {
  margin-top: 40px;
  width: 80%;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

/* 打印样式优化 */
@media print {
  .page-header,
  .action-buttons {
    display: none;
  }
  
  .certificate-card {
    box-shadow: none;
    width: 100%;
    max-width: none;
  }
}
</style>