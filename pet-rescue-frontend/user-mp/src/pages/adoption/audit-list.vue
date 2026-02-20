<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">待办审核</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <view class="section-header" v-if="list.length > 0">
        <text class="section-title">待审核申请 ({{ list.length }})</text>
      </view>
      
      <view class="list-container" v-if="list.length > 0">
        <view class="list-item" v-for="item in list" :key="item.id" @click="handleDetail(item.id)">
          <view class="item-left">
            <image :src="item.petCover || '/static/logo.png'" mode="aspectFill" class="cover"></image>
          </view>
          <view class="item-right">
            <view class="item-header">
              <view class="title-area">
                <text class="pet-name">{{ item.petName || '宠物' }}</text>
                <text class="apply-no">申请编号: #{{ item.id }}</text>
              </view>
              <view class="status-tag" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </view>
            </view>
            <view class="item-info">
              <view class="info-row">
                <uni-icons type="person" size="12" color="#6B7280"></uni-icons>
                <text class="info-text">申请人: {{ item.realName }}</text>
              </view>
              <view class="info-row">
                <uni-icons type="calendar" size="12" color="#6B7280"></uni-icons>
                <text class="info-text">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <u-empty v-else mode="data" text="暂无待审核申请" marginTop="100"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMyReceivedApplications } from '@/api/adoption'
import dayjs from 'dayjs'

const statusBarHeight = ref(44)
const list = ref([])
const loading = ref(false)

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
})

const goBack = () => {
  uni.navigateBack()
}

onShow(() => {
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getMyReceivedApplications()
    if (res.data) {
      list.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'passed', 2: 'rejected', 3: 'cancelled' }
  return map[status] || 'pending'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : ''
}

const handleDetail = (id) => {
  uni.navigateTo({
    url: `/pages/adoption/audit-detail?id=${id}`
  })
}
</script>

<style lang="scss" scoped>
.container {
  background-color: #F9FAFB;
  min-height: 100vh;
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

.section-header {
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 24rpx;
    font-weight: 600;
    color: #6B7280;
    text-transform: uppercase;
    letter-spacing: 1rpx;
  }
}

.list-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
  
  .item-left {
    .cover {
      width: 140rpx;
      height: 140rpx;
      border-radius: 16rpx;
      background-color: #eee;
      flex-shrink: 0;
    }
  }
  
  .item-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }
  
  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .title-area {
      flex: 1;
      min-width: 0;
      
      .pet-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #1F2937;
        display: block;
      }
      
      .apply-no {
        font-size: 22rpx;
        color: #6B7280;
        margin-top: 4rpx;
        display: block;
      }
    }
    
    .status-tag {
      font-size: 20rpx;
      font-weight: 600;
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      text-transform: uppercase;
      letter-spacing: 0.5rpx;
      flex-shrink: 0;
      
      &.pending {
        background: rgba(255, 193, 7, 0.2);
        color: #B45309;
      }
      
      &.passed {
        background: rgba(46, 125, 50, 0.1);
        color: #2E7D32;
      }
      
      &.rejected {
        background: #fef2f2;
        color: #ef4444;
      }
      
      &.cancelled {
        background: #f3f4f6;
        color: #9CA3AF;
      }
    }
  }
  
  .item-info {
    margin-top: 12rpx;
    
    .info-row {
      display: flex;
      align-items: center;
      margin-bottom: 6rpx;
      
      .info-text {
        font-size: 24rpx;
        color: #6B7280;
        margin-left: 8rpx;
      }
    }
  }
}
</style>
