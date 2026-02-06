<template>
  <view class="ai-page">
    <view class="page-header">
      <text class="page-title">🤖 AI助手</text>
      <view class="add-btn" @click="handleNewChat">
        <text class="plus-icon">+</text>
        <text>新对话</text>
      </view>
    </view>
    
    <scroll-view scroll-y class="content" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing">
      <view v-if="sessionList.length === 0" class="empty-state">
        <image src="/static/empty.png" mode="aspectFit" class="empty-img" v-if="false"></image> <!-- 暂时不放图 -->
        <view class="welcome-card" @click="handleNewChat">
          <text class="welcome-emoji">👋</text>
          <text class="welcome-text">开始第一次对话</text>
          <text class="welcome-desc">点击这里或右上角按钮开始</text>
        </view>
      </view>

      <view v-else class="session-list">
        <view 
          class="session-item" 
          v-for="item in sessionList" 
          :key="item.id"
          @click="handleSessionClick(item)"
        >
          <view class="session-icon">
            <text>💬</text>
          </view>
          <view class="session-info">
            <view class="session-top">
              <text class="session-title">{{ item.title || '新对话' }}</text>
              <text class="session-time">{{ formatTime(item.updateTime || item.createTime) }}</text>
            </view>
            <text class="session-preview">点击继续对话...</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getAiSessionList } from '@/api/ai'
import dayjs from 'dayjs'
import { checkLogin } from '@/utils/auth'

const sessionList = ref([])
const isRefreshing = ref(false)

onShow(() => {
  if (checkLogin('/pages/ai/ai')) {
    loadSessionList()
  }
})

const loadSessionList = async () => {
  try {
    const res = await getAiSessionList()
    if (res.code === 200 || res.code === 0) {
      sessionList.value = res.data || []
    }
  } catch (error) {
    console.error('获取会话列表失败', error)
  } finally {
    isRefreshing.value = false
  }
}

const onRefresh = () => {
  isRefreshing.value = true
  loadSessionList()
}

const handleNewChat = () => {
  uni.navigateTo({
    url: '/pages/ai/chat'
  })
}

const handleSessionClick = (item) => {
  uni.navigateTo({
    url: `/pages/ai/chat?sessionId=${item.id}&title=${encodeURIComponent(item.title || '对话')}`
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now.subtract(1, 'day'), 'day')) {
    return '昨天'
  } else {
    return date.format('MM-DD')
  }
}
</script>

<style lang="scss" scoped>
.ai-page {
  height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 44px 20px 20px; // 适配状态栏
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02);
}

.page-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
}

.add-btn {
  background: #7c3aed;
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(124, 58, 237, 0.3);
  
  &:active {
    transform: scale(0.96);
  }
}

.plus-icon {
  font-size: 32rpx;
  font-weight: bold;
}

.content {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  height: 0; // 配合 flex: 1 确保滚动正常
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.session-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02);
  transition: all 0.2s;
  
  &:active {
    background: #fafafa;
  }
}

.session-icon {
  width: 88rpx;
  height: 88rpx;
  background: #f3e8ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    font-size: 40rpx;
  }
}

.session-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.session-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 360rpx;
}

.session-time {
  font-size: 24rpx;
  color: #999;
}

.session-preview {
  font-size: 26rpx;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.welcome-card {
  background: #fff;
  padding: 60rpx 80rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.05);
  
  &:active {
    transform: scale(0.98);
  }
}

.welcome-emoji {
  font-size: 80rpx;
  margin-bottom: 10rpx;
}

.welcome-text {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.welcome-desc {
  font-size: 26rpx;
  color: #999;
}
</style>
