<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter, RouterView } from 'vue-router'
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
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  GridOutline,
  HomeOutline,
} from '@vicons/ionicons5'

const router = useRouter()
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
]

function handleMenuUpdate(key: string) {
  switch (key) {
    case 'dashboard':
      router.push('/console/manage')
      break
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
        default-value="dashboard"
        @update:value="handleMenuUpdate"
      />
    </NLayoutSider>

    <!-- Main Area -->
    <NLayout>
      <NLayoutHeader bordered class="admin-header">
        <NSpace align="center" justify="space-between" style="width: 100%; padding: 0 24px; height: 100%;">
          <NText strong style="font-size: 16px;">控制台</NText>
          <NSpace align="center" :size="16">
            <NButton
              text
              @click="router.push('/')"
            >
              <template #icon>
                <NIcon :component="HomeOutline" />
              </template>
              返回首页
            </NButton>
          </NSpace>
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
