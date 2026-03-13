import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import Home from '../view/Home.vue'
import PlanManager from '../view/PlanManager.vue'
import TaskManager from '../view/TaskManager.vue'
import DeviceManager from '../view/DeviceMonitor.vue' 
import InjectionTaskPage from '../view/InjectionTaskPage.vue'
import PatternManager from '../view/PatternManager.vue'
import Login from '../view/auth.vue'

const routes = [
  { 
    path: '/', 
    redirect: (to) => {
      // 根据认证状态决定重定向目标
      const authStore = useAuthStore()
      return authStore.isAuthenticated ? '/home' : '/login'
    }
  },
  { 
    path: '/login', 
    component: Login,
    meta: { 
      requiresAuth: false,
      title: '用户登录'
    }
  },
  { 
    path: '/home', 
    component: Home,
    meta: { 
      requiresAuth: true,
      title: '系统首页'
    }
  },
  { 
    path: '/plan', 
    component: PlanManager,
    meta: { 
      requiresAuth: true,
      title: '生产计划管理'
    }
  },
  { 
    path: '/task', 
    component: TaskManager,
    meta: { 
      requiresAuth: true,
      title: '任务管理'
    }
  },
  { 
    path: '/device', 
    component: DeviceManager,
    meta: { 
      requiresAuth: true,
      title: '设备监控'
    }
  },
  { 
    path: '/injection', 
    component: InjectionTaskPage,
    meta: { 
      requiresAuth: true,
      title: '注塑任务管理'
    }
  },
  { 
    path: '/pattern', 
    component: PatternManager,
    meta: { 
      requiresAuth: true,
      title: '模式管理'
    }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局前置守卫 - 权限控制核心
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 初始化认证状态
  authStore.initAuth()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - MES智能生产系统`
  }
  
  // 检查路由是否需要认证
  const requiresAuth = to.meta.requiresAuth !== false // 默认需要认证
  
  if (requiresAuth) {
    // 需要认证的页面
    if (!authStore.isAuthenticated) {
      ElMessage.warning('请先登录后再访问')
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存原始访问路径
      })
      return
    }
    
    // 验证token有效性
    if (!authStore.checkTokenValidity()) {
      ElMessage.error('登录已过期，请重新登录')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  } else {
    // 不需要认证的页面（如登录页）
    if (authStore.isAuthenticated && to.path === '/login') {
      // 已登录用户访问登录页，重定向到首页
      next('/home')
      return
    }
  }
  
  // 特殊处理：访问首页时获取数据
  if (to.path === '/home' && authStore.isAuthenticated) {
    try {
      const appStore = useAppStore()
      await appStore.fetchAllData()
    } catch (error) {
      console.error('获取首页数据失败:', error)
      ElMessage.error('获取数据失败，请刷新页面重试')
    }
  }
  
  next()
})

// 全局后置守卫 - 页面加载完成后的处理
router.afterEach((to, from) => {
  // 可以在这里添加页面访问日志、统计等
  console.log(`页面跳转: ${from.path} -> ${to.path}`)
})

export default router