<template>
  <view class="wallpaper-container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">萌宠壁纸</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="waterfall-list" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="left-col">
        <view 
          v-for="item in leftList" 
          :key="item.id" 
          class="wallpaper-card"
          @click="previewImage(item)"
        >
          <view class="img-wrapper">
            <image :src="item.coverImg" mode="aspectFill" class="cover"></image>
            <view class="download-btn" @click.stop="downloadImage(item)">
              <uni-icons type="download" size="18" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="info">
            <view class="title">{{ item.title }}</view>
            <view class="view-count">
              <uni-icons type="eye" size="12" color="#9CA3AF"></uni-icons>
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
          <view class="img-wrapper">
            <image :src="item.coverImg" mode="aspectFill" class="cover"></image>
            <view class="download-btn" @click.stop="downloadImage(item)">
              <uni-icons type="download" size="18" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="info">
            <view class="title">{{ item.title }}</view>
            <view class="view-count">
              <uni-icons type="eye" size="12" color="#9CA3AF"></uni-icons>
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

const statusBarHeight = ref(20)
const wallpaperList = ref([])
const leftList = ref([])
const rightList = ref([])
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

const goBack = () => {
  uni.navigateBack()
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

const downloadImage = (item) => {
  const imageUrl = item.wallpaperUrl || item.coverImg
  if (!imageUrl) {
    uni.showToast({
      title: '图片地址不存在',
      icon: 'none'
    })
    return
  }
  
  uni.showLoading({
    title: '保存中...'
  })
  
  uni.downloadFile({
    url: imageUrl,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '保存成功',
              icon: 'success'
            })
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({
              title: '保存失败',
              icon: 'none'
            })
          }
        })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
    }
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

.waterfall-list {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24rpx;
  padding-bottom: 40rpx;

  .left-col, .right-col {
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

    .img-wrapper {
      position: relative;
      width: 100%;
      
      .cover {
        width: 100%;
        height: 400rpx;
        display: block;
      }
      
      .download-btn {
        position: absolute;
        bottom: 16rpx;
        right: 16rpx;
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        background: rgba(46, 125, 50, 0.9);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
      }
    }
    
    &:active .download-btn {
      opacity: 1;
    }

    .info {
      padding: 16rpx 20rpx;

      .title {
        font-size: 26rpx;
        font-weight: bold;
        color: #1F2937;
        margin-bottom: 8rpx;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .view-count {
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
}
</style>
