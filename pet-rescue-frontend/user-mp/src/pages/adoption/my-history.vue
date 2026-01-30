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

    <!-- 申请详情弹窗 -->
    <u-popup :show="showDetailPopup" @close="showDetailPopup = false" mode="center" :round="10" :closeable="true">
      <view class="detail-popup">
        <view class="popup-title">申请详情</view>
        <view class="info-item" v-if="currentApply">
           <view class="row"><text class="label">申请人：</text>{{ currentApply.realName }}</view>
           <view class="row"><text class="label">电话：</text>{{ currentApply.phone }}</view>
           <view class="row"><text class="label">养宠经验：</text>{{ currentApply.experience }}</view>
           <view class="row"><text class="label">住房情况：</text>{{ currentApply.housingCondition }}</view>
           <view class="row"><text class="label">工作状况：</text>{{ currentApply.jobStatus }}</view>
           <view class="row"><text class="label">居住地址：</text>{{ currentApply.address }}</view>
           <view class="row status-row">
             <text class="label">状态：</text>
             <u-tag :text="getStatusText(currentApply.status)" :type="getStatusType(currentApply.status)" size="mini"></u-tag>
           </view>
           <view class="row reject-row" v-if="currentApply.status === 2">
             <text class="label">驳回原因：</text>
             <text class="reject-text">{{ currentApply.adminRemark }}</text>
           </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyAdoptionApplications } from '@/api/adoption'

const dataList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const showDetailPopup = ref(false)
const currentApply = ref(null)

onLoad(() => {
  fetchData()
})

const handleItemClick = (item) => {
  currentApply.value = item
  showDetailPopup.value = true
}

const fetchData = async () => {
  uni.showLoading({ title: '加载中' })
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
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
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

.detail-popup {
  width: 600rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 20rpx;
  
  .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 30rpx;
  }
  
  .info-item {
    .row {
      margin-bottom: 20rpx;
      font-size: 28rpx;
      color: #333;
      display: flex;
      
      .label {
        color: #666;
        width: 150rpx;
      }
      
      &.reject-row {
        flex-direction: column;
        .label { margin-bottom: 10rpx; }
        .reject-text { color: #ff4d4f; }
      }
    }
  }
}
</style>
