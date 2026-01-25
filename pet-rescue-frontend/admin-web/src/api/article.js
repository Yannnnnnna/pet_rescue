import request from '../utils/request'

export const getArticleList = (data) => {
  return request({
    url: '/cms-article/list',
    method: 'post',
    data
  })
}

export const getArticleDetail = (id) => {
  return request({
    url: `/cms-article/${id}`,
    method: 'get'
  })
}

export const saveArticle = (data) => {
  return request({
    url: '/cms-article/save',
    method: 'post',
    data
  })
}

// 删除文章
export const deleteArticle = (id) => {
  return request({
    url: `/cms-article/delete/${id}`,
    method: 'delete'
  })
}
