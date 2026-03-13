<script setup lang="ts">
import { ref, computed, provide, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NIcon, NButton, NSpace, NAvatar, NText,
  NDropdown, NDrawer, NDrawerContent,
} from 'naive-ui'
import {
  MenuOutline, SettingsOutline, LogOutOutline, HomeOutline,
  GridOutline, SunnyOutline, MoonOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import CharacterSidebar from '@/components/chat/CharacterSidebar.vue'
import ChatArea from '@/components/chat/ChatArea.vue'
import SettingsPanel from '@/components/chat/SettingsPanel.vue'
import type { ChatMessage } from '@/components/chat/ChatArea.vue'

const router = useRouter()
const auth = useAuthStore()
const theme = useThemeStore()

// 侧栏状态
const leftOpen = ref(true)
const rightOpen = ref(false)

// 移动端
const isMobile = ref(window.innerWidth < 768)
const mobileDrawer = ref<'left' | 'right' | null>(null)

window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) mobileDrawer.value = null
})

function toggleLeft() {
  if (isMobile.value) {
    mobileDrawer.value = mobileDrawer.value === 'left' ? null : 'left'
  } else {
    leftOpen.value = !leftOpen.value
  }
}

function toggleRight() {
  if (isMobile.value) {
    mobileDrawer.value = mobileDrawer.value === 'right' ? null : 'right'
  } else {
    rightOpen.value = !rightOpen.value
  }
}

// ==================== 角色 & 对话数据 ====================

const characterMap: Record<string, { name: string; avatar: string; desc: string }> = {
  c1: { name: '默认助手', avatar: '🤖', desc: '通用 AI 助理，可以回答各种问题' },
  c2: { name: '猫娘', avatar: '🐱', desc: '可爱的猫娘角色，善于卖萌和陪伴' },
  c3: { name: '代码导师', avatar: '👨‍💻', desc: '资深全栈工程师，擅长代码审查和教学' },
  c4: { name: '小说家', avatar: '✍️', desc: '创意写作专家，擅长构建世界观和故事' },
  c5: { name: '翻译官', avatar: '🌐', desc: '精通中英日三语的专业翻译' },
}

const mockReplies: Record<string, string[]> = {
  c1: [
    '你好！我是 InsvnterAI 的默认助手，有什么我可以帮助你的吗？',
    '这是一个很好的问题！让我来详细解答一下...',
    '我理解你的需求，让我为你整理一下思路。',
  ],
  c2: [
    '喵~ 主人你好呀！今天猫娘很开心能见到你喵！(=^・ω・^=)',
    '呜呜...主人不要忘了猫娘哦，猫娘会很努力的喵~',
    '主人想聊什么呀喵？猫娘什么都知道的哦！...大概吧喵~',
  ],
  c3: [
    '让我来看看这段代码。首先，你的整体架构思路是对的，但有几个地方可以优化...',
    '在软件工程中，这种设计模式叫做策略模式（Strategy Pattern）。核心思想是把算法封装起来，使它们可以互相替换。',
    '好的，作为代码导师，我建议你从以下几个方面入手学习：\n1. 先理解数据结构\n2. 掌握常用算法\n3. 多做项目实践',
  ],
  c4: [
    '一个好故事需要三个要素：引人入胜的开头、扣人心弦的冲突、出人意料的结局。让我来为你构思...',
    '黎明前的黑暗中，一个孤独的身影站在古老城堡的塔楼上，望着远方即将升起的第一缕阳光...',
    '角色的深度来源于矛盾。一个完美的角色是无趣的，正是缺陷让角色变得真实。',
  ],
  c5: [
    '好的，我来为你翻译。请注意，翻译不仅仅是字面转换，更重要的是传达原文的语气和意境。',
    'Translation complete. Here is the result with cultural context adapted for the target audience.',
    '这句话的翻译要注意语境。日语中「お疲れ様です」不只是"辛苦了"，更是一种社交礼仪。',
  ],
}

const activeCharacterId = ref<string>('c1')
const activeChatId = ref<string | null>(null)
const messages = ref<ChatMessage[]>([])
const loading = ref(false)
let msgCounter = 0

const defaultChar = { name: '默认助手', avatar: '🤖', desc: '通用 AI 助理，可以回答各种问题' }
const activeCharacter = computed(() => characterMap[activeCharacterId.value] ?? defaultChar)

function handleSelectCharacter(id: string) {
  activeCharacterId.value = id
  messages.value = []
  activeChatId.value = null
  if (isMobile.value) mobileDrawer.value = null
}

function handleNewChat() {
  messages.value = []
  activeChatId.value = null
}

function handleSelectChat(id: string) {
  activeChatId.value = id
  messages.value = [
    { id: 'loaded-1', role: 'user', content: '你好', timestamp: Date.now() - 60000 },
    { id: 'loaded-2', role: 'assistant', content: '你好！很高兴见到你，有什么需要我帮助的吗？', timestamp: Date.now() - 55000, characterName: activeCharacter.value.name },
  ]
  if (isMobile.value) mobileDrawer.value = null
}

let typingAbort = false

async function handleSend(content: string) {
  typingAbort = false
  const userMsg: ChatMessage = {
    id: `msg-${++msgCounter}`,
    role: 'user',
    content,
    timestamp: Date.now(),
  }
  messages.value.push(userMsg)

  loading.value = true
  const replies = mockReplies[activeCharacterId.value] ?? mockReplies.c1!
  const reply = replies[Math.floor(Math.random() * replies.length)]!

  await new Promise(r => setTimeout(r, 500 + Math.random() * 700))
  if (typingAbort) { loading.value = false; return }

  const aiMsg: ChatMessage = {
    id: `msg-${++msgCounter}`,
    role: 'assistant',
    content: '',
    timestamp: Date.now(),
    characterName: activeCharacter.value.name,
    typing: true,
  }
  messages.value.push(aiMsg)
  loading.value = false

  for (let i = 0; i < reply.length; i++) {
    if (typingAbort) break
    await new Promise(r => setTimeout(r, 15 + Math.random() * 25))
    aiMsg.content = reply.slice(0, i + 1)
  }
  aiMsg.typing = false
}

function handleStop() {
  typingAbort = true
  loading.value = false
}

// 用户菜单
function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const userMenuOptions = computed(() => [
  ...(auth.user?.role === 'ADMIN' ? [{ label: '管理控制台', key: 'console', icon: renderIcon(GridOutline) }] : []),
  { label: '返回首页', key: 'home', icon: renderIcon(HomeOutline) },
  { type: 'divider' as const, key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) },
])

async function handleUserMenu(key: string) {
  if (key === 'console') router.push('/console/manage')
  else if (key === 'home') router.push('/')
  else if (key === 'logout') { await auth.logout(); router.push('/') }
}
</script>

<template>
  <div class="chat-layout" :class="{ 'mobile': isMobile }">
    <!-- 顶栏 -->
    <header class="chat-header">
      <div class="header-left">
        <NButton quaternary circle size="small" @click="toggleLeft" class="panel-toggle">
          <template #icon><NIcon :component="MenuOutline" :size="20" /></template>
        </NButton>
        <div class="header-brand">
          <span class="brand-icon">✦</span>
          <span v-if="!isMobile" class="brand-text">InsvnterAI</span>
        </div>
      </div>

      <div class="header-center">
        <div class="chat-title-area" @click="toggleLeft">
          <span class="chat-avatar-small">{{ activeCharacter.avatar }}</span>
          <NText class="chat-title">{{ activeCharacter.name }}</NText>
        </div>
      </div>

      <div class="header-right">
        <NButton quaternary circle size="small" @click="theme.toggle()" class="panel-toggle">
          <template #icon>
            <NIcon :component="theme.isDark ? SunnyOutline : MoonOutline" :size="18" />
          </template>
        </NButton>
        <NButton quaternary circle size="small" @click="toggleRight" class="panel-toggle">
          <template #icon><NIcon :component="SettingsOutline" :size="20" /></template>
        </NButton>
        <NDropdown :options="userMenuOptions" trigger="click" @select="handleUserMenu">
          <NButton quaternary size="small" class="user-btn">
            <NSpace align="center" :size="6">
              <NAvatar round :size="26" color="#6366f1" style="font-size: 11px; font-weight: 600;">
                {{ auth.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </NAvatar>
              <span v-if="!isMobile" class="username">{{ auth.user?.username }}</span>
            </NSpace>
          </NButton>
        </NDropdown>
      </div>
    </header>

    <!-- 主体 -->
    <div class="chat-body">
      <!-- 桌面端左侧栏 -->
      <aside v-if="!isMobile && leftOpen" class="side-panel left-panel">
        <CharacterSidebar
          :active-character-id="activeCharacterId"
          :active-chat-id="activeChatId"
          @select-character="handleSelectCharacter"
          @select-chat="handleSelectChat"
          @new-chat="handleNewChat"
        />
      </aside>

      <!-- 中央聊天区 -->
      <main class="center-panel">
        <ChatArea
          :messages="messages"
          :loading="loading"
          :character-name="activeCharacter.name"
          :character-avatar="activeCharacter.avatar"
          @send="handleSend"
          @stop="handleStop"
        />
      </main>

      <!-- 桌面端右侧栏 -->
      <aside v-if="!isMobile && rightOpen" class="side-panel right-panel">
        <SettingsPanel
          :character-name="activeCharacter.name"
          :character-avatar="activeCharacter.avatar"
          :character-desc="activeCharacter.desc"
        />
      </aside>
    </div>

    <!-- 移动端抽屉 - 左侧 -->
    <NDrawer :width="300" placement="left"
      :show="mobileDrawer === 'left'" @update:show="(v: boolean) => { if(!v) mobileDrawer = null }">
      <NDrawerContent title="角色 & 对话" :native-scrollbar="false" body-content-style="padding: 0;">
        <CharacterSidebar
          :active-character-id="activeCharacterId"
          :active-chat-id="activeChatId"
          @select-character="handleSelectCharacter"
          @select-chat="handleSelectChat"
          @new-chat="handleNewChat"
        />
      </NDrawerContent>
    </NDrawer>

    <!-- 移动端抽屉 - 右侧 -->
    <NDrawer :width="320" placement="right"
      :show="mobileDrawer === 'right'" @update:show="(v: boolean) => { if(!v) mobileDrawer = null }">
      <NDrawerContent title="设置" :native-scrollbar="false" body-content-style="padding: 0;">
        <SettingsPanel
          :character-name="activeCharacter.name"
          :character-avatar="activeCharacter.avatar"
          :character-desc="activeCharacter.desc"
        />
      </NDrawerContent>
    </NDrawer>
  </div>
</template>

<style scoped>
.chat-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  user-select: none;
}

/* ========== 顶栏 ========== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 12px;
  flex-shrink: 0;
  z-index: 10;
}

[data-theme="dark"] .chat-header {
  background: rgba(15, 15, 22, 0.95);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .chat-header {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(12px);
}

.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 120px;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.chat-title-area {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}
[data-theme="dark"] .chat-title-area:hover { background: rgba(255,255,255,0.04); }
[data-theme="light"] .chat-title-area:hover { background: rgba(0,0,0,0.03); }

.chat-avatar-small {
  font-size: 18px;
}

.chat-title {
  font-weight: 600;
  font-size: 15px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}

.brand-icon {
  font-size: 18px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-text {
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.username {
  font-size: 13px;
  font-weight: 500;
}

[data-theme="dark"] .username { color: #d4d4d8; }
[data-theme="light"] .username { color: #3f3f46; }

/* ========== 主体 ========== */
.chat-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ========== 侧面板 ========== */
.side-panel {
  width: 280px;
  flex-shrink: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.right-panel {
  width: 300px;
}

[data-theme="dark"] .left-panel {
  background: rgba(12, 12, 18, 0.98);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .left-panel {
  background: #fafafa;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

[data-theme="dark"] .right-panel {
  background: rgba(12, 12, 18, 0.98);
  border-left: 1px solid rgba(255, 255, 255, 0.06);
}
[data-theme="light"] .right-panel {
  background: #fafafa;
  border-left: 1px solid rgba(0, 0, 0, 0.06);
}

/* ========== 中央 ========== */
.center-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

[data-theme="dark"] .center-panel {
  background: #0a0a0f;
}
[data-theme="light"] .center-panel {
  background: #ffffff;
}

/* ========== 交互 ========== */
.panel-toggle {
  opacity: 0.7;
  transition: opacity 0.2s;
}
.panel-toggle:hover {
  opacity: 1;
}

/* ========== 移动端 ========== */
@media (max-width: 768px) {
  .header-left { min-width: auto; }
  .header-right { min-width: auto; }
}
</style>
