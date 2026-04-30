<script setup lang="ts">
import { Key, Message, PictureFilled, RefreshRight, User } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { fetchCaptcha, login, register, type CaptchaInfo } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref<'login' | 'register'>('login')
const loading = ref(false)
const captcha = ref<CaptchaInfo | null>(null)

const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  captchaCode: ''
})

async function refreshCaptcha() {
  captcha.value = await fetchCaptcha()
  loginForm.captchaCode = ''
  registerForm.captchaCode = ''
}

async function handleLogin() {
  if (!captcha.value) {
    await refreshCaptcha()
    return
  }

  loading.value = true
  try {
    const result = await login({
      username: loginForm.username,
      password: loginForm.password,
      captchaCode: loginForm.captchaCode,
      captchaKey: captcha.value.captchaKey
    })
    authStore.setAuth(result.token, result.userInfo)
    ElMessage.success('登录成功')
    await router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!captcha.value) {
    await refreshCaptcha()
    return
  }

  loading.value = true
  try {
    const result = await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      captchaCode: registerForm.captchaCode,
      captchaKey: captcha.value.captchaKey
    })
    authStore.setAuth(result.token, result.userInfo)
    ElMessage.success('注册成功，已自动登录')
    await router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '注册失败')
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

refreshCaptcha()
</script>

<template>
  <div class="auth-layout">
    <section class="auth-hero">
      <p class="auth-kicker">Account Gateway</p>
      <h1>欢迎进入你的业务控制台</h1>
      <p class="auth-copy">
        这里已经接入了登录、注册、图形验证码和登录态保护。先通过人机验证，再进入系统。
      </p>

      <div class="auth-feature-list">
        <div class="auth-feature">
          <span class="auth-dot"></span>
          <span>JWT 登录态与接口拦截</span>
        </div>
        <div class="auth-feature">
          <span class="auth-dot"></span>
          <span>BCrypt 密码加密存储</span>
        </div>
        <div class="auth-feature">
          <span class="auth-dot"></span>
          <span>SVG 图形验证码防刷</span>
        </div>
      </div>
    </section>

    <section class="auth-panel">
      <el-card shadow="never" class="auth-card">
        <template #header>
          <el-segmented
            v-model="activeTab"
            :options="[
              { label: '登录', value: 'login' },
              { label: '注册', value: 'register' }
            ]"
            block
          />
        </template>

        <el-form v-if="activeTab === 'login'" label-position="top" @submit.prevent="handleLogin">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" :prefix-icon="User" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="loginForm.password"
              :prefix-icon="Key"
              placeholder="请输入密码"
              show-password
              type="password"
            />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captchaCode"
                :prefix-icon="PictureFilled"
                maxlength="6"
                placeholder="输入验证码"
              />
              <button class="captcha-box" type="button" @click="refreshCaptcha">
                <img v-if="captcha" :src="captcha.captchaSvg" alt="captcha" />
                <el-icon v-else class="captcha-icon"><RefreshRight /></el-icon>
              </button>
            </div>
          </el-form-item>
          <el-button class="auth-submit" type="primary" :loading="loading" @click="handleLogin">
            立即登录
          </el-button>
        </el-form>

        <el-form v-else label-position="top" @submit.prevent="handleRegister">
          <el-form-item label="用户名">
            <el-input v-model="registerForm.username" :prefix-icon="User" placeholder="4-50 位字母数字下划线" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="registerForm.email" :prefix-icon="Message" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="registerForm.password"
              :prefix-icon="Key"
              placeholder="请输入密码"
              show-password
              type="password"
            />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input
              v-model="registerForm.confirmPassword"
              :prefix-icon="Key"
              placeholder="再次输入密码"
              show-password
              type="password"
            />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="captcha-row">
              <el-input
                v-model="registerForm.captchaCode"
                :prefix-icon="PictureFilled"
                maxlength="6"
                placeholder="输入验证码"
              />
              <button class="captcha-box" type="button" @click="refreshCaptcha">
                <img v-if="captcha" :src="captcha.captchaSvg" alt="captcha" />
                <el-icon v-else class="captcha-icon"><RefreshRight /></el-icon>
              </button>
            </div>
          </el-form-item>
          <el-button class="auth-submit" type="primary" :loading="loading" @click="handleRegister">
            创建账号
          </el-button>
        </el-form>
      </el-card>
    </section>
  </div>
</template>
