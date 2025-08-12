<template>
  <div v-if="isLoading" class="auth-loading">
    <el-loading-service />
  </div>
  <div v-else-if="hasPermission">
    <slot />
  </div>
  <div v-else class="auth-denied">
    <el-result
      icon="warning"
      title="访问被拒绝"
      sub-title="您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">回到首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  // 需要的权限列表
  permissions: {
    type: Array,
    default: () => []
  },
  // 需要的角色列表
  roles: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()
const authStore = useAuthStore()
const isLoading = ref(true)

// 检查是否有权限
const hasPermission = computed(() => {
  if (!authStore.isAuthenticated) {
    return false
  }
  
  // 如果没有指定权限要求，则默认通过
  if (props.permissions.length === 0 && props.roles.length === 0) {
    return true
  }
  
  const user = authStore.userInfo
  if (!user) return false
  
  // 检查角色权限
  if (props.roles.length > 0) {
    const userRoles = user.roles || []
    const hasRole = props.roles.some(role => userRoles.includes(role))
    if (!hasRole) return false
  }
  
  // 检查具体权限
  if (props.permissions.length > 0) {
    const userPermissions = user.permissions || []
    const hasPermission = props.permissions.some(permission => 
      userPermissions.includes(permission)
    )
    if (!hasPermission) return false
  }
  
  return true
})

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/home')
}

onMounted(() => {
  // 模拟权限检查延迟
  setTimeout(() => {
    isLoading.value = false
  }, 100)
})
</script>

<style scoped>
.auth-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.auth-denied {
  padding: 40px;
}
</style>