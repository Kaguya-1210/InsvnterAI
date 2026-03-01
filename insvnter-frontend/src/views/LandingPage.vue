<script setup lang="ts">
import { NButton, NIcon, NGradientText } from 'naive-ui'
import {
  RocketOutline,
  ChatbubblesOutline,
  ColorPaletteOutline,
  CodeSlashOutline,
  ExtensionPuzzleOutline,
  ShieldCheckmarkOutline,
} from '@vicons/ionicons5'
import AppNavbar from '@/components/AppNavbar.vue'

const features = [
  {
    icon: ChatbubblesOutline,
    title: 'AI 智能对话',
    desc: '基于 SillyTavern 的高级对话引擎，支持多种 AI 模型接入与自定义对话流程',
  },
  {
    icon: ColorPaletteOutline,
    title: '角色扮演系统',
    desc: '丰富的角色卡创建与管理，支持自定义人设、世界观和对话风格',
  },
  {
    icon: ExtensionPuzzleOutline,
    title: '插件化架构',
    desc: '灵活的插件系统，轻松扩展功能模块，满足个性化需求',
  },
  {
    icon: CodeSlashOutline,
    title: '现代技术栈',
    desc: 'Vue 3 + Spring Boot 3 前后端分离，TypeScript 全栈类型安全',
  },
  {
    icon: ShieldCheckmarkOutline,
    title: '安全可控',
    desc: '私有化部署，数据完全自主掌控，支持多用户权限管理',
  },
  {
    icon: RocketOutline,
    title: '高性能',
    desc: '流式响应、WebSocket 实时通信，毫秒级交互体验',
  },
]
</script>

<template>
  <div class="landing">
    <AppNavbar />

    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-badge">🚀 基于 SillyTavern 二次开发</div>
        <h1>
          <NGradientText
            type="info"
            :size="56"
            gradient="linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%)"
          >
            InsvnterAI
          </NGradientText>
        </h1>
        <p class="hero-subtitle">
          新一代 AI 对话与角色扮演平台<br />
          强大、灵活、可扩展的私有化解决方案
        </p>
        <div class="hero-actions">
          <NButton
            size="large"
            round
            quaternary
            tag="a"
            href="https://github.com/SillyTavern/SillyTavern"
            target="_blank"
          >
            了解 SillyTavern →
          </NButton>
        </div>
      </div>
      <div class="hero-glow"></div>
    </section>

    <!-- Features -->
    <section class="features">
      <h2 class="section-title">核心能力</h2>
      <div class="features-grid">
        <div v-for="f in features" :key="f.title" class="feature-card">
          <div class="feature-icon">
            <NIcon :size="28" :component="f.icon" />
          </div>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- Tech Stack -->
    <section class="tech-stack">
      <h2 class="section-title">技术栈</h2>
      <div class="tech-tags">
        <span v-for="t in ['Vue 3', 'TypeScript', 'Vite', 'Naive UI', 'Pinia', 'Spring Boot 3', 'Java 17', 'Maven']" :key="t" class="tech-tag">
          {{ t }}
        </span>
      </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
      <p>InsvnterAI &copy; {{ new Date().getFullYear() }} &mdash; Powered by SillyTavern</p>
    </footer>
  </div>
</template>

<style scoped>
.landing {
  min-height: 100vh;
  overflow-x: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* ============ HERO ============ */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 2rem;
  text-align: center;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
}

.hero-badge {
  display: inline-block;
  padding: 6px 16px;
  margin-bottom: 2rem;
  font-size: 14px;
  border-radius: 999px;
}

.hero-content h1 {
  margin: 0 0 1.5rem;
  font-size: 56px;
  font-weight: 800;
  letter-spacing: -1px;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: 20px;
  line-height: 1.7;
  margin: 0 0 2.5rem;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

/* ============ FEATURES ============ */
.features {
  padding: 6rem 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 3rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.feature-card {
  padding: 32px;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 12px;
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
}

.feature-card p {
  font-size: 14px;
  line-height: 1.7;
  margin: 0;
}

/* ============ TECH STACK ============ */
.tech-stack {
  padding: 4rem 2rem 6rem;
  text-align: center;
}

.tech-tags {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  max-width: 700px;
  margin: 0 auto;
}

.tech-tag {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 999px;
  transition: all 0.2s ease;
}

.tech-tag:hover {
  color: #a78bfa;
  border-color: rgba(139, 92, 246, 0.3);
  background: rgba(139, 92, 246, 0.08);
}

/* ============ FOOTER ============ */
.landing-footer {
  padding: 2rem;
  text-align: center;
}

.landing-footer p {
  margin: 0;
  font-size: 13px;
}

/* ============ DARK THEME ============ */
[data-theme="dark"] .landing {
  background: #0a0a0f;
  color: #e4e4e7;
}

[data-theme="dark"] .hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgba(99, 102, 241, 0.15), transparent),
    radial-gradient(ellipse 60% 40% at 70% 80%, rgba(139, 92, 246, 0.08), transparent);
}

[data-theme="dark"] .hero-glow {
  position: absolute;
  top: 30%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  filter: blur(60px);
  pointer-events: none;
}

[data-theme="dark"] .hero-badge {
  color: #a78bfa;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
}

[data-theme="dark"] .hero-subtitle {
  color: #a1a1aa;
}

[data-theme="dark"] .section-title {
  color: #f4f4f5;
}

[data-theme="dark"] .feature-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

[data-theme="dark"] .feature-card:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .feature-card h3 {
  color: #f4f4f5;
}

[data-theme="dark"] .feature-card p {
  color: #a1a1aa;
}

[data-theme="dark"] .tech-tag {
  color: #d4d4d8;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

[data-theme="dark"] .landing-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

[data-theme="dark"] .landing-footer p {
  color: #71717a;
}

/* ============ LIGHT THEME ============ */
[data-theme="light"] .landing {
  background: #ffffff;
  color: #18181b;
}

[data-theme="light"] .hero-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgba(99, 102, 241, 0.08), transparent),
    radial-gradient(ellipse 60% 40% at 70% 80%, rgba(139, 92, 246, 0.04), transparent);
}

[data-theme="light"] .hero-glow {
  position: absolute;
  top: 30%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%);
  filter: blur(60px);
  pointer-events: none;
}

[data-theme="light"] .hero-badge {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.08);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

[data-theme="light"] .hero-subtitle {
  color: #71717a;
}

[data-theme="light"] .section-title {
  color: #18181b;
}

[data-theme="light"] .feature-card {
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

[data-theme="light"] .feature-card:hover {
  background: #f5f3ff;
  border-color: rgba(99, 102, 241, 0.25);
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.08);
}

[data-theme="light"] .feature-card h3 {
  color: #18181b;
}

[data-theme="light"] .feature-card p {
  color: #71717a;
}

[data-theme="light"] .tech-tag {
  color: #3f3f46;
  background: #f4f4f5;
  border: 1px solid #e4e4e7;
}

[data-theme="light"] .landing-footer {
  border-top: 1px solid #f0f0f0;
}

[data-theme="light"] .landing-footer p {
  color: #a1a1aa;
}
</style>
