import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// 引入 vue-echarts 组件
import VChart from 'vue-echarts'
import { createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
// 引入权限指令
import { permission, role, auth } from '@/directives/permission'

const app = createApp(App)  // 先创建 app 实例

// 注册图标组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.component('v-chart', VChart) // 注册全局组件

// 注册权限指令
app.directive('permission', permission)
app.directive('role', role)
app.directive('auth', auth)

// 使用插件
app.use(ElementPlus)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 初始化认证状态
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')
