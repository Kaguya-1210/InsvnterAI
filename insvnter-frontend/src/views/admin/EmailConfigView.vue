<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NCard, NForm, NFormItem, NInput, NButton, NSpace, NIcon, NTag, NDivider,
  NSpin, NSelect, NText, useMessage,
} from 'naive-ui'
import { MailOutline, SendOutline, SaveOutline, ColorPaletteOutline } from '@vicons/ionicons5'
import { adminApi } from '@/api'

const message = useMessage()
const loading = ref(true)
const saving = ref(false)
const testing = ref(false)

const form = ref({
  smtpHost: '',
  smtpPort: '587',
  smtpUsername: '',
  smtpPassword: '',
  fromAddress: '',
  fromName: '',
  encryption: 'starttls',
  activeTemplateId: null as string | null,
})
const configured = ref(false)
const testEmail = ref('')

const encryptionOptions = [
  { label: 'SSL/TLS', value: 'ssl' },
  { label: 'STARTTLS', value: 'starttls' },
  { label: '无加密', value: 'none' },
]

// 模板选项
const templateOptions = ref<{ label: string; value: string }[]>([])

onMounted(async () => {
  try {
    const [configRes, templatesRes] = await Promise.all([
      adminApi.getEmailConfig(),
      adminApi.getEmailTemplates(),
    ])

    templateOptions.value = templatesRes.data.map((t: any) => ({ label: t.name, value: t.id }))

    const d = configRes.data
    form.value = {
      smtpHost: d.smtpHost || '',
      smtpPort: d.smtpPort || '587',
      smtpUsername: d.smtpUsername || '',
      smtpPassword: d.smtpPassword || '',
      fromAddress: d.fromAddress || '',
      fromName: d.fromName || '',
      encryption: d.encryption || 'starttls',
      activeTemplateId: d.activeTemplateId || null,
    }
    configured.value = d.configured
  } catch { /* ignore */ }
  loading.value = false
})

async function handleSave() {
  if (!form.value.smtpHost || !form.value.smtpUsername) {
    message.warning('请填写 SMTP 服务器和账号')
    return
  }
  saving.value = true
  try {
    await adminApi.saveEmailConfig({
      smtpHost: form.value.smtpHost,
      smtpPort: form.value.smtpPort,
      smtpUsername: form.value.smtpUsername,
      smtpPassword: form.value.smtpPassword,
      fromAddress: form.value.fromAddress,
      fromName: form.value.fromName,
      encryption: form.value.encryption,
      activeTemplateId: form.value.activeTemplateId || '',
    })
    configured.value = true
    message.success('邮件配置已保存')
  } catch (e: any) {
    message.error(e.message || '保存失败')
  }
  saving.value = false
}

async function handleTest() {
  if (!testEmail.value) { message.warning('请填写收件邮箱'); return }
  testing.value = true
  try {
    await adminApi.testEmailConfig(testEmail.value)
    message.success('测试邮件已发送！请检查收件箱')
  } catch (e: any) {
    message.error(e.message || '发送失败')
  }
  testing.value = false
}
</script>

<template>
  <div class="email-config-page">
    <h2 class="page-title">邮件配置</h2>
    <p class="page-desc">
      配置 SMTP 邮件服务器，用于发送验证码等邮件
      <NTag v-if="!loading" :type="configured ? 'success' : 'warning'" size="small" round style="margin-left: 8px;">
        {{ configured ? '已配置' : '未配置' }}
      </NTag>
    </p>

    <NSpin :show="loading" style="min-height: 200px;">
      <NCard class="config-card">
        <template #header>
          <NSpace align="center" :size="8">
            <div class="section-icon"><NIcon :component="MailOutline" :size="18" /></div>
            SMTP 设置
          </NSpace>
        </template>

        <NForm label-placement="left" label-width="100" :show-feedback="false">
          <NFormItem label="SMTP 服务器">
            <NInput v-model:value="form.smtpHost" placeholder="smtp.qq.com / smtp.163.com" autocomplete="off" />
          </NFormItem>
          <NFormItem label="端口">
            <NInput v-model:value="form.smtpPort" placeholder="587" style="width: 120px;" autocomplete="off" />
          </NFormItem>
          <NFormItem label="加密方式">
            <NSelect v-model:value="form.encryption" :options="encryptionOptions" style="width: 260px;" />
          </NFormItem>
          <NFormItem label="账号">
            <NInput v-model:value="form.smtpUsername" placeholder="your-email@qq.com" autocomplete="off" />
          </NFormItem>
          <NFormItem label="密码/授权码">
            <NInput v-model:value="form.smtpPassword" type="password" show-password-on="click" placeholder="SMTP 授权码" autocomplete="new-password" />
          </NFormItem>

          <NDivider style="margin: 16px 0;" />

          <NFormItem label="发件人名称">
            <NInput v-model:value="form.fromName" placeholder="InsvnterAI" autocomplete="off" />
          </NFormItem>
          <NFormItem label="发件人地址">
            <NInput v-model:value="form.fromAddress" placeholder="noreply@insvnter.ai" autocomplete="off" />
          </NFormItem>
        </NForm>
      </NCard>

      <!-- 邮件模板选择 -->
      <NCard class="config-card" style="margin-top: 16px;">
        <template #header>
          <NSpace align="center" :size="8">
            <div class="section-icon" style="background: rgba(139, 92, 246, 0.12); color: #8b5cf6;">
              <NIcon :component="ColorPaletteOutline" :size="18" />
            </div>
            验证码邮件模板
          </NSpace>
        </template>

        <NFormItem label="当前模板" label-placement="left" label-width="100" :show-feedback="false">
          <NSelect
            v-model:value="form.activeTemplateId"
            :options="templateOptions"
            placeholder="不使用模板（纯文字）"
            clearable
            style="width: 300px;"
          />
        </NFormItem>
        <p style="font-size: 12px; color: #71717a; margin: 8px 0 0;">
          选择注册验证码使用的邮件模板，不选择则发送纯文字邮件
        </p>
      </NCard>

      <NSpace style="margin-top: 20px;">
        <NButton type="primary" :loading="saving" @click="handleSave">
          <template #icon><NIcon :component="SaveOutline" /></template>
          保存全部配置
        </NButton>
      </NSpace>

      <!-- 测试发送 -->
      <NCard class="config-card" style="margin-top: 16px;">
        <template #header>
          <NSpace align="center" :size="8">
            <div class="section-icon" style="background: rgba(16, 185, 129, 0.12); color: #10b981;">
              <NIcon :component="SendOutline" :size="18" />
            </div>
            测试发送
          </NSpace>
        </template>

        <NSpace>
          <NInput v-model:value="testEmail" placeholder="收件邮箱" style="width: 280px;" autocomplete="off" />
          <NButton type="success" :loading="testing" :disabled="!configured" @click="handleTest">发送测试邮件</NButton>
        </NSpace>
        <p v-if="!configured" style="font-size: 12px; color: #f59e0b; margin: 8px 0 0;">请先保存 SMTP 配置</p>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped>
.email-config-page { max-width: 700px; }
.page-title { font-size: 24px; font-weight: 700; margin: 0 0 4px; }
.page-desc { font-size: 14px; color: #a1a1aa; margin: 0 0 24px; display: flex; align-items: center; }
.config-card { transition: all 0.2s ease; }
.config-card:hover { transform: translateY(-1px); }
.section-icon {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 8px;
  background: rgba(99, 102, 241, 0.12); color: #6366f1;
}
</style>
