<template>
  <view class="container" v-if="pet">
    <!-- Custom Navbar -->
    <u-navbar
      :autoBack="true"
      bgColor="transparent"
      leftIconColor="#fff"
      :fixed="true"
      :placeholder="false"
      title=""
    >
      <template #left>
        <view class="nav-back-btn" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="20"></u-icon>
        </view>
      </template>
    </u-navbar>

    <!-- Image Carousel -->
    <view class="carousel-section">
      <swiper 
        class="swiper" 
        circular 
        autoplay 
        interval="4000" 
        duration="500"
        @change="onSwiperChange"
      >
        <swiper-item v-if="pet.detailImgList && pet.detailImgList.length > 0" v-for="(img, index) in parseImages(pet.detailImgList)" :key="index">
          <image :src="img" mode="aspectFill" class="swiper-img" @click="previewImage(index)"></image>
        </swiper-item>
        <swiper-item v-else>
           <image :src="pet.coverImg" mode="aspectFill" class="swiper-img"></image>
        </swiper-item>
      </swiper>
      
      <!-- Custom Dots -->
      <view class="swiper-dots" v-if="imgList.length > 1">
        <view 
          v-for="(item, index) in imgList" 
          :key="index"
          class="dot" 
          :class="{ active: currentSwiperIndex === index }"
        ></view>
      </view>
    </view>

    <!-- Content Sheet -->
    <view class="content-sheet">
      <!-- Header Info -->
      <view class="header-section">
        <view class="info-left">
          <text class="pet-name">{{ pet.name }}</text>
          <view class="meta-row">
            <view class="meta-item">
              <u-icon :name="pet.sex === 1 ? 'man' : 'woman'" :color="pet.sex === 1 ? '#2979ff' : '#ff9c00'" size="18"></u-icon>
              <text class="meta-text">{{ pet.sex === 1 ? '公' : (pet.sex === 0 ? '母' : '未知') }} · {{ pet.age }}</text>
            </view>
            <view class="meta-item" v-if="displayAddress">
              <u-icon name="map-fill" color="#2E7D32" size="18"></u-icon>
              <text class="meta-text">{{ displayAddress }}</text>
            </view>
          </view>
        </view>
        
        <!-- Rescuer / Contact -->
        <view class="rescuer-info" v-if="pet.publisherAvatar || pet.publisherName">
          <image :src="pet.publisherAvatar || '/static/default-avatar.png'" class="rescuer-avatar" mode="aspectFill"></image>
          <text class="rescuer-name">{{ pet.publisherName || '爱心救助人' }}</text>
        </view>
      </view>

      <!-- Health Tags -->
      <scroll-view scroll-x class="tags-scroll" :show-scrollbar="false">
        <view class="tags-wrapper">
          <view class="health-tag" v-if="pet.isVaccinated === 1">
            <u-icon name="checkmark-circle-fill" color="#fff" size="14"></u-icon>
            <text>已疫苗</text>
          </view>
          <view class="health-tag" v-if="pet.isSterilized === 1">
            <u-icon name="checkmark-circle-fill" color="#fff" size="14"></u-icon>
            <text>已绝育</text>
          </view>
          <view class="health-tag" v-if="pet.isDewormed === 1">
             <u-icon name="checkmark-circle-fill" color="#fff" size="14"></u-icon>
             <text>已驱虫</text>
          </view>
           <view class="health-tag" v-else>
             <u-icon name="info-circle-fill" color="#fff" size="14"></u-icon>
             <text>健康状况良好</text>
          </view>
        </view>
      </scroll-view>

      <!-- Personality Tags -->
      <view class="section-block" v-if="tagList.length > 0">
        <text class="section-label">性格标签</text>
        <view class="personality-tags">
          <view 
            class="p-tag" 
            v-for="(tag, index) in tagList" 
            :key="index"
            :class="`tag-color-${index % 4}`"
          >
            {{ tag }}
          </view>
        </view>
      </view>
      
      <!-- Interaction / Feed (Added Feature) -->
      <view class="section-block interaction-block">
        <view class="feed-stats">
           <u-icon name="xiaoyugan" custom-prefix="custom-icon" color="#ff9c00" size="20"></u-icon>
           <text class="feed-count">{{ pet.lovePoint || 0 }} 份小鱼干</text>
        </view>
        <view class="feed-btn" @click="handleFeed">
           <image src="../../static/小鱼干.png" style="width: 32rpx; height: 32rpx;" mode="aspectFit"></image>
           <text>投喂</text>
        </view>
      </view>

      <!-- About Section -->
      <view class="section-block about-block">
        <view class="title-row">
          <view class="accent-bar"></view>
          <text class="section-title">关于 {{ pet.name }}</text>
        </view>
        <text class="about-text">{{ pet.description || '暂无详细介绍' }}</text>
      </view>
      
      <!-- Spacer for bottom bar -->
      <view class="bottom-spacer"></view>
    </view>

    <!-- Bottom Action Bar -->
    <view class="bottom-bar">
      <!-- Left: Favorite -->
      <view class="fav-btn" @click="toggleFav">
        <u-icon 
          :name="isFavorited ? 'shoucang' : 'shoucang'" 
          custom-prefix="custom-icon"
          :color="isFavorited ? '#ff4d4f' : '#999'" 
          size="28"
        ></u-icon>
      </view>

      <!-- Right Actions Group -->
      <view class="action-group">
        <template v-if="isOwner">
           <!-- Owner Actions -->
           <view class="action-btn secondary" v-if="pet.status !== 2" @click="handleEdit">
             <u-icon name="edit-pen" color="#333" size="18"></u-icon>
             <text>编辑</text>
           </view>
           <view class="action-btn danger" v-if="pet.status !== 2" @click="handleDelete">
             <u-icon name="trash" color="#fff" size="18"></u-icon>
             <text>删除</text>
           </view>
           <view class="action-btn success full-width" v-if="pet.status === 2" @click="handleReturnVisit">
             <u-icon name="xiaoxi" custom-prefix="custom-icon" color="#fff" size="18"></u-icon>
             <text>回访记录</text>
           </view>
        </template>
        
        <template v-else-if="isAdopter">
           <!-- Adopter Actions -->
           <view class="action-btn primary full-width" @click="handleViewAgreement">
             <u-icon name="file-text" color="#fff" size="18"></u-icon>
             <text>查看领养协议</text>
           </view>
        </template>
        
        <template v-else>
           <!-- User Actions -->
           <view class="action-btn warning" @click="handleConsult">
             <u-icon name="chat" color="#333" size="18"></u-icon>
             <text>咨询</text>
           </view>
           <view 
             class="action-btn success" 
             :class="{ disabled: pet.status !== 0 }"
             @click="handleApply"
           >
             <text>申请领养</text>
             <u-icon name="arrow-right" color="#fff" size="16"></u-icon>
           </view>
        </template>
      </view>
    </view>

    <!-- Feed Popup (Kept from original) -->
    <u-popup :show="showFeedPopup" @close="showFeedPopup = false" mode="bottom" round="16">
      <view class="feed-popup">
        <view class="popup-title">选择投喂数量</view>
        <view class="feed-options">
          <view 
            class="option-item" 
            v-for="num in feedOptions" 
            :key="num"
            :class="{ active: feedCount === num }"
            @click="feedCount = num"
          >
            <text class="num">{{ num }}</text>
            <text class="unit">个</text>
          </view>
        </view>
        <view class="popup-btn">
          <u-button 
            shape="circle" 
            color="linear-gradient(to right, #ff9c00, #ffc107)" 
            @click="confirmFeed"
          >
            确认投喂
          </u-button>
        </view>
      </view>
    </u-popup>

    <!-- Feeding Animation -->
    <view class="feeding-anim" :class="{ active: isFeedingAnim }">
       <u-icon name="xiaoyugan" custom-prefix="custom-icon" color="#ff9c00" size="60"></u-icon>
       <text class="plus-one">+{{ feedCount }}</text>
    </view>

  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetDetail, deletePet, toggleFavorite, checkFavorite, feedPet } from '@/api/pet'
import { getAdopterInfo, getAdoptionApplicationDetail, getMyAdoptionApplications } from '@/api/adoption'
import { getMyInfo } from '@/api/user'

const petId = ref(null)
const pet = ref(null)
const currentUserId = ref(null)
const urlIsOwner = ref(false)
const isFavorited = ref(false)
const isFeedingAnim = ref(false)
const showFeedPopup = ref(false)
const feedCount = ref(1)
const feedOptions = [1, 5, 10, 20]
const adopterInfo = ref(null)
const adoptionDetail = ref(null)
const currentSwiperIndex = ref(0)

onLoad((options) => {
  if (options.id) {
    petId.value = options.id
    if (options.isOwner === 'true') {
      urlIsOwner.value = true
    }
    fetchInfoAndDetail()
    checkFavStatus()
  }
})

const goBack = () => {
  uni.navigateBack()
}

const onSwiperChange = (e) => {
  currentSwiperIndex.value = e.detail.current
}

const checkFavStatus = async () => {
  try {
    const res = await checkFavorite(petId.value)
    if (res.data) {
      isFavorited.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const toggleFav = async () => {
  try {
    const res = await toggleFavorite({ petId: petId.value })
    if (res.code === 200) {
      isFavorited.value = !isFavorited.value
      uni.showToast({ title: isFavorited.value ? '已收藏' : '已取消收藏', icon: 'none' })
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchInfoAndDetail = async () => {
   try {
     const uReq = await getMyInfo()
     if (uReq.data) currentUserId.value = uReq.data.id
   } catch(e) {}
   
   fetchDetail()
}

const fetchDetail = async () => {
  uni.showLoading({ title: '加载中' })
  try {
    const res = await getPetDetail(petId.value)
    if (res.data) {
      pet.value = res.data
      
      if (res.data.status === 2 && currentUserId.value) {
        try {
          const myAppsRes = await getMyAdoptionApplications({ petId: petId.value })
          if (myAppsRes.data && myAppsRes.data.records) {
            const passedApp = myAppsRes.data.records.find(app => 
              app.petId === petId.value && app.status === 1 && app.agreementStatus === 1
            )
            if (passedApp) {
              adopterInfo.value = { id: currentUserId.value }
              adoptionDetail.value = passedApp
            }
          }
        } catch (e) {
          console.error('获取申请信息失败', e)
        }
      }
    }
  } catch (error) {
    console.error('加载失败', error)
    uni.showToast({ title: '详情加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const parseImages = (jsonStr) => {
  try {
    if (Array.isArray(jsonStr)) return jsonStr
    return JSON.parse(jsonStr)
  } catch (e) {
    return []
  }
}

const imgList = computed(() => {
  if (!pet.value) return []
  return parseImages(pet.value.detailImgList) || (pet.value.coverImg ? [pet.value.coverImg] : [])
})

const previewImage = (current) => {
  const urls = imgList.value
  uni.previewImage({
    current,
    urls
  })
}

// Keep original handler logic
const handleFeed = () => {
  showFeedPopup.value = true
}

const confirmFeed = async () => {
  try {
    const res = await feedPet({ petId: petId.value, feedCount: feedCount.value })
    if (res.code === 200) {
      showFeedPopup.value = false
      pet.value.lovePoint = (pet.value.lovePoint || 0) + feedCount.value
      
      isFeedingAnim.value = true
      setTimeout(() => {
        isFeedingAnim.value = false
      }, 1500)
      
      uni.showToast({ title: '投喂成功', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '投喂失败', icon: 'none' })
  }
}

const handleEdit = () => {
  uni.navigateTo({ url: `/pages/pet/edit?id=${petId.value}` })
}

const handleDelete = () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条宠物信息吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deletePet(petId.value)
          uni.showToast({ title: '删除成功' })
          setTimeout(() => uni.navigateBack(), 1500)
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const handleReturnVisit = () => {
  uni.navigateTo({ url: `/pages/pet/diary-list?petId=${petId.value}` })
}

const handleViewAgreement = () => {
  if (adoptionDetail.value && adoptionDetail.value.id) {
    uni.navigateTo({ url: `/pages/adoption/sign-agreement?applicationId=${adoptionDetail.value.id}&readonly=true` })
  }
}

const handleConsult = () => {
  if (!currentUserId.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: `/pages/message/chat-history?targetId=${pet.value.userId || pet.value.publisherId}` })
}

const handleApply = () => {
  if (!currentUserId.value) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: `/pages/adoption/apply?petId=${petId.value}` })
}

// Computed properties
const isOwner = computed(() => {
  if (urlIsOwner.value) return true
  if (!pet.value || !currentUserId.value) return false
  const uid = String(currentUserId.value)
  return (pet.value.userId && String(pet.value.userId) === uid) ||
         (pet.value.publisherId && String(pet.value.publisherId) === uid)
})

const isAdopter = computed(() => {
  if (!pet.value || !currentUserId.value || pet.value.status !== 2) return false
  return adopterInfo.value && adoptionDetail.value && adoptionDetail.value.agreementStatus === 1
})

const tagList = computed(() => {
  if (!pet.value || !pet.value.tags) return []
  return pet.value.tags.split(/[,，]/).filter(t => t && t.trim())
})

const displayAddress = computed(() => {
  if (!pet.value) return ''
  const parts = [pet.value.province, pet.value.city, pet.value.district].filter(p => p)
  return [...new Set(parts)].join(' ')
})
</script>

<style lang="scss" scoped>
.fish-icon {
  width: 40rpx;
  height: 40rpx;
}
.fish-icon-sm {
  width: 32rpx;
  height: 32rpx;
}
.fish-icon-lg {
  width: 120rpx;
  height: 120rpx;
}

.container {
  min-height: 100vh;
  background-color: #f8f8f8;
  position: relative;
}

.nav-back-btn {
  width: 72rpx;
  height: 72rpx;
  background-color: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-left: 20rpx;
}

/* Image Carousel */
.carousel-section {
  position: relative;
  height: 50vh;
  width: 100%;
}

.swiper {
  height: 100%;
  width: 100%;
}

.swiper-img {
  width: 100%;
  height: 100%;
}

.swiper-dots {
  position: absolute;
  bottom: 80rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12rpx;
  z-index: 10;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s;
  
  &.active {
    background-color: #fff;
    width: 32rpx;
    border-radius: 8rpx;
  }
}

/* Content Sheet */
.content-sheet {
  position: relative;
  margin-top: -60rpx;
  border-top-left-radius: 60rpx;
  border-top-right-radius: 60rpx;
  background-color: #fff;
  min-height: 60vh;
  padding: 48rpx;
  z-index: 20;
  padding-bottom: 200rpx; /* Space for fixed bottom bar */
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;
}

.pet-name {
  font-size: 56rpx;
  font-weight: 900;
  color: #0f172a;
  margin-bottom: 16rpx;
  display: block;
}

.meta-row {
  display: flex;
  gap: 32rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  
  .meta-text {
    font-size: 28rpx;
    font-weight: bold;
    color: #64748b;
  }
}

.rescuer-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.rescuer-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 4rpx solid #e8f5e9;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.rescuer-name {
  font-size: 24rpx;
  font-weight: bold;
  color: #94a3b8;
}

/* Tags */
.tags-scroll {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 40rpx;
}

.tags-wrapper {
  display: flex;
  gap: 16rpx;
}

.health-tag {
  background-color: #2E7D32;
  color: #fff;
  padding: 12rpx 32rpx;
  border-radius: 100rpx;
  font-size: 24rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

/* Section Blocks */
.section-block {
  margin-bottom: 40rpx;
}

.section-label {
  font-size: 24rpx;
  font-weight: bold;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  margin-bottom: 24rpx;
  display: block;
}

.personality-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.p-tag {
  padding: 16rpx 32rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: bold;
  
  &.tag-color-0 { background-color: #eff6ff; color: #2563eb; }
  &.tag-color-1 { background-color: #fdf2f8; color: #db2777; }
  &.tag-color-2 { background-color: #fff7ed; color: #ea580c; }
  &.tag-color-3 { background-color: #faf5ff; color: #9333ea; }
}

/* Interaction Block */
.interaction-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fffbeb;
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  border: 1px solid #fef3c7;
}

.feed-stats {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .feed-count {
    font-size: 28rpx;
    font-weight: bold;
    color: #b45309;
  }
}

.feed-btn {
  background: linear-gradient(to right, #ff9c00, #ffc107);
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 100rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(255, 156, 0, 0.3);
  
  &:active { transform: scale(0.95); }
}

/* About Section */
.title-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.accent-bar {
  width: 12rpx;
  height: 40rpx;
  background-color: #ffc107;
  border-radius: 100rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 900;
  color: #0f172a;
}

.about-text {
  font-size: 28rpx;
  color: #475569;
  line-height: 1.8;
  display: block;
}

/* Bottom Bar */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 30rpx 48rpx 60rpx 48rpx;
  border-top: 1px solid rgba(0,0,0,0.05);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.fav-btn {
  width: 100rpx;
  height: 100rpx;
  background-color: #f8fafc;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #f1f5f9;
  color: #94a3b8;
  
  &:active { background-color: #f1f5f9; }
}

.action-group {
  flex: 1;
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 100rpx;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-weight: bold;
  font-size: 30rpx;
  transition: transform 0.1s;
  
  &:active { transform: scale(0.98); }
  
  &.warning {
    background-color: #ffc107;
    color: #0f172a;
    box-shadow: 0 8rpx 20rpx rgba(255, 193, 7, 0.2);
  }
  
  &.success {
    background-color: #2E7D32;
    color: #fff;
    box-shadow: 0 8rpx 20rpx rgba(46, 125, 50, 0.2);
    flex: 1.5;
  }

  &.primary {
    background-color: #2979ff;
    color: #fff;
  }

  &.secondary {
    background-color: #f1f5f9;
    color: #333;
    border: 1px solid #e2e8f0;
  }

  &.danger {
    background-color: #ff4d4f;
    color: #fff;
  }
  
  &.full-width {
    width: 100%;
    flex: 1;
  }
  
  &.disabled {
    opacity: 0.6;
    pointer-events: none;
    background-color: #ccc;
  }
}

/* Feed Popup */
.feed-popup {
  padding: 40rpx;
  background-color: #fff;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40rpx;
  color: #333;
}

.feed-options {
  display: flex;
  justify-content: space-between;
  margin-bottom: 60rpx;
}

.option-item {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2rpx solid transparent;
  
  &.active {
    background-color: #fff8e1;
    border-color: #ffc107;
    color: #ff9c00;
  }
  
  .num {
    font-size: 40rpx;
    font-weight: bold;
  }
  
  .unit {
    font-size: 24rpx;
    color: #999;
  }
}

/* Feeding Animation */
.feeding-anim {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0);
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  
  &.active {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  
  .plus-one {
    color: #ff9c00;
    font-size: 48rpx;
    font-weight: bold;
    margin-top: 20rpx;
    text-shadow: 0 4rpx 8rpx rgba(0,0,0,0.1);
  }
}
</style>