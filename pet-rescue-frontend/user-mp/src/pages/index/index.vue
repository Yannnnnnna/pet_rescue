<template>
  <view class="container">
    <!-- 1. 顶部导航栏 -->
    <view class="header-bar">
      <view class="location">
        <u-icon name="map-fill" size="18" color="#333"></u-icon>
        <text class="city-name">{{ currentCity }}</text>
      </view>
      <view class="search-box" @click="handleSearch">
        <u-icon name="search" size="20" color="#999"></u-icon>
        <text class="placeholder">搜索品种、名字或救助站...</text>
      </view>
      <view class="msg-icon" @click="handleMessage">
        <u-icon name="bell" size="24" color="#333"></u-icon>
        <view class="badge" v-if="hasNewMsg"></view>
      </view>
    </view>

    <!-- 2. 轮播图 -->
    <view class="banner-box">
      <swiper class="swiper" circular indicator-dots autoplay interval="4000" indicator-active-color="#fff">
        <swiper-item v-for="(item, index) in bannerList" :key="index">
          <image :src="item.image" mode="aspectFill" class="banner-img" @click="handleBannerClick(item)"></image>
        </swiper-item>
      </swiper>
    </view>

    <!-- 3. 金刚区 (核心功能) -->
    <view class="king-kong-area">
      <view class="grid-item" @click="handleAISelect">
        <view class="icon-box ai-bg">
           <u-icon name="heart-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">AI选宠</text>
      </view>
      <view class="grid-item" @click="handleWiki">
        <view class="icon-box wiki-bg">
           <u-icon name="book-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">养宠百科</text>
      </view>
      <view class="grid-item" @click="handleCloudAdopt">
        <view class="icon-box cloud-bg">
           <u-icon name="star-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">云养计划</text>
      </view>
      <view class="grid-item" @click="handleProcess">
        <view class="icon-box process-bg">
           <u-icon name="list-dot" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">领养流程</text>
      </view>
    </view>

    <!-- 4. 宠物列表 -->
    <view class="pet-section">
      <!-- Tab 切换 -->
      <view class="sticky-tabs">
        <u-tabs 
          :list="tabList" 
          :current="currentTab" 
          @change="handleTabChange"
          active-color="#19be6b"
          line-color="#19be6b"
        ></u-tabs>
        
        <!-- 筛选栏 -->
        <view class="filter-bar">
          <view class="filter-item" :class="{ active: filterType === 'latest' }" @click="changeFilter('latest')">最新发布</view>
          <view class="filter-item" :class="{ active: filterType === 'nearest' }" @click="changeFilter('nearest')">离我最近</view>
          <view class="filter-item" :class="{ active: filterType === 'sterilized' }" @click="changeFilter('sterilized')">已绝育</view>
        </view>
      </view>

      <!-- 瀑布流列表 -->
      <view class="waterfall-list">
        <view class="left-col">
          <view class="pet-card" v-for="item in leftList" :key="item.id" @click="goDetail(item)">
            <image :src="item.coverImg" mode="widthFix" class="cover"></image>
            <view class="info">
              <view class="name-row">
                 <text class="name">{{ item.name }}</text>
                 <u-tag :text="item.sex === 1 ? '公' : '母'" :type="item.sex === 1 ? 'primary' : 'error'" size="mini" plain shape="circle"></u-tag>
              </view>
              <view class="tags">
                <text class="tag">{{ item.age }}</text>
                <text class="tag">{{ item.breed }}</text>
              </view>
              <view class="location">
                <u-icon name="map" size="12" color="#999"></u-icon>
                <text class="city">{{ item.city || '未知' }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="right-col">
          <view class="pet-card" v-for="item in rightList" :key="item.id" @click="goDetail(item)">
            <image :src="item.coverImg" mode="widthFix" class="cover"></image>
            <view class="info">
              <view class="name-row">
                 <text class="name">{{ item.name }}</text>
                 <u-tag :text="item.sex === 1 ? '公' : '母'" :type="item.sex === 1 ? 'primary' : 'error'" size="mini" plain shape="circle"></u-tag>
              </view>
              <view class="tags">
                <text class="tag">{{ item.age }}</text>
                <text class="tag">{{ item.breed }}</text>
              </view>
              <view class="location">
                <u-icon name="map" size="12" color="#999"></u-icon>
                <text class="city">{{ item.city || '未知' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
    </view>

    <!-- 5. AI 悬浮球 -->
    <view class="ai-float-btn" @click="handleAIAssistant">
      <image src="/static/ai-robot.png" class="ai-icon" mode="aspectFit"></image>
      <view class="pulse-ring"></view>
    </view>
    
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'

// 状态定义
const currentCity = ref('重庆')
const hasNewMsg = ref(true)
const statusBarHeight = ref(20) // 默认值，防止闪烁

// 计算吸顶高度
const headerStyleTop = computed(() => {
  return `calc(104rpx + ${statusBarHeight.value}px)`
})

const bannerList = ref([
  { image: 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?q=80&w=2069&auto=format&fit=crop', title: '领养日活动' },
  { image: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?q=80&w=2043&auto=format&fit=crop', title: '紧急救助' }
])

// Tab 配置
const tabList = ref([
  { name: '推荐' },
  { name: '猫猫' },
  { name: '狗狗' },
  { name: '异宠' }
])
const currentTab = ref(0)
const filterType = ref('latest')

// 列表数据
const petList = ref([])
const leftList = ref([])
const rightList = ref([])
const loadStatus = ref('loadmore')

onLoad(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  // 模拟加载初始数据
  mockLoadData()
})

onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  loadStatus.value = 'loading'
  setTimeout(() => {
    mockLoadData(true)
  }, 1000)
})

// 模拟数据加载
const mockLoadData = (append = false) => {
  const newItems = Array.from({ length: 6 }).map((_, i) => ({
    id: Date.now() + i,
    name: ['咪咪', '旺财', '小白', '奥利奥'][Math.floor(Math.random()*4)],
    coverImg: `https://images.unsplash.com/photo-${['1529778873929-fa8732783943', '1573865526739-10659fec78a5', '1495360019614-50fc7926073c'][Math.floor(Math.random()*3)]}?q=80&w=800&auto=format&fit=crop`,
    sex: Math.random() > 0.5 ? 1 : 0,
    age: '2岁',
    breed: '中华田园',
    city: '重庆'
  }))
  
  if (append) {
    petList.value = [...petList.value, ...newItems]
  } else {
    petList.value = newItems
  }
  
  // 简单的左右分栏逻辑
  leftList.value = petList.value.filter((_, i) => i % 2 === 0)
  rightList.value = petList.value.filter((_, i) => i % 2 !== 0)
  
  loadStatus.value = petList.value.length > 20 ? 'nomore' : 'loadmore'
}

// 事件处理
const handleSearch = () => {
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

const handleMessage = () => {
  uni.switchTab({ url: '/pages/message/message' })
}

const handleBannerClick = (item) => {
  console.log('Banner click:', item)
}

const handleAISelect = () => {
  uni.showToast({ title: 'AI选宠即将上线', icon: 'none' })
}

const handleWiki = () => {
  uni.showToast({ title: '养宠百科即将上线', icon: 'none' })
}

const handleCloudAdopt = () => {
  uni.showToast({ title: '云养计划即将上线', icon: 'none' })
}

const handleProcess = () => {
  uni.showToast({ title: '领养流程即将上线', icon: 'none' })
}

const handleTabChange = (item) => {
  currentTab.value = item.index
  // TODO: 重新加载对应分类数据
  mockLoadData(false)
}

const changeFilter = (type) => {
  filterType.value = type
  // TODO: 重新加载筛选数据
}

const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages/pet/detail?id=${item.id}`
  })
}

const handleAIAssistant = () => {
  uni.switchTab({ url: '/pages/ai/ai' })
}

</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f6f7f9;
  padding-bottom: 20rpx;
}

/* 顶部导航 */
.header-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .location {
    display: flex;
    align-items: center;
    margin-right: 20rpx;
    
    .city-name {
      font-size: 30rpx;
      font-weight: bold;
      margin-left: 6rpx;
      color: #333;
    }
  }
  
  .search-box {
    flex: 1;
    height: 64rpx;
    background: #f0f2f5;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    padding: 0 24rpx;
    margin-right: 20rpx;
    
    .placeholder {
      font-size: 26rpx;
      color: #999;
      margin-left: 10rpx;
    }
  }
  
  .msg-icon {
    position: relative;
    padding: 10rpx;
    
    .badge {
      position: absolute;
      top: 6rpx;
      right: 6rpx;
      width: 14rpx;
      height: 14rpx;
      background: #ff4d4f;
      border-radius: 50%;
    }
  }
}

/* 轮播图 */
.banner-box {
  padding: 20rpx 30rpx;
  background: #fff;
  
  .swiper {
    height: 300rpx;
    border-radius: 20rpx;
    overflow: hidden;
    transform: translateY(0); // 修复圆角在某些机型无效
    
    .banner-img {
      width: 100%;
      height: 100%;
    }
  }
}

/* 金刚区 */
.king-kong-area {
  display: flex;
  justify-content: space-between;
  padding: 30rpx;
  background: #fff;
  margin-bottom: 20rpx;
  
  .grid-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .icon-box {
      width: 90rpx;
      height: 90rpx;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;
      
      &.ai-bg { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.wiki-bg { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%); }
      &.cloud-bg { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }
      &.process-bg { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
    }
    
    .label {
      font-size: 26rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

/* 宠物列表 */
.pet-section {
  .sticky-tabs {
    position: sticky;
    top: 100rpx; // 假设header高度
    z-index: 99;
    background: #f6f7f9;
  }
  
  .filter-bar {
    display: flex;
    padding: 20rpx 30rpx;
    gap: 30rpx;
    
    .filter-item {
      font-size: 26rpx;
      color: #666;
      padding: 8rpx 20rpx;
      border-radius: 24rpx;
      background: #fff;
      
      &.active {
        color: #19be6b;
        background: #e1f3d8;
        font-weight: bold;
      }
    }
  }
  
  .waterfall-list {
    display: flex;
    padding: 0 20rpx;
    justify-content: space-between;
    align-items: flex-start;
    
    .left-col, .right-col {
      width: 48%;
      display: flex;
      flex-direction: column;
    }
    
    .pet-card {
      background: #fff;
      border-radius: 16rpx;
      overflow: hidden;
      margin-bottom: 20rpx;
      box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
      
      .cover {
        width: 100%;
        display: block; // 消除图片底部空隙
      }
      
      .info {
        padding: 16rpx;
        
        .name-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12rpx;
          
          .name {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
          }
        }
        
        .tags {
          margin-bottom: 16rpx;
          display: flex;
          gap: 10rpx;
          flex-wrap: wrap;
          
          .tag {
            font-size: 22rpx;
            color: #666;
            background: #f5f5f5;
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
          }
        }
        
        .location {
          display: flex;
          align-items: center;
          
          .city {
            font-size: 22rpx;
            color: #999;
            margin-left: 6rpx;
          }
        }
      }
    }
  }
}

/* AI 悬浮球 */
.ai-float-btn {
  position: fixed;
  right: 30rpx;
  bottom: 120rpx; // 高于 tabbar
  width: 100rpx;
  height: 100rpx;
  z-index: 999;
  
  .ai-icon {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    box-shadow: 0 8rpx 32rpx rgba(25, 190, 107, 0.4);
    background: #fff;
  }
  
  .pulse-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 4rpx solid #19be6b;
    opacity: 0;
    animation: pulse 2s infinite;
  }
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}
</style>
