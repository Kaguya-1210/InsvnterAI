<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NCard, NDataTable, NSpace, NButton, NInput, NSelect, NIcon,
  NTag, NPopconfirm, useMessage, useDialog, NText, NPagination,
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline } from '@vicons/ionicons5'
import { adminApi } from '@/api'

const message = useMessage()
const dialog = useDialog()

interface UserRow {
  id: number
  username: string
  email: string
  role: string
  enabled: boolean
  lastLoginAt: string | null
  createdAt: string
}

const loading = ref(false)
const users = ref<UserRow[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const roleFilter = ref<string | null>(null)

const roleOptions = [
  { label: '全部', value: 'ALL' },
  { label: '管理员', value: 'ADMIN' },
  { label: '普通用户', value: 'USER' },
]

const columns: DataTableColumns<UserRow> = [
  { title: 'ID', key: 'id', width: 60, align: 'center' },
  { title: '用户名', key: 'username', width: 140 },
  { title: '邮箱', key: 'email', width: 200, ellipsis: { tooltip: true } },
  {
    title: '角色',
    key: 'role',
    width: 100,
    align: 'center',
    render(row) {
      return h(NTag, {
        type: row.role === 'ADMIN' ? 'warning' : 'info',
        size: 'small',
        round: true,
      }, { default: () => row.role === 'ADMIN' ? '管理员' : '用户' })
    },
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    align: 'center',
    render(row) {
      return h(NTag, {
        type: row.enabled ? 'success' : 'error',
        size: 'small',
        round: true,
      }, { default: () => row.enabled ? '正常' : '禁用' })
    },
  },
  {
    title: '最后登录',
    key: 'lastLoginAt',
    width: 170,
    render(row) {
      return row.lastLoginAt ? formatDate(row.lastLoginAt) : h(NText, { depth: 3 }, { default: () => '从未登录' })
    },
  },
  {
    title: '注册时间',
    key: 'createdAt',
    width: 170,
    render(row) {
      return formatDate(row.createdAt)
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          // 角色切换
          h(NButton, {
            size: 'small',
            secondary: true,
            type: row.role === 'ADMIN' ? 'warning' : 'info',
            onClick: () => handleToggleRole(row),
          }, { default: () => row.role === 'ADMIN' ? '降为用户' : '升为管理员' }),
          // 启用/禁用
          h(NButton, {
            size: 'small',
            secondary: true,
            type: row.enabled ? 'error' : 'success',
            onClick: () => handleToggleStatus(row),
          }, { default: () => row.enabled ? '禁用' : '启用' }),
          // 重置密码
          h(NButton, {
            size: 'small',
            secondary: true,
            onClick: () => handleResetPassword(row),
          }, { default: () => '重置密码' }),
          // 删除
          h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row),
          }, {
            trigger: () => h(NButton, { size: 'small', secondary: true, type: 'error' }, { default: () => '删除' }),
            default: () => `确定删除用户 ${row.username}？此操作不可撤销。`,
          }),
        ],
      })
    },
  },
]

function formatDate(str: string): string {
  const d = new Date(str)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminApi.getUsers({
      page: page.value - 1,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      role: roleFilter.value || undefined,
    })
    users.value = res.data.content
    total.value = res.data.totalElements
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchUsers()
}

async function handleToggleRole(row: UserRow) {
  const newRole = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  dialog.warning({
    title: '确认角色变更',
    content: `将 ${row.username} 的角色从 ${row.role === 'ADMIN' ? '管理员' : '用户'} 改为 ${newRole === 'ADMIN' ? '管理员' : '用户'}？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await adminApi.updateUserRole(row.id, newRole)
        message.success('角色已更新')
        fetchUsers()
      } catch (e: any) {
        message.error(e.message)
      }
    },
  })
}

async function handleToggleStatus(row: UserRow) {
  try {
    await adminApi.updateUserStatus(row.id, !row.enabled)
    message.success(row.enabled ? '用户已禁用' : '用户已启用')
    fetchUsers()
  } catch (e: any) {
    message.error(e.message)
  }
}

async function handleResetPassword(row: UserRow) {
  dialog.warning({
    title: '重置密码',
    content: `确定要重置 ${row.username} 的密码？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await adminApi.resetUserPassword(row.id)
        dialog.success({
          title: '密码已重置',
          content: `新密码: ${res.data.tempPassword}\n\n请记录此密码，关闭后无法再次查看。`,
          positiveText: '已记录',
        })
      } catch (e: any) {
        message.error(e.message)
      }
    },
  })
}

async function handleDelete(row: UserRow) {
  try {
    await adminApi.deleteUser(row.id)
    message.success('用户已删除')
    fetchUsers()
  } catch (e: any) {
    message.error(e.message)
  }
}

onMounted(fetchUsers)
</script>

<template>
  <div class="user-manage">
    <h2 class="page-title">用户管理</h2>
    <p class="page-desc">管理平台用户账户、角色和访问权限</p>

    <NCard>
      <!-- 搜索工具栏 -->
      <NSpace style="margin-bottom: 16px;" align="center" justify="space-between">
        <NSpace align="center">
          <NInput
            v-model:value="keyword"
            placeholder="搜索用户名或邮箱"
            clearable
            style="width: 260px;"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <NIcon :component="SearchOutline" />
            </template>
          </NInput>
          <NSelect
            v-model:value="roleFilter"
            :options="roleOptions"
            placeholder="角色筛选"
            style="width: 130px;"
            clearable
            @update:value="handleSearch"
          />
          <NButton type="primary" @click="handleSearch">搜索</NButton>
        </NSpace>
        <NButton @click="fetchUsers" :loading="loading">
          <template #icon>
            <NIcon :component="RefreshOutline" />
          </template>
          刷新
        </NButton>
      </NSpace>

      <!-- 数据表格 -->
      <NDataTable
        :columns="columns"
        :data="users"
        :loading="loading"
        :bordered="false"
        :scroll-x="1200"
        :row-key="(row: UserRow) => row.id"
      />

      <!-- 分页 -->
      <NSpace justify="end" style="margin-top: 16px;">
        <NPagination
          v-model:page="page"
          :page-size="pageSize"
          :item-count="total"
          show-quick-jumper
          @update:page="fetchUsers"
        />
      </NSpace>
    </NCard>
  </div>
</template>

<style scoped>
.user-manage {
  max-width: 1400px;
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
</style>
