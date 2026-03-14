<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { usePageTransition } from '@/composables/usePageTransition'

const theme = useThemeStore()
const isDark = computed(() => theme.isDark)
const { pageReady } = usePageTransition()

const emit = defineEmits<{
  (e: 'preload'): void
  (e: 'done'): void
}>()

const phase = ref(0)
const cardEntered = ref(false) // 卡片入场动画是否完成
let exiting = false

function triggerExit() {
  if (exiting) return
  exiting = true
  phase.value = 3
  setTimeout(() => { emit('done') }, 600)
}

onMounted(() => {
  // Phase 1: 立即开始入场动画
  requestAnimationFrame(() => { phase.value = 1 })

  // 200ms 后通知预加载（覆盖层已可见）
  setTimeout(() => { emit('preload') }, 200)

  // Phase 2: 700ms 后卡片稳定
  setTimeout(() => {
    phase.value = 2
    cardEntered.value = true
  }, 700)
})

// 响应式退出：卡片入场完成 + 页面就绪 → 立即退出
watch([cardEntered, pageReady], ([entered, ready]) => {
  if (entered && ready) triggerExit()
})
</script>

<template>
  <Teleport to="body">
    <div class="codm-transition" :class="[`phase-${phase}`, isDark ? 'dark' : 'light']">
      <!-- 六边形网格 -->
      <div class="hex-grid">
        <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="hexP" width="56" height="49" patternUnits="userSpaceOnUse" patternTransform="scale(1.5)">
              <path d="M28 0L56 14V35L28 49L0 35V14Z" fill="none" stroke-width="0.5" class="hex-stroke" />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#hexP)" />
        </svg>
      </div>

      <!-- 动态网格线（水平） -->
      <div class="grid-lines-h">
        <div v-for="i in 24" :key="'h'+i" class="g-line-h"
          :style="{ top: `${i * 4.166}%`, animationDelay: `${i * 0.04}s` }" />
      </div>

      <!-- 动态网格线（垂直） -->
      <div class="grid-lines-v">
        <div v-for="i in 32" :key="'v'+i" class="g-line-v"
          :style="{ left: `${i * 3.125}%`, animationDelay: `${i * 0.03}s` }" />
      </div>

      <!-- 对角线（扫掠式） -->
      <div class="diag-lines">
        <div v-for="i in 10" :key="'d'+i" class="diag-line"
          :style="{
            top: `${-10 + i * 12}%`,
            animationDelay: `${0.2 + i * 0.08}s`,
            opacity: 0.3 + Math.random() * 0.4,
          }" />
      </div>

      <!-- 扫光束 -->
      <div class="scan-beam" />
      <div class="v-scan" />

      <!-- 环境光 -->
      <div class="ambient" />

      <!-- ===== 名片 ===== -->
      <div class="card-stage">
        <div class="card">
          <!-- 全息水波纹 -->
          <div class="holo" />
          <!-- 电弧边框 -->
          <div class="arc" />

          <!-- 顶部 -->
          <div class="top-strip">
            <div class="strip-line" /><span>INSVNTER SYSTEMS</span><div class="strip-line" />
          </div>

          <!-- 主体 -->
          <div class="body">
            <div class="emblem">
              <div class="ring r1" />
              <div class="ring r2" />
              <div class="ring r3" />
              <span class="icon">✦</span>
            </div>
            <div class="info">
              <div class="name">InsvnterAI</div>
              <div class="rank">TITAN PROTOCOL · 巨神系统</div>
              <div class="desc">NEXT-GEN AI DIALOGUE PLATFORM</div>
            </div>
          </div>

          <!-- 底部 -->
          <div class="bot-strip">
            <div class="dot green" /><span>CONNECTED</span>
            <div class="mini-bar"><div class="mini-fill" /></div>
            <span class="ver">v2.0</span>
          </div>

          <!-- 角标线 -->
          <div class="cm tl" /><div class="cm tr" /><div class="cm bl" /><div class="cm br" />

          <!-- 闪白 -->
          <div class="flash" />

          <!-- 数据流线（卡片内） -->
          <div class="data-flow">
            <div v-for="i in 6" :key="'df'+i" class="df-line"
              :style="{ top: `${15 + i * 13}%`, animationDelay: `${0.5 + i * 0.15}s`, width: `${30 + Math.random() * 50}%` }" />
          </div>
        </div>
      </div>

      <!-- 光线爆发 -->
      <div class="rays">
        <div v-for="i in 12" :key="i" class="ray"
          :style="{ transform: `rotate(${i * 30}deg)`, animationDelay: `${i * 0.03}s` }" />
      </div>

      <!-- 粒子 -->
      <div class="sparks">
        <div v-for="i in 35" :key="i" class="sp"
          :style="{
            '--x': `${(Math.random()-0.5)*200}vw`,
            '--y': `${(Math.random()-0.5)*200}vh`,
            '--s': `${0.5+Math.random()*1.5}`,
            animationDelay: `${1.7+Math.random()*0.3}s`,
            left: '50%', top: '50%',
          }" />
      </div>

      <!-- 速度线 -->
      <div class="warps">
        <div v-for="i in 20" :key="i" class="wl"
          :style="{
            top: `${3 + i * 4.7}%`,
            animationDelay: `${Math.random()*0.12}s`,
            width: `${10+Math.random()*45}%`,
            left: `${Math.random()*50}%`,
          }" />
      </div>

      <!-- Glitch -->
      <div class="glitch" />
    </div>
  </Teleport>
</template>

<style scoped>
/* ========== 基础 ========== */
.codm-transition {
  position: fixed;
  inset: 0;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  animation: bgIn 0.25s ease;
}
.codm-transition.dark { background: #04040a; }
.codm-transition.light { background: #f0f0f5; }

@keyframes bgIn { from { opacity: 0; } to { opacity: 1; } }

/* ========== 颜色变量 ========== */
.dark { --accent: 99, 102, 241; --accent2: 167, 139, 250; --fg: #e4e4e7; --fg2: #a1a1aa; --bg-card: linear-gradient(135deg, #0c0c18, #111128, #0a0a1a); --line: rgba(99,102,241,0.06); }
.light { --accent: 79, 70, 229; --accent2: 124, 58, 237; --fg: #18181b; --fg2: #71717a; --bg-card: linear-gradient(135deg, #ffffff, #f5f3ff, #eef2ff); --line: rgba(79,70,229,0.08); }

/* ========== 六边形 ========== */
.hex-grid { position: absolute; inset: 0; opacity: 0; transition: opacity 0.6s; }
.phase-1 .hex-grid, .phase-2 .hex-grid { opacity: 1; }
.phase-3 .hex-grid { opacity: 0; transition: opacity 0.2s; }
.dark .hex-stroke { stroke: rgba(99,102,241,0.06); }
.light .hex-stroke { stroke: rgba(79,70,229,0.08); }

/* ========== 网格线 ========== */
.grid-lines-h, .grid-lines-v { position: absolute; inset: 0; pointer-events: none; }

.g-line-h {
  position: absolute; left: 0; right: 0; height: 1px;
  transform: scaleX(0); transform-origin: left;
  opacity: 0;
}
.dark .g-line-h { background: linear-gradient(90deg, transparent, rgba(99,102,241,0.08), transparent); }
.light .g-line-h { background: linear-gradient(90deg, transparent, rgba(79,70,229,0.1), transparent); }

.phase-1 .g-line-h { animation: lineRevealH 0.6s ease forwards; }

@keyframes lineRevealH {
  0% { transform: scaleX(0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: scaleX(1); opacity: 0.5; }
}

.g-line-v {
  position: absolute; top: 0; bottom: 0; width: 1px;
  transform: scaleY(0); transform-origin: top;
  opacity: 0;
}
.dark .g-line-v { background: linear-gradient(180deg, transparent, rgba(99,102,241,0.06), transparent); }
.light .g-line-v { background: linear-gradient(180deg, transparent, rgba(79,70,229,0.08), transparent); }

.phase-1 .g-line-v { animation: lineRevealV 0.7s ease forwards; }

@keyframes lineRevealV {
  0% { transform: scaleY(0); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: scaleY(1); opacity: 0.4; }
}

.phase-3 .g-line-h, .phase-3 .g-line-v { opacity: 0 !important; transition: opacity 0.2s; }

/* ========== 对角线 ========== */
.diag-lines { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }

.diag-line {
  position: absolute;
  left: -20%;
  width: 140%;
  height: 1px;
  transform: rotate(-35deg) scaleX(0);
  transform-origin: left;
  opacity: 0;
}
.dark .diag-line { background: linear-gradient(90deg, transparent 10%, rgba(167,139,250,0.12) 50%, transparent 90%); }
.light .diag-line { background: linear-gradient(90deg, transparent 10%, rgba(124,58,237,0.1) 50%, transparent 90%); }

.phase-1 .diag-line { animation: diagSlide 0.8s ease forwards; }
.phase-3 .diag-line { opacity: 0 !important; }

@keyframes diagSlide {
  0% { transform: rotate(-35deg) scaleX(0); opacity: 0; }
  30% { opacity: 1; }
  100% { transform: rotate(-35deg) scaleX(1); opacity: 0.6; }
}

/* ========== 扫光 ========== */
.scan-beam {
  position: absolute; top: 0; left: -100%; width: 100%; height: 100%;
  pointer-events: none;
}
.dark .scan-beam { background: linear-gradient(90deg, transparent, rgba(99,102,241,0.04) 45%, rgba(167,139,250,0.08) 50%, rgba(99,102,241,0.04) 55%, transparent); }
.light .scan-beam { background: linear-gradient(90deg, transparent, rgba(79,70,229,0.05) 45%, rgba(124,58,237,0.08) 50%, rgba(79,70,229,0.05) 55%, transparent); }
.phase-1 .scan-beam { animation: scanH 0.9s ease-in-out forwards; }
@keyframes scanH { to { left: 100%; } }

.v-scan {
  position: absolute; left: 0; top: -100%; width: 100%; height: 100%;
  pointer-events: none;
}
.dark .v-scan { background: linear-gradient(180deg, transparent, rgba(99,102,241,0.03) 45%, rgba(167,139,250,0.06) 50%, rgba(99,102,241,0.03) 55%, transparent); }
.light .v-scan { background: linear-gradient(180deg, transparent, rgba(79,70,229,0.04) 45%, rgba(124,58,237,0.06) 50%, rgba(79,70,229,0.04) 55%, transparent); }
.phase-1 .v-scan { animation: scanV 1.1s ease-in-out 0.2s forwards; }
@keyframes scanV { to { top: 100%; } }

/* ========== 环境光 ========== */
.ambient {
  position: absolute; width: 600px; height: 400px; left: 50%; top: 50%;
  transform: translate(-50%,-50%); filter: blur(50px); pointer-events: none;
  opacity: 0; transition: opacity 0.5s;
}
.dark .ambient { background: radial-gradient(ellipse, rgba(99,102,241,0.12), transparent 70%); }
.light .ambient { background: radial-gradient(ellipse, rgba(79,70,229,0.08), transparent 70%); }
.phase-1 .ambient, .phase-2 .ambient { opacity: 1; }
.phase-3 .ambient { opacity: 0; }

/* ========== 名片 ========== */
.card-stage { perspective: 1400px; z-index: 2; }

.card {
  position: relative; width: 520px; height: 280px;
  border-radius: 14px; overflow: hidden;
  background: var(--bg-card);
  transform: translateX(120vw) rotateY(-20deg) scale(0.6);
  opacity: 0;
  transition: transform 0.65s cubic-bezier(0.16,1,0.3,1), opacity 0.35s ease;
}

.dark .card {
  border: 1px solid rgba(99,102,241,0.15);
  box-shadow: 0 0 50px rgba(99,102,241,0.12), 0 0 100px rgba(99,102,241,0.04), inset 0 0 60px rgba(99,102,241,0.03);
}
.light .card {
  border: 1px solid rgba(79,70,229,0.12);
  box-shadow: 0 4px 60px rgba(79,70,229,0.1), 0 0 100px rgba(79,70,229,0.04);
}

.phase-1 .card { transform: translateX(20px) rotateY(-6deg) scale(1.02); opacity: 1; }
.phase-2 .card { transform: translateX(0) rotateY(0) scale(1); transition: transform 0.45s cubic-bezier(0.22,1,0.36,1); }
.phase-3 .card { transform: scale(4) translateY(-15vh); opacity: 0; filter: blur(10px); transition: all 0.45s cubic-bezier(0.4,0,1,1); }

/* 全息 */
.holo {
  position: absolute; inset: 0;
  background:
    repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(var(--accent),0.02) 2px, rgba(var(--accent),0.02) 4px),
    repeating-linear-gradient(90deg, transparent, transparent 50px, rgba(var(--accent),0.015) 50px, rgba(var(--accent),0.015) 51px);
  animation: holoScroll 3s linear infinite;
}
@keyframes holoScroll { to { background-position: 0 40px; } }

/* 电弧 */
.arc {
  position: absolute; inset: -1px; border-radius: 15px; z-index: -1; opacity: 0;
  background: conic-gradient(from 0deg, transparent 0%, rgba(var(--accent),0.5) 8%, transparent 16%, transparent 34%, rgba(var(--accent2),0.4) 42%, transparent 50%, transparent 68%, rgba(var(--accent),0.3) 76%, transparent 84%, transparent 100%);
  animation: arcR 1.8s linear infinite;
}
.phase-1 .arc, .phase-2 .arc { opacity: 1; }
@keyframes arcR { to { transform: rotate(360deg); } }

/* 顶部条 */
.top-strip {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 18px 0; font-size: 9px;
  letter-spacing: 4px; font-weight: 600;
  color: rgba(var(--accent),0.45);
  opacity: 0; transform: translateY(-6px);
  transition: all 0.35s ease 0.25s;
}
.phase-1 .top-strip, .phase-2 .top-strip { opacity: 1; transform: translateY(0); }

.strip-line { flex: 1; height: 1px; background: linear-gradient(90deg, transparent, rgba(var(--accent),0.25), transparent); }

/* 主体 */
.body {
  display: flex; align-items: center; gap: 24px;
  padding: 20px 28px; opacity: 0; transform: translateX(16px);
  transition: all 0.4s ease 0.4s;
}
.phase-1 .body, .phase-2 .body { opacity: 1; transform: translateX(0); }

/* 徽章 */
.emblem {
  position: relative; width: 84px; height: 84px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.ring {
  position: absolute; border-radius: 50%;
}
.ring.r1 { inset: 0; border: 2px solid rgba(var(--accent),0.25); animation: rp 2.2s ease-in-out infinite; }
.ring.r2 { inset: 6px; border: 1px solid rgba(var(--accent),0.12); animation: rp 2.2s ease-in-out 0.3s infinite reverse; }
.ring.r3 { inset: -5px; border: 1px dashed rgba(var(--accent),0.08); animation: dashSpin 8s linear infinite; }
@keyframes rp { 0%,100% { transform: scale(1); opacity:1; } 50% { transform: scale(1.06); opacity:0.6; } }
@keyframes dashSpin { to { transform: rotate(360deg); } }

.icon {
  font-size: 38px;
  background: linear-gradient(135deg, rgb(var(--accent)), rgb(var(--accent2)), #c4b5fd);
  -webkit-background-clip: text; background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 14px rgba(var(--accent),0.5));
}

/* 信息 */
.info { display: flex; flex-direction: column; gap: 3px; }

.name {
  font-size: 30px; font-weight: 800; letter-spacing: 3px;
  background-size: 200% auto;
  -webkit-background-clip: text; background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: txtShine 2.5s linear infinite;
}
.dark .name { background-image: linear-gradient(90deg, #e4e4e7 0%, #a5b4fc 50%, #e4e4e7 100%); }
.light .name { background-image: linear-gradient(90deg, #18181b 0%, #4f46e5 50%, #18181b 100%); }
@keyframes txtShine { to { background-position: 200% center; } }

.rank {
  font-size: 12px; font-weight: 700; letter-spacing: 4px;
  color: rgb(var(--accent));
  text-shadow: 0 0 8px rgba(var(--accent),0.35);
}

.desc { font-size: 10px; letter-spacing: 2px; color: var(--fg2); margin-top: 3px; opacity: 0.6; }

/* 底部 */
.bot-strip {
  display: flex; align-items: center; gap: 7px;
  padding: 0 18px 12px; font-size: 9px; letter-spacing: 2px;
  color: rgba(var(--accent),0.4);
  opacity: 0; transition: opacity 0.35s ease 0.5s;
}
.phase-1 .bot-strip, .phase-2 .bot-strip { opacity: 1; }

.dot { width: 5px; height: 5px; border-radius: 50%; animation: blink 1.4s ease-in-out infinite; }
.dot.green { background: #22c55e; box-shadow: 0 0 6px rgba(34,197,94,0.6); }
@keyframes blink { 0%,100% { opacity:1; } 50% { opacity:0.3; } }

.mini-bar { width: 70px; height: 2px; border-radius: 1px; overflow: hidden; margin-left: auto; background: rgba(var(--accent),0.1); }
.mini-fill { height: 100%; border-radius: 1px; background: linear-gradient(90deg, rgb(var(--accent)), rgb(var(--accent2))); width: 0%; animation: mf 1.6s ease-in-out 0.5s forwards; }
@keyframes mf { to { width: 100%; } }

.ver { font-size: 8px; opacity: 0.5; }

/* 角标 */
.cm {
  position: absolute; width: 14px; height: 14px; opacity: 0;
  transition: opacity 0.3s ease 0.6s;
}
.cm::before, .cm::after { content: ''; position: absolute; background: rgba(var(--accent),0.25); }
.cm::before { height: 1.5px; width: 100%; }
.cm::after { width: 1.5px; height: 100%; }
.phase-1 .cm, .phase-2 .cm { opacity: 1; }

.cm.tl { top: 8px; left: 8px; } .cm.tl::before { top:0; left:0; } .cm.tl::after { top:0; left:0; }
.cm.tr { top: 8px; right: 8px; } .cm.tr::before { top:0; right:0; } .cm.tr::after { top:0; right:0; }
.cm.bl { bottom: 8px; left: 8px; } .cm.bl::before { bottom:0; left:0; } .cm.bl::after { bottom:0; left:0; }
.cm.br { bottom: 8px; right: 8px; } .cm.br::before { bottom:0; right:0; } .cm.br::after { bottom:0; right:0; }

/* 闪白 */
.flash { position: absolute; inset: 0; pointer-events: none; opacity: 0; }
.dark .flash { background: white; }
.light .flash { background: rgba(79,70,229,0.15); }
.phase-2 .flash { animation: fp 0.25s ease-out; }
@keyframes fp { 0% { opacity:0.5; } 100% { opacity:0; } }

/* 卡片内数据流 */
.data-flow { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.df-line {
  position: absolute; height: 1px; left: 0;
  transform: scaleX(0); transform-origin: left;
  opacity: 0;
}
.dark .df-line { background: linear-gradient(90deg, transparent, rgba(99,102,241,0.12), transparent); }
.light .df-line { background: linear-gradient(90deg, transparent, rgba(79,70,229,0.1), transparent); }
.phase-1 .df-line, .phase-2 .df-line { animation: dfAnim 1s ease forwards; }
@keyframes dfAnim { 0% { transform: scaleX(0); opacity:0; } 40% { opacity:0.8; } 100% { transform: scaleX(1); opacity:0.3; } }

/* ========== 光线 ========== */
.rays { position: absolute; left: 50%; top: 50%; transform: translate(-50%,-50%); pointer-events: none; z-index: 1; }
.ray {
  position: absolute; width: 2px; height: 0; left: 0; top: 0;
  transform-origin: center top; opacity: 0;
}
.dark .ray { background: linear-gradient(to bottom, rgba(99,102,241,0.5), transparent); }
.light .ray { background: linear-gradient(to bottom, rgba(79,70,229,0.4), transparent); }
.phase-2 .ray { animation: rayB 0.5s ease-out forwards; }
@keyframes rayB { 0% { height:0; opacity:0.7; } 100% { height:220px; opacity:0; } }

/* ========== 粒子 ========== */
.sparks { position: absolute; inset: 0; pointer-events: none; z-index: 3; }
.sp {
  position: absolute; width: 3px; height: 3px; border-radius: 50%; opacity: 0;
}
.dark .sp { background: #a78bfa; box-shadow: 0 0 5px rgba(167,139,250,0.5); }
.light .sp { background: #6366f1; box-shadow: 0 0 5px rgba(99,102,241,0.4); }
.phase-3 .sp { animation: spF 0.55s ease-out forwards; }
@keyframes spF { 0% { opacity:1; transform: translate(0,0) scale(var(--s,1)); } 100% { opacity:0; transform: translate(var(--x),var(--y)) scale(0); } }

/* ========== 速度线 ========== */
.warps { position: absolute; inset: 0; pointer-events: none; z-index: 4; opacity: 0; }
.phase-3 .warps { opacity: 1; }
.wl {
  position: absolute; height: 1.5px; opacity: 0;
}
.dark .wl { background: linear-gradient(90deg, transparent, rgba(99,102,241,0.5), transparent); }
.light .wl { background: linear-gradient(90deg, transparent, rgba(79,70,229,0.4), transparent); }
.phase-3 .wl { animation: wlD 0.35s ease-out forwards; opacity: 1; }
@keyframes wlD { 0% { transform: scaleX(0); } 100% { transform: scaleX(3.5); opacity:0; } }

/* ========== Glitch ========== */
.glitch { position: absolute; inset: 0; pointer-events: none; z-index: 5; opacity: 0; }
.phase-1 .glitch { animation: gf 0.08s ease 3; }
@keyframes gf {
  0%,100% { opacity:0; }
  50% { opacity:1; background: linear-gradient(transparent 0%, rgba(var(--accent),0.03) 49.5%, transparent 50.5%, transparent 100%); transform: translateX(3px); }
}

/* ========== 最终消散 ========== */
.phase-3 { animation: fadeAll 0.5s ease 0.15s forwards; }
@keyframes fadeAll { to { opacity: 0; } }

/* ========== 移动端 ========== */
@media (max-width: 600px) {
  .card { width: 340px; height: 185px; }
  .body { padding: 14px 18px; gap: 14px; }
  .emblem { width: 54px; height: 54px; }
  .icon { font-size: 24px; }
  .name { font-size: 20px; letter-spacing: 2px; }
  .rank { font-size: 9px; letter-spacing: 3px; }
  .desc { font-size: 8px; }
  .top-strip { padding: 8px 12px 0; font-size: 8px; }
  .bot-strip { padding: 0 12px 8px; }
}
</style>
