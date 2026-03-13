/**
 * 任务状态管理器
 * 自动检测和建议任务状态更新
 */

import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'

class TaskStatusManager {
  constructor() {
    this.statusRules = new Map()
    this.autoUpdateEnabled = false
    this.listeners = new Set()
    this.initializeStatusRules()
  }

  /**
   * 初始化状态转换规则
   */
  initializeStatusRules() {
    // 状态转换规则
    this.statusRules.set('待下发', {
      canTransitionTo: ['进行中', '已暂停'],
      autoTransitionRules: [
        {
          condition: (task) => task.completedQuantity > 0,
          targetStatus: '进行中',
          reason: '已有生产进度'
        }
      ]
    })

    this.statusRules.set('进行中', {
      canTransitionTo: ['已完成', '已暂停'],
      autoTransitionRules: [
        {
          condition: (task) => task.completedQuantity >= task.quantity && task.quantity > 0,
          targetStatus: '已完成',
          reason: '生产数量已达到任务要求'
        }
      ]
    })

    this.statusRules.set('已暂停', {
      canTransitionTo: ['进行中', '已完成'],
      autoTransitionRules: [
        {
          condition: (task) => task.completedQuantity >= task.quantity && task.quantity > 0,
          targetStatus: '已完成',
          reason: '生产数量已达到任务要求'
        },
        {
          condition: (task) => task.completedQuantity > 0 && task.completedQuantity < task.quantity,
          targetStatus: '进行中',
          reason: '生产已恢复'
        }
      ]
    })

    this.statusRules.set('已完成', {
      canTransitionTo: ['进行中'], // 允许重新开始
      autoTransitionRules: []
    })
  }

  /**
   * 添加状态变更监听器
   * @param {Function} listener 监听器函数
   */
  addListener(listener) {
    this.listeners.add(listener)
  }

  /**
   * 移除状态变更监听器
   * @param {Function} listener 监听器函数
   */
  removeListener(listener) {
    this.listeners.delete(listener)
  }

  /**
   * 通知监听器状态变更
   * @param {Object} changeInfo 变更信息
   */
  notifyListeners(changeInfo) {
    this.listeners.forEach(listener => {
      try {
        listener(changeInfo)
      } catch (error) {
        console.error('状态变更监听器执行失败:', error)
      }
    })
  }

  /**
   * 检查任务是否需要状态更新
   * @param {Object} task 任务对象
   * @returns {Object|null} 建议的状态更新信息
   */
  checkStatusUpdate(task) {
    const currentStatus = task.status
    const rules = this.statusRules.get(currentStatus)
    
    if (!rules) {
      return null
    }

    // 检查自动转换规则
    for (const rule of rules.autoTransitionRules) {
      if (rule.condition(task)) {
        return {
          taskId: task.id,
          currentStatus,
          suggestedStatus: rule.targetStatus,
          reason: rule.reason,
          confidence: this.calculateConfidence(task, rule),
          autoApplicable: true
        }
      }
    }

    return null
  }

  /**
   * 计算状态更新建议的置信度
   * @param {Object} task 任务对象
   * @param {Object} rule 规则对象
   * @returns {number} 置信度 (0-100)
   */
  calculateConfidence(task, rule) {
    let confidence = 80 // 基础置信度

    // 根据完成比例调整置信度
    if (task.quantity > 0) {
      const completionRate = task.completedQuantity / task.quantity
      if (completionRate >= 1.0) {
        confidence = 95 // 完成率100%，高置信度
      } else if (completionRate >= 0.8) {
        confidence = 85 // 完成率80%以上，较高置信度
      } else if (completionRate > 0) {
        confidence = 75 // 有进度但未完成，中等置信度
      }
    }

    // 根据时间因素调整
    if (task.updatedAt) {
      const lastUpdateTime = new Date(task.updatedAt)
      const now = new Date()
      const hoursSinceUpdate = (now - lastUpdateTime) / (1000 * 60 * 60)
      
      if (hoursSinceUpdate > 24) {
        confidence -= 10 // 超过24小时未更新，降低置信度
      }
    }

    return Math.max(50, Math.min(100, confidence))
  }

  /**
   * 批量检查任务状态
   * @param {Array} tasks 任务列表
   * @returns {Array} 需要更新状态的任务列表
   */
  batchCheckStatus(tasks) {
    const suggestions = []
    
    tasks.forEach(task => {
      const suggestion = this.checkStatusUpdate(task)
      if (suggestion) {
        suggestions.push(suggestion)
      }
    })

    return suggestions
  }

  /**
   * 应用状态更新建议
   * @param {Object} suggestion 状态更新建议
   * @param {Function} updateCallback 更新回调函数
   */
  async applySuggestion(suggestion, updateCallback) {
    try {
      const { taskId, currentStatus, suggestedStatus, reason } = suggestion
      
      // 确认更新
      const confirmResult = await ElMessageBox.confirm(
        `任务状态建议更新：\n\n` +
        `当前状态：${currentStatus}\n` +
        `建议状态：${suggestedStatus}\n` +
        `更新原因：${reason}\n\n` +
        `是否确认更新？`,
        '状态更新确认',
        {
          confirmButtonText: '确认更新',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).catch(() => false)

      if (!confirmResult) {
        return false
      }

      // 执行更新
      await updateCallback(taskId, suggestedStatus)
      
      this.notifyListeners({
        type: 'STATUS_UPDATED',
        taskId,
        oldStatus: currentStatus,
        newStatus: suggestedStatus,
        reason
      })

      ElMessage.success(`任务状态已更新为：${suggestedStatus}`)
      return true
    } catch (error) {
      console.error('应用状态更新建议失败:', error)
      ElMessage.error('状态更新失败: ' + error.message)
      return false
    }
  }

  /**
   * 自动应用高置信度的状态更新
   * @param {Array} suggestions 状态更新建议列表
   * @param {Function} updateCallback 更新回调函数
   * @param {number} confidenceThreshold 置信度阈值
   */
  async autoApplySuggestions(suggestions, updateCallback, confidenceThreshold = 90) {
    const highConfidenceSuggestions = suggestions.filter(s => 
      s.confidence >= confidenceThreshold && s.autoApplicable
    )

    if (highConfidenceSuggestions.length === 0) {
      return []
    }

    const results = []
    
    for (const suggestion of highConfidenceSuggestions) {
      try {
        await updateCallback(suggestion.taskId, suggestion.suggestedStatus)
        results.push({
          taskId: suggestion.taskId,
          success: true,
          oldStatus: suggestion.currentStatus,
          newStatus: suggestion.suggestedStatus
        })
      } catch (error) {
        results.push({
          taskId: suggestion.taskId,
          success: false,
          error: error.message
        })
      }
    }

    const successCount = results.filter(r => r.success).length
    if (successCount > 0) {
      ElNotification({
        title: '自动状态更新',
        message: `已自动更新 ${successCount} 个任务的状态`,
        type: 'success',
        duration: 3000
      })
    }

    return results
  }

  /**
   * 生成状态检查报告
   * @param {Array} suggestions 状态更新建议列表
   * @returns {string} 报告文本
   */
  generateStatusReport(suggestions) {
    let report = `任务状态检查报告\n`
    report += `==================\n`
    report += `检查时间: ${new Date().toLocaleString()}\n`
    report += `建议更新任务数: ${suggestions.length}\n\n`

    if (suggestions.length > 0) {
      // 按置信度分组
      const highConfidence = suggestions.filter(s => s.confidence >= 90)
      const mediumConfidence = suggestions.filter(s => s.confidence >= 70 && s.confidence < 90)
      const lowConfidence = suggestions.filter(s => s.confidence < 70)

      report += `高置信度建议 (≥90%): ${highConfidence.length}\n`
      report += `中等置信度建议 (70-89%): ${mediumConfidence.length}\n`
      report += `低置信度建议 (<70%): ${lowConfidence.length}\n\n`

      report += `详细建议:\n`
      suggestions.forEach((suggestion, index) => {
        report += `${index + 1}. 任务 ${suggestion.taskId}\n`
        report += `   当前状态: ${suggestion.currentStatus}\n`
        report += `   建议状态: ${suggestion.suggestedStatus}\n`
        report += `   更新原因: ${suggestion.reason}\n`
        report += `   置信度: ${suggestion.confidence}%\n`
        report += `   可自动应用: ${suggestion.autoApplicable ? '是' : '否'}\n\n`
      })
    }

    return report
  }

  /**
   * 启用自动状态更新
   * @param {Function} updateCallback 更新回调函数
   * @param {Object} options 配置选项
   */
  enableAutoUpdate(updateCallback, options = {}) {
    this.autoUpdateEnabled = true
    this.autoUpdateCallback = updateCallback
    this.autoUpdateOptions = {
      confidenceThreshold: 90,
      checkInterval: 60000, // 1分钟检查一次
      ...options
    }

    // 启动定时检查
    if (this.autoUpdateInterval) {
      clearInterval(this.autoUpdateInterval)
    }

    this.autoUpdateInterval = setInterval(() => {
      this.performAutoCheck()
    }, this.autoUpdateOptions.checkInterval)

    ElMessage.info('任务状态自动更新已启用')
  }

  /**
   * 禁用自动状态更新
   */
  disableAutoUpdate() {
    this.autoUpdateEnabled = false
    if (this.autoUpdateInterval) {
      clearInterval(this.autoUpdateInterval)
      this.autoUpdateInterval = null
    }
    ElMessage.info('任务状态自动更新已禁用')
  }

  /**
   * 执行自动检查（需要外部提供任务数据）
   */
  async performAutoCheck() {
    if (!this.autoUpdateEnabled || !this.autoUpdateCallback) {
      return
    }

    // 这里需要外部提供任务数据，因为这个工具类无法直接获取
    // 实际使用时需要在组件中调用
    console.log('执行自动状态检查...')
  }

  /**
   * 验证状态转换是否合法
   * @param {string} fromStatus 原状态
   * @param {string} toStatus 目标状态
   * @returns {boolean} 是否合法
   */
  isValidTransition(fromStatus, toStatus) {
    const rules = this.statusRules.get(fromStatus)
    return rules ? rules.canTransitionTo.includes(toStatus) : false
  }

  /**
   * 获取状态可转换的目标状态列表
   * @param {string} currentStatus 当前状态
   * @returns {Array} 可转换的状态列表
   */
  getValidTransitions(currentStatus) {
    const rules = this.statusRules.get(currentStatus)
    return rules ? rules.canTransitionTo : []
  }
}

// 创建全局实例
export const taskStatusManager = new TaskStatusManager()

// 导出类供其他地方使用
export default TaskStatusManager