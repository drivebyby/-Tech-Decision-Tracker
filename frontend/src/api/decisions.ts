import { http, type ApiResponse } from './http'

export interface DecisionOption {
  label: string
  description: string
  pros: string
  cons: string
}

export interface ChainNode {
  id: number
  title: string
  relationType: string
}

export interface CommitInfo {
  id: number
  commitHash: string
  commitMessage: string
  filePath: string
  repoUrl: string
}

export interface DecisionDetail {
  id: number
  title: string
  context: string
  options: string
  chosenOption: string
  reason: string
  impact: string
  status: string
  category: string
  createdBy: string
  createdAt: string
  updatedAt: string
  parents: ChainNode[]
  children: ChainNode[]
  commits: CommitInfo[]
}

export interface DecisionListItem {
  id: number
  title: string
  chosenOption: string
  status: string
  category: string
  createdBy: string
  createdAt: string
  parentCount: number
  childCount: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface DecisionCreateRequest {
  title: string
  context: string
  options: string
  chosenOption: string
  reason: string
  impact: string
  category: string
}

export interface SupersedeRequest {
  parentId: number
  title: string
  context: string
  options: string
  chosenOption: string
  reason: string
  impact: string
  category: string
}

export interface GraphData {
  nodes: { id: string; name: string; category: string; status: string }[]
  edges: { source: string; target: string; relationType: string }[]
}

export async function fetchDecisions(params: {
  page?: number
  size?: number
  category?: string
  status?: string
}) {
  const response = await http.get<ApiResponse<PageResult<DecisionListItem>>>('/decisions', { params })
  return response.data.data
}

export async function fetchDecisionDetail(id: number) {
  const response = await http.get<ApiResponse<DecisionDetail>>(`/decisions/${id}`)
  return response.data.data
}

export async function createDecision(data: DecisionCreateRequest) {
  const response = await http.post<ApiResponse<DecisionDetail>>('/decisions', data)
  return response.data.data
}

export async function updateDecision(id: number, data: DecisionCreateRequest & { status?: string }) {
  const response = await http.put<ApiResponse<DecisionDetail>>(`/decisions/${id}`, data)
  return response.data.data
}

export async function supersedeDecision(id: number, data: Omit<SupersedeRequest, 'parentId'>) {
  const response = await http.post<ApiResponse<DecisionDetail>>(`/decisions/${id}/supersede`, {
    ...data,
    parentId: id
  })
  return response.data.data
}

export async function fetchTimeline(category?: string) {
  const response = await http.get<ApiResponse<DecisionListItem[]>>('/decisions/timeline', {
    params: category ? { category } : {}
  })
  return response.data.data
}

export async function fetchGraph() {
  const response = await http.get<ApiResponse<GraphData>>('/decisions/graph')
  return response.data.data
}

export async function linkCommit(decisionId: number, data: {
  commitHash: string
  commitMessage: string
  filePath: string
  repoUrl: string
}) {
  const response = await http.post<ApiResponse<CommitInfo>>(`/decisions/${decisionId}/commits`, data)
  return response.data.data
}
