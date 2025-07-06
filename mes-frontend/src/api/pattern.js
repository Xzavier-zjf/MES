// src/api/pattern.js
const BASE_URL = 'http://localhost:8080/api/v1/process/print-patterns'

export const getPrintPatterns = async (params = {}) => {
  const searchParams = new URLSearchParams()
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 20)
  searchParams.append('sort', params.sort || 'id,desc')

  const res = await fetch(`${BASE_URL}/all?${searchParams.toString()}`)
  if (!res.ok) throw new Error('获取印刷图案列表失败')
  return await res.json()
}

export const getPrintPatternById = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`)
  if (!res.ok) throw new Error('获取印刷图案详情失败')
  return await res.json()
}

export const createPrintPattern = async (patternData) => {
  const formData = new FormData()
  Object.keys(patternData).forEach(key => {
    if (key === 'imageFile' && patternData[key]) {
      formData.append(key, patternData[key])
    } else if (patternData[key] !== undefined && patternData[key] !== null) {
      formData.append(key, patternData[key])
    }
  })

  const res = await fetch(BASE_URL, {
    method: 'POST',
    body: formData,
  })
  if (!res.ok) throw new Error('创建印刷图案失败')
  return await res.json()
}

export const updatePrintPattern = async (id, patternData) => {
  const formData = new FormData()
  Object.keys(patternData).forEach(key => {
    if (key === 'imageFile' && patternData[key]) {
      formData.append(key, patternData[key])
    } else if (patternData[key] !== undefined && patternData[key] !== null) {
      formData.append(key, patternData[key])
    }
  })

  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    body: formData,
  })
  if (!res.ok) throw new Error('更新印刷图案失败')
  return await res.json()
}

export const deletePrintPattern = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除印刷图案失败')
  return await res.text()
} 