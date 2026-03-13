<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  NInput, NButton, NIcon, NSpace, NAvatar, NTag, NEmpty,
  NScrollbar, NDivider, NText,
} from 'naive-ui'
import { SearchOutline, AddOutline, ChatbubblesOutline } from '@vicons/ionicons5'

const emit = defineEmits<{
  (e: 'select-character', id: string): void
  (e: 'select-chat', id: string): void
  (e: 'new-chat'): void
}>()

const props = defineProps<{
  activeCharacterId?: string | null
  activeChatId?: string | null
}>()

const searchQuery = ref('')
const activeSection = ref<'characters' | 'chats'>('characters')

// Mock 角色数据
const characters = ref([
  { id: 'c1', name: '默认助手', desc: '通用 AI 助理，可以回答各种问题', avatar: '🤖', tags: ['通用', '助手'] },
  { id: 'c2', name: '猫娘', desc: '可爱的猫娘角色，善于卖萌和陪伴', avatar: '🐱', tags: ['角色扮演'] },
  { id: 'c3', name: '代码导师', desc: '资深全栈工程师，擅长代码审查和教学', avatar: '👨‍💻', tags: ['编程', '教学'] },
  { id: 'c4', name: '小说家', desc: '创意写作专家，擅长构建世界观和故事', avatar: '✍️', tags: ['创作'] },
  { id: 'c5', name: '翻译官', desc: '精通中英日三语的专业翻译', avatar: '🌐', tags: ['翻译'] },
])

// Mock 对话历史
const chatHistory = ref([
  { id: 'h1', title: '关于 Vue 3 的问题', characterName: '代码导师', time: '今天 18:30', preview: '帮我看看这段代码...' },
  { id: 'h2', title: '日常聊天', characterName: '猫娘', time: '今天 15:10', preview: '主人今天过得怎么样喵~' },
  { id: 'h3', title: '翻译文档', characterName: '翻译官', time: '昨天', preview: '请帮我翻译这段...' },
])

const filteredCharacters = computed(() => {
  if (!searchQuery.value) return characters.value
  const q = searchQuery.value.toLowerCase()
  return characters.value.filter(c =>
    c.name.toLowerCase().includes(q) || c.desc.toLowerCase().includes(q)
  )
})
</script>

<template>
  <div class="sidebar-content">
    <!-- 搜索框 -->
    <div class="sidebar-search">
      <NInput
        v-model:value="searchQuery"
        placeholder="搜索角色..."
        size="small"
        round
        clearable
      >
        <template #prefix>
          <NIcon :component="SearchOutline" :size="16" />
        </template>
      </NInput>
    </div>

    <!-- Tab 切换 -->
    <div class="sidebar-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeSection === 'characters' }"
        @click="activeSection = 'characters'"
      >
        角色
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeSection === 'chats' }"
        @click="activeSection = 'chats'"
      >
        历史
      </button>
    </div>

    <!-- 新建对话按钮 -->
    <div class="sidebar-action">
      <NButton block size="small" type="primary" ghost round @click="emit('new-chat')">
        <template #icon><NIcon :component="AddOutline" /></template>
        新建对话
      </NButton>
    </div>

    <NScrollbar style="flex: 1;">
      <!-- 角色列表 -->
      <div v-if="activeSection === 'characters'" class="list-section">
        <NEmpty v-if="filteredCharacters.length === 0" description="没有匹配的角色" style="padding: 40px 0;" />
        <div
          v-for="char in filteredCharacters"
          :key="char.id"
          class="item-card"
          :class="{ active: props.activeCharacterId === char.id }"
          @click="emit('select-character', char.id)"
        >
          <div class="item-avatar">{{ char.avatar }}</div>
          <div class="item-info">
            <div class="item-name">{{ char.name }}</div>
            <div class="item-desc">{{ char.desc }}</div>
            <NSpace :size="4" style="margin-top: 4px;">
              <NTag v-for="tag in char.tags" :key="tag" size="tiny" round :bordered="false">
                {{ tag }}
              </NTag>
            </NSpace>
          </div>
        </div>
      </div>

      <!-- 对话历史 -->
      <div v-else class="list-section">
        <NEmpty v-if="chatHistory.length === 0" description="暂无对话记录" style="padding: 40px 0;" />
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="item-card chat-item"
          :class="{ active: props.activeChatId === chat.id }"
          @click="emit('select-chat', chat.id)"
        >
          <NIcon :component="ChatbubblesOutline" :size="20" class="chat-icon" />
          <div class="item-info">
            <div class="item-name">{{ chat.title }}</div>
            <div class="item-desc">
              <span class="chat-char">{{ chat.characterName }}</span>
              <span class="chat-time">{{ chat.time }}</span>
            </div>
            <div class="item-desc">{{ chat.preview }}</div>
          </div>
        </div>
      </div>
    </NScrollbar>
  </div>
</template>

<style scoped>
.sidebar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
  gap: 10px;
}

.sidebar-search {
  flex-shrink: 0;
}

/* Tab 切换 */
.sidebar-tabs {
  display: flex;
  gap: 2px;
  padding: 3px;
  border-radius: 10px;
  flex-shrink: 0;
}
[data-theme="dark"] .sidebar-tabs { background: rgba(255,255,255,0.04); }
[data-theme="light"] .sidebar-tabs { background: rgba(0,0,0,0.04); }

.tab-btn {
  flex: 1;
  padding: 6px 0;
  font-size: 13px;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
}
[data-theme="dark"] .tab-btn { color: #71717a; }
[data-theme="light"] .tab-btn { color: #a1a1aa; }

[data-theme="dark"] .tab-btn.active {
  background: rgba(99, 102, 241, 0.15);
  color: #a78bfa;
}
[data-theme="light"] .tab-btn.active {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

.sidebar-action { flex-shrink: 0; }

/* 卡片列表 */
.list-section {
  padding: 0 2px;
}

.item-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
  margin-bottom: 4px;
}

[data-theme="dark"] .item-card:hover { background: rgba(255,255,255,0.04); }
[data-theme="light"] .item-card:hover { background: rgba(0,0,0,0.03); }

[data-theme="dark"] .item-card.active {
  background: rgba(99, 102, 241, 0.12);
  border: 1px solid rgba(99, 102, 241, 0.2);
}
[data-theme="light"] .item-card.active {
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.item-card { border: 1px solid transparent; }

.item-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
[data-theme="dark"] .item-avatar { background: rgba(255,255,255,0.06); }
[data-theme="light"] .item-avatar { background: rgba(0,0,0,0.04); }

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
}
[data-theme="dark"] .item-name { color: #e4e4e7; }
[data-theme="light"] .item-name { color: #18181b; }

.item-desc {
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
[data-theme="dark"] .item-desc { color: #71717a; }
[data-theme="light"] .item-desc { color: #a1a1aa; }

/* 对话历史卡片 */
.chat-icon {
  margin-top: 2px;
  flex-shrink: 0;
}
[data-theme="dark"] .chat-icon { color: #52525b; }
[data-theme="light"] .chat-icon { color: #a1a1aa; }

.chat-char {
  font-weight: 500;
}
[data-theme="dark"] .chat-char { color: #a78bfa; }
[data-theme="light"] .chat-char { color: #6366f1; }

.chat-time {
  margin-left: 6px;
  font-size: 11px;
}
</style>
