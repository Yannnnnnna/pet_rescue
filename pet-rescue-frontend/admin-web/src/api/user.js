import request from '../utils/request'

export const getUserList = (data) => {
  return request({
    url: '/sys-user/list',
    method: 'post',
    data
  })
}

export const removeUser = (id) => {
  return request({
    url: `/sys-user/delete/${id}`,
    method: 'delete'
  })
}

export const getMyInfo = () => {
  return request({
    url: '/sys-user/my/info',
    method: 'get'
  })
}

export const updateUserInfo = (data) => {
  return request({
    url: '/sys-user/update/info',
    method: 'post',
    data
  })
}

export const updatePassword = (data) => {
  return request({
    url: '/sys-user/update/password',
    method: 'post',
    data
  })
}
