// src/api/tasks.js
import request from './request'

const BASE_URL = '/api/v1/production/tasks'

export const getTasks = async (params = {}) => {
  const searchParams = new URLSearchParams()
  if (params.planCode) searchParams.append('planCode', params.planCode)
  if (params.processType) searchParams.append('processType', params.processType)
  if (params.status) searchParams.append('status', params.status)
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 100)

  return await request.get(`${BASE_URL}?${searchParams.toString()}`)
}

export const createTask = async (taskData) => {
  return await request.post(`${BASE_URL}?planId=${taskData.planId}`, taskData)
}

export const updateTask = async (id, taskData) => {
  return await request.put(`${BASE_URL}/${id}`, taskData)
}

// 更新任务数量
export const updateTaskQuantity = async (taskId, quantity) => {
  try {
    const taskData = { quantity: quantity }
    const response = await updateTask(taskId, taskData)
    return response
  } catch (error) {
    throw new Error('更新任务数量失败: ' + error.message)
  }
}

export const deleteTask = async (id) => {
  return await request.delete(`${BASE_URL}/${id}`)
}

// 更新任务状态
export const updateTaskStatus = async (taskId, newStatus, reason = '') => {
  try {
    const taskData = { 
      status: newStatus,
      reason: reason,
      timestamp: new Date().toISOString()
    }
    
    const response = await updateTask(taskId, taskData)
    return response
  } catch (error) {
    throw new Error('更新任务状态失败: ' + error.message)
  }
}

// 批量状态更新
export const batchUpdateTaskStatus = async (updates) => {
  try {
    const promises = updates.map(update => 
      updateTaskStatus(update.taskId, update.suggestedStatus, update.reason)
    )
    
    const results = await Promise.all(promises)
    return results
  } catch (error) {
    throw new Error('批量更新任务状态失败: ' + error.message)
  }
}
