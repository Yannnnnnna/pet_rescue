<template>
  <view class="detail-container">
    <!-- 文章头部 -->
    <view class="article-header">
      <view class="article-title">{{ articleDetail.title }}</view>
      <view class="author-info">
        <view class="author-meta">
          <view class="author-name">{{ articleDetail.author || '官方发布' }}</view>
          <view class="publish-info">
            <text>{{ formatTime(articleDetail.createTime) }}</text>
            <text class="separator">·</text>
            <text>{{ formatViewCount(articleDetail.viewCount) }}阅读</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 正文区域 -->
    <view class="article-content">
      <u-parse :content="articleDetail.content" :tag-style="tagStyle"></u-parse>
    </view>

    <!-- 底部占位，防止内容被操作栏遮挡 -->
    <view class="bottom-placeholder"></view>

    <!-- 底部悬浮操作栏 -->
    <view class="action-bar">
      <view class="action-item" @click="handleFavorite">
        <u-icon 
          :name="isFavorited ? 'shoucang-fill' : 'shoucang'" 
          custom-prefix="custom-icon"
          :color="isFavorited ? '#ff9500' : '#999'" 
          size="24"
        ></u-icon>
        <text :class="{ active: isFavorited }">收藏</text>
      </view>
      <view class="action-item" @click="handleLike">
        <u-icon 
          :name="articleDetail.isLiked ? 'dianzan-fill' : 'dianzan'" 
          custom-prefix="custom-icon"
          :color="articleDetail.isLiked ? '#19be6b' : '#999'" 
          size="24"
        ></u-icon>
        <text :class="{ active: articleDetail.isLiked }">{{ articleDetail.likeCount }}</text>
      </view>
      <view class="action-item">
        <button open-type="share" class="share-btn">
          <u-icon name="fenxiang" custom-prefix="custom-icon" color="#999" size="24"></u-icon>
          <text>分享</text>
        </button>
      </view>
    </view>

    <!-- AI 辅助浮窗 -->
    <view class="ai-float-dialog" @click="handleAISummary">
      <view class="dialog-content">
        <text class="dialog-text">AI 帮我总结</text>
        <view class="dialog-arrow"></view>
      </view>
      <view class="avatar-wrapper">
        <image src="/static/ai-robot.png" class="ai-avatar-img" mode="aspectFit"></image>
      </view>
    </view>

    <!-- AI 总结弹窗 -->
    <u-popup :show="showAISummary" mode="center" round="16" :closeable="true" @close="showAISummary = false" :customStyle="{width: '600rpx'}">
      <view class="ai-summary-popup">
        <view class="popup-title">
          <u-icon name="jiqiren" custom-prefix="custom-icon" color="#19be6b" size="24" style="margin-right: 10rpx;"></u-icon>
          AI 智能总结
        </view>
        
        <view class="popup-content">
          <view v-if="aiLoading" class="loading-state">
            <u-loading-icon mode="circle" size="36" color="#19be6b"></u-loading-icon>
            <text class="loading-text">正在阅读文章并生成总结...</text>
          </view>
          
          <view v-else class="summary-text">
            <u-parse :content="renderMarkdown(aiSummary)"></u-parse>
          </view>
        </view>
        
        <view class="popup-footer" v-if="!aiLoading">
          <button class="regenerate-btn" @click="handleRegenerateSummary">
            <u-icon name="shuaxin" custom-prefix="custom-icon" color="#666" size="16"></u-icon>
            <text>重新生成</text>
          </button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getArticleDetail, toggleLike, checkFavorite, toggleFavorite } from '@/api/article'
import { getArticleSummary } from '@/api/ai'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({ html: true, breaks: true, linkify: true })
const renderMarkdown = (text) => text ? md.render(text) : ''

const articleId = ref(null)
const articleDetail = ref({
  id: 0,
  title: '',
  summary: '',
  tags: '',
  type: 0,
  category: '',
  coverImg: '',
  content: '',
  author: '',
  viewCount: 0,
  isSynced: 0,
  createTime: '',
  updateTime: '',
  isDeleted: 0,
  likeCount: 0,
  isLiked: false
})

const isFavorited = ref(false)
const showAISummary = ref(false)
const aiLoading = ref(false)
const aiSummary = ref('')

const tagStyle = {
  h2: 'font-size: 36rpx; font-weight: bold; color: #333; margin: 40rpx 0 20rpx; line-height: 1.4;',
  p: 'font-size: 28rpx; color: #666; line-height: 1.8; margin: 20rpx 0; text-align: justify;',
  img: 'width: 100%; height: auto; margin: 20rpx 0; border-radius: 12rpx;'
}

onLoad((options) => {
  if (options.id) {
    articleId.value = options.id
    loadArticleDetail()
    checkFavoriteStatus()
  }
})

onShareAppMessage(() => {
  return {
    title: '这篇文章很有用，推荐给你！',
    path: '/pages/wiki/detail?id=' + articleId.value,
    imageUrl: articleDetail.value.coverImg
  }
})

onShareTimeline(() => {
  return {
    title: articleDetail.value.title,
    query: 'id=' + articleId.value
  }
})

const loadArticleDetail = async () => {
  try {
    const res = await getArticleDetail(articleId.value)
    if (res.data) {
      articleDetail.value = res.data
    }
  } catch (error) {
    console.error('加载文章详情失败', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(articleId.value)
    if (res.data) {
      isFavorited.value = true
    }
  } catch (error) {
    console.error('检查收藏状态失败', error)
  }
}

const handleFavorite = async () => {
  try {
    const res = await toggleFavorite(articleId.value)
    isFavorited.value = res.data === true
    uni.showToast({
      title: isFavorited.value ? '已收藏' : '已取消收藏',
      icon: 'success'
    })
  } catch (error) {
    console.error('收藏操作失败', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

const handleLike = async () => {
  try {
    const res = await toggleLike(articleId.value)
    articleDetail.value.isLiked = res.data === 1
    if (articleDetail.value.isLiked) {
      articleDetail.value.likeCount++
    } else {
      articleDetail.value.likeCount--
    }
    uni.showToast({
      title: articleDetail.value.isLiked ? '已点赞' : '已取消点赞',
      icon: 'success'
    })
  } catch (error) {
    console.error('点赞操作失败', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

const handleAISummary = () => {
  showAISummary.value = true
  // 如果没有总结过，则自动开始总结
  if (!aiSummary.value) {
    generateSummary()
  }
}

const handleRegenerateSummary = () => {
  generateSummary(true)
}

const generateSummary = async (refresh = false) => {
  aiLoading.value = true
  try {
    const res = await getArticleSummary(articleId.value, refresh)
    if (res.code === 200 || res.code === 0) {
      // 解析 JSON 字符串 (假设返回的是类似推荐接口的结构，或者是纯文本)
      // 如果后端直接返回文本，则直接使用
      // 这里根据示例响应 "data": ""，如果内容在data里
      // 假设 data 可能是 json 字符串或者直接是内容字符串
      
      let summaryContent = res.data
      
      // 尝试解析 JSON (以防万一后端包装了)
      try {
        const json = JSON.parse(res.data)
        if (json.summary) {
           summaryContent = json.summary
        } else if (typeof json === 'string') {
            summaryContent = json
        }
      } catch (e) {
        // 不是 JSON，直接使用字符串
      }
      
      aiSummary.value = summaryContent
    } else {
      uni.showToast({ title: res.msg || '生成总结失败', icon: 'none' })
    }
  } catch (error) {
    console.error('获取文章总结失败', error)
    uni.showToast({ title: '网络请求失败', icon: 'none' })
  } finally {
    aiLoading.value = false
  }
}

const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const publishTime = new Date(time)
  const diff = Math.floor((now - publishTime) / 1000 / 60 / 60 / 24)
  
  if (diff === 0) {
    return '今天'
  } else if (diff === 1) {
    return '1天前'
  } else if (diff < 7) {
    return diff + '天前'
  } else {
    return publishTime.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
  }
}
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background: linear-gradient(to bottom, #e6f2e6, #f5f5f5);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 文章头部 */
.article-header {
  background: #e6f2e6;
  padding: 40rpx 30rpx 30rpx;
  margin-bottom: 0;

  .article-title {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
    margin-bottom: 30rpx;
  }

  .author-info {
    display: flex;
    align-items: center;

    .author-meta {
      flex: 1;

      .author-name {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 8rpx;
      }

      .publish-info {
        font-size: 24rpx;
        color: #666;

        .separator {
          margin: 0 8rpx;
        }
      }
    }
  }
}

/* 正文区域 */
.article-content {
  background: #f5f5f5;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  line-height: 1.8;
}

/* 底部占位 */
.bottom-placeholder {
  height: 120rpx;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20rpx 0;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 100;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;

    .share-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6rpx;
      background: transparent;
      border: none;
      padding: 0;
      margin: 0;
      line-height: 1;

      &::after {
        border: none;
      }
    }

    text {
      font-size: 22rpx;
      color: #999;

      &.active {
        color: #19be6b;
      }
    }
  }
}

/* AI 悬浮对话框 */
.ai-float-dialog {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx; // 高于底部操作栏
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  
  .dialog-content {
    background: linear-gradient(135deg, #19be6b, #28d07e);
    padding: 12rpx 24rpx;
    border-radius: 32rpx 32rpx 4rpx 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(25, 190, 107, 0.3);
    margin-bottom: 10rpx;
    position: relative;
    animation: float 3s ease-in-out infinite;
    
    .dialog-text {
      color: #fff;
      font-size: 26rpx;
      font-weight: bold;
    }
    
    .dialog-arrow {
      position: absolute;
      bottom: -10rpx;
      right: 30rpx;
      width: 0;
      height: 0;
      border-left: 10rpx solid transparent;
      border-right: 10rpx solid transparent;
      border-top: 12rpx solid #28d07e;
    }
  }
  
  .avatar-wrapper {
    width: 90rpx;
    height: 90rpx;
    background: #fff;
    border-radius: 50%;
    padding: 10rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    
    .ai-avatar-img {
      width: 100%;
      height: 100%;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

/* AI 总结弹窗 */
.ai-summary-popup {
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  
  .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    text-align: center;
    margin-bottom: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .popup-content {
    min-height: 200rpx;
    max-height: 60vh;
    overflow-y: auto;
    
    .loading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60rpx 0;
      
      .loading-text {
        margin-top: 20rpx;
        font-size: 28rpx;
        color: #666;
      }
    }
    
    .summary-text {
      font-size: 28rpx;
      color: #333;
      line-height: 1.8;
      text-align: justify;
    }
  }
  
  .popup-footer {
    margin-top: 30rpx;
    display: flex;
    justify-content: center;
    
    .regenerate-btn {
      display: flex;
      align-items: center;
      gap: 10rpx;
      background: #f5f5f5;
      color: #666;
      font-size: 26rpx;
      padding: 10rpx 30rpx;
      border-radius: 30rpx;
      border: none;
      
      &:after {
        border: none;
      }
      
      &:active {
        background: #eee;
      }
    }
  }
}
</style>
