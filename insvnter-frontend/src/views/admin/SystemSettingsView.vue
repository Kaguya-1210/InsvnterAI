<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NGrid, NGi, NSpace, NText, NTag, NIcon, NSpin } from 'naive-ui'
import {
  ServerOutline, LayersOutline, KeyOutline, GlobeOutline,
  ShieldCheckmarkOutline, TimeOutline, MailOutline,
} from '@vicons/ionicons5'
import { adminApi } from '@/api'

const emailConfig = ref<Record<string, any>>({})
const emailLoading = ref(true)

onMounted(async () => {
  try {
    const res = await adminApi.getEmailConfig()
    emailConfig.value = res.data
  } catch { /* ignore */ }
  emailLoading.value = false
})

const sections = [
  {
    title: '数据库配置', icon: ServerOutline,
    items: [
      { label: '数据库类型', value: 'MySQL 8.0' },
      { label: '连接地址', value: 'localhost:3306' },
      { label: '数据库名', value: 'insvnter_ai' },
      { label: '字符集', value: 'UTF-8 / utf8mb4' },
      { label: '连接池', value: 'HikariCP' },
    ],
  },
  {
    title: '缓存配置', icon: LayersOutline,
    items: [
      { label: 'Redis 地址', value: 'localhost:6379' },
      { label: '用途', value: '验证码缓存 / JWT 黑名单 / IP 限流' },
    ],
  },
  {
    title: '文档存储', icon: GlobeOutline,
    items: [
      { label: 'MongoDB 地址', value: 'localhost:27017' },
      { label: '数据库名', value: 'insvnter_ai' },
      { label: '用途', value: '配置预设 / AI 聊天记录' },
    ],
  },
  {
    title: 'JWT 认证', icon: KeyOutline,
    items: [
      { label: '签名算法', value: 'HMAC-SHA256' },
      { label: 'Token 有效期', value: '24 小时' },
      { label: '黑名单机制', value: 'Redis TTL 自动过期' },
    ],
  },
  {
    title: '安全策略', icon: ShieldCheckmarkOutline,
    items: [
      { label: '密码加密', value: 'BCrypt' },
      { label: '图形验证码', value: 'Kaptcha (60秒 TTL)' },
      { label: 'IP 限流 - 登录', value: '10次/分钟' },
      { label: 'IP 限流 - 注册', value: '3次/小时' },
      { label: 'IP 限流 - 邮件', value: '5次/小时' },
      { label: 'CORS 策略', value: 'localhost:5173, localhost:8080' },
    ],
  },
  {
    title: '运行时信息', icon: TimeOutline,
    items: [
      { label: '后端框架', value: 'Spring Boot 3.4.3' },
      { label: 'Java 版本', value: 'Java 17' },
      { label: '前端框架', value: 'Vue 3 + Naive UI' },
      { label: '构建工具', value: 'Vite 7.3 / Maven' },
      { label: '服务端口', value: '前端 5173 / 后端 8080' },
    ],
  },
]
</script>

<template>
  <div class="system-settings">
    <h2 class="page-title">系统设置</h2>
    <p class="page-desc">当前系统配置与运行参数概览</p>

    <NGrid :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <!-- 邮件服务（动态） -->
      <NGi span="2 m:1">
        <NCard class="config-card">
          <template #header>
            <NSpace align="center" :size="8">
              <div class="section-icon" style="background: rgba(16, 185, 129, 0.12); color: #10b981;">
                <NIcon :component="MailOutline" :size="18" />
              </div>
              邮件服务
              <NTag v-if="!emailLoading" :type="emailConfig.configured ? 'success' : 'warning'" size="small" round>
                {{ emailConfig.configured ? '已配置' : '未配置' }}
              </NTag>
            </NSpace>
          </template>
          <NSpin v-if="emailLoading" size="small" />
          <div v-else class="config-list">
            <div class="config-item">
              <NText depth="3" class="config-label">SMTP 服务器</NText>
              <NText strong>{{ emailConfig.smtpHost || '-' }}</NText>
            </div>
            <div class="config-item">
              <NText depth="3" class="config-label">SMTP 端口</NText>
              <NText strong>{{ emailConfig.smtpPort || '-' }}</NText>
            </div>
            <div class="config-item">
              <NText depth="3" class="config-label">发件账号</NText>
              <NText strong>{{ emailConfig.smtpUsername || '-' }}</NText>
            </div>
            <div class="config-item">
              <NText depth="3" class="config-label">发件人名称</NText>
              <NText strong>{{ emailConfig.fromName || '-' }}</NText>
            </div>
            <div class="config-item">
              <NText depth="3" class="config-label">发件地址</NText>
              <NText strong>{{ emailConfig.fromAddress || '-' }}</NText>
            </div>
          </div>
        </NCard>
      </NGi>

      <!-- 静态配置 -->
      <NGi v-for="section in sections" :key="section.title" span="2 m:1">
        <NCard class="config-card">
          <template #header>
            <NSpace align="center" :size="8">
              <div class="section-icon">
                <NIcon :component="section.icon" :size="18" />
              </div>
              {{ section.title }}
            </NSpace>
          </template>
          <div class="config-list">
            <div v-for="item in section.items" :key="item.label" class="config-item">
              <NText depth="3" class="config-label">{{ item.label }}</NText>
              <NText strong>{{ item.value }}</NText>
            </div>
          </div>
        </NCard>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
.system-settings { max-width: 1200px; }
.page-title { font-size: 24px; font-weight: 700; margin: 0 0 4px; }
.page-desc { font-size: 14px; color: #a1a1aa; margin: 0 0 24px; }
.config-card { transition: all 0.2s ease; }
.config-card:hover { transform: translateY(-1px); }

.section-icon {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border-radius: 8px;
  background: rgba(99, 102, 241, 0.12); color: #6366f1;
}

.config-list { display: flex; flex-direction: column; gap: 12px; }
.config-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 4px 0; border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}
.config-item:last-child { border-bottom: none; }
.config-label { font-size: 13px; }
</style>
