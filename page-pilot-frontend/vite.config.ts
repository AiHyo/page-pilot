import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    host: '127.0.0.1',
    port: 5175,
    strictPort: true,
    proxy: {
      '/api': {
        // 本机 8123 是 z-ai-agent。覆盖：VITE_PROXY_TARGET=http://localhost:8123
        target: process.env.VITE_PROXY_TARGET || 'http://localhost:8124',
        changeOrigin: true,
        ws: true,
      },
      '/generated': {
        target: process.env.VITE_PROXY_TARGET || 'http://localhost:8124',
        changeOrigin: true,
      }
    }
  }
})
