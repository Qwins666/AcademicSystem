import request from './request'

// 获取成果列表
export function getAchievementList(params = {}) {
  return request({
    url: '/achievements',
    method: 'get',
    params
  })
}

// 获取成果详情
export function getAchievementById(id) {
  return request({
    url: `/achievements/${id}`,
    method: 'get'
  })
}

// 创建成果
export function createAchievement(data) {
  return request({
    url: '/achievements',
    method: 'post',
    data
  })
}

// 更新成果
export function updateAchievement(id, data) {
  return request({
    url: `/achievements/${id}`,
    method: 'put',
    data
  })
}

// 删除成果
export function deleteAchievement(id) {
  return request({
    url: `/achievements/${id}`,
    method: 'delete'
  })
}

// 根据类型获取成果
export function getAchievementsByType(type) {
  return request({
    url: `/achievements/type/${type}`,
    method: 'get'
  })
}

// 根据级别获取成果
export function getAchievementsByLevel(level) {
  return request({
    url: `/achievements/level/${level}`,
    method: 'get'
  })
}

// 获取用户成果（支持搜索和分页）
export function getUserAchievements(userId, params = {}) {
  // 解构参数，提取分页和搜索条件
  const { pageNum = 1, pageSize = 10, title, type, status } = params;
  return request({
    url: `/achievements/submitter/${userId}/search`,
    method: 'get',
    params: {
      pageNum,
      pageSize,
      title,
      type,
      status
    }
  })
}

// 上传成果文件
export function uploadAchievementFile(formData) {
  return request({
    url: '/achievements/upload-file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    transformRequest: [function(data) {
      return data
    }]
  })
}