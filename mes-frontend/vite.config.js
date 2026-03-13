import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    host: true,
    // 禁用缓存以解决开发环境缓存问题
    hmr: {
      overlay: false
    },
    // 配置代理解决CORS问题
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on('error', (err) => {
            console.log('代理错误:', err);
          });
          // 减少日志输出，只记录错误
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.url.includes('/uploads/')) {
              console.log('请求图片资源:', req.url);
            }
          });
          proxy.on('proxyRes', (proxyRes, req) => {
            if (proxyRes.statusCode >= 400) {
              console.log('代理响应错误:', proxyRes.statusCode, req.url);
            }
          });
        }
      }
    }
  },
  // 构建配置
  build: {
    // 清除输出目录
    emptyOutDir: true,
    // 生成源映射
    sourcemap: true
  },
  // 开发环境缓存配置
  optimizeDeps: {
    force: true // 强制重新构建依赖
  }
})
