<template>
  <view class="container">
    <view class="search-header">
      <u-search
        v-model="keyword"
        placeholder="请输入搜索关键词"
        :show-action="true"
        action-text="搜索"
        :animation="true"
        @search="onSearch"
        @custom="onSearch"
        @clear="onClear"
        shape="round"
        bg-color="#f2f2f2"
        focus
      ></u-search>
      
      <view class="filter-type">
        <text class="label">搜索类型：</text>
        <view class="tags">
            <u-tag 
                text="昵称" 
                :plain="searchType !== 'name'" 
                :type="searchType === 'name' ? 'primary' : 'info'" 
                shape="circle" 
                size="mini"
                @click="searchType = 'name'"
                style="margin-right: 20rpx;"
            ></u-tag>
            <u-tag 
                text="品种" 
                :plain="searchType !== 'breed'" 
                :type="searchType === 'breed' ? 'primary' : 'info'" 
                shape="circle" 
                size="mini"
                @click="searchType = 'breed'"
            ></u-tag>
        </view>
      </view>
    </view>

    <view class="result-list">
      <!-- 宠物搜索结果 -->
      <u-list @scrolltolower="loadMore" v-if="searchFrom === 'pet' && petList.length > 0">
        <u-list-item v-for="(item, index) in petList" :key="index">
          <view class="pet-card" @click="goDetail(item)">
            <image :src="item.coverImg" mode="aspectFill" class="pet-img"></image>
            <view class="pet-info">
              <view class="header-row">
                <text class="pet-name">{{ item.name }}</text>
                <u-tag :text="item.sex === 1 ? '公' : '母'" :type="item.sex === 1 ? 'primary' : 'error'" size="mini" plain shape="circle"></u-tag>
              </view>
              <view class="tags-row">
                <text class="breed">{{ item.breed }}</text>
                <text class="age">{{ item.age }}</text>
              </view>
              <view class="location-row">
                  <u-icon name="map" size="12" color="#999"></u-icon>
                  <text class="city">{{ item.city }}</text>
              </view>
              <view class="desc">{{ item.description || '暂无描述' }}</view>
            </view>
          </view>
        </u-list-item>
      </u-list>
      
      <!-- 百科文章搜索结果 -->
      <view class="wiki-list" v-if="searchFrom === 'wiki' && articleList.length > 0">
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
                  <text>{{ item.viewCount }}</text>
                </view>
              </view>
              <view class="meta-right">{{ formatTime(item.createTime) }}</view>
            </view>
          </view>
        </view>
      </view>
      
      <u-empty v-else-if="searched" mode="search" icon="http://cdn.uviewui.com/uview/empty/search.png" :text="searchFrom === 'wiki' ? '未找到相关文章' : '未找到相关宠物'"></u-empty>
      <view v-else class="empty-placeholder">
          <text>请输入关键词开始搜索</text>
      </view>
      
      <u-loadmore :status="loadStatus" v-if="(searchFrom === 'pet' && petList.length > 0) || (searchFrom === 'wiki' && articleList.length > 0)" marginTop="20"></u-loadmore>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetList } from '@/api/pet'
import { getArticleList } from '@/api/article'

const keyword = ref('')
const searchType = ref('name')
const petList = ref([])
const articleList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const loadStatus = ref('loadmore')
const searched = ref(false)
const searchFrom = ref('pet')

onLoad((options) => {
  if (options.from) {
    searchFrom.value = options.from
  }
})

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
    }
    
    const newItems = searchFrom.value === 'wiki' ? articleList.value : petList.value
    if (newItems.length < pageSize.value) {
      loadStatus.value = 'nomore'
    } else {
      loadStatus.value = 'loadmore'
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

.search-header {
  padding: 20rpx 30rpx;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .filter-type {
    display: none;
  }
}

.result-list {
  padding: 20rpx;
}

.empty-placeholder {
    display: flex;
    justify-content: center;
    padding-top: 100rpx;
    color: #999;
    font-size: 28rpx;
}

.pet-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 20rpx;
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
  overflow: hidden;
  
  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .pet-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
  
  .tags-row {
    display: flex;
    gap: 16rpx;
    font-size: 24rpx;
    color: #666;
  }
  
  .location-row {
      display: flex;
      align-items: center;
      
      .city {
          font-size: 24rpx;
          color: #999;
          margin-left: 4rpx;
      }
  }
  
  .desc {
    font-size: 24rpx;
    color: #999;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.wiki-list {
  padding: 0 20rpx;
}

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
</style>
