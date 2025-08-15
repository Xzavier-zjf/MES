// 上传图片文件
export const uploadImage = async (file) => {
  try {
    console.log('开始上传文件:', file.name, file.type, file.size)
    
    // 文件验证
    if (!file) {
      throw new Error('未选择文件')
    }
    
    // 文件类型验证
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
    if (!allowedTypes.includes(file.type)) {
      throw new Error(`不支持的文件类型: ${file.type}`)
    }
    
    // 文件大小验证 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      throw new Error(`文件大小超过限制，当前: ${(file.size / 1024 / 1024).toFixed(2)}MB，最大: 5MB`)
    }
    
    const formData = new FormData()
    formData.append('file', file)
    
    // 尝试多个可能的后端端口
    const possibleUrls = [
      'http://localhost:8103/api/upload/image', // 直接连接到process service
      'http://localhost:8080/api/upload/image', // 通过gateway
      'http://localhost:8081/api/upload/image',
      'http://localhost:9001/api/upload/image'
    ]
    
    let lastError = null
    
    for (const url of possibleUrls) {
      try {
        console.log('尝试上传到:', url)
        
        const response = await fetch(url, {
          method: 'POST',
          body: formData,
          // 不设置Content-Type，让浏览器自动设置multipart/form-data边界
        })
        
        if (!response.ok) {
          const errorText = await response.text()
          console.warn(`上传到 ${url} 失败: ${response.status} ${response.statusText}`)
          lastError = new Error(`上传失败: ${response.status} ${response.statusText} - ${errorText}`)
          continue
        }
        
        const result = await response.json()
        console.log('图片上传成功:', result)
        
        // 确保返回的URL格式正确
        if (result.success && result.url) {
          // 标准化URL格式，确保以/开头
          if (!result.url.startsWith('/')) {
            result.url = '/' + result.url
          }
          console.log('标准化后的图片URL:', result.url)
          
          // 为编辑模式添加额外的验证
          if (result.url) {
            console.log('✅ 图片上传成功，可用于编辑模式替换')
          }
        }
        
        return result
        
      } catch (error) {
        console.warn(`连接到 ${url} 失败:`, error.message)
        lastError = error
        continue
      }
    }
    
    // 如果所有URL都失败了，抛出最后一个错误
    throw lastError || new Error('所有上传端点都无法连接')
    
  } catch (error) {
    console.error('图片上传失败:', error)
    throw error
  }
}

// 获取图片URL
export const getImageUrl = (path) => {
  if (!path) return ''
  
  console.log('处理图片路径:', path)
  
  // 如果是完整的URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    console.log('返回完整URL:', path)
    return path
  }
  
  // 尝试多个可能的后端端口
  const possiblePorts = [8080, 8081, 9001]
  const defaultPort = 8080 // 默认使用8080端口
  
  const baseUrl = `http://localhost:${defaultPort}`
  let fullUrl = ''
  
  // 处理路径，确保正确的格式
  let processedPath = path
  if (path.startsWith('/uploads/')) {
    // 直接使用API路径访问
    processedPath = path
  } else if (path.startsWith('/')) {
    // 以/开头的路径，直接使用
    processedPath = path
  } else {
    // 不以/开头的路径，添加/uploads/patterns/前缀
    processedPath = `/uploads/patterns/${path}`
  }
  
  // 构建完整URL，智能处理文件名编码
  const pathParts = processedPath.split('/')
  const encodedParts = pathParts.map((part, index) => {
    // 只对文件名部分进行编码，不对路径分隔符编码
    if (index === pathParts.length - 1 && part) {
      // 检查文件名是否已经被URL编码
      try {
        const decoded = decodeURIComponent(part)
        // 如果解码成功且与原始不同，说明已经编码过了
        if (decoded !== part) {
          console.log('文件名已编码，直接使用:', part)
          return part
        }
      } catch (e) {
        // 解码失败，说明不是有效的URL编码
      }
      
      // 对文件名进行URL编码
      const encoded = encodeURIComponent(part)
      console.log('文件名编码:', part, '->', encoded)
      return encoded
    }
    return part
  })
  
  const encodedPath = encodedParts.join('/')
  fullUrl = `${baseUrl}/api${encodedPath}`
  
  console.log('生成的完整URL:', fullUrl)
  console.log('原始路径:', path, '-> 处理后路径:', processedPath, '-> 编码后路径:', encodedPath)
  
  return fullUrl
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