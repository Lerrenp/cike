<template>
  <div class="profile">
    <SideNav v-if="isDesktop" />

    <div class="profile-main" :class="{ 'with-side': isDesktop }">
      <header class="p-top">
        <span class="p-title">{{ isSelf ? '我的' : '个人主页' }}</span>
        <el-dropdown v-if="isSelf" trigger="click" @command="onAccount">
          <span class="more"><el-icon><Setting /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit"><el-icon><Edit /></el-icon>编辑资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <div class="p-body">
        <!-- 个人信息卡 -->
        <section class="profile-card">
          <el-avatar :size="72" :src="viewUser.avatar" class="p-avatar">
            {{ (viewUser.nickname || 'U').charAt(0) }}
          </el-avatar>
          <div class="p-info">
            <div class="p-nickname">{{ viewUser.nickname || '未设置昵称' }}</div>
            <div class="p-bio">{{ viewUser.bio || '这个人很懒，什么都没有写~' }}</div>
          </div>
        </section>

        <!-- 数据统计 -->
        <section class="stats">
          <div class="stat">
            <div class="num">{{ viewUser.note_count || 0 }}</div>
            <div class="label">作品</div>
          </div>
          <div class="stat">
            <div class="num">{{ viewUser.like_total || 0 }}</div>
            <div class="label">获赞</div>
          </div>
          <div class="stat">
            <div class="num">{{ collectTotal }}</div>
            <div class="label">收藏</div>
          </div>
        </section>

        <!-- tab 切换 -->
        <nav class="tabs">
          <div
            v-for="t in tabs"
            :key="t.value"
            class="tab"
            :class="{ active: activeTab === t.value }"
            @click="switchTab(t.value)"
          >
            {{ t.label }}
          </div>
        </nav>

        <!-- 内容列表 -->
        <section class="notes-list">
          <div v-if="noteList.length" class="notes-grid">
            <NoteCard v-for="n in noteList" :key="n.id" :note="n" />
          </div>
          <el-empty v-else-if="!loadingList" description="这里内容空空如也~" />
          <div v-if="loadingList" class="list-loading"><span class="spinner" /></div>
        </section>
      </div>

      <!-- 底部 tab（非本人查看也显示，方便返回） -->
      <TabBar v-if="!isDesktop" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NoteCard from '@/components/NoteCard.vue'
import TabBar from '@/components/TabBar.vue'
import SideNav from '@/components/SideNav.vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { useViewport } from '@/composables/useViewport'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isDesktop } = useViewport()

const tabs = [
  { label: '作品', value: 'works' },
  { label: '收藏', value: 'collects' },
  { label: '点赞', value: 'likes' }
]

const activeTab = ref('works')
const noteList = ref([])
const loadingList = ref(false)
const collectTotal = ref(0)

const viewUser = reactive({
  id: null,
  nickname: '',
  avatar: '',
  bio: '',
  note_count: 0,
  like_total: 0
})

// 判断是否为本人主页（无 uid 参数或 uid 等于自己）
const isSelf = computed(
  () => !route.query.uid || String(route.query.uid) === String(userStore.userId)
)
const viewUid = computed(() =>
  isSelf.value ? userStore.userId : Number(route.query.uid)
)

async function loadUserInfo() {
  if (!viewUid.value) return
  try {
    const res = await userApi.detail(viewUid.value)
    Object.assign(viewUser, res.data || {})
    // 本人查看时同步到 store
    if (isSelf.value) {
      userStore.updateUserInfo({
        nickname: viewUser.nickname,
        avatar: viewUser.avatar,
        bio: viewUser.bio
      })
    }
  } catch (e) {}
}

async function loadList() {
  if (!viewUid.value) return
  loadingList.value = true
  try {
    let data = []
    if (activeTab.value === 'works') {
      const res = await userApi.notes(viewUid.value)
      data = res?.data || []
    } else if (activeTab.value === 'collects') {
      const res = await userApi.collects(viewUid.value)
      data = res?.data || []
      collectTotal.value = data.length
    } else {
      const res = await userApi.likes(viewUid.value)
      data = res?.data || []
    }
    noteList.value = data
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    loadingList.value = false
  }
}

function switchTab(value) {
  activeTab.value = value
  loadList()
}

// ---------- 账号操作 ----------
async function onAccount(command) {
  if (command === 'edit') {
    editProfile()
  } else if (command === 'logout') {
    logout()
  }
}

function editProfile() {
  ElMessageBox.prompt('请输入新的昵称', '修改昵称', {
    inputValue: viewUser.nickname,
    inputValidator: (v) => (v && v.trim() ? true : '昵称不能为空')
  })
    .then(async ({ value }) => {
      try {
        await userApi.update(viewUid.value, { nickname: value.trim() })
        await loadUserInfo()
        ElMessage.success('昵称修改成功')
      } catch (e) {}
    })
    .catch(() => {})
}

function logout() {
  ElMessageBox.confirm('确定要退出登录吗？', '退出提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.replace('/login')
    })
    .catch(() => {})
}

// 未登录访问个人中心跳到登录
onMounted(() => {
  if (!userStore.isLogin) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  loadUserInfo()
  loadList()
})

watch(() => route.query.uid, () => {
  if (userStore.isLogin) {
    loadUserInfo()
    loadList()
  }
})
</script>

<style scoped>
.profile {
  min-height: 100vh;
}
.profile-main.with-side {
  margin-left: 220px;
}
.p-top {
  position: sticky;
  top: 0;
  z-index: 50;
  height: var(--header-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--cike-border);
}
.p-title {
  font-size: 17px;
  font-weight: 600;
}
.more {
  display: flex;
  align-items: center;
  font-size: 20px;
  cursor: pointer;
  color: var(--cike-text-2);
  padding: 8px;
}
.p-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px calc(60px + env(safe-area-inset-bottom));
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #fff5f1, #ffe9e6);
  border-radius: 16px;
  padding: 20px;
}
.p-avatar {
  flex-shrink: 0;
  background: var(--cike-primary-soft);
  color: var(--cike-primary);
  font-size: 28px;
}
.p-nickname {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 6px;
}
.p-bio {
  font-size: 13px;
  color: var(--cike-text-2);
}
.stats {
  display: flex;
  background: #fff;
  border-radius: 14px;
  box-shadow: var(--cike-shadow);
  margin-top: 16px;
  padding: 16px 0;
}
.stat {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--cike-border);
}
.stat:last-child {
  border-right: none;
}
.stat .num {
  font-size: 20px;
  font-weight: 700;
  color: var(--cike-text);
}
.stat .label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--cike-text-3);
}
.tabs {
  display: flex;
  margin-top: 20px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--cike-border);
}
.tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 14px;
  color: var(--cike-text-2);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.15s ease;
}
.tab:hover {
  color: var(--cike-primary);
}
.tab.active {
  color: var(--cike-primary);
  font-weight: 600;
  border-bottom-color: var(--cike-primary);
}
.notes-list {
  margin-top: 20px;
}
.notes-grid {
  columns: 2;
  column-gap: 12px;
}
@media (min-width: 769px) {
  .notes-grid {
    columns: 3;
  }
}
.list-loading {
  text-align: center;
  padding: 20px 0;
}
.spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 2px solid #eee;
  border-top-color: var(--cike-primary);
  border-radius: 50%;
  animation: rotate 0.8s linear infinite;
}
@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}

/* 电脑端：左信息右内容 */
@media (min-width: 1201px) {
  .p-body {
    max-width: 1100px;
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 24px;
  }
  .profile-card,
  .stats {
    grid-column: 1;
  }
  .profile-card {
    flex-direction: column;
    align-items: flex-start;
    margin-top: 0;
  }
  .stats {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-top: 16px;
  }
  .stat {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    border-right: none;
    padding: 8px 24px;
  }
  .tabs,
  .notes-list {
    grid-column: 2;
    grid-row: 1 / span 2;
    margin-top: 16px;
    height: fit-content;
  }
  .tabs {
    grid-row: 1;
  }
  .notes-list {
    grid-row: 2;
  }
}
</style>
