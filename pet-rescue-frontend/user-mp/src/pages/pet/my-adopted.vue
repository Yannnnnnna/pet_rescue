<template>
  <view class="page-container">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">我的爱宠</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view v-if="loading" class="loading-state">
        <u-loading-icon mode="circle" text="加载中..."></u-loading-icon>
      </view>
      
      <block v-else-if="petList.length > 0">
        <view class="stats-section">
          <view class="stats-left">
            <text class="stats-label">感谢您的爱心守护</text>
            <view class="stats-title">
              <text>已守护</text>
              <text class="stats-num">{{ petList.length }}</text>
              <text>只宝贝</text>
            </view>
          </view>
          <view class="stats-icon">
            <uni-icons type="heart-filled" size="32" color="#FFC107"></uni-icons>
          </view>
        </view>

        <swiper 
          class="pet-swiper" 
          :indicator-dots="petList.length > 1"
          indicator-color="rgba(255,255,255,0.5)"
          indicator-active-color="#FFC107"
          previous-margin="40rpx"
          next-margin="40rpx"
        >
          <swiper-item v-for="(pet, index) in petList" :key="pet.id">
            <view class="pet-card" :class="{ 'active': index === currentIndex }">
              <view class="card-image-box">
                <image :src="pet.cover" mode="aspectFill" class="pet-image"></image>
                <view class="gender-badge" :class="pet.sex === 1 ? 'male' : 'female'">
                  <uni-icons :type="pet.sex === 1 ? 'person' : 'person-filled'" size="14" color="#fff"></uni-icons>
                </view>
                <view class="days-badge">
                  <uni-icons type="calendar" size="14" color="#FFC107"></uni-icons>
                  <text>已陪伴 {{ calculateDays(pet.adoptionTime) }} 天</text>
                </view>
              </view>

              <view class="card-content">
                <view class="pet-info">
                  <view class="pet-header">
                    <text class="pet-name">{{ pet.name }}</text>
                    <view class="breed-tag">
                      <text>{{ pet.breed }}</text>
                    </view>
                  </view>
                </view>

                <view class="milestone-row">
                  <view class="milestone-item">
                    <text class="m-label">领养日期</text>
                    <text class="m-value">{{ formatDate(pet.adoptionTime) }}</text>
                  </view>
                  <view class="milestone-divider"></view>
                  <view class="milestone-item highlight">
                    <text class="m-label">陪伴天数</text>
                    <view class="days-group">
                      <text class="days-num">{{ calculateDays(pet.adoptionTime) }}</text>
                      <text class="days-unit">天</text>
                    </view>
                  </view>
                </view>

                <view class="vaccine-row">
                  <uni-icons 
                    :type="pet.isVaccinated ? 'checkbox-filled' : 'info'" 
                    size="16" 
                    :color="pet.isVaccinated ? '#19be6b' : '#999'"
                  ></uni-icons>
                  <text :class="{ 'vaccinated': pet.isVaccinated }">
                    {{ pet.isVaccinated ? '已完成疫苗接种' : '疫苗接种状态未知' }}
                  </text>
                </view>

                <view class="action-row">
                  <view class="action-btn publish" @click="handlePublish(pet)">
                    <uni-icons type="compose" size="20" color="#fff"></uni-icons>
                    <text>记录点滴</text>
                  </view>
                  <view class="action-btn history" @click="handleViewHistory(pet)">
                    <uni-icons type="list" size="20" color="#2E7D32"></uni-icons>
                    <text>成长记录</text>
                  </view>
                </view>

                <view class="guardian-section">
                  <view class="guardian-left">
                    <text class="section-label">守护天使 (送养人)</text>
                    <view class="guardian-user">
                      <image :src="pet.publisherAvatar || '/static/logo.png'" class="guardian-avatar"></image>
                      <text class="guardian-name">{{ pet.publisherNickname || '爱心人士' }}</text>
                    </view>
                  </view>
                  <view class="contact-btn" @click="handleContact(pet)">
                    <uni-icons type="chat" size="18" color="#fff"></uni-icons>
                    <text>联系TA</text>
                  </view>
                </view>

                <view class="archive-links">
                  <view class="link-item" @click="viewApplication(pet)">
                    <uni-icons type="paperclip" size="18" color="#666"></uni-icons>
                    <text>查看申请表快照</text>
                    <uni-icons type="right" size="14" color="#ccc"></uni-icons>
                  </view>
                  <view class="link-item" @click="viewAgreement(pet)">
                    <uni-icons type="auth-filled" size="18" color="#666"></uni-icons>
                    <text>电子领养协议</text>
                    <uni-icons type="right" size="14" color="#ccc"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </block>
      
      <u-empty v-else mode="data" icon="http://cdn.uviewui.com/uview/empty/data.png" text="您还没有领养宠物哦"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyAdoptedPets } from '@/api/pet'
import { getMyAdoptionApplications } from '@/api/adoption'
import dayjs from 'dayjs'

const petList = ref([])
const loading = ref(true)
const statusBarHeight = ref(20)
const currentIndex = ref(0)

onLoad(() => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  fetchData()
})

const fetchData = async () => {
  try {
    const res = await getMyAdoptedPets()
    if (res.data) {
      petList.value = res.data
    }
  } catch (error) {
    console.error('获取领养宠物失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goBack = () => uni.navigateBack()

const handlePublish = (pet) => {
  uni.navigateTo({
    url: `/pages/post/publish?petId=${pet.id}&petName=${pet.name}`
  })
}

const handleViewHistory = (pet) => {
  uni.navigateTo({
    url: `/pages/pet/diary-list?petId=${pet.id}`
  })
}

const formatDate = (date) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY年MM月DD日')
}

const calculateDays = (date) => {
  if (!date) return 0
  const start = dayjs(date)
  const now = dayjs()
  const days = now.diff(start, 'day')
  return days >= 0 ? days : 0
}

const handleContact = (pet) => {
  if (pet.publisherId) {
    uni.navigateTo({
      url: `/pages/message/chat-history?petId=${pet.id}`
    })
  } else {
    uni.showToast({ title: '无法获取送养人信息', icon: 'none' })
  }
}

const viewApplication = async (pet) => {
  uni.showLoading({ title: '加载中' })
  try {
    const res = await getMyAdoptionApplications({ petId: pet.id })
    let apps = res.data
    if (apps && apps.records) apps = apps.records
    if (apps && Array.isArray(apps)) {
      const passedApp = apps.find(app => 
        String(app.petId) === String(pet.id) && 
        (app.status === 1 || app.status === 4)
      )
      if (passedApp) {
        uni.hideLoading()
        uni.navigateTo({ url: `/pages/adoption/application-detail?id=${passedApp.id}` })
      } else {
        uni.hideLoading()
        uni.showToast({ title: '未找到申请记录', icon: 'none' })
      }
    } else {
      uni.hideLoading()
      uni.showToast({ title: '未找到申请记录', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '获取申请记录失败', icon: 'none' })
  }
}

const viewAgreement = (pet) => {
  getApp().globalData.agreementData = {
    signatureImg: pet.signatureImg,
    signTime: pet.signTime
  }
  uni.navigateTo({
    url: `/pages/adoption/sign-agreement?petId=${pet.id}&mode=view`
  })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 450rpx;
  background: linear-gradient(180deg, #2E7D32 0%, #388E3C 60%, rgba(46, 125, 50, 0) 100%);
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
  padding-bottom: 40rpx;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}

.stats-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  margin-bottom: 24rpx;
  
  .stats-left {
    .stats-label {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 8rpx;
      display: block;
    }
    
    .stats-title {
      display: flex;
      align-items: baseline;
      gap: 8rpx;
      color: #fff;
      font-size: 32rpx;
      font-weight: 600;
      
      .stats-num {
        font-size: 48rpx;
        font-weight: 700;
        color: #FFC107;
      }
    }
  }
  
  .stats-icon {
    width: 100rpx;
    height: 100rpx;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
    border: 2rpx solid rgba(255, 255, 255, 0.2);
  }
}

.pet-swiper {
  height: calc(100vh - 280rpx);
}

.pet-card {
  background: #fff;
  border-radius: 40rpx;
  overflow: hidden;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
  height: 98%;
  margin: 0 10rpx;
  display: flex;
  flex-direction: column;
  
  .card-image-box {
    position: relative;
    width: 100%;
    height: 380rpx;
    
    .pet-image {
      width: 100%;
      height: 100%;
    }
    
    .gender-badge {
      position: absolute;
      top: 20rpx;
      right: 20rpx;
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      
      &.male {
        background: rgba(66, 165, 245, 0.9);
      }
      
      &.female {
        background: rgba(236, 64, 122, 0.9);
      }
    }
    
    .days-badge {
      position: absolute;
      bottom: 20rpx;
      left: 20rpx;
      background: rgba(46, 125, 50, 0.9);
      backdrop-filter: blur(10px);
      padding: 10rpx 20rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      text {
        font-size: 24rpx;
        color: #fff;
        font-weight: 500;
      }
    }
  }
  
  .card-content {
    flex: 1;
    padding: 30rpx;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
  }
  
  .pet-info {
    margin-bottom: 24rpx;
    
    .pet-header {
      display: flex;
      align-items: center;
      gap: 16rpx;
      
      .pet-name {
        font-size: 40rpx;
        font-weight: 700;
        color: #2E7D32;
      }
      
      .breed-tag {
        background: rgba(46, 125, 50, 0.1);
        padding: 8rpx 20rpx;
        border-radius: 20rpx;
        
        text {
          font-size: 24rpx;
          color: #2E7D32;
          font-weight: 500;
        }
      }
    }
  }
  
  .milestone-row {
    display: flex;
    align-items: center;
    background: #f8f9fa;
    border-radius: 24rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    
    .milestone-item {
      flex: 1;
      text-align: center;
      
      .m-label {
        display: block;
        font-size: 24rpx;
        color: #999;
        margin-bottom: 8rpx;
      }
      
      .m-value {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
      }
      
      &.highlight {
        .days-group {
          display: flex;
          align-items: baseline;
          justify-content: center;
          gap: 4rpx;
        }
        
        .days-num {
          font-size: 44rpx;
          color: #FFC107;
          font-weight: 700;
        }
        
        .days-unit {
          font-size: 24rpx;
          color: #FFC107;
        }
      }
    }
    
    .milestone-divider {
      width: 2rpx;
      height: 60rpx;
      background: #e0e0e0;
    }
  }
  
  .vaccine-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    margin-bottom: 24rpx;
    
    text {
      font-size: 26rpx;
      color: #999;
      
      &.vaccinated {
        color: #19be6b;
      }
    }
  }
  
  .action-row {
    display: flex;
    gap: 20rpx;
    margin-bottom: 24rpx;
    
    .action-btn {
      flex: 1;
      height: 88rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10rpx;
      font-size: 28rpx;
      font-weight: 600;
      
      &.publish {
        background: linear-gradient(135deg, #FFC107, #FFD54F);
        color: #2E7D32;
        box-shadow: 0 8rpx 24rpx rgba(255, 193, 7, 0.3);
      }
      
      &.history {
        background: #fff;
        color: #2E7D32;
        border: 2rpx solid #2E7D32;
      }
    }
  }
  
  .guardian-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    background: rgba(46, 125, 50, 0.05);
    border-radius: 24rpx;
    margin-bottom: 20rpx;
    
    .guardian-left {
      .section-label {
        font-size: 22rpx;
        color: #888;
        display: block;
        margin-bottom: 12rpx;
      }
      
      .guardian-user {
        display: flex;
        align-items: center;
        gap: 16rpx;
        
        .guardian-avatar {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          background: #eee;
        }
        
        .guardian-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }
      }
    }
    
    .contact-btn {
      background: #2E7D32;
      padding: 16rpx 28rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      gap: 8rpx;
      box-shadow: 0 6rpx 16rpx rgba(46, 125, 50, 0.3);
      
      text {
        font-size: 26rpx;
        color: #fff;
        font-weight: 500;
      }
    }
  }
  
  .archive-links {
    border-top: 1rpx solid #f0f0f0;
    padding-top: 16rpx;
    
    .link-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 20rpx 0;
      
      text {
        flex: 1;
        font-size: 28rpx;
        color: #666;
      }
      
      &:active {
        opacity: 0.7;
      }
    }
  }
}
</style>
