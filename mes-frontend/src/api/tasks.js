// src/api/tasks.js
const BASE_URL = 'http://localhost:8080/api/v1/production/tasks'

export const getTasks = async (params = {}) => {
  const searchParams = new URLSearchParams()
  if (params.planCode) searchParams.append('planCode', params.planCode)
  if (params.processType) searchParams.append('processType', params.processType)
  if (params.status) searchParams.append('status', params.status)
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 100)

  const res = await fetch(`${BASE_URL}?${searchParams.toString()}`)
  if (!res.ok) throw new Error('获取任务列表失败')
  return await res.json()
}

export const createTask = async (taskData) => {
  const res = await fetch(`${BASE_URL}?planId=${taskData.planId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(taskData),
  })
  if (!res.ok) throw new Error('创建任务失败')
  
  // 检查响应体是否为空
  const text = await res.text()
  if (!text) {
    throw new Error('创建任务失败：后端返回空响应')
  }
  
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('后端返回非JSON格式响应:', text)
    throw new Error('创建任务失败：响应格式错误')
  }
}

export const updateTask = async (id, taskData) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(taskData),
  })
  if (!res.ok) throw new Error('更新任务失败')
  
  // 检查响应体是否为空
  const text = await res.text()
  if (!text) {
    return { success: true, message: '更新成功' }
  }
  
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('后端返回非JSON格式响应:', text)
    return { success: true, message: '更新成功' }
  }
}

export const deleteTask = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除任务失败')
  return await res.text()
} 