import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('insvnter_token')
        if (token) config.headers.Authorization = `Bearer ${token}`
        return config
    },
    (error) => Promise.reject(error),
)

api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        const status = error.response?.status
        if (status === 401 || status === 403) {
            localStorage.removeItem('insvnter_token')
            localStorage.removeItem('insvnter_user')
            if (window.location.pathname.startsWith('/console')) window.location.href = '/'
        }
        const message = error.response?.data?.message || error.message || '请求失败'
        return Promise.reject(new Error(message))
    },
)

export default api

// ========== Auth ==========
export const authApi = {
    getCaptcha: () => api.get('/captcha') as Promise<{ code: number; data: { captchaId: string; image: string } }>,
    login: (data: { account: string; password: string; captcha: string; captchaId: string }) =>
        api.post('/auth/login', data) as Promise<{ code: number; data: { token: string; username: string; email: string; role: string } }>,
    register: (data: { username: string; email: string; password: string; captcha: string; captchaId: string; emailCode: string }) =>
        api.post('/auth/register', data) as Promise<{ code: number; data: { token: string; username: string; email: string; role: string } }>,
    logout: () => api.post('/auth/logout'),
    me: () => api.get('/auth/me') as Promise<{ code: number; data: { username: string; email: string; role: string } }>,
    updateUsername: (username: string) =>
        api.put('/auth/profile/username', { username }) as Promise<{ code: number; data: { token: string; username: string; email: string; role: string } }>,
    updatePassword: (oldPassword: string, newPassword: string) =>
        api.put('/auth/profile/password', { oldPassword, newPassword }) as Promise<{ code: number; message: string }>,
}

// ========== Email ==========
export const emailApi = {
    sendCode: (email: string) => api.post('/email/send-code', { email }) as Promise<{ code: number; message: string }>,
}

// ========== Admin ==========
export const adminApi = {
    getDashboard: () => api.get('/admin/dashboard') as Promise<{ code: number; data: Record<string, number> }>,
    getUsers: (params: { page?: number; size?: number; keyword?: string; role?: string }) =>
        api.get('/admin/users', { params }) as Promise<{ code: number; data: any }>,
    updateUserRole: (id: number, role: string) => api.put(`/admin/users/${id}/role`, { role }) as Promise<{ code: number }>,
    updateUserStatus: (id: number, enabled: boolean) => api.put(`/admin/users/${id}/status`, { enabled }) as Promise<{ code: number }>,
    resetUserPassword: (id: number) => api.post(`/admin/users/${id}/reset-password`) as Promise<{ code: number; data: { tempPassword: string } }>,
    deleteUser: (id: number) => api.delete(`/admin/users/${id}`) as Promise<{ code: number }>,

    // 邮件配置
    getEmailConfig: () => api.get('/admin/email-config') as Promise<{ code: number; data: Record<string, any> }>,
    saveEmailConfig: (data: Record<string, string>) => api.put('/admin/email-config', data) as Promise<{ code: number }>,
    testEmailConfig: (email: string) => api.post('/admin/email-config/test', { email }) as Promise<{ code: number }>,

    // 邮件模板
    getEmailTemplates: () => api.get('/admin/email-templates') as Promise<{ code: number; data: any[] }>,
    getEmailTemplate: (id: string) => api.get(`/admin/email-templates/${id}`) as Promise<{ code: number; data: any }>,
    saveEmailTemplate: (id: string, data: Record<string, string>) => api.put(`/admin/email-templates/${id}`, data) as Promise<{ code: number }>,
    createEmailTemplate: (data: Record<string, string>) => api.post('/admin/email-templates', data) as Promise<{ code: number; data: any }>,
    deleteEmailTemplate: (id: string) => api.delete(`/admin/email-templates/${id}`) as Promise<{ code: number }>,
}
