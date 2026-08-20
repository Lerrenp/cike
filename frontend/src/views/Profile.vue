<template>
  <div class="profile">
    <SideNav v-if="isDesktop" />

    <div class="profile-main" :class="{ 'with-side': isDesktop }">
      <header class="p-top">
        <span class="p-title">{{ isSelf ? '我的' : '个人主页' }}</span>

        <!-- 设置菜单（本人可见） -->
        <v-menu v-if="isSelf" :close-on-content-click="false">
          <template #activator="{ props }">
            <v-btn v-bind="props" variant="text" icon class="more">
              <v-icon icon="mdi-dots-horizontal" />
            </v-btn>
          </template>
          <v-list density="compact" class="settings-menu">
            <v-list-item @click="openEdit">
              <template #prepend><v-icon icon="mdi-pencil" /></template>
              <v-list-item-title>编辑资料</v-list-item-title>
            </v-list-item>
            <v-list-item @click="logout">
              <template #prepend><v-icon icon="mdi-logout" color="error" /></template>
              <v-list-item-title class="text-error">退出登录</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </header>

      <div class="p-body">
        <!-- 个人信息卡 -->
        <section class="profile-card">
          <v-avatar size="72" class="p-avatar">
            <v-img v-if="viewUser.avatar" :src="viewUser.avatar" cover />
            <span v-else>{{ (viewUser.nickname || 'U').charAt(0) }}</span>
          </v-avatar>
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
        <v-tabs v-model="activeTab" class="tabs" @update:model-value="onTabChange">
          <v-tab v-for="t in tabs" :key="t.value" :value="t.value" class="tab">
            {{ t.label }}
          </v-tab>
        </v-tabs>

        <!-- 内容列表 -->
        <section class="notes-list">
          <div v-if="noteList.length" class="notes-grid">
            <NoteCard v-for="n in noteList" :key="n.id" :note="n" />
          </div>
          <div v-else-if="!loadingList" class="empty">
            <v-icon icon="mdi-image-outline" size="48" class="empty-icon" />
            <p>这里内容空空如也~</p>
          </div>
          <div v-if="loadingList" class="list-loading">
            <v-progress-circular indeterminate color="primary" :size="24" width="3" />
          </div>
        </section>
      </div>

      <!-- 底部 tab（非本人查看也显示，方便返回） -->
      <TabBar v-if="!isDesktop" />
    </div>

    <!-- 编辑资料弹窗 -->
    <v-dialog v-model="editDialog" max-width="420" persistent>
      <v-card class="edit-card" rounded="xl">
        <v-card-title class="edit-title">编辑资料</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="editForm.avatar"
            label="头像地址"
            placeholder="https://..."
            prepend-inner-icon="mdi-image"
            outlined
            class="mb-3"
          />
          <v-text-field
            v-model="editForm.nickname"
            label="昵称"
            prepend-inner-icon="mdi-account"
            outlined
            :rules="[nickRule]"
            class="mb-3"
          />
          <v-textarea
            v-model="editForm.bio"
            label="个人简介"
            prepend-inner-icon="mdi-text"
            outlined
            rows="3"
            auto-grow
          />
        </v-card-text>
        <v-card-actions class="edit-actions">
          <v-spacer />
          <v-btn variant="text" @click="editDialog = false">取消</v-btn>
          <v-btn color="primary" :loading="saving" @click="saveProfile">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 退出登录确认 -->
    <v-dialog v-model="logoutDialog" max-width="360">
      <v-card rounded="xl">
        <v-card-text class="logout-text">
          确定要退出登录吗？
        </v-card-text>
        <v-card-actions class="edit-actions">
          <v-spacer />
          <v-btn variant="text" @click="logoutDialog = false">取消</v-btn>
          <v-btn color="error" @click="confirmLogout">退出</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from '@/utils/toast'
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

// ---------- 编辑资料 ----------
const editDialog = ref(false)
const saving = ref(false)
const editForm = reactive({ avatar: '', nickname: '', bio: '' })

function nickRule(v) {
  return v && v.trim() ? true : '昵称不能为空'
}

function openEdit() {
  editForm.avatar = viewUser.avatar || ''
  editForm.nickname = viewUser.nickname || ''
  editForm.bio = viewUser.bio || ''
  editDialog.value = true
}

async function saveProfile() {
  if (!editForm.nickname || !editForm.nickname.trim()) {
    toast.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const data = {
      avatar: editForm.avatar.trim(),
      nickname: editForm.nickname.trim(),
      bio: editForm.bio
    }
    await userApi.update(viewUid.value, data)
    userStore.updateUserInfo(data)
    await loadUserInfo()
    editDialog.value = false
    toast.success('资料修改成功')
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

// ---------- 退出登录 ----------
const logoutDialog = ref(false)

function logout() {
  logoutDialog.value = true
}

async function confirmLogout() {
  await userStore.logout()
  logoutDialog.value = false
  toast.success('已退出登录')
  router.replace('/login')
}

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

function onTabChange(value) {
  activeTab.value = value
  loadList()
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
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px 0 16px;
  background: var(--v-theme-surface);
  border-bottom: 1px solid rgb(var(--v-theme-outline-variant));
}
.p-title {
  font-size: 17px;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
}
.more {
  color: rgb(var(--v-theme-on-surface-variant));
}
.p-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px calc(72px + env(safe-area-inset-bottom));
}
.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgb(var(--v-theme-surface-light));
  border-radius: 20px;
  padding: 20px;
}
.p-avatar {
  flex-shrink: 0;
  background: rgb(var(--v-theme-primary-container));
  color: rgb(var(--v-theme-on-primary-container));
  font-size: 28px;
  font-weight: 600;
}
.p-nickname {
  font-size: 20px;
  font-weight: 700;
  color: rgb(var(--v-theme-on-surface));
  margin-bottom: 6px;
}
.p-bio {
  font-size: 13px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.stats {
  display: flex;
  background: var(--v-theme-surface);
  border-radius: 16px;
  border: 1px solid rgb(var(--v-theme-outline-variant));
  margin-top: 16px;
  padding: 16px 0;
}
.stat {
  flex: 1;
  text-align: center;
  border-right: 1px solid rgb(var(--v-theme-outline-variant));
}
.stat:last-child {
  border-right: none;
}
.stat .num {
  font-size: 20px;
  font-weight: 700;
  color: rgb(var(--v-theme-on-surface));
}
.stat .label {
  margin-top: 4px;
  font-size: 12px;
  color: rgb(var(--v-theme-on-surface-variant));
}
.tabs {
  margin-top: 20px;
  background: var(--v-theme-surface) !important;
  border: 1px solid rgb(var(--v-theme-outline-variant));
  border-radius: 14px;
}
.tabs :deep(.v-tab) {
  font-weight: 600;
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
.empty {
  text-align: center;
  padding: 48px 0;
  color: rgb(var(--v-theme-on-surface-variant));
}
.empty-icon {
  opacity: 0.4;
  margin-bottom: 8px;
}
.empty p {
  font-size: 14px;
}
.settings-menu {
  border-radius: 12px;
}
.edit-card {
  padding-top: 8px;
}
.edit-title {
  font-weight: 700;
}
.edit-actions {
  padding: 8px 16px 16px;
}
.logout-text {
  font-size: 15px;
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
