import request from './request'

// 获取系统概览数据
export function getSystemOverview() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

// 获取项目总数
export function getTotalProjects() {
  return request({
    url: '/statistics/projects/total',
    method: 'get'
  })
}

// 获取项目按状态分布
export function getProjectsByStatus() {
  return request({
    url: '/statistics/projects/status',
    method: 'get'
  })
}

// 获取项目按类型分布
export function getProjectsByType() {
  return request({
    url: '/statistics/projects/type',
    method: 'get'
  })
}

// 获取项目趋势数据
export function getProjectTrend(startDate, endDate, period = 'month') {
  return request({
    url: '/statistics/projects/trend',
    method: 'get',
    params: {
      startDate,
      endDate,
      period
    }
  })
}

// 获取活动总数
export function getTotalActivities() {
  return request({
    url: '/statistics/activities/total',
    method: 'get'
  })
}

// 获取活动按类型分布
export function getActivitiesByType() {
  return request({
    url: '/statistics/activities/type',
    method: 'get'
  })
}

// 获取活动参与总人数
export function getTotalParticipants() {
  return request({
    url: '/statistics/activities/participants',
    method: 'get'
  })
}

// 获取活动趋势数据
export function getActivityTrend(startDate, endDate, period = 'month') {
  return request({
    url: '/statistics/activities/trend',
    method: 'get',
    params: {
      startDate,
      endDate,
      period
    }
  })
}

//获取用户总数
export function getTotalUsers() {
  return request({
    url: '/statistics/users/total',
    method: 'get'
  })
}

// 获取成果趋势数据
export function getAchievementTrend(startDate, endDate, type = null, period = 'month') {
  return request({
    url: '/statistics/achievements/trend',
    method: 'get',
    params: {
      startDate,
      endDate,
      type,
      period
    }
  })
}

// 获取成果分布数据
export function getAchievementDistribution(startDate, endDate, dimension = 'type') {
  return request({
    url: '/statistics/achievement-distribution',
    method: 'get',
    params: {
      startDate,
      endDate,
      dimension
    }
  })
}

// 获取审核状态分布数据
export function getApprovalStatusDistribution(startDate, endDate, type = null) {
  return request({
    url: '/statistics/approval-status',
    method: 'get',
    params: {
      startDate,
      endDate,
      type
    }
  })
}

// 获取活动报名率统计数据
export function getActivityParticipation(startDate, endDate, activityType = null) {
  return request({
    url: '/statistics/activity-participation',
    method: 'get',
    params: {
      startDate,
      endDate,
      activityType
    }
  })
}