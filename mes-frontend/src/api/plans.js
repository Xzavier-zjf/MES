// src/api/plans.js
import request from './request'

const BASE_URL = '/api/v1/production/plans'

export const getPlans = async (page = 0, size = 10) => {
  return await request.get(`${BASE_URL}?page=${page}&size=${size}`)
}

export const createPlan = async (planData) => {
  console.log('创建计划请求:', planData)
  
  return await request.post(BASE_URL, planData)
}

export const updatePlan = async (id, planData) => {
  console.log('更新计划请求:', { id, planData })
  
  return await request.put(`${BASE_URL}/${id}`, planData)
}

export const deletePlan = async (id) => {
  return await request.delete(`${BASE_URL}/${id}`)
}
