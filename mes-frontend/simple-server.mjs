import express from 'express';
import cors from 'cors';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 8080;

// 启用CORS
app.use(cors());
app.use(express.json());

console.log('🚀 启动MES模拟服务器...');

// 健康检查端点
app.get('/api/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'mock-server',
    timestamp: Date.now(),
    message: '模拟服务器运行正常'
  });
});

app.get('/api/equipment/health', (req, res) => {
  res.json({ status: 'UP', service: 'equipment' });
});

app.get('/api/process/health', (req, res) => {
  res.json({ status: 'UP', service: 'process' });
});

app.get('/api/production/health', (req, res) => {
  res.json({ status: 'UP', service: 'production' });
});

// 图片占位符服务
app.get('/api/uploads/patterns/:filename', (req, res) => {
  const filename = req.params.filename;
  
  // 返回SVG占位图片
  const placeholderSvg = `<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
    <rect width="100" height="100" fill="#e0e0e0" stroke="#ccc" stroke-width="1"/>
    <text x="50" y="40" text-anchor="middle" font-family="Arial" font-size="10" fill="#666">测试图片</text>
    <text x="50" y="55" text-anchor="middle" font-family="Arial" font-size="8" fill="#999">${filename.substring(0, 12)}...</text>
    <text x="50" y="70" text-anchor="middle" font-family="Arial" font-size="8" fill="#999">占位符</text>
  </svg>`;
  
  res.set({
    'Content-Type': 'image/svg+xml',
    'Cache-Control': 'no-cache'
  });
  
  res.send(placeholderSvg);
});

// 模拟图案API
app.get('/api/patterns', (req, res) => {
  const mockPatterns = {
    content: [
      {
        id: 1,
        patternCode: 'PTN-001',
        patternName: '测试图案1',
        machineModel: 'A',
        defaultPrintSpeed: 100,
        defaultPressure: 50,
        planId: '1',
        taskId: '1',
        deviceId: '1',
        imageUrl: '/uploads/patterns/test-pattern-1.jpg'
      },
      {
        id: 2,
        patternCode: 'PTN-002',
        patternName: '测试图案2',
        machineModel: 'B',
        defaultPrintSpeed: 120,
        defaultPressure: 60,
        planId: '2',
        taskId: '2',
        deviceId: '2',
        imageUrl: '/uploads/patterns/test-pattern-2.jpg'
      }
    ],
    totalElements: 2,
    totalPages: 1,
    last: true
  };
  
  res.json(mockPatterns);
});

// 其他模拟API
app.get('/api/plans', (req, res) => {
  res.json({
    content: [
      { id: 1, planCode: 'PLAN-001' },
      { id: 2, planCode: 'PLAN-002' }
    ]
  });
});

app.get('/api/tasks', (req, res) => {
  res.json({
    content: [
      { id: 1, taskCode: 'TASK-001' },
      { id: 2, taskCode: 'TASK-002' }
    ]
  });
});

app.get('/api/devices', (req, res) => {
  res.json({
    content: [
      { id: 1, deviceCode: 'DEV-001' },
      { id: 2, deviceCode: 'DEV-002' }
    ]
  });
});

// 启动服务器
app.listen(PORT, () => {
  console.log('');
  console.log('🎉 MES模拟服务器启动成功！');
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/api/health`);
  console.log(`📊 图案API: http://localhost:${PORT}/api/patterns`);
  console.log(`🖼️ 图片服务: http://localhost:${PORT}/api/uploads/patterns/test.jpg`);
  console.log('');
  console.log('✅ 现在可以刷新前端页面测试了！');
  console.log('按 Ctrl+C 停止服务器');
});