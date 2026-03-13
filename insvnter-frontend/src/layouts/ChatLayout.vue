<script setup lang="ts">
import { ref, computed, provide, watch } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import {
  NLayout, NLayoutSider, NIcon, NButton, NSpace, NAvatar, NText,
  NDropdown, NDrawer, NDrawerContent,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  MenuOutline, SettingsOutline, LogOutOutline, HomeOutline,
  PersonOutline, ChevronBackOutline, ChevronForwardOutline,
  GridOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { SunnyOutline, MoonOutline } from '@vicons/ionicons5'

const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()

// 侧栏状态
const leftOpen = ref(true)
const rightOpen = ref(false)

// 移动端
const isMobile = ref(window.innerWidth < 768)
const mobileDrawer = ref<'left' | 'right' | null>(null)

window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) mobileDrawer.value = null
})

function toggleLeft() {
  if (isMobile.value) {
    mobileDrawer.value = mobileDrawer.value === 'left' ? null : 'left'
  } else {
    leftOpen.value = !leftOpen.value
  }
}

function toggleRight() {
  if (isMobile.value) {
    mobileDrawer.value = mobileDrawer.value === 'right' ? null : 'right'
  } else {
    rightOpen.value = !rightOpen.value
  }
}

// 提供给子组件
provide('toggleLeft', toggleLeft)
provide('toggleRight', toggleRight)
provide('isMobile', isMobile)

// 用户菜单
function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

import { h } from 'vue'

const userMenuOptions = [
  { label: '管理控制台', key: 'console', icon: renderIcon(GridOutline), show: computed(() => auth.user?.role === 'ADMIN') },
  { label: '返回首页', key: 'home', icon: renderIcon(HomeOutline) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) },
]

async function handleUserMenu(key: string) {
  if (key === 'console') router.push('/console/manage')
  else if (key === 'home') router.push('/')
  else if (key === 'logout') { await auth.logout(); router.push('/') }
}
</script>

<template>
  <div class="chat-layout" :class="{ 'mobile': isMobile }">
    <!-- 顶栏 -->
    <header class="chat-header">
      <div class="header-left">
        <NButton quaternary circle size="small" @click="toggleLeft" class="panel-toggle">
          <template #icon><NIcon :component="MenuOutline" :size="20" /></template>
        </NButton>
        <div class="header-brand">
          <span class="brand-icon">✦</span>
          <span class="brand-text">InsvnterAI</span>
        </div>
      </div>

      <div class="header-center">
        <NText class="chat-title">新对话</NText>
      </div>

      <div class="header-right">
        <NButton quaternary circle size="small" @click="theme.toggle()" class="panel-toggle">
          <template #icon>
            <NIcon :component="theme.isDark ? SunnyOutline : MoonOutline" :size="18" />
          </template>
        </NButton>
        <NButton quaternary circle size="small" @click="toggleRight" class="panel-toggle">
          <template #icon><NIcon :component="SettingsOutline" :size="20" /></template>
        </NButton>
        <NDropdown :options="userMenuOptions" trigger="click" @select="handleUserMenu">
          <NButton quaternary size="small" class="user-btn">
            <NSpace align="center" :size="6">
              <NAvatar round :size="26" color="#6366f1" style="font-size: 11px; font-weight: 600;">
                {{ auth.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </NAvatar>
              <span v-if="!isMobile" class="username">{{ auth.user?.username }}</span>
            </NSpace>
          </NButton>
        </NDropdown>
      </div>
    </header>

    <!-- 主体 -->
    <div class="chat-body">
      <!-- 桌面端左侧栏 -->
      <aside v-if="!isMobile" v-show="leftOpen" class="side-panel left-panel">
        <slot name="sidebar" />
      </aside>

      <!-- 中央聊天区 -->
      <main class="center-panel">
        <RouterView />
      </main>

      <!-- 桌面端右侧栏 -->
      <aside v-if="!isMobile" v-show="rightOpen" class="side-panel right-panel">
        <slot name="settings" />
      </aside>
    </div>

    <!-- 移动端抽屉 - 左侧 -->
    <NDrawer v-model:show="mobileDrawer" :width="300" placement="left"
      :show="mobileDrawer === 'left'" @update:show="(v: boolean) => { if(!v) mobileDrawer = null }">
      <NDrawerContent title="角色 & 对话" :native-scrollbar="false">
        <slot name="sidebar" />
      </NDrawerContent>
    </NDrawer>

    <!-- 移动端抽屉 - 右侧 -->
    <NDrawer :width="320" placement="right"
      :show="mobileDrawer === 'right'" @update:show="(v: boolean) => { if(!v) mobileDrawer = null }">
      <NDrawerContent title="设置" :native-scrollbar="false">
        <slot name="settings" />
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<style scoped>
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* ========== 顶栏 ========== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 12px;
  flex-shrink: 0;
  z-index: 10;
}

[data-theme="dark"] .chat-header {
  background: rgba(15, 15, 22, 0.95);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .chat-header {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(12px);
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-center {
  flex: 1;
  text-align: center;
}

.chat-title {
  font-weight: 600;
  font-size: 15px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}

.brand-icon {
  font-size: 18px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.username {
  font-size: 13px;
  font-weight: 500;
}

[data-theme="dark"] .username { color: #d4d4d8; }
[data-theme="light"] .username { color: #3f3f46; }

/* ========== 主体 ========== */
.chat-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ========== 侧面板 ========== */
.side-panel {
  width: 280px;
  flex-shrink: 0;
  overflow-y: auto;
  transition: width 0.2s ease;
}

[data-theme="dark"] .left-panel {
  background: rgba(12, 12, 18, 0.98);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .left-panel {
  background: #fafafa;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

[data-theme="dark"] .right-panel {
  background: rgba(12, 12, 18, 0.98);
  border-left: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .right-panel {
  background: #fafafa;
  border-left: 1px solid rgba(0, 0, 0, 0.06);
}

/* ========== 中央 ========== */
.center-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

[data-theme="dark"] .center-panel {
  background: #0a0a0f;
}
[data-theme="light"] .center-panel {
  background: #ffffff;
}

/* ========== 移动端 ========== */
.mobile .header-brand .brand-text {
  display: none;
}

.panel-toggle {
  opacity: 0.7;
  transition: opacity 0.2s;
}
.panel-toggle:hover {
  opacity: 1;
}
</style>
