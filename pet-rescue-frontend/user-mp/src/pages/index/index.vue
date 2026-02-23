<template>
  <view class="container">
    <!-- 1. 顶部区域 (绿色背景 + 圆角) -->
    <view class="header-container">
      <!-- 状态栏占位 -->
      <view :style="{ height: statusBarHeight + 'px' }"></view>
      
      <!-- 顶部导航栏 -->
      <view class="nav-bar">
        <!-- 城市选择 -->
        <picker mode="multiSelector" :range="range" range-key="name" :value="multiIndex" @change="handlePickerChange" @columnchange="handleColumnChange">
          <view class="location-chip">
            <u-icon name="map-fill" size="14" color="#FFC107"></u-icon>
            <text class="city-text">{{ currentCity.split(' ')[0] }}</text>
            <u-icon name="arrow-down" size="10" color="#fff"></u-icon>
          </view>
        </picker>

        <!-- 搜索框 -->
        <view class="search-bar" @click="handleSearch">
          <u-icon name="search" size="18" color="#2E7D32"></u-icon>
          <text class="placeholder">搜索宠物或资讯</text>
        </view>

        <!-- 消息通知 -->
        <view class="notification-btn" @click="handleMessage">
          <u-icon name="xiaoxi" custom-prefix="custom-icon" size="20" color="#fff"></u-icon>
          <view class="badge" v-if="hasNewMsg"></view>
        </view>
      </view>

      <!-- 2. 轮播图/精选故事 (嵌入在头部背景中) -->
      <view class="banner-section">
        <swiper class="banner-swiper" circular autoplay interval="4000" indicator-dots indicator-active-color="#FFC107" indicator-color="rgba(255,255,255,0.6)">
          <swiper-item v-for="(item, index) in bannerList" :key="index">
            <view class="banner-card" @click="handleBannerClick(item)">
              <image :src="item.image" mode="aspectFill" class="banner-img"></image>
              <view class="banner-overlay">
                <view class="banner-tag">精选故事</view>
                <text class="banner-title">寻找爱的归宿<br/>每一个生命都值得被温柔以待</text>
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>
    </view>

    <!-- 3. 金刚区 (3列布局) -->
    <view class="grid-section">
      <view class="grid-card">
        <view class="grid-item" @click="handleWiki">
          <view class="icon-circle bg-green-light">
            <u-icon name="chongwuziliaoguanli-gongzuotai" custom-prefix="custom-icon" color="#2E7D32" size="28"></u-icon>
          </view>
          <text class="grid-label">养宠百科</text>
        </view>
        <view class="grid-item" @click="handleAdoptionDiary">
          <view class="icon-circle bg-yellow-light">
            <u-icon name="riji" custom-prefix="custom-icon" color="#a16207" size="28"></u-icon>
          </view>
          <text class="grid-label">领养日记</text>
        </view>
        <view class="grid-item" @click="handlePublish">
          <view class="icon-circle bg-orange-light">
            <u-icon name="fabu" custom-prefix="custom-icon" color="#E64A19" size="28"></u-icon>
          </view>
          <text class="grid-label">发布送养</text>
        </view>
        <view class="grid-item" @click="handleProcess">
          <view class="icon-circle bg-teal-light">
            <u-icon name="liucheng" custom-prefix="custom-icon" color="#0f766e" size="28"></u-icon>
          </view>
          <text class="grid-label">领养流程</text>
        </view>
        <view class="grid-item" @click="handleWallpaper">
          <view class="icon-circle bg-purple-light">
            <u-icon name="bizhi" custom-prefix="custom-icon" color="#7e22ce" size="28"></u-icon>
          </view>
          <text class="grid-label">精选壁纸</text>
        </view>
        <view class="grid-item" @click="handleNews">
          <view class="icon-circle bg-blue-light">
            <u-icon name="zixun" custom-prefix="custom-icon" color="#1d4ed8" size="28"></u-icon>
          </view>
          <text class="grid-label">资讯中心</text>
        </view>
      </view>
    </view>

    <!-- 4. 新家成员 (横向滚动) -->
    <view class="section-container" v-if="adoptionPosts.length > 0">
      <view class="section-header">
        <view class="header-left">
          <view class="icon-box-small">
            <u-icon name="home-fill" color="#E64A19" size="16"></u-icon>
          </view>
          <text class="section-title">新家成员</text>
        </view>
        <view class="view-all-btn" @click="handleAdoptionDiary">
          <text>查看全部</text>
          <u-icon name="arrow-right" size="12" color="#2E7D32"></u-icon>
        </view>
      </view>
      
      <scroll-view scroll-x class="member-scroll" show-scrollbar="false">
        <view class="member-list">
          <view class="member-card" v-for="(item, index) in adoptionPosts" :key="item.id" @click="goPostDetail(item.id)">
            <view class="image-wrapper">
              <image :src="item.cover" mode="aspectFill" class="post-img"></image>
              <view class="user-badge">
                <image :src="item.userAvatar" class="avatar-img"></image>
              </view>
            </view>
            <text class="member-desc">{{ item.shortContent || '新生活' }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 5. 宠物列表 -->
    <view class="pet-list-section">
      <!-- 胶囊Tabs -->
      <view class="sticky-header" :style="{ top: headerStyleTop }">
        <scroll-view scroll-x class="capsule-tabs" show-scrollbar="false">
          <view class="tabs-wrapper">
            <view 
              class="capsule-item" 
              :class="{ active: currentTab === index }" 
              v-for="(item, index) in tabList" 
              :key="index"
              @click="handleTabChange({ index })"
            >
              {{ item.name }}
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 瀑布流内容 -->
      <view class="waterfall-container">
        <view class="column">
          <view class="pet-card-modern" v-for="item in leftList" :key="item.id" @click="goDetail(item)">
            <view class="card-image-box">
              <image :src="item.coverImg" mode="widthFix" class="card-img"></image>
              <view class="like-btn-float">
                <u-icon name="shoucang" custom-prefix="custom-icon" color="#fff" size="18"></u-icon>
              </view>
            </view>
            <view class="card-info">
              <view class="card-row-top">
                <text class="pet-name">{{ item.name }}</text>
                <view class="type-badge" :class="item.type === 1 ? 'badge-dog' : 'badge-cat'">
                  {{ item.type === 1 ? '狗狗' : (item.type === 0 ? '猫猫' : '其他') }}
                </view>
              </view>
              <view class="card-row-bottom">
                <text>{{ item.age }}</text>
                <view class="dot"></view>
                <text>{{ item.breed }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="column">
          <view class="pet-card-modern" v-for="item in rightList" :key="item.id" @click="goDetail(item)">
            <view class="card-image-box">
              <image :src="item.coverImg" mode="widthFix" class="card-img"></image>
              <view class="like-btn-float">
                <u-icon name="heart" color="#fff" size="18"></u-icon>
              </view>
            </view>
            <view class="card-info">
              <view class="card-row-top">
                <text class="pet-name">{{ item.name }}</text>
                <view class="type-badge" :class="item.type === 1 ? 'badge-dog' : 'badge-cat'">
                  {{ item.type === 1 ? '狗狗' : (item.type === 0 ? '猫猫' : '其他') }}
                </view>
              </view>
              <view class="card-row-bottom">
                <text>{{ item.age }}</text>
                <view class="dot"></view>
                <text>{{ item.breed }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <u-empty v-if="petList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png" marginTop="40"></u-empty>
      <u-loadmore :status="loadStatus" marginTop="30" marginBottom="30"></u-loadmore>
    </view>

    <!-- AI 悬浮球 -->
    <view class="ai-float-btn" @click="handleAIAssistant">
      <image src="../../static/AI助手图标.png" class="ai-icon"></image>
    </view>

    <!-- AI 弹窗保持原样 -->
    <u-popup :show="showRecommendPopup" mode="center" round="16" :closeable="true" @close="closeRecommendPopup" :customStyle="{width: '600rpx'}">
      <view class="recommend-popup">
        <view class="popup-title">AI 智能选宠</view>
        <view v-if="step === 1" class="form-content">
          <view class="form-item">
            <text class="label">🏠 居住环境</text>
            <view class="tags-group">
              <view class="tag-item" :class="{ active: recommendForm.housing === item }" v-for="item in ['公寓', '整租', '合租', '自有住房']" :key="item" @click="recommendForm.housing = item">{{ item }}</view>
            </view>
          </view>
          <view class="form-item">
            <text class="label">⏰ 闲暇时间</text>
            <view class="tags-group">
              <view class="tag-item" :class="{ active: recommendForm.time === item }" v-for="item in ['充裕', '工作忙', '周末有空', '不固定']" :key="item" @click="recommendForm.time = item">{{ item }}</view>
            </view>
          </view>
          <view class="form-item">
            <text class="label">🎓 养宠经验</text>
            <view class="tags-group">
              <view class="tag-item" :class="{ active: recommendForm.experience === item }" v-for="item in ['新手', '有经验', '资深']" :key="item" @click="recommendForm.experience = item">{{ item }}</view>
            </view>
          </view>
          <view class="form-item">
            <text class="label">❤️ 性格偏好</text>
            <view class="tags-group">
              <view class="tag-item" :class="{ active: recommendForm.preference === item }" v-for="item in ['粘人', '独立', '活泼', '安静']" :key="item" @click="recommendForm.preference = item">{{ item }}</view>
            </view>
          </view>
          <view class="form-item row-between">
            <text class="label">🧠 深度思考 (更精准)</text>
            <u-switch v-model="recommendForm.enableThinking" activeColor="#2E7D32" size="20"></u-switch>
          </view>
          <button class="submit-btn" @click="submitRecommend">开始分析</button>
        </view>
        <view v-if="step === 2" class="loading-content">
          <u-loading-icon mode="circle" size="40" color="#2E7D32"></u-loading-icon>
          <text class="loading-text">AI 正在分析您的画像...</text>
          <text class="loading-sub">可能需要几秒钟，请耐心等待</text>
        </view>
        <view v-if="step === 3" class="result-content">
          <scroll-view scroll-y class="result-scroll">
            <view class="analysis-box">
              <text class="section-title">📊 分析建议</text>
              <text class="analysis-text">{{ recommendResult.analysis }}</text>
            </view>
            <view class="recommend-list">
              <text class="section-title">🌟 推荐宠物</text>
              <view class="rec-item" v-for="(item, index) in recommendResult.recommendations" :key="index">
                <view class="rec-header">
                  <text class="rec-index">{{ index + 1 }}</text>
                  <text class="rec-name">{{ item.petName }}</text>
                </view>
                <text class="rec-reason">{{ item.reason }}</text>
              </view>
            </view>
          </scroll-view>
          <button class="retry-btn" @click="step = 1">重新测评</button>
        </view>
      </view>
    </u-popup>

    <my-tabbar :current="0"></my-tabbar>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app'
import { getPetList } from '@/api/pet'
import { getAdoptionPostList } from '@/api/adoption-post'
import { getMyInfo, getUserDetail } from '@/api/user'
import { getPetDetail } from '@/api/pet'
import { getAiRecommend } from '@/api/ai'
import { getBannerList } from '@/api/banner'
import { cityData } from '@/utils/cityData'

// 状态定义
const currentCity = ref('重庆市') 
const filterCity = ref('重庆市') 
const hasNewMsg = ref(true)
const statusBarHeight = ref(20) 

// 城市选择器
const multiIndex = ref([21, 0]) 
const range = ref([cityData, cityData[21].cities])

const initCityPicker = () => {
  const pIndex = cityData.findIndex(p => p.name === '重庆市')
  if (pIndex > -1) {
    multiIndex.value[0] = pIndex
    range.value[1] = cityData[pIndex].cities
    multiIndex.value[1] = 0
  } else {
    multiIndex.value = [0, 0]
    range.value[1] = cityData[0].cities
  }
}

// 计算 sticky 顶部距离 (状态栏 + 导航栏高度)
const headerStyleTop = computed(() => {
  // 假设导航栏内容高度约 44px (88rpx)
  return (statusBarHeight.value) + 'px'
})

const bannerList = ref([])
const fetchBannerList = async () => {
  try {
    const res = await getBannerList()
    if (res.data && Array.isArray(res.data)) {
      bannerList.value = res.data.map(item => ({
        id: item.id,
        image: item.imgUrl,
        targetUrl: item.targetUrl
      }))
    }
  } catch (e) {
    console.error('获取轮播图失败', e)
  }
}

// Tab 配置
const tabList = ref([
  { name: '推荐' },
  { name: '猫猫' },
  { name: '狗狗' },
  { name: '其他' }
])
const currentTab = ref(0)
const filterType = ref('latest')

const petList = ref([])
const leftList = ref([])
const rightList = ref([])
const loadStatus = ref('loadmore')
const pageNum = ref(1)
const pageSize = ref(10)

// 领养日记/新家成员
const adoptionPosts = ref([])
const fetchAdoptionPosts = async () => {
  try {
    const res = await getAdoptionPostList({ pageNum: 1, pageSize: 5 })
    if (res.data && Array.isArray(res.data)) {
      const posts = res.data

      // 异步获取每个帖子的用户信息并处理数据
      const processedPosts = await Promise.all(posts.map(async (post) => {
        let userAvatar = '' // 默认头像
        try {
          // 获取用户信息
          const userRes = await getUserDetail(post.userId)
          if (userRes.data && userRes.data.avatar) {
            userAvatar = userRes.data.avatar
          }
        } catch (e) {
          console.error(`获取用户 ${post.userId} 信息失败`, e)
        }

        // 提取封面图 (处理不规范的 images 字符串)
        const cover = post.images ? post.images.replace(/`/g, '').trim() : ''
        
        // 创建简短内容
        const shortContent = post.content.length > 20 ? post.content.substring(0, 20) + '...' : post.content

        return {
          ...post,
          userAvatar,
          cover,
          shortContent,
        }
      }))
      
      adoptionPosts.value = processedPosts
    }
  } catch (error) {
    console.error('获取领养日记失败', error)
  }
}

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  initCityPicker()
  fetchBannerList()
  loadData(true)
  fetchAdoptionPosts()
})

onShow(() => {
  uni.hideTabBar()
  fetchAdoptionPosts()
})

onReachBottom(() => {
  if (loadStatus.value === 'nomore') return
  loadStatus.value = 'loading'
  pageNum.value++
  loadData()
})

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    petList.value = []
    leftList.value = []
    rightList.value = []
    loadStatus.value = 'loading'
  }

  try {
    let type = null
    if (currentTab.value === 1) type = 0 
    else if (currentTab.value === 2) type = 1 
    else if (currentTab.value === 3) type = 3 
    
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: type,
      status: 0 
    }

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

const handleColumnChange = (e) => {
  if (e.detail.column === 0) {
    const pIndex = e.detail.value
    range.value[1] = cityData[pIndex].cities
    multiIndex.value[0] = pIndex
    multiIndex.value[1] = 0
  }
}

const handlePickerChange = (e) => {
  const pIndex = e.detail.value[0]
  const cIndex = e.detail.value[1]
  const provinceObj = range.value[0][pIndex]
  const cityList = range.value[1]
  const cityObj = cityList[cIndex] || cityList[0]
  const province = provinceObj.name
  const city = cityObj.name
  const municipalities = ['北京市', '天津市', '上海市', '重庆市']
  
  if (municipalities.includes(province)) {
    currentCity.value = province
    filterCity.value = province
  } else {
    currentCity.value = `${province} ${city}`
    filterCity.value = city
  }
  if (filterType.value === 'nearest') loadData(true)
}

const handleSearch = () => uni.navigateTo({ url: '/pages/search/search' })
const handleMessage = () => uni.switchTab({ url: '/pages/message/message' })
const handleBannerClick = (item) => {
  if (item.targetUrl && item.targetUrl.startsWith('/pages/')) {
    uni.navigateTo({ url: item.targetUrl })
  }
}
const handleWiki = () => uni.navigateTo({ url: '/pages/wiki/index' })
const handleAdoptionDiary = () => uni.navigateTo({ url: '/pages/post/list' })
const handlePublish = async () => {
  try {
    const res = await getMyInfo()
    if (res.data && res.data.phone) {
      uni.navigateTo({ url: '/pages/pet/publish' })
    } else {
      uni.showModal({
        title: '提示',
        content: '发布送养信息需要绑定手机号，是否前往绑定？',
        success: (res) => {
          if (res.confirm) uni.switchTab({ url: '/pages/profile/profile' })
        }
      })
    }
  } catch (error) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 1000)
  }
}
const handleProcess = () => uni.navigateTo({ url: '/pages/adoption/process' })
const handleWallpaper = () => uni.navigateTo({ url: '/pages/cms/wallpaper' })
const handleNews = () => uni.navigateTo({ url: '/pages/cms/news' })
const handleTabChange = (item) => {
  currentTab.value = item.index
  loadData(true)
}
const goDetail = (item) => uni.navigateTo({ url: `/pages/pet/detail?id=${item.id}` })
const goPostDetail = (id) => uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
const handleAIAssistant = () => {
  showRecommendPopup.value = true
  step.value = 1
}

// AI Popup Logic
const showRecommendPopup = ref(false)
const step = ref(1)
const recommendForm = ref({ housing: '', time: '', experience: '', preference: '', enableThinking: false })
const recommendResult = ref({})
const closeRecommendPopup = () => showRecommendPopup.value = false
const submitRecommend = async () => {
  if (!recommendForm.value.housing || !recommendForm.value.time || !recommendForm.value.experience || !recommendForm.value.preference) {
    uni.showToast({ title: '请完整选择您的偏好', icon: 'none' })
    return
  }
  step.value = 2
  try {
    const res = await getAiRecommend(recommendForm.value)
    if (res.code === 200 || res.code === 0) {
      try {
        const data = JSON.parse(res.data)
        recommendResult.value = data
        step.value = 3
      } catch (e) {
        step.value = 1
        uni.showToast({ title: '解析结果失败', icon: 'none' })
      }
    } else {
      step.value = 1
      uni.showToast({ title: res.message || '分析失败', icon: 'none' })
    }
  } catch (error) {
    step.value = 1
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  background-color: #f8f8f8;
  padding-bottom: 120rpx; // 为自定义tabbar留出空间
}

// 1. 顶部区域
.header-container {
  background: linear-gradient(180deg, #388E3C 0%, #2E7D32 100%);
  border-radius: 0 0 60rpx 60rpx;
  padding: 0 32rpx 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  gap: 16rpx;
}

.location-chip {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8rpx 16rpx;
  border-radius: 32rpx;
  .city-text {
    color: #fff;
    font-size: 26rpx;
    font-weight: bold;
    margin: 0 8rpx;
  }
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #fff;
  height: 64rpx;
  border-radius: 32rpx;
  padding: 0 24rpx;
  .placeholder {
    color: #999;
    font-size: 26rpx;
    margin-left: 12rpx;
  }
}

.notification-btn {
  position: relative;
  padding: 8rpx;
  .badge {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 12rpx;
    height: 12rpx;
    background-color: #FF5722;
    border-radius: 50%;
    border: 2rpx solid #fff;
  }
}

// 2. 轮播图
.banner-section {
  margin-top: 20rpx;
}

.banner-swiper {
  height: 280rpx;
}

.banner-card {
  position: relative;
  height: 100%;
  border-radius: 32rpx;
  overflow: hidden;
  .banner-img {
    width: 100%;
    height: 100%;
  }
  .banner-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 40rpx 24rpx 24rpx;
    background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
    .banner-tag {
      background-color: #FFC107;
      color: #2E7D32;
      font-size: 20rpx;
      font-weight: bold;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      display: inline-block;
    }
    .banner-title {
      color: #fff;
      font-size: 28rpx;
      font-weight: bold;
      margin-top: 12rpx;
      display: block;
      line-height: 1.4;
    }
  }
}

// 3. 金刚区
.grid-section {
  margin: -40rpx 32rpx 0;
  position: relative;
  z-index: 11;
}

.grid-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  background-color: #fff;
  padding: 32rpx;
  border-radius: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  .icon-circle {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
    &.bg-green-light { background-color: #E8F5E9; }
    &.bg-yellow-light { background-color: #FFF9C4; }
    &.bg-orange-light { background-color: #FFE0B2; }
    &.bg-teal-light { background-color: #B2DFDB; }
    &.bg-purple-light { background-color: #E1BEE7; }
    &.bg-blue-light { background-color: #D3EAFD; }
  }
  .grid-label {
    font-size: 24rpx;
    color: #333;
  }
}

// 4. 新家成员
.section-container {
  background-color: #fff;
  margin: 32rpx;
  padding: 24rpx;
  border-radius: 32rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  .header-left {
    display: flex;
    align-items: center;
    gap: 12rpx;
    .icon-box-small {
      width: 40rpx;
      height: 40rpx;
      border-radius: 12rpx;
      background-color: #FFF3E0;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
  .view-all-btn {
    display: flex;
    align-items: center;
    color: #2E7D32;
    font-size: 24rpx;
  }
}

.member-scroll {
  .member-list {
    display: flex;
    gap: 24rpx;
  }
}

.member-card {
  width: 200rpx;
  flex-shrink: 0;
  .image-wrapper {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    border-radius: 24rpx;
    overflow: hidden;
    .post-img {
      width: 100%;
      height: 100%;
    }
    .user-badge {
      position: absolute;
      bottom: -10rpx;
      right: -10rpx;
      width: 50rpx;
      height: 50rpx;
      border-radius: 50%;
      background: #fff;
      padding: 4rpx;
      .avatar-img {
        width: 100%;
        height: 100%;
        border-radius: 50%;
      }
    }
  }
  .member-desc {
    font-size: 24rpx;
    color: #666;
    margin-top: 12rpx;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

// 5. 宠物列表
.pet-list-section {
  padding: 0 32rpx;
}

.sticky-header {
  position: sticky;
  // top is calculated dynamically
  background-color: #f8f8f8;
  padding: 20rpx 0;
  z-index: 9;
}

.capsule-tabs {
  .tabs-wrapper {
    display: flex;
    gap: 16rpx;
  }
  .capsule-item {
    background-color: #fff;
    color: #333;
    padding: 12rpx 32rpx;
    border-radius: 32rpx;
    font-size: 26rpx;
    transition: all 0.3s ease;
    white-space: nowrap;
    &.active {
      background-color: #2E7D32;
      color: #fff;
      font-weight: bold;
    }
  }
}

.waterfall-container {
  display: flex;
  gap: 16rpx;
  .column {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }
}

.pet-card-modern {
  background-color: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  break-inside: avoid;
  .card-image-box {
    position: relative;
    .card-img {
      width: 100%;
      display: block;
    }
    .like-btn-float {
      position: absolute;
      top: 16rpx;
      right: 16rpx;
      width: 50rpx;
      height: 50rpx;
      border-radius: 50%;
      background-color: rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(4px);
    }
  }
  .card-info {
    padding: 20rpx;
    .card-row-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .pet-name {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
      }
      .type-badge {
        font-size: 20rpx;
        padding: 4rpx 10rpx;
        border-radius: 8rpx;
        &.badge-cat { background-color: #E8F5E9; color: #2E7D32; }
        &.badge-dog { background-color: #FFF3E0; color: #E65100; }
      }
    }
    .card-row-bottom {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
      .dot { width: 4rpx; height: 4rpx; background: #ccc; border-radius: 50%; margin: 0 12rpx; }
    }
  }
}

// AI 悬浮球
.ai-float-btn {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(46, 125, 50, 0.4);
  z-index: 100;
  overflow: hidden;
  
  .ai-icon {
    width: 100%;
    height: 100%;
  }
}

// AI 弹窗
.recommend-popup {
  padding: 32rpx;
  .popup-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 32rpx;
    color: #2E7D32;
  }
  .form-content, .result-content, .loading-content {
    .form-item {
      margin-bottom: 24rpx;
      .label { font-size: 28rpx; color: #333; margin-bottom: 16rpx; display: block; }
      .tags-group {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;
        .tag-item {
          background: #f0f0f0;
          color: #666;
          padding: 10rpx 24rpx;
          border-radius: 24rpx;
          font-size: 24rpx;
          &.active { background: #2E7D32; color: #fff; }
        }
      }
      &.row-between { display: flex; justify-content: space-between; align-items: center; }
    }
    .submit-btn, .retry-btn {
      background: linear-gradient(90deg, #4CAF50, #388E3C);
      color: white;
      border-radius: 40rpx;
      height: 80rpx;
      line-height: 80rpx;
      font-size: 30rpx;
      margin-top: 40rpx;
      border: none;
      &:after { border: none; }
    }
  }
  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80rpx 0;
    .loading-text { font-size: 30rpx; color: #333; margin-top: 24rpx; }
    .loading-sub { font-size: 24rpx; color: #999; margin-top: 12rpx; }
  }
  .result-content {
    .result-scroll { max-height: 60vh; }
    .analysis-box, .recommend-list {
      margin-bottom: 32rpx;
      .section-title { font-size: 30rpx; font-weight: bold; color: #2E7D32; margin-bottom: 16rpx; display: block; }
      .analysis-text { font-size: 26rpx; color: #666; line-height: 1.6; }
    }
    .recommend-list {
      .rec-item {
        background: #f8f8f8;
        padding: 20rpx;
        border-radius: 16rpx;
        margin-bottom: 16rpx;
        .rec-header {
          display: flex;
          align-items: center;
          gap: 12rpx;
          .rec-index { font-weight: bold; color: #2E7D32; }
          .rec-name { font-weight: bold; color: #333; }
        }
        .rec-reason { font-size: 24rpx; color: #777; margin-top: 8rpx; }
      }
    }
    .retry-btn { margin-top: 20rpx; }
  }
}
</style>
