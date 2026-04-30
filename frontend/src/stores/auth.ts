import { defineStore } from 'pinia'

import { fetchCurrentUser, type UserProfile } from '../api/auth'

interface AuthState {
  token: string
  user: UserProfile | null
}

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function readUserCache() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUserCache()
  }),
  actions: {
    setAuth(token: string, user: UserProfile) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
    async fetchProfile() {
      const user = await fetchCurrentUser()
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
      return user
    }
  }
})
