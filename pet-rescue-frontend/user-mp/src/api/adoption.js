import request from '@/utils/request'

// 查询我提交的申请
export const getMyAdoptionApplications = (params) => {
    return request.get('/pet-adoption/my/apply', { params })
}

// 获取某只宠物的成功领养人信息
export const getAdopterInfo = (petId) => {
    return request.get(`/pet-adoption/adopter/${petId}`)
}

// 查询谁申请了我的宠物 (我是送养人)
export const getMyReceivedApplications = () => {
    return request.get('/pet-adoption/my/receive')
}

// 获取申请详情 (用于审核查看)
export const getAdoptionApplicationDetail = (id) => {
    return request.get(`/pet-adoption/detail/${id}`)
}

// 送养人审核申请
export const auditAdoptionApplication = (data) => {
    return request.post('/pet-adoption/audit', data)
}
