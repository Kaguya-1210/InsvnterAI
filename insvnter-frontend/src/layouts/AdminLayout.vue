<script setup lang="ts">
import { ref, h, computed } from 'vue'
import { useRouter, useRoute, RouterView } from 'vue-router'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NIcon,
  NButton,
  NAvatar,
  NSpace,
  NText,
  NDropdown,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  GridOutline,
  HomeOutline,
  PeopleOutline,
  SettingsOutline,
  LogOutOutline,
  PersonCircleOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const collapsed = ref(false)

function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: '仪表盘',
    key: 'dashboard',
    icon: renderIcon(GridOutline),
  },
  {
    label: '用户管理',
    key: 'user-manage',
    icon: renderIcon(PeopleOutline),
  },
  {
    label: '系统设置',
    key: 'system-settings',
    icon: renderIcon(SettingsOutline),
  },
]

const activeKey = computed(() => {
  return route.name as string || 'dashboard'
})

function handleMenuUpdate(key: string) {
  switch (key) {
    case 'dashboard':
      router.push('/console/manage')
      break
    case 'user-manage':
      router.push('/console/manage/users')
      break
    case 'system-settings':
      router.push('/console/manage/settings')
      break
  }
}

const userMenuOptions = [
  { label: '返回首页', key: 'home', icon: renderIcon(HomeOutline) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) },
]

async function handleUserMenuSelect(key: string) {
  if (key === 'home') {
    router.push('/')
  } else if (key === 'logout') {
    await auth.logout()
    router.push('/')
  }
}
</script>

<template>
  <NLayout class="admin-layout" has-sider>
    <!-- Sidebar -->
    <NLayoutSider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      :native-scrollbar="false"
      class="admin-sider"
    >
      <div class="sider-header">
        <NAvatar
          round
          :size="collapsed ? 32 : 36"
          color="#6366f1"
          style="font-weight: 700; font-size: 14px;"
        >
          AI
        </NAvatar>
        <Transition name="fade">
          <span v-if="!collapsed" class="sider-title">InsvnterAI</span>
        </Transition>
      </div>

      <NMenu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuUpdate"
      />
    </NLayoutSider>

    <!-- Main Area -->
    <NLayout>
      <NLayoutHeader bordered class="admin-header">
        <NSpace align="center" justify="space-between" style="width: 100%; padding: 0 24px; height: 100%;">
          <NText strong style="font-size: 16px;">管理控制台</NText>
          <NDropdown
            :options="userMenuOptions"
            @select="handleUserMenuSelect"
            trigger="click"
          >
            <NButton text style="display: flex; align-items: center; gap: 8px;">
              <template #icon>
                <NIcon :component="PersonCircleOutline" :size="20" />
              </template>
              {{ auth.user?.username || 'Admin' }}
            </NButton>
          </NDropdown>
        </NSpace>
      </NLayoutHeader>

      <NLayoutContent
        class="admin-content"
        content-style="padding: 24px;"
        :native-scrollbar="false"
      >
        <RouterView />
      </NLayoutContent>
    </NLayout>
  </NLayout>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sider-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  margin-bottom: 8px;
  min-height: 64px;
}

.sider-title {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.admin-header {
  height: 56px;
  display: flex;
  align-items: center;
}

/* Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
