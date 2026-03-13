/**
 * 清理测试文件脚本
 * 删除对项目启动运行无关影响的测试页面
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 需要保留的文件（对项目运行有用或重要的）
const keepFiles = [
    // 核心配置文件
    'package.json',
    'package-lock.json',
    'vite.config.js',
    'index.html',
    '.env',
    '.env.production',
    '.gitignore',
    
    // 启动脚本（重要）
    '启动前端.bat',
    '启动后端.bat',
    '完整系统启动.bat',
    '完美启动.bat',
    '快速启动.bat',
    
    // 系统诊断工具（有用）
    '系统诊断.bat',
    '检查环境.cmd',
    
    // 核心工具脚本
    'mock-server.js',
    'simple-server.mjs',
    
    // 图片创建脚本（重要）
    'create-test-images.js',
    'create-real-images.js',
    'create-real-png-images.js',
    '创建测试图片.js',
    
    // 重要的测试和修复工具
    'verify-fix.html',
    'test-smart-switching-final.html',
    'test-table-preview-fix.html'
];

// 需要删除的测试文件（对项目启动运行无关）
const deleteFiles = [
    // 调试页面
    'debug-data-consistency.html',
    'debug-data-sync.html',
    'debug-injection-api.html',
    'debug-login.html',
    'debug-path-selection-live.html',
    'debug-pattern-update.html',
    'debug-progress-display.html',
    
    // 修复工具页面（已完成修复，不再需要）
    'fix-cache-issue.html',
    'fix-jpg-display-issue.html',
    'fix-jpg-svg-display.html',
    'fix-path-selection-issue.html',
    'fix-pattern-image-preview.html',
    'fix-system-issues.html',
    
    // 测试页面（功能已验证，不再需要）
    'test-backend-debug.html',
    'test-backend-status.html',
    'test-content-type-fix.html',
    'test-cors-fix.html',
    'test-data-consistency-simple.html',
    'test-data-persistence.html',
    'test-direct-access.html',
    'test-elmessage-fix.html',
    'test-error-fixes.html',
    'test-file-upload.html',
    'test-fix.html',
    'test-fixed-update.html',
    'test-image-access.html',
    'test-image-display-simple.html',
    'test-image-update.html',
    'test-image-upload.html',
    'test-injection-simple.html',
    'test-injection-update.html',
    'test-jpg-fix-final.html',
    'test-path-selection-debug.html',
    'test-pattern-file-upload.html',
    'test-pattern-image-update.html',
    'test-pattern-image.html',
    'test-pattern-preview-fix.html',
    'test-progress-sync.html',
    'test-simplified-pattern.html',
    'test-status-update.html',
    'test-task-quantity-update.html',
    'test-url-preview-fix.html',
    'test-url-preview.html',
    'test-url-update-fix.html',
    'test-user-status.html',
    
    // 其他测试文件
    'final-test-cors-fix.html',
    'final-path-selection-test.html',
    'microservice-debug.html',
    'service-status-check.html',
    
    // 临时文件
    'test-pattern-image-replacement.js',
    '纯前端解决方案.html',
    '诊断问题.js'
];

console.log('开始清理测试文件...');
console.log('===================');

let deletedCount = 0;
let keptCount = 0;
let notFoundCount = 0;

// 删除不需要的测试文件
deleteFiles.forEach(filename => {
    const filePath = path.join(__dirname, filename);
    
    if (fs.existsSync(filePath)) {
        try {
            fs.unlinkSync(filePath);
            console.log(`✅ 已删除: ${filename}`);
            deletedCount++;
        } catch (error) {
            console.log(`❌ 删除失败: ${filename} - ${error.message}`);
        }
    } else {
        console.log(`⚠️ 文件不存在: ${filename}`);
        notFoundCount++;
    }
});

console.log('');
console.log('清理统计:');
console.log(`- 删除文件: ${deletedCount} 个`);
console.log(`- 文件不存在: ${notFoundCount} 个`);

// 列出保留的重要文件
console.log('');
console.log('保留的重要文件:');
console.log('================');

const allFiles = fs.readdirSync(__dirname);
const htmlFiles = allFiles.filter(file => file.endsWith('.html'));
const jsFiles = allFiles.filter(file => file.endsWith('.js') && !file.includes('node_modules'));
const batFiles = allFiles.filter(file => file.endsWith('.bat'));

console.log('');
console.log('保留的HTML文件:');
htmlFiles.forEach(file => {
    if (keepFiles.includes(file) || !deleteFiles.includes(file)) {
        console.log(`  - ${file}`);
        keptCount++;
    }
});

console.log('');
console.log('保留的JS工具文件:');
jsFiles.forEach(file => {
    if (keepFiles.includes(file)) {
        console.log(`  - ${file}`);
    }
});

console.log('');
console.log('保留的启动脚本:');
batFiles.forEach(file => {
    if (keepFiles.includes(file)) {
        console.log(`  - ${file}`);
    }
});

console.log('');
console.log('清理完成！');
console.log(`总计删除 ${deletedCount} 个测试文件`);
console.log('项目现在更加整洁，启动运行不受影响。');

// 创建清理报告
const reportContent = `# 测试文件清理报告

## 清理时间
${new Date().toLocaleString()}

## 清理统计
- 删除文件: ${deletedCount} 个
- 保留文件: ${keptCount} 个
- 文件不存在: ${notFoundCount} 个

## 删除的文件类别

### 调试页面 (${deleteFiles.filter(f => f.startsWith('debug-')).length} 个)
${deleteFiles.filter(f => f.startsWith('debug-')).map(f => `- ${f}`).join('\n')}

### 修复工具页面 (${deleteFiles.filter(f => f.startsWith('fix-')).length} 个)
${deleteFiles.filter(f => f.startsWith('fix-')).map(f => `- ${f}`).join('\n')}

### 测试页面 (${deleteFiles.filter(f => f.startsWith('test-')).length} 个)
${deleteFiles.filter(f => f.startsWith('test-')).map(f => `- ${f}`).join('\n')}

### 其他临时文件
${deleteFiles.filter(f => !f.startsWith('debug-') && !f.startsWith('fix-') && !f.startsWith('test-')).map(f => `- ${f}`).join('\n')}

## 保留的重要文件

### 启动脚本
${keepFiles.filter(f => f.endsWith('.bat')).map(f => `- ${f}`).join('\n')}

### 工具脚本
${keepFiles.filter(f => f.endsWith('.js') && f.includes('create')).map(f => `- ${f}`).join('\n')}

### 核心测试工具
- verify-fix.html - 修复验证工具
- test-smart-switching-final.html - 智能切换测试
- test-table-preview-fix.html - 表格预览修复测试

## 清理效果
- ✅ 项目目录更加整洁
- ✅ 启动运行不受影响
- ✅ 保留了重要的工具和脚本
- ✅ 删除了临时的调试和测试文件
`;

fs.writeFileSync(path.join(__dirname, '../修复方案/测试文件清理报告.md'), reportContent, 'utf8');
console.log('');
console.log('📄 清理报告已生成: 修复方案/测试文件清理报告.md');