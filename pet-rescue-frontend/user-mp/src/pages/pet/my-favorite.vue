<template>
  <view class="container">
    <!-- Tabs -->
    <view class="tabs-box">
      <u-tabs 
        :list="tabList" 
        :current="currentTab" 
        @change="handleTabChange"
        active-color="#19be6b"
        line-color="#19be6b"
        :scrollable="false"
      ></u-tabs>
    </view>

    <!-- 内容区域 -->
    <view class="content-area">
      <!-- 萌宠列表 -->
      <view v-if="currentTab === 0">
        <u-empty v-if="!loading && petList.length === 0" mode="favor" icon="http://cdn.uviewui.com/uview/empty/favor.png" text="暂无收藏宠物"></u-empty>
        <view class="pet-list" v-else>
          <view class="pet-item" v-for="(item, index) in petList" :key="index" @click="goPetDetail(item.id)">
            <image :src="item.coverImg" mode="aspectFill" class="cover"></image>
            <view class="info">
              <view class="header">
                 <text class="name">{{ item.name }}</text>
                 <u-tag :text="item.sex === 1 ? '公' : '母'" :type="item.sex === 1 ? 'primary' : 'error'" size="mini" plain shape="circle"></u-tag>
              </view>
              <view class="sub-info">
                 <text>{{ item.breed }}</text>
                 <text class="divider">|</text>
                 <text>{{ item.age }}</text>
              </view>
              <view class="location">
                 <u-icon name="map" size="12" color="#999"></u-icon>
                 <text class="city">{{ item.city || '未知' }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 文章列表 (百科/资讯/活动) -->
      <view v-else>
        <u-empty v-if="!loading && articleList.length === 0" mode="favor" icon="http://cdn.uviewui.com/uview/empty/favor.png" text="暂无收藏内容"></u-empty>
        <view class="article-list" v-else>
          <view class="article-item" v-for="(item, index) in articleList" :key="index" @click="goArticleDetail(item.id)">
            <view class="info">
              <view class="title">{{ item.title }}</view>
              <view class="meta">
                <view class="view-count">
                  <u-icon name="eye" size="14" color="#999"></u-icon>
                  <text>{{ item.viewCount || 0 }}次浏览</text>
                </view>
                <text class="time">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
            <image :src="item.coverImg" mode="aspectFill" class="cover"></image>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyFavorites } from '@/api/pet'
import { getMyFavoriteArticles } from '@/api/article'
import { formatTime } from '@/utils/index.js' // 假设有这个工具函数，如果没有需要自己实现或导入

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

onShow(() => {
  fetchData()
})

const handleTabChange = (item) => {
  currentTab.value = item.index
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    if (currentTab.value === 0) {
      // 萌宠
      const res = await getMyFavorites()
      if (res.data) {
        petList.value = res.data
      }
    } else {
      // 百科(type=0)、资讯(type=1)、活动(type=2)
      // Tab索引对应: 1->0, 2->1, 3->2
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
  // 根据不同的类型跳转可能不同，这里假设都跳转到通用的文章详情页，或者根据实际情况调整
  // 百科通常在 wiki 模块，资讯/活动在 cms 模块
  // 假设都有统一的详情页或根据类型分发
  // 暂时统一跳转到 cms/news 详情，或者根据现有路由调整
  // 检查现有文件结构:
  // src/pages/wiki/detail.vue (百科详情)
  // src/pages/cms/news.vue (资讯列表?) -> 需要确认详情页路由
  
  if (currentTab.value === 1) {
      uni.navigateTo({ url: `/pages/wiki/detail?id=${id}` })
  } else {
      // 假设资讯和活动共用一个详情页或者也是 wiki/detail 结构兼容
      // 如果没有专门的资讯详情页，可能需要复用 wiki/detail 或者创建新页面
      // 暂时假设用 wiki/detail 或者通用的详情页
       uni.navigateTo({ url: `/pages/wiki/detail?id=${id}` })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.tabs-box {
  background-color: #fff;
  padding: 10rpx 0;
}

.content-area {
  flex: 1;
  padding: 20rpx;
}

.pet-item {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  
  .cover {
    width: 180rpx;
    height: 180rpx;
    border-radius: 12rpx;
    margin-right: 20rpx;
  }
  
  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    
    .header {
      display: flex;
      align-items: center;
      gap: 10rpx;
      
      .name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
    }
    
    .sub-info {
      font-size: 26rpx;
      color: #666;
      
      .divider {
        margin: 0 10rpx;
        color: #eee;
      }
    }
    
    .location {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;
      
      .city {
        margin-left: 6rpx;
      }
    }
  }
}

.article-item {
  display: flex;
  justify-content: space-between;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;

  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    margin-right: 20rpx;

    .title {
      font-size: 30rpx;
      color: #333;
      font-weight: bold;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 24rpx;
      color: #999;
      margin-top: 20rpx;

      .view-count {
        display: flex;
        align-items: center;
        gap: 6rpx;
      }
    }
  }

  .cover {
    width: 200rpx;
    height: 150rpx;
    border-radius: 12rpx;
    background-color: #eee;
  }
}
</style>