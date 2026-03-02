<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NGrid, NGi, NStatistic, NCard, NIcon, NSpace, NText, NSkeleton } from 'naive-ui'
import {
  PeopleOutline,
  PersonAddOutline,
  CheckmarkCircleOutline,
  BanOutline,
  ShieldCheckmarkOutline,
  PersonOutline,
} from '@vicons/ionicons5'
import { adminApi } from '@/api'

const loading = ref(true)

const stats = ref([
  { label: '总用户数', value: 0, icon: PeopleOutline, color: '#6366f1' },
  { label: '今日新增', value: 0, icon: PersonAddOutline, color: '#10b981' },
  { label: '活跃用户', value: 0, icon: CheckmarkCircleOutline, color: '#3b82f6' },
  { label: '已禁用', value: 0, icon: BanOutline, color: '#ef4444' },
  { label: '管理员', value: 0, icon: ShieldCheckmarkOutline, color: '#8b5cf6' },
  { label: '普通用户', value: 0, icon: PersonOutline, color: '#f59e0b' },
])

onMounted(async () => {
  try {
    const res = await adminApi.getDashboard()
    const d = res.data
    stats.value[0].value = d.totalUsers
    stats.value[1].value = d.todayNewUsers
    stats.value[2].value = d.activeUsers
    stats.value[3].value = d.disabledUsers
    stats.value[4].value = d.adminCount
    stats.value[5].value = d.userCount
  } catch (e: any) {
    console.error('Failed to load dashboard:', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    <p class="page-desc">系统运行状态概览</p>

    <!-- Stats Cards -->
    <NGrid :cols="6" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGi v-for="s in stats" :key="s.label" span="6 s:3 m:2 l:1">
        <NCard hoverable class="stat-card">
          <NSkeleton v-if="loading" text :repeat="2" />
          <NSpace v-else align="center" :size="16">
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
            <NText strong>Spring Boot 3.4.3</NText>
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
