<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

import {
  fetchDecisionDetail,
  fetchGraph,
  supersedeDecision,
  linkCommit,
  type DecisionDetail,
  type GraphData
} from '../api/decisions'

const route = useRoute()
const router = useRouter()
const detail = ref<DecisionDetail | null>(null)
const loading = ref(false)
const showSupersedeForm = ref(false)
const showCommitForm = ref(false)
const graphRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

const supersedeForm = ref({
  title: '',
  context: '',
  options: '',
  chosenOption: '',
  reason: '',
  impact: '',
  category: 'tech-stack'
})

const commitForm = ref({
  commitHash: '',
  commitMessage: '',
  filePath: '',
  repoUrl: ''
})

const categoryOptions = [
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

const relationTextMap: Record<string, string> = {
  supersedes: '推翻',
  extends: '扩展',
  refines: '完善'
}

function parseOptions(optionsJson: string) {
  if (!optionsJson) return []
  try {
    return JSON.parse(optionsJson)
  } catch {
    return []
  }
}

async function loadDetail() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    detail.value = await fetchDecisionDetail(id)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载决策详情失败')
  } finally {
    loading.value = false
  }
}

async function loadGraph() {
  try {
    const graphData = await fetchGraph()
    renderGraph(graphData)
  } catch {
    // graph is optional, don't show error
  }
}

function renderGraph(data: GraphData) {
  if (!graphRef.value || data.nodes.length === 0) return

  const currentId = String(route.params.id)

  // 构建邻接表，BFS 找出所有关联节点
  const adj = new Map<string, string[]>()
  for (const e of data.edges) {
    if (!adj.has(e.source)) adj.set(e.source, [])
    if (!adj.has(e.target)) adj.set(e.target, [])
    adj.get(e.source)!.push(e.target)
    adj.get(e.target)!.push(e.source)
  }

  const relatedIds = new Set<string>()
  const queue = [currentId]
  relatedIds.add(currentId)
  while (queue.length) {
    const id = queue.shift()!
    for (const neighbor of adj.get(id) || []) {
      if (!relatedIds.has(neighbor)) {
        relatedIds.add(neighbor)
        queue.push(neighbor)
      }
    }
  }

  const relatedEdges = data.edges.filter(e => relatedIds.has(e.source) && relatedIds.has(e.target))
  const relatedNodes = data.nodes.filter(n => relatedIds.has(n.id))

  chart = echarts.init(graphRef.value)

  chart.setOption({
    tooltip: {
      formatter: (params: { data: { name: string; category: string; status: string } }) =>
        `<b>${params.data.name}</b><br/>分类：${categoryTextMap[params.data.category] || params.data.category}<br/>状态：${statusTextMap[params.data.status] || params.data.status}`
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        draggable: true,
        force: {
          repulsion: 200,
          edgeLength: [150, 300]
        },
        data: relatedNodes.map(n => ({
          ...n,
          symbolSize: String(n.id) === currentId ? 36 : 24,
          itemStyle: {
            color: String(n.id) === currentId ? '#0d6c63' :
                   n.status === 'active' ? '#409e42' :
                   n.status === 'superseded' ? '#e67e22' : '#909399'
          }
        })),
        edges: relatedEdges.map(e => ({
          ...e,
          label: {
            show: true,
            formatter: relationTextMap[e.relationType] || e.relationType
          }
        })),
        label: {
          show: true,
          fontSize: 12
        },
        lineStyle: {
          color: '#c0c4cc',
          curveness: 0.2
        }
      }
    ]
  })

  chart.off('click')
  chart.on('click', (params: any) => {
    if (params?.data?.id && params.data.id !== currentId) {
      router.push(`/decisions/${params.data.id}`)
    }
  })
}

async function handleSupersede() {
  if (!supersedeForm.value.title.trim()) {
    ElMessage.warning('请输入决策标题')
    return
  }
  try {
    const id = Number(route.params.id)
    const result = await supersedeDecision(id, supersedeForm.value)
    ElMessage.success('已创建推翻决策并建立关联')
    showSupersedeForm.value = false
    router.push(`/decisions/${result.id}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function handleLinkCommit() {
  if (!commitForm.value.commitHash.trim()) {
    ElMessage.warning('请输入 Commit Hash')
    return
  }
  try {
    const id = Number(route.params.id)
    await linkCommit(id, commitForm.value)
    ElMessage.success('Commit 关联成功')
    showCommitForm.value = false
    commitForm.value = { commitHash: '', commitMessage: '', filePath: '', repoUrl: '' }
    await loadDetail()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '关联失败')
  }
}

onMounted(() => {
  loadDetail()
  loadGraph()
})

watch(() => route.params.id, () => {
  chart?.dispose()
  chart = null
  showSupersedeForm.value = false
  showCommitForm.value = false
  loadDetail()
  loadGraph()
})
</script>

<template>
  <main class="shell">
    <div class="back-nav">
      <el-button :icon="ArrowLeft" text @click="router.push('/decisions')">返回列表</el-button>
    </div>

    <div v-if="detail" class="detail-layout">
      <section class="detail-main">
        <el-card shadow="never" v-loading="loading">
          <template #header>
            <div class="detail-header">
              <div>
                <h2>{{ detail.title }}</h2>
                <div class="detail-meta">
                  <el-tag>{{ categoryTextMap[detail.category] || detail.category }}</el-tag>
                  <el-tag :type="detail.status === 'active' ? 'success' : detail.status === 'superseded' ? 'warning' : 'info'">
                    {{ statusTextMap[detail.status] || detail.status }}
                  </el-tag>
                  <span class="meta-text">{{ detail.createdBy }} · {{ detail.createdAt }}</span>
                </div>
              </div>
              <div class="detail-actions">
                <el-button v-if="detail.status !== 'superseded'" type="primary" @click="showSupersedeForm = !showSupersedeForm">
                  {{ showSupersedeForm ? '取消' : '推翻此决策' }}
                </el-button>
                <el-button @click="showCommitForm = !showCommitForm">
                  {{ showCommitForm ? '取消' : '关联 Commit' }}
                </el-button>
              </div>
            </div>
          </template>

          <div class="detail-section" v-if="detail.context">
            <h3>决策背景</h3>
            <p>{{ detail.context }}</p>
          </div>

          <div class="detail-section" v-if="detail.options">
            <h3>候选方案</h3>
            <div class="options-grid">
              <div
                v-for="(opt, index) in parseOptions(detail.options)"
                :key="index"
                class="option-card"
                :class="{ chosen: opt.label === detail.chosenOption }"
              >
                <div class="option-label">
                  {{ opt.label }}
                  <el-tag v-if="opt.label === detail.chosenOption" size="small" type="success" effect="dark">最终选择</el-tag>
                </div>
                <p class="option-desc">{{ opt.description }}</p>
                <div class="option-pros-cons" v-if="opt.pros || opt.cons">
                  <div v-if="opt.pros" class="pros">
                    <strong>优点：</strong>{{ opt.pros }}
                  </div>
                  <div v-if="opt.cons" class="cons">
                    <strong>缺点：</strong>{{ opt.cons }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="detail.reason">
            <h3>选择理由</h3>
            <p>{{ detail.reason }}</p>
          </div>

          <div class="detail-section" v-if="detail.impact">
            <h3>影响范围</h3>
            <p>{{ detail.impact }}</p>
          </div>

          <div
            v-if="showSupersedeForm"
            class="detail-section supersede-form"
          >
            <h3>创建推翻决策</h3>
            <el-form label-position="top">
              <el-form-item label="新决策标题">
                <el-input v-model="supersedeForm.title" placeholder="新的技术方案或决策" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="supersedeForm.category">
                  <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="决策背景">
                <el-input v-model="supersedeForm.context" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="候选方案 JSON">
                <el-input v-model="supersedeForm.options" type="textarea" :rows="3" placeholder='[{"label":"方案A","description":"...","pros":"...","cons":"..."}]' />
              </el-form-item>
              <el-form-item label="最终方案">
                <el-input v-model="supersedeForm.chosenOption" />
              </el-form-item>
              <el-form-item label="选择理由">
                <el-input v-model="supersedeForm.reason" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="影响范围">
                <el-input v-model="supersedeForm.impact" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSupersede">确认推翻并创建新决策</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div
            v-if="showCommitForm"
            class="detail-section commit-form"
          >
            <h3>关联 Git Commit</h3>
            <el-form label-position="top">
              <el-form-item label="Commit Hash">
                <el-input v-model="commitForm.commitHash" placeholder="abc123..." />
              </el-form-item>
              <el-form-item label="Commit Message">
                <el-input v-model="commitForm.commitMessage" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="关联文件路径">
                <el-input v-model="commitForm.filePath" placeholder="src/utils/request.ts" />
              </el-form-item>
              <el-form-item label="仓库地址">
                <el-input v-model="commitForm.repoUrl" placeholder="https://github.com/..." />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLinkCommit">确认关联</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </section>

      <aside class="detail-sidebar">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>决策链</span>
            </div>
          </template>
          <div class="chain-list">
            <div v-if="!detail.parents.length && !detail.children.length" class="chain-empty">暂无关联决策</div>
            <div v-for="p in detail.parents" :key="'p' + p.id" class="chain-item">
              <span class="chain-tag">← {{ relationTextMap[p.relationType] || p.relationType }}</span>
              <router-link :to="`/decisions/${p.id}`" class="chain-link">{{ p.title }}</router-link>
            </div>
            <div class="chain-current">
              <el-tag type="success" effect="dark" size="small">当前</el-tag>
              <span>{{ detail.title }}</span>
            </div>
            <div v-for="c in detail.children" :key="'c' + c.id" class="chain-item">
              <span class="chain-tag">→ {{ relationTextMap[c.relationType] || c.relationType }}</span>
              <router-link :to="`/decisions/${c.id}`" class="chain-link">{{ c.title }}</router-link>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" v-if="detail.commits.length">
          <template #header>
            <div class="card-header">
              <span>关联 Commit ({{ detail.commits.length }})</span>
            </div>
          </template>
          <div class="commit-list">
            <div v-for="c in detail.commits" :key="c.id" class="commit-item">
              <code>{{ c.commitHash.substring(0, 8) }}</code>
              <p v-if="c.commitMessage">{{ c.commitMessage }}</p>
              <p v-if="c.filePath" class="commit-file">{{ c.filePath }}</p>
            </div>
          </div>
        </el-card>

        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>决策关系图</span>
            </div>
          </template>
          <div ref="graphRef" class="graph-mini"></div>
        </el-card>
      </aside>
    </div>
  </main>
</template>

<style scoped>
.back-nav {
  margin-bottom: 16px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
  align-items: start;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.detail-header h2 {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #103c38;
}

.detail-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.meta-text {
  color: #909399;
  font-size: 0.9rem;
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.detail-section {
  margin-bottom: 28px;
}

.detail-section h3 {
  margin: 0 0 12px;
  color: #103c38;
  font-size: 1.05rem;
  border-left: 3px solid #0d6c63;
  padding-left: 10px;
}

.options-grid {
  display: grid;
  gap: 12px;
}

.option-card {
  border: 1px solid rgba(16, 60, 56, 0.1);
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 251, 246, 0.5);
}

.option-card.chosen {
  border-color: #67c23a;
  background: rgba(103, 194, 58, 0.05);
}

.option-label {
  display: flex;
  gap: 8px;
  align-items: center;
  font-weight: 700;
  margin-bottom: 6px;
}

.option-desc {
  color: #546662;
  margin: 0 0 6px;
}

.option-pros-cons {
  font-size: 0.9rem;
}

.pros { color: #67c23a; }
.cons { color: #e6a23c; }

.supersede-form,
.commit-form {
  border-top: 1px solid rgba(16, 60, 56, 0.1);
  padding-top: 20px;
}

.chain-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chain-item {
  display: flex;
  gap: 6px;
  align-items: center;
}

.chain-link {
  color: #0d6c63;
  cursor: pointer;
  font-weight: 600;
}

.chain-link:hover {
  text-decoration: underline;
}

.chain-empty {
  color: #909399;
  font-size: 0.9rem;
  padding: 4px 0;
}

.chain-tag {
  font-size: 0.8rem;
  color: #909399;
  white-space: nowrap;
}

.chain-current {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px;
  background: rgba(103, 194, 58, 0.06);
  border-radius: 8px;
  font-weight: 700;
  color: #103c38;
}

.commit-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.commit-item {
  padding: 8px;
  border: 1px solid rgba(16, 60, 56, 0.08);
  border-radius: 8px;
}

.commit-item code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.85rem;
}

.commit-item p {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: #546662;
}

.commit-file {
  font-family: monospace;
  font-size: 0.8rem !important;
  color: #909399 !important;
}

.graph-mini {
  min-height: 280px;
}

@media (max-width: 920px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-header {
    flex-direction: column;
  }

  .detail-actions {
    width: 100%;
  }
}
</style>
