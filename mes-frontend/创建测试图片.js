import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

// 获取当前文件的目录路径（ES模块中的__dirname替代）
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 创建上传目录
const uploadDir = path.join(__dirname, 'public', 'uploads', 'patterns');

// 确保目录存在
function ensureDirectoryExists(dirPath) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
    console.log(`✅ 创建目录: ${dirPath}`);
  }
}

// 创建1x1像素的测试图片
function createTestImage(filename, color = '#cccccc') {
  const svg = `<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg">
    <rect width="100" height="100" fill="${color}"/>
    <text x="50" y="50" text-anchor="middle" dy=".3em" font-family="Arial" font-size="12" fill="white">测试图片</text>
  </svg>`;
  
  return Buffer.from(svg);
}

// 需要创建的测试图片列表
const testImages = [
  'test-pattern-1.jpg',
  'test-pattern-2.jpg',
  'deeaa61e-3742-40dc-9e41-3507caf50e5e_R-C.jpg',
  '819aad2c-5506-4a54-9288-1836982876ed_2126.png_860.png',
  '4106e9db-40d5-4922-96d8-3240a6c99e3f_R-C (1).jpg',
  'geometric-pattern.jpg',
  'marble-texture.jpg',
  'chinese-cloud.png'
];

console.log('🎨 开始创建测试图片...');

// 创建目录
ensureDirectoryExists(uploadDir);

// 创建测试图片
testImages.forEach((filename, index) => {
  const colors = ['#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57', '#ff9ff3', '#54a0ff', '#5f27cd'];
  const color = colors[index % colors.length];
  
  const imagePath = path.join(uploadDir, filename);
  const imageBuffer = createTestImage(filename, color);
  
  fs.writeFileSync(imagePath, imageBuffer);
  console.log(`✅ 创建图片: ${filename}`);
});

console.log('');
console.log('🎉 测试图片创建完成！');
console.log(`📁 图片目录: ${uploadDir}`);
console.log('');
console.log('💡 现在可以启动静态文件服务器来提供图片访问');