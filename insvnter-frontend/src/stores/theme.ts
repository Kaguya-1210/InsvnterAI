import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
    const isDark = ref(true)

    // 从 localStorage 恢复
    const saved = localStorage.getItem('insvnter_theme')
    if (saved !== null) {
        isDark.value = saved === 'dark'
    }

    watch(isDark, (val) => {
        localStorage.setItem('insvnter_theme', val ? 'dark' : 'light')
        document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
    }, { immediate: true })

    function toggle() {
        isDark.value = !isDark.value
    }

    return { isDark, toggle }
})
