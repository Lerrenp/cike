<template>
  <div class="publish">
    <v-app-bar elevation="1" color="surface" class="pub-top">
      <template #prepend>
        <v-btn icon variant="text" @click="onBack">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
      </template>
      <v-app-bar-title class="pub-title">发布</v-app-bar-title>
      <template #append>
        <v-btn
          color="primary"
          rounded="pill"
          class="pub-btn"
          :loading="publishing"
          :disabled="!canPublish"
          @click="publish"
        >
          发布
        </v-btn>
      </template>
    </v-app-bar>

    <div class="pub-main">
      <!-- 图片上传预览 -->
      <section class="img-section">
        <div class="img-grid">
          <div
            v-for="(img, i) in images"
            :key="img.uid || i"
            class="img-item"
            @click="download(img.url)"
          >
            <img :src="img.url" :alt="`图片${i + 1}`" />
            <span class="delete" @click.stop="removeImage(i)">
              <v-icon size="16">mdi-close</v-icon>
            </span>
            <span v-if="i === 0" class="cover-tag">封面</span>
          </div>
          <label v-if="images.length < 9" class="img-add">
            <input type="file" accept="image/*" multiple hidden @change="onFileChange" />
            <v-icon size="28" color="on-surface-variant">mdi-plus</v-icon>
            <span>{{ images.length }}/9</span>
          </label>
        </div>
        <p class="tip">最多上传 9 张图片，第一张为封面</p>
      </section>

      <!-- 编辑区 -->
      <section class="edit-section">
        <v-text-field
          v-model="form.title"
          class="title-input"
          placeholder="填写标题（必填）"
          maxlength="100"
          counter
          variant="plain"
          single-line
          hide-details="auto"
          @update:model-value="saveDraft"
        />

        <v-textarea
          v-model="form.content"
          rows="6"
          auto-grow
          placeholder="记录此刻的美好瞬间…"
          variant="solo-filled"
          class="content-input"
          hide-details
          @update:model-value="saveDraft"
        />

        <!-- 话题选择 -->
        <div class="topic-block">
          <div class="block-label">添加话题</div>
          <div class="topic-list">
            <v-chip
              v-for="t in topics"
              :key="t.id"
              class="topic"
              :color="selectedTopics.includes(t.topicName) ? 'primary' : 'surface-variant'"
              variant="tonal"
              @click="toggleTopic(t.topicName)"
            >
              <v-icon v-if="selectedTopics.includes(t.topicName)" start size="16">mdi-check</v-icon>
              {{ t.topicName }}
            </v-chip>
          </div>
          <div v-if="selectedTopics.length" class="selected-topics">
            <v-chip
              v-for="name in selectedTopics"
              :key="name"
              closable
              color="primary"
              variant="tonal"
              @click:close="removeTopic(name)"
            >
              {{ name }}
            </v-chip>
          </div>
        </div>

        <!-- 可见性 -->
        <div class="visible-block">
          <div class="block-label">可见范围</div>
          <v-radio-group v-model="form.visible" inline>
            <v-radio :value="1" label="公开"></v-radio>
            <v-radio :value="2" label="仅自己可见"></v-radio>
          </v-radio-group>
        </div>
      </section>
    </div>

    <!-- 返回确认对话框 -->
    <v-dialog v-model="showBackDialog" max-width="360">
      <v-card class="back-dialog">
        <v-card-title class="text-body-1 font-weight-bold">返回提示</v-card-title>
        <v-card-text class="text-body-2">是否保存当前编辑内容为草稿？</v-card-text>
        <v-card-actions class="justify-end px-4 pb-4">
          <v-btn variant="text" color="on-surface-variant" @click="clearDraftAndBack">放弃</v-btn>
          <v-btn color="primary" variant="tonal" @click="saveDraftAndBack">保存草稿</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from '@/utils/toast'
import { noteApi } from '@/api/note'
import { topicApi } from '@/api/topic'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const images = ref([]) // { uid, url, file }
const publishing = ref(false)
const topics = ref([])
const selectedTopics = ref([])
const showBackDialog = ref(false)

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
    toast.warning(`最多上传 9 张图片，本次仅添加 ${remaining} 张`)
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
    if (!images.value.length) toast.warning('请至少上传一张图片')
    else if (!form.title.trim()) toast.warning('请填写标题')
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
    toast.success('发布成功')
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
  showBackDialog.value = true
}
function saveDraftAndBack() {
  saveDraft()
  toast.success('草稿已保存')
  showBackDialog.value = false
  router.push('/')
}
function clearDraftAndBack() {
  clearDraft()
  showBackDialog.value = false
  router.push('/')
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
  background: rgb(var(--v-theme-surface));
}
.pub-top {
  position: sticky;
  top: 0;
  z-index: 20;
}
.pub-title {
  font-weight: 600;
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
  border-radius: 12px;
  overflow: hidden;
}
.img-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  display: block;
}
.img-item .delete {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
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
  background: rgb(var(--v-theme-primary));
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
  background: rgb(var(--v-theme-surface-variant));
  border: 1px dashed rgb(var(--v-theme-outline));
  color: rgb(var(--v-theme-on-surface-variant));
  cursor: pointer;
  font-size: 12px;
}
.tip {
  margin-top: 10px;
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.edit-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.title-input :deep(.v-field) {
  font-size: 20px;
  font-weight: 600;
}
.title-input :deep(.v-field__input) {
  padding-left: 0;
}
.block-label {
  font-size: 14px;
  color: rgb(var(--v-theme-on-surface-variant));
  margin-bottom: 10px;
  font-weight: 500;
}
.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic {
  cursor: pointer;
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
