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
        { path: '', name: 'dashboard', component: () => import('../views/admin/DashboardView.vue') },
        { path: 'users', name: 'user-manage', component: () => import('../views/admin/UserManageView.vue') },
        { path: 'settings', name: 'system-settings', component: () => import('../views/admin/SystemSettingsView.vue') },
        { path: 'profile', name: 'profile-settings', component: () => import('../views/admin/ProfileSettingsView.vue') },
        { path: 'email-config', name: 'email-config', component: () => import('../views/admin/EmailConfigView.vue') },
        { path: 'email-templates', name: 'email-templates', component: () => import('../views/admin/EmailTemplateView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('insvnter_token')
  if (!to.meta.requiresAuth) { next(); return }
  if (!token) { clearSession(); next({ name: 'landing' }); return }

  if (to.meta.requiresAdmin) {
    try {
      const res = await authApi.me()
      if (res.data.role !== 'ADMIN') { clearSession(); next({ name: 'landing' }); return }
      localStorage.setItem('insvnter_user', JSON.stringify(res.data))
    } catch {
      clearSession(); next({ name: 'landing' }); return
    }
  }
  next()
})

function clearSession() {
  localStorage.removeItem('insvnter_token')
  localStorage.removeItem('insvnter_user')
}

export default router
