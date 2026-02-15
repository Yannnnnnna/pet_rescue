import request from '../utils/request'

// 列表展示领养生活记录贴
export const getAdoptionPostList = (params) => {
  return request({
    url: '/adoption-post/list',
    method: 'get',
    params
  })
}

// 审核领养生活记录贴
export const reviewAdoptionPost = (data) => {
  return request({
    url: '/adoption-post/review',
    method: 'post',
    data
  })
}

// 获取领养生活记录贴详情
export const getAdoptionPostDetail = (postId) => {
  return request({
    url: `/adoption-post/${postId}`,
    method: 'get'
  })
}
