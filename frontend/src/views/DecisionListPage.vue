<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'

import {
  fetchDecisions,
  type DecisionListItem
} from '../api/decisions'

const router = useRouter()
const loading = ref(false)
const decisions = ref<DecisionListItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filters = reactive({
  category: '',
  status: ''
})

const categoryOptions = [
  { label: '全部', value: '' },
  { label: '技术栈选型', value: 'tech-stack' },
  { label: '架构设计', value: 'architecture' },
  { label: '工具链', value: 'tooling' },
  { label: '流程规范', value: 'process' }
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '生效中', value: 'active' },
  { label: '已推翻', value: 'superseded' },
  { label: '已废弃', value: 'deprecated' }
]

const statusTagMap: Record<string, string> = {
  active: 'success',
  superseded: 'warning',
  deprecated: 'info'
}

const statusTextMap: Record<string, string> = {
  active: '生效中',
  superseded: '已推翻',
  deprecated: '已废弃'
}

const categoryTextMap: Record<string, string> = {
  'tech-stack': '技术栈选型',
  architecture: '架构设计',
  tooling: '工具链',
  process: '流程规范'
}

async function loadDecisions() {
  loading.value = true
  try {
    const page = await fetchDecisions({
      page: currentPage.value,
      size: pageSize.value,
      category: filters.category || undefined,
      status: filters.status || undefined
    })
    decisions.value = page.records
    total.value = page.total
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载决策列表失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadDecisions()
}

function goDetail(row: { id: number }) {
  router.push(`/decisions/${row.id}`)
}

function goCreate() {
  router.push('/decisions/new')
}

onMounted(loadDecisions)
</script>

<template>
  <main class="shell">
    <section class="page-header">
      <div>
        <p class="eyebrow">Decision Tracker</p>
        <h1>技术决策</h1>
        <p class="subtitle">记录每一次技术选型、架构决策及其演化过程，让决策链可追踪。</p>
      </div>
      <el-button type="primary" :icon="Plus" size="large" @click="goCreate">新建决策</el-button>
    </section>

    <section class="filters">
      <el-select v-model="filters.category" placeholder="分类筛选" clearable @change="loadDecisions">
        <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="loadDecisions">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button :icon="RefreshRight" @click="loadDecisions">刷新</el-button>
    </section>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="decisions" empty-text="暂无决策记录" @row-click="goDetail">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="决策标题" min-width="200">
          <template #default="{ row }">
            <span class="decision-title-link">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ categoryTextMap[row.category] || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chosenOption" label="最终方案" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status] || 'info'" size="small">
              {{ statusTextMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="100" />
        <el-table-column label="关联" width="80">
          <template #default="{ row }">
            <span v-if="row.parentCount" style="color: #e6a23c; margin-right: 6px">←{{ row.parentCount }}</span>
            <span v-if="row.childCount" style="color: #67c23a">→{{ row.childCount }}</span>
            <span v-if="!row.parentCount && !row.childCount" style="color: #c0c4cc">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          background
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </main>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: clamp(2rem, 5vw, 3.2rem);
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.decision-title-link {
  color: #0d6c63;
  cursor: pointer;
  font-weight: 600;
}

.decision-title-link:hover {
  text-decoration: underline;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
