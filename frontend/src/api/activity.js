import request from './request'

// 获取活动列表
export function getActivityList(params = {}) {
  return request({
    url: '/activities',
    method: 'get',
    params
  })
}

// 获取活动详情
export function getActivityById(id) {
  return request({
    url: `/activities/${id}`,
    method: 'get'
  })
}

// 创建活动
export function createActivity(data) {
  return request({
    url: '/activities',
    method: 'post',
    data
  })
}

// 更新活动
export function updateActivity(id, data) {
  return request({
    url: `/activities/${id}`,
    method: 'put',
    data
  })
}

// 删除活动
export function deleteActivity(id) {
  return request({
    url: `/activities/${id}`,
    method: 'delete'
  })
}

// 根据状态获取活动列表
export function getActivitiesByStatus(status) {
  return request({
    url: `/activities/status/${status}`,
    method: 'get'
  })
}

// 报名参加活动
export function joinActivity(activityId) {
  return request({
    url: `/activities/${activityId}/register`,
    method: 'post',
    data: {} // 添加空对象作为data参数，确保请求格式正确
  })
}

// 取消活动报名
export function cancelJoinActivity(activityId) {
  return request({
    url: `/activities/${activityId}/cancel-register`,
    method: 'post'
  })
}

// 生成参与证明
export function generateCertificate(activityId) {
  return request({
    url: `/activities/${activityId}/certificate`,
    method: 'get'
  })
}

// 下载参与名单
export function downloadParticipants(activityId, type = 'excel') {
  return request({
    url: `/activities/${activityId}/participants/export`,
    method: 'get',
    params: {
      type: type
    },
    responseType: 'blob' // 重要：设置响应类型为blob
  })
}