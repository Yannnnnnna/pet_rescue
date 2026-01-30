<template>
  <view class="container">
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
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getConsultationSummary } from '@/api/consultation'
import dayjs from 'dayjs'

const petId = ref(null)
const userList = ref([])

onLoad((options) => {
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
  // Go to chat history with this specific user
  // 这里传递 applicantId，复用 chat-history 页面
  uni.navigateTo({
    url: `/pages/message/chat-history?petId=${petId.value}&applicantId=${item.askUserId}`
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const loadMore = () => {
  // Assuming API returns all data for now
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.user-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  
  &:active {
    background: #f9f9f9;
  }
}

.left-section {
  position: relative;
  margin-right: 24rpx;
  
  .avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 12rpx;
    background-color: #eee;
  }
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100rpx;
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .nickname {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }
    
    .time {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .message {
    font-size: 26rpx;
    color: #666;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
    margin-top: 10rpx;
  }
}
</style>