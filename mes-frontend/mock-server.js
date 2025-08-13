import express from 'express';
import cors from 'cors';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

// 获取当前文件的目录路径（ES模块中的__dirname替代）
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = 8080;

// 启用CORS
app.use(cors());
app.use(express.json());

// 静态文件服务 - 提供图片访问
app.use('/uploads', express.static(path.join(__dirname, 'public', 'uploads')));

// 健康检查端点
app.get('/api/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'mock-server',
    timestamp: Date.now()
  });
});

app.get('/api/equipment/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'equipment',
    timestamp: Date.now()
  });
});

app.get('/api/process/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'process',
    timestamp: Date.now()
  });
});

app.get('/api/production/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'production',
    timestamp: Date.now()
  });
});

// 模拟图片服务 - 如果静态文件不存在，返回占位图片
app.get('/api/uploads/patterns/:filename', (req, res) => {
  const filename = req.params.filename;
  const filePath = path.join(__dirname, 'public', 'uploads', 'patterns', filename);
  
  // 检查文件是否存在
  if (fs.existsSync(filePath)) {
    // 文件存在，直接返回
    res.sendFile(filePath);
  } else {
    // 文件不存在，返回SVG占位图片
    const placeholderSvg = `<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
      <rect width="100" height="100" fill="#f0f0f0" stroke="#ddd" stroke-width="1"/>
      <text x="50" y="45" text-anchor="middle" font-family="Arial" font-size="10" fill="#999">图片不存在</text>
      <text x="50" y="60" text-anchor="middle" font-family="Arial" font-size="8" fill="#999">${filename.substring(0, 15)}...</text>
    </svg>`;
    
    res.set({
      'Content-Type': 'image/svg+xml',
      'Cache-Control': 'no-cache'
    });
    
    res.send(placeholderSvg);
  }
});

// 模拟图案管理API
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

// 模拟计划API
app.get('/api/plans', (req, res) => {
  const mockPlans = {
    content: [
      { id: 1, planCode: 'PLAN-001' },
      { id: 2, planCode: 'PLAN-002' }
    ]
  };
  res.json(mockPlans);
});

// 模拟任务API
app.get('/api/tasks', (req, res) => {
  const mockTasks = {
    content: [
      { id: 1, taskCode: 'TASK-001' },
      { id: 2, taskCode: 'TASK-002' }
    ]
  };
  res.json(mockTasks);
});

// 模拟设备API
app.get('/api/devices', (req, res) => {
  const mockDevices = {
    content: [
      { id: 1, deviceCode: 'DEV-001' },
      { id: 2, deviceCode: 'DEV-002' }
    ]
  };
  res.json(mockDevices);
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`🚀 MES模拟服务器启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/api/health`);
  console.log(`📊 图案API: http://localhost:${PORT}/api/patterns`);
  console.log('');
  console.log('✅ 现在可以刷新前端页面测试了！');
});