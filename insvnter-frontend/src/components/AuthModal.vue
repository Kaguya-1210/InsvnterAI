<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  NModal, NForm, NFormItem, NInput, NButton, NDivider, NIcon, NSpin, useMessage,
} from 'naive-ui'
import { CloseOutline } from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { authApi, emailApi } from '@/api'

const props = defineProps<{ show: boolean; initialTab?: 'login' | 'register' }>()
const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const auth = useAuthStore()
const message = useMessage()
const activeTab = ref<'login' | 'register'>('login')

// Captcha
const captchaId = ref('')
const captchaImage = ref('')
const captchaLoading = ref(false)

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const res = await authApi.getCaptcha()
    captchaId.value = res.data.captchaId
    captchaImage.value = res.data.image
  } catch {
    captchaImage.value = ''
  } finally {
    captchaLoading.value = false
  }
}

// Forms
const loginForm = ref({ account: '', password: '', captcha: '' })
const loginLoading = ref(false)
const registerForm = ref({ username: '', email: '', password: '', confirmPassword: '', captcha: '', emailCode: '' })
const registerLoading = ref(false)

// 邮箱验证码倒计时
const emailCooldown = ref(0)
let cooldownTimer: ReturnType<typeof setInterval> | null = null

function resetForms() {
  loginForm.value = { account: '', password: '', captcha: '' }
  registerForm.value = { username: '', email: '', password: '', confirmPassword: '', captcha: '', emailCode: '' }
  captchaId.value = ''
  captchaImage.value = ''
  emailCooldown.value = 0
  if (cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = null }
}

// 弹窗打开时加载验证码，关闭时重置
watch(() => props.show, (val) => {
  if (val) {
    if (props.initialTab) activeTab.value = props.initialTab
    loadCaptcha()
  } else {
    resetForms()
  }
})

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val),
})

// 发送邮箱验证码
async function sendEmailCode() {
  const email = registerForm.value.email
  if (!email) { message.warning('请先输入邮箱'); return }
  if (!/^[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}$/.test(email)) { message.warning('邮箱格式不正确'); return }

  try {
    await emailApi.sendCode(email)
    message.success('验证码已发送到邮箱')
    emailCooldown.value = 60
    cooldownTimer = setInterval(() => {
      emailCooldown.value--
      if (emailCooldown.value <= 0) { clearInterval(cooldownTimer!); cooldownTimer = null }
    }, 1000)
  } catch (e: any) {
    message.error(e.message || '发送失败')
  }
}

async function handleLogin() {
  if (!loginForm.value.account || !loginForm.value.password) { message.warning('请填写用户名/邮箱和密码'); return }
  if (!loginForm.value.captcha) { message.warning('请输入验证码'); return }

  loginLoading.value = true
  try {
    await auth.login(loginForm.value.account, loginForm.value.password, loginForm.value.captcha, captchaId.value)
    message.success('登录成功！')
    visible.value = false
    emit('success')
  } catch (e: any) {
    message.error(e.message || '登录失败')
    loginForm.value.captcha = ''
    loadCaptcha()
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const f = registerForm.value
  if (!f.username || !f.email || !f.password) { message.warning('请填写所有必填项'); return }
  if (f.password !== f.confirmPassword) { message.error('两次密码不一致'); return }
  if (!f.captcha) { message.warning('请输入图形验证码'); return }
  if (!f.emailCode) { message.warning('请输入邮箱验证码'); return }

  registerLoading.value = true
  try {
    await auth.register(f.username, f.email, f.password, f.captcha, captchaId.value, f.emailCode)
    message.success(`注册成功，欢迎 ${f.username}！`)
    visible.value = false
    emit('success')
  } catch (e: any) {
    message.error(e.message || '注册失败')
    registerForm.value.captcha = ''
    loadCaptcha()
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <NModal v-model:show="visible" :mask-closable="true" transform-origin="center" class="auth-modal">
    <div class="auth-dialog">
      <button class="close-btn" @click="visible = false">
        <NIcon :component="CloseOutline" :size="18" />
      </button>

      <div class="auth-header">
        <div class="auth-logo">✦</div>
        <h2 class="auth-title">InsvnterAI</h2>
        <p class="auth-subtitle">新一代 AI 对话与角色扮演平台</p>
      </div>

      <NDivider style="margin: 10px 0 6px;" />

      <!-- Login -->
      <template v-if="activeTab === 'login'">
        <NForm class="auth-form" @submit.prevent="handleLogin">
          <NFormItem path="account">
            <NInput v-model:value="loginForm.account" placeholder="用户名 / 邮箱" round />
          </NFormItem>
          <NFormItem path="password">
            <NInput v-model:value="loginForm.password" type="password" show-password-on="click" placeholder="密码" round />
          </NFormItem>
          <NFormItem path="captcha">
            <div class="captcha-row">
              <NInput v-model:value="loginForm.captcha" placeholder="验证码" round class="captcha-input" />
              <div class="captcha-img" @click="loadCaptcha" title="点击刷新验证码">
                <NSpin v-if="captchaLoading" size="small" />
                <img v-else-if="captchaImage" :src="captchaImage" alt="captcha" />
                <span v-else>加载中</span>
              </div>
            </div>
          </NFormItem>
          <NButton type="primary" block strong round :loading="loginLoading" attr-type="submit">登录</NButton>
        </NForm>
        <div class="auth-switch">
          还没有账号？<NButton text type="primary" size="tiny" @click="activeTab = 'register'; loadCaptcha()">立即注册</NButton>
        </div>
      </template>

      <!-- Register -->
      <template v-else>
        <NForm class="auth-form" @submit.prevent="handleRegister">
          <NFormItem path="username">
            <NInput v-model:value="registerForm.username" placeholder="用户名（3-20位）" round maxlength="20" />
          </NFormItem>
          <NFormItem path="email">
            <div class="email-row">
              <NInput v-model:value="registerForm.email" placeholder="邮箱地址" round class="email-input" />
              <NButton
                size="small" round type="primary" ghost
                :disabled="emailCooldown > 0 || !registerForm.email"
                @click="sendEmailCode"
              >
                {{ emailCooldown > 0 ? `${emailCooldown}s` : '获取验证码' }}
              </NButton>
            </div>
          </NFormItem>
          <NFormItem path="emailCode">
            <NInput v-model:value="registerForm.emailCode" placeholder="邮箱验证码（6位）" round maxlength="6" />
          </NFormItem>
          <NFormItem path="password">
            <NInput v-model:value="registerForm.password" type="password" show-password-on="click" placeholder="密码（至少6位）" round />
          </NFormItem>
          <NFormItem path="confirmPassword">
            <NInput v-model:value="registerForm.confirmPassword" type="password" show-password-on="click" placeholder="确认密码" round />
          </NFormItem>
          <NFormItem path="captcha">
            <div class="captcha-row">
              <NInput v-model:value="registerForm.captcha" placeholder="图形验证码" round class="captcha-input" />
              <div class="captcha-img" @click="loadCaptcha" title="点击刷新验证码">
                <NSpin v-if="captchaLoading" size="small" />
                <img v-else-if="captchaImage" :src="captchaImage" alt="captcha" />
                <span v-else>加载中</span>
              </div>
            </div>
          </NFormItem>
          <NButton type="primary" block strong round :loading="registerLoading" attr-type="submit">注册</NButton>
        </NForm>
        <div class="auth-switch">
          已有账号？<NButton text type="primary" size="tiny" @click="activeTab = 'login'; loadCaptcha()">立即登录</NButton>
        </div>
      </template>
    </div>
  </NModal>
</template>

<style scoped>
.auth-dialog {
  position: relative;
  width: 380px;
  max-width: 92vw;
  padding: 24px;
  border-radius: 18px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.close-btn {
  position: absolute; top: 12px; right: 12px;
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  background: transparent; border: none; border-radius: 50%;
  cursor: pointer; transition: all 0.2s; z-index: 1;
}
[data-theme="dark"] .close-btn { color: #71717a; }
[data-theme="dark"] .close-btn:hover { background: rgba(255,255,255,0.08); color: #e4e4e7; }
[data-theme="light"] .close-btn { color: #a1a1aa; }
[data-theme="light"] .close-btn:hover { background: rgba(0,0,0,0.05); color: #18181b; }

[data-theme="dark"] .auth-dialog {
  background: linear-gradient(145deg, rgba(30,30,40,0.95), rgba(20,20,28,0.98));
  border: 1px solid rgba(255,255,255,0.08);
  box-shadow: 0 24px 80px rgba(0,0,0,0.5);
}
[data-theme="light"] .auth-dialog {
  background: linear-gradient(145deg, rgba(255,255,255,0.98), rgba(250,250,255,0.95));
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 24px 80px rgba(0,0,0,0.12);
}

.auth-header { text-align: center; }
.auth-logo {
  font-size: 28px; margin-bottom: 4px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.auth-title {
  font-size: 20px; font-weight: 700; margin: 0 0 2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.auth-subtitle { font-size: 12px; margin: 0; }
[data-theme="dark"] .auth-subtitle { color: #71717a; }
[data-theme="light"] .auth-subtitle { color: #a1a1aa; }

.auth-form { padding-top: 4px; }
.auth-form :deep(.n-form-item) {
  margin: 0; --n-blank-height: 30px; --n-feedback-height: 0px;
  grid-template-rows: minmax(var(--n-label-height), auto) 0.5fr;
}
.auth-form :deep(.n-form-item-feedback-wrapper) { display: none; }
.auth-form :deep(.n-form-item .n-form-item-blank) { min-height: 0; }
.auth-form > :deep(.n-button) {
  margin-top: 26px; width: 55%; margin-left: auto; margin-right: auto;
}

.captcha-row, .email-row { display: flex; align-items: center; gap: 10px; width: 100%; }
.captcha-input, .email-input { flex: 1; min-width: 0; }

.captcha-img {
  width: 100px; height: 34px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 17px; font-size: 12px;
  cursor: pointer; user-select: none; flex-shrink: 0; overflow: hidden;
}
.captcha-img img { width: 100%; height: 100%; object-fit: cover; }
[data-theme="dark"] .captcha-img {
  background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.1));
  border: 1px solid rgba(99,102,241,0.2); color: #a78bfa;
}
[data-theme="light"] .captcha-img {
  background: linear-gradient(135deg, rgba(99,102,241,0.08), rgba(139,92,246,0.05));
  border: 1px solid rgba(99,102,241,0.15); color: #6366f1;
}

.auth-switch { text-align: center; margin-top: 10px; font-size: 12px; }
[data-theme="dark"] .auth-switch { color: #71717a; }
[data-theme="light"] .auth-switch { color: #a1a1aa; }

@media (max-width: 480px) {
  .auth-dialog { width: 100%; max-width: 100vw; border-radius: 16px 16px 0 0; padding: 20px 16px; }
}
</style>
