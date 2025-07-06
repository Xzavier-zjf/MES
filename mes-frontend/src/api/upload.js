// 上传图片文件
export const uploadImage = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await fetch('http://localhost:8080/api/upload/image', {
      method: 'POST',
      body: formData
    })
    
    if (!response.ok) {
      throw new Error(`上传失败: ${response.status} ${response.statusText}`)
    }
    
    const result = await response.json()
    console.log('图片上传成功:', result)
    return result
  } catch (error) {
    console.error('图片上传失败:', error)
    throw error
  }
}

// 获取图片URL
export const getImageUrl = (path) => {
  if (!path) return ''
  
  // 如果是完整的URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  
  // 如果是相对路径，拼接基础URL
  const baseUrl = 'http://localhost:8080'
  return `${baseUrl}${path}`
}

// 验证图片URL是否有效
export const validateImageUrl = async (url) => {
  try {
    const response = await fetch(url, { method: 'HEAD' })
    return response.ok
  } catch (error) {
    console.error('图片URL验证失败:', error)
    return false
  }
} 