<template>
  <div class="comments-list" v-if="comments.length">
    <div v-for="c in comments" :key="c.id || c.commentId" class="comment-item" @click="onClickComment(c)">
      <v-avatar size="32" class="c-avatar" :image="c.user?.avatar">
        {{ (c.user?.nickname || 'U').charAt(0) }}
      </v-avatar>
      <div class="c-body">
        <div class="c-head">
          <span class="c-nick">{{ c.user?.nickname || '匿名用户' }}</span>
          <v-btn v-if="isMine(c)" icon size="x-small" variant="text" class="c-delete" @click.stop="removeComment(c)">
            <v-icon size="16">mdi-delete</v-icon>
          </v-btn>
        </div>
        <p class="c-text" v-if="!c.replyUser">{{ c.content }}</p>
        <p class="c-text" v-else>
          回复 <span class="reply-to">@{{ c.replyUser?.nickname }}</span>：{{ c.content }}
        </p>
        <div class="c-meta">
          <span class="c-time">{{ formatTime(c.createTime) }}</span>
          <span class="c-reply" @click.stop="onReply(c)">回复</span>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="no-comments">还没有评论，快来抢沙发~</div>
</template>

<script setup>
import { computed } from 'vue'
import { toast } from '@/utils/toast'
import { interactApi } from '@/api/interact'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  comments: {
    type: Array,
    default: () => []
  }
})
const emit = defineEmits(['reply', 'refresh'])

const userStore = useUserStore()

const isMine = computed(() => (c) => userStore.userId && userStore.userId === c.userId)

function formatTime(t) {
  if (!t) return ''
  const date = new Date(t)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function removeComment(c) {
  if (!confirm('确定删除这条评论吗？')) return
  try {
    await interactApi.deleteComment(c.id != null ? c.id : c.commentId)
    toast.success('删除成功')
    emit('refresh')
  } catch (e) {}
}

function onClickComment(c) {
  onReply(c)
}
function onReply(c) {
  emit('reply', c)
}
</script>

<style scoped>
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.comment-item {
  display: flex;
  gap: 10px;
}
.c-avatar {
  flex-shrink: 0;
  background: rgb(var(--v-theme-primary-container));
  color: rgb(var(--v-theme-on-primary-container));
  font-size: 14px;
}
.c-body {
  flex: 1;
  min-width: 0;
}
.c-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.c-nick {
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
  font-weight: 500;
}
.c-delete {
  color: rgb(var(--v-theme-on-surface-variant));
}
.c-delete:hover {
  color: rgb(var(--v-theme-error));
}
.c-text {
  font-size: 14px;
  line-height: 1.5;
  color: rgb(var(--v-theme-on-surface));
  margin-top: 2px;
  word-break: break-word;
}
.reply-to {
  color: rgb(var(--v-theme-primary));
}
.c-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.c-time {
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.c-reply {
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
  cursor: pointer;
}
.c-reply:hover {
  color: rgb(var(--v-theme-primary));
}
.no-comments {
  color: rgb(var(--v-theme-on-surface-variant));
  font-size: 13px;
  padding: 12px 0;
  text-align: center;
}
</style>
