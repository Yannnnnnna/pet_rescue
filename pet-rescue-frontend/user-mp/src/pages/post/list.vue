<template>
  <view class="diary-list-page">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">领养日记</text>
        <view class="publish-btn" @click="handlePublish">
          <uni-icons type="plus" size="20" color="#fff"></uni-icons>
        </view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="filter-section">
        <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
          <view class="filter-tabs">
            <view 
              class="filter-item" 
              :class="{ active: currentFilter === 'all' }"
              @click="currentFilter = 'all'"
            >
              <uni-icons type="fire" size="16" :color="currentFilter === 'all' ? '#FFC107' : '#666'"></uni-icons>
              <text>全部</text>
            </view>
            <view 
              class="filter-item" 
              :class="{ active: currentFilter === 'cat' }"
              @click="currentFilter = 'cat'"
            >
              <text class="emoji-icon">🐱</text>
              <text>喵星人</text>
            </view>
            <view 
              class="filter-item" 
              :class="{ active: currentFilter === 'dog' }"
              @click="currentFilter = 'dog'"
            >
              <text class="emoji-icon">🐕</text>
              <text>汪星人</text>
            </view>
            <view 
              class="filter-item" 
              :class="{ active: currentFilter === 'mine' }"
              @click="currentFilter = 'mine'"
            >
              <uni-icons type="person" size="16" :color="currentFilter === 'mine' ? '#FFC107' : '#666'"></uni-icons>
              <text>我的</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="diary-waterfall">
        <view class="waterfall-column left-column">
          <view 
            class="diary-card" 
            v-for="(item, index) in leftList" 
            :key="item.id"
            @click="goDetail(item.id)"
          >
            <view class="card-image-box" v-if="item.coverImg">
              <image :src="item.coverImg" mode="aspectFill" class="card-img"></image>
              <view class="image-count" v-if="item.imageCount > 1">
                <uni-icons type="image" size="12" color="#fff"></uni-icons>
                <text>{{ item.imageCount }}</text>
              </view>
            </view>
            <view class="card-content">
              <text class="card-text">{{ item.content }}</text>
              <view class="card-footer">
                <view class="user-info">
                  <image :src="item.userAvatar || '/static/logo.png'" class="user-avatar"></image>
                  <text class="user-name">{{ item.userName || '爱心用户' }}</text>
                </view>
                <view class="like-info">
                  <uni-icons type="heart-filled" size="14" :color="item.isLiked ? '#ff4d4f' : '#ccc'"></uni-icons>
                  <text class="like-count">{{ item.likeCount || 0 }}</text>
                </view>
              </view>
            </view>
            <view class="pet-badge" v-if="item.petName">
              <text class="pet-emoji">{{ item.petType === 1 ? '🐕' : '🐱' }}</text>
              <text class="pet-name">{{ item.petName }}</text>
            </view>
          </view>
        </view>
        
        <view class="waterfall-column right-column">
          <view 
            class="diary-card" 
            v-for="(item, index) in rightList" 
            :key="item.id"
            @click="goDetail(item.id)"
          >
            <view class="card-image-box" v-if="item.coverImg">
              <image :src="item.coverImg" mode="aspectFill" class="card-img"></image>
              <view class="image-count" v-if="item.imageCount > 1">
                <uni-icons type="image" size="12" color="#fff"></uni-icons>
                <text>{{ item.imageCount }}</text>
              </view>
            </view>
            <view class="card-content">
              <text class="card-text">{{ item.content }}</text>
              <view class="card-footer">
                <view class="user-info">
                  <image :src="item.userAvatar || '/static/logo.png'" class="user-avatar"></image>
                  <text class="user-name">{{ item.userName || '爱心用户' }}</text>
                </view>
                <view class="like-info">
                  <uni-icons type="heart-filled" size="14" :color="item.isLiked ? '#ff4d4f' : '#ccc'"></uni-icons>
                  <text class="like-count">{{ item.likeCount || 0 }}</text>
                </view>
              </view>
            </view>
            <view class="pet-badge" v-if="item.petName">
              <text class="pet-emoji">{{ item.petType === 1 ? '🐕' : '🐱' }}</text>
              <text class="pet-name">{{ item.petName }}</text>
            </view>
          </view>
        </view>
      </view>

      <u-empty v-if="dataList.length === 0 && !loading" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png" text="暂无日记"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionPostList, checkPostLike } from '@/api/adoption-post'
import { getUserDetail, getMyInfo } from '@/api/user'
import { getPetDetail } from '@/api/pet'

const statusBarHeight = ref(20)
const currentFilter = ref('all')
const allDataList = ref([])
const dataList = ref([])
const leftList = ref([])
const rightList = ref([])
const loading = ref(false)
const currentUserId = ref(null)

onLoad(async () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  
  try {
    const res = await getMyInfo()
    if (res.data) {
      currentUserId.value = res.data.id
    }
  } catch (e) {
    console.log('未登录')
  }
  
  fetchData()
})

watch(currentFilter, () => {
  applyFilter()
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { auditStatus: 1 }
    
    const res = await getAdoptionPostList(params)
    const list = Array.isArray(res.data) ? res.data : []
    
    const processedList = await Promise.all(list.map(async (item) => {
      let imageList = []
      if (item.images) {
        try {
          const parsed = JSON.parse(item.images)
          if (Array.isArray(parsed)) imageList = parsed
          else imageList = [item.images]
        } catch (e) {
          imageList = item.images.split(',').filter(i => i)
        }
      }
      
      let userAvatar = '/static/logo.png'
      let userName = '爱心用户'
      let petName = ''
      let petType = null
      
      if (item.userId) {
        try {
          const userRes = await getUserDetail(item.userId)
          if (userRes.data) {
            userAvatar = userRes.data.avatar || userAvatar
            userName = userRes.data.nickname || userName
          }
        } catch (e) {}
      }
      
      if (item.petId) {
        try {
          const petRes = await getPetDetail(item.petId)
          if (petRes.data) {
            petName = petRes.data.name || ''
            petType = petRes.data.type
          }
        } catch (e) {}
      }
      
      let isLiked = false
      let likeCount = item.likeCount || 0
      try {
        const likeRes = await checkPostLike(item.id)
        if (likeRes.data) {
          isLiked = likeRes.data.checked || false
          likeCount = likeRes.data.count || likeCount
        }
      } catch (e) {}
      
      return {
        ...item,
        imageList,
        coverImg: imageList[0] || '',
        imageCount: imageList.length,
        userAvatar,
        userName,
        petName,
        petType,
        isLiked,
        likeCount
      }
    }))
    
    allDataList.value = processedList
    applyFilter()
    
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  let filtered = allDataList.value
  
  if (currentFilter.value === 'mine' && currentUserId.value) {
    filtered = filtered.filter(item => item.userId === currentUserId.value)
  } else if (currentFilter.value === 'cat') {
    filtered = filtered.filter(item => item.petType === 0)
  } else if (currentFilter.value === 'dog') {
    filtered = filtered.filter(item => item.petType === 1)
  }
  
  dataList.value = filtered
  leftList.value = filtered.filter((_, i) => i % 2 === 0)
  rightList.value = filtered.filter((_, i) => i % 2 !== 0)
}

const goBack = () => uni.navigateBack()

const handlePublish = () => {
  uni.navigateTo({ url: '/pages/post/publish' })
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
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
  
  .back-btn, .publish-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
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

.filter-section {
  margin-bottom: 24rpx;
  
  .filter-scroll {
    white-space: nowrap;
  }
  
  .filter-tabs {
    display: inline-flex;
    gap: 16rpx;
    padding: 0 6rpx;
  }
  
  .filter-item {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    padding: 16rpx 28rpx;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 40rpx;
    font-size: 26rpx;
    color: #666;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    
    &.active {
      background: linear-gradient(135deg, #FFC107, #FFD54F);
      color: #333;
      font-weight: 500;
      box-shadow: 0 6rpx 16rpx rgba(255, 193, 7, 0.3);
    }
    
    .emoji-icon {
      font-size: 28rpx;
    }
  }
}

.diary-waterfall {
  display: flex;
  gap: 20rpx;
  
  .waterfall-column {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
}

.diary-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
  position: relative;
  transition: transform 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
  
  .card-image-box {
    position: relative;
    width: 100%;
    
    .card-img {
      width: 100%;
      height: 280rpx;
    }
    
    .image-count {
      position: absolute;
      bottom: 16rpx;
      right: 16rpx;
      background: rgba(0, 0, 0, 0.5);
      padding: 6rpx 14rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      gap: 6rpx;
      
      text {
        font-size: 22rpx;
        color: #fff;
      }
    }
  }
  
  .card-content {
    padding: 20rpx;
    
    .card-text {
      font-size: 28rpx;
      color: #333;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 20rpx;
      padding-top: 16rpx;
      border-top: 1rpx solid #f5f5f5;
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 12rpx;
        
        .user-avatar {
          width: 44rpx;
          height: 44rpx;
          border-radius: 50%;
          border: 2rpx solid #f0f0f0;
        }
        
        .user-name {
          font-size: 24rpx;
          color: #666;
          max-width: 120rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      
      .like-info {
        display: flex;
        align-items: center;
        gap: 8rpx;
        
        .like-count {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
  
  .pet-badge {
    position: absolute;
    top: 16rpx;
    left: 16rpx;
    background: rgba(255, 255, 255, 0.95);
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    gap: 6rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    
    .pet-emoji {
      font-size: 24rpx;
    }
    
    .pet-name {
      font-size: 22rpx;
      color: #333;
      font-weight: 500;
    }
  }
}
</style>
