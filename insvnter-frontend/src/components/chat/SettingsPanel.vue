<script setup lang="ts">
import { ref } from 'vue'
import {
  NScrollbar, NSpace, NText, NSlider, NInput, NSelect, NDivider,
  NSwitch, NTag, NIcon, NCollapse, NCollapseItem,
} from 'naive-ui'
import { PersonOutline, FlashOutline, SettingsOutline, GlobeOutline } from '@vicons/ionicons5'

const props = defineProps<{
  characterName?: string
  characterAvatar?: string
  characterDesc?: string
}>()

// 模型参数 (mock)
const temperature = ref(0.9)
const topP = ref(0.95)
const maxTokens = ref(2048)
const repetitionPenalty = ref(1.1)
const streamMode = ref(true)

// API 设置 (mock)
const apiType = ref('openai')
const apiTypeOptions = [
  { label: 'OpenAI', value: 'openai' },
  { label: 'Claude', value: 'claude' },
  { label: 'OpenRouter', value: 'openrouter' },
  { label: 'Ollama (本地)', value: 'ollama' },
  { label: '自定义', value: 'custom' },
]
const apiUrl = ref('')
const apiKey = ref('')
const modelName = ref('gpt-4o')
</script>

<template>
  <div class="settings-panel">
    <NScrollbar>
      <div class="settings-content">
        <!-- 角色信息 -->
        <div class="section char-info">
          <div class="char-avatar-large">{{ characterAvatar || '🤖' }}</div>
          <h3 class="char-name">{{ characterName || '默认助手' }}</h3>
          <p class="char-desc">{{ characterDesc || '通用 AI 助理，可以回答各种问题' }}</p>
        </div>

        <NDivider style="margin: 12px 0;" />

        <NCollapse :default-expanded-names="['api', 'params']" arrow-placement="right">
          <!-- API 连接 -->
          <NCollapseItem name="api">
            <template #header>
              <NSpace align="center" :size="6">
                <NIcon :component="GlobeOutline" :size="16" />
                <NText style="font-weight: 600; font-size: 13px;">API 连接</NText>
              </NSpace>
            </template>

            <div class="field-group">
              <div class="field">
                <label>API 类型</label>
                <NSelect v-model:value="apiType" :options="apiTypeOptions" size="small" />
              </div>
              <div class="field">
                <label>API 地址</label>
                <NInput v-model:value="apiUrl" placeholder="https://api.openai.com/v1" size="small" />
              </div>
              <div class="field">
                <label>API Key</label>
                <NInput v-model:value="apiKey" type="password" show-password-on="click" placeholder="sk-..." size="small" />
              </div>
              <div class="field">
                <label>模型</label>
                <NInput v-model:value="modelName" placeholder="gpt-4o" size="small" />
              </div>
            </div>
          </NCollapseItem>

          <!-- 生成参数 -->
          <NCollapseItem name="params">
            <template #header>
              <NSpace align="center" :size="6">
                <NIcon :component="FlashOutline" :size="16" />
                <NText style="font-weight: 600; font-size: 13px;">生成参数</NText>
              </NSpace>
            </template>

            <div class="field-group">
              <div class="field slider-field">
                <div class="field-header">
                  <label>温度 (Temperature)</label>
                  <NTag size="tiny" round :bordered="false">{{ temperature.toFixed(2) }}</NTag>
                </div>
                <NSlider v-model:value="temperature" :min="0" :max="2" :step="0.01" />
                <p class="field-hint">越高越有创造力，越低越精确</p>
              </div>

              <div class="field slider-field">
                <div class="field-header">
                  <label>Top P</label>
                  <NTag size="tiny" round :bordered="false">{{ topP.toFixed(2) }}</NTag>
                </div>
                <NSlider v-model:value="topP" :min="0" :max="1" :step="0.01" />
              </div>

              <div class="field slider-field">
                <div class="field-header">
                  <label>最大 Token</label>
                  <NTag size="tiny" round :bordered="false">{{ maxTokens }}</NTag>
                </div>
                <NSlider v-model:value="maxTokens" :min="256" :max="8192" :step="256" />
              </div>

              <div class="field slider-field">
                <div class="field-header">
                  <label>重复惩罚</label>
                  <NTag size="tiny" round :bordered="false">{{ repetitionPenalty.toFixed(2) }}</NTag>
                </div>
                <NSlider v-model:value="repetitionPenalty" :min="1" :max="2" :step="0.01" />
              </div>

              <div class="field switch-field">
                <label>流式输出</label>
                <NSwitch v-model:value="streamMode" size="small" />
              </div>
            </div>
          </NCollapseItem>

          <!-- 角色设定 -->
          <NCollapseItem name="persona">
            <template #header>
              <NSpace align="center" :size="6">
                <NIcon :component="PersonOutline" :size="16" />
                <NText style="font-weight: 600; font-size: 13px;">角色设定</NText>
              </NSpace>
            </template>

            <div class="field-group">
              <div class="field">
                <label>系统提示 (System Prompt)</label>
                <NInput
                  type="textarea"
                  :rows="4"
                  placeholder="你是一个友好的 AI 助手..."
                  size="small"
                />
              </div>
              <div class="field">
                <label>用户人设</label>
                <NInput
                  type="textarea"
                  :rows="2"
                  placeholder="描述你自己的身份..."
                  size="small"
                />
              </div>
            </div>
          </NCollapseItem>
        </NCollapse>
      </div>
    </NScrollbar>
  </div>
</template>

<style scoped>
.settings-panel {
  height: 100%;
}

.settings-content {
  padding: 16px;
}

/* 角色信息 */
.char-info {
  text-align: center;
  padding: 8px 0;
}

.char-avatar-large {
  font-size: 48px;
  margin-bottom: 8px;
}

.char-name {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 4px;
}
[data-theme="dark"] .char-name { color: #f4f4f5; }
[data-theme="light"] .char-name { color: #18181b; }

.char-desc {
  font-size: 13px;
  margin: 0;
  line-height: 1.5;
}
[data-theme="dark"] .char-desc { color: #71717a; }
[data-theme="light"] .char-desc { color: #a1a1aa; }

/* 字段 */
.field-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
}
[data-theme="dark"] .field label { color: #a1a1aa; }
[data-theme="light"] .field label { color: #52525b; }

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.field-hint {
  font-size: 11px;
  margin: 4px 0 0;
}
[data-theme="dark"] .field-hint { color: #52525b; }
[data-theme="light"] .field-hint { color: #a1a1aa; }

.switch-field {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-field label {
  margin-bottom: 0;
}
</style>
