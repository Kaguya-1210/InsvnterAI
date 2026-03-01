<script setup lang="ts">
import { ref, h } from 'vue'
import {
  NButton,
  NIcon,
  NAvatar,
  NDropdown,
  NSpace,
} from 'naive-ui'
import {
  SunnyOutline,
  MoonOutline,
  PersonCircleOutline,
  LogOutOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import AuthModal from './AuthModal.vue'

const auth = useAuthStore()
const theme = useThemeStore()
const showAuth = ref(false)
const authTab = ref<'login' | 'register'>('login')

function openAuth(tab: 'login' | 'register') {
  authTab.value = tab
  showAuth.value = true
}

const userMenuOptions = [
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, null, { default: () => h(LogOutOutline) }) },
]

function handleUserMenu(key: string) {
  if (key === 'logout') {
    auth.logout()
  }
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <div class="navbar-brand">
        <span class="brand-icon">✦</span>
        <span class="brand-text">InsvnterAI</span>
      </div>

      <!-- Right Area -->
      <NSpace align="center" :size="12">
        <!-- Theme Toggle -->
        <NButton
          quaternary
          circle
          @click="theme.toggle()"
          class="theme-btn"
        >
          <template #icon>
            <NIcon :component="theme.isDark ? SunnyOutline : MoonOutline" :size="18" />
          </template>
        </NButton>

        <!-- Auth Area -->
        <template v-if="auth.isLoggedIn">
          <NDropdown
            :options="userMenuOptions"
            trigger="click"
            @select="handleUserMenu"
          >
            <NButton quaternary class="user-btn">
              <NSpace align="center" :size="8">
                <NAvatar
                  round
                  :size="28"
                  color="#6366f1"
                  style="font-size: 12px; font-weight: 600;"
                >
                  {{ auth.user?.username?.charAt(0)?.toUpperCase() }}
                </NAvatar>
                <span class="username">{{ auth.user?.username }}</span>
              </NSpace>
            </NButton>
          </NDropdown>
        </template>
        <template v-else>
          <NButton
            size="small"
            quaternary
            @click="openAuth('login')"
          >
            登录
          </NButton>
          <NButton
            size="small"
            type="primary"
            round
            @click="openAuth('register')"
          >
            注册
          </NButton>
        </template>
      </NSpace>
    </div>

    <!-- Auth Modal -->
    <AuthModal v-model:show="showAuth" :initial-tab="authTab" />
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.navbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  cursor: default;
}

.brand-icon {
  font-size: 22px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

/* Dark theme defaults */
[data-theme="dark"] .navbar {
  background: rgba(10, 10, 15, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

[data-theme="dark"] .username {
  color: #e4e4e7;
}

/* Light theme */
[data-theme="light"] .navbar {
  background: rgba(255, 255, 255, 0.85);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

[data-theme="light"] .username {
  color: #18181b;
}
</style>
