<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { NConfigProvider, NMessageProvider, darkTheme, NNotificationProvider } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const naiveTheme = computed(() => (themeStore.isDark ? darkTheme : null))
</script>

<template>
  <NConfigProvider :theme="naiveTheme">
    <NMessageProvider>
      <NNotificationProvider>
        <RouterView />
      </NNotificationProvider>
    </NMessageProvider>
  </NConfigProvider>
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
