<template>
  <aside class="side-nav">
    <div class="brand">此刻</div>
    <div
      v-for="item in items"
      :key="item.path"
      class="nav-item"
      :class="{ active: isActive(item) }"
      @click="go(item)"
    >
      <el-icon :size="20"><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </div>
    <div class="spacer" />
    <div class="nav-item publish" :class="{ active: isActive({ path: '/publish' }) }" @click="go({ path: '/publish' })">
      <el-icon :size="20"><Plus /></el-icon>
      <span>发布</span>
    </div>
    <div v-if="userStore.isLogin" class="nav-item" :class="{ active: isActive({ path: '/profile' }) }" @click="go({ path: '/profile' })">
      <el-icon :size="20"><User /></el-icon>
      <span>我的</span>
    </div>
    <div v-else class="nav-item" @click="go({ path: '/login' })">
      <el-icon :size="20"><User /></el-icon>
      <span>登录</span>
    </div>
  </aside>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const items = [
  { path: '/', label: '首页', icon: 'HomeFilled' }
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
  width: 220px;
  height: 100vh;
  background: #fff;
  border-right: 1px solid var(--cike-border);
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  z-index: 100;
}
.brand {
  font-size: 22px;
  font-weight: 800;
  color: var(--cike-primary);
  padding: 4px 12px 20px;
  letter-spacing: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  color: var(--cike-text-2);
  font-size: 15px;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.nav-item:hover {
  background: var(--cike-primary-soft);
  color: var(--cike-primary);
}
.nav-item.active {
  background: var(--cike-primary-soft);
  color: var(--cike-primary);
  font-weight: 600;
}
.spacer {
  flex: 1;
}
</style>
