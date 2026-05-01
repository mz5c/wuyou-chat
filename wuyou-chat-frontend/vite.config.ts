import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // SSE 流式请求保持连接活跃
            if (req.url?.includes('/chat/ask/stream')) {
              proxyReq.setHeader('Connection', 'keep-alive');
            }
          });
          proxy.on('proxyRes', (proxyRes, req) => {
            // SSE 响应不缓存
            if (req.url?.includes('/chat/ask/stream')) {
              proxyRes.headers['cache-control'] = 'no-cache';
            }
          });
        },
      },
    },
  },
})
