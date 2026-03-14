import { ref, nextTick } from 'vue'

// 全局转场状态 — 独立于路由生命周期
const visible = ref(false)
const pageReady = ref(false)
let _navigateFn: (() => void) | null = null

export function usePageTransition() {
  /**
   * 播放转场动画并在预加载阶段执行导航
   * @param navigateFn 导航回调，会在动画开始时调用
   */
  function play(navigateFn: () => void) {
    _navigateFn = navigateFn
    pageReady.value = false
    visible.value = true
  }

  function handlePreload() {
    _navigateFn?.()
    _navigateFn = null
    // 路由导航 + 组件渲染后标记页面就绪
    nextTick(() => {
      requestAnimationFrame(() => {
        pageReady.value = true
      })
    })
  }

  function handleDone() {
    visible.value = false
    pageReady.value = false
  }

  return { visible, pageReady, play, handlePreload, handleDone }
}
