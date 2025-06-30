import { createRouter, createWebHistory } from 'vue-router'
import Home from '../view/Home.vue'
import PlanManager from '../view/PlanManager.vue'
import TaskManager from '../view/TaskManager.vue'
import DeviceManager from '../view/DeviceMonitor.vue' 
import InjectionTaskPage from '../view/InjectionTaskPage.vue'// ✅ 新增导入
import PatternManager from '../view/PatternManager.vue'

const routes = [
  { path: '/', redirect: '/home' },              // 默认跳转到首页
  {
    path: '/home',
    name: 'Home',
    component: Home
  },
  { path: '/plan', component: PlanManager },     // 生产计划管理
  { path: '/task', component: TaskManager }, 
   { path: '/device', component: DeviceManager }, 
    { path: '/injection', component: InjectionTaskPage }, 
    { path: '/pattern', component: PatternManager }  // ✅ 新增任务管理页面
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
