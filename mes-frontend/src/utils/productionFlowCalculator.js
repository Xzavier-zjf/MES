/**
 * 生产流程数量计算工具
 * 考虑工序类型、进度和生产流程的复杂计算
 */

// 工序流程定义（按顺序）
export const PROCESS_FLOW = [
  '注塑',
  '印刷', 
  '组装',
  '质检',
  '包装'
]

// 工序优先级映射
export const PROCESS_PRIORITY = {
  '注塑': 1,
  '印刷': 2,
  '组装': 3,
  '质检': 4,
  '包装': 5
}

/**
 * 计算计划的详细任务分配情况
 * @param {Object} plan 生产计划
 * @param {Array} tasks 相关任务列表
 * @returns {Object} 详细的分配情况
 */
export function calculateDetailedAllocation(plan, tasks) {
  const relatedTasks = tasks.filter(task => task.planId === plan.id)
  
  // 按工序类型分组统计
  const processSummary = {}
  PROCESS_FLOW.forEach(processType => {
    const processTasks = relatedTasks.filter(task => task.processType === processType)
    
    processSummary[processType] = {
      totalAllocated: processTasks.reduce((sum, task) => sum + (task.quantity || 0), 0),
      completedQuantity: processTasks
        .filter(task => task.status === '已完成')
        .reduce((sum, task) => sum + (task.quantity || 0), 0),
      inProgressQuantity: processTasks
        .filter(task => task.status === '进行中')
        .reduce((sum, task) => sum + (task.quantity || 0), 0),
      pendingQuantity: processTasks
        .filter(task => task.status === '待下发')
        .reduce((sum, task) => sum + (task.quantity || 0), 0),
      taskCount: processTasks.length
    }
  })
  
  // 计算总体统计
  const totalAllocated = relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
  const totalCompleted = relatedTasks
    .filter(task => task.status === '已完成')
    .reduce((sum, task) => sum + (task.quantity || 0), 0)
  const totalInProgress = relatedTasks
    .filter(task => task.status === '进行中')
    .reduce((sum, task) => sum + (task.quantity || 0), 0)
  
  // 计算实际剩余（考虑已完成的数量）
  const actualRemaining = plan.totalQuantity - totalCompleted
  const allocatedRemaining = plan.totalQuantity - totalAllocated
  
  return {
    planId: plan.id,
    planCode: plan.planCode,
    totalQuantity: plan.totalQuantity,
    totalAllocated,
    totalCompleted,
    totalInProgress,
    actualRemaining, // 实际还需要生产的数量
    allocatedRemaining, // 还未分配任务的数量
    processSummary,
    flowProgress: calculateFlowProgress(processSummary, plan.totalQuantity)
  }
}

/**
 * 计算生产流程进度
 * @param {Object} processSummary 工序汇总
 * @param {number} totalQuantity 计划总量
 * @returns {Object} 流程进度
 */
function calculateFlowProgress(processSummary, totalQuantity) {
  const progress = {}
  
  PROCESS_FLOW.forEach(processType => {
    const summary = processSummary[processType]
    const completionRate = totalQuantity > 0 ? (summary.completedQuantity / totalQuantity) * 100 : 0
    
    progress[processType] = {
      completionRate: Math.round(completionRate * 100) / 100,
      status: getProcessStatus(summary, totalQuantity),
      bottleneck: isBottleneck(processType, processSummary, totalQuantity)
    }
  })
  
  return progress
}

/**
 * 获取工序状态
 * @param {Object} summary 工序汇总
 * @param {number} totalQuantity 总量
 * @returns {string} 状态
 */
function getProcessStatus(summary, totalQuantity) {
  if (summary.completedQuantity === totalQuantity) return 'completed'
  if (summary.inProgressQuantity > 0) return 'in_progress'
  if (summary.totalAllocated > 0) return 'allocated'
  return 'not_started'
}

/**
 * 判断是否为瓶颈工序
 * @param {string} processType 工序类型
 * @param {Object} processSummary 工序汇总
 * @param {number} totalQuantity 总量
 * @returns {boolean} 是否为瓶颈
 */
function isBottleneck(processType, processSummary, totalQuantity) {
  const currentProcess = processSummary[processType]
  const currentIndex = PROCESS_FLOW.indexOf(processType)
  
  // 检查前序工序是否已完成足够数量
  if (currentIndex > 0) {
    const prevProcess = PROCESS_FLOW[currentIndex - 1]
    const prevSummary = processSummary[prevProcess]
    
    // 如果前序工序完成数量 > 当前工序分配数量，可能是瓶颈
    if (prevSummary.completedQuantity > currentProcess.totalAllocated) {
      return true
    }
  }
  
  return false
}

/**
 * 计算可分配的任务数量（考虑工序依赖）
 * @param {string} processType 工序类型
 * @param {Object} plan 计划
 * @param {Array} tasks 任务列表
 * @returns {Object} 可分配数量信息
 */
export function calculateAvailableQuantity(processType, plan, tasks) {
  const allocation = calculateDetailedAllocation(plan, tasks)
  const currentIndex = PROCESS_FLOW.indexOf(processType)
  
  let maxAvailable = plan.totalQuantity
  
  // 对于非首工序，需要考虑前序工序的完成情况
  if (currentIndex > 0) {
    const prevProcess = PROCESS_FLOW[currentIndex - 1]
    const prevCompleted = allocation.processSummary[prevProcess].completedQuantity
    
    // 当前工序最多只能分配前序工序已完成的数量
    maxAvailable = Math.min(maxAvailable, prevCompleted)
  }
  
  // 减去当前工序已分配的数量
  const currentAllocated = allocation.processSummary[processType].totalAllocated
  const available = Math.max(0, maxAvailable - currentAllocated)
  
  return {
    maxAvailable,
    currentAllocated,
    available,
    processType,
    dependsOn: currentIndex > 0 ? PROCESS_FLOW[currentIndex - 1] : null,
    canAllocate: available > 0,
    reason: available === 0 ? getUnavailableReason(processType, allocation) : null
  }
}

/**
 * 获取无法分配的原因
 * @param {string} processType 工序类型
 * @param {Object} allocation 分配情况
 * @returns {string} 原因
 */
function getUnavailableReason(processType, allocation) {
  const currentIndex = PROCESS_FLOW.indexOf(processType)
  const currentSummary = allocation.processSummary[processType]
  
  if (currentSummary.totalAllocated >= allocation.totalQuantity) {
    return '已完全分配'
  }
  
  if (currentIndex > 0) {
    const prevProcess = PROCESS_FLOW[currentIndex - 1]
    const prevSummary = allocation.processSummary[prevProcess]
    
    if (prevSummary.completedQuantity < currentSummary.totalAllocated) {
      return `等待前序工序"${prevProcess}"完成更多数量`
    }
  }
  
  return '未知原因'
}

/**
 * 生成生产流程报告
 * @param {Object} plan 计划
 * @param {Array} tasks 任务列表
 * @returns {string} 报告文本
 */
export function generateFlowReport(plan, tasks) {
  const allocation = calculateDetailedAllocation(plan, tasks)
  
  let report = `生产流程报告 - ${plan.planCode}\n`
  report += `==========================================\n`
  report += `计划总量: ${plan.totalQuantity}\n`
  report += `已分配: ${allocation.totalAllocated}\n`
  report += `已完成: ${allocation.totalCompleted}\n`
  report += `进行中: ${allocation.totalInProgress}\n`
  report += `实际剩余: ${allocation.actualRemaining}\n\n`
  
  report += `各工序详情:\n`
  PROCESS_FLOW.forEach(processType => {
    const summary = allocation.processSummary[processType]
    const progress = allocation.flowProgress[processType]
    
    report += `${processType}:\n`
    report += `  已分配: ${summary.totalAllocated}\n`
    report += `  已完成: ${summary.completedQuantity}\n`
    report += `  进行中: ${summary.inProgressQuantity}\n`
    report += `  完成率: ${progress.completionRate}%\n`
    report += `  状态: ${progress.status}\n`
    if (progress.bottleneck) {
      report += `  ⚠️ 疑似瓶颈工序\n`
    }
    report += `\n`
  })
  
  return report
}

/**
 * 获取推荐的下一步操作
 * @param {Object} plan 计划
 * @param {Array} tasks 任务列表
 * @returns {Array} 推荐操作列表
 */
export function getRecommendedActions(plan, tasks) {
  const allocation = calculateDetailedAllocation(plan, tasks)
  const recommendations = []
  
  // 检查是否有未分配的数量
  if (allocation.allocatedRemaining > 0) {
    recommendations.push({
      type: 'allocate_tasks',
      priority: 'high',
      message: `还有 ${allocation.allocatedRemaining} 个未分配任务，建议创建相应工序任务`,
      action: 'create_tasks'
    })
  }
  
  // 检查工序瓶颈
  PROCESS_FLOW.forEach(processType => {
    const progress = allocation.flowProgress[processType]
    if (progress.bottleneck) {
      recommendations.push({
        type: 'bottleneck',
        priority: 'medium',
        message: `${processType} 工序可能存在瓶颈，建议增加资源投入`,
        action: 'optimize_process'
      })
    }
  })
  
  // 检查工序不平衡
  const completionRates = PROCESS_FLOW.map(p => allocation.flowProgress[p].completionRate)
  const maxRate = Math.max(...completionRates)
  const minRate = Math.min(...completionRates)
  
  if (maxRate - minRate > 20) {
    recommendations.push({
      type: 'imbalance',
      priority: 'medium',
      message: '各工序进度不平衡，建议调整资源分配',
      action: 'balance_processes'
    })
  }
  
  return recommendations.sort((a, b) => {
    const priorityOrder = { high: 3, medium: 2, low: 1 }
    return priorityOrder[b.priority] - priorityOrder[a.priority]
  })
}