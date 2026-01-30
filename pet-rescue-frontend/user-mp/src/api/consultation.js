import request from '@/utils/request'

// 获取特定宠物的沟通记录 (管理员/送养人与申请人)
export const getConsultationHistory = (params) => {
    return request.get('/pet-consultation/admin/history', { params })
}

// 回复提问
export const replyConsultation = (data) => {
    return request.post('/pet-consultation/reply', data)
}

// 发起提问
export const askConsultation = (data) => {
    return request.post('/pet-consultation/ask', data)
}

// 查询我收到的提问
export const getMyReceivedConsultations = () => {
    return request.get('/pet-consultation/my/received')
}

// 查询我发起的提问
export const getMyAskedConsultations = () => {
    return request.get('/pet-consultation/my/asked')
}

// 获取某只宠物的所有咨询用户列表 (管理员/送养人)
export const getConsultationSummary = (params) => {
    return request.get('/pet-consultation/admin/summary', { params })
}

// 发起回访 (送养人)
export const sendReturnVisit = (data) => {
    return request.post('/pet-consultation/return-visit', data)
}
