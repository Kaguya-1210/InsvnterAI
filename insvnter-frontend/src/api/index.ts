import axios from 'axios'

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// 请求拦截器
api.interceptors.request.use(
    (config) => {
        // TODO: 添加 token 等认证信息
        return config
    },
    (error) => Promise.reject(error),
)

// 响应拦截器
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        // TODO: 统一错误处理
        console.error('API Error:', error)
        return Promise.reject(error)
    },
)

export default api
