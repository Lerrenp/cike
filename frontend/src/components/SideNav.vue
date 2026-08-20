<template>
  <v-navigation-drawer permanent floating width="220" class="side-nav" :border="false">
    <div class="brand">此刻</div>
    <v-list density="comfortable" nav class="menu-list">
      <v-list-item
        v-for="item in items"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item) }"
        :active="isActive(item)"
        @click="go(item)"
      >
        <template #prepend><v-icon :icon="item.icon" /></template>
        <v-list-item-title>{{ item.label }}</v-list-item-title>
      </v-list-item>

      <v-list-item
        class="nav-item publish"
        :class="{ active: isActive({ path: '/publish' }) }"
        :active="isActive({ path: '/publish' })"
        @click="go({ path: '/publish' })"
      >
        <template #prepend><v-icon icon="mdi-plus" /></template>
        <v-list-item-title>发布</v-list-item-title>
      </v-list-item>

      <v-list-item
        v-if="userStore.isLogin"
        class="nav-item"
        :class="{ active: isActive({ path: '/profile' }) }"
        :active="isActive({ path: '/profile' })"
        @click="go({ path: '/profile' })"
      >
        <template #prepend><v-icon icon="mdi-account" /></template>
        <v-list-item-title>我的</v-list-item-title>
      </v-list-item>

      <v-list-item v-else class="nav-item" @click="go({ path: '/login' })">
        <template #prepend><v-icon icon="mdi-account" /></template>
        <v-list-item-title>登录</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const items = [
  { path: '/', label: '首页', icon: 'mdi-home' }
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
.side-nav {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 100;
  background: var(--v-theme-surface) !important;
}
.brand {
  font-size: 22px;
  font-weight: 800;
  color: rgb(var(--v-theme-primary));
  padding: 20px 16px 8px;
  letter-spacing: 2px;
}
.menu-list {
  padding: 4px 8px;
}
.nav-item {
  border-radius: 12px;
  margin-bottom: 4px;
}
.nav-item :deep(.v-list-item__overlay) {
  display: none;
}
.nav-item.active {
  background: rgb(var(--v-theme-primary-container));
  color: rgb(var(--v-theme-on-primary-container));
  font-weight: 600;
}
.nav-item :deep(.v-icon) {
  color: inherit;
}
.nav-item :deep(.v-list-item__prepend) {
  margin-inline-end: 12px;
  opacity: 1;
}
</style>
