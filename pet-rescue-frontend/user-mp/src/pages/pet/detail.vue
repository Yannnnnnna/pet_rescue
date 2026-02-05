<template>
  <view class="container" v-if="pet">
    <!-- 顶部轮播图 -->
    <swiper class="banner" circular indicator-dots autoplay>
      <swiper-item v-if="pet.detailImgList && pet.detailImgList.length > 0" v-for="(img, index) in parseImages(pet.detailImgList)" :key="index">
        <image :src="img" mode="aspectFill" class="banner-img" @click="previewImage(index)"></image>
      </swiper-item>
      <swiper-item v-else>
         <image :src="pet.coverImg" mode="aspectFill" class="banner-img"></image>
      </swiper-item>
    </swiper>

    <!-- 基本信息 -->
    <view class="info-card">
      <view class="header-row">
        <text class="name">{{ pet.name }}</text>
        <view class="right-actions">
           <u-icon 
             :name="isFavorited ? 'star-fill' : 'star'" 
             :color="isFavorited ? '#ff9c00' : '#999'" 
             size="24" 
             @click="toggleFav"
             style="margin-right: 20rpx;"
           ></u-icon>
           <u-tag :text="getStatusText(pet.status)" :type="getStatusType(pet.status)" size="mini"></u-tag>
        </view>
      </view>
      
      <view class="tags-row">
        <view class="tag-item">{{ pet.age }}</view>
        <view class="divider"></view>
        <view class="tag-item">{{ pet.breed }}</view>
        <view class="divider"></view>
        <view class="tag-item">{{ pet.sex === 1 ? '公' : (pet.sex === 0 ? '母' : '未知') }}</view>
      </view>

      <!-- 宠物个性标签 -->
      <view class="pet-tags-container" v-if="tagList.length > 0">
        <u-tag 
          v-for="(tag, index) in tagList" 
          :key="index" 
          :text="tag" 
          plain 
          shape="circle" 
          size="mini" 
          type="warning"
          class="pet-feature-tag"
        ></u-tag>
      </view>
      
      <view class="location-row">
        <u-icon name="map" color="#999" size="16"></u-icon>
        <text class="address">{{ displayAddress }}</text>
      </view>
      
      <view class="health-row">
         <u-tag text="已绝育" type="success" plain size="mini" v-if="pet.isSterilized === 1" class="health-tag"></u-tag>
         <u-tag text="未绝育" type="info" plain size="mini" v-else class="health-tag"></u-tag>
         
         <u-tag text="已疫苗" type="success" plain size="mini" v-if="pet.isVaccinated === 1" class="health-tag"></u-tag>
         <u-tag text="未疫苗" type="info" plain size="mini" v-else class="health-tag"></u-tag>
      </view>

      <view class="publish-time">
        <text>发布于 {{ pet.createTime || '未知时间' }}</text>
      </view>
    </view>

    <!-- 详细描述 -->
    <view class="desc-card">
      <view class="section-title">送养人说</view>
      <text class="desc-text">{{ pet.description || '暂无详细介绍' }}</text>
    </view>
    
    <!-- 底部栏 -->
    <view class="bottom-bar">
      <view class="btn-group" v-if="isOwner">
        <!-- 如果是宠物主人，且未被领养，显示修改和删除 -->
        <u-button v-if="pet.status !== 2" shape="circle" icon="edit-pen" text="修改信息" @click="handleEdit"></u-button>
        <u-button v-if="pet.status !== 2" shape="circle" type="error" icon="trash" text="删除" @click="handleDelete"></u-button>
        <!-- 已领养状态下，只显示回访按钮 -->
        <u-button v-if="pet.status === 2" shape="circle" type="success" icon="chat" text="回访" @click="handleReturnVisit"></u-button>
      </view>
      <view class="btn-group" v-else>
        <!-- 如果是普通用户，显示咨询和申请领养 -->
        <u-button shape="circle" icon="chat" text="咨询记录" @click="handleConsult"></u-button>
        <u-button shape="circle" type="primary" text="申请领养" @click="handleApply" :disabled="pet.status !== 0"></u-button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetDetail, deletePet, toggleFavorite, checkFavorite } from '@/api/pet'
import { getAdopterInfo } from '@/api/adoption'
import { getMyInfo } from '@/api/user'

const petId = ref(null)
const pet = ref(null)
const currentUserId = ref(null)
const urlIsOwner = ref(false)
const isFavorited = ref(false)

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
   // 获取当前用户信息以便判断是否是送养人
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
    // 假设后端返回的是JSON字符串数组，如 "[\"url1\", \"url2\"]"
    // 或者直接是数组
    if (Array.isArray(jsonStr)) return jsonStr
    return JSON.parse(jsonStr)
  } catch (e) {
    return []
  }
}

const previewImage = (current) => {
  const urls = parseImages(pet.value.detailImgList) || [pet.value.coverImg]
  uni.previewImage({
    current,
    urls
  })
}

const getStatusText = (status) => {
  const map = { 0: '待领养', 1: '申请中', 2: '已领养' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'primary', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

// 判断是否是宠物主人
const isOwner = computed(() => {
  if (urlIsOwner.value) return true
  if (!pet.value || !currentUserId.value) return false
  
  const uid = String(currentUserId.value)
  // 检查 userId 和 publisherId 两个可能的字段
  return (pet.value.userId && String(pet.value.userId) === uid) ||
         (pet.value.publisherId && String(pet.value.publisherId) === uid)
})

// 处理标签列表
const tagList = computed(() => {
  if (!pet.value || !pet.value.tags) return []
  return pet.value.tags.split(/[,，]/).filter(t => t && t.trim())
})

// 处理地址显示（去重）
const displayAddress = computed(() => {
  if (!pet.value || !pet.value.address) return '未知地点'
  const parts = pet.value.address.split('/')
  // 使用 Set 去重
  return [...new Set(parts)].join('')
})

const handleConsult = () => {
    // 判断是否是送养人(自己)
    if (isOwner.value) {
       // 我是送养人，查看咨询列表
       uni.navigateTo({
         url: `/pages/message/consultation-list?petId=${petId.value}`
       })
    } else {
       // 我是申请人，跳转到聊天记录
       uni.navigateTo({
         url: `/pages/message/chat-history?petId=${petId.value}`
       })
    }
}

const handleApply = () => {
  uni.navigateTo({
    url: `/pages/adoption/apply?petId=${petId.value}`
  })
}

const handleEdit = () => {
  // 跳转到编辑页面（假设有一个编辑页面）
  uni.navigateTo({
    url: `/pages/pet/edit?id=${petId.value}`
  })
}

const handleReturnVisit = async () => {
    // 回访：送养人需要获取领养人信息，然后进入聊天页面
    // 此时 applicantId 应为领养人的ID
    uni.showLoading({ title: '获取领养人信息' })
    try {
        const res = await getAdopterInfo(petId.value)
        if (res.data) {
            const adopter = res.data
            uni.navigateTo({
                url: `/pages/message/chat-history?petId=${petId.value}&applicantId=${adopter.id}&chatType=return_visit`
            })
        } else {
             uni.showToast({ title: '未找到领养人信息', icon: 'none' })
        }
    } catch (error) {
        console.error(error)
        // 错误提示已在 request.js 中统一处理，或者根据需要额外提示
    } finally {
        uni.hideLoading()
    }
}

const handleDelete = () => {
  uni.showModal({
    title: '确认删除',
    content: '删除后将无法恢复，确定要删除这只宠物吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '删除中...' })
        try {
          await deletePet(petId.value)
          uni.hideLoading()
          uni.showToast({ title: '删除成功', icon: 'success' })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.hideLoading()
          console.error('删除失败', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.banner {
  width: 100%;
  height: 500rpx;
  background-color: #eee;
  
  .banner-img {
    width: 100%;
    height: 100%;
  }
}

.info-card, .desc-card {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .name {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
  }
}

.tags-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  .tag-item {
    font-size: 28rpx;
    color: #666;
  }
  
  .divider {
    width: 2rpx;
    height: 24rpx;
    background: #ccc;
    margin: 0 16rpx;
  }
}

.location-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  .address {
    font-size: 26rpx;
    color: #999;
    margin-left: 8rpx;
  }
}

.health-row {
  display: flex;
  gap: 16rpx;
  
  .health-tag {
    margin-right: 10rpx;
  }
}

.pet-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 20rpx;
  
  .pet-feature-tag {
    margin-right: 0;
  }
}

.publish-time {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  font-size: 24rpx;
  color: #999;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
  border-left: 8rpx solid #19be6b;
  padding-left: 16rpx;
}

.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
  box-sizing: border-box;
  
  .btn-group {
    display: flex;
    gap: 20rpx;
    
    u-button {
      flex: 1;
    }
  }
}
</style>
