import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api'

export interface User {
    username: string
    email: string
    role: string
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(null)
    const isLoggedIn = computed(() => !!token.value)

    // 初始化：从 localStorage 恢复
    const savedToken = localStorage.getItem('insvnter_token')
    const savedUser = localStorage.getItem('insvnter_user')
    if (savedToken && savedUser) {
        try {
            token.value = savedToken
            user.value = JSON.parse(savedUser)
        } catch {
            localStorage.removeItem('insvnter_token')
            localStorage.removeItem('insvnter_user')
        }
    }

    function saveSession(data: { token: string; username: string; email: string; role: string }) {
        token.value = data.token
        user.value = { username: data.username, email: data.email, role: data.role }
        localStorage.setItem('insvnter_token', data.token)
        localStorage.setItem('insvnter_user', JSON.stringify(user.value))
    }

    async function login(account: string, password: string, captcha: string, captchaId: string) {
        const res = await authApi.login({ account, password, captcha, captchaId })
        saveSession(res.data)
    }

    async function register(username: string, email: string, password: string, captcha: string, captchaId: string, emailCode: string) {
        const res = await authApi.register({ username, email, password, captcha, captchaId, emailCode })
        saveSession(res.data)
    }

    async function logout() {
        try {
            await authApi.logout()
        } catch {
            // ignore
        }
        token.value = null
        user.value = null
        localStorage.removeItem('insvnter_token')
        localStorage.removeItem('insvnter_user')
    }

    function updateSession(data: { token: string; username: string; email: string; role: string }) {
        saveSession(data)
    }

    /** 页面刷新时验证 token 是否仍然有效（用户被删除/禁用/密码被重置时会失效） */
    async function checkSession() {
        if (!token.value) return
        try {
            const res = await authApi.me()
            user.value = { username: res.data.username, email: res.data.email, role: res.data.role }
        } catch {
            // token 无效，清除本地会话
            token.value = null
            user.value = null
            localStorage.removeItem('insvnter_token')
            localStorage.removeItem('insvnter_user')
        }
    }

    return { user, token, isLoggedIn, login, register, logout, updateSession, checkSession }
})
