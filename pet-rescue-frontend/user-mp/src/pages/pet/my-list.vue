<template>
  <view class="container">
    <u-list @scrolltolower="loadMore" v-if="dataList.length > 0">
      <u-list-item v-for="(item, index) in dataList" :key="index">
        <view class="pet-card">
          <image :src="item.coverImg" mode="aspectFill" class="pet-img" @click="handlePetClick(item)"></image>
          <view class="pet-info" @click="handlePetClick(item)">
            <view class="header-row">
              <text class="pet-name">{{ item.name }}</text>
              <u-tag :text="getStatusText(item.status)" :type="getStatusType(item.status)" size="mini" plain></u-tag>
            </view>
            <view class="tags-row">
              <text class="breed">{{ item.breed }}</text>
              <text class="age">{{ item.age }}</text>
              <text class="address">{{ item.city }} {{ item.address }}</text>
            </view>
            <view class="desc">{{ item.description }}</view>
          </view>
          
          <!-- 如果是"我发布的"，显示咨询记录按钮 -->
          <view class="action-btn" v-if="type === 'published'" @click.stop="handleConsultClick(item)">
            <u-icon name="chat" size="20" color="#19be6b"></u-icon>
            <text class="btn-text">咨询记录</text>
          </view>
        </view>
      </u-list-item>
    </u-list>
    <u-empty v-else mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png" text="暂无数据"></u-empty>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyFavorites, getMyPublishedPets, getMyChattedPets } from '@/api/pet.js'

const type = ref('')
const dataList = ref([])

onLoad((options) => {
  type.value = options.type
  let title = '宠物列表'
  switch (options.type) {
    case 'favorites':
      title = '我的收藏'
      break
    case 'published':
      title = '我发布的'
      break
    case 'chatted': // Corrected key from 'chats' to 'chatted' as per plan logic
      title = '咨询足迹'
      break
  }
  uni.setNavigationBarTitle({ title })
  fetchData()
})

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

const getStatusType = (status) => {
  const map = { 0: 'primary', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

const handlePetClick = (item) => {
  // 跳转到详情页，如果是"我发布的"，带上 isOwner 参数
  let url = `/pages/pet/detail?id=${item.id}`
  if (type.value === 'published') {
    url += '&isOwner=true'
  }
  uni.navigateTo({ url })
}

const handleConsultClick = (item) => {
  // 跳转到咨询列表页
  uni.navigateTo({
    url: `/pages/message/consultation-list?petId=${item.id}`
  })
}

const loadMore = () => {
  // 目前接口似乎是一次性返回，暂不处理分页
}
</script>

<style lang="scss" scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.pet-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 20rpx;
  position: relative;
}

.pet-img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.action-btn {
  position: absolute;
  bottom: 20rpx;
  right: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12rpx 16rpx;
  background: #f0f9ff;
  border-radius: 12rpx;
  border: 1rpx solid #19be6b;
  
  .btn-text {
    font-size: 22rpx;
    color: #19be6b;
    margin-top: 4rpx;
  }
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  font-size: 24rpx;
  color: #666;
}

.desc {
  font-size: 26rpx;
  color: #999;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
