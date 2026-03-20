// src/api/pattern.js
import request from './request'

const BASE_URL = '/api/v1/process/print-patterns'

export const getPrintPatterns = async (params = {}) => {
  const searchParams = new URLSearchParams()
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 20)
  searchParams.append('sort', params.sort || 'id,desc')

  return await request.get(`${BASE_URL}/all?${searchParams.toString()}`)
}

export const getPrintPatternById = async (id) => {
  return await request.get(`${BASE_URL}/${id}`)
}

export const createPrintPattern = async (patternData) => {
  console.log('创建图案请求数据:', patternData)
  
  // 提取非文件数据，构建JSON请求体
  const jsonData = {}
  Object.keys(patternData).forEach(key => {
    if (key !== 'imageFile' && patternData[key] !== undefined && patternData[key] !== null) {
      jsonData[key] = patternData[key]
    }
  })

  console.log('发送JSON数据:', jsonData)

  return await request.post(BASE_URL, jsonData)
}

export const updatePrintPattern = async (id, patternData) => {
  console.log('更新图案请求数据:', { id, patternData })
  
  // 提取非文件数据，构建JSON请求体
  const jsonData = {}
  Object.keys(patternData).forEach(key => {
    if (key !== 'imageFile' && patternData[key] !== undefined && patternData[key] !== null) {
      jsonData[key] = patternData[key]
    }
  })

  console.log('发送到后端的JSON数据:', jsonData)

  return await request.put(`${BASE_URL}/${id}`, jsonData)
}

export const deletePrintPattern = async (id) => {
  return await request.delete(`${BASE_URL}/${id}`)
}
