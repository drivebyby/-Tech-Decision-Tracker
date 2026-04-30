import { http, type ApiResponse } from './http'

export interface HealthInfo {
  status: string
  time: string
}

export async function checkHealth() {
  const response = await http.get<ApiResponse<HealthInfo>>('/health')
  return response.data.data
}
