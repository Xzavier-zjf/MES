import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from '@/stores'
import Home from '../view/Home.vue'
import PlanManager from '../view/PlanManager.vue'
import TaskManager from '../view/TaskManager.vue'
import DeviceManager from '../view/DeviceMonitor.vue' 
import InjectionTaskPage from '../view/InjectionTaskPage.vue'// 新增导入
import PatternManager from '../view/PatternManager.vue'
import Login from '../view/auth.vue'

const routes = [
  { path: '/', redirect: '/login' },              // 默认跳转到首页
  { path: '/login', component: Login },            // 登录页面
  { path: '/home', component: Home },            // 首页
  { path: '/plan', component: PlanManager },     // 生产计划管理页面
  { path: '/task', component: TaskManager },     // 任务管理页面
   { path: '/device', component: DeviceManager },   // 设备管理页面
    { path: '/injection', component: InjectionTaskPage },   // 注塑任务管理页面
    { path: '/pattern', component: PatternManager }  //  新增任务管理页面
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, from) => {
  if (to.path === '/home') {
    const appStore = useAppStore()
    await appStore.fetchAllData()
  }
})

export default router