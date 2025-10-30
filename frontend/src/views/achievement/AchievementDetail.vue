<template>
  <div class="achievement-detail">
    <!-- PDF预览功能已修改为直接在新标签页打开 -->
    <div class="page-header">
      <h2>成果详情</h2>
      <el-button type="primary" @click="handleBack">返回列表</el-button>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div class="detail-card" v-else-if="achievement">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <h3>{{ achievement.title }}</h3>
            <div class="status-tag">
              <el-tag :type="getStatusTag(achievement.status)">{{ getStatusName(achievement.status) }}</el-tag>
            </div>
          </div>
        </template>
        
        <div class="detail-content">
          <div class="info-row">
            <span class="label">成果类型：</span>
            <el-tag :type="getTypeTag(achievement.type)">{{ getTypeName(achievement.type) }}</el-tag>
          </div>
          
          <div class="info-row">
            <span class="label">级别：</span>
            <el-tag v-if="achievement.level" :type="getLevelTag(achievement.level)">{{ getLevelName(achievement.level) }}</el-tag>
            <span v-else>-</span>
          </div>
          
          <div class="info-row">
            <span class="label">提交者：</span>
            <span class="value">{{ achievement.submitterName || '未知' }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatDateTime(achievement.createTime) }}</span>
          </div>
          
          <div class="info-row">
            <span class="label">成果描述：</span>
            <div class="value description">{{ achievement.description || '-' }}</div>
          </div>
          
          <div class="info-row file-section">
            <span class="label">相关附件：</span>
            <div class="value">
              <template v-if="achievement.fileUrl">
                <div class="file-info">
                  <div class="file-details">
                    <div class="file-name">{{ achievement.fileName || '附件文件' }}</div>
                    <div class="file-size">{{ formatFileSize(achievement.fileSize) }}</div>
                  </div>
                  <!-- PDF文件显示预览和下载按钮 -->
                  <template v-if="isPdfFile(achievement.fileName)">
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="handlePreview"
                      icon="DocumentChecked"
                      style="margin-right: 10px"
                    >
                      预览PDF
                    </el-button>
                    <el-button 
                      type="success" 
                      size="small" 
                      @click="handleDownload"
                      icon="DocumentDownload"
                    >
                      下载附件
                    </el-button>
                  </template>
                  <!-- 非PDF文件只显示下载按钮 -->
                  <el-button 
                    v-else
                    type="primary" 
                    size="small" 
                    @click="handleDownload"
                    icon="DocumentDownload"
                  >
                    下载附件
                  </el-button>
                </div>
              </template>
              <div v-else class="no-file">暂无附件</div>
            </div>
          </div>
          
          <!-- 审核信息 -->
          <div v-if="showApprovalInfo" class="approval-info">
            <h4>审核信息</h4>
            
            <!-- 初审信息 -->
            <div class="approval-section" v-if="achievement.firstApproval">
              <div class="approval-title">初审：</div>
              <div class="approval-content">
                <div class="approval-item">
                  <span class="approval-label">审核结果：</span>
                  <el-tag :type="achievement.firstApproval.approved ? 'success' : 'danger'">
                    {{ achievement.firstApproval.approved ? '通过' : '驳回' }}
                  </el-tag>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核意见：</span>
                  <span class="approval-value">{{ achievement.firstApproval.comment || '-' }}</span>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核时间：</span>
                  <span class="approval-value">{{ formatDateTime(achievement.firstApproval.approvalTime) }}</span>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核人：</span>
                  <span class="approval-value">{{ achievement.firstApproval.approverName || '未知' }}</span>
                </div>
              </div>
            </div>
            
            <!-- 终审信息 -->
            <div class="approval-section" v-if="achievement.finalApproval">
              <div class="approval-title">终审：</div>
              <div class="approval-content">
                <div class="approval-item">
                  <span class="approval-label">审核结果：</span>
                  <el-tag :type="achievement.finalApproval.approved ? 'success' : 'danger'">
                    {{ achievement.finalApproval.approved ? '通过' : '驳回' }}
                  </el-tag>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核意见：</span>
                  <span class="approval-value">{{ achievement.finalApproval.comment || '-' }}</span>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核时间：</span>
                  <span class="approval-value">{{ formatDateTime(achievement.finalApproval.approvalTime) }}</span>
                </div>
                <div class="approval-item">
                  <span class="approval-label">审核人：</span>
                  <span class="approval-value">{{ achievement.finalApproval.approverName || '未知' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <div v-else class="empty-container">
      <el-empty description="未找到成果信息" />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

export default {
  name: 'AchievementDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const loading = ref(true)
    const achievement = ref(null)
    const userCache = ref(new Map()) // 用户信息缓存
    
    const userRole = computed(() => store.getters.userRole)
    const userId = computed(() => store.getters.user?.id)
    
    // 显示审核信息的条件
    const showApprovalInfo = computed(() => {
      // 只有管理员、教师或提交者可以查看审核信息
      return userRole.value === 'ADMIN' || 
             userRole.value === 'TEACHER' || 
             (achievement.value && achievement.value.submitterId === userId.value)
    })
    
    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-'
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
    
    // 格式化文件大小
    const formatFileSize = (size) => {
      if (!size) return '-'
      const num = parseInt(size)
      if (isNaN(num)) return '-'
      
      if (num < 1024) {
        return num + ' B'
      } else if (num < 1024 * 1024) {
        return (num / 1024).toFixed(2) + ' KB'
      } else if (num < 1024 * 1024 * 1024) {
        return (num / (1024 * 1024)).toFixed(2) + ' MB'
      } else {
        return (num / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
      }
    }
    
    // 获取类型名称
    const getTypeName = (type) => {
      const typeMap = {
        'PAPER': '论文',
        'PATENT': '专利',
        'CERTIFICATE': '证书',
        'PROJECT': '项目'
      }
      return typeMap[type] || type
    }
    
    // 获取类型标签
    const getTypeTag = (type) => {
      const tagMap = {
        'PAPER': 'primary',
        'PATENT': 'success',
        'CERTIFICATE': 'warning',
        'PROJECT': 'info'
      }
      return tagMap[type] || 'info'
    }
    
    // 获取级别名称
    const getLevelName = (level) => {
      const levelMap = {
        'NATIONAL': '国家级',
        'PROVINCIAL': '省级',
        'SCHOOL': '校级'
      }
      return levelMap[level] || level
    }
    
    // 获取级别标签
    const getLevelTag = (level) => {
      const tagMap = {
        'NATIONAL': 'danger',
        'PROVINCIAL': 'warning',
        'SCHOOL': 'success'
      }
      return tagMap[level] || 'info'
    }
    
    // 获取状态名称
    const getStatusName = (status) => {
      const statusMap = {
        'PENDING': '待审核',
        'PENDING_FINAL': '待终审',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    }
    
    // 获取状态标签
    const getStatusTag = (status) => {
      const tagMap = {
        'PENDING': 'warning',
        'PENDING_FINAL': 'success',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return tagMap[status] || 'info'
    }
    
    // 获取用户详情并缓存结果
    const fetchUserDetail = async (userId) => {
      // 检查缓存中是否已有该用户信息
      if (userCache.value.has(userId)) {
        console.log(`从缓存中获取的用户信息:`, userCache.value.get(userId))
        return userCache.value.get(userId)
      }
      
      try {
        console.log(`开始获取用户${userId}信息`)
        const response = await request.get(`/users/basic/${userId}`)
        console.log(`获取用户${userId}信息成功:`, response)
        const userInfo = response?.data || response
        userCache.value.set(userId, userInfo)
        return userInfo
      } catch (error) {
        console.error(`获取用户${userId}信息失败:`, error)
        // 返回默认值，避免影响页面显示
        return { realName: '未知用户', username: '未知用户' }
      }
    }

    // 加载成果详情
    const loadAchievementDetail = async () => {
      loading.value = true
      try {
        const achievementId = route.params.id
        const response = await request.get(`/achievements/${achievementId}`)
        
        if (response.code === 200) {
          achievement.value = response.data || response
          
          // 获取提交者姓名
          if (achievement.value.submitterId && !achievement.value.submitterName) {
            const userInfo = await fetchUserDetail(achievement.value.submitterId)
            achievement.value.submitterName = userInfo.realName || userInfo.username || '未知用户'
          }
          
          // 获取初审审核者姓名
          if (achievement.value.firstApproval && achievement.value.firstApproval.approverId) {
            console.log(`尝试获取初审审核者ID: ${achievement.value.firstApproval.approverId} 的真实姓名`)
            console.log(`当前初审审核者姓名: ${achievement.value.firstApproval.approverName}`)
            const userInfo = await fetchUserDetail(achievement.value.firstApproval.approverId)
            console.log(`获取到的用户信息:`, userInfo)
            achievement.value.firstApproval.approverName = userInfo.realName || userInfo.username || '未知用户'
            console.log(`更新后初审审核者姓名: ${achievement.value.firstApproval.approverName}`)
          }
          
          // 获取终审审核者姓名
          if (achievement.value.finalApproval && achievement.value.finalApproval.approverId) {
            console.log(`尝试获取终审审核者ID: ${achievement.value.finalApproval.approverId} 的真实姓名`)
            console.log(`当前终审审核者姓名: ${achievement.value.finalApproval.approverName}`)
            const userInfo = await fetchUserDetail(achievement.value.finalApproval.approverId)
            console.log(`获取到的用户信息:`, userInfo)
            achievement.value.finalApproval.approverName = userInfo.realName || userInfo.username || '未知用户'
            console.log(`更新后终审审核者姓名: ${achievement.value.finalApproval.approverName}`)
          }
        }
      } catch (error) {
        console.error('加载成果详情失败:', error)
        ElMessage.error('加载成果详情失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 处理返回
    const handleBack = () => {
      router.push('/dashboard/achievements')
    }
    
    // 检查是否为PDF文件
    const isPdfFile = (fileName) => {
      return fileName && fileName.toLowerCase().endsWith('.pdf')
    }
    
    // 处理附件预览 - 直接在新标签页中打开PDF
    const handlePreview = () => {
      if (achievement.value?.fileUrl) {
        // 确保fileUrl包含正确的API前缀
        let fileUrl = achievement.value.fileUrl
        if (!fileUrl.startsWith('/api') && !fileUrl.startsWith('http')) {
          fileUrl = '/api' + fileUrl
        }
        
        // 在新标签页中打开PDF
        window.open(fileUrl, '_blank')
      }
    }
    
    // 处理附件下载
    const handleDownload = () => {
      if (achievement.value?.fileUrl) {
        // 尝试使用更安全的下载方式
        const link = document.createElement('a')
        // 确保fileUrl包含正确的API前缀
        let fileUrl = achievement.value.fileUrl
        if (!fileUrl.startsWith('/api') && !fileUrl.startsWith('http')) {
          fileUrl = '/api' + fileUrl
        }
        link.href = fileUrl
        link.download = achievement.value.fileName || '附件'
        document.body.appendChild(link)
        link.click()
        
        // 清理
        setTimeout(() => {
          document.body.removeChild(link)
        }, 100)
        
        // 提供下载提示
        ElMessage.success('开始下载附件')
      } else {
        ElMessage.warning('没有可下载的附件')
      }
    }
    
    onMounted(() => {
      loadAchievementDetail()
    })
    
    return {
      loading,
      achievement,
      showApprovalInfo,
      formatDateTime,
      formatFileSize,
      getTypeName,
      getTypeTag,
      getLevelName,
      getLevelTag,
      getStatusTag,
      getStatusName,
      handleBack,
      handleDownload,
      isPdfFile,
      handlePreview
    }
  }
}
</script>

<style scoped>
.achievement-detail {
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
  color: #2c3e50;
}

.detail-content {
  margin-top: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.label {
  width: 100px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.value {
    flex: 1;
    color: #303133;
  }
  
  .file-section .file-info {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 8px;
    border: 1px solid #ebeef5;
  }
  
  .file-section .file-details {
    flex: 1;
  }
  
  .file-section .file-name {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 5px;
  }
  
  .file-section .file-size {
    font-size: 14px;
    color: #606266;
  }
  
  .file-section .no-file {
    padding: 20px;
    color: #999;
    background-color: #f5f7fa;
    border-radius: 8px;
    text-align: center;
  }

  .description {
  white-space: pre-wrap;
  line-height: 1.8;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.loading-container {
  margin-top: 20px;
}

.empty-container {
  margin-top: 40px;
  text-align: center;
}

/* 审核信息样式 */
.approval-info {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.approval-info h4 {
  margin-bottom: 15px;
  color: #2c3e50;
}

.approval-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.approval-title {
  font-weight: 500;
  color: #606266;
  margin-bottom: 10px;
}

.approval-content {
  margin-left: 20px;
}

.approval-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.approval-label {
  width: 80px;
  color: #606266;
  flex-shrink: 0;
}

.approval-value {
  flex: 1;
  color: #303133;
}
  
  /* PDF预览样式已简化 */
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .pdf-preview-container {
      height: 60vh;
    }
    
    .pdf-preview-container iframe {
      height: 60vh;
    }
    
    .el-dialog {
      width: 95% !important;
      top: 2vh !important;
    }
  }
</style>