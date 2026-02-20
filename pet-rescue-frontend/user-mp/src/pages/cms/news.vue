<template>
  <view class="news-container">
    <view class="tabs-wrapper">
      <u-tabs 
        :list="tabList" 
        :current="currentTab" 
        @change="handleTabChange"
        active-color="#19be6b"
        line-color="#19be6b"
      ></u-tabs>
    </view>

    <view class="content-wrapper">
      <view v-if="currentTab === 0">
        <view 
          v-for="item in noticeList" 
          :key="item.id" 
          class="notice-card"
          @click="goDetail(item)"
        >
          <view class="notice-content">
            <view class="notice-title">{{ item.title }}</view>
            <view class="notice-summary">{{ item.summary }}</view>
            <view class="notice-time">{{ formatTime(item.createTime) }}</view>
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
              <u-icon name="clock" size="14" color="#999"></u-icon>
              <text class="activity-time">{{ formatTime(item.activityStartTime) }}</text>
            </view>
            <view class="activity-meta">
              <u-icon name="map" size="14" color="#999"></u-icon>
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
  background-color: #f5f5f5;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  
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
    color: #333;
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.tabs-wrapper {
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 99;
}

.content-wrapper {
  padding: 20rpx;
}

.notice-card {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .notice-content {
    flex: 1;
    margin-right: 20rpx;

    .notice-title {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 12rpx;
    }

    .notice-summary {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
      margin-bottom: 12rpx;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .notice-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .notice-thumb {
    width: 160rpx;
    height: 120rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }
}

.activity-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  position: relative;

  .activity-cover {
    width: 100%;
    height: 400rpx;
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
      background: #19be6b;
      color: #fff;
    }

    &.status-ended {
      background: #999;
      color: #fff;
    }
  }

  .activity-info {
    padding: 20rpx;

    .activity-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 16rpx;
    }

    .activity-meta {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 8rpx;

      text {
        margin-left: 6rpx;
      }
    }
  }
}
</style>
