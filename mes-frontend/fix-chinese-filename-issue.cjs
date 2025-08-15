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

// 查找包含中文字符的文件
const chineseFiles = files.filter(file => {
    return /[\u4e00-\u9fa5]/.test(file) && (file.endsWith('.png') || file.endsWith('.jpg'));
});

console.log('🔍 找到包含中文字符的文件:', chineseFiles.length, '个');
chineseFiles.forEach(file => console.log('  📄', file));

chineseFiles.forEach(file => {
    console.log('\n📄 处理文件:', file);
    
    const originalPath = path.join(uploadsDir, file);
    
    // 检查文件是否存在
    if (!fs.existsSync(originalPath)) {
        console.warn('⚠️ 文件不存在:', originalPath);
        return;
    }
    
    // 读取文件内容
    const fileContent = fs.readFileSync(originalPath);
    console.log('✅ 读取文件成功，大小:', fileContent.length, 'bytes');
    
    // 获取文件扩展名
    const ext = path.extname(file);
    const baseName = path.basename(file, ext);
    
    // 创建对应的SVG版本（实际上是原文件内容，但扩展名为SVG）
    const svgFileName = baseName + '.svg';
    const svgPath = path.join(uploadsDir, svgFileName);
    
    if (!fs.existsSync(svgPath)) {
        fs.writeFileSync(svgPath, fileContent);
        console.log('✅ 创建SVG版本:', svgFileName);
    } else {
        console.log('ℹ️ SVG版本已存在:', svgFileName);
    }
    
    // 创建URL编码版本的文件名
    const encodedFileName = encodeURIComponent(file);
    if (encodedFileName !== file) {
        console.log('🔄 创建URL编码版本:', file, '->', encodedFileName);
        
        const encodedPath = path.join(uploadsDir, encodedFileName);
        if (!fs.existsSync(encodedPath)) {
            fs.writeFileSync(encodedPath, fileContent);
            console.log('✅ 创建URL编码版本:', encodedFileName);
        } else {
            console.log('ℹ️ URL编码版本已存在:', encodedFileName);
        }
        
        // 也创建编码版本的SVG
        const encodedSvgFileName = encodedFileName.replace(ext, '.svg');
        const encodedSvgPath = path.join(uploadsDir, encodedSvgFileName);
        if (!fs.existsSync(encodedSvgPath)) {
            fs.writeFileSync(encodedSvgPath, fileContent);
            console.log('✅ 创建URL编码SVG版本:', encodedSvgFileName);
        } else {
            console.log('ℹ️ URL编码SVG版本已存在:', encodedSvgFileName);
        }
    }
});

console.log('\n🎉 中文文件名问题修复完成！');

// 验证修复结果
console.log('\n📊 修复后的文件列表:');
const updatedFiles = fs.readdirSync(uploadsDir);
const relevantFiles = updatedFiles.filter(file => 
    /[\u4e00-\u9fa5]/.test(file) || file.includes('%') || file.includes('b86dc126')
);

relevantFiles.forEach(file => {
    const filePath = path.join(uploadsDir, file);
    const stats = fs.statSync(filePath);
    console.log(`  📄 ${file} (${stats.size} bytes)`);
});

console.log('\n✅ 现在系统应该能够正确访问包含中文字符的图片文件了！');

// 特别检查问题文件
const problemFile = 'b86dc126-f32f-49b8-ba83-52b60b6af368_【哲风壁纸】创意-动漫-动漫壁纸.png';
const problemFilePath = path.join(uploadsDir, problemFile);
if (fs.existsSync(problemFilePath)) {
    console.log('\n🎯 问题文件检查:');
    console.log('✅ 原始文件存在:', problemFile);
    
    const svgVersion = problemFile.replace('.png', '.svg');
    const svgPath = path.join(uploadsDir, svgVersion);
    console.log('✅ SVG版本:', fs.existsSync(svgPath) ? '存在' : '不存在');
    
    const encodedVersion = encodeURIComponent(problemFile);
    const encodedPath = path.join(uploadsDir, encodedVersion);
    console.log('✅ URL编码版本:', fs.existsSync(encodedPath) ? '存在' : '不存在');
    
    const encodedSvgVersion = encodedVersion.replace('.png', '.svg');
    const encodedSvgPath = path.join(uploadsDir, encodedSvgVersion);
    console.log('✅ URL编码SVG版本:', fs.existsSync(encodedSvgPath) ? '存在' : '不存在');
}