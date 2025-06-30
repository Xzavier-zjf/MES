import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { 
    path: '/', 
    redirect: '/auth'  // 默认重定向到认证页
  },
  { 
    path: '/auth', 
    component: () => import('../view/auth.vue'),  // 使用动态导入
    meta: { public: true }  // 标记为公开路由
  },
  { 
    path: '/home', 
    component: () => import('../view/Home.vue'),
    meta: { requiresAuth: true }  // 需要认证
  },
  { 
    path: '/plan', 
    component: () => import('../view/PlanManager.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/task', 
    component: () => import('../view/TaskManager.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/device', 
    component: () => import('../view/DeviceMonitor.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/injection', 
    component: () => import('../view/InjectionTaskPage.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/pattern', 
    component: () => import('../view/PatternManager.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 检查路由是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const token = localStorage.getItem('token')
  
  if (requiresAuth && !token) {
    // 保存原始目标路径，登录后可以重定向回去
    next({
      path: '/auth',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router