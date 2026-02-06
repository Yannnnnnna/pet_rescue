import request from '@/utils/request'

export function getArticleList(params) {
  return request.post('/cms-article/list', {
    pageNum: params.pageNum || 1,
    pageSize: params.pageSize || 10,
    keyword: params.keyword || '',
    type: params.type || 0,
    category: params.category || ''
  })
}

export function getArticleDetail(id) {
  return request.get(`/cms-article/${id}`)
}

export function toggleLike(articleId) {
  return request.post('/cms-article-like/like', { articleId })
}

export function checkFavorite(articleId) {
  return request.get('/cms-article-favorite/check', { params: { articleId } })
}

export function toggleFavorite(articleId) {
  return request.post('/cms-article-favorite/toggle', { articleId })
}

export function getMyFavoriteArticles(type) {
  return request.get('/cms-article-favorite/my', { params: { type } })
}
