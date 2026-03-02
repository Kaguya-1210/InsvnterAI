import { createRouter, createWebHistory } from 'vue-router'
import { authApi } from '@/api'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: () => import('../views/LandingPage.vue'),
    },
    {
      path: '/console/manage',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('../views/admin/DashboardView.vue'),
        },
        {
          path: 'users',
          name: 'user-manage',
          component: () => import('../views/admin/UserManageView.vue'),
        },
        {
          path: 'settings',
          name: 'system-settings',
          component: () => import('../views/admin/SystemSettingsView.vue'),
        },
        {
          path: 'profile',
          name: 'profile-settings',
          component: () => import('../views/admin/ProfileSettingsView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('insvnter_token')
  const userStr = localStorage.getItem('insvnter_user')

  // 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // 需要认证但无 token
  if (!token) {
    clearSession()
    next({ name: 'landing' })
    return
  }

  // 需要管理员权限：向后端验证 token 有效性和角色
  if (to.meta.requiresAdmin) {
    try {
      const res = await authApi.me()
      const user = res.data
      // 服务端确认角色是 ADMIN
      if (user.role !== 'ADMIN') {
        clearSession()
        next({ name: 'landing' })
        return
      }
      // 同步最新的用户信息到 localStorage
      localStorage.setItem('insvnter_user', JSON.stringify(user))
    } catch {
      // token 无效或用户已被删除 → 清除并跳走
      clearSession()
      next({ name: 'landing' })
      return
    }
  }

  next()
})

function clearSession() {
  localStorage.removeItem('insvnter_token')
  localStorage.removeItem('insvnter_user')
}

export default router
