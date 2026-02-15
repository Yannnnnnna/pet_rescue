import request from '@/utils/request'

// 获取领养生活记录贴列表
export const getAdoptionPostList = (params) => {
  return request.get('/adoption-post/list', { params })
}

// 获取领养生活记录贴详情
export const getAdoptionPostDetail = (postId) => {
  return request.get(`/adoption-post/${postId}`)
}

// 发布领养日记
export const addAdoptionPost = (data) => {
  return request.post('/adoption-post/add', data)
}

// 修改领养日记
export const updateAdoptionPost = (data) => {
  return request.put('/adoption-post/add', data)
}

// 删除领养日记
export const deleteAdoptionPost = (postId) => {
  return request.delete(`/adoption-post/${postId}`)
}

