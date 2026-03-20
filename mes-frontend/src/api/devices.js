// src/api/devices.js
import request from './request'

const BASE_URL = '/api/v1/equipment/devices'

export const getDevices = async (params = {}) => {
  const searchParams = new URLSearchParams()
  if (params.name) searchParams.append('name', params.name)
  if (params.status) searchParams.append('status', params.status)
  searchParams.append('page', params.page || 0)
  searchParams.append('size', params.size || 1000)

  return await request.get(`${BASE_URL}?${searchParams.toString()}`)
}

export const getDeviceById = async (id) => {
  return await request.get(`${BASE_URL}/${id}`)
}

export const createDevice = async (deviceData) => {
  return await request.post(BASE_URL, deviceData)
}

export const updateDeviceStatus = async (id, status) => {
  return await request.put(`${BASE_URL}/${id}/status`, { status })
}

export const updateDevice = async (id, deviceData) => {
  return await request.put(`${BASE_URL}/${id}`, deviceData)
}

export const deleteDevice = async (id) => {
  return await request.delete(`${BASE_URL}/${id}`)
}
