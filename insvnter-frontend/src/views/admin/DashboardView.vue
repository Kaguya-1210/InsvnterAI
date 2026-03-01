<script setup lang="ts">
import { NGrid, NGi, NStatistic, NCard, NIcon, NSpace, NText } from 'naive-ui'
import {
  PeopleOutline,
  ChatbubblesOutline,
  CubeOutline,
  FlashOutline,
} from '@vicons/ionicons5'

const stats = [
  { label: '用户数', value: 128, icon: PeopleOutline, color: '#6366f1' },
  { label: '对话数', value: 2048, icon: ChatbubblesOutline, color: '#8b5cf6' },
  { label: '模型数', value: 12, icon: CubeOutline, color: '#a78bfa' },
  { label: 'API 调用', value: 36500, icon: FlashOutline, color: '#c4b5fd' },
]
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    <p class="page-desc">欢迎使用 InsvnterAI 管理控制台</p>

    <!-- Stats Cards -->
    <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGi v-for="s in stats" :key="s.label" span="4 m:2 l:1">
        <NCard hoverable class="stat-card">
          <NSpace align="center" :size="16">
            <div class="stat-icon" :style="{ background: s.color + '18', color: s.color }">
              <NIcon :size="24" :component="s.icon" />
            </div>
            <NStatistic :label="s.label" :value="s.value" />
          </NSpace>
        </NCard>
      </NGi>
    </NGrid>

    <!-- Info Card -->
    <NCard title="系统信息" style="margin-top: 24px;">
      <NGrid :cols="2" :x-gap="24" :y-gap="12" responsive="screen" item-responsive>
        <NGi span="2 m:1">
          <NSpace vertical :size="8">
            <NText depth="3">项目版本</NText>
            <NText strong>0.1.0-SNAPSHOT</NText>
          </NSpace>
        </NGi>
        <NGi span="2 m:1">
          <NSpace vertical :size="8">
            <NText depth="3">运行环境</NText>
            <NText strong>Development</NText>
          </NSpace>
        </NGi>
        <NGi span="2 m:1">
          <NSpace vertical :size="8">
            <NText depth="3">前端框架</NText>
            <NText strong>Vue 3 + Naive UI</NText>
          </NSpace>
        </NGi>
        <NGi span="2 m:1">
          <NSpace vertical :size="8">
            <NText depth="3">后端框架</NText>
            <NText strong>Spring Boot 3</NText>
          </NSpace>
        </NGi>
      </NGrid>
    </NCard>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
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

.stat-card {
  transition: all 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  flex-shrink: 0;
}
</style>
