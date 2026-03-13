// src/api/plans.js
const BASE_URL = 'http://localhost:8080/api/v1/production/plans'

export const getPlans = async (page = 0, size = 10) => {
  const res = await fetch(`${BASE_URL}?page=${page}&size=${size}`)
  if (!res.ok) throw new Error('获取生产计划失败')
  return await res.json()
}

export const createPlan = async (planData) => {
  console.log('创建计划请求:', planData)
  
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(planData),
  })
  
  if (!res.ok) {
    const errorText = await res.text()
    console.error('创建计划失败:', { status: res.status, statusText: res.statusText, errorText })
    throw new Error(`创建生产计划失败: ${res.status} ${res.statusText} - ${errorText}`)
  }
  
  // 检查响应体是否为空
  const text = await res.text()
  if (!text) {
    throw new Error('创建生产计划失败：后端返回空响应')
  }
  
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('后端返回非JSON格式响应:', text)
    throw new Error('创建生产计划失败：响应格式错误')
  }
}

export const updatePlan = async (id, planData) => {
  console.log('更新计划请求:', { id, planData })
  
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(planData),
  })
  
  if (!res.ok) {
    const errorText = await res.text()
    console.error('更新计划失败:', { status: res.status, statusText: res.statusText, errorText })
    throw new Error(`更新生产计划失败: ${res.status} ${res.statusText} - ${errorText}`)
  }
  
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

export const deletePlan = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除生产计划失败')
  return await res.text()
}