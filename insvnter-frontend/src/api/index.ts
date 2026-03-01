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
}
