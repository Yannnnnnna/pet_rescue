<template>
  <view class="page-container">
    <u-navbar title="我的爱宠" :autoBack="true" bgColor="transparent" leftIconColor="#333" titleStyle="color: #333; font-weight: 600;"></u-navbar>
    
    <view class="content-area" :style="{ paddingTop: navHeight + 'px' }">
      <view v-if="loading" class="loading-state">
        <u-loading-icon mode="circle" text="加载中..."></u-loading-icon>
      </view>
      
      <block v-else-if="petList.length > 0">
        <swiper class="pet-swiper" :indicator-dots="petList.length > 1" previous-margin="30rpx" next-margin="30rpx">
          <swiper-item v-for="(pet, index) in petList" :key="pet.id">
            <view class="pet-card-container">
              <!-- 电子身份证区域 -->
              <view class="identity-card">
                <view class="card-header">
                  <view class="header-left">
                    <text class="card-title">PET IDENTITY</text>
                    <text class="card-subtitle">专属电子档案</text>
                  </view>
                  <view class="header-right">
                    <text class="pet-no">No.{{ generatePetNo(pet) }}</text>
                  </view>
                </view>

                <view class="avatar-box">
                  <image :src="pet.cover" mode="aspectFill" class="pet-avatar"></image>
                  <view class="gender-badge" :class="pet.sex === 1 ? 'male' : 'female'">
                    <u-icon :name="pet.sex === 1 ? 'man' : 'woman'" color="#fff" size="16"></u-icon>
                  </view>
                </view>

                <view class="info-primary">
                  <text class="pet-name">{{ pet.name }}</text>
                  <text class="pet-breed">{{ pet.breed }}</text>
                </view>

                <view class="milestone-section">
                  <view class="milestone-item">
                    <text class="m-label">领养日期</text>
                    <text class="m-value">{{ formatDate(pet.adoptionTime) }}</text>
                  </view>
                  <view class="divider"></view>
                  <view class="milestone-item highlight">
                    <text class="m-label">陪伴天数</text>
                    <view class="m-value-group">
                      <text class="days-num">{{ calculateDays(pet.adoptionTime) }}</text>
                      <text class="days-unit">天</text>
                    </view>
                  </view>
                </view>
                
                <view class="vaccine-status">
                   <u-icon name="checkmark-circle-fill" :color="pet.isVaccinated ? '#19be6b' : '#909399'" size="18"></u-icon>
                   <text :class="{ active: pet.isVaccinated }">{{ pet.isVaccinated ? '已完成疫苗接种' : '疫苗接种状态未知' }}</text>
                </view>
              </view>

              <!-- 溯源区域 -->
              <view class="roots-section">
                <view class="guardian-row">
                  <view class="guardian-info">
                    <text class="section-label">守护天使 (送养人)</text>
                    <view class="user-box">
                      <image :src="pet.publisherAvatar || '/static/logo.png'" class="guardian-avatar"></image>
                      <text class="guardian-name">{{ pet.publisherNickname || '爱心人士' }}</text>
                    </view>
                  </view>
                  <button class="contact-btn" @click="handleContact(pet)">
                    <u-icon name="chat" color="#fff" size="20"></u-icon>
                    <text>联系TA</text>
                  </button>
                </view>
                
                <view class="archive-links">
                  <view class="link-item" @click="viewApplication(pet)">
                    <u-icon name="file-text" color="#606266" size="20"></u-icon>
                    <text>查看申请表快照</text>
                    <u-icon name="arrow-right" color="#ccc" size="14"></u-icon>
                  </view>
                  <view class="link-item" @click="viewAgreement(pet)">
                    <u-icon name="order" color="#606266" size="20"></u-icon>
                    <text>电子领养协议</text>
                    <u-icon name="arrow-right" color="#ccc" size="14"></u-icon>
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
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyAdoptedPets } from '@/api/pet'
import dayjs from 'dayjs'

const petList = ref([])
const loading = ref(true)
const navHeight = ref(44) // 默认值，实际应该获取系统状态栏高度

onLoad(() => {
  const sysInfo = uni.getSystemInfoSync()
  navHeight.value = sysInfo.statusBarHeight + 44
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

const generatePetNo = (pet) => {
  if (!pet.adoptionTime) return `NO.${pet.id}`
  const dateStr = dayjs(pet.adoptionTime).format('YYYYMMDD')
  // padding ID to 3 digits
  const idStr = String(pet.id).padStart(3, '0')
  return `${dateStr}-${idStr}`
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
    // 我是领养人(Adopter)，联系送养人(Publisher)
    // 此时 applicantId 应为我自己。chat-history 中如果不传 applicantId，默认使用当前登录用户
    // 所以只需要传 petId 即可，或者显式传 petId
    uni.navigateTo({
      url: `/pages/message/chat-history?petId=${pet.id}`
    })
  } else {
    uni.showToast({ title: '无法获取送养人信息', icon: 'none' })
  }
}

const viewApplication = (pet) => {
   // 跳转到申请详情
   uni.navigateTo({ url: '/pages/adoption/my-history' })
}

const viewAgreement = (pet) => {
  uni.showToast({ title: '暂无电子协议', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #fce4ec; /* 浅粉色背景 */
  background: linear-gradient(180deg, #fce4ec 0%, #f3e5f5 100%);
}

.content-area {
  height: 100vh;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}

.pet-swiper {
  height: 100%;
}

.pet-card-container {
  height: 100%;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  overflow-y: auto;
}

/* 电子身份证 */
.identity-card {
  background: #fff;
  border-radius: 30rpx;
  padding: 40rpx;
  box-shadow: 0 16rpx 40rpx rgba(233, 30, 99, 0.1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50rpx;
    right: -50rpx;
    width: 200rpx;
    height: 200rpx;
    background: rgba(233, 30, 99, 0.05);
    border-radius: 50%;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40rpx;
  
  .header-left {
    display: flex;
    flex-direction: column;
  }
  
  .card-title {
    font-size: 24rpx;
    color: #999;
    letter-spacing: 2rpx;
    font-weight: 700;
  }
  
  .card-subtitle {
    font-size: 32rpx;
    color: #333;
    font-weight: 600;
  }
  
  .pet-no {
    font-family: monospace;
    font-size: 26rpx;
    color: #e91e63;
    background: rgba(233, 30, 99, 0.1);
    padding: 6rpx 16rpx;
    border-radius: 10rpx;
  }
}

.avatar-box {
  display: flex;
  justify-content: center;
  position: relative;
  margin-bottom: 30rpx;
  
  .pet-avatar {
    width: 240rpx;
    height: 240rpx;
    border-radius: 50%;
    border: 8rpx solid #fff;
    box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.15);
  }
  
  .gender-badge {
    position: absolute;
    bottom: 10rpx;
    right: 32%; /* 大致对齐 */
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4rpx solid #fff;
    
    &.male { background: #42a5f5; }
    &.female { background: #ec407a; }
  }
}

.info-primary {
  text-align: center;
  margin-bottom: 40rpx;
  
  .pet-name {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #333;
    margin-bottom: 8rpx;
  }
  
  .pet-breed {
    font-size: 26rpx;
    color: #666;
    background: #f5f5f5;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
  }
}

.milestone-section {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 20rpx;
  padding: 30rpx;
  
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
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
    }
    
    &.highlight {
      .m-value-group {
        display: flex;
        align-items: baseline;
        justify-content: center;
        gap: 4rpx;
      }
      
      .days-num {
        font-size: 40rpx;
        color: #e91e63;
        font-weight: 700;
      }
      
      .days-unit {
        font-size: 24rpx;
        color: #e91e63;
      }
    }
  }
  
  .divider {
    width: 2rpx;
    height: 60rpx;
    background: #e0e0e0;
    margin: 0 10rpx;
  }
}

.vaccine-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  margin-top: 30rpx;
  font-size: 24rpx;
  
  text {
    color: #909399;
    &.active { color: #19be6b; }
  }
}


/* 溯源区域 */
.roots-section {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 30rpx;
  padding: 30rpx;
}

.guardian-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  
  .guardian-info {
    .section-label {
      font-size: 22rpx;
      color: #888;
      display: block;
      margin-bottom: 12rpx;
    }
    
    .user-box {
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
    margin: 0;
    background: #4caf50;
    color: #fff;
    font-size: 26rpx;
    height: 64rpx;
    line-height: 64rpx;
    padding: 0 24rpx;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    gap: 8rpx;
    box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
    
    &::after { border: none; }
  }
}

.archive-links {
  border-top: 1rpx solid rgba(0,0,0,0.05);
  padding-top: 20rpx;
  
  .link-item {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 20rpx 0;
    
    text {
      flex: 1;
      font-size: 28rpx;
      color: #606266;
    }
    
    &:active {
      opacity: 0.7;
    }
  }
}
</style>
