<template>
  <view class="wiki-container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">宠物百科</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="header-section" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <view class="search-bar" @click="handleSearch">
        <uni-icons type="search" size="18" color="#2E7D32"></uni-icons>
        <text class="placeholder">搜索文章、活动或公告...</text>
      </view>

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
    </view>

    <view class="content-section">
      <view class="section-header">
        <text class="section-title">推荐阅读</text>
        <text class="view-all">查看全部</text>
      </view>

      <view class="article-list">
        <view 
          v-for="item in articleList" 
          :key="item.id" 
          class="article-card"
          @click="goDetail(item)"
        >
          <view class="article-cover-wrapper">
            <image :src="item.coverImg" mode="aspectFill" class="article-cover"></image>
          </view>
          <view class="article-content">
            <view class="tag-row">
              <view class="article-tag" :class="getTagClass(item.tags)">{{ getFirstTag(item.tags) }}</view>
            </view>
            <view class="article-title">{{ item.title }}</view>
            <view class="article-meta">
              <text class="read-time">{{ getReadTime(item.content) }}分钟阅读</text>
              <text class="publish-time">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>
        </view>
      </view>

      <u-empty v-if="articleList.length === 0 && loadStatus !== 'loading'" mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png">
      </u-empty>

      <u-loadmore :status="loadStatus" marginTop="30"></u-loadmore>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { getArticleList } from '@/api/article'

const statusBarHeight = ref(20)
const categoryList = ref([
  { name: '百科文章', value: '' },
  { name: '新手必读', value: '新手必读' },
  { name: '健康医疗', value: '健康医疗' },
  { name: '日常护理', value: '日常护理' },
  { name: '行为训练', value: '行为训练' },
  { name: '营养饮食', value: '营养饮食' }
])
const currentCategory = ref('')
const articleList = ref([])
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

const goBack = () => {
  uni.navigateBack()
}

const getFirstTag = (tags) => {
  if (!tags) return '百科'
  const tagArray = tags.split(',').map(t => t.trim()).filter(t => t)
  return tagArray[0] || '百科'
}

const getTagClass = (tags) => {
  const tag = getFirstTag(tags)
  if (tag.includes('新手') || tag.includes('指南')) return 'tag-green'
  if (tag.includes('猫咪') || tag.includes('行为')) return 'tag-orange'
  if (tag.includes('健康') || tag.includes('饮食')) return 'tag-blue'
  return 'tag-default'
}

const getReadTime = (content) => {
  if (!content) return 3
  const wordCount = content.replace(/<[^>]+>/g, '').length
  return Math.max(1, Math.ceil(wordCount / 500))
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
    return '昨天'
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
  background-color: #F9FAFB;
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

.header-section {
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.search-bar {
  height: 72rpx;
  background: #F3F4F6;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  margin-bottom: 20rpx;

  .placeholder {
    font-size: 28rpx;
    color: #9CA3AF;
    margin-left: 12rpx;
  }
}

.category-tabs {
  margin-top: 10rpx;

  .tabs-scroll {
    white-space: nowrap;

    .tab-item {
      display: inline-block;
      padding: 16rpx 28rpx;
      margin-right: 16rpx;
      font-size: 28rpx;
      color: #6B7280;
      border-radius: 24rpx;
      background: transparent;
      transition: all 0.3s;
      font-weight: 500;

      &.active {
        color: #2E7D32;
        background: #E8F5E9;
        font-weight: bold;
      }
    }
  }
}

.content-section {
  padding: 24rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #1F2937;
  }
  
  .view-all {
    font-size: 24rpx;
    color: #2E7D32;
    font-weight: 600;
  }
}

.article-list {
  .article-card {
    display: flex;
    background: #fff;
    border-radius: 20rpx;
    overflow: hidden;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);

    .article-cover-wrapper {
      width: 200rpx;
      height: 180rpx;
      flex-shrink: 0;

      .article-cover {
        width: 100%;
        height: 100%;
      }
    }

    .article-content {
      flex: 1;
      padding: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .tag-row {
        margin-bottom: 8rpx;
      }

      .article-tag {
        display: inline-block;
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 20rpx;
        font-weight: bold;
        
        &.tag-green {
          background: #E8F5E9;
          color: #2E7D32;
        }
        
        &.tag-orange {
          background: #FFF3E0;
          color: #E65100;
        }
        
        &.tag-blue {
          background: #E3F2FD;
          color: #1565C0;
        }
        
        &.tag-default {
          background: #F3F4F6;
          color: #6B7280;
        }
      }

      .article-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #1F2937;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .article-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12rpx;

        .read-time, .publish-time {
          font-size: 22rpx;
          color: #9CA3AF;
        }
      }
    }
  }
}
</style>
