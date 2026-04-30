<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isAuthPage = computed(() => route.path === '/login')

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div v-if="!isAuthPage && authStore.token" class="top-nav">
    <div class="nav-inner">
      <div class="nav-links">
        <router-link to="/decisions" class="nav-link">决策列表</router-link>
        <router-link to="/timeline" class="nav-link">时间线</router-link>
        <router-link to="/dashboard" class="nav-link">仪表盘</router-link>
      </div>
      <div class="nav-right">
        <span class="nav-user">{{ authStore.user?.username }}</span>
        <el-button size="small" text @click="logout">退出</el-button>
      </div>
    </div>
  </div>
  <router-view />
</template>

<style scoped>
.top-nav {
  background: rgba(255, 251, 246, 0.9);
  border-bottom: 1px solid rgba(16, 60, 56, 0.08);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-inner {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 52px;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-link {
  padding: 8px 16px;
  border-radius: 8px;
  color: #29433f;
  font-weight: 600;
  font-size: 0.95rem;
  transition: background 0.15s;
}

.nav-link:hover {
  background: rgba(13, 108, 99, 0.08);
}

.nav-link.router-link-active {
  background: rgba(13, 108, 99, 0.12);
  color: #0d6c63;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-user {
  color: #546662;
  font-weight: 600;
}
</style>
