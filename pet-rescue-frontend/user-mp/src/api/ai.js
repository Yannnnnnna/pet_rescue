import request from '@/utils/request'

// 获取会话列表
export const getAiSessionList = () => {
  return request.get('/ai/session/list')
}

// 获取会话消息列表
export const getAiMessageList = (sessionId) => {
  return request.get('/ai/message/list', { params: { sessionId } })
}

// AI 推荐
export const getAiRecommend = (data) => {
  return request.post('/ai/recommend', data)
}

// 获取文章摘要
export const getArticleSummary = (articleId, refresh = false) => {
  return request.get('/ai/article/summary', { params: { articleId, refresh } })
}

// AI 匹配度分析
export const analyzeMatch = (data) => {
  return request.post('/ai/match/analyze', data)
}
