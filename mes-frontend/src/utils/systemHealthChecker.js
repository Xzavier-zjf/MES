/**
 * 系统健康检查器
 * 综合检查和修复系统中的各种问题
 */

import { dataSyncManager } from './dataSyncManager'
import { taskStatusManager } from './taskStatusManager'
import { validateInjectionQuantityLogic, validatePatternQuantityLogic } from './quantityLogicValidator'
import { checkPlanTaskConsistency } from './dataConsistencyCheck'
import { ElNotification, ElMessage } from 'element-plus'

class SystemHealthChecker {
  constructor() {
    this.checkResults = new Map()
    this.fixHistory = []
    this.isChecking = false
  }

  /**
   * 执行全面的系统健康检查
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async performFullHealthCheck(systemData) {
    if (this.isChecking) {
      ElMessage.warning('系统检查正在进行中，请稍候...')
      return null
    }

    this.isChecking = true
    const startTime = Date.now()

    try {
      ElNotification({
        title: '系统健康检查',
        message: '正在执行全面的系统健康检查...',
        type: 'info',
        duration: 3000
      })

      const results = {
        timestamp: new Date().toISOString(),
        duration: 0,
        overallHealth: 'unknown',
        checks: {},
        issues: [],
        warnings: [],
        suggestions: []
      }

      // 1. 数据一致性检查
      results.checks.dataConsistency = await this.checkDataConsistency(systemData)
      
      // 2. 任务状态检查
      results.checks.taskStatus = await this.checkTaskStatus(systemData)
      
      // 3. 数量逻辑检查
      results.checks.quantityLogic = await this.checkQuantityLogic(systemData)
      
      // 4. 计划任务一致性检查
      results.checks.planTaskConsistency = await this.checkPlanTaskConsistency(systemData)
      
      // 5. 权限配置检查
      results.checks.permissions = await this.checkPermissions(systemData)

      // 汇总结果
      this.summarizeResults(results)
      
      results.duration = Date.now() - startTime
      this.checkResults.set('latest', results)

      // 显示检查结果
      this.displayHealthCheckResults(results)

      return results
    } catch (error) {
      console.error('系统健康检查失败:', error)
      ElMessage.error('系统健康检查失败: ' + error.message)
      return null
    } finally {
      this.isChecking = false
    }
  }

  /**
   * 检查数据一致性
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async checkDataConsistency(systemData) {
    const { tasks = [], injectionParams = [] } = systemData
    
    const result = dataSyncManager.checkDataConsistency(tasks, injectionParams)
    
    return {
      name: '数据一致性检查',
      status: result.isConsistent ? 'pass' : 'fail',
      issues: result.inconsistencies.length,
      details: result,
      severity: result.summary.seriousInconsistencies > 0 ? 'critical' : 
                result.inconsistencies.length > 0 ? 'warning' : 'info'
    }
  }

  /**
   * 检查任务状态
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async checkTaskStatus(systemData) {
    const { tasks = [] } = systemData
    
    const suggestions = taskStatusManager.batchCheckStatus(tasks)
    const highPrioritySuggestions = suggestions.filter(s => s.confidence >= 90)
    
    return {
      name: '任务状态检查',
      status: suggestions.length === 0 ? 'pass' : 'warning',
      issues: suggestions.length,
      details: {
        totalSuggestions: suggestions.length,
        highPriority: highPrioritySuggestions.length,
        suggestions
      },
      severity: highPrioritySuggestions.length > 0 ? 'warning' : 'info'
    }
  }

  /**
   * 检查数量逻辑
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async checkQuantityLogic(systemData) {
    const { tasks = [], injectionParams = [], patterns = [], plans = [] } = systemData
    
    const injectionResult = validateInjectionQuantityLogic(injectionParams, tasks, plans)
    const patternResult = validatePatternQuantityLogic(patterns, tasks, plans)
    
    const totalIssues = injectionResult.issues.length + patternResult.issues.length
    const totalWarnings = injectionResult.warnings.length + patternResult.warnings.length
    
    return {
      name: '数量逻辑检查',
      status: totalIssues === 0 ? (totalWarnings === 0 ? 'pass' : 'warning') : 'fail',
      issues: totalIssues,
      warnings: totalWarnings,
      details: {
        injection: injectionResult,
        pattern: patternResult
      },
      severity: totalIssues > 0 ? 'critical' : totalWarnings > 0 ? 'warning' : 'info'
    }
  }

  /**
   * 检查计划任务一致性
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async checkPlanTaskConsistency(systemData) {
    const { plans = [], tasks = [] } = systemData
    
    const result = checkPlanTaskConsistency(plans, tasks)
    
    return {
      name: '计划任务一致性检查',
      status: result.isConsistent ? 'pass' : 'fail',
      issues: result.inconsistencies.length,
      warnings: result.warnings.length,
      details: result,
      severity: result.inconsistencies.length > 0 ? 'critical' : 
                result.warnings.length > 0 ? 'warning' : 'info'
    }
  }

  /**
   * 检查权限配置
   * @param {Object} systemData 系统数据
   * @returns {Object} 检查结果
   */
  async checkPermissions(systemData) {
    // 这里可以添加权限配置检查逻辑
    // 暂时返回通过状态
    return {
      name: '权限配置检查',
      status: 'pass',
      issues: 0,
      details: {
        message: '权限配置检查功能待实现'
      },
      severity: 'info'
    }
  }

  /**
   * 汇总检查结果
   * @param {Object} results 检查结果
   */
  summarizeResults(results) {
    const checks = Object.values(results.checks)
    const criticalIssues = checks.filter(c => c.severity === 'critical').length
    const warnings = checks.filter(c => c.severity === 'warning').length
    const passed = checks.filter(c => c.status === 'pass').length
    
    // 计算总体健康状态
    if (criticalIssues > 0) {
      results.overallHealth = 'critical'
    } else if (warnings > 0) {
      results.overallHealth = 'warning'
    } else {
      results.overallHealth = 'healthy'
    }

    // 收集所有问题和警告
    checks.forEach(check => {
      if (check.severity === 'critical') {
        results.issues.push({
          check: check.name,
          count: check.issues || 0,
          details: check.details
        })
      } else if (check.severity === 'warning') {
        results.warnings.push({
          check: check.name,
          count: check.issues || check.warnings || 0,
          details: check.details
        })
      }
    })

    // 生成修复建议
    results.suggestions = this.generateFixSuggestions(results)
  }

  /**
   * 生成修复建议
   * @param {Object} results 检查结果
   * @returns {Array} 修复建议列表
   */
  generateFixSuggestions(results) {
    const suggestions = []

    results.issues.forEach(issue => {
      switch (issue.check) {
        case '数据一致性检查':
          suggestions.push({
            priority: 'high',
            category: 'data',
            title: '修复数据不一致问题',
            description: `发现 ${issue.count} 项数据不一致，建议使用自动修复功能`,
            action: 'autoFixDataInconsistency'
          })
          break
        case '数量逻辑检查':
          suggestions.push({
            priority: 'high',
            category: 'logic',
            title: '修复数量逻辑错误',
            description: `发现 ${issue.count} 项数量逻辑错误，需要手动检查和修复`,
            action: 'manualFixQuantityLogic'
          })
          break
        case '计划任务一致性检查':
          suggestions.push({
            priority: 'high',
            category: 'consistency',
            title: '修复计划任务不一致',
            description: `发现 ${issue.count} 项计划任务不一致，建议检查计划状态`,
            action: 'fixPlanTaskConsistency'
          })
          break
      }
    })

    results.warnings.forEach(warning => {
      switch (warning.check) {
        case '任务状态检查':
          suggestions.push({
            priority: 'medium',
            category: 'status',
            title: '更新任务状态',
            description: `发现 ${warning.count} 个任务状态需要更新`,
            action: 'updateTaskStatus'
          })
          break
      }
    })

    return suggestions.sort((a, b) => {
      const priorityOrder = { high: 3, medium: 2, low: 1 }
      return priorityOrder[b.priority] - priorityOrder[a.priority]
    })
  }

  /**
   * 显示健康检查结果
   * @param {Object} results 检查结果
   */
  displayHealthCheckResults(results) {
    const { overallHealth, checks } = results
    const totalChecks = Object.keys(checks).length
    const passedChecks = Object.values(checks).filter(c => c.status === 'pass').length
    
    let notificationType = 'success'
    let title = '系统健康检查完成'
    let message = `所有 ${totalChecks} 项检查均通过`

    if (overallHealth === 'critical') {
      notificationType = 'error'
      title = '发现严重问题'
      message = `${totalChecks} 项检查中发现严重问题，需要立即处理`
    } else if (overallHealth === 'warning') {
      notificationType = 'warning'
      title = '发现警告问题'
      message = `${passedChecks}/${totalChecks} 项检查通过，发现一些需要关注的问题`
    }

    ElNotification({
      title,
      message,
      type: notificationType,
      duration: 5000
    })

    // 输出详细报告到控制台
    console.group('系统健康检查报告')
    console.log('总体健康状态:', overallHealth)
    console.log('检查耗时:', results.duration + 'ms')
    console.log('检查结果:', checks)
    console.log('修复建议:', results.suggestions)
    console.groupEnd()
  }

  /**
   * 执行自动修复
   * @param {Object} systemData 系统数据
   * @param {Array} suggestions 修复建议
   * @returns {Object} 修复结果
   */
  async performAutoFix(systemData, suggestions) {
    const fixResults = {
      timestamp: new Date().toISOString(),
      attempted: 0,
      successful: 0,
      failed: 0,
      details: []
    }

    for (const suggestion of suggestions) {
      if (suggestion.action === 'autoFixDataInconsistency') {
        try {
          fixResults.attempted++
          
          const { tasks = [], injectionParams = [] } = systemData
          const inconsistencies = dataSyncManager.checkDataConsistency(tasks, injectionParams).inconsistencies
          
          const results = await dataSyncManager.autoFixInconsistencies(inconsistencies, 'task-priority')
          const successCount = results.filter(r => r.success).length
          
          fixResults.successful += successCount
          fixResults.failed += results.length - successCount
          
          fixResults.details.push({
            suggestion: suggestion.title,
            action: suggestion.action,
            success: successCount > 0,
            details: results
          })
        } catch (error) {
          fixResults.failed++
          fixResults.details.push({
            suggestion: suggestion.title,
            action: suggestion.action,
            success: false,
            error: error.message
          })
        }
      }
    }

    this.fixHistory.push(fixResults)
    return fixResults
  }

  /**
   * 获取系统健康评分
   * @param {Object} results 检查结果
   * @returns {number} 健康评分 (0-100)
   */
  calculateHealthScore(results) {
    if (!results || !results.checks) {
      return 0
    }

    const checks = Object.values(results.checks)
    let score = 100
    
    checks.forEach(check => {
      switch (check.severity) {
        case 'critical':
          score -= 20
          break
        case 'warning':
          score -= 10
          break
        case 'info':
          score -= 2
          break
      }
    })

    return Math.max(0, score)
  }

  /**
   * 生成健康报告
   * @param {Object} results 检查结果
   * @returns {string} 报告文本
   */
  generateHealthReport(results) {
    const score = this.calculateHealthScore(results)
    
    let report = `系统健康检查报告\n`
    report += `=====================\n`
    report += `检查时间: ${new Date(results.timestamp).toLocaleString()}\n`
    report += `检查耗时: ${results.duration}ms\n`
    report += `健康评分: ${score}/100\n`
    report += `总体状态: ${results.overallHealth}\n\n`

    report += `检查项目:\n`
    Object.values(results.checks).forEach((check, index) => {
      report += `${index + 1}. ${check.name}: ${check.status.toUpperCase()}\n`
      if (check.issues > 0) {
        report += `   问题数量: ${check.issues}\n`
      }
      if (check.warnings > 0) {
        report += `   警告数量: ${check.warnings}\n`
      }
    })

    if (results.suggestions.length > 0) {
      report += `\n修复建议:\n`
      results.suggestions.forEach((suggestion, index) => {
        report += `${index + 1}. [${suggestion.priority.toUpperCase()}] ${suggestion.title}\n`
        report += `   ${suggestion.description}\n`
      })
    }

    return report
  }

  /**
   * 获取最新的检查结果
   * @returns {Object|null} 最新的检查结果
   */
  getLatestResults() {
    return this.checkResults.get('latest') || null
  }

  /**
   * 获取修复历史
   * @returns {Array} 修复历史记录
   */
  getFixHistory() {
    return [...this.fixHistory]
  }
}

// 创建全局实例
export const systemHealthChecker = new SystemHealthChecker()

// 导出类供其他地方使用
export default SystemHealthChecker