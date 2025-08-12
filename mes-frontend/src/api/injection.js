// src/api/injection.js
import request from './request'

const BASE_URL = '/api/v1/process/injection-params'

export const getInjectionParams = async (params = {}) => {
  const searchParams = new URLSearchParams()
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 20)
  searchParams.append('sort', params.sort || 'id,desc')

  try {
    return await request.get(`${BASE_URL}/all?${searchParams.toString()}`)
  } catch (error) {
    console.error('获取注塑参数列表失败:', error)
    throw new Error('获取注塑参数列表失败')
  }
}

export const getInjectionParamById = async (id) => {
  try {
    return await request.get(`${BASE_URL}/${id}`)
  } catch (error) {
    console.error('获取注塑参数详情失败:', error)
    throw new Error('获取注塑参数详情失败')
  }
}

export const createInjectionParam = async (paramData) => {
  try {
    console.log('发送创建请求:', paramData)
    
    // 数据验证和清理
    const cleanedData = {
      ...paramData,
      // 确保数值类型正确
      quantity: parseInt(paramData.quantity) || 0,
      pressure: paramData.pressure ? parseFloat(paramData.pressure) : null,
      injectionSpeed: paramData.injectionSpeed ? parseFloat(paramData.injectionSpeed) : null,
      holdTime: paramData.holdTime ? parseFloat(paramData.holdTime) : null,
      coolingTime: paramData.coolingTime ? parseFloat(paramData.coolingTime) : null,
      moldTemperature: paramData.moldTemperature ? parseFloat(paramData.moldTemperature) : null,
      materialTemperature: paramData.materialTemperature ? parseFloat(paramData.materialTemperature) : null
    }
    
    console.log('清理后的数据:', cleanedData)
    const response = await request.post(BASE_URL, cleanedData)
    console.log('创建响应:', response)
    return response
  } catch (error) {
    console.error('创建注塑参数失败:', error)
    
    // 提供更详细的错误信息
    if (error.response) {
      const { status, data } = error.response
      console.error('错误状态:', status)
      console.error('错误数据:', data)
      
      if (status === 400) {
        // 解析具体的错误信息
        let errorMsg = '请求参数错误'
        if (data?.message) {
          errorMsg = data.message
        } else if (typeof data === 'string') {
          errorMsg = data
        }
        throw new Error(errorMsg)
      } else if (status === 500) {
        throw new Error(data?.message || '服务器内部错误，请稍后重试')
      } else if (status === 404) {
        throw new Error('API 接口不存在，请检查服务器配置')
      }
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络连接')
    }
    
    throw new Error(error.message || '创建注塑参数失败')
  }
}

export const updateInjectionParam = async (id, paramData) => {
  try {
    console.log('发送更新请求:', { id, paramData })
    
    // 数据验证和清理，与创建时保持一致
    const cleanedData = {
      ...paramData,
      // 确保数值类型正确
      quantity: paramData.quantity ? parseInt(paramData.quantity) : undefined,
      pressure: paramData.pressure ? parseFloat(paramData.pressure) : null,
      injectionSpeed: paramData.injectionSpeed ? parseFloat(paramData.injectionSpeed) : null,
      holdTime: paramData.holdTime ? parseFloat(paramData.holdTime) : null,
      coolingTime: paramData.coolingTime ? parseFloat(paramData.coolingTime) : null,
      moldTemperature: paramData.moldTemperature ? parseFloat(paramData.moldTemperature) : null,
      materialTemperature: paramData.materialTemperature ? parseFloat(paramData.materialTemperature) : null
    }
    
    console.log('清理后的更新数据:', cleanedData)
    const response = await request.put(`${BASE_URL}/${id}`, cleanedData)
    console.log('更新响应:', response)
    return response
  } catch (error) {
    console.error('更新注塑参数失败:', error)
    
    // 提供更详细的错误信息
    if (error.response) {
      const { status, data } = error.response
      console.error('更新错误状态:', status)
      console.error('更新错误数据:', data)
      
      if (status === 400) {
        let errorMsg = '更新参数错误'
        if (data?.message) {
          errorMsg = data.message
        } else if (typeof data === 'string') {
          errorMsg = data
        }
        throw new Error(errorMsg)
      } else if (status === 404) {
        throw new Error('要更新的参数不存在')
      } else if (status === 500) {
        throw new Error(data?.message || '服务器内部错误，请稍后重试')
      }
    } else if (error.request) {
      throw new Error('网络连接失败，请检查网络连接')
    }
    
    throw new Error(error.message || '更新注塑参数失败')
  }
}

export const deleteInjectionParam = async (id) => {
  try {
    console.log('发送删除请求:', id)
    const response = await request.delete(`${BASE_URL}/${id}`)
    console.log('删除响应:', response)
    return response
  } catch (error) {
    console.error('删除注塑参数失败:', error)
    
    if (error.response) {
      const { status, data } = error.response
      if (status === 404) {
        throw new Error('要删除的参数不存在')
      } else if (status === 500) {
        throw new Error(data?.message || '服务器内部错误，请稍后重试')
      }
    }
    
    throw new Error(error.message || '删除注塑参数失败')
  }
} 