<template>
  <div class="home">
    <!-- 顶部导航（PC 版侧边栏） -->
    <SideNav v-if="isDesktop" />

    <div class="home-main" :class="{ 'with-side': isDesktop }">
      <!-- MD3 顶部导航栏 -->
      <header class="top-nav">
        <span class="logo" @click="go('/')">此刻</span>
        <div class="search-box" @click="focusSearch">
          <v-text-field
            ref="searchInput"
            v-model="keyword"
            placeholder="搜索笔记、作者…"
            prepend-inner-icon="mdi-magnify"
            density="compact"
            variant="solo-filled"
            hide-details
            clearable
            rounded="pill"
            class="search-field"
            @keyup.enter="onSearch"
          />
        </div>
        <v-btn
          v-if="isDesktop"
          class="publish-btn"
          color="primary"
          rounded="pill"
          @click="go('/publish')"
        >
          <v-icon icon="mdi-plus" size="18" />
          <span>发布</span>
        </v-btn>
      </header>

      <!-- 分类标签（MD3 pill chip） -->
      <div class="category-wrap">
        <v-chip
          v-for="cat in categories"
          :key="cat.value"
          class="cat-chip"
          :color="category === cat.value ? 'primary' : 'surface-variant'"
          :text-color="category === cat.value ? 'on-primary' : 'on-surface-variant'"
          :variant="category === cat.value ? 'flat' : 'tonal'"
          rounded="pill"
          @click="switchCategory(cat.value)"
        >
          {{ cat.label }}
        </v-chip>
      </div>

      <!-- 内容流 -->
      <main
        ref="listRef"
        class="content"
        :class="{ 'pull-ready': pulling }"
        @scroll="onScroll"
        @touchstart="onTouchStart"
        @touchmove="onTouchMove"
        @touchend="onTouchEnd"
        @touchcancel="onTouchEnd"
      >
        <!-- 下拉刷新提示 -->
        <div class="pull-hint" :class="{ visible: pulling }">
          <v-icon v-if="!refreshing" :class="{ spin: pulling }" icon="mdi-refresh" size="16" />
          <span>{{ refreshing ? '刷新中…' : pullText }}</span>
        </div>

        <!-- 搜索状态条 -->
        <div v-if="isSearching" class="search-status">
          搜索“{{ keyword }}” 共 {{ total }} 条结果
          <span class="clear" @click="clearSearch">清除搜索</span>
        </div>

        <!-- 瀑布流（CSS Grid） -->
        <div v-if="notes.length" class="waterfall" :class="{ 'waterfall-desktop': isDesktop }">
          <NoteCard v-for="note in notes" :key="note.id" :note="note" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty">
          <v-icon icon="mdi-image-off-outline" size="48" color="outline" />
          <p class="empty-text">还没有内容，去看看别的吧~</p>
          <v-btn color="primary" variant="tonal" rounded="pill" @click="reload">刷新看看</v-btn>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="load-state">
          <v-progress-circular indeterminate :size="24" color="primary" />
        </div>
        <div v-else-if="notes.length && finished" class="load-state end">没有更多内容啦</div>
      </main>

      <!-- 底部 tab（移动/平板） -->
      <TabBar v-if="!isDesktop" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import NoteCard from '@/components/NoteCard.vue'
import TabBar from '@/components/TabBar.vue'
import SideNav from '@/components/SideNav.vue'
import { noteApi } from '@/api/note'
import { useViewport } from '@/composables/useViewport'

const router = useRouter()
const { isDesktop, isMobile } = useViewport()

const categories = [
  { label: '推荐', value: 'recommend' },
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'hot' },
  { label: '美食', value: '美食' },
  { label: '穿搭', value: '穿搭' },
  { label: '风景', value: '风景' },
  { label: '干货', value: '干货' }
]

const notes = ref([])
const category = ref('recommend')
const keyword = ref('')
const isSearching = ref(false)
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const listRef = ref()
const searchInput = ref()

// ------- 触控下拉刷新 -------
const pulling = ref(false)
const pullText = ref('下拉刷新')
const touchStartY = ref(0)
const pullingDistance = ref(0)

function onTouchStart(e) {
  if (listRef.value.scrollTop <= 0) {
    touchStartY.value = e.touches[0].clientY
  } else {
    touchStartY.value = -1
  }
}
function onTouchMove(e) {
  if (touchStartY.value < 0) return
  const delta = e.touches[0].clientY - touchStartY.value
  if (delta > 0) {
    pullingDistance.value = Math.min(delta, 80)
    pulling.value = true
    pullText.value = pullingDistance.value > 50 ? '松开刷新' : '下拉刷新'
  }
}
function onTouchEnd() {
  if (!pulling.value) return
  pulling.value = false
  if (pullingDistance.value > 50) {
    refresh()
  }
  pullingDistance.value = 0
}

// ------- 数据加载 -------
async function fetchNotes(isRefresh = false) {
  if (loading.value) return
  loading.value = true
  try {
    const params = {
      page: isRefresh ? 1 : page.value,
      size,
      category: category.value
    }
    if (isSearching.value && keyword.value) {
      params.keyword = keyword.value
    }
    const res = await noteApi.list(params)
    const data = res?.data || {}
    const records = data.records || []
    total.value = data.total || 0
    if (isRefresh) {
      notes.value = records
      page.value = 2
    } else {
      notes.value.push(...records)
      page.value += 1
    }
    finished.value = notes.value.length >= total.value
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function refresh() {
  refreshing.value = true
  fetchNotes(true)
}

function reload() {
  page.value = 1
  notes.value = []
  finished.value = false
  fetchNotes(true)
}

function switchCategory(value) {
  category.value = value
  isSearching.value = false
  reload()
}

function onSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    isSearching.value = false
    reload()
    return
  }
  isSearching.value = true
  reload()
}

function clearSearch() {
  keyword.value = ''
  isSearching.value = false
  reload()
}

function focusSearch() {
  if (isDesktop.value) searchInput.value?.focus?.()
}

function onScroll() {
  const el = listRef.value
  if (!el || loading.value || finished.value) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 80) {
    fetchNotes(false)
  }
}

function go(path) {
  router.push(path)
}

onMounted(() => {
  fetchNotes(true)
})
onUnmounted(() => {})
</script>

<style scoped>
.home {
  min-height: 100vh;
}
.home-main {
  min-height: 100vh;
}
.with-side {
  margin-left: 220px;
}
.top-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  background: rgba(251, 250, 249, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgb(var(--v-theme-outline-variant));
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: rgb(var(--v-theme-primary));
  letter-spacing: 2px;
  cursor: pointer;
}
.search-box {
  flex: 1;
  max-width: 480px;
}
.search-field {
  border-radius: 24px;
}
.publish-btn {
  flex-shrink: 0;
}
.category-wrap {
  position: sticky;
  top: var(--header-h);
  z-index: 40;
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  background: rgb(var(--v-theme-background));
  overflow-x: auto;
  white-space: nowrap;
}
.cat-chip {
  flex-shrink: 0;
}
.content {
  overflow-y: auto;
  padding: 4px 20px calc(60px + env(safe-area-inset-bottom));
  height: calc(100vh - var(--header-h) - 110px);
  -webkit-overflow-scrolling: touch;
}
.pull-hint {
  height: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 13px;
  transition: height 0.2s ease;
}
.pull-hint.visible,
.pull-ready .pull-hint {
  height: 40px;
}
.spin {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}
.search-status {
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
  padding: 8px 4px;
}
.search-status .clear {
  color: rgb(var(--v-theme-primary));
  cursor: pointer;
  margin-left: 8px;
}
/* CSS Grid 瀑布流：固定图片高度 + object-fit，替代多列布局 */
.waterfall {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  align-items: start;
}
.waterfall-desktop {
  grid-template-columns: repeat(3, 1fr);
}
@media (min-width: 1600px) {
  .waterfall-desktop {
    grid-template-columns: repeat(4, 1fr);
  }
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: rgb(var(--v-theme-on-surface-variant));
}
.empty-text {
  font-size: 14px;
}
.load-state {
  text-align: center;
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 13px;
  padding: 16px 0;
}
.load-state.end {
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
