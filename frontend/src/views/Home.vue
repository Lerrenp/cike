<template>
  <div class="home">
    <!-- 顶部导航（PC 版侧边栏） -->
    <SideNav v-if="isDesktop" />

    <div class="home-main" :class="{ 'with-side': isDesktop }">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <span class="logo" @click="go('/')">此刻</span>
        <div class="search-box" @click="focusSearch">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            ref="searchInput"
            v-model="keyword"
            placeholder="搜索笔记、作者…"
            clearable
            @keyup.enter="onSearch"
          />
        </div>
        <button v-if="isDesktop" class="publish-btn" @click="go('/publish')">＋ 发布</button>
      </header>

      <!-- 分类标签 -->
      <div class="category-wrap">
        <div
          v-for="cat in categories"
          :key="cat.value"
          class="cat-item"
          :class="{ active: category === cat.value }"
          @click="switchCategory(cat.value)"
        >
          {{ cat.label }}
        </div>
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
          <el-icon v-if="!refreshing" :class="{ spin: pulling }"><Refresh /></el-icon>
          <span>{{ refreshing ? '刷新中…' : pullText }}</span>
        </div>

        <!-- 搜索状态条 -->
        <div v-if="isSearching" class="search-status">
          搜索“{{ keyword }}” 共 {{ total }} 条结果
          <span class="clear" @click="clearSearch">清除搜索</span>
        </div>

        <!-- 瀑布流 -->
        <div v-if="notes.length" class="waterfall">
          <NoteCard v-for="note in notes" :key="note.id" :note="note" />
        </div>

        <!-- 空状态 -->
        <el-empty v-else-if="!loading" description="还没有内容，去看看别的吧~">
          <el-button type="primary" @click="reload">刷新看看</el-button>
        </el-empty>

        <!-- 加载状态 -->
        <div v-if="loading" class="load-state"><span class="spinner" /></div>
        <div v-else-if="notes.length" class="load-state end">{{ finished ? '没有更多内容啦' : '加载中…' }}</div>
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
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--cike-border);
}
.logo {
  font-size: 22px;
  font-weight: 800;
  color: var(--cike-primary);
  letter-spacing: 2px;
  cursor: pointer;
}
.search-box {
  flex: 1;
  max-width: 480px;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 0 12px;
}
.search-box .search-icon {
  color: var(--cike-text-3);
  margin-right: 6px;
}
.search-box :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: none;
  padding: 0;
}
.search-box :deep(.el-input__inner) {
  height: 36px;
}
.publish-btn {
  border: none;
  background: var(--cike-primary);
  color: #fff;
  border-radius: 20px;
  padding: 8px 18px;
  cursor: pointer;
  font-weight: 600;
}
.publish-btn:hover {
  background: var(--cike-primary-dark);
}
.category-wrap {
  position: sticky;
  top: var(--header-h);
  z-index: 40;
  display: flex;
  gap: 10px;
  padding: 12px 20px;
  background: var(--cike-bg);
  overflow-x: auto;
  white-space: nowrap;
}
.cat-item {
  flex-shrink: 0;
  padding: 6px 16px;
  border-radius: 18px;
  font-size: 14px;
  color: var(--cike-text-2);
  background: #fff;
  cursor: pointer;
  transition: all 0.15s ease;
  border: 1px solid var(--cike-border);
}
.cat-item:hover {
  color: var(--cike-primary);
}
.cat-item.active {
  background: var(--cike-primary);
  color: #fff;
  border-color: var(--cike-primary);
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
  color: var(--cike-text-3);
  font-size: 13px;
  transition: height 0.2s ease;
}
.pull-hint.visible {
  height: 40px;
}
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
  color: var(--cike-text-2);
  padding: 8px 4px;
}
.search-status .clear {
  color: var(--cike-primary);
  cursor: pointer;
  margin-left: 8px;
}
.waterfall {
  columns: 2;
  column-gap: 12px;
}
@media (min-width: 769px) {
  .waterfall {
    columns: 3;
  }
}
@media (min-width: 1201px) {
  .waterfall {
    columns: 4;
  }
}
.load-state {
  text-align: center;
  color: var(--cike-text-3);
  font-size: 13px;
  padding: 16px 0;
}
.load-state.end {
  color: var(--cike-text-3);
}
.spinner {
  display: inline-block;
  width: 22px;
  height: 22px;
  border: 2px solid #eee;
  border-top-color: var(--cike-primary);
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
}
</style>
