<template>
  <div class="note-card" @click="goDetail">
    <div class="cover-wrap">
      <img
        :src="note.coverUrl || fallback"
        :alt="note.title"
        loading="lazy"
        @error="onError"
      />
      <!-- 点赞角标 -->
      <span class="like-badge" v-if="note.likeCount">
        <el-icon><Pointer /></el-icon>
        {{ note.likeCount }}
      </span>
    </div>
    <div class="info">
      <p class="title ellipsis-2">{{ note.title }}</p>
      <p class="desc ellipsis" v-if="note.content">{{ note.content }}</p>
      <div class="author">
        <el-avatar :size="20" :src="note.author?.avatar" class="avatar">
          {{ (note.author?.nickname || 'U').charAt(0) }}
        </el-avatar>
        <span class="name ellipsis">{{ note.author?.nickname || '未知用户' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  note: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const fallback =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="300" height="360"><rect width="100%" height="100%" fill="#f2e8e6"/><text x="50%" y="50%" dy="8" fill="#c9a99f" font-size="16" text-anchor="middle" font-family="sans-serif">暂无封面</text></svg>`
  )

function onError(e) {
  e.target.src = fallback
}

function goDetail() {
  router.push(`/note/${props.note.id}`)
}
</script>

<style scoped>
.note-card {
  background: var(--cike-card);
  border-radius: var(--cike-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--cike-shadow);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  margin-bottom: 12px;
  break-inside: avoid;
}
.note-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--cike-shadow-hover);
}
.cover-wrap {
  position: relative;
  width: 100%;
}
.cover-wrap img {
  width: 100%;
  height: auto;
  aspect-ratio: 3 / 4;
  object-fit: cover;
  background: #f2e8e6;
}
.like-badge {
  position: absolute;
  right: 8px;
  bottom: 8px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #fff;
  font-size: 12px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 20px;
  padding: 2px 8px;
  backdrop-filter: blur(2px);
}
.info {
  padding: 10px 12px 12px;
}
.title {
  font-size: 14px;
  line-height: 1.4;
  font-weight: 500;
  color: var(--cike-text);
  margin-bottom: 4px;
}
.desc {
  font-size: 12px;
  color: var(--cike-text-3);
  margin-bottom: 8px;
}
.author {
  display: flex;
  align-items: center;
  gap: 6px;
}
.avatar {
  flex-shrink: 0;
  background: var(--cike-primary-soft);
  color: var(--cike-primary);
  font-size: 12px;
}
.name {
  font-size: 12px;
  color: var(--cike-text-2);
}
</style>
