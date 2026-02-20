<template>
  <view class="post-detail-page">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">日记详情</text>
        <view class="more-btn" @click="showActions = true" v-if="isOwner">
          <uni-icons type="more" size="20" color="#fff"></uni-icons>
        </view>
        <view class="placeholder" v-else></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="main-card">
        <view class="user-section">
          <image :src="postDetail.userAvatar || '/static/logo.png'" class="user-avatar"></image>
          <view class="user-info">
            <text class="user-name">{{ postDetail.userName || '爱心用户' }}</text>
            <text class="post-time">{{ formatTime(postDetail.createTime) }}</text>
          </view>
          <view class="follow-btn" v-if="!isOwner">
            <text>+ 关注</text>
          </view>
        </view>

        <view class="pet-tag-section" v-if="postDetail.petName">
          <view class="pet-tag">
            <text class="pet-emoji">{{ postDetail.petType === 1 ? '🐕' : '🐱' }}</text>
            <text class="pet-text">我是 {{ postDetail.petName }}</text>
          </view>
        </view>

        <view class="text-content">
          <text>{{ postDetail.content }}</text>
        </view>

        <view class="image-section" v-if="imageList.length > 0">
          <swiper 
            class="image-swiper" 
            circular 
            :indicator-dots="imageList.length > 1"
            indicator-color="rgba(255,255,255,0.5)"
            indicator-active-color="#FFC107"
          >
            <swiper-item v-for="(img, idx) in imageList" :key="idx">
              <image :src="img" mode="aspectFill" class="swiper-img" @click="previewImage(idx)"></image>
            </swiper-item>
          </swiper>
          <view class="image-counter" v-if="imageList.length > 1">
            <uni-icons type="image" size="14" color="#fff"></uni-icons>
            <text>{{ imageList.length }} 张</text>
          </view>
        </view>

        <view class="interaction-section">
          <view class="interaction-item" @click="handleLike">
            <view class="icon-box" :class="{ liked: isLiked }">
              <uni-icons type="heart-filled" size="24" :color="isLiked ? '#ff4d4f' : '#999'"></uni-icons>
            </view>
            <text class="count">{{ likeCount || 0 }}</text>
          </view>
        </view>
      </view>

      <view class="related-section" v-if="relatedPosts.length > 0">
        <view class="section-header">
          <view class="header-icon">
            <uni-icons type="star-filled" size="18" color="#FFC107"></uni-icons>
          </view>
          <text class="section-title">更多日记</text>
        </view>
        <scroll-view scroll-x class="related-scroll">
          <view class="related-list">
            <view class="related-item" v-for="item in relatedPosts" :key="item.id" @click="goDetail(item.id)">
              <image :src="item.coverImg" mode="aspectFill" class="related-img"></image>
              <text class="related-text">{{ item.content.substring(0, 20) }}...</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <u-action-sheet 
      :show="showActions" 
      :actions="actionList" 
      @close="showActions = false"
      @select="handleActionSelect"
    ></u-action-sheet>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionPostDetail, deleteAdoptionPost, likePost, checkPostLike } from '@/api/adoption-post'
import { getUserDetail, getMyInfo } from '@/api/user'
import { getPetDetail } from '@/api/pet'
import dayjs from 'dayjs'

const statusBarHeight = ref(20)
const postDetail = ref({})
const imageList = ref([])
const isLiked = ref(false)
const likeCount = ref(0)
const currentUserId = ref(null)
const showActions = ref(false)
const relatedPosts = ref([])

const actionList = [
  { name: '编辑', value: 'edit' },
  { name: '删除', value: 'delete', color: '#ff4d4f' }
]

const isOwner = computed(() => {
  if (!currentUserId.value || !postDetail.value.userId) return false
  return String(currentUserId.value) === String(postDetail.value.userId)
})

onLoad(async (options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  try {
    const res = await getMyInfo()
    if (res.data) {
      currentUserId.value = res.data.id
    }
  } catch (e) {}
  
  if (options.id) {
    fetchDetail(options.id)
  }
})

const fetchDetail = async (id) => {
  try {
    const res = await getAdoptionPostDetail(id)
    if (res.data) {
      postDetail.value = res.data
      
      if (postDetail.value.images) {
        if (typeof postDetail.value.images === 'string') {
          try {
            imageList.value = JSON.parse(postDetail.value.images)
          } catch (e) {
            imageList.value = postDetail.value.images.split(',').filter(i => i)
          }
        } else if (Array.isArray(postDetail.value.images)) {
          imageList.value = postDetail.value.images
        }
      }
      
      if (postDetail.value.userId) {
        try {
          const userRes = await getUserDetail(postDetail.value.userId)
          if (userRes.data) {
            postDetail.value.userAvatar = userRes.data.avatar
            postDetail.value.userName = userRes.data.nickname
          }
        } catch (e) {}
      }

      if (postDetail.value.petId) {
        try {
          const petRes = await getPetDetail(postDetail.value.petId)
          if (petRes.data) {
            postDetail.value.petName = petRes.data.name
            postDetail.value.petType = petRes.data.type
          }
        } catch (e) {}
      }

      try {
        const likeRes = await checkPostLike(id)
        if (likeRes.data) {
          isLiked.value = likeRes.data.checked || false
          likeCount.value = likeRes.data.count || postDetail.value.likeCount || 0
        }
      } catch (e) {}
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const handleLike = async () => {
  try {
    const res = await likePost(postDetail.value.id)
    if (res.code === 200 || res.code === 0) {
      isLiked.value = !isLiked.value
      likeCount.value = res.data || (isLiked.value ? likeCount.value + 1 : likeCount.value - 1)
      
      uni.showToast({
        title: isLiked.value ? '点赞成功' : '已取消',
        icon: 'none'
      })
    }
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const handleActionSelect = (item) => {
  if (item.value === 'edit') {
    uni.navigateTo({ url: `/pages/post/publish?id=${postDetail.value.id}` })
  } else if (item.value === 'delete') {
    uni.showModal({
      title: '提示',
      content: '确定要删除这条日记吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const result = await deleteAdoptionPost(postDetail.value.id)
            if (result.code === 200 || result.code === 0) {
              uni.showToast({ title: '删除成功', icon: 'success' })
              setTimeout(() => uni.navigateBack(), 1500)
            }
          } catch (e) {
            uni.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      }
    })
  }
}

const previewImage = (idx) => {
  uni.previewImage({
    urls: imageList.value,
    current: idx
  })
}

const goBack = () => uni.navigateBack()

const goDetail = (id) => {
  uni.redirectTo({ url: `/pages/post/detail?id=${id}` })
}

const formatTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const target = dayjs(time)
  const diff = now.diff(target, 'day')
  
  if (diff === 0) {
    const diffHour = now.diff(target, 'hour')
    if (diffHour === 0) {
      const diffMin = now.diff(target, 'minute')
      return diffMin <= 1 ? '刚刚' : `${diffMin}分钟前`
    }
    return `${diffHour}小时前`
  } else if (diff === 1) {
    return '昨天'
  } else if (diff < 7) {
    return `${diff}天前`
  } else {
    return target.format('MM-DD')
  }
}
</script>

<style lang="scss" scoped>
.post-detail-page {
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
  
  .back-btn, .more-btn {
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

.main-card {
  background: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 24rpx;
}

.user-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
  
  .user-avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    border: 4rpx solid #f0f0f0;
  }
  
  .user-info {
    flex: 1;
    margin-left: 20rpx;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    
    .user-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    .post-time {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .follow-btn {
    padding: 12rpx 28rpx;
    background: linear-gradient(135deg, #FFC107, #FFD54F);
    border-radius: 30rpx;
    
    text {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

.pet-tag-section {
  padding: 20rpx 30rpx 0;
  
  .pet-tag {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    background: linear-gradient(135deg, rgba(46, 125, 50, 0.1), rgba(56, 142, 60, 0.1));
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
    
    .pet-emoji {
      font-size: 28rpx;
    }
    
    .pet-text {
      font-size: 26rpx;
      color: #2E7D32;
      font-weight: 500;
    }
  }
}

.text-content {
  padding: 30rpx;
  
  text {
    font-size: 32rpx;
    color: #333;
    line-height: 1.8;
  }
}

.image-section {
  position: relative;
  margin: 0 30rpx 30rpx;
  border-radius: 24rpx;
  overflow: hidden;
  
  .image-swiper {
    width: 100%;
    height: 500rpx;
    
    .swiper-img {
      width: 100%;
      height: 100%;
    }
  }
  
  .image-counter {
    position: absolute;
    bottom: 20rpx;
    right: 20rpx;
    background: rgba(0, 0, 0, 0.5);
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    text {
      font-size: 24rpx;
      color: #fff;
    }
  }
}

.interaction-section {
  display: flex;
  justify-content: space-around;
  padding: 30rpx;
  border-top: 1rpx solid #f5f5f5;
  
  .interaction-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    
    .icon-box {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      
      &.liked {
        background: rgba(255, 77, 79, 0.1);
      }
    }
    
    .count {
      font-size: 26rpx;
      color: #666;
    }
  }
}

.related-section {
  background: #fff;
  border-radius: 32rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 24rpx;
    
    .header-icon {
      width: 48rpx;
      height: 48rpx;
      border-radius: 12rpx;
      background: rgba(255, 193, 7, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
  
  .related-scroll {
    white-space: nowrap;
  }
  
  .related-list {
    display: inline-flex;
    gap: 20rpx;
  }
  
  .related-item {
    width: 240rpx;
    flex-shrink: 0;
    
    .related-img {
      width: 240rpx;
      height: 180rpx;
      border-radius: 16rpx;
    }
    
    .related-text {
      display: block;
      margin-top: 12rpx;
      font-size: 26rpx;
      color: #666;
      white-space: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
  }
}
</style>
