/**
 * 数量逻辑验证工具
 * 用于验证各页面的计划任务数量逻辑是否正确
 */

/**
 * 验证注塑工艺参数页面的数量逻辑
 * @param {Array} injectionParams 注塑参数列表
 * @param {Array} tasks 任务列表
 * @param {Array} plans 计划列表
 * @returns {Object} 验证结果
 */
export function validateInjectionQuantityLogic(injectionParams, tasks, plans) {
  const issues = []
  const warnings = []
  
  // 检查1: 注塑参数是否对应有效的任务
  injectionParams.forEach(param => {
    if (param.taskId) {
      const relatedTask = tasks.find(t => t.id === param.taskId)
      if (!relatedTask) {
        issues.push({
          type: 'INVALID_TASK_REFERENCE',
          paramId: param.id,
          message: `注塑参数 ${param.id} 引用了不存在的任务 ${param.taskId}`
        })
      } else {
        // 检查任务是否为注塑工序
        if (relatedTask.processType !== '注塑') {
          warnings.push({
            type: 'WRONG_PROCESS_TYPE',
            paramId: param.id,
            taskId: param.taskId,
            message: `注塑参数 ${param.id} 关联的任务 ${param.taskId} 不是注塑工序 (实际: ${relatedTask.processType})`
          })
        }
        
        // 检查数量一致性
        if (param.quantity && relatedTask.quantity && param.quantity !== relatedTask.quantity) {
          warnings.push({
            type: 'QUANTITY_MISMATCH',
            paramId: param.id,
            taskId: param.taskId,
            message: `注塑参数数量 (${param.quantity}) 与任务数量 (${relatedTask.quantity}) 不一致`
          })
        }
      }
    }
  })
  
  // 检查2: 注塑任务是否都有对应的工艺参数
  const injectionTasks = tasks.filter(t => t.processType === '注塑')
  injectionTasks.forEach(task => {
    const hasParams = injectionParams.some(p => p.taskId === task.id)
    if (!hasParams) {
      warnings.push({
        type: 'MISSING_INJECTION_PARAMS',
        taskId: task.id,
        message: `注塑任务 ${task.taskCode || task.id} 缺少工艺参数配置`
      })
    }
  })
  
  return {
    isValid: issues.length === 0,
    issues,
    warnings,
    summary: {
      totalParams: injectionParams.length,
      validParams: injectionParams.filter(p => p.taskId && tasks.some(t => t.id === p.taskId)).length,
      injectionTasks: injectionTasks.length,
      tasksWithParams: injectionTasks.filter(t => injectionParams.some(p => p.taskId === t.id)).length
    }
  }
}

/**
 * 验证印刷图案管理页面的数量逻辑
 * @param {Array} patterns 图案列表
 * @param {Array} tasks 任务列表
 * @param {Array} plans 计划列表
 * @returns {Object} 验证结果
 */
export function validatePatternQuantityLogic(patterns, tasks, plans) {
  const issues = []
  const warnings = []
  
  // 检查1: 图案是否对应有效的任务
  patterns.forEach(pattern => {
    if (pattern.taskId) {
      const relatedTask = tasks.find(t => t.id === pattern.taskId)
      if (!relatedTask) {
        issues.push({
          type: 'INVALID_TASK_REFERENCE',
          patternId: pattern.id,
          message: `图案 ${pattern.patternCode} 引用了不存在的任务 ${pattern.taskId}`
        })
      } else {
        // 检查任务是否为印刷工序
        if (relatedTask.processType !== '印刷') {
          warnings.push({
            type: 'WRONG_PROCESS_TYPE',
            patternId: pattern.id,
            taskId: pattern.taskId,
            message: `图案 ${pattern.patternCode} 关联的任务 ${pattern.taskId} 不是印刷工序 (实际: ${relatedTask.processType})`
          })
        }
      }
    }
    
    // 检查计划关联
    if (pattern.planId) {
      const relatedPlan = plans.find(p => p.id === pattern.planId)
      if (!relatedPlan) {
        issues.push({
          type: 'INVALID_PLAN_REFERENCE',
          patternId: pattern.id,
          message: `图案 ${pattern.patternCode} 引用了不存在的计划 ${pattern.planId}`
        })
      }
    }
  })
  
  // 检查2: 印刷任务是否都有对应的图案
  const printTasks = tasks.filter(t => t.processType === '印刷')
  printTasks.forEach(task => {
    const hasPattern = patterns.some(p => p.taskId === task.id)
    if (!hasPattern) {
      warnings.push({
        type: 'MISSING_PATTERN',
        taskId: task.id,
        message: `印刷任务 ${task.taskCode || task.id} 缺少图案配置`
      })
    }
  })
  
  // 检查3: 图案配置完整性
  patterns.forEach(pattern => {
    const missingFields = []
    if (!pattern.patternCode) missingFields.push('图案编号')
    if (!pattern.patternName) missingFields.push('图案名称')
    if (!pattern.machineModel) missingFields.push('适用机型')
    if (!pattern.imageUrl) missingFields.push('图案图片')
    
    if (missingFields.length > 0) {
      warnings.push({
        type: 'INCOMPLETE_PATTERN',
        patternId: pattern.id,
        message: `图案 ${pattern.patternCode || pattern.id} 缺少必要信息: ${missingFields.join(', ')}`
      })
    }
  })
  
  return {
    isValid: issues.length === 0,
    issues,
    warnings,
    summary: {
      totalPatterns: patterns.length,
      validPatterns: patterns.filter(p => p.taskId && tasks.some(t => t.id === p.taskId)).length,
      printTasks: printTasks.length,
      tasksWithPatterns: printTasks.filter(t => patterns.some(p => p.taskId === t.id)).length,
      completePatterns: patterns.filter(p => 
        p.patternCode && p.patternName && p.machineModel && p.imageUrl
      ).length
    }
  }
}

/**
 * 验证页面统计数据的准确性
 * @param {Object} displayedStats 页面显示的统计数据
 * @param {Array} actualData 实际数据
 * @param {string} pageType 页面类型
 * @returns {Object} 验证结果
 */
export function validatePageStatistics(displayedStats, actualData, pageType) {
  const issues = []
  
  switch (pageType) {
    case 'injection':
      // 验证注塑参数页面统计
      const actualTotal = actualData.length
      const actualConfigured = actualData.filter(p => 
        p.pressure && p.injectionSpeed && p.holdTime && p.coolingTime && 
        p.moldTemperature && p.materialTemperature
      ).length
      
      if (displayedStats.total !== actualTotal) {
        issues.push({
          type: 'STAT_MISMATCH',
          field: 'total',
          displayed: displayedStats.total,
          actual: actualTotal,
          message: `总数统计不匹配: 显示 ${displayedStats.total}, 实际 ${actualTotal}`
        })
      }
      
      if (displayedStats.configured !== actualConfigured) {
        issues.push({
          type: 'STAT_MISMATCH',
          field: 'configured',
          displayed: displayedStats.configured,
          actual: actualConfigured,
          message: `已配置统计不匹配: 显示 ${displayedStats.configured}, 实际 ${actualConfigured}`
        })
      }
      break
      
    case 'pattern':
      // 验证图案管理页面统计
      const patternTotal = actualData.length
      const patternConfigured = actualData.filter(p => 
        p.patternCode && p.patternName && p.machineModel && 
        p.defaultPrintSpeed && p.defaultPressure && p.imageUrl
      ).length
      
      if (displayedStats.total !== patternTotal) {
        issues.push({
          type: 'STAT_MISMATCH',
          field: 'total',
          displayed: displayedStats.total,
          actual: patternTotal,
          message: `图案总数统计不匹配: 显示 ${displayedStats.total}, 实际 ${patternTotal}`
        })
      }
      
      if (displayedStats.configured !== patternConfigured) {
        issues.push({
          type: 'STAT_MISMATCH',
          field: 'configured',
          displayed: displayedStats.configured,
          actual: patternConfigured,
          message: `已配置图案统计不匹配: 显示 ${displayedStats.configured}, 实际 ${patternConfigured}`
        })
      }
      break
  }
  
  return {
    isValid: issues.length === 0,
    issues
  }
}

/**
 * 生成数量逻辑验证报告
 * @param {Object} injectionResult 注塑验证结果
 * @param {Object} patternResult 图案验证结果
 * @returns {string} 报告文本
 */
export function generateQuantityLogicReport(injectionResult, patternResult) {
  let report = `数量逻辑验证报告\n`
  report += `==================\n\n`
  
  // 注塑工艺参数页面
  report += `注塑工艺参数页面:\n`
  report += `- 总参数数: ${injectionResult.summary.totalParams}\n`
  report += `- 有效参数: ${injectionResult.summary.validParams}\n`
  report += `- 注塑任务数: ${injectionResult.summary.injectionTasks}\n`
  report += `- 已配置参数的任务: ${injectionResult.summary.tasksWithParams}\n`
  report += `- 验证状态: ${injectionResult.isValid ? '✅ 通过' : '❌ 失败'}\n`
  
  if (injectionResult.issues.length > 0) {
    report += `- 严重问题:\n`
    injectionResult.issues.forEach(issue => {
      report += `  • ${issue.message}\n`
    })
  }
  
  if (injectionResult.warnings.length > 0) {
    report += `- 警告信息:\n`
    injectionResult.warnings.forEach(warning => {
      report += `  • ${warning.message}\n`
    })
  }
  
  report += `\n`
  
  // 印刷图案管理页面
  report += `印刷图案管理页面:\n`
  report += `- 总图案数: ${patternResult.summary.totalPatterns}\n`
  report += `- 有效图案: ${patternResult.summary.validPatterns}\n`
  report += `- 印刷任务数: ${patternResult.summary.printTasks}\n`
  report += `- 已配置图案的任务: ${patternResult.summary.tasksWithPatterns}\n`
  report += `- 完整图案数: ${patternResult.summary.completePatterns}\n`
  report += `- 验证状态: ${patternResult.isValid ? '✅ 通过' : '❌ 失败'}\n`
  
  if (patternResult.issues.length > 0) {
    report += `- 严重问题:\n`
    patternResult.issues.forEach(issue => {
      report += `  • ${issue.message}\n`
    })
  }
  
  if (patternResult.warnings.length > 0) {
    report += `- 警告信息:\n`
    patternResult.warnings.forEach(warning => {
      report += `  • ${warning.message}\n`
    })
  }
  
  return report
}

/**
 * 获取修复建议
 * @param {Object} validationResult 验证结果
 * @param {string} pageType 页面类型
 * @returns {Array} 修复建议列表
 */
export function getFixSuggestions(validationResult, pageType) {
  const suggestions = []
  
  validationResult.issues.forEach(issue => {
    switch (issue.type) {
      case 'INVALID_TASK_REFERENCE':
        suggestions.push({
          priority: 'high',
          action: '删除无效引用或创建对应任务',
          description: `${issue.message}，建议检查数据完整性`
        })
        break
        
      case 'INVALID_PLAN_REFERENCE':
        suggestions.push({
          priority: 'high',
          action: '删除无效引用或创建对应计划',
          description: `${issue.message}，建议检查数据完整性`
        })
        break
        
      case 'STAT_MISMATCH':
        suggestions.push({
          priority: 'medium',
          action: '修复统计计算逻辑',
          description: `${issue.message}，建议检查computed函数`
        })
        break
    }
  })
  
  validationResult.warnings.forEach(warning => {
    switch (warning.type) {
      case 'WRONG_PROCESS_TYPE':
        suggestions.push({
          priority: 'medium',
          action: '检查工序类型匹配',
          description: `${warning.message}，建议调整关联关系`
        })
        break
        
      case 'MISSING_INJECTION_PARAMS':
      case 'MISSING_PATTERN':
        suggestions.push({
          priority: 'low',
          action: '补充缺失的配置',
          description: `${warning.message}，建议完善配置信息`
        })
        break
        
      case 'QUANTITY_MISMATCH':
        suggestions.push({
          priority: 'medium',
          action: '统一数量信息',
          description: `${warning.message}，建议保持数据一致性`
        })
        break
        
      case 'INCOMPLETE_PATTERN':
        suggestions.push({
          priority: 'low',
          action: '完善图案信息',
          description: `${warning.message}，建议补充必要字段`
        })
        break
    }
  })
  
  return suggestions.sort((a, b) => {
    const priorityOrder = { high: 3, medium: 2, low: 1 }
    return priorityOrder[b.priority] - priorityOrder[a.priority]
  })
}