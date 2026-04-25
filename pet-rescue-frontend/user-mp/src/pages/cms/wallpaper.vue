<template>
  <view class="wallpaper-page">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">萌宠壁纸</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="waterfall-list">
        <view class="waterfall-column left-column">
          <view 
            v-for="item in leftList" 
            :key="item.id" 
            class="wallpaper-card"
            @click="goDetail(item.id)"
          >
            <view class="card-image-box">
              <image :src="item.coverImg" mode="aspectFill" class="card-img"></image>
              <view class="download-badge">
                <uni-icons type="download" size="12" color="#fff"></uni-icons>
              </view>
            </view>
            <view class="card-content">
              <text class="card-title">{{ item.title }}</text>
              <view class="card-footer">
                <view class="view-info">
                  <uni-icons type="eye" size="12" color="#9CA3AF"></uni-icons>
                  <text class="view-count">{{ formatViewCount(item.viewCount) }}</text>
                </view>
                <view class="like-info">
                  <uni-icons type="heart-filled" size="12" :color="item.isLiked ? '#ff4d4f' : '#ccc'"></uni-icons>
                  <text class="like-count">{{ item.likeCount || 0 }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        
        <view class="waterfall-column right-column">
          <view 
            v-for="item in rightList" 
            :key="item.id" 
            class="wallpaper-card"
            @click="goDetail(item.id)"
          >
            <view class="card-image-box">
              <image :src="item.coverImg" mode="aspectFill" class="card-img"></image>
              <view class="download-badge">
                <uni-icons type="download" size="12" color="#fff"></uni-icons>
              </view>
            </view>
            <view class="card-content">
              <text class="card-title">{{ item.title }}</text>
              <view class="card-footer">
                <view class="view-info">
                  <uni-icons type="eye" size="12" color="#9CA3AF"></uni-icons>
                  <text class="view-count">{{ formatViewCount(item.viewCount) }}</text>
                </view>
                <view class="like-info">
                  <uni-icons type="heart-filled" size="12" :color="item.isLiked ? '#ff4d4f' : '#ccc'"></uni-icons>
                  <text class="like-count">{{ item.likeCount || 0 }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <u-empty v-if="wallpaperList.length === 0 && !loading" mode="list" text="暂无壁纸"></u-empty>
      <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'

const statusBarHeight = ref(20)
const wallpaperList = ref([])
const leftList = ref([])
const rightList = ref([])
const loadStatus = ref('loadmore')
const loading = ref(false)
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
    wallpaperList.value = []
    leftList.value = []
    rightList.value = []
    loadStatus.value = 'loading'
    loading.value = true
  }

  try {
    const res = await getArticleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: 3
    })

    const newItems = res.data.records || []
    
    if (reset) {
      wallpaperList.value = newItems
    } else {
      wallpaperList.value = [...wallpaperList.value, ...newItems]
    }
    
    leftList.value = wallpaperList.value.filter((_, i) => i % 2 === 0)
    rightList.value = wallpaperList.value.filter((_, i) => i % 2 !== 0)
    
    if (newItems.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (error) {
    console.error('加载壁纸列表失败', error)
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const goDetail = (id) => {
  uni.navigateTo({
    url: `/pages/cms/wallpaper-detail?id=${id}`
  })
}

const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}
</script>

<style lang="scss" scoped>
.wallpaper-page {
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 400rpx;
  background: linear-gradient(180deg, #2E7D32 0%, #388E3C 50%, rgba(46, 125, 50, 0) 100%);
  z-index: 0;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: transparent;
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
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.content-area {
  position: relative;
  z-index: 1;
  padding: 24rpx;
  padding-bottom: 40rpx;
}

.waterfall-list {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.waterfall-column {
  width: 48%;
  display: flex;
  flex-direction: column;
}

.wallpaper-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  
  .card-image-box {
    position: relative;
    width: 100%;
    
    .card-img {
      width: 100%;
      height: 400rpx;
      display: block;
    }
    
    .download-badge {
      position: absolute;
      bottom: 16rpx;
      right: 16rpx;
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .card-content {
    padding: 20rpx;
    
    .card-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.4;
      margin-bottom: 16rpx;
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .view-info, .like-info {
        display: flex;
        align-items: center;
        gap: 6rpx;
      }
      
      .view-count, .like-count {
        font-size: 24rpx;
        color: #9CA3AF;
      }
    }
  }
}
</style>
