<template>
  <div class="detail">
    <!-- 顶部栏 -->
    <v-app-bar color="surface" flat class="detail-top">
      <template #prepend>
        <v-btn icon variant="text" @click="goBack" aria-label="返回">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
      </template>
      <v-toolbar-title class="top-title">{{ detail.note?.title || '笔记详情' }}</v-toolbar-title>
      <template #append>
        <v-menu v-if="isAuthor" location="bottom end">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon variant="text" aria-label="更多">
              <v-icon>mdi-dots-horizontal</v-icon>
            </v-btn>
          </template>
          <v-list density="compact" class="pa-0">
            <v-list-item density="compact" @click="deleteDialog = true">
              <template #prepend>
                <v-icon color="error" size="small">mdi-delete</v-icon>
              </template>
              <v-list-item-title class="text-body-2">删除</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </v-app-bar>

    <div class="detail-body" :class="{ 'with-side': isDesktop }">
      <main class="main-col">
        <!-- 图片区：首图大图 + 其余小图 -->
        <section class="images" v-if="detail.images?.length">
          <v-img
            :src="currentImage"
            :alt="detail.note?.title"
            class="main-img rounded-xl"
            aspect-ratio="1"
            max-height="560"
            cover
            @click="openLightbox"
          />
          <div class="thumb-row" v-if="detail.images.length > 1">
            <v-img
              v-for="(img, i) in detail.images"
              :key="i"
              :src="img"
              class="thumb"
              :class="{ active: i === currentIdx }"
              aspect-ratio="1"
              cover
              @click="currentIdx = i"
            />
          </div>
        </section>

        <!-- 标题与内容 -->
        <section class="content">
          <h1 class="c-title">{{ detail.note?.title }}</h1>
          <p class="c-text">{{ detail.note?.content }}</p>
          <div class="topics" v-if="detail.topics?.length">
            <v-chip
              v-for="t in detail.topics"
              :key="t"
              label
              color="primary-container"
              size="small"
              class="topic"
            >
              #{{ t.replace('#', '') }}
            </v-chip>
          </div>
          <p class="time">
            <v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>
            {{ formatTime(detail.note?.createTime) }} · {{ detail.note?.viewCount }} 次浏览
          </p>
        </section>

        <!-- 作者模块（电脑端不显示，右侧已含） -->
        <section v-if="!isDesktop" class="author" @click="goAuthor">
          <v-avatar size="40" :image="detail.note?.author?.avatar" color="primary-container">
            {{ (detail.note?.author?.nickname || 'U').charAt(0) }}
          </v-avatar>
          <div class="author-info">
            <div class="nickname text-subtitle-1 font-weight-medium">{{ detail.note?.author?.nickname }}</div>
            <div class="desc text-caption">创作者 · 分享美好生活</div>
          </div>
          <v-btn
            size="small"
            variant="tonal"
            :color="followed ? 'primary' : 'primary'"
            @click.stop="toggleFollow"
          >
            {{ followed ? '已关注' : '关注' }}
          </v-btn>
        </section>
      </main>

      <!-- 电脑端右侧栏 -->
      <aside v-if="isDesktop" class="side-col">
        <section class="side-author" @click="goAuthor">
          <v-avatar size="48" :image="detail.note?.author?.avatar" color="primary-container">
            {{ (detail.note?.author?.nickname || 'U').charAt(0) }}
          </v-avatar>
          <div class="side-author-info">
            <div class="nickname text-subtitle-1 font-weight-medium">{{ detail.note?.author?.nickname }}</div>
            <div class="desc text-caption">创作者 · 分享美好生活</div>
          </div>
          <v-btn
            size="small"
            variant="tonal"
            :color="followed ? 'primary' : 'primary'"
            @click.stop="toggleFollow"
          >
            {{ followed ? '已关注' : '关注' }}
          </v-btn>
        </section>

        <!-- 评论列表 -->
        <section class="side-comments">
          <h3 class="comments-title text-h6">评论 {{ detail.comments?.length || 0 }}</h3>
          <CommentsList :comments="detail.comments || []" @reply="onReplyTarget" @refresh="loadDetail" />
        </section>
      </aside>
    </div>

    <!-- 评论区（移动端内嵌在正文下） -->
    <section v-if="!isDesktop" class="comments-section">
      <h3 class="comments-title text-h6">评论 {{ detail.comments?.length || 0 }}</h3>
      <CommentsList :comments="detail.comments || []" @reply="onReplyTarget" @refresh="loadDetail" />
    </section>

    <!-- 相关推荐 -->
    <section class="related">
      <h3 class="related-title text-h6">相关推荐</h3>
      <div class="related-grid">
        <NoteCard v-for="n in related" :key="n.id" :note="n" />
      </div>
      <v-empty v-if="!loading && !related.length" text="暂无相关推荐" icon="mdi-image-off-outline" />
    </section>

    <!-- 底部互动栏 -->
    <div class="action-bar" :class="{ 'with-side': isDesktop }">
      <div class="action" :class="{ active: detail.isLiked }" @click="toggleLike">
        <v-icon size="22">{{ detail.isLiked ? 'mdi-heart' : 'mdi-heart-outline' }}</v-icon>
        <span>{{ detail.note?.likeCount || 0 }}</span>
      </div>
      <div class="action" :class="{ active: detail.isCollected }" @click="toggleCollect">
        <v-icon size="22">{{ detail.isCollected ? 'mdi-bookmark' : 'mdi-bookmark-outline' }}</v-icon>
        <span>{{ detail.note?.collectCount || 0 }}</span>
      </div>
      <div class="action" @click="scrollToComments">
        <v-icon size="22">mdi-comment-outline</v-icon>
        <span>{{ detail.note?.commentCount || 0 }}</span>
      </div>

      <div class="comment-input">
        <v-text-field
          ref="commentInputRef"
          v-model="commentText"
          placeholder="说点什么…"
          density="comfortable"
          hide-details
          variant="outlined"
          @keyup.enter="submitComment"
          @focus="commenting = true"
          @blur="commenting = false"
        />
        <v-btn color="primary" @click="submitComment">发表</v-btn>
      </div>
    </div>

    <!-- 删除确认 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="xl">
        <v-card-title class="text-h6">删除提示</v-card-title>
        <v-card-text>确定删除这篇笔记吗？删除后不可恢复。</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="tonal" @click="confirmDelete">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 图片灯箱 -->
    <v-dialog v-model="lightboxOpen" fullscreen transition="fade-transition">
      <div class="lightbox" @click="lightboxOpen = false">
        <v-img :src="currentImage" :alt="detail.note?.title" max-height="90vh" contain />
      </div>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NoteCard from '@/components/NoteCard.vue'
import CommentsList from '@/components/CommentsList.vue'
import { toast } from '@/utils/toast'
import { noteApi } from '@/api/note'
import { interactApi } from '@/api/interact'
import { useUserStore } from '@/stores/user'
import { useViewport } from '@/composables/useViewport'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isDesktop } = useViewport()

const noteId = computed(() => Number(route.params.id))

const detail = ref({ note: {}, images: [], topics: [], comments: [], isLiked: false, isCollected: false })
const related = ref([])
const loading = ref(false)
const followed = ref(false)
const commentText = ref('')
const commenting = ref(false)
const replyTarget = ref(null)
const currentIdx = ref(0)
const currentImage = computed(() => detail.value.images?.[currentIdx.value] || detail.value.note?.coverUrl || '')
const commentInputRef = ref(null)
const deleteDialog = ref(false)
const lightboxOpen = ref(false)

const isAuthor = computed(
  () => userStore.isLogin && detail.value.note?.userId === userStore.userId
)

async function loadDetail() {
  loading.value = true
  try {
    const res = await noteApi.detail(noteId.value)
    detail.value = res?.data || {}
    currentIdx.value = 0
  } catch (e) {
    toast.error('笔记不存在或已被删除')
    setTimeout(() => router.replace('/'), 800)
  } finally {
    loading.value = false
  }
}

async function loadRelated() {
  try {
    const res = await noteApi.list({ page: 1, size: 6, category: 'hot' })
    related.value = (res?.data?.records || []).filter((n) => n.id !== noteId.value).slice(0, 6)
  } catch (e) {}
}

function requireLogin() {
  if (!userStore.isLogin) {
    toast.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function toggleLike() {
  if (!requireLogin()) return
  const wasLiked = detail.value.isLiked
  try {
    if (wasLiked) {
      await interactApi.unlike(noteId.value)
      detail.value.isLiked = false
      detail.value.note.likeCount = (detail.value.note.likeCount || 1) - 1
    } else {
      await interactApi.like(noteId.value)
      detail.value.isLiked = true
      detail.value.note.likeCount = (detail.value.note.likeCount || 0) + 1
    }
  } catch (e) {}
}

async function toggleCollect() {
  if (!requireLogin()) return
  const wasCollected = detail.value.isCollected
  try {
    if (wasCollected) {
      await interactApi.uncollect(noteId.value)
      detail.value.isCollected = false
      detail.value.note.collectCount = (detail.value.note.collectCount || 1) - 1
    } else {
      await interactApi.collect(noteId.value)
      detail.value.isCollected = true
      detail.value.note.collectCount = (detail.value.note.collectCount || 0) + 1
    }
  } catch (e) {}
}

function toggleFollow() {
  if (!requireLogin()) return
  followed.value = !followed.value
  toast.success(followed.value ? '关注成功' : '已取消关注')
}

function onReplyTarget(c) {
  if (!requireLogin()) return
  replyTarget.value = c
  commentText.value = `回复 @${c.user?.nickname || '用户'}：`
  focusComment()
}

async function submitComment() {
  const text = commentText.value.trim()
  if (!requireLogin()) return
  if (!text) {
    toast.warning('请输入评论内容')
    return
  }
  try {
    await interactApi.comment({
      noteId: noteId.value,
      content: text,
      parentId: replyTarget.value ? replyTarget.value.parentId || replyTarget.value.id || 0 : 0,
      replyUserId: replyTarget.value ? replyTarget.value.userId : 0
    })
    toast.success('评论成功')
    commentText.value = ''
    replyTarget.value = null
    // 重新拉取详情刷新评论
    await loadDetail()
  } catch (e) {}
}

async function confirmDelete() {
  try {
    await noteApi.remove(noteId.value)
    toast.success('删除成功')
    router.replace('/')
  } catch (e) {}
}

function openLightbox() {
  lightboxOpen.value = true
}

function scrollToComments() {
  const el = document.querySelector('.comments-section')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}
function focusComment() {
  commentInputRef.value?.focus()
}

function goBack() {
  if (window.history.length > 1) router.back()
  else router.replace('/')
}
function goAuthor() {
  if (detail.value.note?.userId) {
    router.push({ path: '/profile', query: { uid: detail.value.note.userId } })
  }
}

function formatTime(t) {
  if (!t) return ''
  const date = new Date(t)
  if (isNaN(date.getTime())) return t
  const now = Date.now()
  const diff = (now - date.getTime()) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 2592000) return `${Math.floor(diff / 86400)} 天前`
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadDetail()
  loadRelated()
})
</script>

<style scoped>
.detail {
  min-height: 100vh;
}
.detail-top {
  position: sticky;
  top: 0;
  z-index: 5;
  border-bottom: 1px solid rgb(var(--v-theme-outline-variant));
}
.top-title {
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-body {
  display: flex;
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}
.detail-body.with-side {
  align-items: flex-start;
}
.main-col {
  flex: 1;
  min-width: 0;
  max-width: 100%;
}
.images {
  margin-bottom: 20px;
}
.main-img {
  cursor: zoom-in;
  width: 100%;
  background: rgb(var(--v-theme-surface-variant));
}
.thumb-row {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  opacity: 0.7;
}
.thumb.active {
  border-color: rgb(var(--v-theme-primary));
  opacity: 1;
}
.content {
  margin-bottom: 16px;
}
.c-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 8px;
}
.c-text {
  font-size: 15px;
  line-height: 1.7;
  color: rgb(var(--v-theme-on-surface));
  word-break: break-word;
  white-space: pre-wrap;
  margin-bottom: 12px;
}
.topics {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.time {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.author,
.side-author {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgb(var(--v-theme-surface-variant));
  border-radius: 16px;
  cursor: pointer;
  margin-top: 16px;
}
.side-author {
  margin-top: 0;
}
.author-info,
.side-author-info {
  flex: 1;
  min-width: 0;
}
.nickname {
  color: rgb(var(--v-theme-on-surface));
}
.desc {
  color: rgb(var(--v-theme-on-surface-variant));
}
.side-col {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
}
.side-comments {
  margin-top: 24px;
}
.comments-title {
  margin-bottom: 12px;
  color: rgb(var(--v-theme-on-surface));
}
.comments-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 24px;
}
.related {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 120px;
}
.related-title {
  margin-bottom: 16px;
  color: rgb(var(--v-theme-on-surface));
}
.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 6;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: rgb(var(--v-theme-surface));
  border-top: 1px solid rgb(var(--v-theme-outline-variant));
}
.action-bar.with-side {
  left: 240px;
}
.action {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 14px;
}
.action.active {
  color: rgb(var(--v-theme-primary));
}
.action:hover {
  color: rgb(var(--v-theme-primary));
}
.comment-input {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.lightbox {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.9);
  cursor: zoom-out;
  padding: 16px;
}
</style>
