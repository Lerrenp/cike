<template>
  <div class="login-page">
    <v-card class="login-card" elevation="0" rounded="xl">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="logo">刻</div>
        <h1 class="name">此刻</h1>
        <p class="slogan">记录此刻，分享美好</p>
      </div>

      <!-- 登录 / 注册 切换 -->
      <v-tabs v-model="mode" color="primary" density="comfortable" class="mode-tabs" @update:model-value="onTabChange">
        <v-tab value="login">登录</v-tab>
        <v-tab value="register">注册</v-tab>
      </v-tabs>

      <!-- 手机号 -->
      <v-text-field
        v-model="form.phone"
        label="手机号"
        placeholder="请输入手机号"
        variant="outlined"
        maxlength="11"
        prepend-inner-icon="mdi-cellphone"
        class="mt-4"
        @input="phoneDirty = true"
        @keyup.enter="submit"
      />

      <!-- 验证码（注册） -->
      <div v-if="mode === 'register'" class="code-row">
        <v-text-field
          v-model="form.code"
          label="验证码"
          placeholder="请输入验证码"
          variant="outlined"
          maxlength="6"
        />
        <v-btn
          class="code-btn"
          color="primary"
          variant="tonal"
          :disabled="codeCountdown > 0 || !validPhone"
          @click="sendCode"
        >
          {{ codeCountdown > 0 ? `${codeCountdown}s 后重发` : '获取验证码' }}
        </v-btn>
      </div>

      <!-- 昵称（注册） -->
      <v-text-field
        v-if="mode === 'register'"
        v-model="form.nickname"
        label="昵称"
        placeholder="设置昵称"
        variant="outlined"
        maxlength="20"
        prepend-inner-icon="mdi-account"
      />

      <!-- 密码 -->
      <v-text-field
        v-model="form.password"
        :label="mode === 'register' ? '密码（6-20位）' : '密码'"
        :placeholder="mode === 'register' ? '设置密码（6-20位）' : '请输入密码'"
        variant="outlined"
        type="password"
        prepend-inner-icon="mdi-lock"
        @keyup.enter="submit"
      />

      <!-- 确认密码（注册） -->
      <v-text-field
        v-if="mode === 'register'"
        v-model="form.confirmPassword"
        label="确认密码"
        placeholder="请再次输入密码"
        variant="outlined"
        type="password"
        prepend-inner-icon="mdi-lock-check"
        @keyup.enter="submit"
      />

      <v-btn
        color="primary"
        class="submit-btn"
        size="large"
        :loading="loading"
        @click="submit"
      >
        {{ mode === 'login' ? '登 录' : '注 册' }}
      </v-btn>

      <p v-if="mode === 'login'" class="switch-hint" @click="mode = 'register'">
        还没有账号？<span>立即注册</span>
      </p>
      <p v-else class="switch-hint" @click="mode = 'login'">
        已有账号？<span>去登录</span>
      </p>

      <div class="agreement">
        <span>登录即代表同意</span>
        <a href="javascript:void(0)" @click="agree = true">《用户协议》</a>
        <span>与</span>
        <a href="javascript:void(0)" @click="agree = true">《隐私政策》</a>
      </div>
    </v-card>

    <!-- 验证码已发送 弹窗 -->
    <v-dialog v-model="smsDialogVisible" max-width="420" persistent>
      <v-card class="sms-dialog-card" rounded="xl">
        <v-card-title class="text-h6 font-weight-bold text-center pt-6">验证码已发送</v-card-title>
        <v-card-text class="text-center pa-4">
          <p class="text-body-2 text-medium-emphasis mb-2">您的注册验证码为</p>
          <div class="sms-code">{{ lastSentCode }}</div>
          <p class="sms-hint mt-3">验证码有效 120 秒，{{ codeCountdown }} 秒后重发</p>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer />
          <v-btn variant="text" color="on-surface-variant" @click="smsDialogVisible = false">稍后自己输入</v-btn>
          <v-btn color="primary" variant="tonal" @click="autoFill">自动填入</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { toast } from '@/utils/toast'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)
const phoneDirty = ref(false)
const agree = ref(true)

const form = reactive({
  phone: '',
  code: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

// 手机号格式
const phoneRegex = /^1\d{10}$/
const validPhone = computed(() => phoneRegex.test(form.phone))

// 验证码倒计时（120 秒，弹窗关闭后仍持续，归零后自动恢复）→ 用于发送短信与「获取验证码」按钮禁用
const codeCountdown = ref(0)
const lastSentCode = ref('')
const smsDialogVisible = ref(false)
let codeTimer = null

function startCountdown() {
  codeCountdown.value = 120
  clearInterval(codeTimer)
  codeTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(codeTimer)
      codeTimer = null
    }
  }, 1000)
}

async function sendCode() {
  if (!phoneRegex.test(form.phone)) {
    toast.error('请输入正确的手机号')
    return
  }
  try {
    const res = await authApi.smsCode({
      phone: form.phone,
      scene: 'register'
    })
    lastSentCode.value = res.data?.code || ''
    startCountdown()
    smsDialogVisible.value = true
  } catch (e) {
    /* 拦截器已提示 */
  }
}

function autoFill() {
  form.code = lastSentCode.value
  smsDialogVisible.value = false
}

function onTabChange() {
  // 切换后清空表单字段
  form.code = ''
  form.nickname = ''
  form.password = ''
  form.confirmPassword = ''
  clearInterval(codeTimer)
  codeTimer = null
  codeCountdown.value = 0
}

function doLoginRedirect() {
  const redirect = route.query.redirect
  router.replace(redirect && redirect !== '/login' ? redirect : '/')
}

async function submit() {
  if (!agree.value) {
    toast.warning('请先同意用户协议与隐私政策')
    return
  }
  loading.value = true
  try {
    if (mode.value === 'login') {
      // 登录：手机号 + 密码
      if (!phoneRegex.test(form.phone)) {
        toast.error('请输入正确的手机号')
        return
      }
      if (!form.password) {
        toast.error('请输入密码')
        return
      }
      await userStore.login({
        phone: form.phone,
        password: form.password
      })
      toast.success('登录成功')
    } else {
      // 注册：手机号 + 验证码 + 昵称 + 密码
      if (!phoneRegex.test(form.phone)) {
        toast.error('请输入正确的手机号')
        return
      }
      if (!form.code) {
        toast.error('请输入验证码')
        return
      }
      if (!form.nickname) {
        toast.error('请输入昵称')
        return
      }
      if (form.password.length < 6) {
        toast.error('密码至少 6 位')
        return
      }
      if (form.password !== form.confirmPassword) {
        toast.error('两次输入的密码不一致')
        return
      }
      await userStore.register({
        phone: form.phone,
        code: form.code,
        nickname: form.nickname,
        password: form.password
      })
      toast.success('注册成功')
    }
    doLoginRedirect()
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
}

onUnmounted(() => clearInterval(codeTimer))
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #fff5f1 0%, #ffe9e6 45%, #ffe4df 100%);
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--v-theme-surface);
  padding: 36px 32px 24px;
  box-shadow: var(--cike-shadow-hover);
}
.brand {
  text-align: center;
  margin-bottom: 20px;
}
.logo {
  width: 64px;
  height: 64px;
  line-height: 64px;
  margin: 0 auto 12px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff5a4e, #ff2442);
  color: #fff;
  font-size: 30px;
  font-weight: 800;
}
.name {
  font-size: 24px;
  font-weight: 800;
  color: var(--cike-text);
  letter-spacing: 4px;
}
.slogan {
  margin-top: 6px;
  color: var(--cike-text-3);
  font-size: 13px;
}
.mode-tabs {
  margin-bottom: 8px;
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-btn {
  flex-shrink: 0;
  width: 120px;
  margin-top: 2px;
}
.submit-btn {
  width: 100%;
  margin-top: 16px;
  letter-spacing: 4px;
  font-weight: 600;
}
.switch-hint {
  text-align: center;
  margin-top: 16px;
  color: var(--cike-text-3);
  font-size: 13px;
  cursor: pointer;
}
.switch-hint span {
  color: var(--cike-primary);
  font-weight: 500;
}
.agreement {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: var(--cike-text-3);
}
.agreement a {
  color: var(--cike-primary);
}
.sms-dialog-card {
  background: var(--v-theme-surface);
}
.sms-code {
  font-size: 40px;
  font-weight: 800;
  letter-spacing: 12px;
  text-indent: 12px;
  color: rgb(var(--v-theme-primary));
}
.sms-hint {
  color: var(--cike-text-3);
  font-size: 13px;
}
</style>
