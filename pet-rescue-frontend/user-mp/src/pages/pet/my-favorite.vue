<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">我的收藏</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="tabs-box" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <u-tabs 
        :list="tabList" 
        :current="currentTab" 
        @change="handleTabChange"
        active-color="#2E7D32"
        line-color="#2E7D32"
        :scrollable="false"
      ></u-tabs>
    </view>

    <view class="content-area">
      <view v-if="currentTab === 0">
        <u-empty v-if="!loading && petList.length === 0" mode="favor" text="暂无收藏宠物" marginTop="100"></u-empty>
        <view class="pet-grid" v-else>
          <view class="pet-item" v-for="(item, index) in petList" :key="index" @click="goPetDetail(item.id)">
            <view class="pet-cover">
              <image :src="item.coverImg" mode="aspectFill" class="cover-img"></image>
              <view class="pet-overlay">
                <text class="pet-name">{{ item.name }}</text>
                <view class="pet-tag">
                  <text>{{ item.age }} · {{ item.sex === 1 ? '雄性' : '雌性' }} · {{ item.breed }}</text>
                </view>
              </view>
            </view>
            <view class="favorite-btn">
              <uni-icons type="heart-filled" size="16" color="#ef4444"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view v-else>
        <u-empty v-if="!loading && articleList.length === 0" mode="favor" text="暂无收藏内容" marginTop="100"></u-empty>
        <view class="article-list" v-else>
          <view class="article-item" v-for="(item, index) in articleList" :key="index" @click="goArticleDetail(item.id)">
            <view class="article-info">
              <text class="article-title">{{ item.title }}</text>
              <view class="article-meta">
                <view class="view-count">
                  <uni-icons type="eye" size="14" color="#9CA3AF"></uni-icons>
                  <text>{{ item.viewCount || 0 }}次浏览</text>
                </view>
                <text class="time">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
            <image :src="item.coverImg" mode="aspectFill" class="article-cover"></image>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { getMyFavorites } from '@/api/pet'
import { getMyFavoriteArticles } from '@/api/article'
import { formatTime } from '@/utils/index.js'

const statusBarHeight = ref(44)

const tabList = [
  { name: '萌宠' },
  { name: '百科' },
  { name: '资讯' },
  { name: '活动' }
]

const currentTab = ref(0)
const loading = ref(true)
const petList = ref([])
const articleList = ref([])

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
})

onShow(() => {
  fetchData()
})

const goBack = () => {
  uni.navigateBack()
}

const handleTabChange = (item) => {
  currentTab.value = item.index
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    if (currentTab.value === 0) {
      const res = await getMyFavorites()
      if (res.data) {
        petList.value = res.data
      }
    } else {
      const type = currentTab.value - 1
      const res = await getMyFavoriteArticles(type)
      if (res.data) {
        articleList.value = res.data
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goPetDetail = (id) => {
  uni.navigateTo({
    url: `/pages/pet/detail?id=${id}`
  })
}

const goArticleDetail = (id) => {
  if (currentTab.value === 1) {
    uni.navigateTo({ url: `/pages/wiki/detail?id=${id}` })
  } else {
    uni.navigateTo({ url: `/pages/wiki/detail?id=${id}` })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F9FAFB;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  border-bottom: 1rpx solid #f0f0f0;
  
  .nav-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
  }
  
  .back-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #1F2937;
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.tabs-box {
  background-color: #fff;
  padding: 10rpx 0;
}

.content-area {
  flex: 1;
  padding: 24rpx;
}

.pet-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.pet-item {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  position: relative;
  
  .pet-cover {
    position: relative;
    aspect-ratio: 4/5;
    
    .cover-img {
      width: 100%;
      height: 100%;
      background-color: #eee;
    }
    
    .pet-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 20rpx 16rpx;
      background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
      
      .pet-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
        display: block;
      }
      
      .pet-tag {
        margin-top: 6rpx;
        display: inline-block;
        background: rgba(255,255,255,0.2);
        backdrop-filter: blur(4px);
        padding: 4rpx 10rpx;
        border-radius: 8rpx;
        
        text {
          font-size: 20rpx;
          color: #fff;
        }
      }
    }
  }
  
  .favorite-btn {
    position: absolute;
    top: 12rpx;
    right: 12rpx;
    width: 56rpx;
    height: 56rpx;
    background: rgba(255,255,255,0.8);
    backdrop-filter: blur(8px);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.article-item {
  display: flex;
  justify-content: space-between;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);

  .article-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    margin-right: 20rpx;

    .article-title {
      font-size: 30rpx;
      color: #1F2937;
      font-weight: 600;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .article-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 22rpx;
      color: #9CA3AF;
      margin-top: 16rpx;

      .view-count {
        display: flex;
        align-items: center;
        gap: 6rpx;
      }
    }
  }

  .article-cover {
    width: 180rpx;
    height: 130rpx;
    border-radius: 12rpx;
    background-color: #eee;
    flex-shrink: 0;
  }
}
</style>
