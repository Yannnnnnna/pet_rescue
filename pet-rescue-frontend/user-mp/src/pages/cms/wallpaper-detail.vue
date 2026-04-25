<template>
  <view class="detail-page">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">壁纸详情</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="image-section" v-if="detail.wallpaperUrl || detail.coverImg">
        <image 
          :src="detail.wallpaperUrl || detail.coverImg" 
          mode="widthFix" 
          class="wallpaper-img"
          @click="previewImage"
        ></image>
      </view>

      <view class="info-section">
        <text class="title">{{ detail.title }}</text>
        
        <view class="stats-row">
          <view class="stat-item">
            <uni-icons type="eye" size="16" color="#9CA3AF"></uni-icons>
            <text>{{ detail.viewCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <uni-icons type="heart-filled" size="16" :color="detail.isLiked ? '#ff4d4f' : '#9CA3AF'"></uni-icons>
            <text>{{ detail.likeCount || 0 }}</text>
          </view>
        </view>

        <view class="summary" v-if="detail.summary">
          <text class="summary-text">{{ detail.summary }}</text>
        </view>

        <view class="tags" v-if="detail.tags">
          <text class="tag" v-for="(tag, index) in detail.tags.split(',')" :key="index">{{ tag }}</text>
        </view>

        <view class="time-info">
          <text class="time-text">发布于 {{ formatTime(detail.createTime) }}</text>
        </view>
      </view>

      <view class="action-bar">
        <view class="action-btn like-btn" :class="{ liked: detail.isLiked }" @click="handleLike">
          <uni-icons type="heart-filled" size="20" :color="detail.isLiked ? '#fff' : '#ff4d4f'"></uni-icons>
          <text>{{ detail.isLiked ? '已点赞' : '点赞' }}</text>
        </view>
        <view class="action-btn download-btn" @click="downloadImage">
          <uni-icons type="download" size="20" color="#fff"></uni-icons>
          <text>保存壁纸</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getArticleDetail, toggleLike } from '@/api/article'
import dayjs from 'dayjs'

const statusBarHeight = ref(20)
const detail = ref({})
const articleId = ref(null)

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  if (options.id) {
    articleId.value = options.id
    loadDetail()
  }
})

const loadDetail = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await getArticleDetail(articleId.value)
    if (res.code === 200 || res.code === 0) {
      detail.value = res.data
    }
  } catch (error) {
    console.error('加载详情失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const goBack = () => {
  uni.navigateBack()
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const previewImage = () => {
  const imageUrl = detail.value.wallpaperUrl || detail.value.coverImg
  if (!imageUrl) return
  uni.previewImage({
    urls: [imageUrl],
    current: 0
  })
}

const handleLike = async () => {
  try {
    const res = await toggleLike(articleId.value)
    if (res.code === 200 || res.code === 0) {
      detail.value.isLiked = !detail.value.isLiked
      detail.value.likeCount = detail.value.isLiked 
        ? (detail.value.likeCount || 0) + 1 
        : Math.max((detail.value.likeCount || 1) - 1, 0)
      uni.showToast({ 
        title: detail.value.isLiked ? '点赞成功' : '取消点赞', 
        icon: 'none' 
      })
    }
  } catch (error) {
    console.error('点赞失败', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const downloadImage = () => {
  const imageUrl = detail.value.wallpaperUrl || detail.value.coverImg
  if (!imageUrl) {
    uni.showToast({ title: '图片地址不存在', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '保存中...' })
  
  uni.downloadFile({
    url: imageUrl,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({ title: '保存成功', icon: 'success' })
          },
          fail: (err) => {
            uni.hideLoading()
            if (err.errMsg.includes('auth deny')) {
              uni.showModal({
                title: '提示',
                content: '需要授权保存图片到相册',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    uni.openSetting()
                  }
                }
              })
            } else {
              uni.showToast({ title: '保存失败', icon: 'none' })
            }
          }
        })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '下载失败', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f9fa;
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
    color: #333;
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.content {
  padding-bottom: 140rpx;
}

.image-section {
  background: #fff;
  
  .wallpaper-img {
    width: 100%;
    display: block;
  }
}

.info-section {
  background: #fff;
  margin-top: 20rpx;
  padding: 30rpx;
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    line-height: 1.4;
    display: block;
    margin-bottom: 20rpx;
  }
  
  .stats-row {
    display: flex;
    gap: 40rpx;
    margin-bottom: 20rpx;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      text {
        font-size: 26rpx;
        color: #9CA3AF;
      }
    }
  }
  
  .summary {
    background: #f8f9fa;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    
    .summary-text {
      font-size: 28rpx;
      color: #666;
      line-height: 1.6;
    }
  }
  
  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 20rpx;
    
    .tag {
      padding: 8rpx 20rpx;
      background: rgba(46, 125, 50, 0.1);
      color: #2E7D32;
      font-size: 24rpx;
      border-radius: 20rpx;
    }
  }
  
  .time-info {
    .time-text {
      font-size: 24rpx;
      color: #9CA3AF;
    }
  }
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
  
  .action-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    
    text {
      font-size: 28rpx;
    }
  }
  
  .like-btn {
    background: #fff;
    border: 2rpx solid #ff4d4f;
    color: #ff4d4f;
    
    text {
      color: #ff4d4f;
    }
    
    &.liked {
      background: #ff4d4f;
      
      text {
        color: #fff;
      }
    }
  }
  
  .download-btn {
    background: linear-gradient(135deg, #2E7D32, #388E3C);
    
    text {
      color: #fff;
    }
  }
}
</style>
