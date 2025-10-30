<template>
  <div class="layout-container">
    <!-- 头部 -->
    <el-header class="layout-header">
      <div class="header-content">
        <div class="header-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="sidebar-toggle"
          >
            <el-icon><Menu /></el-icon>
          </el-button>
          <h1 class="system-title">学术活动与科研成果管理系统</h1>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar">
                {{ user?.realName ? user.realName.charAt(0) : 'U' }}
              </el-avatar>
              <span class="user-name">{{ user?.realName || user?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <div class="layout-main">
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="layout-sidebar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <!-- 学术活动 -->
          <el-sub-menu index="activities" v-if="hasPermission(['STUDENT', 'TEACHER', 'ADMIN'])">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>学术活动</span>
            </template>
            <el-menu-item index="/dashboard/activities">活动列表</el-menu-item>
            <el-menu-item 
              index="/dashboard/activities/create" 
              v-if="hasPermission(['TEACHER', 'ADMIN'])"
            >
              发布活动
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 成果申报 -->
          <el-sub-menu index="achievements" v-if="hasPermission(['STUDENT', 'TEACHER', 'ADMIN'])">
            <template #title>
              <el-icon><Trophy /></el-icon>
              <span>成果申报</span>
            </template>
            <el-menu-item index="/dashboard/achievements">成果列表</el-menu-item>
            <el-menu-item 
              index="/dashboard/achievements/create" 
              v-if="hasPermission(['STUDENT'])"
            >
              申报成果
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 科研项目 -->
          <el-sub-menu index="projects" v-if="hasPermission(['TEACHER', 'ADMIN'])">
            <template #title>
              <el-icon><FolderOpened /></el-icon>
              <span>科研项目</span>
            </template>
            <el-menu-item index="/dashboard/projects">项目列表</el-menu-item>
            <el-menu-item index="/dashboard/projects/create">创建项目</el-menu-item>
          </el-sub-menu>
          
          <!-- 数据统计 -->
          <el-menu-item 
            index="/dashboard/statistics" 
            v-if="hasPermission(['ADMIN'])"
          >
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          
          <!-- 用户管理 -->
          <el-menu-item 
            index="/dashboard/users" 
            v-if="hasPermission(['ADMIN'])"
          >
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Layout',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const isCollapse = ref(false)
    const user = computed(() => store.getters.user || {})
    const userRole = computed(() => store.getters.userRole || 'STUDENT')
    
    const sidebarWidth = computed(() => isCollapse.value ? '64px' : '200px')
    const activeMenu = computed(() => route.path)
    
    const toggleSidebar = () => {
      isCollapse.value = !isCollapse.value
    }
    
    // 检查用户是否拥有指定的任一角色权限
    const hasPermission = (requiredRoles) => {
      // 确保userRole.value存在，默认为'STUDENT'
      const currentRole = userRole.value || 'STUDENT'
      console.log('当前用户角色:', currentRole, '需要的角色:', requiredRoles)
      // 检查当前角色是否在需要的角色列表中
      return requiredRoles.includes(currentRole)
    }
    
    const handleCommand = (command) => {
      switch (command) {
        case 'profile':
          router.push('/dashboard/profile')
          break
        case 'logout':
          ElMessageBox.confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            store.dispatch('logout')
            router.push('/login')
            ElMessage.success('已退出登录')
          }).catch(() => {
            // 处理取消操作，避免未捕获的Promise拒绝
          })
          break
      }
    }
    
    onMounted(() => {
      // 检查登录状态
      if (!store.getters.isLoggedIn) {
        router.push('/login')
      }
    })
    
    return {
      isCollapse,
      sidebarWidth,
      activeMenu,
      user,
      userRole,
      toggleSidebar,
      hasPermission,
      handleCommand
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  margin-right: 20px;
  font-size: 18px;
}

.system-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #2c3e50;
}

.layout-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.layout-sidebar {
  background: #001529;
  transition: width 0.3s;
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.layout-content {
  background: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .layout-sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    height: calc(100vh - 60px);
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .layout-sidebar.show {
    transform: translateX(0);
  }
  
  .layout-content {
    padding: 10px;
  }
}
</style>
