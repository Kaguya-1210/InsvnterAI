<script setup lang="ts">
import { ref } from 'vue'
import {
  NCard, NGrid, NGi, NSpace, NText, NButton, NInput, NIcon,
  NForm, NFormItem, NDivider, NAvatar, useMessage,
} from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'

const message = useMessage()
const auth = useAuthStore()

// 用户名修改
const newUsername = ref(auth.user?.username || '')
const usernameLoading = ref(false)

async function handleUpdateUsername() {
  if (!newUsername.value.trim()) {
    message.error('用户名不能为空')
    return
  }
  if (newUsername.value.length < 2 || newUsername.value.length > 20) {
    message.error('用户名长度需在 2-20 之间')
    return
  }

  usernameLoading.value = true
  try {
    const res = await authApi.updateUsername(newUsername.value)
    // 更新本地状态和token
    auth.updateSession(res.data)
    message.success('用户名已更新')
  } catch (e: any) {
    message.error(e.message)
  } finally {
    usernameLoading.value = false
  }
}

// 密码修改
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const passwordLoading = ref(false)

async function handleUpdatePassword() {
  if (!oldPassword.value) {
    message.error('请输入当前密码')
    return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    message.error('新密码至少 6 位')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.error('两次输入的密码不一致')
    return
  }

  passwordLoading.value = true
  try {
    await authApi.updatePassword(oldPassword.value, newPassword.value)
    message.success('密码已更新')
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
  } catch (e: any) {
    message.error(e.message)
  } finally {
    passwordLoading.value = false
  }
}
</script>

<template>
  <div class="profile-settings">
    <h2 class="page-title">个人设置</h2>
    <p class="page-desc">管理你的账户信息和安全设置</p>

    <NGrid :cols="2" :x-gap="20" :y-gap="20" responsive="screen" item-responsive>
      <!-- 账户信息 -->
      <NGi span="2 m:1">
        <NCard class="profile-card">
          <template #header>
            <NSpace align="center" :size="8">
              <div class="section-icon">
                <NIcon :component="PersonOutline" :size="18" />
              </div>
              账户信息
            </NSpace>
          </template>

          <NSpace vertical :size="16">
            <!-- 头像 -->
            <NSpace align="center" :size="16">
              <NAvatar round :size="56" color="#6366f1" style="font-size: 20px; font-weight: 700;">
                {{ auth.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </NAvatar>
              <NSpace vertical :size="4">
                <NText strong style="font-size: 16px;">{{ auth.user?.username }}</NText>
                <NText depth="3">{{ auth.user?.email }}</NText>
                <NText depth="3" style="font-size: 12px;">角色: {{ auth.user?.role === 'ADMIN' ? '管理员' : '用户' }}</NText>
              </NSpace>
            </NSpace>

            <NDivider style="margin: 8px 0;" />

            <!-- 修改用户名 -->
            <NForm label-placement="left" label-width="80">
              <NFormItem label="用户名">
                <NSpace :size="8" style="width: 100%;">
                  <NInput
                    v-model:value="newUsername"
                    placeholder="新用户名"
                    style="flex: 1;"
                    maxlength="20"
                  />
                  <NButton
                    type="primary"
                    @click="handleUpdateUsername"
                    :loading="usernameLoading"
                    :disabled="newUsername === auth.user?.username"
                  >
                    保存
                  </NButton>
                </NSpace>
              </NFormItem>
            </NForm>
          </NSpace>
        </NCard>
      </NGi>

      <!-- 修改密码 -->
      <NGi span="2 m:1">
        <NCard class="profile-card">
          <template #header>
            <NSpace align="center" :size="8">
              <div class="section-icon" style="background: rgba(239, 68, 68, 0.12); color: #ef4444;">
                <NIcon :component="LockClosedOutline" :size="18" />
              </div>
              修改密码
            </NSpace>
          </template>

          <NForm label-placement="top">
            <NFormItem label="当前密码">
              <NInput
                v-model:value="oldPassword"
                type="password"
                show-password-on="click"
                placeholder="输入当前密码"
              />
            </NFormItem>
            <NFormItem label="新密码">
              <NInput
                v-model:value="newPassword"
                type="password"
                show-password-on="click"
                placeholder="至少 6 位"
              />
            </NFormItem>
            <NFormItem label="确认新密码">
              <NInput
                v-model:value="confirmPassword"
                type="password"
                show-password-on="click"
                placeholder="再次输入新密码"
              />
            </NFormItem>
            <NButton
              type="primary"
              block
              @click="handleUpdatePassword"
              :loading="passwordLoading"
              :disabled="!oldPassword || !newPassword || !confirmPassword"
            >
              更新密码
            </NButton>
          </NForm>
        </NCard>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.profile-settings {
  max-width: 1000px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
}

.page-desc {
  font-size: 14px;
  color: #a1a1aa;
  margin: 0 0 24px;
}

.profile-card {
  transition: all 0.2s ease;
}

.profile-card:hover {
  transform: translateY(-1px);
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(99, 102, 241, 0.12);
  color: #6366f1;
}
</style>
