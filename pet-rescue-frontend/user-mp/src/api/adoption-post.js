import request from '@/utils/request'

export const getAdoptionPostList = (params) => {
  return request.get('/adoption-post/list', { params })
}

export const getAdoptionPostDetail = (postId) => {
  return request.get(`/adoption-post/${postId}`)
}

export const addAdoptionPost = (data) => {
  return request.post('/adoption-post/add', data)
}

export const updateAdoptionPost = (data) => {
  return request.put('/adoption-post/add', data)
}

export const deleteAdoptionPost = (postId) => {
  return request.delete(`/adoption-post/${postId}`)
}

export const likePost = (postId) => {
  return request.post(`/post-like/like/${postId}`)
}

export const checkPostLike = (postId) => {
  return request.get('/post-like/check', { postId })
}
