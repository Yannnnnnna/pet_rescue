<template>
  <view class="diary-list-page">
    <u-navbar title="成长日记" :autoBack="true" bgColor="#fff" leftIconColor="#333" titleStyle="color: #333; font-weight: 600;"></u-navbar>
    
    <view class="content-area" :style="{ paddingTop: navHeight + 'px' }">
      <view v-if="postList.length > 0" class="list-container">
        <view 
          class="diary-card" 
          v-for="post in postList" 
          :key="post.id"
          @click="goPostDetail(post)"
        >
          <!-- 头部状态 -->
          <view class="card-header">
            <text class="time">{{ formatTime(post.createTime) }}</text>
            <u-tag :text="getStatusText(post.auditStatus)" :type="getStatusColor(post.auditStatus)" size="mini" plain shape="circle"></u-tag>
          </view>
          
          <!-- 内容摘要 -->
          <view class="card-content">
            <text class="text u-line-2">{{ post.content || '暂无文字内容' }}</text>
          </view>
          
          <!-- 图片预览 (只显示前3张) -->
          <view class="image-grid" v-if="getImages(post).length > 0">
            <view 
              class="img-item" 
              v-for="(img, idx) in getImages(post).slice(0, 3)" 
              :key="idx"
            >
              <image :src="img" mode="aspectFill"></image>
              <view class="more-mask" v-if="idx === 2 && getImages(post).length > 3">
                <text>+{{ getImages(post).length - 3 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <u-empty v-else mode="history" icon="http://cdn.uviewui.com/uview/empty/history.png" text="暂无成长日记"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionPostList } from '@/api/adoption-post'
import dayjs from 'dayjs'

const navHeight = 44 + (uni.getSystemInfoSync().statusBarHeight || 0)
const postList = ref([])
const petId = ref('')

onLoad((options) => {
  if (options.petId) {
    petId.value = options.petId
    fetchData()
  }
})

const fetchData = async () => {
  try {
    // 使用新的查询接口，petId即为查询参数
    const res = await getAdoptionPostList({ petId: petId.value })
    if (res.data) {
      postList.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
  
}



const getImages = (post) => {
  if (!post.images) return []
  if (typeof post.images === 'string') {
     try {
       return JSON.parse(post.images)
     } catch (e) {
       return post.images.split(',')
     }
  }
  return post.images
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY年MM月DD日 HH:mm')
}

const getStatusText = (auditStatus) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已驳回' }
  return map[auditStatus] || '未知'
}

const getStatusColor = (auditStatus) => {
  const map = { 0: 'warning', 1: 'success', 2: 'error' }
  return map[auditStatus] || 'info'
}

const goPostDetail = (post) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${post.id}` })
}
</script>

<style lang="scss" scoped>
.diary-list-page {
  min-height: 100vh;
  background-color: #f5f6fa;
}

.content-area {
  padding: 30rpx;
}

.diary-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  
  .time {
    font-size: 26rpx;
    color: #999;
  }
}

.card-content {
  margin-bottom: 20rpx;
  
  .text {
    font-size: 30rpx;
    color: #333;
    line-height: 1.6;
  }
}

.image-grid {
  display: flex;
  gap: 16rpx;
  
  .img-item {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
    overflow: hidden;
    position: relative;
    
    image {
      width: 100%;
      height: 100%;
    }
    
    .more-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        color: #fff;
        font-size: 36rpx;
        font-weight: 600;
      }
    }
  }
}
</style>