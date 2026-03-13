/**
 * 生产数量逻辑验证工具
 */

/**
 * 验证任务数量是否超过计划总数量
 * @param {number} planTotalQuantity 计划总数量
 * @param {Array} tasks 任务列表
 * @returns {Object} 验证结果
 */
export function validateTaskQuantities(planTotalQuantity, tasks) {
  const totalTaskQuantity = tasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
  
  return {
    isValid: totalTaskQuantity <= planTotalQuantity,
    planTotal: planTotalQuantity,
    taskTotal: totalTaskQuantity,
    remaining: planTotalQuantity - totalTaskQuantity,
    message: totalTaskQuantity > planTotalQuantity 
      ? `任务总数量(${totalTaskQuantity})超过计划数量(${planTotalQuantity})`
      : totalTaskQuantity === planTotalQuantity
      ? '任务数量与计划数量完全匹配'
      : `还有${planTotalQuantity - totalTaskQuantity}个未分配到任务`
  }
}

/**
 * 计算计划完成进度
 * @param {number} planTotalQuantity 计划总数量
 * @param {Array} completedTasks 已完成任务列表
 * @returns {Object} 进度信息
 */
export function calculatePlanProgress(planTotalQuantity, completedTasks) {
  const completedQuantity = completedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
  const progress = planTotalQuantity > 0 ? (completedQuantity / planTotalQuantity * 100) : 0
  
  return {
    completedQuantity,
    totalQuantity: planTotalQuantity,
    progress: Math.round(progress * 100) / 100, // 保留2位小数
    isCompleted: progress >= 100,
    remaining: Math.max(0, planTotalQuantity - completedQuantity)
  }
}

/**
 * 验证新任务数量是否合理
 * @param {number} planId 计划ID
 * @param {number} newTaskQuantity 新任务数量
 * @param {Array} existingTasks 现有任务列表
 * @param {number} planTotalQuantity 计划总数量
 * @returns {Object} 验证结果
 */
export function validateNewTaskQuantity(planId, newTaskQuantity, existingTasks, planTotalQuantity) {
  const relatedTasks = existingTasks.filter(task => task.planId === planId)
  const currentTaskTotal = relatedTasks.reduce((sum, task) => sum + (task.quantity || 0), 0)
  const afterAddTotal = currentTaskTotal + newTaskQuantity
  
  return {
    isValid: afterAddTotal <= planTotalQuantity,
    currentTotal: currentTaskTotal,
    afterAddTotal,
    planTotal: planTotalQuantity,
    available: planTotalQuantity - currentTaskTotal,
    message: afterAddTotal > planTotalQuantity
      ? `添加此任务后总数量(${afterAddTotal})将超过计划数量(${planTotalQuantity})`
      : `可以添加，剩余可分配数量: ${planTotalQuantity - afterAddTotal}`
  }
}

/**
 * 获取计划的数量分配状态
 * @param {Object} plan 计划对象
 * @param {Array} tasks 相关任务列表
 * @returns {Object} 分配状态
 */
export function getPlanQuantityStatus(plan, tasks) {
  const relatedTasks = tasks.filter(task => task.planId === plan.id)
  const validation = validateTaskQuantities(plan.totalQuantity, relatedTasks)
  const progress = calculatePlanProgress(plan.totalQuantity, relatedTasks.filter(t => t.status === '已完成'))
  
  return {
    ...validation,
    progress: progress.progress,
    status: validation.remaining === 0 ? 'fully_allocated' : 
            validation.remaining > 0 ? 'partially_allocated' : 'over_allocated',
    statusText: validation.remaining === 0 ? '已完全分配' :
                validation.remaining > 0 ? '部分分配' : '超额分配'
  }
}