<template>
  <view class="wiki-container">
    <!-- 顶部搜索区 -->
    <view class="search-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="search-box" @click="handleSearch">
        <u-icon name="search" size="18" color="#999"></u-icon>
        <text class="placeholder">搜索关键词，如：猫咪感冒、新手养狗</text>
      </view>
    </view>

    <!-- 分类导航栏 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll" show-scrollbar="false">
        <view 
          v-for="(item, index) in categoryList" 
          :key="index"
          class="tab-item"
          :class="{ active: currentCategory === item.value }"
          @click="handleCategoryChange(item)"
        >
          {{ item.name }}
        </view>
      </scroll-view>
    </view>

    <!-- 轮播推荐区 -->
    <view class="banner-section" v-if="featuredList.length > 0">
      <swiper class="banner-swiper" circular indicator-dots autoplay interval="4000" indicator-active-color="#19be6b">
        <swiper-item v-for="(item, index) in featuredList" :key="index" @click="goDetail(item)">
          <image :src="item.coverImg" mode="aspectFill" class="banner-img"></image>
          <view class="banner-title">{{ item.title }}</view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 文章列表区 -->
    <view class="article-list">
      <view 
        v-for="item in articleList" 
        :key="item.id" 
        class="article-card"
        @click="goDetail(item)"
      >
        <image :src="item.coverImg" mode="aspectFill" class="article-cover"></image>
        <view class="article-content">
          <view class="article-title">{{ item.title }}</view>
          <view class="article-summary">{{ item.summary }}</view>
          <view class="article-meta">
            <view class="meta-left">
              <u-tag :text="getFirstTag(item.tags)" type="success" size="mini" plain></u-tag>
              <view class="view-count">
                <u-icon name="eye" size="12" color="#999"></u-icon>
                <text>{{ formatViewCount(item.viewCount) }}</text>
              </view>
            </view>
            <view class="meta-right">{{ formatTime(item.createTime) }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <u-empty v-if="articleList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png">
    </u-empty>

    <!-- 加载更多 -->
    <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'

const statusBarHeight = ref(20)
const categoryList = ref([
  { name: '全部', value: '' },
  { name: '新手必读', value: '新手必读' },
  { name: '健康医疗', value: '健康医疗' },
  { name: '日常护理', value: '日常护理' },
  { name: '行为训练', value: '行为训练' },
  { name: '营养饮食', value: '营养饮食' }
])
const currentCategory = ref('')
const articleList = ref([])
const featuredList = ref([])
const loadStatus = ref('loadmore')
const pageNum = ref(1)
const pageSize = ref(10)

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20
  loadData(true)
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
    articleList.value = []
    featuredList.value = []
    loadStatus.value = 'loading'
  }

  try {
    const res = await getArticleList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: 0,
      category: currentCategory.value
    })

    const newItems = res.data.records || []
    
    if (reset) {
      articleList.value = newItems
      featuredList.value = newItems.slice(0, 3)
    } else {
      articleList.value = [...articleList.value, ...newItems]
    }
    
    if (newItems.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
    }
  } catch (error) {
    console.error('加载文章列表失败', error)
    loadStatus.value = 'loadmore'
  }
}

const handleSearch = () => {
  uni.navigateTo({ url: '/pages/search/search?from=wiki' })
}

const handleCategoryChange = (item) => {
  currentCategory.value = item.value
  loadData(true)
}

const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages/wiki/detail?id=${item.id}`
  })
}

const getFirstTag = (tags) => {
  if (!tags) return ''
  const tagArray = tags.split(',').map(t => t.trim()).filter(t => t)
  return tagArray[0] || ''
}

const formatViewCount = (count) => {
  if (!count) return '0'
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
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
</script>

<style lang="scss" scoped>
.wiki-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 搜索栏 */
.search-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  .search-box {
    height: 64rpx;
    background: #f0f2f5;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    padding: 0 24rpx;

    .placeholder {
      font-size: 26rpx;
      color: #999;
      margin-left: 10rpx;
    }
  }
}

/* 分类标签 */
.category-tabs {
  background: #fff;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  .tabs-scroll {
    white-space: nowrap;
    padding: 0 30rpx;

    .tab-item {
      display: inline-block;
      padding: 12rpx 24rpx;
      margin-right: 20rpx;
      font-size: 28rpx;
      color: #666;
      border-radius: 32rpx;
      background: #f5f5f5;
      transition: all 0.3s;

      &.active {
        color: #fff;
        background: #19be6b;
        font-weight: bold;
      }
    }
  }
}

/* 轮播图 */
.banner-section {
  padding: 20rpx 30rpx;
  background: #fff;
  margin-bottom: 20rpx;

  .banner-swiper {
    height: 320rpx;
    border-radius: 16rpx;
    overflow: hidden;
    position: relative;

    .banner-img {
      width: 100%;
      height: 100%;
    }

    .banner-title {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 40rpx 30rpx 20rpx;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
      color: #fff;
      font-size: 32rpx;
      font-weight: bold;
      text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
    }
  }
}

/* 文章列表 */
.article-list {
  padding: 0 30rpx;

  .article-card {
    display: flex;
    background: #fff;
    border-radius: 16rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .article-cover {
      width: 200rpx;
      height: 150rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }

    .article-content {
      flex: 1;
      margin-left: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .article-title {
        font-size: 30rpx;
        font-weight: bold;
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
        margin: 8rpx 0;
      }

      .article-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .meta-left {
          display: flex;
          align-items: center;
          gap: 16rpx;

          .view-count {
            display: flex;
            align-items: center;
            font-size: 22rpx;
            color: #999;

            text {
              margin-left: 4rpx;
            }
          }
        }

        .meta-right {
          font-size: 22rpx;
          color: #999;
        }
      }
    }
  }
}
</style>
