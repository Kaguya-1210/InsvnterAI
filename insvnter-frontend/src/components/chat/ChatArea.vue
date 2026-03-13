<script setup lang="ts">
import { ref, nextTick, watch, onMounted } from 'vue'
import { NInput, NButton, NIcon, NSpace, NScrollbar, NText, NSpin } from 'naive-ui'
import { SendOutline, StopCircleOutline } from '@vicons/ionicons5'

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  characterName?: string
  typing?: boolean
}

const props = defineProps<{
  messages: ChatMessage[]
  loading?: boolean
  characterName?: string
  characterAvatar?: string
}>()

const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'stop'): void
}>()

const inputText = ref('')
const scrollRef = ref<InstanceType<typeof NScrollbar> | null>(null)

function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.loading) return
  emit('send', text)
  inputText.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 自动滚动到底部
watch(() => props.messages.length, () => {
  nextTick(() => {
    scrollRef.value?.scrollTo({ top: 99999, behavior: 'smooth' })
  })
})

onMounted(() => {
  nextTick(() => {
    scrollRef.value?.scrollTo({ top: 99999 })
  })
})

function formatTime(ts: number) {
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="chat-area">
    <!-- 消息列表 -->
    <NScrollbar ref="scrollRef" class="message-scroll">
      <div class="message-list">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-avatar">{{ characterAvatar || '✦' }}</div>
          <h2 class="welcome-title">{{ characterName || 'InsvnterAI' }}</h2>
          <p class="welcome-desc">选择一个角色开始对话，或直接发送消息</p>
          <div class="welcome-hints">
            <div class="hint-card" @click="emit('send', '你好，介绍一下你自己')">
              💬 你好，介绍一下你自己
            </div>
            <div class="hint-card" @click="emit('send', '帮我写一段有趣的故事')">
              ✍️ 帮我写一段有趣的故事
            </div>
            <div class="hint-card" @click="emit('send', '今天有什么新闻？')">
              📰 今天有什么新闻？
            </div>
          </div>
        </div>

        <!-- 消息气泡 -->
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="msg.role"
        >
          <!-- AI 头像 -->
          <div v-if="msg.role === 'assistant'" class="msg-avatar">
            {{ characterAvatar || '🤖' }}
          </div>

          <div class="msg-bubble-wrap">
            <div class="msg-meta" v-if="msg.role === 'assistant'">
              <span class="msg-name">{{ msg.characterName || characterName || 'AI' }}</span>
              <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="msg-meta user-meta" v-if="msg.role === 'user'">
              <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
              <span class="msg-name">你</span>
            </div>
            <div class="msg-bubble" :class="msg.role">
              <span class="msg-text">{{ msg.content }}</span>
              <span v-if="msg.typing" class="typing-cursor">▍</span>
            </div>
          </div>

          <!-- 用户头像 -->
          <div v-if="msg.role === 'user'" class="msg-avatar user-avatar">
            👤
          </div>
        </div>

        <!-- 加载指示器 -->
        <div v-if="loading" class="message-row assistant">
          <div class="msg-avatar">{{ characterAvatar || '🤖' }}</div>
          <div class="msg-bubble-wrap">
            <div class="msg-bubble assistant">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </NScrollbar>

    <!-- 输入区 -->
    <div class="input-area">
      <div class="input-wrap">
        <textarea
          v-model="inputText"
          class="chat-input"
          rows="1"
          placeholder="发送消息... (Shift+Enter 换行)"
          @keydown="handleKeydown"
          :disabled="loading"
        />
        <NButton
          v-if="loading"
          circle
          type="error"
          size="small"
          class="send-btn"
          @click="emit('stop')"
        >
          <template #icon><NIcon :component="StopCircleOutline" /></template>
        </NButton>
        <NButton
          v-else
          circle
          type="primary"
          size="small"
          class="send-btn"
          :disabled="!inputText.trim()"
          @click="handleSend"
        >
          <template #icon><NIcon :component="SendOutline" /></template>
        </NButton>
      </div>
      <p class="input-hint">InsvnterAI 可能会产生不准确的信息</p>
    </div>
  </div>
</template>

<style scoped>
.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.message-scroll {
  flex: 1;
}

.message-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px 12px;
}

/* ========== 欢迎页 ========== */
.welcome {
  text-align: center;
  padding: 60px 20px;
}

.welcome-avatar {
  font-size: 48px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #6366f1, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-desc {
  font-size: 14px;
  margin: 0 0 32px;
}
[data-theme="dark"] .welcome-desc { color: #71717a; }
[data-theme="light"] .welcome-desc { color: #a1a1aa; }

.welcome-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 500px;
  margin: 0 auto;
}

.hint-card {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
[data-theme="dark"] .hint-card {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.06);
  color: #d4d4d8;
}
[data-theme="dark"] .hint-card:hover {
  background: rgba(99,102,241,0.1);
  border-color: rgba(99,102,241,0.2);
}
[data-theme="light"] .hint-card {
  background: #f4f4f5;
  border: 1px solid #e4e4e7;
  color: #3f3f46;
}
[data-theme="light"] .hint-card:hover {
  background: #ede9fe;
  border-color: rgba(99,102,241,0.2);
}

/* ========== 消息 ========== */
.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 18px;
}
[data-theme="dark"] .msg-avatar { background: rgba(255,255,255,0.06); }
[data-theme="light"] .msg-avatar { background: #f0f0f0; }

[data-theme="dark"] .user-avatar { background: rgba(99,102,241,0.15); }
[data-theme="light"] .user-avatar { background: rgba(99,102,241,0.1); }

.msg-bubble-wrap {
  max-width: 75%;
  min-width: 60px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 12px;
}
[data-theme="dark"] .msg-meta { color: #71717a; }
[data-theme="light"] .msg-meta { color: #a1a1aa; }

.msg-name { font-weight: 600; font-size: 12px; }
[data-theme="dark"] .msg-name { color: #a1a1aa; }
[data-theme="light"] .msg-name { color: #52525b; }

.msg-time { font-size: 11px; }

.user-meta {
  justify-content: flex-end;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

[data-theme="dark"] .msg-bubble.assistant {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.06);
  color: #e4e4e7;
  border-top-left-radius: 4px;
}
[data-theme="light"] .msg-bubble.assistant {
  background: #f4f4f5;
  border: 1px solid #e4e4e7;
  color: #18181b;
  border-top-left-radius: 4px;
}

[data-theme="dark"] .msg-bubble.user {
  background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(139,92,246,0.15));
  border: 1px solid rgba(99,102,241,0.2);
  color: #e4e4e7;
  border-top-right-radius: 4px;
}
[data-theme="light"] .msg-bubble.user {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #ffffff;
  border-top-right-radius: 4px;
}

/* 打字光标 */
.typing-cursor {
  animation: blink 0.8s infinite;
  color: #6366f1;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 打字动画点 */
.typing-dots {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  animation: dotPulse 1.4s infinite ease-in-out;
}
[data-theme="dark"] .typing-dots span { background: #71717a; }
[data-theme="light"] .typing-dots span { background: #a1a1aa; }

.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes dotPulse {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

/* ========== 输入区 ========== */
.input-area {
  flex-shrink: 0;
  padding: 12px 20px 16px;
}

.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 800px;
  margin: 0 auto;
  padding: 8px 12px;
  border-radius: 16px;
  transition: all 0.2s;
}

[data-theme="dark"] .input-wrap {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
}
[data-theme="dark"] .input-wrap:focus-within {
  border-color: rgba(99,102,241,0.3);
  box-shadow: 0 0 0 2px rgba(99,102,241,0.08);
}

[data-theme="light"] .input-wrap {
  background: #f9fafb;
  border: 1px solid #e4e4e7;
}
[data-theme="light"] .input-wrap:focus-within {
  border-color: rgba(99,102,241,0.3);
  box-shadow: 0 0 0 2px rgba(99,102,241,0.06);
}

.chat-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  max-height: 120px;
  font-family: inherit;
  padding: 4px 0;
}
[data-theme="dark"] .chat-input { color: #e4e4e7; }
[data-theme="light"] .chat-input { color: #18181b; }
[data-theme="dark"] .chat-input::placeholder { color: #52525b; }
[data-theme="light"] .chat-input::placeholder { color: #a1a1aa; }

.send-btn {
  flex-shrink: 0;
}

.input-hint {
  text-align: center;
  font-size: 11px;
  margin: 8px 0 0;
}
[data-theme="dark"] .input-hint { color: #3f3f46; }
[data-theme="light"] .input-hint { color: #d4d4d8; }

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .message-list { padding: 16px 12px 8px; }
  .input-area { padding: 8px 12px 12px; }
  .msg-bubble-wrap { max-width: 85%; }
  .welcome { padding: 40px 12px; }
  .welcome-title { font-size: 24px; }
}
</style>
