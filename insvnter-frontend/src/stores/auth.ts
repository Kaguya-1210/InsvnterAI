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

    async function login(username: string, password: string, captcha: string, captchaId: string) {
        const res = await authApi.login({ username, password, captcha, captchaId })
        saveSession(res.data)
    }

    async function register(username: string, email: string, password: string, captcha: string, captchaId: string) {
        const res = await authApi.register({ username, email, password, captcha, captchaId })
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

    return { user, token, isLoggedIn, login, register, logout, updateSession }
})
