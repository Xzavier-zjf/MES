import { hasPermission, hasRole } from '@/utils/auth'

/**
 * 权限指令
 * 用法：v-permission="'user:create'" 或 v-permission="['user:create', 'user:update']"
 */
export const permission = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasPermission(value)
      if (!hasAuth) {
        // 隐藏元素而不是删除，避免影响布局
        el.style.display = 'none'
        el.setAttribute('data-permission-hidden', 'true')
      }
    } else {
      console.warn('权限指令需要指定权限值')
    }
  },
  updated(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasPermission(value)
      if (!hasAuth) {
        el.style.display = 'none'
        el.setAttribute('data-permission-hidden', 'true')
      } else {
        el.style.display = ''
        el.removeAttribute('data-permission-hidden')
      }
    }
  }
}

/**
 * 角色指令
 * 用法：v-role="'admin'" 或 v-role="['admin', 'manager']"
 */
export const role = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasRole(value)
      if (!hasAuth) {
        el.style.display = 'none'
        el.setAttribute('data-role-hidden', 'true')
      }
    } else {
      console.warn('角色指令需要指定角色值')
    }
  },
  updated(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasRole(value)
      if (!hasAuth) {
        el.style.display = 'none'
        el.setAttribute('data-role-hidden', 'true')
      } else {
        el.style.display = ''
        el.removeAttribute('data-role-hidden')
      }
    }
  }
}

/**
 * 权限或角色指令（满足其中一个即可）
 * 用法：v-auth="{ permissions: ['user:create'], roles: ['admin'] }"
 */
export const auth = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value) {
      const { permissions = [], roles = [] } = value
      const hasPermAuth = permissions.length === 0 || hasPermission(permissions)
      const hasRoleAuth = roles.length === 0 || hasRole(roles)
      
      // 只要满足权限或角色其中一个条件即可
      if (!hasPermAuth && !hasRoleAuth) {
        el.style.display = 'none'
        el.setAttribute('data-auth-hidden', 'true')
      }
    }
  },
  updated(el, binding) {
    const { value } = binding
    
    if (value) {
      const { permissions = [], roles = [] } = value
      const hasPermAuth = permissions.length === 0 || hasPermission(permissions)
      const hasRoleAuth = roles.length === 0 || hasRole(roles)
      
      if (!hasPermAuth && !hasRoleAuth) {
        el.style.display = 'none'
        el.setAttribute('data-auth-hidden', 'true')
      } else {
        el.style.display = ''
        el.removeAttribute('data-auth-hidden')
      }
    }
  }
}

/**
 * 禁用指令 - 当没有权限时禁用元素而不是隐藏
 * 用法：v-permission-disable="'user:create'"
 */
export const permissionDisable = {
  mounted(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasPermission(value)
      if (!hasAuth) {
        el.disabled = true
        el.classList.add('permission-disabled')
        el.title = '您没有权限执行此操作'
      }
    }
  },
  updated(el, binding) {
    const { value } = binding
    
    if (value) {
      const hasAuth = hasPermission(value)
      if (!hasAuth) {
        el.disabled = true
        el.classList.add('permission-disabled')
        el.title = '您没有权限执行此操作'
      } else {
        el.disabled = false
        el.classList.remove('permission-disabled')
        el.title = ''
      }
    }
  }
}