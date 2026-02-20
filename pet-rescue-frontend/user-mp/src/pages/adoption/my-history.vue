<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">申请记录</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <view class="section-header" v-if="dataList.length > 0">
        <text class="section-title">最近申请 ({{ dataList.length }})</text>
      </view>
      
      <u-list @scrolltolower="loadMore" v-if="dataList.length > 0">
        <u-list-item v-for="(item, index) in dataList" :key="index">
          <view class="apply-card" @click="handleItemClick(item)">
            <view class="card-left">
              <image :src="item.petInfo?.coverImg || item.petCover || '/static/logo.png'" mode="aspectFill" class="pet-img"></image>
            </view>
            <view class="card-right">
              <view class="card-header">
                <view class="title-area">
                  <text class="pet-name">{{ item.petInfo?.name || item.petName || '宠物' }}</text>
                  <text class="apply-no">申请编号: #{{ item.id }}</text>
                </view>
                <view class="status-tag" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </view>
              </view>
              <view class="card-info" v-if="item.petInfo">
                <text class="info-item">{{ item.petInfo.breed || '未知品种' }}</text>
                <text class="divider">·</text>
                <text class="info-item">{{ item.petInfo.age || '未知年龄' }}</text>
              </view>
              <view class="card-footer">
                <text class="time">{{ item.createTime }}</text>
                <view class="detail-btn">
                  <text class="btn-text">查看详情</text>
                  <uni-icons type="right" size="12" color="#2E7D32"></uni-icons>
                </view>
              </view>
            </view>
          </view>
          
          <view class="reject-reason" v-if="item.status === 2 && item.adminRemark">
            <uni-icons type="info" size="14" color="#ff4d4f"></uni-icons>
            <text class="reason-text">驳回原因: {{ item.adminRemark }}</text>
          </view>
        </u-list-item>
      </u-list>
      <u-empty v-else mode="list" text="暂无申请记录" marginTop="100"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMyAdoptionApplications } from '@/api/adoption'
import { getPetDetail } from '@/api/pet'

const statusBarHeight = ref(44)
const dataList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  fetchData()
})

const goBack = () => {
  uni.navigateBack()
}

onShow(() => {
  pageNum.value = 1
  fetchData()
})

const handleItemClick = (item) => {
  uni.navigateTo({
    url: `/pages/adoption/application-detail?id=${item.id}`
  })
}

const fetchPetDetail = async (petId) => {
  try {
    const res = await getPetDetail(petId)
    if (res.data) {
      return res.data
    }
  } catch (error) {
    console.error('获取宠物详情失败', error)
  }
  return null
}

const fetchData = async () => {
  if (pageNum.value === 1) uni.showLoading({ title: '加载中' })
  try {
    const res = await getMyAdoptionApplications({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    if (res && res.data && res.data.records) {
      const records = res.data.records
      
      const promises = records.map(async (item) => {
        if (item.petId) {
          const petInfo = await fetchPetDetail(item.petId)
          return { ...item, petInfo }
        }
        return item
      })
      
      const enrichedRecords = await Promise.all(promises)
      
      if (pageNum.value === 1) {
        dataList.value = enrichedRecords
      } else {
        dataList.value = [...dataList.value, ...enrichedRecords]
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const loadMore = () => {
  if (dataList.value.length < total.value) {
    pageNum.value++
    fetchData()
  }
}

const getStatusText = (status) => {
  const map = { 0: '审核中', 1: '已通过', 2: '未通过', 3: '已取消', 4: '已领养' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'pending', 1: 'passed', 2: 'rejected', 3: 'cancelled' }
  return map[status] || 'pending'
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

.section-header {
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 24rpx;
    font-weight: 600;
    color: #6B7280;
    text-transform: uppercase;
    letter-spacing: 1rpx;
  }
}

.apply-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 16rpx;
  display: flex;
  gap: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-left {
  .pet-img {
    width: 140rpx;
    height: 140rpx;
    border-radius: 16rpx;
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
  align-items: flex-start;
  
  .title-area {
    flex: 1;
    min-width: 0;
    
    .pet-name {
      font-size: 32rpx;
      font-weight: 600;
      color: #1F2937;
      display: block;
    }
    
    .apply-no {
      font-size: 22rpx;
      color: #6B7280;
      margin-top: 6rpx;
      display: block;
    }
  }
  
  .status-tag {
    font-size: 20rpx;
    font-weight: 600;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    text-transform: uppercase;
    letter-spacing: 0.5rpx;
    flex-shrink: 0;
    
    &.pending {
      background: rgba(255, 193, 7, 0.2);
      color: #B45309;
    }
    
    &.passed {
      background: rgba(46, 125, 50, 0.1);
      color: #2E7D32;
    }
    
    &.rejected {
      background: #f3f4f6;
      color: #6B7280;
    }
    
    &.cancelled {
      background: #f3f4f6;
      color: #9CA3AF;
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

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  
  .time {
    font-size: 22rpx;
    color: #9CA3AF;
  }
  
  .detail-btn {
    display: flex;
    align-items: center;
    padding: 10rpx 20rpx;
    background: #2E7D32;
    border-radius: 12rpx;
    
    .btn-text {
      font-size: 22rpx;
      font-weight: 600;
      color: #fff;
      margin-right: 4rpx;
    }
  }
}

.reject-reason {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 28rpx;
  margin-bottom: 16rpx;
  margin-top: -8rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  
  .reason-text {
    font-size: 24rpx;
    color: #ff4d4f;
  }
}
</style>
