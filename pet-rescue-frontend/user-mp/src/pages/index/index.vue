<template>
  <view class="container">
    <!-- 1. 顶部导航栏 -->
    <view class="header-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <picker mode="multiSelector" :range="range" range-key="name" :value="multiIndex" @change="handlePickerChange" @columnchange="handleColumnChange" class="location-picker">
        <view class="location">
          <u-icon name="map-fill" size="18" color="#333"></u-icon>
          <text class="city-name">{{ currentCity }}</text>
        </view>
      </picker>
      <view class="search-box" @click="handleSearch">
        <u-icon name="search" size="20" color="#999"></u-icon>
        <text class="placeholder">搜索品种或名字</text>
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
      <view class="grid-item" @click="handlePublish">
        <view class="icon-box cloud-bg">
           <u-icon name="plus" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">发布</text>
      </view>
      <view class="grid-item" @click="handleProcess">
        <view class="icon-box process-bg">
           <u-icon name="list-dot" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">领养流程</text>
      </view>
      <view class="grid-item" @click="handleWallpaper">
        <view class="icon-box wallpaper-bg">
           <u-icon name="image-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">壁纸</text>
      </view>
      <view class="grid-item" @click="handleNews">
        <view class="icon-box news-bg">
           <u-icon name="chat-fill" color="#fff" size="28"></u-icon>
        </view>
        <text class="label">资讯中心</text>
      </view>
    </view>

    <!-- 4. 宠物列表 -->
    <view class="pet-section">
      <!-- Tab 切换 -->
      <view class="sticky-tabs" :style="{ top: headerStyleTop }">
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
      
      <!-- 空状态 -->
      <u-empty v-if="petList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png">
      </u-empty>

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
import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app'
import { getPetList } from '@/api/pet'
import { getMyInfo } from '@/api/user'
import { cityData } from '@/utils/cityData'

// 状态定义
const currentCity = ref('重庆市') // 默认为重庆市
const filterCity = ref('重庆市') // 用于API筛选的城市名
const hasNewMsg = ref(true)
const statusBarHeight = ref(20) // 默认值，防止闪烁

// 城市选择器相关
const multiIndex = ref([21, 0]) // 默认选中重庆 (index 21 in cityData roughly, but let's init properly)
const range = ref([cityData, cityData[21].cities]) // Init with Chongqing's cities

// 初始化城市选择器位置
const initCityPicker = () => {
  // 查找当前城市的索引
  const pIndex = cityData.findIndex(p => p.name === '重庆市')
  if (pIndex > -1) {
    multiIndex.value[0] = pIndex
    range.value[1] = cityData[pIndex].cities
    // 重庆市的 cities 只有一项
    multiIndex.value[1] = 0
  } else {
    // Fallback to Beijing
    multiIndex.value = [0, 0]
    range.value[1] = cityData[0].cities
  }
}


// 计算吸顶高度 (header高度约 104rpx + statusBarHeight)
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
const pageNum = ref(1)
const pageSize = ref(10)

onLoad(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  initCityPicker()

  // 加载初始数据
  loadData(true)
})

onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  loadStatus.value = 'loading'
  pageNum.value++
  loadData()
})

// 数据加载
const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    petList.value = []
    leftList.value = []
    rightList.value = []
    loadStatus.value = 'loading'
  }

  try {
    // 映射 Tab 到 API type
    // Tab: 0-推荐, 1-猫, 2-狗, 3-异宠
    // API: 0-猫, 1-狗, 2-鸟, 3-异宠, 4-其他
    let type = null
    if (currentTab.value === 1) type = 0 // 猫
    else if (currentTab.value === 2) type = 1 // 狗
    else if (currentTab.value === 3) type = 3 // 异宠
    
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: type,
      status: 0 // 只看待领养
    }

    // 只有点击“离我最近”才按照城市筛选
    if (filterType.value === 'nearest') {
      params.city = filterCity.value
    }

    const res = await getPetList(params)
    const newItems = res.data.records || []
    
    if (reset) {
      petList.value = newItems
    } else {
      petList.value = [...petList.value, ...newItems]
    }
    
    // 瀑布流逻辑
    leftList.value = petList.value.filter((_, i) => i % 2 === 0)
    rightList.value = petList.value.filter((_, i) => i % 2 !== 0)
    
    if (newItems.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (error) {
    console.error('加载宠物列表失败', error)
    loadStatus.value = 'loadmore'
  }
}

// 事件处理
const handleColumnChange = (e) => {
  // 列发生改变
  if (e.detail.column === 0) {
    const pIndex = e.detail.value
    // 更新第二列数据
    range.value[1] = cityData[pIndex].cities
    // 重置第二列选中
    multiIndex.value[0] = pIndex
    multiIndex.value[1] = 0
  }
}

const handlePickerChange = (e) => {
  // 确认选择
  const pIndex = e.detail.value[0]
  const cIndex = e.detail.value[1]
  
  const provinceObj = range.value[0][pIndex]
  // 确保 city 存在 (防止第二列未更新导致索引越界)
  const cityList = range.value[1]
  const cityObj = cityList[cIndex] || cityList[0]
  
  const province = provinceObj.name
  const city = cityObj.name
  
  const municipalities = ['北京市', '天津市', '上海市', '重庆市']
  
  if (municipalities.includes(province)) {
    // 直辖市
    currentCity.value = province
    filterCity.value = province
  } else {
    // 省+市
    currentCity.value = `${province} ${city}`
    filterCity.value = city
  }

  // 如果当前是“离我最近”模式，切换城市后需要重新加载
  if (filterType.value === 'nearest') {
    loadData(true)
  }
}

const handleSearch = () => {
  uni.navigateTo({ url: '/pages/search/search' })
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
  uni.navigateTo({ url: '/pages/wiki/index' })
}

const handlePublish = async () => {
  // 检查是否绑定手机号
  try {
    const res = await getMyInfo()
    if (res.data && res.data.phone) {
      // 已绑定，跳转发布页
      uni.navigateTo({
        url: '/pages/pet/publish'
      })
    } else {
      // 未绑定
      uni.showModal({
        title: '提示',
        content: '发布送养信息需要绑定手机号，是否前往绑定？',
        success: (res) => {
          if (res.confirm) {
            // 跳转到个人中心
            uni.switchTab({
              url: '/pages/profile/profile'
            })
          }
        }
      })
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/login' })
    }, 1000)
  }
}

const handleProcess = () => {
  uni.showToast({ title: '领养流程即将上线', icon: 'none' })
}

const handleWallpaper = () => {
  uni.navigateTo({ url: '/pages/cms/wallpaper' })
}

const handleNews = () => {
  uni.navigateTo({ url: '/pages/cms/news' })
}

const handleTabChange = (item) => {
  currentTab.value = item.index
  loadData(true)
}

const changeFilter = (type) => {
  filterType.value = type
  loadData(true)
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
  
  .location-picker {
    margin-right: 20rpx;
  }

  .location {
    display: flex;
    align-items: center;
    
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
      &.wallpaper-bg { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.news-bg { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
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