<template>
  <div class="register-container">
    <div class="register-form">
      <div class="register-header">
        <h1>用户注册</h1>
        <p>请填写以下信息完成注册</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form-content"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item label="学号/工号" prop="studentId">
          <el-input
            v-model="registerForm.studentId"
            placeholder="请输入学号或工号"
            prefix-icon="CreditCard"
          />
        </el-form-item>
        
        <el-form-item label="院系" prop="department">
          <el-input
            v-model="registerForm.department"
            placeholder="请输入院系"
            prefix-icon="OfficeBuilding"
          />
        </el-form-item>
        
        <el-form-item label="专业" prop="major">
          <el-input
            v-model="registerForm.major"
            placeholder="请输入专业"
            prefix-icon="Reading"
          />
        </el-form-item>
        
        <el-form-item label="年级" prop="grade">
          <el-select
            v-model="registerForm.grade"
            placeholder="请选择年级"
            style="width: 100%"
          >
            <el-option label="2021级" value="2021级" />
            <el-option label="2022级" value="2022级" />
            <el-option label="2023级" value="2023级" />
            <el-option label="2024级" value="2024级" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="registerForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择用户角色" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            class="register-button"
          >
            注册
          </el-button>
          <el-button @click="$router.push('/login')">
            返回登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

export default {
  name: 'Register',
  setup() {
    const router = useRouter()
    
    const registerFormRef = ref()
    const loading = ref(false)
    
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      realName: '',
      email: '',
      phone: '',
      studentId: '',
      department: '',
      major: '',
      grade: '',
      gender: 1,
      role: 'STUDENT' // 默认学生角色
    })
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
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
      ],
      studentId: [
        { required: true, message: '请输入学号或工号', trigger: 'blur' }
      ],
      department: [
        { required: true, message: '请输入院系', trigger: 'blur' }
      ],
      gender: [
        { required: true, message: '请选择性别', trigger: 'change' }
      ],
      role: [
        { required: true, message: '请选择用户角色', trigger: 'change' }
      ]
    }
    
    const handleRegister = async () => {
      if (!registerFormRef.value) return
      
      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const response = await register(registerForm)
            if (response.code === 200) {
              ElMessage.success('注册成功，等待管理员审批')
              router.push('/login')
            } else {
              ElMessage.error(response.message || '注册失败')
            }
          } catch (error) {
            ElMessage.error('注册失败，请检查输入信息')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    return {
      registerFormRef,
      registerForm,
      registerRules,
      loading,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 10px;
  font-weight: 600;
}

.register-header p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.register-form-content {
  margin-top: 20px;
}

.register-button {
  width: 100px;
}

@media (max-width: 768px) {
  .register-form {
    padding: 30px 20px;
    max-width: 100%;
  }
  
  .register-header h1 {
    font-size: 20px;
  }
}
</style>
