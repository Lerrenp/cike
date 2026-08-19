<template>
  <div class="publish">
    <header class="pub-top">
      <span class="back" @click="onBack"><el-icon><ArrowLeft /></el-icon></span>
      <span class="title">发布笔记</span>
      <el-button
        type="primary"
        class="pub-btn"
        :loading="publishing"
        :disabled="!canPublish"
        @click="publish"
      >
        发布
      </el-button>
    </header>

    <div class="pub-main">
      <!-- 左侧/上方：图片上传预览 -->
      <section class="img-section">
        <div class="img-grid">
          <div v-for="(img, i) in images" :key="img.uid || i" class="img-item" :class="{ first: i === 0 }">
            <img :src="img.url" :alt="`图片${i + 1}`" @click="download(img.url)" />
            <span class="delete" @click="removeImage(i)" @click.stop>
              <el-icon><Close /></el-icon>
            </span>
            <span class="cover-tag" v-if="i === 0">封面</span>
          </div>
          <label v-if="images.length < 9" class="img-add">
            <input type="file" accept="image/*" multiple hidden @change="onFileChange" />
            <el-icon :size="28"><Plus /></el-icon>
            <span>{{ images.length }}/9</span>
          </label>
        </div>
        <p class="tip">最多上传 9 张图片，第一张为封面</p>
      </section>

      <!-- 右侧/下方：编辑区 -->
      <section class="edit-section">
        <el-input
          v-model="form.title"
          class="title-input"
          placeholder="填写标题（必填）"
          maxlength="100"
          show-word-limit
          @input="saveDraft"
        />
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          resize="none"
          placeholder="记录此刻的美好瞬间…"
          @input="saveDraft"
        />

        <!-- 话题选择 -->
        <div class="topic-block">
          <div class="block-label">添加话题</div>
          <div class="topic-list">
            <span
              v-for="t in topics"
              :key="t.id"
              class="topic"
              :class="{ active: selectedTopics.includes(t.topicName) }"
              @click="toggleTopic(t.topicName)"
            >
              {{ t.topicName }}
            </span>
          </div>
          <div class="selected-topics" v-if="selectedTopics.length">
            <el-tag
              v-for="name in selectedTopics"
              :key="name"
              closable
              @close="removeTopic(name)"
              type="danger"
              effect="plain"
            >
              {{ name }}
            </el-tag>
          </div>
        </div>

        <!-- 可见性 -->
        <div class="visible-block">
          <div class="block-label">可见范围</div>
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">公开</el-radio>
            <el-radio :value="2">仅自己可见</el-radio>
          </el-radio-group>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi } from '@/api/note'
import { topicApi } from '@/api/topic'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const images = ref([]) // { uid, url, file }
const publishing = ref(false)
const topics = ref([])
const selectedTopics = ref([])

const DRAFT_KEY = 'cike_draft'

const form = reactive({
  title: '',
  content: '',
  visible: 1
})

const canPublish = computed(
  () => images.value.length > 0 && !!form.title.trim() && !publishing.value
)

// 图片压缩转 base64
function compressFile(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const MAX = 1200
        let { width, height } = img
        if (width > MAX || height > MAX) {
          const ratio = Math.min(MAX / width, MAX / height)
          width = Math.round(width * ratio)
          height = Math.round(height * ratio)
        }
        canvas.width = width
        canvas.height = height
        canvas.getContext('2d').drawImage(img, 0, 0, width, height)
        resolve(canvas.toDataURL('image/jpeg', 0.8))
      }
      img.onerror = () => resolve(e.target.result)
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}

async function onFileChange(e) {
  const files = Array.from(e.target.files || [])
  const remaining = 9 - images.value.length
  if (files.length > remaining) {
    ElMessage.warning(`最多上传 9 张图片，本次仅添加 ${remaining} 张`)
  }
  const picked = files.slice(0, remaining)
  for (const file of picked) {
    if (!file.type.startsWith('image/')) continue
    const url = await compressFile(file)
    images.value.push({ uid: Date.now() + Math.random(), url, file })
  }
  saveDraft()
  e.target.value = ''
}

function removeImage(i) {
  images.value.splice(i, 1)
  saveDraft()
}

function download(url) {
  // 预览大图
  window.open(url, '_blank')
}

function toggleTopic(name) {
  if (selectedTopics.value.includes(name)) {
    removeTopic(name)
  } else {
    selectedTopics.value.push(name)
    saveDraft()
  }
}
function removeTopic(name) {
  selectedTopics.value = selectedTopics.value.filter((t) => t !== name)
  saveDraft()
}

// ---------- 草稿 ----------
function saveDraft() {
  const draft = {
    title: form.title,
    content: form.content,
    visible: form.visible,
    images: images.value.map((i) => i.url),
    topics: selectedTopics.value
  }
  localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
}

function loadDraft() {
  try {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    const d = JSON.parse(raw)
    form.title = d.title || ''
    form.content = d.content || ''
    form.visible = d.visible || 1
    selectedTopics.value = d.topics || []
    images.value = (d.images || []).map((url) => ({ uid: Date.now() + Math.random(), url }))
  } catch (e) {
    /* 忽略损坏草稿 */
  }
}

function clearDraft() {
  localStorage.removeItem(DRAFT_KEY)
}

async function loadTopics() {
  try {
    const res = await topicApi.list()
    topics.value = res?.data || []
  } catch (e) {
    /* 话题加载失败不强求 */
  }
}

async function publish() {
  if (!canPublish.value) {
    if (!images.value.length) ElMessage.warning('请至少上传一张图片')
    else if (!form.title.trim()) ElMessage.warning('请填写标题')
    return
  }
  publishing.value = true
  try {
    await noteApi.publish({
      title: form.title.trim(),
      content: form.content.trim(),
      images: images.value.map((i) => i.url),
      topics: selectedTopics.value,
      visible: form.visible
    })
    ElMessage.success('发布成功')
    clearDraft()
    router.push('/profile')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    publishing.value = false
  }
}

function onBack() {
  // 有未发布内容时提示保存草稿
  const hasContent =
    images.value.length || form.title.trim() || form.content.trim()
  if (!hasContent) {
    router.push('/')
    return
  }
  ElMessageBox.confirm('是否保存当前编辑内容为草稿？', '返回提示', {
    confirmButtonText: '保存草稿',
    cancelButtonText: '放弃',
    distinguishCancelAndClose: true
  })
    .then(() => {
      saveDraft()
      ElMessage.success('草稿已保存')
      router.push('/')
    })
    .catch((action) => {
      if (action === 'cancel') {
        clearDraft()
        router.push('/')
      }
    })
}

onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: '/publish' } })
    return
  }
  loadDraft()
  loadTopics()
})
</script>

<style scoped>
.publish {
  min-height: 100vh;
  background: #fff;
}
.pub-top {
  position: sticky;
  top: 0;
  z-index: 20;
  height: var(--header-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--cike-border);
}
.back {
  display: flex;
  align-items: center;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
}
.title {
  font-weight: 600;
  font-size: 16px;
}
.pub-btn {
  border-radius: 18px;
  padding: 8px 22px;
}
.pub-btn.is-disabled {
  opacity: 0.5;
}
.pub-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px 16px;
  max-width: 720px;
  margin: 0 auto;
}
.img-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.img-item,
.img-add {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
}
.img-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}
.img-item .delete {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.img-item .cover-tag {
  position: absolute;
  bottom: 6px;
  left: 6px;
  background: var(--cike-primary);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}
.img-add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: #f7f7f7;
  border: 1px dashed #d5d5d5;
  color: var(--cike-text-3);
  cursor: pointer;
  font-size: 12px;
}
.tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--cike-text-3);
}
.edit-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  padding-left: 0;
}
.title-input :deep(.el-input__inner) {
  font-size: 20px;
  font-weight: 600;
}
.edit-section :deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 1.6;
  box-shadow: none;
  background: #fafafa;
  border-radius: 10px;
}
.block-label {
  font-size: 14px;
  color: var(--cike-text-2);
  margin-bottom: 10px;
  font-weight: 500;
}
.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic {
  padding: 6px 14px;
  border-radius: 16px;
  background: #f5f5f5;
  color: var(--cike-text-2);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.topic:hover {
  color: var(--cike-primary);
}
.topic.active {
  background: var(--cike-primary-soft);
  color: var(--cike-primary);
  border: 1px solid var(--cike-primary);
}
.selected-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

/* 电脑端左右分栏 */
@media (min-width: 1201px) {
  .pub-main {
    flex-direction: row;
    max-width: 1100px;
    gap: 40px;
  }
  .img-section {
    flex: 1;
  }
  .edit-section {
    flex: 1;
  }
  .img-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
