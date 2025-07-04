// src/api/auth.js
const BASE_URL = 'http://localhost:8080/auth' // 修改为你后端地址

export const login = async (username, password) => {
  const res = await fetch(`${BASE_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  if (!res.ok) throw new Error('登录失败')
  return await res.text() // 返回 token（不是 JSON）
}

export const register = async (user) => {
  const res = await fetch(`${BASE_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  })
  if (!res.ok) throw new Error('注册失败')
  return await res.text()
}
