import fs from 'fs';
import path from 'path';
import net from 'net';
import express from 'express';
import { fileURLToPath } from 'url';

// 获取当前文件的目录路径（ES模块中的__dirname替代）
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

console.log('🔍 MES系统问题诊断');
console.log('========================================');

// 检查Node.js版本
console.log('📋 环境检查:');
console.log('Node.js版本:', process.version);
console.log('当前目录:', process.cwd());

// 检查文件存在性
console.log('\n📁 文件检查:');
const files = [
    'package.json',
    'mock-server.js',
    '创建测试图片.js',
    'node_modules/express/package.json'
];

files.forEach(file => {
    if (fs.existsSync(file)) {
        console.log(`✅ ${file} - 存在`);
    } else {
        console.log(`❌ ${file} - 不存在`);
    }
});

// 检查端口占用
console.log('\n🔌 端口检查:');

function checkPort(port) {
    return new Promise((resolve) => {
        const server = net.createServer();
        server.listen(port, () => {
            server.once('close', () => {
                resolve(false); // 端口可用
            });
            server.close();
        });
        server.on('error', () => {
            resolve(true); // 端口被占用
        });
    });
}

async function checkPorts() {
    const port8080 = await checkPort(8080);
    const port5173 = await checkPort(5173);

    console.log(`端口8080 (后端): ${port8080 ? '被占用' : '可用'}`);
    console.log(`端口5173 (前端): ${port5173 ? '被占用' : '可用'}`);

    // 尝试启动简单的HTTP服务器测试
    console.log('\n🧪 启动测试服务器...');

    const app = express();

    app.get('/test', (req, res) => {
        res.json({ status: 'OK', message: '测试成功' });
    });

    const server = app.listen(8081, () => {
        console.log('✅ 测试服务器启动成功: http://localhost:8081/test');
        console.log('请在浏览器中访问上述地址验证');

        setTimeout(() => {
            server.close();
            console.log('测试服务器已关闭');
        }, 10000);
    });
}

checkPorts().catch(console.error);