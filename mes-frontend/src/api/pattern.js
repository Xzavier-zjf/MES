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
  console.log('创建图案请求数据:', patternData)
  
  // 提取非文件数据，构建JSON请求体
  const jsonData = {}
  Object.keys(patternData).forEach(key => {
    if (key !== 'imageFile' && patternData[key] !== undefined && patternData[key] !== null) {
      jsonData[key] = patternData[key]
    }
  })

  console.log('发送JSON数据:', jsonData)

  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(jsonData)
  })
  
  if (!res.ok) {
    const errorText = await res.text()
    console.error('创建印刷图案失败:', { status: res.status, statusText: res.statusText, errorText })
    throw new Error(`创建印刷图案失败: ${res.status} ${res.statusText} - ${errorText}`)
  }
  
  // 检查响应体是否为空
  const text = await res.text()
  if (!text) {
    throw new Error('创建印刷图案失败：后端返回空响应')
  }
  
  try {
    return JSON.parse(text)
  } catch (error) {
    console.warn('后端返回非JSON格式响应:', text)
    throw new Error('创建印刷图案失败：响应格式错误')
  }
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

  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(jsonData)
  })
  
  console.log('更新请求响应状态:', res.status, res.statusText)
  
  if (!res.ok) {
    const errorText = await res.text()
    console.error('更新印刷图案失败:', { status: res.status, statusText: res.statusText, errorText })
    throw new Error(`更新印刷图案失败: ${res.status} ${res.statusText} - ${errorText}`)
  }
  
  // 检查响应体
  const text = await res.text()
  console.log('后端响应内容:', text || '(空响应)')
  
  if (!text) {
    // 如果后端返回空响应，返回我们发送的数据作为确认
    console.log('后端返回空响应，使用发送的数据作为更新确认')
    return {
      success: true,
      message: '图案更新成功',
      data: jsonData // 返回我们发送的数据
    }
  }
  
  try {
    const responseData = JSON.parse(text)
    console.log('解析后的响应数据:', responseData)
    return responseData
  } catch (error) {
    console.warn('后端返回非JSON格式响应，但更新可能成功:', text)
    // 即使响应格式不正确，也返回我们发送的数据
    return {
      success: true,
      message: '图案更新成功',
      data: jsonData,
      rawResponse: text
    }
  }
}

export const deletePrintPattern = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除印刷图案失败')
  return await res.text()
} 