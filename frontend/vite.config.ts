import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    allowedHosts: ['.cpolar.top', '.cpolar.cn'],
    proxy: {
      '/api': {
        target: 'http://61c92721.r7.cpolar.cn',
        changeOrigin: true
      }
    }
  }
})
