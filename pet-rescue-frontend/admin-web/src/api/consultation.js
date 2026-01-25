import request from '../utils/request'

// 查询我发起的提问 (领养人视角)
export const getMyAskedConsultations = () => {
  return request({
    url: '/pet-consultation/my/asked',
    method: 'get'
  })
}

// 管理员-获取某只宠物的所有咨询用户列表
export const getConsultationSummary = (petId) => {
  return request({
    url: '/pet-consultation/admin/summary',
    method: 'get',
    params: { petId }
  })
}

// 管理员-获取特定宠物的沟通记录
export const getConsultationHistory = (petId, applicantId) => {
  return request({
    url: '/pet-consultation/admin/history',
    method: 'get',
    params: { petId, applicantId }
  })
}
