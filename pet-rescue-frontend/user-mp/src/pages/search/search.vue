<template>
  <view class="container">
    <view class="header-section">
      <view class="header-pattern"></view>
      <view class="header-decor decor-1"></view>
      <view class="header-decor decor-2"></view>
      
      <view :style="{ height: statusBarHeight + 'px' }"></view>
      
      <view class="nav-bar">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">搜索</text>
        <view class="placeholder"></view>
      </view>
      
      <view class="search-wrapper">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#2E7D32"></uni-icons>
          <input 
            class="search-input" 
            v-model="keyword" 
            :placeholder="searchFrom === 'pet' ? '搜索宠物' : '搜索资讯'" 
            confirm-type="search"
            @confirm="onSearch"
            focus
          />
          <view class="clear-btn" v-if="keyword" @click="onClear">
            <uni-icons type="clear" size="18" color="#999"></uni-icons>
          </view>
        </view>
        <view class="search-btn" @click="onSearch">
          <text>搜索</text>
        </view>
      </view>
      
      <view class="type-tabs">
        <view 
          class="type-tab" 
          :class="{ active: searchFrom === 'pet' }"
          @click="switchType('pet')"
        >
          <text>宠物</text>
        </view>
        <view 
          class="type-tab" 
          :class="{ active: searchFrom === 'wiki' }"
          @click="switchType('wiki')"
        >
          <text>资讯</text>
        </view>
      </view>
    </view>

    <view class="result-section" :style="{ marginTop: resultMarginTop + 'px' }">
      <view class="pet-list" v-if="searchFrom === 'pet' && petList.length > 0">
        <view 
          class="pet-card" 
          v-for="item in petList" 
          :key="item.id"
          @click="goDetail(item)"
        >
          <view class="card-image-box">
            <image :src="item.coverImg" mode="aspectFill" class="pet-img"></image>
            <view class="love-badge">
              <uni-icons type="heart-filled" size="12" color="#FFC107"></uni-icons>
              <text>{{ item.lovePoint || 0 }}</text>
            </view>
          </view>
          <view class="pet-info">
            <view class="pet-header">
              <text class="pet-name">{{ item.name }}</text>
              <view class="sex-tag" :class="item.sex === 1 ? 'male' : 'female'">
                <text>{{ item.sex === 1 ? '公' : '母' }}</text>
              </view>
            </view>
            <view class="pet-tags">
              <text class="tag">{{ item.breed }}</text>
              <text class="tag">{{ item.age }}</text>
            </view>
            <view class="pet-location">
              <uni-icons type="location" size="12" color="#999"></uni-icons>
              <text>{{ item.city }}</text>
            </view>
            <view class="pet-desc">{{ item.description || '暂无描述' }}</view>
          </view>
        </view>
        
        <u-loadmore 
          :status="loadStatus" 
          marginTop="20"
        ></u-loadmore>
      </view>
      
      <view class="article-list" v-else-if="searchFrom === 'wiki' && articleList.length > 0">
        <view 
          class="article-card" 
          v-for="item in articleList" 
          :key="item.id"
          @click="goDetail(item)"
        >
          <image :src="item.coverImg" mode="aspectFill" class="article-cover"></image>
          <view class="article-content">
            <view class="article-title">{{ item.title }}</view>
            <view class="article-summary">{{ item.summary }}</view>
            <view class="article-meta">
              <view class="meta-left">
                <view class="tag-badge">{{ getFirstTag(item.tags) }}</view>
                <view class="view-count">
                  <uni-icons type="eye" size="12" color="#999"></uni-icons>
                  <text>{{ item.viewCount }}</text>
                </view>
              </view>
              <text class="meta-time">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>
        </view>
        
        <u-loadmore 
          :status="loadStatus" 
          marginTop="20"
        ></u-loadmore>
      </view>
      
      <view class="empty-state" v-else-if="searched">
        <view class="empty-icon">
          <uni-icons type="search" size="60" color="#ccc"></uni-icons>
        </view>
        <text class="empty-text">{{ searchFrom === 'wiki' ? '未找到相关文章' : '未找到相关宠物' }}</text>
        <text class="empty-hint">换个关键词试试吧</text>
      </view>
      
      <view class="empty-placeholder" v-else>
        <view class="empty-icon">
          <uni-icons type="search" size="60" color="#ccc"></uni-icons>
        </view>
        <text class="empty-text">请输入关键词开始搜索</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetList } from '@/api/pet'
import { getArticleList } from '@/api/article'

const statusBarHeight = ref(44)
const keyword = ref('')
const petList = ref([])
const articleList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const loadStatus = ref('loadmore')
const searched = ref(false)
const searchFrom = ref('pet')

const resultMarginTop = computed(() => {
  return statusBarHeight.value + 44 + 160
})

onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  
  if (options.from) {
    searchFrom.value = options.from
  }
})

const goBack = () => {
  uni.navigateBack()
}

const switchType = (type) => {
  if (searchFrom.value === type) return
  searchFrom.value = type
  keyword.value = ''
  petList.value = []
  articleList.value = []
  searched.value = false
  loadStatus.value = 'loadmore'
}

const onSearch = () => {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入关键词', icon: 'none' })
    return
  }
  searched.value = true
  loadData(true)
}

const onClear = () => {
  keyword.value = ''
  petList.value = []
  articleList.value = []
  searched.value = false
  loadStatus.value = 'loadmore'
}

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    petList.value = []
    articleList.value = []
    loadStatus.value = 'loading'
  }
  
  try {
    if (searchFrom.value === 'wiki') {
      const res = await getArticleList({
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        type: 0,
        keyword: keyword.value
      })
      const newItems = res.data.records || []
      
      if (reset) {
        articleList.value = newItems
      } else {
        articleList.value = [...articleList.value, ...newItems]
      }
      
      if (articleList.value.length < pageSize.value) {
        loadStatus.value = 'nomore'
      } else {
        loadStatus.value = 'loadmore'
      }
    } else {
      const params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        status: 0,
        keyword: keyword.value
      }
      
      const res = await getPetList(params)
      const newItems = res.data.records || []
      
      if (reset) {
        petList.value = newItems
      } else {
        petList.value = [...petList.value, ...newItems]
      }
      
      if (petList.value.length < pageSize.value) {
        loadStatus.value = 'nomore'
      } else {
        loadStatus.value = 'loadmore'
      }
    }
  } catch (error) {
    console.error('搜索失败', error)
    loadStatus.value = 'loadmore'
  }
}

const loadMore = () => {
  if (loadStatus.value === 'nomore') return
  pageNum.value++
  loadData()
}

const goDetail = (item) => {
  if (searchFrom.value === 'wiki') {
    uni.navigateTo({
      url: `/pages/wiki/detail?id=${item.id}`
    })
  } else {
    uni.navigateTo({
      url: `/pages/pet/detail?id=${item.id}`
    })
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const publishTime = new Date(time)
  const diff = Math.floor((now - publishTime) / 1000 / 60 / 60 / 24)
  
  if (diff === 0) {
    return '今天'
  } else if (diff === 1) {
    return '1天前'
  } else if (diff < 7) {
    return diff + '天前'
  } else {
    return publishTime.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

const getFirstTag = (tags) => {
  if (!tags) return ''
  const tagArray = tags.split(',').map(t => t.trim()).filter(t => t)
  return tagArray[0] || ''
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f6f7f9;
}

.header-section {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #2E7D32 0%, #1B5E20 100%);
  border-radius: 0 0 50rpx 50rpx;
  overflow: hidden;
  padding-bottom: 30rpx;
}

.header-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.08) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.5;
}

.header-decor {
  position: absolute;
  border-radius: 50%;
  
  &.decor-1 {
    width: 200rpx;
    height: 200rpx;
    background: rgba(255,255,255,0.1);
    top: 40rpx;
    right: -40rpx;
    filter: blur(40rpx);
  }
  
  &.decor-2 {
    width: 160rpx;
    height: 160rpx;
    background: rgba(255, 214, 0, 0.15);
    bottom: 40rpx;
    left: -40rpx;
    filter: blur(30rpx);
  }
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  position: relative;
  z-index: 2;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:active {
    background: rgba(255,255,255,0.3);
  }
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.placeholder {
  width: 64rpx;
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;
  position: relative;
  z-index: 2;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 0 24rpx;
  height: 80rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  margin: 0 16rpx;
}

.clear-btn {
  padding: 10rpx;
}

.search-btn {
  background: #FFC107;
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  
  text {
    font-size: 28rpx;
    font-weight: 500;
    color: #1B5E20;
  }
  
  &:active {
    opacity: 0.8;
  }
}

.type-tabs {
  display: flex;
  padding: 0 30rpx 30rpx;
  gap: 20rpx;
  position: relative;
  z-index: 2;
}

.type-tab {
  flex: 1;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 36rpx;
  background: rgba(255,255,255,0.2);
  
  text {
    font-size: 28rpx;
    color: #fff;
  }
  
  &.active {
    background: #FFC107;
    
    text {
      color: #1B5E20;
      font-weight: 600;
    }
  }
}

.result-section {
  padding: 30rpx 20rpx 20rpx;
}

.pet-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.pet-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.card-image-box {
  position: relative;
  height: 320rpx;
}

.pet-img {
  width: 100%;
  height: 100%;
}

.love-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(0,0,0,0.5);
  padding: 8rpx 16rpx;
  border-radius: 30rpx;
  
  text {
    font-size: 22rpx;
    color: #FFC107;
  }
}

.pet-info {
  padding: 24rpx;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.pet-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.sex-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  
  &.male {
    background: rgba(30, 136, 229, 0.1);
    
    text {
      color: #1E88E5;
    }
  }
  
  &.female {
    background: rgba(233, 30, 99, 0.1);
    
    text {
      color: #E91E63;
    }
  }
}

.pet-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
  
  .tag {
    font-size: 24rpx;
    color: #666;
    background: #f5f5f5;
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
  }
}

.pet-location {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 12rpx;
  
  text {
    font-size: 24rpx;
    color: #999;
  }
}

.pet-desc {
  font-size: 24rpx;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.article-card {
  display: flex;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
}

.article-cover {
  width: 200rpx;
  height: 180rpx;
  flex-shrink: 0;
}

.article-content {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.article-summary {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  margin: 12rpx 0;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.tag-badge {
  background: rgba(46, 125, 50, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #2E7D32;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 6rpx;
  
  text {
    font-size: 22rpx;
    color: #999;
  }
}

.meta-time {
  font-size: 22rpx;
  color: #999;
}

.empty-state, .empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
}

.empty-icon {
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
}
</style>
