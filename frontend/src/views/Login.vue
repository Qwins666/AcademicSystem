<template>
  <div class="login-container">
    <!-- 装饰元素 -->
    <div class="login-decoration decor-1"></div>
    <div class="login-decoration decor-2"></div>
    <div class="login-decoration decor-3"></div>
    
    <div class="login-form">
      <!-- 系统图标 -->
      <div class="login-logo">
        <div class="logo-circle">
          <span class="logo-icon">🎓</span>
        </div>
      </div>
      
      <div class="login-header">
        <h1>学术活动与科研成果管理系统</h1>
        <p>Academic Activity & Research Achievement Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form-content"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username" class="form-item-custom">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            class="custom-input"
            :validate-event="false"
          />
        </el-form-item>
        
        <el-form-item prop="password" class="form-item-custom">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
            class="custom-input"
            :validate-event="false"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
            :class="{ 'button-loading': loading }"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        
        <div class="login-footer">
          <el-link type="primary" @click="$router.push('/register')" class="register-link">
            还没有账号？立即注册
          </el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    const loginFormRef = ref()
    const loading = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            const response = await login(loginForm)
            if (response.code === 200) {
              const { token, user } = response.data
              // 确保正确获取用户角色，如果roles数组不存在或为空，则使用默认角色
              const role = user.roles && user.roles.length > 0 ? user.roles[0] : 'STUDENT'
              console.log('用户角色:', role)
              
              store.dispatch('login', { token, user, role })
              ElMessage.success('登录成功')
              router.push('/dashboard')
            } else {
              ElMessage.error(response.message || '登录失败')
            }
          } catch (error) {
            ElMessage.error('登录失败，请检查用户名和密码')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
/* 全局容器样式 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('@/assets/background.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 装饰元素 */
.login-decoration {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
  z-index: 0;
}

.decor-1 {
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.2);
  top: -100px;
  left: -100px;
  animation: float 15s ease-in-out infinite;
}

.decor-2 {
  width: 400px;
  height: 400px;
  background: rgba(102, 126, 234, 0.3);
  bottom: -200px;
  right: -100px;
  animation: float 20s ease-in-out infinite reverse;
}

.decor-3 {
  width: 250px;
  height: 250px;
  background: rgba(118, 75, 162, 0.25);
  top: 50%;
  right: 10%;
  animation: float 18s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, 30px); }
}

/* 登录表单卡片 */
.login-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-form:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(255, 255, 255, 0.3);
}

/* 系统图标 */
.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.logo-icon {
  font-size: 36px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10px); }
  60% { transform: translateY(-5px); }
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #2c3e50;
  font-size: 26px;
  margin-bottom: 10px;
  font-weight: 700;
  background: linear-gradient(135deg, #2c3e50 0%, #3498db 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* 表单内容 */
.login-form-content {
  margin-top: 25px;
}

/* 表单项 */
.form-item-custom {
  position: relative;
  margin-bottom: 25px;
}

/* 自定义输入框 */
.custom-input {
  border-radius: 12px;
  height: 50px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e9ecef;
  font-size: 15px;
}

.custom-input:hover {
  border-color: #d5e0f0;
}

.custom-input:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.custom-input .el-input__inner {
  height: 100%;
  border: none;
  box-shadow: none !important;
  background: transparent;
  padding: 0 15px;
}

.custom-input .el-input__prefix {
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  transition: color 0.3s ease;
}

.custom-input:focus-within .el-input__prefix {
  color: #667eea;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transition: all 0.4s ease;
  z-index: -1;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-button:hover::before {
  left: 0;
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.4);
}

.button-loading {
  opacity: 0.8;
  transform: scale(0.98);
}

/* 底部链接 */
.login-footer {
  text-align: center;
  margin-top: 25px;
}

.register-link {
  font-weight: 500;
  font-size: 14px;
  position: relative;
  transition: all 0.3s ease;
  color: #667eea !important;
}

.register-link:hover {
  color: #764ba2 !important;
}

.register-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transform-origin: right;
  transition: transform 0.3s ease;
}

.register-link:hover::after {
  transform: scaleX(1);
  transform-origin: left;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 15px;
  }
  
  .login-form {
    padding: 30px 20px;
    margin: 10px;
  }
  
  .login-header h1 {
    font-size: 22px;
  }
  
  .login-header p {
    font-size: 12px;
  }
  
  .logo-circle {
    width: 60px;
    height: 60px;
  }
  
  .logo-icon {
    font-size: 28px;
  }
  
  .custom-input,
  .login-button {
    height: 45px;
  }
  
  .decor-1, .decor-2, .decor-3 {
    transform: scale(0.7);
  }
}
</style>
