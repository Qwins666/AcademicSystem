import request from './request'

// 获取所有项目
export function getAllProjects(params = {}) {
  return request({
    url: '/projects',
    method: 'get',
    params
  })
}

// 获取项目详情
export function getProjectById(id) {
  return request({
    url: `/projects/${id}`,
    method: 'get'
  })
}

// 创建项目
export function createProject(data) {
  return request({
    url: '/projects',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(id, data) {
  return request({
    url: `/projects/${id}`,
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id) {
  return request({
    url: `/projects/${id}`,
    method: 'delete'
  })
}

// 根据负责人ID获取项目
export function getProjectsByLeaderId(leaderId) {
  return request({
    url: `/projects/leader/${leaderId}`,
    method: 'get'
  })
}

// 根据成员ID获取项目
export function getProjectsByMemberId(memberId) {
  return request({
    url: `/projects/member/${memberId}`,
    method: 'get'
  })
}

// 添加项目成员
export function addProjectMember(projectId, userId) {
  return request({
    url: `/projects/${projectId}/members`,
    method: 'post',
    data: { memberId: userId, role: 'MEMBER' } // 使用memberId参数名并添加role参数
  })
}

// 移除项目成员
export function removeProjectMember(projectId, userId) {
  return request({
    url: `/projects/${projectId}/members/${userId}`,
    method: 'delete'
  })
}

// 获取项目成员列表
export function getProjectMembers(projectId) {
  return request({
    url: `/projects/${projectId}/members`,
    method: 'get'
  })
}

// 根据类型获取项目
export function getProjectsByType(type) {
  return request({
    url: `/projects/type/${type}`,
    method: 'get'
  })
}

// 根据状态获取项目
export function getProjectsByStatus(status) {
  return request({
    url: `/projects/status/${status}`,
    method: 'get'
  })
}

// 添加项目里程碑
export function addProjectMilestone(projectId, data) {
  return request({
    url: `/projects/${projectId}/milestones`,
    method: 'post',
    data
  })
}

// 更新项目里程碑
export function updateProjectMilestone(projectId, milestoneId, data) {
  return request({
    url: `/projects/${projectId}/milestones/${milestoneId}`,
    method: 'put',
    data
  })
}

// 删除项目里程碑
export function deleteProjectMilestone(projectId, milestoneId) {
  return request({
    url: `/projects/${projectId}/milestones/${milestoneId}`,
    method: 'delete'
  })
}

// 获取项目里程碑列表
export function getProjectMilestones(projectId) {
  return request({
    url: `/projects/${projectId}/milestones`,
    method: 'get'
  })
}

// 上传项目文件
export function uploadProjectFile(projectId, file, milestoneId, milestoneKey, uploaderId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('milestoneId', milestoneId || '')
  formData.append('milestoneKey', milestoneKey || '')
  formData.append('uploaderId', uploaderId)
  return request({
    url: `/projects/${projectId}/files`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取项目文件列表
export function getProjectFiles(projectId) {
  return request({
    url: `/projects/${projectId}/files`,
    method: 'get'
  })
}

// 删除项目文件
export function deleteProjectFile(projectId, fileId) {
  return request({
    url: `/projects/${projectId}/files/${fileId}`,
    method: 'delete'
  })
}

// 更改项目状态
export function updateProjectStatus(projectId, status) {
  return request({
    url: `/projects/${projectId}/status`,
    method: 'put',
    data: { status }
  })
}

// 获取项目负责人详情
export function getUserDetailAtProject(id) {
  return request({
    url: `/projects/leaderDetail/${id}`,
    method: 'get'
  })
}

// 获取可用学生列表（教师可访问）
export function getAvailableStudents() {
  return request({
    url: '/projects/available-students',
    method: 'get'
  })
}