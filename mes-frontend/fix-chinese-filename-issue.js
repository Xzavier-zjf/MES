const fs = require('fs');
const path = require('path');

// 修复中文文件名问题的脚本
console.log('🔧 开始修复中文文件名问题...');

const uploadsDir = path.join(__dirname, '../MES-demo/uploads/patterns');

// 检查目录是否存在
if (!fs.existsSync(uploadsDir)) {
    console.error('❌ 上传目录不存在:', uploadsDir);
    process.exit(1);
}

// 读取目录中的所有文件
const files = fs.readdirSync(uploadsDir);
console.log('📁 找到文件:', files.length, '个');

// 查找包含中文字符的PNG文件
const chineseFiles = files.filter(file => {
    return /[\u4e00-\u9fa5]/.test(file) && file.endsWith('.png');
});

console.log('🔍 找到包含中文字符的PNG文件:', chineseFiles.length, '个');

chineseFiles.forEach(file => {
    console.log('📄 处理文件:', file);
    
    const originalPath = path.join(uploadsDir, file);
    
    // 检查文件是否存在
    if (!fs.existsSync(originalPath)) {
        console.warn('⚠️ 文件不存在:', originalPath);
        return;
    }
    
    // 读取文件内容
    const fileContent = fs.readFileSync(originalPath);
    
    // 创建对应的SVG版本（实际上是PNG内容，但扩展名为SVG）
    const svgFileName = file.replace('.png', '.svg');
    const svgPath = path.join(uploadsDir, svgFileName);
    
    // 写入SVG版本
    fs.writeFileSync(svgPath, fileContent);
    console.log('✅ 创建SVG版本:', svgFileName);
    
    // 创建URL编码版本的文件名
    const encodedFileName = encodeURIComponent(file);
    if (encodedFileName !== file) {
        const encodedPath = path.join(uploadsDir, encodedFileName);
        fs.writeFileSync(encodedPath, fileContent);
        console.log('✅ 创建URL编码版本:', encodedFileName);
        
        // 也创建编码版本的SVG
        const encodedSvgFileName = encodedFileName.replace('.png', '.svg');
        const encodedSvgPath = path.join(uploadsDir, encodedSvgFileName);
        fs.writeFileSync(encodedSvgPath, fileContent);
        console.log('✅ 创建URL编码SVG版本:', encodedSvgFileName);
    }
});

console.log('🎉 中文文件名问题修复完成！');

// 验证修复结果
console.log('\n📊 修复后的文件列表:');
const updatedFiles = fs.readdirSync(uploadsDir);
updatedFiles.forEach(file => {
    if (/[\u4e00-\u9fa5]/.test(file) || file.includes('%')) {
        console.log('  📄', file);
    }
});

console.log('\n✅ 现在系统应该能够正确访问包含中文字符的图片文件了！');