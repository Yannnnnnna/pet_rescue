import request from '../utils/request'

// 获取轮播图列表
export const getBannerList = () => {
  return request({
    url: '/sys-banner/list',
    method: 'get'
  })
}

// 添加轮播图
export const addBanner = (data) => {
  return request({
    url: '/sys-banner/add',
    method: 'post',
    data
  })
}

// 更新轮播图
export const updateBanner = (data) => {
  return request({
    url: '/sys-banner/update',
    method: 'post',
    data
  })
}

// 删除轮播图
export const deleteBanner = (id) => {
  return request({
    url: `/sys-banner/delete/${id}`,
    method: 'delete'
  })
}
