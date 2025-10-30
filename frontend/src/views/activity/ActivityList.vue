<template>
  <div class="activity-list">
    <div class="page-header">
      <h1>学术活动</h1>
      <el-button 
        type="primary" 
        @click="handleCreate"
        v-if="hasPermission(['TEACHER', 'ADMIN'])"
      >
        <el-icon><Plus /></el-icon>
        发布活动
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动标题">
          <el-input 
            v-model="searchForm.title" 
            placeholder="请输入活动标题"
            clearable
          />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" style="width: 120px" placeholder="请选择类型" clearable>
            <el-option label="讲座" value="LECTURE" />
            <el-option label="赛事" value="COMPETITION" />
            <el-option label="培训" value="TRAINING" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" style="width: 120px" placeholder="请选择状态" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已完成" value="COMPLETED" />
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

    <!-- 活动列表 -->
    <div class="table-container">
      <el-table :data="activityList" v-loading="loading">
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="organizer" label="主办方" width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="currentParticipants" label="参与人数" width="100" />
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
            <!-- 只有学生角色才显示报名和已报名按钮 -->
            <!-- 先标记为未报名，后面会从响应中获取正确状态 -->
            <el-button 
              type="success" 
              size="small" 
              @click="handleJoin(scope.row)"
              v-if="userRole === 'STUDENT' && canJoin(scope.row) && !isUserRegistered(scope.row)"
            >
              报名
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              disabled
              v-else-if="userRole === 'STUDENT' && isUserRegistered(scope.row)"
            >
              已报名
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleEdit(scope.row)"
              v-if="canEdit(scope.row)"
            >
              编辑
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
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, deleteActivity, joinActivity } from '@/api/activity'

export default {
  name: 'ActivityList',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    const loading = ref(false)
    const activityList = ref([])
    
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
    const userId = computed(() => store.getters.userId) // 获取当前登录用户ID
    
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
    
    // 判断当前用户是否已报名活动
    const isUserRegistered = (activity) => {
      // 添加详细调试信息
      console.log('判断报名状态 - 活动ID:', activity.id, '用户ID:', userId.value)
      
      // 确保活动对象存在
      if (!activity) {
        console.log('活动对象不存在')
        return false
      }
      
      // 1. 优先使用后端直接返回的hasRegistered字段（如果有）
      if (typeof activity.hasRegistered === 'boolean') {
        console.log('通过hasRegistered字段检查结果:', activity.hasRegistered)
        return activity.hasRegistered
      }
      
      // 2. 检查是否有registeredUsers数组包含当前用户ID
      if (activity.registeredUsers && Array.isArray(activity.registeredUsers)) {
        const result = activity.registeredUsers.includes(userId.value)
        console.log('通过registeredUsers检查结果:', result)
        return result
      }
      
      // 3. 检查是否有registrations数组，包含当前用户的报名记录
      if (activity.registrations && Array.isArray(activity.registrations)) {
        const result = activity.registrations.some(reg => reg.userId === userId.value)
        console.log('通过registrations检查结果:', result)
        return result
      }
      
      // 默认返回false，确保未报名用户不显示已报名状态
      return false
    }
    
    const canJoin = (activity) => {
      // 直接在这里判断是否可以报名，不依赖isUserRegistered函数
      return userRole.value === 'STUDENT' && 
             activity.status === 'PUBLISHED' && 
             new Date(activity.registrationDeadline) > new Date()
    }
    
    const canEdit = (activity) => {
      return hasPermission(['TEACHER', 'ADMIN']) && 
             activity.status !== 'COMPLETED'
    }
    
    const canDelete = (activity) => {
      return hasPermission(['ADMIN']) || 
             (hasPermission(['TEACHER']) && activity.status === 'DRAFT')
    }
    
    const loadActivityList = async () => {
      loading.value = true
      try {
        // 添加调试信息
        console.log('当前登录用户ID:', userId.value, '角色:', userRole.value)
        
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
        
        // 调用API获取活动列表
        console.log('发送搜索参数:', params)
        const response = await getActivityList(params)
        
        console.log('API响应:', response)
        
        if (response.code === 200) {
          // 确保正确处理后端响应格式
          const responseData = response.data
          activityList.value = Array.isArray(responseData) ? responseData : (responseData.records || [])
          pagination.total = responseData.total || activityList.value.length
          
          // 调试信息
          console.log('加载到的活动数量:', activityList.value.length)
          console.log('设置的分页总数:', pagination.total)
          
          // 如果有搜索条件但返回了所有数据，可能是后端未正确处理搜索
          const hasSearchCondition = !!params.title || !!params.type || !!params.status
          if (hasSearchCondition) {
            console.log('存在搜索条件，检查后端过滤是否生效')
            // 前端额外过滤作为临时解决方案
            if (params.type) {
              activityList.value = activityList.value.filter(activity => activity.type === params.type)
              console.log('前端按类型过滤后数量:', activityList.value.length)
            }
            if (params.status) {
              activityList.value = activityList.value.filter(activity => activity.status === params.status)
              console.log('前端按状态过滤后数量:', activityList.value.length)
            }
            if (params.title) {
              activityList.value = activityList.value.filter(activity => 
                activity.title.toLowerCase().includes(params.title.toLowerCase())
              )
              console.log('前端按标题过滤后数量:', activityList.value.length)
            }
            // 过滤后更新分页总数
            pagination.total = activityList.value.length
          }
          
          // 加载完成后，为每个活动记录报名状态判断结果
          activityList.value.forEach(activity => {
            const registered = isUserRegistered(activity)
            console.log(`活动 ${activity.title} (ID:${activity.id}) - 用户是否已报名:`, registered)
          })
          
        } else {
          ElMessage.error(response.message || '加载活动列表失败')
          activityList.value = []
          pagination.total = 0
        }
      } catch (error) {
        ElMessage.error('加载活动列表失败')
        activityList.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      pagination.current = 1
      loadActivityList()
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
      router.push('/dashboard/activities/create')
    }
    
    const handleView = (activity) => {
      // 查看活动详情
      router.push(`/dashboard/activities/${activity.id}`)
    }
    
    const handleJoin = (activity) => {
      // 添加前置验证
      if (!activity.id) {
        ElMessage.error('活动信息错误，无法报名')
        return
      }
      
      // 检查报名截止时间
      if (new Date(activity.registrationDeadline) <= new Date()) {
        ElMessage.warning('报名已截止')
        return
      }
      
      // 检查是否已达到最大参与人数
      if (activity.currentParticipants >= activity.maxParticipants) {
        ElMessage.warning('活动人数已满')
        return
      }
      
      // 再次确认用户未报名，防止重复报名
      if (isUserRegistered(activity)) {
        ElMessage.warning('您已报名该活动')
        return
      }
      
      ElMessageBox.confirm('确定要报名参加此活动吗？', '确认报名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await joinActivity(activity.id)
          if (response.code === 200 || response.success) {
            ElMessage.success(response.message || '报名成功')
            // 重新加载活动列表以更新报名状态
            loadActivityList()
          } else {
            ElMessage.error(response.message || '报名失败')
          }
        } catch (error) {
          const errorMsg = error.response?.data?.message || error.message || '报名失败，请稍后重试'
          ElMessage.error(errorMsg)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    const handleEdit = (activity) => {
      router.push(`/dashboard/activities/${activity.id}/edit`)
    }
    
    const handleDelete = (activity) => {
      ElMessageBox.confirm('确定要删除此活动吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteActivity(activity.id)
          if (response.code === 200) {
            ElMessage.success(response.message || '删除成功')
            loadActivityList()
          } else {
            ElMessage.error(response.message || '删除失败')
          }
        } catch (error) {
          ElMessage.error('删除失败，请稍后重试')
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    const handleSizeChange = (size) => {
      pagination.size = size
      loadActivityList()
    }
    
    const handleCurrentChange = (current) => {
      pagination.current = current
      loadActivityList()
    }
    
    onMounted(() => {
      loadActivityList()
    })
    
    return {
      loading,
      activityList,
      searchForm,
      pagination,
      userRole,
      userId,
      hasPermission,
      getTypeName,
      getTypeTag,
      getStatusName,
      getStatusTag,
      isUserRegistered,
      canJoin,
      canEdit,
      canDelete,
      handleSearch,
      handleReset,
      handleCreate,
      handleView,
      handleJoin,
      handleEdit,
      handleDelete,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.activity-list {
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
</style>
