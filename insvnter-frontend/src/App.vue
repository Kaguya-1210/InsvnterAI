<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { NConfigProvider, NMessageProvider, NDialogProvider, darkTheme, NNotificationProvider } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'
import { useAuthStore } from '@/stores/auth'
import ChatTransition from '@/components/ChatTransition.vue'
import { usePageTransition } from '@/composables/usePageTransition'

const themeStore = useThemeStore()
const naiveTheme = computed(() => (themeStore.isDark ? darkTheme : null))

// 页面刷新时验证用户会话是否仍然有效
const authStore = useAuthStore()
authStore.checkSession()

// 全局转场动画
const { visible: transitionVisible, handlePreload, handleDone } = usePageTransition()
</script>

<template>
  <NConfigProvider :theme="naiveTheme">
    <NDialogProvider>
      <NMessageProvider>
        <NNotificationProvider>
          <RouterView />
        </NNotificationProvider>
      </NMessageProvider>
    </NDialogProvider>
  </NConfigProvider>

  <!-- 全局转场动画 — 独立于路由生命周期 -->
  <ChatTransition v-if="transitionVisible" @preload="handlePreload" @done="handleDone" />
</template>

<style>
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: background-color 0.3s ease, color 0.3s ease;
}

[data-theme="dark"] body {
  background: #0a0a0f;
  color: #e4e4e7;
}

[data-theme="light"] body {
  background: #ffffff;
  color: #18181b;
}
</style>
