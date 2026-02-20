<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">{{ pageTitle }}</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <u-list @scrolltolower="loadMore" v-if="dataList.length > 0">
        <u-list-item v-for="(item, index) in dataList" :key="index">
          <view class="pet-card" @click="handlePetClick(item)">
            <view class="card-left">
              <image :src="item.coverImg" mode="aspectFill" class="pet-img"></image>
            </view>
            <view class="card-right">
              <view class="card-header">
                <text class="pet-name">{{ item.name }}</text>
                <view class="status-tag" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </view>
              </view>
              <view class="card-info">
                <text class="info-item">{{ item.breed }}</text>
                <text class="divider">·</text>
                <text class="info-item">{{ item.age }}</text>
              </view>
              <view class="card-location">
                <uni-icons type="location" size="12" color="#9CA3AF"></uni-icons>
                <text class="location-text">{{ item.city }} {{ item.address }}</text>
              </view>
              <view class="card-desc">{{ item.description }}</view>
            </view>
            
            <view class="action-btn" v-if="type === 'published'" @click.stop="handleConsultClick(item)">
              <uni-icons type="chat" size="18" color="#2E7D32"></uni-icons>
              <text class="btn-text">咨询记录</text>
            </view>
          </view>
        </u-list-item>
      </u-list>
      <u-empty v-else mode="list" text="暂无数据" marginTop="100"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyFavorites, getMyPublishedPets, getMyChattedPets } from '@/api/pet.js'

const statusBarHeight = ref(44)
const type = ref('')
const pageTitle = ref('宠物列表')
const dataList = ref([])

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  type.value = options.type
  switch (options.type) {
    case 'favorites':
      pageTitle.value = '我的收藏'
      break
    case 'published':
      pageTitle.value = '我发布的'
      break
    case 'chatted':
      pageTitle.value = '咨询足迹'
      break
  }
  fetchData()
})

const goBack = () => {
  uni.navigateBack()
}

const fetchData = async () => {
  uni.showLoading({ title: '加载中' })
  try {
    let res = null
    if (type.value === 'favorites') {
      res = await getMyFavorites()
    } else if (type.value === 'published') {
      res = await getMyPublishedPets()
    } else if (type.value === 'chatted') {
      res = await getMyChattedPets()
    }
    
    if (res && res.data) {
      dataList.value = res.data
    }
  } catch (error) {
    console.error('加载失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const getStatusText = (status) => {
  const map = { 0: '待领养', 1: '申请中', 2: '已领养' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'waiting', 1: 'applying', 2: 'adopted' }
  return map[status] || 'waiting'
}

const handlePetClick = (item) => {
  let url = `/pages/pet/detail?id=${item.id}`
  if (type.value === 'published') {
    url += '&isOwner=true'
  }
  uni.navigateTo({ url })
}

const handleConsultClick = (item) => {
  uni.navigateTo({
    url: `/pages/message/consultation-list?petId=${item.id}`
  })
}

const loadMore = () => {
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

.pet-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 24rpx;
  position: relative;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-left {
  .pet-img {
    width: 160rpx;
    height: 160rpx;
    border-radius: 16rpx;
    flex-shrink: 0;
    background-color: #eee;
  }
}

.card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .pet-name {
    font-size: 32rpx;
    font-weight: 600;
    color: #1F2937;
  }
  
  .status-tag {
    font-size: 20rpx;
    font-weight: 500;
    padding: 6rpx 14rpx;
    border-radius: 16rpx;
    
    &.waiting {
      background: rgba(46, 125, 50, 0.1);
      color: #2E7D32;
    }
    
    &.applying {
      background: rgba(255, 193, 7, 0.2);
      color: #B45309;
    }
    
    &.adopted {
      background: #f3f4f6;
      color: #6B7280;
    }
  }
}

.card-info {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
  
  .info-item {
    font-size: 24rpx;
    color: #6B7280;
  }
  
  .divider {
    margin: 0 8rpx;
    color: #D1D5DB;
  }
}

.card-location {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
  
  .location-text {
    font-size: 22rpx;
    color: #9CA3AF;
    margin-left: 4rpx;
  }
}

.card-desc {
  font-size: 24rpx;
  color: #9CA3AF;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 8rpx;
}

.action-btn {
  position: absolute;
  bottom: 24rpx;
  right: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 20rpx;
  background: rgba(46, 125, 50, 0.08);
  border-radius: 16rpx;
  border: 1rpx solid rgba(46, 125, 50, 0.2);
  
  .btn-text {
    font-size: 20rpx;
    color: #2E7D32;
    margin-top: 4rpx;
    font-weight: 500;
  }
}
</style>
