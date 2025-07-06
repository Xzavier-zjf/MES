// src/api/injection.js
const BASE_URL = 'http://localhost:8080/api/v1/process/injection-params'

export const getInjectionParams = async (params = {}) => {
  const searchParams = new URLSearchParams()
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 20)
  searchParams.append('sort', params.sort || 'id,desc')

  const res = await fetch(`${BASE_URL}/all?${searchParams.toString()}`)
  if (!res.ok) throw new Error('获取注塑参数列表失败')
  return await res.json()
}

export const getInjectionParamById = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`)
  if (!res.ok) throw new Error('获取注塑参数详情失败')
  return await res.json()
}

export const createInjectionParam = async (paramData) => {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(paramData),
  })
  if (!res.ok) throw new Error('创建注塑参数失败')
  return await res.json()
}

export const updateInjectionParam = async (id, paramData) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(paramData),
  })
  if (!res.ok) throw new Error('更新注塑参数失败')
  
  // 检查响应体是否为空
  const text = await res.text()
  if (!text) {
    return { success: true, message: '参数更新成功' }
  }
  
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('后端返回非JSON格式响应:', text)
    return { success: true, message: '参数更新成功' }
  }
}

export const deleteInjectionParam = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除注塑参数失败')
  return await res.text()
} 