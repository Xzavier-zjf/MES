// src/api/plans.js
const BASE_URL = 'http://localhost:8080/api/v1/production/plans'

export const getPlans = async (page = 0, size = 10) => {
  const res = await fetch(`${BASE_URL}?page=${page}&size=${size}`)
  if (!res.ok) throw new Error('获取生产计划失败')
  return await res.json()
}

export const createPlan = async (planData) => {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(planData),
  })
  if (!res.ok) throw new Error('创建生产计划失败')
  return await res.json()
}

export const updatePlan = async (id, planData) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(planData),
  })
  if (!res.ok) throw new Error('更新生产计划失败')
  return await res.json()
}

export const deletePlan = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除生产计划失败')
  return await res.text()
}