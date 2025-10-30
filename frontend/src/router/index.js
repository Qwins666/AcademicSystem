import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/activity/ActivityList.vue'),
        meta: { title: '学术活动', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activity/ActivityDetail.vue'),
        meta: { title: '活动详情', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'activities/create',
        name: 'ActivityCreate',
        component: () => import('@/views/activity/ActivityForm.vue'),
        meta: { title: '发布活动', roles: ['TEACHER', 'ADMIN'] }
      },
      {    
        path: 'activities/:id/edit',
        name: 'ActivityEdit',
        component: () => import('@/views/activity/ActivityForm.vue'),
        meta: { title: '编辑活动', roles: ['TEACHER', 'ADMIN'] }
      },
      {    
        path: 'activities/:activityId/certificate',
        name: 'CertificateView',
        component: () => import('@/views/activity/CertificateView.vue'),
        meta: { title: '电子参赛证明', roles: ['STUDENT'] }
      },
      {
        path: 'achievements',
        name: 'Achievements',
        component: () => import('@/views/achievement/AchievementList.vue'),
        meta: { title: '成果申报', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'achievements/create',
        name: 'AchievementCreate',
        component: () => import('@/views/achievement/AchievementForm.vue'),
        meta: { title: '申报成果', roles: ['STUDENT'] }
      },
      {        
        path: 'achievements/:id/edit',
        name: 'AchievementEdit',
        component: () => import('@/views/achievement/AchievementForm.vue'),
        meta: { title: '编辑成果', roles: ['STUDENT'] }
      },
      {        
        path: 'achievements/:id',
        name: 'AchievementDetail',
        component: () => import('@/views/achievement/AchievementDetail.vue'),
        meta: { title: '成果详情', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '科研项目', roles: ['TEACHER', 'ADMIN'] }
      },
      {
        path: 'projects/create',
        name: 'ProjectCreate',
        component: () => import('@/views/project/ProjectForm.vue'),
        meta: { title: '创建项目', roles: ['TEACHER', 'ADMIN'] }
      },
      {
        path: 'projects/:id/edit',
        name: 'ProjectEdit',
        component: () => import('@/views/project/ProjectForm.vue'),
        meta: { title: '编辑项目', roles: ['TEACHER', 'ADMIN'] }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue'),
        meta: { title: '项目详情', roles: ['STUDENT', 'TEACHER', 'ADMIN'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '数据统计', roles: ['ADMIN'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = store.getters.token
  const userRole = store.getters.userRole
  
  // 检查是否需要认证
  if (to.meta.requiresAuth !== false) {
    if (!token) {
      next('/login')
      return
    }
  }
  
  // 检查角色权限
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
