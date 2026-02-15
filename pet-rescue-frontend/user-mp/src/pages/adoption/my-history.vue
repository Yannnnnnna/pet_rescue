<template>
  <view class="container">
    <u-list @scrolltolower="loadMore" v-if="dataList.length > 0">
      <u-list-item v-for="(item, index) in dataList" :key="index">
        <view class="apply-card" @click="handleItemClick(item)">
          <view class="card-header">
            <text class="time">{{ item.createTime }}</text>
            <u-tag :text="getStatusText(item.status)" :type="getStatusType(item.status)" size="mini"></u-tag>
          </view>
          
          <view class="card-body">
            <view class="info-row">
              <text class="label">申请宠物ID:</text>
              <text class="value">{{ item.petId }}</text>
            </view>
            <view class="info-row">
              <text class="label">申请人:</text>
              <text class="value">{{ item.realName }}</text>
            </view>
            <view class="info-row">
              <text class="label">联系电话:</text>
              <text class="value">{{ item.phone }}</text>
            </view>
          </view>
          
          <view class="card-footer" v-if="item.status === 2 && item.adminRemark">
            <text class="reject-reason">驳回原因: {{ item.adminRemark }}</text>
          </view>
        </view>
      </u-list-item>
    </u-list>
    <u-empty v-else mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png" text="暂无申请记录"></u-empty>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getMyAdoptionApplications } from '@/api/adoption'

const dataList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

onLoad(() => {
  fetchData()
})

onShow(() => {
  // 每次显示页面时刷新第一页数据，以获取最新状态
  pageNum.value = 1
  fetchData()
})

const handleItemClick = (item) => {
  uni.navigateTo({
    url: `/pages/adoption/application-detail?id=${item.id}`
  })
}

const fetchData = async () => {
  if (pageNum.value === 1) uni.showLoading({ title: '加载中' })
  try {
    const res = await getMyAdoptionApplications({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    
    if (res && res.data && res.data.records) {
      if (pageNum.value === 1) {
        dataList.value = res.data.records
      } else {
        dataList.value = [...dataList.value, ...res.data.records]
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载失败', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const loadMore = () => {
  if (dataList.value.length < total.value) {
    pageNum.value++
    fetchData()
  }
}

// 0-待审核 1-通过 2-驳回 3-已取消
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消', 4: '已领养' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'primary', 1: 'success', 2: 'error', 3: 'info' }
  return map[status] || 'info'
}
</script>

<style lang="scss" scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.apply-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .time {
    font-size: 24rpx;
    color: #999;
  }
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
  font-size: 28rpx;
  
  .label {
    color: #666;
    width: 160rpx;
  }
  
  .value {
    color: #333;
    flex: 1;
  }
}

.card-footer {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .reject-reason {
    font-size: 26rpx;
    color: #ff4d4f;
  }
}
</style>
