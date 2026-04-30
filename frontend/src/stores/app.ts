import { defineStore } from 'pinia'

import type { HealthInfo } from '../api/health'

interface AppState {
  loading: boolean
  health: HealthInfo | null
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    loading: false,
    health: null
  }),
  actions: {
    setLoading(loading: boolean) {
      this.loading = loading
    },
    setHealth(health: HealthInfo) {
      this.health = health
    }
  }
})
