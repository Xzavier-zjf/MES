/**
 * 数据同步管理器
 * 用于处理任务数量与工艺参数数量的同步问题
 */

import { updateTaskQuantity } from '@/api/tasks'
import { updateInjectionParam } from '@/api/injection'
import { ElMessage, ElNotification } from 'element-plus'

class DataSyncManager {
  constructor() {
    this.syncQueue = new Map() // 同步队列
    this.isProcessing = false
    this.listeners = new Set() // 数据变更监听器
  }

  /**
   * 添加数据变更监听器
   * @param {Function} listener 监听器函数
   */
  addListener(listener) {
    this.listeners.add(listener)
  }

  /**
   * 移除数据变更监听器
   * @param {Function} listener 监听器函数
   */
  removeListener(listener) {
    this.listeners.delete(listener)
  }

  /**
   * 通知所有监听器数据已变更
   * @param {Object} changeInfo 变更信息
   */
  notifyListeners(changeInfo) {
    this.listeners.forEach(listener => {
      try {
        listener(changeInfo)
      } catch (error) {
        console.error('数据变更监听器执行失败:', error)
      }
    })
  }

  /**
   * 同步任务数量到工艺参数
   * @param {string} taskId 任务ID
   * @param {number} newQuantity 新数量
   * @param {Array} relatedParams 相关的工艺参数列表
   */
  async syncTaskQuantityToParams(taskId, newQuantity, relatedParams = []) {
    try {
      const syncPromises = relatedParams.map(param => {
        if (param.quantity !== newQuantity) {
          return this.updateParamQuantity(param.id, newQuantity)
        }
        return Promise.resolve()
      })

      await Promise.all(syncPromises)
      
      this.notifyListeners({
        type: 'TASK_QUANTITY_SYNCED',
        taskId,
        newQuantity,
        affectedParams: relatedParams.length
      })

      ElMessage.success(`已同步 ${relatedParams.length} 个工艺参数的数量`)
    } catch (error) {
      console.error('同步任务数量到工艺参数失败:', error)
      ElMessage.error('同步工艺参数数量失败: ' + error.message)
      throw error
    }
  }

  /**
   * 更新工艺参数数量
   * @param {string} paramId 参数ID
   * @param {number} newQuantity 新数量
   */
  async updateParamQuantity(paramId, newQuantity) {
    try {
      await updateInjectionParam(paramId, { quantity: newQuantity })
      return true
    } catch (error) {
      console.error(`更新工艺参数 ${paramId} 数量失败:`, error)
      throw error
    }
  }

  /**
   * 检查数据一致性
   * @param {Array} tasks 任务列表
   * @param {Array} injectionParams 注塑参数列表
   * @returns {Object} 一致性检查结果
   */
  checkDataConsistency(tasks, injectionParams) {
    const inconsistencies = []
    const taskMap = new Map(tasks.map(task => [task.id, task]))

    injectionParams.forEach(param => {
      const task = taskMap.get(param.taskId)
      if (!task) {
        inconsistencies.push({
          type: 'MISSING_TASK',
          paramId: param.id,
          taskId: param.taskId,
          message: `工艺参数 ${param.id} 关联的任务 ${param.taskId} 不存在`
        })
      } else if (task.quantity !== param.quantity) {
        const difference = Math.abs(task.quantity - param.quantity)
        const isSerious = difference > task.quantity * 0.1 // 差异超过10%

        inconsistencies.push({
          type: 'QUANTITY_MISMATCH',
          paramId: param.id,
          taskId: param.taskId,
          taskQuantity: task.quantity,
          paramQuantity: param.quantity,
          difference,
          isSerious,
          message: `任务数量 ${task.quantity} 与工艺参数数量 ${param.quantity} 不一致`
        })
      }
    })

    return {
      isConsistent: inconsistencies.length === 0,
      inconsistencies,
      summary: {
        totalParams: injectionParams.length,
        inconsistentParams: inconsistencies.length,
        seriousInconsistencies: inconsistencies.filter(i => i.isSerious).length
      }
    }
  }

  /**
   * 自动修复数据不一致问题
   * @param {Array} inconsistencies 不一致问题列表
   * @param {string} strategy 修复策略: 'task-priority' | 'param-priority'
   */
  async autoFixInconsistencies(inconsistencies, strategy = 'task-priority') {
    const fixResults = []

    for (const inconsistency of inconsistencies) {
      if (inconsistency.type === 'QUANTITY_MISMATCH') {
        try {
          if (strategy === 'task-priority') {
            // 以任务数量为准，更新工艺参数
            await this.updateParamQuantity(inconsistency.paramId, inconsistency.taskQuantity)
            fixResults.push({
              paramId: inconsistency.paramId,
              action: 'UPDATE_PARAM',
              oldValue: inconsistency.paramQuantity,
              newValue: inconsistency.taskQuantity,
              success: true
            })
          } else if (strategy === 'param-priority') {
            // 以工艺参数数量为准，更新任务
            await updateTaskQuantity(inconsistency.taskId, inconsistency.paramQuantity)
            fixResults.push({
              taskId: inconsistency.taskId,
              action: 'UPDATE_TASK',
              oldValue: inconsistency.taskQuantity,
              newValue: inconsistency.paramQuantity,
              success: true
            })
          }
        } catch (error) {
          fixResults.push({
            paramId: inconsistency.paramId,
            taskId: inconsistency.taskId,
            action: strategy === 'task-priority' ? 'UPDATE_PARAM' : 'UPDATE_TASK',
            success: false,
            error: error.message
          })
        }
      }
    }

    const successCount = fixResults.filter(r => r.success).length
    const failCount = fixResults.filter(r => !r.success).length

    if (successCount > 0) {
      ElNotification({
        title: '数据修复完成',
        message: `成功修复 ${successCount} 项数据不一致问题${failCount > 0 ? `，${failCount} 项修复失败` : ''}`,
        type: successCount === fixResults.length ? 'success' : 'warning',
        duration: 5000
      })
    }

    this.notifyListeners({
      type: 'AUTO_FIX_COMPLETED',
      results: fixResults,
      successCount,
      failCount
    })

    return fixResults
  }

  /**
   * 批量验证数据一致性
   * @param {Array} tasks 任务列表
   * @param {Array} injectionParams 注塑参数列表
   * @returns {Promise<Object>} 验证结果
   */
  async batchValidateConsistency(tasks, injectionParams) {
    const result = this.checkDataConsistency(tasks, injectionParams)
    
    if (!result.isConsistent) {
      console.group('数据一致性检查结果')
      console.log('总参数数:', result.summary.totalParams)
      console.log('不一致参数数:', result.summary.inconsistentParams)
      console.log('严重不一致数:', result.summary.seriousInconsistencies)
      console.table(result.inconsistencies)
      console.groupEnd()

      // 显示通知
      const seriousCount = result.summary.seriousInconsistencies
      if (seriousCount > 0) {
        ElNotification({
          title: '发现严重数据不一致',
          message: `发现 ${seriousCount} 项严重数据不一致，建议立即修复`,
          type: 'error',
          duration: 0 // 不自动关闭
        })
      } else {
        ElNotification({
          title: '发现数据不一致',
          message: `发现 ${result.summary.inconsistentParams} 项数据不一致`,
          type: 'warning',
          duration: 5000
        })
      }
    }

    return result
  }

  /**
   * 生成数据同步报告
   * @param {Object} consistencyResult 一致性检查结果
   * @returns {string} 报告文本
   */
  generateSyncReport(consistencyResult) {
    const { inconsistencies, summary } = consistencyResult
    
    let report = `数据同步检查报告\n`
    report += `==================\n`
    report += `检查时间: ${new Date().toLocaleString()}\n`
    report += `总参数数: ${summary.totalParams}\n`
    report += `不一致参数: ${summary.inconsistentParams}\n`
    report += `严重不一致: ${summary.seriousInconsistencies}\n`
    report += `一致性状态: ${consistencyResult.isConsistent ? '✅ 一致' : '❌ 不一致'}\n\n`

    if (inconsistencies.length > 0) {
      report += `详细问题:\n`
      inconsistencies.forEach((issue, index) => {
        report += `${index + 1}. ${issue.message}\n`
        if (issue.type === 'QUANTITY_MISMATCH') {
          report += `   - 任务数量: ${issue.taskQuantity}\n`
          report += `   - 参数数量: ${issue.paramQuantity}\n`
          report += `   - 差异: ${issue.difference}\n`
          report += `   - 严重程度: ${issue.isSerious ? '严重' : '轻微'}\n`
        }
        report += `\n`
      })
    }

    return report
  }
}

// 创建全局实例
export const dataSyncManager = new DataSyncManager()

// 导出类供其他地方使用
export default DataSyncManager