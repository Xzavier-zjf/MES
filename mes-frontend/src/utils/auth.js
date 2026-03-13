import { useAuthStore } from '@/stores/auth'

/**
 * 检查用户是否有指定权限
 * @param {string|Array} permissions 权限名称或权限数组
 * @returns {boolean}
 */
export function hasPermission(permissions) {
    const authStore = useAuthStore()

    if (!authStore.isAuthenticated) {
        return false
    }

    const user = authStore.userInfo
    if (!user || !user.permissions) {
        return false
    }

    const userPermissions = user.permissions

    if (typeof permissions === 'string') {
        return userPermissions.includes(permissions)
    }

    if (Array.isArray(permissions)) {
        return permissions.some(permission => userPermissions.includes(permission))
    }

    return false
}

/**
 * 检查用户是否有指定角色
 * @param {string|Array} roles 角色名称或角色数组
 * @returns {boolean}
 */
export function hasRole(roles) {
    const authStore = useAuthStore()

    if (!authStore.isAuthenticated) {
        return false
    }

    const user = authStore.userInfo
    if (!user || !user.roles) {
        return false
    }

    const userRoles = user.roles

    if (typeof roles === 'string') {
        return userRoles.includes(roles)
    }

    if (Array.isArray(roles)) {
        return roles.some(role => userRoles.includes(role))
    }

    return false
}

/**
 * 检查用户是否有任意一个指定权限
 * @param {Array} permissions 权限数组
 * @returns {boolean}
 */
export function hasAnyPermission(permissions) {
    return hasPermission(permissions)
}

/**
 * 检查用户是否拥有所有指定权限
 * @param {Array} permissions 权限数组
 * @returns {boolean}
 */
export function hasAllPermissions(permissions) {
    const authStore = useAuthStore()

    if (!authStore.isAuthenticated) {
        return false
    }

    const user = authStore.userInfo
    if (!user || !user.permissions) {
        return false
    }

    const userPermissions = user.permissions

    return permissions.every(permission => userPermissions.includes(permission))
}

/**
 * 获取当前用户信息
 * @returns {Object|null}
 */
export function getCurrentUser() {
    const authStore = useAuthStore()
    return authStore.userInfo
}

/**
 * 检查token是否有效
 * @returns {boolean}
 */
export function isTokenValid() {
    const authStore = useAuthStore()
    return authStore.checkTokenValidity()
}

/**
 * 安全退出登录
 */
export function safeLogout() {
    const authStore = useAuthStore()
    authStore.logout()

    // 清除所有可能的敏感数据
    sessionStorage.clear()

    // 重新加载页面确保状态完全清除
    window.location.reload()
}