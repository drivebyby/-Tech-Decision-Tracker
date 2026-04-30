<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

import { createDecision } from '../api/decisions'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  title: '',
  context: '',
  options: '',
  chosenOption: '',
  reason: '',
  impact: '',
  category: 'tech-stack'
})

const categoryOptions = [
  { label: '技术栈选型', value: 'tech-stack' },
  { label: '架构设计', value: 'architecture' },
  { label: '工具链', value: 'tooling' },
  { label: '流程规范', value: 'process' }
]

async function handleCreate() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入决策标题')
    return
  }
  loading.value = true
  try {
    const result = await createDecision({
      title: form.title,
      context: form.context,
      options: form.options,
      chosenOption: form.chosenOption,
      reason: form.reason,
      impact: form.impact,
      category: form.category
    })
    ElMessage.success('决策创建成功')
    router.push(`/decisions/${result.id}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="shell">
    <div class="back-nav">
      <el-button :icon="ArrowLeft" text @click="router.push('/decisions')">返回列表</el-button>
    </div>

    <section>
      <p class="eyebrow">New Decision</p>
      <h1>记录一个新决策</h1>
      <p class="subtitle">结构化地记录技术决策的背景、候选方案、选择理由和影响范围。</p>
    </section>

    <el-card shadow="never" class="form-card">
      <el-form label-position="top" @submit.prevent="handleCreate">
        <el-row :gutter="24">
          <el-col :span="16">
            <el-form-item label="决策标题" required>
              <el-input v-model="form.title" placeholder="例如：将 Axios 替换为原生 Fetch" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" required>
              <el-select v-model="form.category" size="large" style="width: 100%">
                <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="决策背景">
          <el-input v-model="form.context" type="textarea" :rows="3"
            placeholder="描述是什么问题或场景触发了这次决策..." />
        </el-form-item>

        <el-form-item label="候选方案">
          <el-input v-model="form.options" type="textarea" :rows="4"
            placeholder='JSON 格式：[{"label":"方案A","description":"方案描述","pros":"优点","cons":"缺点"}]' />
          <div class="form-hint">每个候选方案包含 label（名称）、description（描述）、pros（优点）、cons（缺点）</div>
        </el-form-item>

        <el-form-item label="最终方案">
          <el-input v-model="form.chosenOption" placeholder="与候选方案中的 label 一致" />
        </el-form-item>

        <el-form-item label="选择理由">
          <el-input v-model="form.reason" type="textarea" :rows="3"
            placeholder="为什么选择这个方案而非其他..." />
        </el-form-item>

        <el-form-item label="影响范围">
          <el-input v-model="form.impact" type="textarea" :rows="3"
            placeholder="这个决策会影响哪些模块、团队或后续开发..." />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleCreate">
            记录决策
          </el-button>
          <el-button size="large" @click="router.push('/decisions')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </main>
</template>

<style scoped>
.back-nav {
  margin-bottom: 16px;
}

h1 {
  font-size: clamp(2rem, 5vw, 3.2rem);
}

.form-card {
  margin-top: 28px;
}

.form-hint {
  font-size: 0.85rem;
  color: #909399;
  margin-top: 6px;
}
</style>
