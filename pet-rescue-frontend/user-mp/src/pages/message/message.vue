<template>
  <view class="message-page">
    <u-tabs 
      :list="tabList" 
      :current="currentTab" 
      @click="handleTabClick" 
      :activeStyle="{ color: '#dc2626', fontWeight: 'bold' }"
      lineColor="#dc2626"
    ></u-tabs>
    
    <view class="content">
      <view class="list-container" v-if="dataList.length > 0">
        <view 
          v-for="(item, index) in dataList" 
          :key="index" 
          class="session-item"
          @click="handleSessionClick(item)"
        >
          <view class="avatar-box">
             <image :src="item.avatar || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
             <u-badge :value="item.unreadCount" max="99" absolute :offset="[-10, -10]" v-if="item.unreadCount > 0"></u-badge>
          </view>
          
          <view class="info-box">
            <view class="top-row">
              <text class="title">{{ item.title || '未知用户' }}</text>
              <text class="time">{{ formatTime(item.lastTime) }}</text>
            </view>
            <view class="bottom-row">
              <text class="message">{{ item.lastMessage || '暂无消息' }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <u-empty v-else mode="message" icon="http://cdn.uviewui.com/uview/empty/message.png" text="暂无消息记录"></u-empty>
    </view>
  </view>
</template>

<script setup>
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
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 20rpx;
}

.session-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.02);
  
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
    border-radius: 12rpx;
    background: #eee;
  }
}

.info-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 90rpx;
  
  .top-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      max-width: 360rpx;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    
    .time {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .bottom-row {
    .message {
      font-size: 26rpx;
      color: #666;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 1;
      overflow: hidden;
    }
  }
}
</style>
