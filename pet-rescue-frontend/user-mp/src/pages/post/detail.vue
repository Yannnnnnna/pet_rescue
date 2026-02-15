<template>
  <view class="post-detail-page">
    <!-- 轮播图 -->
    <view class="swiper-box">
      <u-swiper
        :list="swiperList"
        height="500"
        indicator
        indicatorMode="dot"
        circular
      ></u-swiper>
    </view>

    <!-- 内容区域 -->
    <view class="content-box">
      <!-- 用户信息 -->
      <view class="user-info">
        <u-avatar :src="postDetail.userAvatar" size="40"></u-avatar>
        <view class="info-content">
          <text class="nickname">{{ postDetail.userName || '未知用户' }}</text>
          <u-tag v-if="postDetail.auditStatus !== undefined" :text="currentStatus.text" :type="currentStatus.type" size="mini" plain customStyle="margin-left: 10rpx"></u-tag>
        </view>
      </view>

      <!-- 关联宠物 -->
      <view class="pet-tag" v-if="postDetail.petName">
        <u-tag :text="'我是 ' + postDetail.petName" type="success" plain size="mini" shape="circle"></u-tag>
      </view>

      <!-- 文字内容 -->
      <view class="text-content">
        <text>{{ postDetail.content }}</text>
      </view>

      <!-- 底部互动 -->
      <view class="interaction-bar">
        <view class="left">
          <text class="time">{{ formatTime(postDetail.createTime) }}</text>
        </view>
        <view class="right">
          <view class="action-item">
            <u-icon name="dianzan" custom-prefix="custom-icon" size="24" :color="postDetail.isLiked ? '#2979ff' : '#666'"></u-icon>
            <text class="count">{{ postDetail.likeCount || 0 }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons" v-if="isOwner">
        <u-button type="primary" plain size="small" text="修改" customStyle="margin-right: 20rpx; width: 160rpx" @click="handleEdit"></u-button>
        <u-button type="error" plain size="small" text="删除" customStyle="width: 160rpx" @click="handleDelete"></u-button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionPostDetail, deleteAdoptionPost } from '@/api/adoption-post'
import { getUserDetail, getMyInfo } from '@/api/user'
import { getPetDetail } from '@/api/pet'
import dayjs from 'dayjs'

const postDetail = ref({})
const swiperList = ref([])
const currentUserId = ref(null)
const petInfo = ref(null)

// 状态字典
const statusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '正常', type: 'success' },
  2: { text: '违规', type: 'error' }
}

const currentStatus = computed(() => {
  return statusMap[postDetail.value.auditStatus] || { text: '未知', type: 'info' }
})

const isOwner = computed(() => {
  if (!currentUserId.value || !postDetail.value.userId) return false
  return String(currentUserId.value) === String(postDetail.value.userId)
})

onLoad((options) => {
  if (options.id) {
    fetchCurrentUserInfo()
    fetchDetail(options.id)
  }
})

const fetchCurrentUserInfo = async () => {
  try {
    const res = await getMyInfo()
    if (res.data) {
      currentUserId.value = res.data.id
    }
  } catch (e) {
    console.error('获取当前用户信息失败', e)
  }
}

const fetchDetail = async (id) => {
  try {
    const res = await getAdoptionPostDetail(id)
    if (res.data) {
      postDetail.value = res.data
      if (postDetail.value.images) {
        if (typeof postDetail.value.images === 'string') {
          try {
            swiperList.value = JSON.parse(postDetail.value.images)
          } catch (e) {
            swiperList.value = postDetail.value.images.split(',')
          }
        } else if (Array.isArray(postDetail.value.images)) {
          swiperList.value = postDetail.value.images
        }
      }
      
      if (postDetail.value.userId) {
        try {
          const userRes = await getUserDetail(postDetail.value.userId)
          if (userRes.data) {
            postDetail.value.userAvatar = userRes.data.avatar
            postDetail.value.userName = userRes.data.nickname
          }
        } catch (e) {
          console.error('获取用户信息失败', e)
        }
      }

      if (postDetail.value.petId) {
        try {
          const petRes = await getPetDetail(postDetail.value.petId)
          if (petRes.data) {
            petInfo.value = petRes.data
            postDetail.value.petName = petRes.data.name
          }
        } catch (e) {
          console.error('获取宠物信息失败', e)
        }
      }

      if (!postDetail.value.userAvatar) {
        postDetail.value.userAvatar = 'https://uview-plus.jiangruyi.com/common/logo.png'
      }
      if (!postDetail.value.userName) {
        postDetail.value.userName = '爱心领养人'
      }
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const handleEdit = () => {
  uni.navigateTo({
    url: `/pages/post/publish?id=${postDetail.value.id}`
  })
}

const handleDelete = () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条日记吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await deleteAdoptionPost(postDetail.value.id)
          if (result.code === 200 || result.code === 0) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            uni.showToast({ title: result.msg || '删除失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '系统错误', icon: 'none' })
        }
      }
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.post-detail-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #e6f2e6, #f5f5f5);
}

.swiper-box {
  width: 100%;
  border-bottom-left-radius: 60rpx;
  border-bottom-right-radius: 60rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 16rpx rgba(0,0,0,0.1);
}

.content-box {
  background: rgba(255, 255, 255, 0.8);
  margin: -50rpx 24rpx 24rpx;
  padding: 30rpx;
  border-radius: 32rpx;
  position: relative;
  backdrop-filter: blur(10px);
  
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;
    
    .info-content {
      display: flex;
      align-items: center;
      margin-left: 20rpx;
      
      .nickname {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        margin-right: 10rpx;
      }
    }
  }
  
  .pet-tag {
    margin-bottom: 20rpx;
  }
  
  .text-content {
    font-size: 32rpx;
    color: #333;
    line-height: 1.6;
    margin-bottom: 40rpx;
    min-height: 200rpx;
  }
  
  .interaction-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid #eee;
    
    .time {
      font-size: 24rpx;
      color: #999;
    }
    
    .right {
      display: flex;
      
      .action-item {
        display: flex;
        align-items: center;
        
        .count {
          margin-left: 10rpx;
          font-size: 28rpx;
          color: #666;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    justify-content: flex-end;
    margin-top: 30rpx;
    padding-top: 20rpx;
    border-top: 1rpx dashed #eee;
  }
}
</style>
