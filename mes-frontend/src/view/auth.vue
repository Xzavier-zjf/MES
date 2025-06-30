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

        <el-form :model="form" ref="formRef" label-position="top" size="large">
          <el-form-item label="账号">
            <el-input v-model="form.username" prefix-icon="User" placeholder="请输入账号" />
          </el-form-item>

          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" prefix-icon="Lock" placeholder="请输入密码" />
          </el-form-item>

          <el-form-item v-if="!isLogin" label="确认密码">
            <el-input v-model="form.confirmPassword" type="password" prefix-icon="Lock" placeholder="请再次输入密码" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleSubmit" round>{{ isLogin ? '登录' : '注册' }}</el-button>
        </el-form>

        <div class="form-footer">
          <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
          <span class="switch-link" @click="isLogin = !isLogin">{{ isLogin ? '立即注册' : '立即登录' }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth' // ✅ 导入刚写的接口方法

const router = useRouter()
const isLogin = ref(true)
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
})

const handleSubmit = async () => {
  try {
    if (isLogin.value) {
      const token = await login(form.value.username, form.value.password)
      localStorage.setItem('token', token) // ✅ 保存 JWT
      ElMessage.success('登录成功，正在跳转首页...')
      setTimeout(() => router.push('/home'), 1000)
    } else {
      if (form.value.password !== form.value.confirmPassword) {
        ElMessage.error('两次密码不一致')
        return
      }
      await register({
        username: form.value.username,
        password: form.value.password
      })
      ElMessage.success('注册成功，请登录')
      isLogin.value = true
    }
  } catch (err) {
    ElMessage.error(err.message || '请求失败')
  }
}
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
}
</style>
