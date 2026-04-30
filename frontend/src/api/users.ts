import { http, type ApiResponse } from './http'

export interface User {
  id: number
  username: string
  email: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export async function fetchUsers(page = 1, size = 10) {
  const response = await http.get<ApiResponse<PageResult<User>>>('/users', {
    params: { page, size }
  })
  return response.data.data
}
