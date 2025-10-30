import request from './request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 更新用户角色
export function updateUserRole(id, role) {
  return request({
    url: `/users/${id}/role`,
    method: 'put',
    data: { role }
  })
}

// 获取待审批用户列表
export function getPendingUsers() {
  return request({
    url: '/auth/pending-users',
    method: 'get'
  })
}

// 审批用户
export function approveUser(id) {
  return request({
    url: `/auth/approve-user/${id}`,
    method: 'post'
  })
}

// 拒绝用户申请
export function rejectUser(id) {
  return request({
    url: `/auth/reject-user/${id}`,
    method: 'post'
  })
}

// 重置用户密码
export function resetUserPassword(id) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'put'
  })
}