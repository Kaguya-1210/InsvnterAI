<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  NCard, NGrid, NGi, NButton, NSpace, NIcon, NTag, NModal, NInput, NSpin,
  NDivider, NEmpty, useMessage, useDialog,
} from 'naive-ui'
import { AddOutline, TrashOutline, CreateOutline, ArrowBackOutline } from '@vicons/ionicons5'
import { adminApi } from '@/api'

interface Template {
  id: string; name: string; description: string; htmlContent: string; builtIn: boolean;
}

const message = useMessage()
const dialog = useDialog()
const loading = ref(true)
const templates = ref<Template[]>([])

// 编辑状态
const editing = ref<Template | null>(null)
const editHtml = ref('')
const editName = ref('')
const editDesc = ref('')
const saving = ref(false)

// 新建弹窗
const showCreate = ref(false)
const newName = ref('')
const creating = ref(false)

onMounted(loadTemplates)

async function loadTemplates() {
  loading.value = true
  try {
    const res = await adminApi.getEmailTemplates()
    templates.value = res.data
  } catch { /* ignore */ }
  loading.value = false
}

function startEdit(t: Template) {
  editing.value = t
  editHtml.value = t.htmlContent
  editName.value = t.name
  editDesc.value = t.description
}

function cancelEdit() {
  editing.value = null
}

async function handleSave() {
  if (!editing.value) return
  saving.value = true
  try {
    await adminApi.saveEmailTemplate(editing.value.id, {
      name: editName.value,
      description: editDesc.value,
      htmlContent: editHtml.value,
    })
    message.success('模板已保存')
    editing.value.htmlContent = editHtml.value
    editing.value.name = editName.value
    editing.value.description = editDesc.value
    cancelEdit()
    loadTemplates()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  }
  saving.value = false
}

async function handleCreate() {
  if (!newName.value.trim()) { message.warning('请输入模板名称'); return }
  creating.value = true
  try {
    await adminApi.createEmailTemplate({ name: newName.value.trim() })
    message.success('模板已创建')
    showCreate.value = false
    newName.value = ''
    loadTemplates()
  } catch (e: any) {
    message.error(e.message || '创建失败')
  }
  creating.value = false
}

function handleDelete(t: Template) {
  dialog.warning({
    title: '删除模板',
    content: `确定要删除「${t.name}」吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await adminApi.deleteEmailTemplate(t.id)
        message.success('模板已删除')
        loadTemplates()
      } catch (e: any) {
        message.error(e.message || '删除失败')
      }
    },
  })
}

// 预览 HTML（将变量替换为示例值）
function previewHtml(html: string) {
  return html
    .replace(/\{\{code\}\}/g, '846293')
    .replace(/\{\{username\}\}/g, 'TestUser')
    .replace(/\{\{minutes\}\}/g, '5')
}
</script>

<template>
  <div class="template-page">
    <!-- 编辑模式 -->
    <template v-if="editing">
      <div class="editor-header">
        <NButton text @click="cancelEdit">
          <template #icon><NIcon :component="ArrowBackOutline" /></template>
          返回列表
        </NButton>
        <h2 class="page-title" style="margin: 12px 0 4px;">编辑模板</h2>
        <NSpace style="margin-bottom: 16px; gap: 12px;">
          <NInput v-model:value="editName" placeholder="模板名称" style="width: 200px;" />
          <NInput v-model:value="editDesc" placeholder="描述" style="width: 300px;" />
          <NButton type="primary" :loading="saving" @click="handleSave">保存</NButton>
        </NSpace>
      </div>

      <div class="editor-layout">
        <div class="editor-pane">
          <div class="pane-header">HTML 编辑器</div>
          <textarea
            v-model="editHtml"
            class="html-editor"
            spellcheck="false"
            placeholder="在此编辑 HTML..."
          />
          <p class="editor-hint" v-pre>可用变量: <code>{{code}}</code> <code>{{username}}</code> <code>{{minutes}}</code></p>
        </div>
        <div class="preview-pane">
          <div class="pane-header">实时预览</div>
          <iframe
            class="preview-frame"
            :srcdoc="previewHtml(editHtml)"
            sandbox="allow-same-origin"
          />
        </div>
      </div>
    </template>

    <!-- 列表模式 -->
    <template v-else>
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;">
        <div>
          <h2 class="page-title">邮件模板</h2>
          <p class="page-desc">管理邮件 HTML 模板，支持可视化编辑和实时预览</p>
        </div>
        <NButton type="primary" @click="showCreate = true">
          <template #icon><NIcon :component="AddOutline" /></template>
          新建模板
        </NButton>
      </div>

      <NSpin :show="loading" style="min-height: 200px;">
        <NEmpty v-if="!loading && templates.length === 0" description="暂无模板" />
        <NGrid v-else :cols="2" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
          <NGi v-for="t in templates" :key="t.id" span="2 m:1">
            <NCard class="template-card" hoverable>
              <template #header>
                <NSpace align="center" :size="8">
                  {{ t.name }}
                  <NTag v-if="t.builtIn" size="small" type="info" round>内置</NTag>
                </NSpace>
              </template>
              <template #header-extra>
                <NSpace :size="4">
                  <NButton size="small" quaternary circle @click="startEdit(t)">
                    <template #icon><NIcon :component="CreateOutline" /></template>
                  </NButton>
                  <NButton v-if="!t.builtIn" size="small" quaternary circle type="error" @click="handleDelete(t)">
                    <template #icon><NIcon :component="TrashOutline" /></template>
                  </NButton>
                </NSpace>
              </template>

              <p class="template-desc">{{ t.description || '无描述' }}</p>
              <div class="template-preview-wrap">
                <iframe :srcdoc="previewHtml(t.htmlContent)" class="template-preview" sandbox="allow-same-origin" />
              </div>
            </NCard>
          </NGi>
        </NGrid>
      </NSpin>
    </template>

    <!-- 新建弹窗 -->
    <NModal v-model:show="showCreate" preset="dialog" title="新建模板" positive-text="创建" negative-text="取消"
      :loading="creating" @positive-click="handleCreate">
      <NInput v-model:value="newName" placeholder="模板名称" style="margin-top: 12px;" />
    </NModal>
  </div>
</template>

<style scoped>
.template-page { max-width: 1200px; }
.page-title { font-size: 24px; font-weight: 700; margin: 0 0 4px; }
.page-desc { font-size: 14px; color: #a1a1aa; margin: 0; }

.template-card { transition: all 0.2s ease; }
.template-desc { font-size: 13px; color: #a1a1aa; margin: 0 0 12px; }

.template-preview-wrap {
  border-radius: 8px; overflow: hidden;
  border: 1px solid rgba(255,255,255,0.06);
  height: 200px;
}
.template-preview {
  width: 100%; height: 100%; border: none;
  pointer-events: none; background: #fff;
}

/* 编辑器布局 */
.editor-layout {
  display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
  height: calc(100vh - 260px); min-height: 400px;
}
.editor-pane, .preview-pane {
  display: flex; flex-direction: column;
  border-radius: 12px; overflow: hidden;
}

[data-theme="dark"] .editor-pane,
[data-theme="dark"] .preview-pane {
  border: 1px solid rgba(255,255,255,0.08);
  background: rgba(30,30,40,0.6);
}
[data-theme="light"] .editor-pane,
[data-theme="light"] .preview-pane {
  border: 1px solid rgba(0,0,0,0.08);
  background: rgba(250,250,255,0.8);
}

.pane-header {
  padding: 10px 16px; font-size: 13px; font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
[data-theme="dark"] .pane-header { color: #a1a1aa; }
[data-theme="light"] .pane-header { color: #52525b; border-bottom-color: rgba(0,0,0,0.06); }

.html-editor {
  flex: 1; padding: 16px; border: none; outline: none; resize: none;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px; line-height: 1.6; tab-size: 2;
}
[data-theme="dark"] .html-editor { background: transparent; color: #e4e4e7; }
[data-theme="light"] .html-editor { background: transparent; color: #18181b; }

.editor-hint {
  padding: 8px 16px; font-size: 11px; margin: 0;
  border-top: 1px solid rgba(255,255,255,0.06);
}
[data-theme="dark"] .editor-hint { color: #71717a; }
[data-theme="light"] .editor-hint { color: #a1a1aa; border-top-color: rgba(0,0,0,0.06); }
.editor-hint code {
  padding: 1px 6px; border-radius: 4px; font-size: 11px;
}
[data-theme="dark"] .editor-hint code { background: rgba(99,102,241,0.15); color: #a78bfa; }
[data-theme="light"] .editor-hint code { background: rgba(99,102,241,0.1); color: #6366f1; }

.preview-frame { flex: 1; border: none; background: #fff; }

@media (max-width: 768px) {
  .editor-layout { grid-template-columns: 1fr; }
}
</style>
