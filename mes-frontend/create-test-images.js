/**
 * 创建测试图片的脚本
 * 生成一些简单的测试图片用于验证图片访问功能
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 目标目录
const UPLOAD_DIR = path.join(__dirname, '../MES-demo/services/service-process/uploads/patterns');

// 确保目录存在
if (!fs.existsSync(UPLOAD_DIR)) {
    fs.mkdirSync(UPLOAD_DIR, { recursive: true });
    console.log('创建目录:', UPLOAD_DIR);
}

// 创建简单的SVG测试图片
function createSVGImage(filename, width, height, color, text) {
    const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
    <rect width="100%" height="100%" fill="${color}"/>
    <text x="50%" y="50%" font-family="Arial, sans-serif" font-size="16" 
          text-anchor="middle" dominant-baseline="middle" fill="white">
        ${text}
    </text>
</svg>`;
    
    const filePath = path.join(UPLOAD_DIR, filename);
    fs.writeFileSync(filePath, svg, 'utf8');
    console.log('创建测试图片:', filename);
}

// 创建HTML格式的测试图片（可以被浏览器显示）
function createHTMLImage(filename, color, text) {
    const html = `<!DOCTYPE html>
<html>
<head>
    <style>
        body {
            margin: 0;
            width: 200px;
            height: 200px;
            background: ${color};
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: Arial, sans-serif;
            color: white;
            font-size: 16px;
        }
    </style>
</head>
<body>
    <div>${text}</div>
</body>
</html>`;
    
    const filePath = path.join(UPLOAD_DIR, filename);
    fs.writeFileSync(filePath, html, 'utf8');
    console.log('创建HTML测试图片:', filename);
}

// 创建简单的文本文件作为图片占位符
function createTextPlaceholder(filename, text) {
    const content = `测试图片占位符
文件名: ${filename}
内容: ${text}
创建时间: ${new Date().toISOString()}

这是一个用于测试图片访问功能的占位符文件。
在实际应用中，这里应该是真正的图片文件。`;
    
    const filePath = path.join(UPLOAD_DIR, filename);
    fs.writeFileSync(filePath, content, 'utf8');
    console.log('创建文本占位符:', filename);
}

// 创建测试图片
console.log('开始创建测试图片...');

// SVG 图片
createSVGImage('test-pattern-1.svg', 200, 200, '#ff6b6b', '测试图案1');
createSVGImage('test-pattern-2.svg', 200, 200, '#4ecdc4', '测试图案2');
createSVGImage('geometric-pattern.svg', 200, 200, '#45b7d1', '几何图案');
createSVGImage('marble-texture.svg', 200, 200, '#96ceb4', '大理石纹理');

// HTML 图片（可以在浏览器中显示）
createHTMLImage('test-pattern-1.html', '#ff6b6b', '测试图案1');
createHTMLImage('test-pattern-2.html', '#4ecdc4', '测试图案2');

// 文本占位符（用于测试不同文件类型）
createTextPlaceholder('test-pattern-1.jpg', '红色测试图案');
createTextPlaceholder('test-pattern-2.png', '蓝色测试图案');
createTextPlaceholder('geometric-pattern.jpg', '几何图案');
createTextPlaceholder('marble-texture.jpg', '大理石纹理');

// 创建一些带特殊字符的文件名（测试URL编码）
createTextPlaceholder('4106e9db-40d5-4922-96d8-3240a6c99e3f_R-C (1).jpg', 'UUID图案1');
createTextPlaceholder('deeaa61e-3742-40dc-9e41-3507caf50e5e_R-C.jpg', 'UUID图案2');
createTextPlaceholder('819aad2c-5506-4a54-9288-1836982876ed_2126.png_860.png', 'UUID图案3');

console.log('测试图片创建完成！');
console.log('目录:', UPLOAD_DIR);

// 列出创建的文件
const files = fs.readdirSync(UPLOAD_DIR);
console.log('\n创建的文件列表:');
files.forEach(file => {
    const filePath = path.join(UPLOAD_DIR, file);
    const stats = fs.statSync(filePath);
    console.log(`- ${file} (${stats.size} bytes)`);
});

console.log('\n可以使用以下命令启动测试:');
console.log('1. 启动后端服务');
console.log('2. 打开 test-image-access.html 进行测试');