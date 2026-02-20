<template>
  <view class="wallpaper-container">
    <view class="waterfall-list">
      <view class="left-col">
        <view 
          v-for="item in leftList" 
          :key="item.id" 
          class="wallpaper-card"
          @click="previewImage(item)"
        >
          <image :src="item.coverImg" mode="widthFix" class="cover"></image>
          <view class="info">
            <view class="title">{{ item.title }}</view>
            <view class="view-count">
              <u-icon name="eye" size="12" color="#999"></u-icon>
              <text>{{ formatViewCount(item.viewCount) }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="right-col">
        <view 
          v-for="item in rightList" 
          :key="item.id" 
          class="wallpaper-card"
          @click="previewImage(item)"
        >
          <image :src="item.coverImg" mode="widthFix" class="cover"></image>
          <view class="info">
            <view class="title">{{ item.title }}</view>
            <view class="view-count">
              <u-icon name="eye" size="12" color="#999"></u-icon>
              <text>{{ formatViewCount(item.viewCount) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'

const wallpaperList = ref([])
const leftList = ref([])
const rightList = ref([])
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
    wallpaperList.value = []
    leftList.value = []
    rightList.value = []
    loadStatus.value = 'loading'
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
  }
}

const previewImage = (item) => {
  const imageUrl = item.wallpaperUrl || item.coverImg
  if (!imageUrl) {
    uni.showToast({
      title: '图片地址不存在',
      icon: 'none'
    })
    return
  }
  uni.previewImage({
    urls: [imageUrl],
    current: 0
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
.wallpaper-container {
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

.waterfall-list {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20rpx;

  .left-col, .right-col {
    width: 48%;
    display: flex;
    flex-direction: column;
  }

  .wallpaper-card {
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .cover {
      width: 100%;
      display: block;
    }

    .info {
      padding: 16rpx;

      .title {
        font-size: 26rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 12rpx;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .view-count {
        display: flex;
        align-items: center;
        font-size: 22rpx;
        color: #999;

        text {
          margin-left: 4rpx;
        }
      }
    }
  }
}
</style>
