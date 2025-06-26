import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
// 引入 vue-echarts 组件
import VChart from 'vue-echarts'

const app = createApp(App)  // 先创建 app 实例

app.component('v-chart', VChart) // 注册全局组件

app.use(ElementPlus)
app.use(router)

app.mount('#app')
