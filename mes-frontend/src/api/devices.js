// src/api/devices.js
const BASE_URL = 'http://localhost:8080/api/v1/equipment/devices'

export const getDevices = async (params = {}) => {
  const searchParams = new URLSearchParams()
  if (params.name) searchParams.append('name', params.name)
  if (params.status) searchParams.append('status', params.status)
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 1000)

  const res = await fetch(`${BASE_URL}?${searchParams.toString()}`)
  if (!res.ok) throw new Error('获取设备列表失败')
  return await res.json()
}

export const getDeviceById = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`)
  if (!res.ok) throw new Error('获取设备详情失败')
  return await res.json()
}

export const createDevice = async (deviceData) => {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(deviceData),
  })
  if (!res.ok) throw new Error('创建设备失败')
  return await res.json()
}

export const updateDeviceStatus = async (id, status) => {
  const res = await fetch(`${BASE_URL}/${id}/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  })
  if (!res.ok) throw new Error('更新设备状态失败')
  return await res.json()
}

export const updateDevice = async (id, deviceData) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(deviceData),
  })
  if (!res.ok) throw new Error('更新设备失败')
  return await res.json()
}

export const deleteDevice = async (id) => {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('删除设备失败')
  return await res.text()
} 