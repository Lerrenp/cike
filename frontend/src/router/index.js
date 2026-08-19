import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '此刻 · 首页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '此刻 · 登录', public: true }
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('@/views/Publish.vue'),
    meta: { title: '此刻 · 发布', requiresAuth: true }
  },
  {
    path: '/note/:id',
    name: 'detail',
    component: () => import('@/views/Detail.vue'),
    meta: { title: '此刻 · 笔记详情' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '此刻 · 个人中心', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 全局前置守卫：未登录访问受限页面跳转登录
router.beforeEach((to) => {
  document.title = to.meta.title || '此刻'
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  // 已登录访问登录页则回首页
  if (to.path === '/login' && userStore.isLogin) {
    return { path: '/' }
  }
  return true
})

export default router
