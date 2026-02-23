<template>
  <view class="detail-container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">文章详情</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="article-header" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
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

    <view class="article-content">
      <u-parse :content="articleDetail.content" :tag-style="tagStyle"></u-parse>
    </view>

    <view class="bottom-placeholder"></view>

    <view class="action-bar">
      <view class="action-item" @click="handleFavorite">
        <uni-icons 
          :type="isFavorited ? 'star-filled' : 'star'" 
          size="22"
          :color="isFavorited ? '#FFC107' : '#9CA3AF'"
        ></uni-icons>
        <text :class="{ active: isFavorited }">收藏</text>
      </view>
      <view class="action-item" @click="handleLike">
        <uni-icons 
          :type="articleDetail.isLiked ? 'hand-up-filled' : 'hand-up'" 
          size="22"
          :color="articleDetail.isLiked ? '#2E7D32' : '#9CA3AF'"
        ></uni-icons>
        <text :class="{ active: articleDetail.isLiked }">{{ articleDetail.likeCount }}</text>
      </view>
      <view class="action-item">
        <button open-type="share" class="share-btn">
          <uni-icons type="redo" size="22" color="#9CA3AF"></uni-icons>
          <text>分享</text>
        </button>
      </view>
    </view>

    <view class="ai-float-dialog" @click="handleAISummary">
      <view class="dialog-content">
        <text class="dialog-text">AI 帮我总结</text>
        <view class="dialog-arrow"></view>
      </view>
      <view class="avatar-wrapper">
        <image src="../../static/AI助手图标.png" class="ai-avatar-img" mode="aspectFit"></image>
      </view>
    </view>

    <u-popup :show="showAISummary" mode="center" round="16" :closeable="true" @close="showAISummary = false" :customStyle="{width: '600rpx'}">
      <view class="ai-summary-popup">
        <view class="popup-title">
          <uni-icons type="fire" size="24" color="#2E7D32" style="margin-right: 10rpx;"></uni-icons>
          AI 智能总结
        </view>
        
        <view class="popup-content">
          <view v-if="aiLoading" class="loading-state">
            <u-loading-icon mode="circle" size="36" color="#2E7D32"></u-loading-icon>
            <text class="loading-text">正在阅读文章并生成总结...</text>
          </view>
          
          <view v-else class="summary-text">
            <u-parse :content="renderMarkdown(aiSummary)"></u-parse>
          </view>
        </view>
        
        <view class="popup-footer" v-if="!aiLoading">
          <button class="regenerate-btn" @click="handleRegenerateSummary">
            <uni-icons type="refresh" size="16" color="#6B7280"></uni-icons>
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

const statusBarHeight = ref(44)

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
  h2: 'font-size: 36rpx; font-weight: bold; color: #1F2937; margin: 40rpx 0 20rpx; line-height: 1.4;',
  p: 'font-size: 28rpx; color: #4B5563; line-height: 1.8; margin: 20rpx 0; text-align: justify;',
  img: 'width: 100%; height: auto; margin: 20rpx 0; border-radius: 12rpx;'
}

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  if (options.id) {
    articleId.value = options.id
    loadArticleDetail()
    checkFavoriteStatus()
  }
})

const goBack = () => {
  uni.navigateBack()
}

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
      let summaryContent = res.data
      
      try {
        const json = JSON.parse(res.data)
        if (json.summary) {
           summaryContent = json.summary
        } else if (typeof json === 'string') {
            summaryContent = json
        }
      } catch (e) {
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
  background-color: #F9FAFB;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  border-bottom: 1rpx solid #f0f0f0;
  
  .nav-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
  }
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #1F2937;
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.article-header {
  background: #fff;
  padding: 40rpx 30rpx 30rpx;
  margin-bottom: 20rpx;
}

.article-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #1F2937;
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
      color: #1F2937;
      margin-bottom: 8rpx;
    }

    .publish-info {
      font-size: 24rpx;
      color: #6B7280;

      .separator {
        margin: 0 8rpx;
      }
    }
  }
}

.article-content {
  background: #fff;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  line-height: 1.8;
}

.bottom-placeholder {
  height: 120rpx;
}

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
      color: #9CA3AF;

      &.active {
        color: #2E7D32;
      }
    }
  }
}

.ai-float-dialog {
  position: fixed;
  right: 30rpx;
  bottom: 200rpx;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  
  .dialog-content {
    background: linear-gradient(135deg, #2E7D32, #43A047);
    padding: 12rpx 24rpx;
    border-radius: 32rpx 32rpx 4rpx 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(46, 125, 50, 0.3);
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
      border-top: 12rpx solid #43A047;
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

.ai-summary-popup {
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  
  .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #1F2937;
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
        color: #6B7280;
      }
    }
    
    .summary-text {
      font-size: 28rpx;
      color: #1F2937;
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
      background: #F3F4F6;
      color: #6B7280;
      font-size: 26rpx;
      padding: 10rpx 30rpx;
      border-radius: 30rpx;
      border: none;
      
      &:after {
        border: none;
      }
      
      &:active {
        background: #E5E7EB;
      }
    }
  }
}
</style>
