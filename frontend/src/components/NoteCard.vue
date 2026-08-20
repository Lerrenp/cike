<template>
  <v-card class="note-card" rounded="xl" @click="goDetail">
    <div class="cover-wrap">
      <img :src="imgSrc" :alt="note.title" loading="lazy" @error="onError" />
      <span v-if="likeCount" class="like-badge">
        <v-icon icon="mdi-heart" size="12" color="white" />
        {{ likeCount }}
      </span>
    </div>
    <div class="info">
      <p class="title ellipsis-2">{{ note.title }}</p>
      <div class="meta">
        <span class="stat">
          <v-icon icon="mdi-heart-outline" size="14" color="on-surface" />
          {{ likeCount }}
        </span>
        <span class="stat">
          <v-icon icon="mdi-bookmark-outline" size="14" color="on-surface" />
          {{ collectCount }}
        </span>
      </div>
      <div class="author">
        <v-avatar :image="note.author?.avatar" size="20" class="avatar">
          {{ (note.author?.nickname || 'U').charAt(0) }}
        </v-avatar>
        <span class="name ellipsis">{{ note.author?.nickname || '未知用户' }}</span>
      </div>
    </div>
  </v-card>
</template>

<script setup>
import { ref, computed } from 'vue'
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

const imgSrc = computed(() => props.note.coverUrl || props.note.cover_url || fallback)
const likeCount = computed(() => Number(props.note.likeCount) || 0)
const collectCount = computed(() => Number(props.note.collectCount) || 0)

function onError(e) {
  e.target.src = fallback
}

function goDetail() {
  router.push(`/note/${props.note.id}`)
}
</script>

<style scoped>
.note-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.note-card:hover {
  transform: translateY(-3px);
}
.cover-wrap {
  position: relative;
  width: 100%;
}
.cover-wrap img {
  display: block;
  width: 100%;
  height: 150px;
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
  margin-bottom: 6px;
  color: rgb(var(--v-theme-on-surface));
}
.meta {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 8px;
}
.stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.author {
  display: flex;
  align-items: center;
  gap: 6px;
}
.avatar {
  flex-shrink: 0;
  background: rgb(var(--v-theme-primary-container));
  color: rgb(var(--v-theme-on-primary-container));
  font-size: 12px;
}
.name {
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
}
@media (min-width: 1201px) {
  .cover-wrap img {
    height: 170px;
  }
}
</style>
