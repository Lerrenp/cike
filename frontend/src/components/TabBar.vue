<template>
  <nav class="tab-bar">
    <v-btn
      v-for="item in items"
      :key="item.path"
      variant="text"
      stacked
      class="tab-item"
      :class="{ active: isActive(item) }"
      @click="go(item)"
    >
      <v-icon :icon="item.icon" size="24" />
      <span class="label">{{ item.label }}</span>
    </v-btn>

    <v-btn
      variant="text"
      stacked
      class="tab-item publish"
      :class="{ active: isActive({ path: '/publish' }) }"
      @click="go({ path: '/publish' })"
    >
      <v-icon icon="mdi-plus" size="26" />
      <span class="label">发布</span>
    </v-btn>
  </nav>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const items = [
  { path: '/', label: '首页', icon: 'mdi-home' },
  { path: '/profile', label: '我的', icon: 'mdi-account' }
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
  height: calc(60px + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  display: flex;
  align-items: stretch;
  background: var(--v-theme-surface);
  border-top: 1px solid rgb(var(--v-theme-outline-variant));
  z-index: 100;
}
.tab-item {
  flex: 1;
  height: 100%;
  border-radius: 0;
  color: rgb(var(--v-theme-on-surface-variant));
}
.tab-item :deep(.v-icon) {
  color: inherit;
}
.tab-item .label {
  font-size: 12px;
}
.tab-item.active {
  color: rgb(var(--v-theme-primary));
  font-weight: 600;
}
.tab-item.publish.active {
  color: rgb(var(--v-theme-primary));
}
</style>
