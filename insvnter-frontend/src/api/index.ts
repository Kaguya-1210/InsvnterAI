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

// 响应拦截器：提取 data, 处理 401
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('insvnter_token')
            localStorage.removeItem('insvnter_user')
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
    login: (data: { username: string; password: string; captcha: string; captchaId: string }) =>
        api.post('/auth/login', data) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    register: (data: { username: string; email: string; password: string; captcha: string; captchaId: string }) =>
        api.post('/auth/register', data) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    logout: () => api.post('/auth/logout'),
    me: () => api.get('/auth/me') as Promise<{ code: number; data: { username: string; email: string; role: string; createdAt: string } }>,

    // 个人信息修改
    updateUsername: (username: string) =>
        api.put('/auth/profile/username', { username }) as Promise<{ code: number; message: string; data: { token: string; username: string; email: string; role: string } }>,
    updatePassword: (oldPassword: string, newPassword: string) =>
        api.put('/auth/profile/password', { oldPassword, newPassword }) as Promise<{ code: number; message: string }>,
}

// ========== Admin API ==========
export const adminApi = {
    // 仪表盘
    getDashboard: () =>
        api.get('/admin/dashboard') as Promise<{
            code: number; data: {
                totalUsers: number; todayNewUsers: number; activeUsers: number;
                disabledUsers: number; adminCount: number; userCount: number
            }
        }>,

    // 用户管理
    getUsers: (params: { page?: number; size?: number; keyword?: string; role?: string }) =>
        api.get('/admin/users', { params }) as Promise<{
            code: number; data: {
                content: Array<{
                    id: number; username: string; email: string; role: string;
                    enabled: boolean; lastLoginAt: string | null; createdAt: string
                }>;
                totalElements: number; totalPages: number; number: number; size: number
            }
        }>,

    updateUserRole: (id: number, role: string) =>
        api.put(`/admin/users/${id}/role`, { role }) as Promise<{ code: number; message: string }>,

    updateUserStatus: (id: number, enabled: boolean) =>
        api.put(`/admin/users/${id}/status`, { enabled }) as Promise<{ code: number; message: string }>,

    resetUserPassword: (id: number) =>
        api.post(`/admin/users/${id}/reset-password`) as Promise<{ code: number; data: { tempPassword: string } }>,

    deleteUser: (id: number) =>
        api.delete(`/admin/users/${id}`) as Promise<{ code: number; message: string }>,
}
