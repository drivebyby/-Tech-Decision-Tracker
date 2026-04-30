<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Connection, DataAnalysis, Refresh, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import DashboardChart from '../components/DashboardChart.vue'
import UserTable from '../components/UserTable.vue'
import { checkHealth } from '../api/health'
import { useAppStore } from '../stores/app'
import { useAuthStore } from '../stores/auth'

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()

const serviceStatus = computed(() => (appStore.health?.status === 'UP' ? 'success' : 'warning'))

async function refreshHealth() {
  appStore.setLoading(true)
  try {
    const health = await checkHealth()
    appStore.setHealth(health)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '后端状态获取失败')
  } finally {
    appStore.setLoading(false)
  }
}

function logout() {
  authStore.logout()
  router.push('/login')
}

onMounted(refreshHealth)
</script>

<template>
  <main class="shell">
    <section class="hero">
      <div>
        <p class="eyebrow">Secure Workspace</p>
        <h1>登录与注册流程已经接入</h1>
        <p class="subtitle">
          当前账号：{{ authStore.user?.username || '未登录' }}。系统已启用登录态校验、图形验证码与密码加密存储。
        </p>
        <div class="actions">
          <el-button type="primary" :icon="Refresh" :loading="appStore.loading" @click="refreshHealth">
            刷新后端状态
          </el-button>
          <el-button plain :icon="SwitchButton" @click="logout">退出登录</el-button>
          <el-tag :type="serviceStatus" size="large">
            API {{ appStore.health?.status ?? '未连接' }}
          </el-tag>
        </div>
      </div>

      <el-card class="status-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Connection /></el-icon>
            <span>服务状态</span>
          </div>
        </template>
        <p>接口地址：<strong>/api/health</strong></p>
        <p>服务时间：{{ appStore.health?.time ?? '等待连接' }}</p>
        <p>登录邮箱：{{ authStore.user?.email ?? '暂无' }}</p>
      </el-card>
    </section>

    <section class="grid">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>访问趋势</span>
          </div>
        </template>
        <DashboardChart />
      </el-card>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>用户列表</span>
          </div>
        </template>
        <UserTable />
      </el-card>
    </section>
  </main>
</template>
