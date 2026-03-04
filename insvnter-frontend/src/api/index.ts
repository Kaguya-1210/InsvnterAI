import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 请求拦截器：自动附加 JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('insvnter_token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error),
)

// 响应拦截器：提取 data, 处理 401/403
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        const status = error.response?.status
        if (status === 401 || status === 403) {
            localStorage.removeItem('insvnter_token')
            localStorage.removeItem('insvnter_user')
            // 仅当用户在管理后台时跳转首页
            if (window.location.pathname.startsWith('/console')) {
                window.location.href = '/'
            }
        }
        // 提取后端错误消息
        const message = error.response?.data?.message || error.message || '请求失败'
        return Promise.reject(new Error(message))
    },
)

export default api

// ========== Auth API ==========
export const authApi = {
    getCaptcha: () => api.get('/captcha') as Promise<{ code: number; data: { captchaId: string; image: string } }>,
    login: (data: { account: string; password: string; captcha: string; captchaId: string }) =>
        api.post('/auth/login', data) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    register: (data: { username: string; email: string; password: string; captcha: string; captchaId: string; emailCode: string }) =>
        api.post('/auth/register', data) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    logout: () => api.post('/auth/logout'),
    me: () => api.get('/auth/me') as Promise<{ code: number; data: { username: string; email: string; role: string; createdAt: string } }>,

    // 个人信息修改
    updateUsername: (username: string) =>
        api.put('/auth/profile/username', { username }) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    updatePassword: (oldPassword: string, newPassword: string) =>
        api.put('/auth/profile/password', { oldPassword, newPassword }) as Promise<{ code: number; message: string }>,
}

// ========== Email API ==========
export const emailApi = {
    sendCode: (email: string) =>
        api.post('/email/send-code', { email }) as Promise<{ code: number; message: string }>,
}

// ========== Admin API ==========
export const adminApi = {
    // 仪表盘
    getDashboard: () => api.get('/admin/dashboard') as Promise<{ code: number; data: Record<string, number> }>,
    // 用户列表
    getUsers: (params: { page?: number; size?: number; keyword?: string; role?: string }) =>
        api.get('/admin/users', { params }) as Promise<{ code: number; data: any }>,
    // 修改角色
    updateRole: (id: number, role: string) =>
        api.put(`/admin/users/${id}/role`, { role }) as Promise<{ code: number; message: string }>,
    // 修改状态
    updateStatus: (id: number, enabled: boolean) =>
        api.put(`/admin/users/${id}/status`, { enabled }) as Promise<{ code: number; message: string }>,
    // 重置密码
    resetPassword: (id: number) =>
        api.post(`/admin/users/${id}/reset-password`) as Promise<{ code: number; data: { tempPassword: string } }>,
    // 删除用户
    deleteUser: (id: number) =>
        api.delete(`/admin/users/${id}`) as Promise<{ code: number; message: string }>,
    // 邮件配置
    getEmailConfig: () =>
        api.get('/admin/email-config') as Promise<{ code: number; data: Record<string, any> }>,
}
