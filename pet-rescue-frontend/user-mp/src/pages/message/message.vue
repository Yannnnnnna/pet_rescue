<template>
  <view class="message-page">
    <!-- 头部 -->
    <header class="header">
      <h1 class="title">消息中心</h1>
      <view class="tabs">
        <button 
          :class="['tab-btn', { 'active': currentTab === 0 }]" 
          @click="handleTabClick({ index: 0 })">
          我咨询的
        </button>
        <button 
          :class="['tab-btn', { 'active': currentTab === 1 }]" 
          @click="handleTabClick({ index: 1 })">
          我收到的
        </button>
      </view>
    </header>

    <!-- 消息列表 -->
    <main class="main-content">
      <scroll-view scroll-y class="list-container" v-if="dataList.length > 0">
        <view 
          v-for="(item, index) in dataList" 
          :key="index" 
          class="session-item"
          @click="handleSessionClick(item)"
        >
          <view class="avatar-box">
            <image :src="item.avatar || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
            <view v-if="item.unreadCount > 0" class="unread-dot"></view>
          </view>
          
          <view class="info-box">
            <view class="top-row">
              <view class="title-wrapper">
                <text class="title">{{ item.title || '未知用户' }}</text>
                <text v-if="item.statusText" class="status-tag">{{ item.statusText }}</text>
              </view>
              <text class="time">{{ formatTime(item.lastTime) }}</text>
            </view>
            <view class="bottom-row">
              <text class="message">{{ item.lastMessage || '暂无消息' }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
      
      <u-empty v-else mode="message" icon="http://cdn.uviewui.com/uview/empty/message.png" text="暂无消息记录"></u-empty>
    </main>

    <my-tabbar :current="1"></my-tabbar>
  </view>
</template>

<script setup>
import myTabbar from '@/components/my-tabbar/my-tabbar.vue'
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMyAskedConsultations, getMyReceivedConsultations } from '@/api/consultation'
import dayjs from 'dayjs'

const tabList = ref([
  { name: '我咨询的' },
  { name: '我收到的' }
])
const currentTab = ref(0)
const dataList = ref([])

onShow(() => {
  uni.hideTabBar()
  fetchData()
})

const handleTabClick = (item) => {
  currentTab.value = item.index
  fetchData()
}

const fetchData = async () => {
  uni.showLoading({ title: '加载中...' })
  try {
    let res = null
    if (currentTab.value === 0) {
      // 我发起的
      res = await getMyAskedConsultations()
    } else {
      // 我收到的
      res = await getMyReceivedConsultations()
    }
    
    if (res.data) {
      dataList.value = res.data
    } else {
      dataList.value = []
    }
  } catch (error) {
    console.error('获取消息列表失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const handleSessionClick = (item) => {
  // 跳转到聊天记录页
  // 需要传递 petId 和 applicantId (提问者ID)
  // 如果是 '我咨询的' (user=askUser), applicantId就是我自己(API里没直接给，但后端session里askUserId就是我)
  // 如果是 '我收到的' (user=replyUser), applicantId就是对方(askUserId)
  // 统一取 item.askUserId 即可
  
  uni.navigateTo({
    url: `/pages/message/chat-history?petId=${item.petId}&applicantId=${item.askUserId}`
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now, 'year')) {
    return date.format('MM-DD')
  } else {
    return date.format('YYYY-MM-DD')
  }
}
</script>

<style lang="scss" scoped>
.message-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f8f8;
}

.header {
  background-color: #fff;
  padding: 20rpx 30rpx 30rpx;
  border-bottom-left-radius: 30rpx;
  border-bottom-right-radius: 30rpx;

  .title {
    font-size: 48rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    text-align: left;
  }

  .tabs {
    display: flex;
    background-color: #f0f0f0;
    border-radius: 99px;
    padding: 8rpx;

    .tab-btn {
      flex: 1;
      background: transparent;
      border: none;
      padding: 16rpx 0;
      font-size: 28rpx;
      font-weight: 500;
      color: #666;
      border-radius: 99px;
      transition: all 0.3s ease;
      line-height: 1;

      &.active {
        background-color: #fff;
        color: #005339;
        font-weight: bold;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
      }
      
      &::after {
        border: none;
      }
    }
  }
}

.main-content {
  flex: 1;
  overflow: hidden;
}

.list-container {
  height: 100%;
  padding: 30rpx;
}

.session-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 24rpx;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  transition: background-color 0.2s;

  &:active {
    background-color: #fafafa;
  }
}

.avatar-box {
  position: relative;
  margin-right: 24rpx;

  .avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    background-color: #eee;
  }

  .unread-dot {
    position: absolute;
    top: 0;
    right: 0;
    width: 20rpx;
    height: 20rpx;
    background-color: #FFD700; // Sunflower Yellow for notification
    border-radius: 50%;
    border: 4rpx solid #fff;
  }
}

.info-box {
  flex: 1;
  min-width: 0;

  .top-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8rpx;

    .title-wrapper {
      display: flex;
      align-items: center;
      gap: 12rpx;
    }

    .title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .status-tag {
      background-color: #e6eee9; // primary-light
      color: #005339; // primary
      font-size: 20rpx;
      font-weight: bold;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      flex-shrink: 0;
    }

    .time {
      font-size: 24rpx;
      color: #999;
      flex-shrink: 0;
      margin-left: 10rpx;
    }
  }

  .bottom-row {
    .message {
      font-size: 28rpx;
      color: #666;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}


</style>
