import { ref, computed, onMounted, onUnmounted } from 'vue'

// 响应式视口断点工具
// 断点：移动端 ≤768 / 平板 769-1200 / 电脑端 ≥1201
export function useViewport() {
  const width = ref(window.innerWidth)

  function update() {
    width.value = window.innerWidth
  }

  onMounted(() => window.addEventListener('resize', update))
  onUnmounted(() => window.removeEventListener('resize', update))

  const isMobile = computed(() => width.value <= 768)
  const isTablet = computed(() => width.value > 768 && width.value <= 1200)
  const isDesktop = computed(() => width.value >= 1201)

  return { width, isMobile, isTablet, isDesktop }
}
