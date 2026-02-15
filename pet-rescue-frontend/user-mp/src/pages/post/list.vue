<template>
  <view class="post-list-page">
    <view class="list-container">
      <view class="post-item" v-for="item in dataList" :key="item.id" @click="goDetail(item.id)">
        <view class="header">
          <u-avatar :src="item.userAvatar" size="32"></u-avatar>
          <view class="user-info">
            <text class="nickname">{{ item.userName }}</text>
            <text class="time">{{ formatTime(item.createTime) }}</text>
          </view>
        </view>
        
        <view class="content">
          <text class="text">{{ item.content }}</text>
        </view>
        
        <view class="images" v-if="item.imageList && item.imageList.length > 0">
          <view class="img-grid" :class="'grid-' + (item.imageList.length > 3 ? 3 : item.imageList.length)">
            <image 
              v-for="(img, idx) in item.imageList" 
              :key="idx" 
              :src="img" 
              mode="aspectFill" 
              class="img-item"
              @click.stop="previewImage(item.imageList, idx)"
            ></image>
          </view>
        </view>
        
        <view class="footer">
          <view class="pet-tag">
            <u-icon name="aixin" custom-prefix="custom-icon" size="16" color="#19be6b"></u-icon>
            <text class="pet-name">关联: {{ item.petName }}</text>
          </view>
          <view class="actions">
            <view class="action-btn">
              <u-icon name="dianzan" custom-prefix="custom-icon" size="20" color="#666"></u-icon>
              <text class="count">{{ item.likeCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getAdoptionPostList } from '@/api/adoption-post'
import { getUserDetail } from '@/api/user'
import { getPetDetail } from '@/api/pet'
import dayjs from 'dayjs'

const dataList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const loadStatus = ref('loadmore')

onLoad(() => {
  fetchData(true)
})

onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  pageNum.value++
  fetchData()
})

const fetchData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    dataList.value = []
    loadStatus.value = 'loading'
  }
  
  try {
    const res = await getAdoptionPostList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      auditStatus: 1
    })
    
    const list = Array.isArray(res.data) ? res.data : (res.data && res.data.records ? res.data.records : [])
    
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
      
      let userAvatar = 'https://uview-plus.jiangruyi.com/common/logo.png'
      let userName = '爱心用户'
      let petName = ''
      
      if (item.userId) {
        try {
          const userRes = await getUserDetail(item.userId)
          if (userRes.data) {
            userAvatar = userRes.data.avatar || userAvatar
            userName = userRes.data.nickname || userName
          }
        } catch (e) {
          console.error('获取用户信息失败', e)
        }
      }
      
      if (item.petId) {
        try {
          const petRes = await getPetDetail(item.petId)
          if (petRes.data) {
            petName = petRes.data.name || ''
          }
        } catch (e) {
          console.error('获取宠物信息失败', e)
        }
      }
      
      return {
        ...item,
        imageList,
        userAvatar,
        userName,
        petName
      }
    }))
    
    if (reset) {
      dataList.value = processedList
    } else {
      dataList.value = [...dataList.value, ...processedList]
    }
    
    if (list.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
    
  } catch (e) {
    console.error(e)
    loadStatus.value = 'nomore'
  }
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

const previewImage = (urls, current) => {
  uni.previewImage({
    urls,
    current
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.post-list-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #e6f2e6, #f5f5f5);
  padding: 20rpx;
  box-sizing: border-box;
}

.post-item {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 32rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 16rpx rgba(0,0,0,0.05);
  backdrop-filter: blur(10px);
  
  .header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .user-info {
      margin-left: 20rpx;
      display: flex;
      flex-direction: column;
      
      .nickname {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }
      
      .time {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }
  }
  
  .content {
    margin-bottom: 20rpx;
    
    .text {
      font-size: 30rpx;
      color: #333;
      line-height: 1.6;
    }
  }
  
  .images {
    margin-bottom: 20rpx;
    
    .img-grid {
      display: grid;
      gap: 10rpx;
      
      &.grid-1 {
        grid-template-columns: 1fr;
        .img-item {
          width: 400rpx;
          height: 400rpx;
        }
      }
      
      &.grid-2 {
        grid-template-columns: repeat(2, 1fr);
        .img-item {
          width: 100%;
          height: 300rpx;
        }
      }
      
      &.grid-3 {
        grid-template-columns: repeat(3, 1fr);
        .img-item {
          width: 100%;
          height: 200rpx;
        }
      }
      
      .img-item {
        border-radius: 8rpx;
      }
    }
  }
  
  .footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .pet-tag {
      display: flex;
      align-items: center;
      background: #f0f9eb;
      padding: 6rpx 16rpx;
      border-radius: 24rpx;
      
      .pet-name {
        font-size: 24rpx;
        color: #19be6b;
        margin-left: 8rpx;
      }
    }
    
    .actions {
      .action-btn {
        display: flex;
        align-items: center;
        
        .count {
          font-size: 26rpx;
          color: #666;
          margin-left: 8rpx;
        }
      }
    }
  }
}
</style>
