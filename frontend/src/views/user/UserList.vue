<template>
  <div class="user-list">
    <div class="page-header">
      <h1>用户管理</h1>
    </div>

    <!-- 搜索栏 -->
    <div class="search-container">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.username"
            placeholder="请输入用户名"
            style="width: 150px"
            clearable
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input 
            v-model="searchForm.realName" 
            placeholder="请输入真实姓名"
            style="width: 150px"
            clearable
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 120px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
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

    <!-- 用户列表 -->
    <div class="table-container">
      <el-table :data="userList" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="100">
          <template #default="scope">
            {{ getGenderName(scope.row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="studentId" label="学号/工号" width="120" />
        <el-table-column prop="department" label="院系" width="150" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="roles" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleTag(scope.row.roles ? scope.row.roles[0] : '')">
              {{ getRoleName(scope.row.roles ? scope.row.roles[0] : '') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button 
                type="warning" 
                size="small" 
                @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              
              <!-- 待审批用户显示审批和拒绝按钮 -->
              <template v-if="scope.row.status === 0">
                <el-button 
                  type="success" 
                  size="small" 
                  @click="handleApprove(scope.row)"
                >
                  审批通过
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleReject(scope.row)"
                >
                  拒绝
                </el-button>
              </template>
              
              <!-- 已激活用户显示禁用按钮 -->
              <template v-else>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleToggleStatus(scope.row)"
                >
                  禁用
                </el-button>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleResetPassword(scope.row)"
                >
                  重置密码
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleDelete(scope.row)"
                  v-if="scope.row.role !== 'ADMIN'"
                >
                  删除
                </el-button>
              </template>
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

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="600px"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="学号/工号">
          <el-input v-model="editForm.studentId" placeholder="请输入学号/工号" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="editForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="editForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editForm.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" placeholder="请选择性别">
            <el-option label="未知" value="0" />
            <el-option label="男" value="1" />
            <el-option label="女" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户角色">
          <el-select v-model="editForm.roles" placeholder="请选择用户角色" style="width: 200px">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, deleteUser, approveUser, rejectUser, updateUser, updateUserRole, resetUserPassword } from '../../api/user'

export default {
  name: 'UserList',
  setup() {
    const loading = ref(false)
    const userList = ref([])
    
    const searchForm = reactive({
      username: '',
      realName: '',
      role: '',
      status: ''
    })
    
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    const getRoleName = (role) => {
      const roleMap = {
        'STUDENT': '学生',
        'TEACHER': '教师',
        'ADMIN': '管理员'
      }
      return roleMap[role] || (role || '')
    }
    
    const getRoleTag = (role) => {
      const tagMap = {
        'STUDENT': 'success',
        'TEACHER': 'warning',
        'ADMIN': 'danger'
      }
      return tagMap[role] || 'info'
    }
    
    const loadUserList = async () => {
      loading.value = true
      try {
        // 构建查询参数
        const params = {
          page: pagination.current,
          size: pagination.size,
          username: searchForm.username,
          realName: searchForm.realName,
          role: searchForm.role,
          status: searchForm.status
        }
        
        // 调用API获取用户列表
        const response = await getUserList(params)
        
        if (response.code === 200) {
          userList.value = response.data.records || response.data
          pagination.total = response.data.total || userList.value.length
        } else {
          ElMessage.error(response.message || '加载用户列表失败')
          userList.value = []
          pagination.total = 0
        }
      } catch (error) {
        ElMessage.error('加载用户列表失败')
        console.error('加载用户列表错误:', error)
        userList.value = []
        pagination.total = 0
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      pagination.current = 1
      loadUserList()
    }
    
    const handleReset = () => {
      Object.assign(searchForm, {
        username: '',
        realName: '',
        role: '',
        status: ''
      })
      handleSearch()
    }
    
    // 编辑用户相关状态
    const editDialogVisible = ref(false)
    const editFormRef = ref(null)
    const editForm = ref({})
    // 获取性别名称
    const getGenderName = (gender) => {
      const genderMap = {
        0: '未知',
        1: '男',
        2: '女'
      }
      return genderMap[gender] || '未知'
    }

    const editFormRules = {
      username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
      realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
      email: [
        { required: true, message: '邮箱不能为空', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ],
      phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }]
    }
    
    const handleEdit = (user) => {
      // 深拷贝用户数据到编辑表单
      editForm.value = { ...user }
      // 打开编辑对话框
      editDialogVisible.value = true
    }
    
    const handleSaveEdit = async () => {
      try {
        // 确保roles是数组格式
        const userData = {...editForm.value}
        if (userData.roles && typeof userData.roles === 'string') {
          userData.roles = [userData.roles]
        }
        
        // 更新用户基本信息
        const response = await updateUser(editForm.value.id, userData)
        if (response.code === 200) {
          ElMessage.success('用户信息更新成功')
          editDialogVisible.value = false
          loadUserList() // 重新加载用户列表
        } else {
          ElMessage.error(response.message || '用户信息更新失败')
        }
      } catch (error) {
        ElMessage.error('用户信息更新失败')
        console.error('更新用户信息错误:', error)
      }
    }
    
    // 审批用户
    const handleApprove = async (user) => {
      ElMessageBox.confirm('确定要审批通过此用户吗？', '确认审批', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用API审批用户
          const response = await approveUser(user.id)
          
          if (response.code === 200) {
            ElMessage.success('用户审批通过')
            loadUserList()
          } else {
            ElMessage.error(response.message || '审批失败')
          }
        } catch (error) {
          ElMessage.error('审批失败，请稍后重试')
          console.error('审批用户错误:', error)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 拒绝用户申请
    const handleReject = async (user) => {
      ElMessageBox.confirm('确定要拒绝此用户申请吗？', '确认拒绝', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用API拒绝用户申请
          const response = await rejectUser(user.id)
          
          if (response.code === 200) {
            ElMessage.success('已拒绝用户申请')
            loadUserList()
          } else {
            ElMessage.error(response.message || '拒绝失败')
          }
        } catch (error) {
          ElMessage.error('拒绝失败，请稍后重试')
          console.error('拒绝用户错误:', error)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    const handleToggleStatus = async (user) => {
      const action = user.status === 1 ? '禁用' : '启用'
      const newStatus = user.status === 1 ? 0 : 1
      
      ElMessageBox.confirm(`确定要${action}此用户吗？`, `确认${action}`, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用API更新用户状态
          const response = await updateUserStatus(user.id, newStatus)
          
          if (response.code === 200) {
            ElMessage.success(`${action}成功`)
            loadUserList()
          } else {
            ElMessage.error(response.message || `${action}失败`)
          }
        } catch (error) {
          ElMessage.error(`${action}失败，请稍后重试`)
          console.error('更新用户状态错误:', error)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 重置用户密码
    const handleResetPassword = async (user) => {
      ElMessageBox.confirm('确定要重置此用户的密码吗？重置后密码将恢复为初始密码。', '确认重置密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用API重置用户密码
          const response = await resetUserPassword(user.id)
          
          if (response.code === 200) {
            ElMessage.success('密码重置成功')
          } else {
            ElMessage.error(response.message || '密码重置失败')
          }
        } catch (error) {
          ElMessage.error('密码重置失败，请稍后重试')
          console.error('重置用户密码错误:', error)
        }
      }).catch(() => {
            // 处理取消操作
          })
    }
    
    // 删除用户
    const handleDelete = async (user) => {
      ElMessageBox.confirm('确定要删除此用户吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用API删除用户
          const response = await deleteUser(user.id)
          
          if (response.code === 200) {
            ElMessage.success('删除成功')
            loadUserList()
          } else {
            ElMessage.error(response.message || '删除失败')
          }
        } catch (error) {
          ElMessage.error('删除失败，请稍后重试')
          console.error('删除用户错误:', error)
        }
      }).catch(() => {
            // 处理取消操作
            // ElMessage.info('删除取消')
          })
    }
    
    const handleSizeChange = (size) => {
      pagination.size = size
      loadUserList()
    }
    
    const handleCurrentChange = (current) => {
      pagination.current = current
      loadUserList()
    }
    
    onMounted(() => {
      loadUserList()
    })
    
    return {
      loading,
      userList,
      searchForm,
      pagination,
      getRoleName,
      getRoleTag,
      getGenderName,
      handleSearch,
      handleReset,
      handleEdit,
      handleSaveEdit,
      handleToggleStatus,
      handleDelete,
      handleApprove,
      handleReject,
      handleResetPassword,
      handleSizeChange,
      handleCurrentChange,
      editDialogVisible,
      editForm,
      editFormRef,
      editFormRules
    }
  }
}
</script>

<style scoped>
.user-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
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
