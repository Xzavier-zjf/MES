/**
 * 创建真正的图片文件
 * 使用Canvas生成简单的测试图片
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 目标目录
const UPLOAD_DIR = path.join(__dirname, '../MES-demo/uploads/patterns');

// 确保目录存在
if (!fs.existsSync(UPLOAD_DIR)) {
    fs.mkdirSync(UPLOAD_DIR, { recursive: true });
    console.log('创建目录:', UPLOAD_DIR);
}

// 创建SVG图片（浏览器可以直接显示）
function createSVGImage(filename, width, height, color, text) {
    const svg = `<?xml version="1.0" encoding="UTF-8"?>
<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
    <rect width="100%" height="100%" fill="${color}"/>
    <circle cx="${width/2}" cy="${height/2}" r="${Math.min(width, height)/4}" fill="rgba(255,255,255,0.3)"/>
    <text x="50%" y="45%" font-family="Arial, sans-serif" font-size="16" 
          text-anchor="middle" dominant-baseline="middle" fill="white" font-weight="bold">
        ${text}
    </text>
    <text x="50%" y="60%" font-family="Arial, sans-serif" font-size="12" 
          text-anchor="middle" dominant-baseline="middle" fill="white">
        ${filename.split('.')[0]}
    </text>
</svg>`;
    
    const filePath = path.join(UPLOAD_DIR, filename);
    fs.writeFileSync(filePath, svg, 'utf8');
    console.log('创建SVG图片:', filename);
}

// 创建Base64编码的PNG图片
function createBase64PNG(filename, color, text) {
    // 这是一个简单的1x1像素PNG图片的Base64编码
    // 实际应用中应该使用更复杂的图片生成库
    const base64PNG = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77zgAAAABJRU5ErkJggg==';
    
    // 创建一个简单的HTML文件，包含内联的SVG作为图片
    const htmlContent = `<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { margin: 0; padding: 0; }
        .image-container {
            width: 200px;
            height: 200px;
            background: ${color};
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            font-family: Arial, sans-serif;
            color: white;
        }
        .title { font-size: 16px; font-weight: bold; margin-bottom: 10px; }
        .subtitle { font-size: 12px; }
    </style>
</head>
<body>
    <div class="image-container">
        <div class="title">${text}</div>
        <div class="subtitle">${filename}</div>
    </div>
</body>
</html>`;
    
    const filePath = path.join(UPLOAD_DIR, filename);
    fs.writeFileSync(filePath, htmlContent, 'utf8');
    console.log('创建HTML图片:', filename);
}

// 图片配置
const imageConfigs = [
    { filename: 'test-pattern-1.jpg', color: '#ff6b6b', text: '测试图案1' },
    { filename: 'test-pattern-2.png', color: '#4ecdc4', text: '测试图案2' },
    { filename: 'geometric-pattern.jpg', color: '#45b7d1', text: '几何图案' },
    { filename: 'marble-texture.jpg', color: '#96ceb4', text: '大理石纹理' },
    { filename: '4106e9db-40d5-4922-96d8-3240a6c99e3f_R-C (1).jpg', color: '#f39c12', text: '问题图案1' },
    { filename: 'deeaa61e-3742-40dc-9e41-3507caf50e5e_R-C.jpg', color: '#9b59b6', text: '问题图案2' },
    { filename: '819aad2c-5506-4a54-9288-1836982876ed_2126.png_860.png', color: '#e74c3c', text: '问题图案3' }
];

console.log('开始创建真正的图片文件...');

// 创建SVG版本（推荐，因为浏览器原生支持）
imageConfigs.forEach(config => {
    const svgFilename = config.filename.replace(/\.(jpg|png)$/, '.svg');
    createSVGImage(svgFilename, 200, 200, config.color, config.text);
});

// 也创建原始文件名的版本（用HTML模拟）
imageConfigs.forEach(config => {
    createBase64PNG(config.filename, config.color, config.text);
});

console.log('图片文件创建完成！');
console.log('目录:', UPLOAD_DIR);

// 列出创建的文件
const files = fs.readdirSync(UPLOAD_DIR);
console.log('\n创建的文件列表:');
files.forEach(file => {
    const filePath = path.join(UPLOAD_DIR, file);
    const stats = fs.statSync(filePath);
    console.log(`- ${file} (${stats.size} bytes)`);
});

console.log('\n注意：');
console.log('1. SVG文件可以直接在浏览器中显示为图片');
console.log('2. 原始文件名的版本使用HTML模拟，可能需要特殊处理');
console.log('3. 建议更新PatternManager.vue中的预定义路径使用.svg版本');