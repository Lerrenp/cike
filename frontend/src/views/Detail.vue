<template>
  <div class="detail">
    <!-- 顶部栏 -->
    <header class="detail-top">
      <span class="back" @click="goBack"><el-icon><ArrowLeft /></el-icon></span>
      <span class="top-title">{{ detail.note?.title || '笔记详情' }}</span>
      <el-dropdown v-if="isAuthor" trigger="click" @command="onMore">
        <span class="more"><el-icon><MoreFilled /></el-icon></span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="edit">编辑</el-dropdown-item>
            <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <div class="detail-body" :class="{ 'with-side': isDesktop }">
      <main class="main-col">
        <!-- 图片区：首图大图 + 其余小图 -->
        <section class="images" v-if="detail.images?.length">
          <div class="main-img">
            <img :src="currentImage" :alt="detail.note?.title" @click="openLightbox" />
          </div>
          <div class="thumb-row" v-if="detail.images.length > 1">
            <img
              v-for="(img, i) in detail.images"
              :key="i"
              :src="img"
              :class="{ active: i === currentIdx }"
              @click="currentIdx = i"
            />
          </div>
        </section>

        <!-- 标题与内容 -->
        <section class="content">
          <h1 class="c-title">{{ detail.note?.title }}</h1>
          <p class="c-text">{{ detail.note?.content }}</p>
          <div class="topics" v-if="detail.topics?.length">
            <span v-for="t in detail.topics" :key="t" class="topic">#{{ t.replace('#', '') }}</span>
          </div>
          <p class="time">{{ formatTime(detail.note?.createTime) }} · {{ detail.note?.viewCount }} 次浏览</p>
        </section>

        <!-- 作者模块（电脑端不显示，右侧已含） -->
        <section v-if="!isDesktop" class="author" @click="goAuthor">
          <el-avatar :size="40" :src="detail.note?.author?.avatar">
            {{ (detail.note?.author?.nickname || 'U').charAt(0) }}
          </el-avatar>
          <div class="author-info">
            <div class="nickname">{{ detail.note?.author?.nickname }}</div>
            <div class="desc">创作者 · 分享美好生活</div>
          </div>
          <button class="follow-btn" :class="{ followed: followed }" @click.stop="toggleFollow">
            {{ followed ? '已关注' : '关注' }}
          </button>
        </section>
      </main>

      <!-- 电脑端右侧栏 -->
      <aside v-if="isDesktop" class="side-col">
        <section class="side-author" @click="goAuthor">
          <el-avatar :size="48" :src="detail.note?.author?.avatar">
            {{ (detail.note?.author?.nickname || 'U').charAt(0) }}
          </el-avatar>
          <div class="side-author-info">
            <div class="nickname">{{ detail.note?.author?.nickname }}</div>
            <div class="desc">创作者 · 分享美好生活</div>
          </div>
          <button class="follow-btn" :class="{ followed: followed }" @click.stop="toggleFollow">
            {{ followed ? '已关注' : '关注' }}
          </button>
        </section>

        <!-- 评论列表 -->
        <section class="side-comments">
          <h3 class="comments-title">评论 {{ detail.comments?.length || 0 }}</h3>
          <CommentsList :comments="detail.comments || []" @reply="onReplyTarget" @refresh="loadDetail" />
        </section>
      </aside>
    </div>

    <!-- 评论区（移动端内嵌在正文下） -->
    <section v-if="!isDesktop" class="comments-section">
      <h3 class="comments-title">评论 {{ detail.comments?.length || 0 }}</h3>
      <CommentsList :comments="detail.comments || []" @reply="onReplyTarget" @refresh="loadDetail" />
    </section>

    <!-- 相关推荐 -->
    <section class="related">
      <h3 class="related-title">相关推荐</h3>
      <div class="related-grid">
        <NoteCard v-for="n in related" :key="n.id" :note="n" />
      </div>
      <el-empty v-if="!loading && !related.length" :image-size="80" description="暂无相关推荐" />
    </section>

    <!-- 底部互动栏 -->
    <div class="action-bar" :class="{ 'with-side': isDesktop }">
      <div class="action" :class="{ active: detail.isLiked }" @click="toggleLike">
        <el-icon :size="22"><component :is="detail.isLiked ? 'StarFilled' : 'Star'" /></el-icon>
        <span>{{ detail.note?.likeCount || 0 }}</span>
      </div>
      <div class="action" :class="{ active: detail.isCollected }" @click="toggleCollect">
        <el-icon :size="22"><component :is="detail.isCollected ? 'CollectionTag' : 'CollectionTag'" /></el-icon>
        <span>{{ detail.note?.collectCount || 0 }}</span>
      </div>
      <div class="action" @click="scrollToComments">
        <el-icon :size="22"><ChatDotRound /></el-icon>
        <span>{{ detail.note?.commentCount || 0 }}</span>
      </div>

      <div class="comment-input" @click="focusComment">
        <el-input
          v-model="commentText"
          placeholder="说点什么…"
          @keyup.enter="submitComment"
          @focus="commenting = true"
          @blur="commenting = false"
        />
        <el-button type="primary" @click="submitComment">发表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NoteCard from '@/components/NoteCard.vue'
import CommentsList from '@/components/CommentsList.vue'
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
    ElMessage.error('笔记不存在或已被删除')
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
    ElMessage.warning('请先登录')
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
  ElMessage.success(followed.value ? '关注成功' : '已取消关注')
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
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    await interactApi.comment({
      noteId: noteId.value,
      content: text,
      parentId: replyTarget.value ? replyTarget.value.parentId || replyTarget.value.id || 0 : 0,
      replyUserId: replyTarget.value ? replyTarget.value.userId : 0
    })
    ElMessage.success('评论成功')
    commentText.value = ''
    replyTarget.value = null
    // 重新拉取详情刷新评论
    await loadDetail()
  } catch (e) {}
}

async function onMore(command) {
  if (command === 'edit') {
    router.push('/publish')
  } else if (command === 'delete') {
    ElMessageBox.confirm('确定删除这篇笔记吗？删除后不可恢复。', '删除提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await noteApi.remove(noteId.value)
        ElMessage.success('删除成功')
        router.replace('/profile')
      })
      .catch(() => {})
  }
}

function openLightbox() {
  window.open(currentImage.value, '_blank')
}

function scrollToComments() {
  const el = document.querySelector('.comments-section')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}
function focusComment() {
  const input = document.querySelector('.action-bar .el-input__inner')
  if (input) input.focus()
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
