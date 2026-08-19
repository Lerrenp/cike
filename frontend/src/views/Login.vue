<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="logo">刻</div>
        <h1 class="name">此刻</h1>
        <p class="slogan">记录此刻，分享美好</p>
      </div>

      <!-- tab 切换 -->
      <el-tabs v-model="mode" class="mode-tabs" @tab-change="onTabChange">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent
      >
        <!-- 手机号 -->
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            @input="phoneDirty = true"
          >
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>

        <!-- 验证码（仅注册用验证码，登录可选密码/验证码两种）-->
        <el-form-item v-if="mode === 'register'" prop="code">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="请输入验证码" maxlength="6" />
            <el-button
              class="code-btn"
              :disabled="codeCountdown > 0 || !validPhone"
              @click="sendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 昵称（注册） -->
        <el-form-item v-if="mode === 'register'" prop="nickname">
          <el-input v-model="form.nickname" placeholder="设置昵称" maxlength="20">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="mode === 'register' ? '设置密码（6-20位）' : '请输入密码'"
            @keyup.enter="submit"
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-button
          type="primary"
          class="submit-btn"
          size="large"
          :loading="loading"
          @click="submit"
        >
          {{ mode === 'login' ? '登 录' : '注 册' }}
        </el-button>

        <p v-if="mode === 'login'" class="switch-hint" @click="mode = 'register'">
          还没有账号？<span>立即注册</span>
        </p>
        <p v-else class="switch-hint" @click="mode = 'login'">
          已有账号？<span>去登录</span>
        </p>
      </el-form>

      <div class="agreement">
        <span>登录即代表同意</span>
        <a href="javascript:void(0)" @click="agree = true">《用户协议》</a>
        <span>与</span>
        <a href="javascript:void(0)" @click="agree = true">《隐私政策》</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const formRef = ref()
const loading = ref(false)
const phoneDirty = ref(false)
const agree = ref(true)

const form = reactive({
  phone: '',
  code: '',
  nickname: '',
  password: ''
})

// 手机号格式
const phoneRegex = /^1[3-9]\d{9}$/
const validPhone = computed(() => phoneRegex.test(form.phone))

const codeCountdown = ref(0)
let timer = null

function startCountdown() {
  codeCountdown.value = 60
  timer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function sendCode() {
  if (!validPhone.value) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    const res = await authApi.smsCode({
      phone: form.phone,
      scene: mode.value === 'register' ? 'register' : 'login'
    })
    ElMessage.success(`验证码已发送${res.data?.code ? `：${res.data.code}` : ''}`)
    startCountdown()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phoneRegex, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      validator: (_, val, cb) => {
        if (val && val.length < 6) cb('密码至少 6 位')
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

function onTabChange() {
  // 切换后清空表单与校验
  form.code = ''
  form.nickname = ''
  form.password = ''
  clearInterval(timer)
  codeCountdown.value = 0
  formRef.value?.clearValidate()
}

function doLoginRedirect() {
  const redirect = route.query.redirect
  router.replace(redirect && redirect !== '/login' ? redirect : '/')
}

async function submit() {
  if (!agree.value) {
    ElMessage.warning('请先同意用户协议与隐私政策')
    return
  }
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (mode.value === 'login') {
        await userStore.login({
          phone: form.phone,
          password: form.password
        })
        ElMessage.success('登录成功')
      } else {
        await userStore.register({
          phone: form.phone,
          code: form.code,
          nickname: form.nickname,
          password: form.password
        })
        ElMessage.success('注册成功')
      }
      doLoginRedirect()
    } catch (e) {
      /* 已提示 */
    } finally {
      loading.value = false
    }
  })
}

onUnmounted(() => clearInterval(timer))
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
  background: #fff;
  border-radius: 20px;
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
.mode-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
}
.mode-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--cike-primary);
}
.code-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-btn {
  flex-shrink: 0;
  width: 120px;
}
.submit-btn {
  width: 100%;
  margin-top: 6px;
  border-radius: 12px;
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
</style>
