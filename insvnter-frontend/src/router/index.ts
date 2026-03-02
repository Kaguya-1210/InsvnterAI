import { createRouter, createWebHistory } from 'vue-router'

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
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('insvnter_token')
  const userStr = localStorage.getItem('insvnter_user')

  if (to.meta.requiresAuth && !token) {
    // 未登录，跳转首页
    next({ name: 'landing' })
    return
  }

  if (to.meta.requiresAdmin && userStr) {
    try {
      const user = JSON.parse(userStr)
      if (user.role !== 'ADMIN') {
        // 非管理员，跳转首页
        next({ name: 'landing' })
        return
      }
    } catch {
      next({ name: 'landing' })
      return
    }
  }

  next()
})

export default router
