<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { fetchUsers, type User } from '../api/users'

const users = ref<User[]>([])
const loading = ref(false)

async function loadUsers() {
  loading.value = true
  try {
    const page = await fetchUsers()
    users.value = page.records
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <el-table v-loading="loading" :data="users" empty-text="暂无数据" class="user-table">
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="username" label="用户名" />
    <el-table-column prop="email" label="邮箱" />
    <el-table-column label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'info'">
          {{ row.status === 1 ? '启用' : '禁用' }}
        </el-tag>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.user-table {
  width: 100%;
}
</style>
