<template>
  <div class="achievement-list">
    <div class="page-header">
      <h1>成果申报</h1>
      <el-button 
        type="primary" 
        @click="handleCreate"
        v-if="hasPermission(['STUDENT'])"
      >
        <el-icon><Plus /></el-icon>
        申报成果
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :model="searchForm" inline>
        <el-form-item label="成果标题">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入成果标题"
            clearable
          />
        </el-form-item>
        <el-form-item label="成果类型">
          <el-select v-model="searchForm.type" style="width: 120px" placeholder="请选择类型" clearable>
            <el-option label="论文" value="PAPER" />
            <el-option label="专利" value="PATENT" />
            <el-option label="证书" value="CERTIFICATE" />
            <el-option label="项目" value="PROJECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.status" style="width: 120px" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="待终审" value="PENDING_FINAL" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成果列表 -->
    <div class="table-container">
      <el-table :data="achievementList" v-loading="loading">
        <el-table-column prop="title" label="成果标题" min-width="150" />
        <el-table-column prop="type" label="类型" width="130">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.level" :type="getLevelTag(scope.row.level)">
              {{ getLevelName(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitterName" label="提交者" width="120" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleEdit(scope.row)"
              v-if="canEdit(scope.row)"
            >
              编辑
            </el-button>
            <!-- 教师显示审核按钮，管理员显示待初审或终审按钮 -->
            <el-button 
              type="success" 
              size="small" 
              @click="handleApprove(scope.row)"
              v-if="canApprove(scope.row)"
            >
              {{ getApprovalButtonText(scope.row) }}
            </el-button>
            <!-- 管理员显示待初审按钮（不可点击） -->
            <el-button 
              type="info" 
              size="small" 
              disabled
              v-if="hasPermission(['ADMIN']) && scope.row.status === 'PENDING' && !canApprove(scope.row)"
            >
              待初审
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
              v-if="canDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
      
      <!-- 审核弹窗 -->
      <el-dialog
        v-model="approvalDialogVisible"
        :title="`${currentAchievement?.status === 'PENDING' ? '初审' : '终审'}成果: ${currentAchievement?.title}`"
        width="500px"
        class="approval-dialog"
      >
        <div v-if="currentAchievement">
          <el-form
            :model="approvalForm"
            label-width="80px"
          >
            <el-form-item label="审核结果">
              <el-radio-group v-model="approvalForm.approved">
                <el-radio :label="true">通过</el-radio>
                <el-radio :label="false">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="审核意见">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入审核意见"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeApprovalDialog">取消</el-button>
            <el-button type="primary" @click="submitApproval">确认</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAchievementList, getUserAchievements, deleteAchievement } from '@/api/achievement'
import request from '@/api/request'

export default {
  name: 'AchievementList',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    const loading = ref(false)
    const achievementList = ref([])
    const userCache = ref(new Map()) // 用于缓存用户信息，避免重复请求
    
    const searchForm = reactive({
      title: '',
      type: '',
      status: ''
    })
    
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    const userRole = computed(() => store.getters.userRole)
    const userId = computed(() => store.getters.user?.id)
    
    // 审核弹窗相关
    const approvalDialogVisible = ref(false)
    const currentAchievement = ref(null)
    const approvalForm = reactive({
      approved: true,
      comment: ''
    })
    
    const hasPermission = (roles) => {
      return roles.includes(userRole.value)
    }
    
    const getTypeName = (type) => {
      const typeMap = {
        'PAPER': '论文',
        'PATENT': '专利',
        'CERTIFICATE': '证书',
        'PROJECT': '项目'
      }
      return typeMap[type] || type
    }
    
    const getTypeTag = (type) => {
      const tagMap = {
        'PAPER': 'primary',
        'PATENT': 'success',
        'CERTIFICATE': 'warning',
        'PROJECT': 'info'
      }
      return tagMap[type] || 'info'
    }
    
    const getLevelName = (level) => {
      const levelMap = {
        'NATIONAL': '国家级',
        'PROVINCIAL': '省级',
        'SCHOOL': '校级'
      }
      return levelMap[level] || level
    }
    
    const getLevelTag = (level) => {
      const tagMap = {
        'NATIONAL': 'danger',
        'PROVINCIAL': 'warning',
        'SCHOOL': 'success'
      }
      return tagMap[level] || 'info'
    }
    
    const getStatusName = (status) => {
      const statusMap = {
        'PENDING': '待审核',
        'PENDING_FINAL': '待终审',
        'APPROVED': '已通过',
        'REJECTED': '已驳回'
      }
      return statusMap[status] || status
    }
    
    const getStatusTag = (status) => {
      const tagMap = {
        'PENDING': 'warning',
        'PENDING_FINAL': 'success',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return tagMap[status] || 'info'
    }
    
    const canEdit = (achievement) => {
      return hasPermission(['STUDENT']) && 
             achievement.submitterId === userId.value && 
             achievement.status === 'PENDING'
    }
    
    const canApprove = (achievement) => {
      // 教师可以初审
      if (hasPermission(['TEACHER']) && achievement.status === 'PENDING') {
        return true
      }
      // 管理员只能终审，不能初审
      if (hasPermission(['ADMIN']) && achievement.status === 'PENDING_FINAL') {
        return true
      }
      return false
    }
    
    // 获取审核按钮文本
    const getApprovalButtonText = (achievement) => {
      if (achievement.status === 'PENDING') {
        return userRole.value === 'TEACHER' ? '审核' : '待初审'
      }
      if (achievement.status === 'PENDING_FINAL' && hasPermission(['ADMIN'])) {
        return '终审'
      }
      return '审核'
    }
    
    const canDelete = (achievement) => {
      return hasPermission(['STUDENT']) && 
             achievement.submitterId === userId.value && 
             achievement.status === 'PENDING'
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
        // 返回默认值，避免影响整个列表显示
        return { realName: '未知用户', username: '未知用户' }
      }
    }
    
    // 为成果列表中的每个成果获取提交者姓名
    const enrichAchievementWithUserInfo = async (achievements) => {
      const promises = achievements.map(async (achievement) => {
        if (achievement.submitterId) {
          const userInfo = await fetchUserDetail(achievement.submitterId)
          // 将submitterName设置为用户的真实姓名
          achievement.submitterName = userInfo.realName || userInfo.username || '未知用户'
        } else {
          achievement.submitterName = '未知用户'
        }
        return achievement
      })
      
      return Promise.all(promises)
    }
    
    const loadAchievementList = async () => {
      loading.value = true
      try {
        // 根据用户角色调用不同的API
        let response
        
        if (hasPermission(['STUDENT'])) {
          // 学生只能查看自己的成果，并支持搜索和分页
          // 构建查询参数，确保只传递有效的搜索条件
          const params = {
            pageNum: pagination.current,
            pageSize: pagination.size
          }
          
          // 检查并添加搜索条件
          if (searchForm.title) {
            const trimmedTitle = searchForm.title.trim()
            if (trimmedTitle) {
              params.title = trimmedTitle
            }
          }
          if (searchForm.type) {
            params.type = searchForm.type
          }
          if (searchForm.status) {
            params.status = searchForm.status
          }
          
          response = await getUserAchievements(userId.value, params)
        } else {
          // 教师和管理员可以查看所有成果，并支持搜索和分页
          // 构建查询参数，确保只传递有效的搜索条件
          const params = {
            page: pagination.current,
            pageSize: pagination.size
          }
          
          // 检查并添加搜索条件
          if (searchForm.title) {
            const trimmedTitle = searchForm.title.trim()
            if (trimmedTitle) {
              params.title = trimmedTitle
            }
          }
          if (searchForm.type) {
            params.type = searchForm.type
          }
          if (searchForm.status) {
            params.status = searchForm.status
          }
          
          response = await getAchievementList(params)
        }
        
        // 获取成果数据
        let achievements = []
        if (hasPermission(['STUDENT'])) {
          // 学生API返回的是包含list的对象
          achievements = response.data?.list || []
        } else {
          // 教师和管理员API返回格式
          achievements = response.data?.achievements || response.data || []
        }
        
        // 为每个成果获取提交者的真实姓名
        achievements = await enrichAchievementWithUserInfo(achievements)
        
        achievementList.value = achievements
        pagination.total = response.data?.total || achievementList.value.length
      } catch (error) {
        console.error('加载成果列表失败:', error)
        ElMessage.error('加载成果列表失败: ' + (error.response?.data?.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      pagination.current = 1
      loadAchievementList()
    }
    
    const handleReset = () => {
      Object.assign(searchForm, {
        title: '',
        type: '',
        status: ''
      })
      handleSearch()
    }
    
    const handleCreate = () => {
      router.push('/dashboard/achievements/create')
    }
    
    const handleView = (achievement) => {
      router.push(`/dashboard/achievements/${achievement.id}`)
    }
    
    const handleEdit = (achievement) => {
      router.push(`/dashboard/achievements/${achievement.id}/edit`)
    }
    
    const handleApprove = (achievement) => {
      currentAchievement.value = achievement
      approvalForm.approved = true
      approvalForm.comment = ''
      approvalDialogVisible.value = true
    }
    
    // 提交审核
    const submitApproval = async () => {
      if (!currentAchievement.value) return
      
      // 根据状态决定是初审还是终审
      const isFirstApproval = currentAchievement.value.status === 'PENDING'
      const isFinalApproval = currentAchievement.value.status === 'PENDING_FINAL'
      
      // 验证权限：只有教师可以初审，只有管理员可以终审
      if ((isFirstApproval && !hasPermission(['TEACHER'])) || 
          (isFinalApproval && !hasPermission(['ADMIN']))) {
        ElMessage.warning('您没有权限执行此操作')
        return
      }
      
      const url = isFirstApproval 
        ? `/achievements/${currentAchievement.value.id}/first-approve`
        : `/achievements/${currentAchievement.value.id}/final-approve`
      
      try {
        const response = await request.post(url, approvalForm)
        ElMessage.success(response?.msg || '审核操作成功')
        approvalDialogVisible.value = false
        loadAchievementList() // 重新加载列表
      } catch (error) {
        ElMessage.error(error.response?.data?.msg || '审核操作失败')
      }
    }
    
    // 关闭审核弹窗
    const closeApprovalDialog = () => {
      approvalDialogVisible.value = false
      currentAchievement.value = null
    }
    
    const handleDelete = (achievement) => {
      ElMessageBox.confirm('确定要删除此成果吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteAchievement(achievement.id)
          ElMessage.success('删除成功')
          loadAchievementList()
        } catch (error) {
          console.error('删除成果失败:', error)
          ElMessage.error('删除失败: ' + (error.response?.data?.message || '未知错误'))
        }
      })
    }
    
    const handleSizeChange = (size) => {
      pagination.size = size
      loadAchievementList()
    }
    
    const handleCurrentChange = (current) => {
      pagination.current = current
      loadAchievementList()
    }
    
    onMounted(() => {
      loadAchievementList()
    })
    
    return {
      loading,
      achievementList,
      searchForm,
      pagination,
      hasPermission,
      getTypeName,
      getTypeTag,
      getLevelName,
      getLevelTag,
      getStatusName,
      getStatusTag,
      canEdit,
      canApprove,
      canDelete,
      getApprovalButtonText,
      approvalDialogVisible,
      currentAchievement,
      approvalForm,
      handleSearch,
      handleReset,
      handleCreate,
      handleView,
      handleEdit,
      handleApprove,
      submitApproval,
      closeApprovalDialog,
      handleDelete,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.achievement-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.search-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
}

.approval-dialog .el-dialog__header {
  background-color: #f5f7fa;
  padding: 20px 25px 15px;
}

.approval-dialog .el-dialog__body {
  padding: 20px 25px;
}

.approval-dialog .dialog-footer {
  margin-top: 20px;
  text-align: right;
}
</style>
