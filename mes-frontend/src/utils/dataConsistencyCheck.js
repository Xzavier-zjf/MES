/**
 * 数据一致性检查工具
 */

/**
 * 检查计划状态与任务分配的一致性
 * @param {Array} plans 计划列表
 * @param {Array} tasks 任务列表
 * @returns {Object} 检查结果
 */
export function checkPlanTaskConsistency(plans, tasks) {
  const inconsistencies = []
  const warnings = []
  
  plans.forEach(plan => {
    const relatedTasks = tasks.filter(task => task.planId === plan.id)
    const totalAllocated = relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
    const completedTasks = relatedTasks.filter(task => task.status === '已完成')
    const completedQuantity = completedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
    
    // 检查1: 计划状态为"已完成"但任务分配为0
    if (plan.status === '已完成' && totalAllocated === 0) {
      inconsistencies.push({
        type: 'PLAN_COMPLETED_NO_TASKS',
        planId: plan.id,
        planCode: plan.planCode,
        message: `计划 ${plan.planCode} 状态为"已完成"，但没有分配任何任务`,
        severity: 'error'
      })
    }
    
    // 检查2: 计划状态为"已完成"但任务未全部完成
    if (plan.status === '已完成' && completedQuantity < plan.totalQuantity) {
      inconsistencies.push({
        type: 'PLAN_COMPLETED_TASKS_INCOMPLETE',
        planId: plan.id,
        planCode: plan.planCode,
        message: `计划 ${plan.planCode} 状态为"已完成"，但任务完成数量(${completedQuantity})小于计划总量(${plan.totalQuantity})`,
        severity: 'error'
      })
    }
    
    // 检查3: 任务分配超过计划总量
    if (totalAllocated > plan.totalQuantity) {
      inconsistencies.push({
        type: 'TASKS_EXCEED_PLAN',
        planId: plan.id,
        planCode: plan.planCode,
        message: `计划 ${plan.planCode} 任务分配总量(${totalAllocated})超过计划总量(${plan.totalQuantity})`,
        severity: 'warning'
      })
    }
    
    // 检查4: 计划状态为"进行中"但没有进行中的任务
    const inProgressTasks = relatedTasks.filter(task => task.status === '进行中')
    if (plan.status === '进行中' && inProgressTasks.length === 0 && totalAllocated > 0) {
      warnings.push({
        type: 'PLAN_IN_PROGRESS_NO_ACTIVE_TASKS',
        planId: plan.id,
        planCode: plan.planCode,
        message: `计划 ${plan.planCode} 状态为"进行中"，但没有进行中的任务`,
        severity: 'info'
      })
    }
    
    // 检查5: 计划状态为"待下发"但已有任务
    if (plan.status === '待下发' && totalAllocated > 0) {
      warnings.push({
        type: 'PLAN_PENDING_HAS_TASKS',
        planId: plan.id,
        planCode: plan.planCode,
        message: `计划 ${plan.planCode} 状态为"待下发"，但已分配了 ${totalAllocated} 个任务`,
        severity: 'info'
      })
    }
  })
  
  return {
    isConsistent: inconsistencies.length === 0,
    inconsistencies,
    warnings,
    summary: {
      totalPlans: plans.length,
      inconsistentPlans: inconsistencies.length,
      warningPlans: warnings.length,
      healthScore: Math.max(0, 100 - (inconsistencies.length * 10) - (warnings.length * 2))
    }
  }
}

/**
 * 生成数据一致性报告
 * @param {Object} checkResult 检查结果
 * @returns {string} 报告文本
 */
export function generateConsistencyReport(checkResult) {
  const { inconsistencies, warnings, summary } = checkResult
  
  let report = `数据一致性检查报告\n`
  report += `==================\n`
  report += `总计划数: ${summary.totalPlans}\n`
  report += `不一致计划: ${summary.inconsistentPlans}\n`
  report += `警告计划: ${summary.warningPlans}\n`
  report += `健康评分: ${summary.healthScore}/100\n\n`
  
  if (inconsistencies.length > 0) {
    report += `严重问题:\n`
    inconsistencies.forEach((issue, index) => {
      report += `${index + 1}. ${issue.message}\n`
    })
    report += `\n`
  }
  
  if (warnings.length > 0) {
    report += `警告信息:\n`
    warnings.forEach((warning, index) => {
      report += `${index + 1}. ${warning.message}\n`
    })
  }
  
  return report
}

/**
 * 修复建议生成器
 * @param {Object} inconsistency 不一致问题
 * @returns {Array} 修复建议列表
 */
export function generateFixSuggestions(inconsistency) {
  const suggestions = []
  
  switch (inconsistency.type) {
    case 'PLAN_COMPLETED_NO_TASKS':
      suggestions.push('将计划状态改为"待下发"或"进行中"')
      suggestions.push('为该计划创建相应的生产任务')
      suggestions.push('检查是否存在数据录入错误')
      break
      
    case 'PLAN_COMPLETED_TASKS_INCOMPLETE':
      suggestions.push('将计划状态改为"进行中"')
      suggestions.push('检查任务完成状态是否正确更新')
      suggestions.push('确认是否存在未记录的完成任务')
      break
      
    case 'TASKS_EXCEED_PLAN':
      suggestions.push('检查任务数量是否录入错误')
      suggestions.push('调整计划总量以匹配实际需求')
      suggestions.push('删除多余的任务分配')
      break
      
    case 'PLAN_IN_PROGRESS_NO_ACTIVE_TASKS':
      suggestions.push('启动相关任务的执行')
      suggestions.push('将计划状态改为"待下发"')
      suggestions.push('检查任务状态更新是否及时')
      break
      
    case 'PLAN_PENDING_HAS_TASKS':
      suggestions.push('将计划状态改为"进行中"')
      suggestions.push('确认任务是否应该存在')
      suggestions.push('检查计划下发流程')
      break
      
    default:
      suggestions.push('请联系系统管理员检查数据')
  }
  
  return suggestions
}