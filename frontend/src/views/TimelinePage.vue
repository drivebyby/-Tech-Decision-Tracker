<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

import {
  createDecision,
  fetchTimeline,
  type DecisionListItem
} from '../api/decisions'

const router = useRouter()
const loading = ref(false)
const decisions = ref<DecisionListItem[]>([])
const showCreate = ref(false)

const filters = reactive({
  category: ''
})

const categoryOptions = [
  { label: '全部', value: '' },
  { label: '技术栈选型', value: 'tech-stack' },
  { label: '架构设计', value: 'architecture' },
  { label: '工具链', value: 'tooling' },
  { label: '流程规范', value: 'process' }
]

const categoryTextMap: Record<string, string> = {
  'tech-stack': '技术栈选型',
  architecture: '架构设计',
  tooling: '工具链',
  process: '流程规范'
}

const statusTextMap: Record<string, string> = {
  active: '生效中',
  superseded: '已推翻',
  deprecated: '已废弃'
}

const statusColorMap: Record<string, string> = {
  active: '#67c23a',
  superseded: '#e6a23c',
  deprecated: '#909399'
}

const createForm = ref({
  title: '',
  context: '',
  options: '',
  chosenOption: '',
  reason: '',
  impact: '',
  category: 'tech-stack'
})

async function loadTimeline() {
  loading.value = true
  try {
    decisions.value = await fetchTimeline(filters.category || undefined)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载时间线失败')
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('请输入决策标题')
    return
  }
  try {
    const result = await createDecision(createForm.value)
    ElMessage.success('决策创建成功')
    showCreate.value = false
    createForm.value = { title: '', context: '', options: '', chosenOption: '', reason: '', impact: '', category: 'tech-stack' }
    await loadTimeline()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建失败')
  }
}

function goDetail(id: number) {
  router.push(`/decisions/${id}`)
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(loadTimeline)
</script>

<template>
  <main class="shell">
    <section class="page-header">
      <div>
        <p class="eyebrow">Decision Timeline</p>
        <h1>决策时间线</h1>
        <p class="subtitle">按时间顺序追踪每一个技术决策的诞生。</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filters.category" placeholder="分类筛选" clearable @change="loadTimeline">
          <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="showCreate = !showCreate">
          {{ showCreate ? '取消' : '新建决策' }}
        </el-button>
      </div>
    </section>

    <el-card v-if="showCreate" shadow="never" class="create-card">
      <h3>新建决策</h3>
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="决策标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="createForm.category">
            <el-option v-for="opt in categoryOptions.filter(o => o.value)" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="背景">
          <el-input v-model="createForm.context" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="候选方案 JSON">
          <el-input v-model="createForm.options" type="textarea" :rows="2" placeholder='[{"label":"方案A","description":"...","pros":"...","cons":"..."}]' />
        </el-form-item>
        <el-form-item label="最终方案">
          <el-input v-model="createForm.chosenOption" />
        </el-form-item>
        <el-form-item label="选择理由">
          <el-input v-model="createForm.reason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="影响范围">
          <el-input v-model="createForm.impact" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreate">确认创建</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <el-timeline v-if="decisions.length">
        <el-timeline-item
          v-for="d in decisions"
          :key="d.id"
          :timestamp="d.createdAt"
          placement="top"
          :color="statusColorMap[d.status] || '#909399'"
        >
          <el-card shadow="hover" class="timeline-card" @click="goDetail(d.id)">
            <div class="timeline-card-header">
              <h4>{{ d.title }}</h4>
              <div class="timeline-tags">
                <el-tag size="small">{{ categoryTextMap[d.category] || d.category }}</el-tag>
                <el-tag :type="d.status === 'active' ? 'success' : d.status === 'superseded' ? 'warning' : 'info'" size="small">
                  {{ statusTextMap[d.status] || d.status }}
                </el-tag>
              </div>
            </div>
            <p v-if="d.chosenOption" class="timeline-option">方案：{{ d.chosenOption }}</p>
            <div class="timeline-meta">
              <span>{{ d.createdBy }}</span>
              <span v-if="d.childCount">→ 被推翻 {{ d.childCount }} 次</span>
              <span v-if="d.parentCount">← 推翻 {{ d.parentCount }} 次</span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无决策记录" />
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.create-card {
  margin-bottom: 24px;
}

.create-card h3 {
  margin: 0 0 16px;
  color: #103c38;
}

.timeline-card {
  cursor: pointer;
  transition: transform 0.15s;
}

.timeline-card:hover {
  transform: translateY(-2px);
}

.timeline-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.timeline-card-header h4 {
  margin: 0;
  color: #103c38;
}

.timeline-tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.timeline-option {
  color: #0d6c63;
  font-weight: 600;
  margin: 4px 0;
}

.timeline-meta {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: #909399;
}

@media (max-width: 560px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
