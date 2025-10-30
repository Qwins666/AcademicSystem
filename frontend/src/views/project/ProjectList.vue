<template>
  <div class="project-list">
    <div class="page-header">
      <h1>科研项目</h1>
      <el-button 
        type="primary" 
        @click="handleCreate"
        v-if="hasPermission(['TEACHER', 'ADMIN'])"
      >
        <el-icon><Plus /></el-icon>
        创建项目
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :model="searchForm" inline>
        <el-form-item label="项目名称">
          <el-input 
            v-model="searchForm.name" 
            placeholder="请输入项目名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="searchForm.type" style="width: 120px" placeholder="请选择类型" clearable>
            <el-option label="研究项目" value="RESEARCH" />
            <el-option label="创新项目" value="INNOVATION" />
            <el-option label="实践项目" value="PRACTICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="searchForm.status" style="width: 120px" placeholder="请选择状态" clearable>
            <el-option label="规划中" value="PLANNING" />
            <el-option label="进行中" value="ONGOING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
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

    <!-- 项目列表 -->
    <div class="table-container">
      <el-table :data="projectList" v-loading="loading">
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.level" :type="getLevelTag(scope.row.level)">
              {{ getLevelName(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="120" />
        <el-table-column prop="funding" label="经费" width="120">
          <template #default="scope">
            <span v-if="scope.row.funding">¥{{ scope.row.funding.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
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
            <el-button 
              type="success" 
              size="small" 
              @click="handleJoin(scope.row)"
              v-if="canJoin(scope.row)"
            >
              加入
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
import { 
  getAllProjects, 
  deleteProject, 
  addProjectMember,
  getProjectsByStatus,
  getProjectsByType,
  getUserDetailAtProject
} from '@/api/project'

export default {
  name: 'ProjectList',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    const loading = ref(false)
    const projectList = ref([])
    const searchForm = reactive({
      name: '',
      type: '',
      status: ''
    })
    const userId = ref(store.state.user?.id || 1) // 从store中获取当前用户ID，默认值为1
    
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    const userRole = computed(() => store.getters.userRole)
    
    const hasPermission = (roles) => {
      return roles.includes(userRole.value)
    }
    
    const getTypeName = (type) => {
      const typeMap = {
        'RESEARCH': '研究项目',
        'INNOVATION': '创新项目',
        'PRACTICE': '实践项目'
      }
      return typeMap[type] || type
    }
    
    const getTypeTag = (type) => {
      const tagMap = {
        'RESEARCH': 'primary',
        'INNOVATION': 'success',
        'PRACTICE': 'warning'
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
        'PLANNING': '规划中',
        'ONGOING': '进行中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
    
    const getStatusTag = (status) => {
      const tagMap = {
        'PLANNING': 'info',
        'ONGOING': 'success',
        'COMPLETED': 'warning',
        'CANCELLED': 'danger'
      }
      return tagMap[status] || 'info'
    }
    
    const canEdit = (project) => {
      return hasPermission(['TEACHER', 'ADMIN']) && 
             project.leaderId === userId.value && 
             project.status !== 'COMPLETED'
    }
    
    const canJoin = (project) => {
      return userRole.value === 'STUDENT' && 
             project.status === 'ONGOING'
    }
    
    const canDelete = (project) => {
      return hasPermission(['ADMIN']) || 
             (hasPermission(['TEACHER']) && project.leaderId === userId.value && project.status === 'PLANNING')
    }
    
    const loadProjectList = async () => {
      loading.value = true
      try {
        // 构建查询参数
        const params = {
          page: pagination.current,
          pageSize: pagination.size,
          name: searchForm.name,
          type: searchForm.type,
          status: searchForm.status
        }
        
        // 调用API获取项目列表
        const res = await getAllProjects(params)
        
        // 假设API返回格式为 { data: [], total: 0 }
        projectList.value = res.data || res || []
        pagination.total = res.total || projectList.value.length
        
        // 为每个项目获取负责人姓名
        if (projectList.value && projectList.value.length > 0) {
          for (const project of projectList.value) {
            // 如果有leaderId但没有leaderName，则尝试获取负责人姓名
            if (project.leaderId && !project.leaderName) {
              try {
                const userRes = await getUserDetailAtProject(project.leaderId)
                const userData = userRes.data || userRes
                if (userData.realName) {
                  project.leaderName = userData.realName
                } else {
                  project.leaderName = '未知负责人'
                }
              } catch (userError) {
                console.warn(`获取项目 ${project.id} 负责人信息失败:`, userError)
                project.leaderName = '未知负责人'
              }
            }
          }
        }
      } catch (error) {
        console.error('加载项目列表失败:', error)
        ElMessage.error('加载项目列表失败')
        pagination.total = projectList.value.length
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      pagination.current = 1
      loadProjectList()
    }
    
    const handleReset = () => {
      Object.assign(searchForm, {
        name: '',
        type: '',
        status: ''
      })
      handleSearch()
    }
    
    const handleCreate = () => {
      // 创建项目，可以跳转到创建页面
      router.push('/dashboard/projects/create')
    }
    
    const handleView = (project) => {
      // 查看项目详情，可以跳转到详情页或者打开模态框
      router.push(`/dashboard/projects/${project.id}`)
    }
    
    const handleEdit = (project) => {
      router.push(`/dashboard/projects/${project.id}/edit`)
    }
    
    const handleJoin = (project) => {
      ElMessageBox.confirm('确定要加入此项目吗？', '确认加入', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await addProjectMember(project.id, userId.value)
          ElMessage.success('加入项目成功')
          loadProjectList()
        } catch (error) {
          console.error('加入项目失败:', error)
          ElMessage.error('加入项目失败')
        }
      })
    }
    
    const handleDelete = (project) => {
      ElMessageBox.confirm('确定要删除此项目吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteProject(project.id)
          ElMessage.success('删除成功')
          loadProjectList()
        } catch (error) {
          console.error('删除项目失败:', error)
          ElMessage.error('删除项目失败')
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    const handleSizeChange = (size) => {
      pagination.size = size
      loadProjectList()
    }
    
    const handleCurrentChange = (current) => {
      pagination.current = current
      loadProjectList()
    }
    
    onMounted(() => {
      loadProjectList()
    })
    
    return {
      loading,
      projectList,
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
      canJoin,
      canDelete,
      handleSearch,
      handleReset,
      handleCreate,
      handleView,
      handleEdit,
      handleJoin,
      handleDelete,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.project-list {
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
