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

    <!-- 5. AI 悬浮对话框 -->
    <view class="ai-float-dialog" @click="handleAIAssistant">
      <view class="dialog-content">
        <text class="dialog-text">AI 帮我选宠</text>
        <view class="dialog-arrow"></view>
      </view>
      <view class="avatar-wrapper">
        <image src="/static/ai-robot.png" class="ai-avatar-img" mode="aspectFit"></image>
        <view class="online-dot"></view>
      </view>
    </view>
    
    <!-- AI 推荐弹窗 -->
    <u-popup :show="showRecommendPopup" mode="center" round="16" :closeable="true" @close="closeRecommendPopup" :customStyle="{width: '600rpx'}">
      <view class="recommend-popup">
        <view class="popup-title">AI 智能选宠</view>
        
        <!-- 表单阶段 -->
        <view v-if="step === 1" class="form-content">
          <view class="form-item">
            <text class="label">🏠 居住环境</text>
            <view class="tags-group">
              <view 
                class="tag-item" 
                :class="{ active: recommendForm.housing === item }"
                v-for="item in ['公寓', '整租', '合租', '自有住房']" 
                :key="item"
                @click="recommendForm.housing = item"
              >{{ item }}</view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">⏰ 闲暇时间</text>
            <view class="tags-group">
              <view 
                class="tag-item" 
                :class="{ active: recommendForm.time === item }"
                v-for="item in ['充裕', '工作忙', '周末有空', '不固定']" 
                :key="item"
                @click="recommendForm.time = item"
              >{{ item }}</view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">🎓 养宠经验</text>
            <view class="tags-group">
              <view 
                class="tag-item" 
                :class="{ active: recommendForm.experience === item }"
                v-for="item in ['新手', '有经验', '资深']" 
                :key="item"
                @click="recommendForm.experience = item"
              >{{ item }}</view>
            </view>
          </view>
          
          <view class="form-item">
            <text class="label">❤️ 性格偏好</text>
            <view class="tags-group">
              <view 
                class="tag-item" 
                :class="{ active: recommendForm.preference === item }"
                v-for="item in ['粘人', '独立', '活泼', '安静']" 
                :key="item"
                @click="recommendForm.preference = item"
              >{{ item }}</view>
            </view>
          </view>
          
          <view class="form-item row-between">
            <text class="label">🧠 深度思考 (更精准)</text>
            <u-switch v-model="recommendForm.enableThinking" activeColor="#19be6b" size="20"></u-switch>
          </view>
          
          <button class="submit-btn" @click="submitRecommend">开始分析</button>
        </view>
        
        <!-- 加载阶段 -->
        <view v-if="step === 2" class="loading-content">
          <u-loading-icon mode="circle" size="40" color="#19be6b"></u-loading-icon>
          <text class="loading-text">AI 正在分析您的画像...</text>
          <text class="loading-sub">可能需要几秒钟，请耐心等待</text>
        </view>
        
        <!-- 结果阶段 -->
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
    
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app'
import { getPetList } from '@/api/pet'
import { getMyInfo } from '@/api/user'
import { getAiRecommend } from '@/api/ai'
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
  // uni.switchTab({ url: '/pages/ai/ai' })
  showRecommendPopup.value = true
  step.value = 1
}

// AI 推荐相关
const showRecommendPopup = ref(false)
const step = ref(1)
const recommendForm = ref({
  housing: '',
  time: '',
  experience: '',
  preference: '',
  enableThinking: false
})
const recommendResult = ref({})

const closeRecommendPopup = () => {
  showRecommendPopup.value = false
}

const submitRecommend = async () => {
  if (!recommendForm.value.housing || !recommendForm.value.time || !recommendForm.value.experience || !recommendForm.value.preference) {
    uni.showToast({ title: '请完整选择您的偏好', icon: 'none' })
    return
  }
  
  step.value = 2
  try {
    const res = await getAiRecommend(recommendForm.value)
    if (res.code === 200 || res.code === 0) {
      // 解析 JSON 字符串
      try {
        const data = JSON.parse(res.data)
        recommendResult.value = data
        step.value = 3
      } catch (e) {
        console.error('解析推荐结果失败', e)
        uni.showToast({ title: '结果解析失败', icon: 'none' })
        step.value = 1
      }
    } else {
      uni.showToast({ title: res.msg || '分析失败', icon: 'none' })
      step.value = 1
    }
  } catch (error) {
    console.error('推荐请求失败', error)
    uni.showToast({ title: '网络请求失败', icon: 'none' })
    step.value = 1
  }
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

/* AI 悬浮对话框 */
.ai-float-dialog {
  position: fixed;
  right: 30rpx;
  bottom: 120rpx;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  
  .dialog-content {
    background: linear-gradient(135deg, #19be6b, #28d07e);
    padding: 12rpx 24rpx;
    border-radius: 32rpx 32rpx 4rpx 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(25, 190, 107, 0.3);
    margin-bottom: 10rpx;
    position: relative;
    animation: float 3s ease-in-out infinite;
    
    .dialog-text {
      color: #fff;
      font-size: 26rpx;
      font-weight: bold;
    }
    
    .dialog-arrow {
      position: absolute;
      bottom: -10rpx;
      right: 30rpx;
      width: 0;
      height: 0;
      border-left: 10rpx solid transparent;
      border-right: 10rpx solid transparent;
      border-top: 12rpx solid #28d07e;
    }
  }
  
  .avatar-wrapper {
    width: 90rpx;
    height: 90rpx;
    background: #fff;
    border-radius: 50%;
    padding: 10rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
    position: relative;
    margin-right: 10rpx;
    
    .ai-avatar-img {
      width: 100%;
      height: 100%;
    }
    
    .online-dot {
      position: absolute;
      bottom: 4rpx;
      right: 4rpx;
      width: 20rpx;
      height: 20rpx;
      background: #19be6b;
      border: 4rpx solid #fff;
      border-radius: 50%;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

/* 推荐弹窗样式 */
.recommend-popup {
  padding: 40rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  
  .popup-title {
    font-size: 36rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 40rpx;
    color: #333;
  }
  
  /* 表单阶段 */
  .form-content {
    .form-item {
      margin-bottom: 30rpx;
      
      .label {
        display: block;
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 20rpx;
      }
      
      .tags-group {
        display: flex;
        flex-wrap: wrap;
        gap: 20rpx;
        
        .tag-item {
          padding: 12rpx 24rpx;
          background: #f5f5f5;
          border-radius: 32rpx;
          font-size: 26rpx;
          color: #666;
          border: 2rpx solid transparent;
          transition: all 0.3s;
          
          &.active {
            background: rgba(25, 190, 107, 0.1);
            color: #19be6b;
            border-color: #19be6b;
            font-weight: bold;
          }
        }
      }
      
      &.row-between {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 40rpx;
        
        .label {
          margin-bottom: 0;
        }
      }
    }
    
    .submit-btn {
      width: 100%;
      height: 88rpx;
      line-height: 88rpx;
      background: linear-gradient(90deg, #19be6b, #28d07e);
      color: #fff;
      font-size: 32rpx;
      font-weight: bold;
      border-radius: 44rpx;
      margin-top: 20rpx;
      
      &:active {
        opacity: 0.9;
      }
    }
  }
  
  /* 加载阶段 */
  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60rpx 0;
    
    .loading-text {
      margin-top: 30rpx;
      font-size: 30rpx;
      color: #333;
      font-weight: bold;
    }
    
    .loading-sub {
      margin-top: 10rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
  
  /* 结果阶段 */
  .result-content {
    .result-scroll {
      max-height: 800rpx; // 限制高度，超出滚动
    }
    
    .section-title {
      display: block;
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 20rpx;
      padding-left: 16rpx;
      border-left: 8rpx solid #19be6b;
    }
    
    .analysis-box {
      background: #f9f9f9;
      padding: 24rpx;
      border-radius: 16rpx;
      margin-bottom: 40rpx;
      
      .analysis-text {
        font-size: 28rpx;
        color: #555;
        line-height: 1.6;
      }
    }
    
    .recommend-list {
      margin-bottom: 40rpx;
      
      .rec-item {
        background: #fff;
        border: 2rpx solid #eee;
        border-radius: 16rpx;
        padding: 24rpx;
        margin-bottom: 20rpx;
        box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
        
        .rec-header {
          display: flex;
          align-items: center;
          margin-bottom: 12rpx;
          
          .rec-index {
            width: 36rpx;
            height: 36rpx;
            line-height: 36rpx;
            text-align: center;
            background: #19be6b;
            color: #fff;
            font-size: 22rpx;
            border-radius: 50%;
            margin-right: 16rpx;
          }
          
          .rec-name {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
          }
        }
        
        .rec-reason {
          font-size: 26rpx;
          color: #666;
          line-height: 1.5;
        }
      }
    }
    
    .retry-btn {
      width: 100%;
      height: 80rpx;
      line-height: 80rpx;
      background: #f5f5f5;
      color: #666;
      font-size: 28rpx;
      border-radius: 40rpx;
    }
  }
}
</style>