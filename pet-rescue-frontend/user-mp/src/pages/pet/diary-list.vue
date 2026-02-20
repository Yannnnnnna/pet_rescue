<template>
  <view class="diary-list-page">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">成长日记</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="timeline-container" v-if="postList.length > 0">
        <view class="timeline-item" v-for="(post, index) in postList" :key="post.id" @click="goPostDetail(post)">
          <view class="timeline-left">
            <view class="timeline-dot" :class="{ 'first': index === 0 }"></view>
            <view class="timeline-line" v-if="index < postList.length - 1"></view>
          </view>
          
          <view class="timeline-content">
            <view class="time-badge">
              <uni-icons type="calendar" size="14" color="#FFC107"></uni-icons>
              <text>{{ formatTime(post.createTime) }}</text>
            </view>
            
            <view class="content-card">
              <view class="card-text">
                <text>{{ post.content || '暂无文字内容' }}</text>
              </view>
              
              <view class="image-grid" v-if="getImages(post).length > 0">
                <view 
                  class="img-item" 
                  v-for="(img, idx) in getImages(post).slice(0, 3)" 
                  :key="idx"
                  @click.stop="previewImage(getImages(post), idx)"
                >
                  <image :src="img" mode="aspectFill"></image>
                  <view class="more-mask" v-if="idx === 2 && getImages(post).length > 3">
                    <text>+{{ getImages(post).length - 3 }}</text>
                  </view>
                </view>
              </view>
              
              <view class="card-footer">
                <view class="status-tag">
                  <uni-icons 
                    :type="post.auditStatus === 1 ? 'checkbox-filled' : 'info'" 
                    size="14" 
                    :color="post.auditStatus === 1 ? '#19be6b' : '#ff9c00'"
                  ></uni-icons>
                  <text :class="post.auditStatus === 1 ? 'success' : 'warning'">{{ getStatusText(post.auditStatus) }}</text>
                </view>
                <view class="like-count">
                  <uni-icons type="heart-filled" size="14" color="#ff4d4f"></uni-icons>
                  <text>{{ post.likeCount || 0 }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <u-empty v-else mode="history" icon="http://cdn.uviewui.com/uview/empty/history.png" text="暂无成长日记"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionPostList } from '@/api/adoption-post'
import dayjs from 'dayjs'

const statusBarHeight = ref(20)
const postList = ref([])
const petId = ref('')

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  if (options.petId) {
    petId.value = options.petId
    fetchData()
  }
})

const fetchData = async () => {
  try {
    const res = await getAdoptionPostList({ petId: petId.value })
    if (res.data) {
      postList.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const getImages = (post) => {
  if (!post.images) return []
  if (typeof post.images === 'string') {
    try {
      return JSON.parse(post.images)
    } catch (e) {
      return post.images.split(',').filter(i => i)
    }
  }
  return post.images
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM月DD日 HH:mm')
}

const getStatusText = (auditStatus) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已驳回' }
  return map[auditStatus] || '未知'
}

const goBack = () => uni.navigateBack()

const goPostDetail = (post) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${post.id}` })
}

const previewImage = (urls, current) => {
  uni.previewImage({ urls, current })
}
</script>

<style lang="scss" scoped>
.diary-list-page {
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
  z-index: 100;
  background: transparent;
  
  .nav-content {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
  }
  
  .back-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
  }
  
  .placeholder {
    width: 64rpx;
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

.content-area {
  position: relative;
  z-index: 1;
  padding: 0 24rpx;
  padding-bottom: 30rpx;
}

.timeline-container {
  padding-top: 20rpx;
}

.timeline-item {
  display: flex;
  
  .timeline-left {
    width: 60rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .timeline-dot {
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;
      background: #ccc;
      border: 4rpx solid #fff;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
      flex-shrink: 0;
      
      &.first {
        background: #FFC107;
        width: 32rpx;
        height: 32rpx;
      }
    }
    
    .timeline-line {
      width: 4rpx;
      flex: 1;
      background: linear-gradient(180deg, #e0e0e0, #f5f5f5);
      margin: 8rpx 0;
    }
  }
  
  .timeline-content {
    flex: 1;
    padding-bottom: 30rpx;
    
    .time-badge {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      background: rgba(255, 193, 7, 0.15);
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      margin-bottom: 16rpx;
      
      text {
        font-size: 24rpx;
        color: #a16207;
        font-weight: 500;
      }
    }
    
    .content-card {
      background: #fff;
      border-radius: 24rpx;
      padding: 24rpx;
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
      
      .card-text {
        margin-bottom: 20rpx;
        
        text {
          font-size: 30rpx;
          color: #333;
          line-height: 1.6;
        }
      }
      
      .image-grid {
        display: flex;
        gap: 12rpx;
        margin-bottom: 20rpx;
        
        .img-item {
          width: 180rpx;
          height: 180rpx;
          border-radius: 16rpx;
          overflow: hidden;
          position: relative;
          
          image {
            width: 100%;
            height: 100%;
          }
          
          .more-mask {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            
            text {
              color: #fff;
              font-size: 36rpx;
              font-weight: 600;
            }
          }
        }
      }
      
      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 16rpx;
        border-top: 1rpx solid #f5f5f5;
        
        .status-tag {
          display: flex;
          align-items: center;
          gap: 8rpx;
          
          text {
            font-size: 24rpx;
            
            &.success { color: #19be6b; }
            &.warning { color: #ff9c00; }
          }
        }
        
        .like-count {
          display: flex;
          align-items: center;
          gap: 8rpx;
          
          text {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }
  }
}
</style>
