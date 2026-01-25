import request from '../utils/request'

export const getPetList = (data) => {
  return request({
    url: '/pet-info/list',
    method: 'post',
    data
  })
}

export const addPet = (data) => {
  return request({
    url: '/pet-info/add',
    method: 'post',
    data
  })
}

export const updatePet = (data) => {
  return request({
    url: '/pet-info/update',
    method: 'post',
    data
  })
}

export const removePet = (id) => {
  return request({
    url: `/pet-info/delete/${id}`,
    method: 'delete'
  })
}

export const getPetDetail = (id) => {
  return request({
    url: `/pet-info/${id}`,
    method: 'get'
  })
}

// 预留上传接口调用，虽然通常直接在 Upload 组件的 action 中配置，
// 但如果需要手动上传，可以参考如下：
export const uploadFile = (formData) => {
  return request({
    url: '/file/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: formData
  })
}
