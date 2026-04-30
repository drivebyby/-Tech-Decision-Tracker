import { http, type ApiResponse } from './http'

export interface CaptchaInfo {
  captchaKey: string
  captchaSvg: string
}

export interface UserProfile {
  id: number
  username: string
  email: string
}

export interface AuthPayload {
  token: string
  userInfo: UserProfile
}

export interface LoginForm {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
}

export interface RegisterForm extends LoginForm {
  email: string
  confirmPassword: string
}

export async function fetchCaptcha() {
  const response = await http.get<ApiResponse<CaptchaInfo>>('/auth/captcha')
  return response.data.data
}

export async function login(payload: LoginForm) {
  const response = await http.post<ApiResponse<AuthPayload>>('/auth/login', payload)
  return response.data.data
}

export async function register(payload: RegisterForm) {
  const response = await http.post<ApiResponse<AuthPayload>>('/auth/register', payload)
  return response.data.data
}

export async function fetchCurrentUser() {
  const response = await http.get<ApiResponse<UserProfile>>('/auth/me')
  return response.data.data
}
