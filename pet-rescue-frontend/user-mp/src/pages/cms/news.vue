<template>
  <view class="news-container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">资讯中心</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="tabs-wrapper" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <view class="custom-tabs">
        <view 
          v-for="(item, index) in tabList" 
          :key="index"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="handleTabChange({ index })"
        >
          {{ item.name }}
        </view>
      </view>
    </view>

    <view class="content-wrapper" :style="{ paddingTop: (statusBarHeight + 88) + 'px' }">
      <view v-if="currentTab === 0">
        <view 
          v-for="item in noticeList" 
          :key="item.id" 
          class="notice-card"
          @click="goDetail(item)"
        >
          <view class="notice-content">
            <view class="notice-tag">公告</view>
            <view class="notice-title">{{ item.title }}</view>
            <view class="notice-summary">{{ item.summary }}</view>
            <view class="notice-meta">
              <view class="meta-item">
                <uni-icons type="calendar" size="14" color="#9CA3AF"></uni-icons>
                <text>{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
          </view>
          <image :src="item.coverImg" mode="aspectFill" class="notice-thumb"></image>
        </view>

        <u-empty v-if="noticeList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png"></u-empty>
        <u-loadmore :status="loadStatus" marginTop="30" v-if="noticeList.length > 0"></u-loadmore>
      </view>

      <view v-if="currentTab === 1">
        <view 
          v-for="item in activityList" 
          :key="item.id" 
          class="activity-card"
          @click="goDetail(item)"
        >
          <image :src="item.coverImg" mode="aspectFill" class="activity-cover"></image>
          <view class="activity-status" :class="getStatusClass(item)">
            {{ getStatusText(item) }}
          </view>
          <view class="activity-info">
            <view class="activity-title">{{ item.title }}</view>
            <view class="activity-meta">
              <uni-icons type="calendar" size="14" color="#9CA3AF"></uni-icons>
              <text class="activity-time">{{ formatTime(item.activityStartTime) }}</text>
            </view>
            <view class="activity-meta">
              <uni-icons type="location" size="14" color="#9CA3AF"></uni-icons>
              <text class="activity-address">{{ item.activityAddress || '线上活动' }}</text>
            </view>
          </view>
        </view>

        <u-empty v-if="activityList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png"></u-empty>
        <u-loadmore :status="loadStatus" marginTop="30" v-if="activityList.length > 0"></u-loadmore>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'

const statusBarHeight = ref(20)
const tabList = ref([
  { name: '公告' },
  { name: '活动' }
])
const currentTab = ref(0)
const noticeList = ref([])
const activityList = ref([])
const loadStatus = ref('loadmore')
const pageNum = ref(1)
const pageSize = ref(10)

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  loadData(true)
})

onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  loadStatus.value = 'loading'
  pageNum.value++
  loadData()
})

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    noticeList.value = []
    activityList.value = []
    loadStatus.value = 'loading'
  }

  try {
    const res = await getArticleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: currentTab.value === 0 ? 1 : 2
    })

    const newItems = res.data.records || []
    
    if (currentTab.value === 0) {
      if (reset) {
        noticeList.value = newItems
      } else {
        noticeList.value = [...noticeList.value, ...newItems]
      }
    } else {
      if (reset) {
        activityList.value = newItems
      } else {
        activityList.value = [...activityList.value, ...newItems]
      }
    }
    
    if (newItems.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (error) {
    console.error('加载资讯列表失败', error)
    loadStatus.value = 'loadmore'
  }
}

const handleTabChange = (item) => {
  currentTab.value = item.index
  loadData(true)
}

const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages/wiki/detail?id=${item.id}`
  })
}

const goBack = () => {
  uni.navigateBack()
}

const getStatusText = (item) => {
  if (!item.activityEndTime) return '进行中'
  const now = new Date()
  const endTime = new Date(item.activityEndTime)
  return now > endTime ? '已结束' : '进行中'
}

const getStatusClass = (item) => {
  const status = getStatusText(item)
  return status === '进行中' ? 'status-active' : 'status-ended'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style lang="scss" scoped>
.news-container {
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

.tabs-wrapper {
  background: #fff;
  position: fixed;
  left: 0;
  right: 0;
  z-index: 99;
  border-bottom: 1rpx solid #f0f0f0;
}

.custom-tabs {
  display: flex;
  padding: 0 30rpx;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    color: #6B7280;
    font-weight: 500;
    position: relative;
    
    &.active {
      color: #2E7D32;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 48rpx;
        height: 6rpx;
        background: #2E7D32;
        border-radius: 3rpx;
      }
    }
  }
}

.content-wrapper {
  padding: 24rpx;
}

.notice-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);

  .notice-content {
    flex: 1;
    margin-right: 20rpx;

    .notice-tag {
      display: inline-block;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      font-size: 20rpx;
      font-weight: bold;
      background: #E8F5E9;
      color: #2E7D32;
      margin-bottom: 12rpx;
    }

    .notice-title {
      font-size: 30rpx;
      font-weight: bold;
      color: #1F2937;
      margin-bottom: 12rpx;
      line-height: 1.4;
    }

    .notice-summary {
      font-size: 26rpx;
      color: #6B7280;
      line-height: 1.6;
      margin-bottom: 12rpx;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .notice-meta {
      display: flex;
      align-items: center;
      
      .meta-item {
        display: flex;
        align-items: center;
        gap: 6rpx;
        
        text {
          font-size: 22rpx;
          color: #9CA3AF;
        }
      }
    }
  }

  .notice-thumb {
    width: 180rpx;
    height: 140rpx;
    border-radius: 16rpx;
    flex-shrink: 0;
  }
}

.activity-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  position: relative;

  .activity-cover {
    width: 100%;
    height: 360rpx;
    display: block;
  }

  .activity-status {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    padding: 8rpx 20rpx;
    border-radius: 24rpx;
    font-size: 24rpx;
    font-weight: bold;

    &.status-active {
      background: #2E7D32;
      color: #fff;
    }

    &.status-ended {
      background: #9CA3AF;
      color: #fff;
    }
  }

  .activity-info {
    padding: 24rpx;

    .activity-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #1F2937;
      margin-bottom: 16rpx;
      line-height: 1.4;
    }

    .activity-meta {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;
      gap: 8rpx;

      .activity-time, .activity-address {
        font-size: 24rpx;
        color: #6B7280;
      }
    }
  }
}
</style>
