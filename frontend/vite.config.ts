import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/auth':     { target: 'http://localhost:8081', changeOrigin: true },
      '/ingest':   { target: 'http://localhost:8082', changeOrigin: true },
      '/query':    { target: 'http://localhost:8084', changeOrigin: true },
      '/alerting': { target: 'http://localhost:8085', changeOrigin: true },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test-setup.ts',
  },
});
