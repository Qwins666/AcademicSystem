<template>
  <div class="profile">
    <div class="page-header">
      <h1>个人中心</h1>
    </div>

    <div class="profile-content">
      <el-row :gutter="20">
        <!-- 个人信息 -->
        <el-col :span="16">
          <div class="profile-card">
            <h3>个人信息</h3>
            <el-form
              ref="formRef"
              :model="userForm"
              :rules="rules"
              label-width="120px"
              @submit.prevent="handleUpdate"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="userForm.realName" />
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="userForm.email" />
              </el-form-item>
              
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="userForm.phone" />
              </el-form-item>
              
              <el-form-item label="学号/工号" prop="studentId">
                <el-input v-model="userForm.studentId" />
              </el-form-item>
              
              <el-form-item label="院系" prop="department">
                <el-input v-model="userForm.department" />
              </el-form-item>
              
              <el-form-item label="专业" prop="major">
                <el-input v-model="userForm.major" />
              </el-form-item>
              
              <el-form-item label="年级" prop="grade">
                <el-input v-model="userForm.grade" />
              </el-form-item>
              
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="userForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" :loading="loading" @click="handleUpdate">
                  更新信息
                </el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
        
        <!-- 头像和统计 -->
        <el-col :span="8">
          <div class="avatar-card">
            <div class="avatar-section">
              <el-avatar :size="120" :src="userForm.avatar">
                {{ userForm.realName ? userForm.realName.charAt(0) : 'U' }}
              </el-avatar>
              <div class="user-info">
                <h3>{{ userForm.realName || userForm.username }}</h3>
                <p>{{ getRoleName(userRole) }}</p>
              </div>
            </div>
            
            <div class="stats-section">
              <h4>我的统计</h4>
              <div class="stat-item">
                <span class="stat-label">参与活动</span>
                <span class="stat-value">{{ userStats.activities }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">申报成果</span>
                <span class="stat-value">{{ userStats.achievements }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">参与项目</span>
                <span class="stat-value">{{ userStats.projects }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 密码修改 -->
      <div class="password-card">
        <h3>修改密码</h3>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="120px"
          @submit.prevent="handleChangePassword"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
              修改密码
            </el-button>
            <el-button @click="handleResetPassword">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { updateProfile, changePassword } from '@/api/auth'
import router from '@/router'

export default {
  name: 'Profile',
  setup() {
    const store = useStore()
    
    const formRef = ref()
    const passwordFormRef = ref()
    const loading = ref(false)
    const passwordLoading = ref(false)
    
    const user = computed(() => store.getters.user)
    const userRole = computed(() => store.getters.userRole)
    
    const userForm = reactive({
      username: '',
      realName: '',
      email: '',
      phone: '',
      studentId: '',
      department: '',
      major: '',
      grade: '',
      gender: 1,
      avatar: ''
    })
    
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const userStats = reactive({
      activities: 5,
      achievements: 3,
      projects: 2
    })
    
    const rules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ]
    }
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    }
    
    const getRoleName = (role) => {
      const roleMap = {
        'STUDENT': '学生',
        'TEACHER': '教师',
        'ADMIN': '管理员'
      }
      return roleMap[role] || role
    }
    
    const loadUserData = () => {
      if (user.value) {
        Object.assign(userForm, {
          username: user.value.username || '',
          realName: user.value.realName || '',
          email: user.value.email || '',
          phone: user.value.phone || '',
          studentId: user.value.studentId || '',
          department: user.value.department || '',
          major: user.value.major || '',
          grade: user.value.grade || '',
          gender: user.value.gender || 1,
          avatar: user.value.avatar || ''
        })
      }
    }
    
    const handleUpdate = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            // 调用真实API更新用户信息（使用auth模块的updateProfile，不需要用户ID）
            await updateProfile(userForm)
            
            // 更新store中的用户信息
            store.dispatch('updateUser', userForm)
            ElMessage.success('信息更新成功')
          } catch (error) {
            ElMessage.error('更新失败: ' + (error.response?.data?.message || error.message))
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    const handleReset = () => {
      loadUserData()
    }
    
    const handleChangePassword = async () => {
      if (!passwordFormRef.value) return
      
      await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
          passwordLoading.value = true
          try {
            // 调用真实API修改密码
            await changePassword({
              oldPassword: passwordForm.oldPassword,
              newPassword: passwordForm.newPassword
            })
            ElMessage.success('密码修改成功')
            // 先重置表单，再进行路由跳转
            handleResetPassword()
            // 短暂延迟，确保用户看到成功提示
            setTimeout(() => {
              router.push('/login')
            }, 1000)
          } catch (error) {
            ElMessage.error('密码修改失败: ' + (error.response?.data?.message || error.message))
          } finally {
            passwordLoading.value = false
          }
        }
      })
    }
    
    const handleResetPassword = () => {
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields()
      }
    }
    
    onMounted(() => {
      loadUserData()
    })
    
    return {
      formRef,
      passwordFormRef,
      loading,
      passwordLoading,
      user,
      userRole,
      userForm,
      passwordForm,
      userStats,
      rules,
      passwordRules,
      getRoleName,
      handleUpdate,
      handleReset,
      handleChangePassword,
      handleResetPassword
    }
  }
}
</script>

<style scoped>
.profile {
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

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card,
.avatar-card,
.password-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.profile-card h3,
.password-card h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.user-info h3 {
  margin: 15px 0 5px 0;
  color: #2c3e50;
  font-size: 20px;
}

.user-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

.stats-section h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: #7f8c8d;
  font-size: 14px;
}

.stat-value {
  color: #409eff;
  font-size: 18px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .profile-card,
  .avatar-card,
  .password-card {
    padding: 20px;
  }
  
  .avatar-section {
    margin-bottom: 20px;
  }
}
</style>
