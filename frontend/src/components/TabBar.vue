<template>
  <nav class="tab-bar">
    <div
      v-for="item in items"
      :key="item.path"
      class="tab-item"
      :class="{ active: isActive(item) }"
      @click="go(item)"
    >
      <el-icon :size="22"><component :is="item.icon" /></el-icon>
      <span class="label">{{ item.label }}</span>
    </div>
    <!-- 中间发布按钮 -->
    <div class="tab-item publish" :class="{ active: isActive({ path: '/publish' }) }" @click="go({ path: '/publish' })">
      <el-icon :size="24"><Plus /></el-icon>
      <span class="label">发布</span>
    </div>
  </nav>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const items = [
  { path: '/', label: '首页', icon: 'HomeFilled' },
  { path: '/profile', label: '我的', icon: 'UserFilled' }
]

function isActive(item) {
  if (item.path === '/') return route.path === '/'
  return route.path.startsWith(item.path)
}

function go(item) {
  router.push(item.path)
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc(56px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  align-items: stretch;
  background: #fff;
  border-top: 1px solid var(--cike-border);
  z-index: 100;
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--cike-text-3);
  cursor: pointer;
  transition: color 0.15s ease;
}
.tab-item .label {
  font-size: 11px;
}
.tab-item.active {
  color: var(--cike-primary);
}
.tab-item.publish.active {
  color: var(--cike-primary);
}
</style>
