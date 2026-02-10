import request from '../utils/request'

// 管理员-全平台领养记录分页
export const getAdoptionList = (params) => {
  return request({
    url: '/pet-adoption/admin/list',
    method: 'get',
    params
  })
}

// 获取申请详情
export const getAdoptionDetail = (id) => {
  return request({
    url: `/pet-adoption/detail/${id}`,
    method: 'get'
  })
}

// 审核申请
// Note: API documentation for audit was not provided, assuming standard structure.
export const auditAdoption = (data) => {
  return request({
    url: '/pet-adoption/audit',
    method: 'post',
    data
  })
}

// AI 匹配分析
export const analyzeMatch = (data) => {
  return request({
    url: '/ai/match/analyze',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
