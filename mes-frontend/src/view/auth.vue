<template>
  <div class="container">
    <!-- 左侧品牌介绍 -->
    <div class="left-panel">
      <div class="logo-container">
        <div class="logo">
          <div class="logo-inner">MES</div>
        </div>
        <div class="brand-text">
          <div class="brand-name">智能生产系统</div>
          <div class="brand-sub">Manufacturing Execution System</div>
        </div>
      </div>

      <h1 class="title">一站式生产流程<br />数字化管理平台</h1>
      <p class="description">
        提升效率，掌控每一个生产细节<br />
        智能制造 · 数字孪生 · 实时监控
      </p>
    </div>

    <!-- 右侧登录注册 -->
    <div class="right-panel">
      <el-card class="form-card">
        <div class="form-header">
          <h2 class="form-title">{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
          <p class="form-subtitle">{{ isLogin ? '请登录您的账户以继续' : '注册新账户以开始使用系统' }}</p>
        </div>

        <el-form 
          :model="form" 
          :rules="rules"
          ref="formRef" 
          label-position="top" 
          size="large"
          @keyup.enter="handleSubmit"
        >
          <el-form-item label="账号" prop="username">
            <el-input 
              v-model="form.username" 
              prefix-icon="User" 
              placeholder="请输入账号"
              :disabled="loading"
              clearable
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              prefix-icon="Lock" 
              placeholder="请输入密码"
              :disabled="loading"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item v-if="!isLogin" label="确认密码" prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              prefix-icon="Lock" 
              placeholder="请再次输入密码"
              :disabled="loading"
              show-password
              clearable
            />
          </el-form-item>

          <el-button 
            type="primary" 
            class="submit-btn" 
            @click="handleSubmit" 
            :loading="loading"
            :disabled="loading"
            round
          >
            {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
          </el-button>
        </el-form>

        <div class="form-footer">
          <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
          <span class="switch-link" @click="switchMode" :class="{ disabled: loading }">
            {{ isLogin ? '立即注册' : '立即登录' }}
          </span>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { login, register } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isLogin = ref(true)
const loading = ref(false)
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
})

// 表单验证规则
const formRef = ref()
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== form.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    loading.value = true
    
    if (isLogin.value) {
      // 登录逻辑
      const response = await login(form.value.username, form.value.password)
      
      // 使用认证store管理登录状态
      authStore.login(response.token || response, response.user)
      
      ElMessage.success('登录成功！')
      
      // 获取重定向路径，默认跳转到首页
      const redirectPath = route.query.redirect || '/home'
      await router.push(redirectPath)
      
    } else {
      // 注册逻辑
      await register({
        username: form.value.username,
        password: form.value.password
      })
      
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
      
      // 清空确认密码字段
      form.value.confirmPassword = ''
    }
  } catch (error) {
    console.error('认证失败:', error)
    ElMessage.error(error.message || (isLogin.value ? '登录失败' : '注册失败'))
  } finally {
    loading.value = false
  }
}

// 切换登录/注册模式
const switchMode = () => {
  isLogin.value = !isLogin.value
  // 清空表单
  form.value = {
    username: '',
    password: '',
    confirmPassword: '',
  }
  // 清除验证状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 组件挂载时检查是否已登录
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/home')
  }
})
</script>


<style scoped>
.container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.left-panel {
  flex: 1.4;
  background: linear-gradient(135deg, #0f1c3f, #1d3a6c);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.logo {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24px;
}

.logo-inner {
  width: 70px;
  height: 70px;
  background: white;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: #1d3a6c;
  font-size: 28px;
}

.brand-text .brand-name {
  font-size: 32px;
  font-weight: 800;
}
.brand-sub {
  font-size: 16px;
  opacity: 0.9;
  font-weight: 500;
}

.title {
  font-size: 42px;
  font-weight: 800;
  text-align: center;
  margin: 30px 0 20px;
  line-height: 1.5;
}

.description {
  font-size: 18px;
  line-height: 1.8;
  text-align: center;
  opacity: 0.95;
  font-weight: 400;
}

.right-panel {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f8fafc, #eef2f7);
  padding: 40px;
}

.form-card {
  width: 100%;
  max-width: 420px;
  padding: 35px;
  border-radius: 16px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #1d3a6c;
}

.form-subtitle {
  font-size: 14px;
  color: #64748b;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
}

.form-footer {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: #555;
}

.switch-link {
  color: #3a7bd5;
  font-weight: 600;
  margin-left: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.switch-link:hover {
  color: #2563eb;
}

.switch-link.disabled {
  color: #9ca3af;
  cursor: not-allowed;
}

.submit-btn {
  transition: all 0.3s ease;
}
</style>
