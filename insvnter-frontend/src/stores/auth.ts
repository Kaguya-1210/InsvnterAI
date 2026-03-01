import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface User {
    id: string
    username: string
    email: string
    avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null)
    const isLoggedIn = computed(() => !!user.value)

    // 初始化：从 localStorage 恢复
    const saved = localStorage.getItem('insvnter_user')
    if (saved) {
        try {
            user.value = JSON.parse(saved)
        } catch {
            localStorage.removeItem('insvnter_user')
        }
    }

    function login(username: string, _password: string) {
        // TODO: 对接后端 API
        const mockUser: User = {
            id: crypto.randomUUID(),
            username,
            email: `${username}@insvnter.ai`,
        }
        user.value = mockUser
        localStorage.setItem('insvnter_user', JSON.stringify(mockUser))
    }

    function register(username: string, email: string, _password: string) {
        // TODO: 对接后端 API
        const mockUser: User = {
            id: crypto.randomUUID(),
            username,
            email,
        }
        user.value = mockUser
        localStorage.setItem('insvnter_user', JSON.stringify(mockUser))
    }

    function logout() {
        user.value = null
        localStorage.removeItem('insvnter_user')
    }

    return { user, isLoggedIn, login, register, logout }
})
