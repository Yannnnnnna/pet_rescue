<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">咨询中心</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <u-list @scrolltolower="loadMore" v-if="userList.length > 0">
        <u-list-item v-for="(item, index) in userList" :key="index">
          <view class="user-card" @click="handleUserClick(item)">
            <view class="left-section">
              <image :src="item.askUserAvatar || '/static/logo.png'" mode="aspectFill" class="avatar"></image>
              <u-badge :value="item.msgCount" absolute :offset="[-5, -5]" type="error" v-if="item.msgCount > 0"></u-badge>
            </view>
            
            <view class="right-section">
              <view class="header">
                <text class="nickname">{{ item.askUserNickname || '用户' + item.askUserId }}</text>
                <text class="time">{{ formatTime(item.lastTime) }}</text>
              </view>
              <view class="message">{{ item.lastMessage || '暂无消息' }}</view>
            </view>
          </view>
        </u-list-item>
      </u-list>
      <u-empty v-else mode="message" text="暂无咨询记录" marginTop="100"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getConsultationSummary } from '@/api/consultation'
import dayjs from 'dayjs'

const statusBarHeight = ref(44)
const petId = ref(null)
const userList = ref([])

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  if (options.petId) {
    petId.value = options.petId
  }
})

onShow(() => {
  if (petId.value) {
    fetchData()
  }
})

onPullDownRefresh(() => {
  if (petId.value) {
    fetchData().then(() => {
      uni.stopPullDownRefresh()
    })
  } else {
    uni.stopPullDownRefresh()
  }
})

const goBack = () => {
  uni.navigateBack()
}

const fetchData = async () => {
  uni.showLoading({ title: '加载中' })
  try {
    const res = await getConsultationSummary({ petId: petId.value })
    if (res.data) {
      userList.value = res.data
    }
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const handleUserClick = (item) => {
  uni.navigateTo({
    url: `/pages/message/chat-history?petId=${petId.value}&applicantId=${item.askUserId}`
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const loadMore = () => {
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F9FAFB;
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

.content-area {
  padding: 24rpx;
}

.user-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  
  &:active {
    background: #f9f9f9;
  }
}

.left-section {
  position: relative;
  margin-right: 28rpx;
  
  .avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 20rpx;
    background-color: #eee;
  }
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100rpx;
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .nickname {
      font-size: 32rpx;
      font-weight: 600;
      color: #1F2937;
    }
    
    .time {
      font-size: 24rpx;
      color: #9CA3AF;
    }
  }
  
  .message {
    font-size: 26rpx;
    color: #6B7280;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    margin-top: 12rpx;
  }
}
</style>
