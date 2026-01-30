<template>
  <view class="container">
    <view class="list-container">
      <view class="list-item" v-for="item in list" :key="item.id" @click="handleDetail(item.id)">
        <view class="header">
           <text class="time">申请时间：{{ formatTime(item.createTime) }}</text>
           <u-tag :text="getStatusText(item.status)" :type="getStatusType(item.status)" size="mini"></u-tag>
        </view>
        <view class="content">
            <image :src="item.petCover" mode="aspectFill" class="cover"></image>
            <view class="info">
                <view class="row">
                    <text class="label">申请宠物：</text>
                    <text class="value">{{ item.petName }}</text>
                </view>
                <view class="row">
                    <text class="label">申请人：</text>
                    <text class="value">{{ item.realName }}</text>
                </view>
                 <view class="row">
                    <text class="label">性别：</text>
                    <text class="value">{{ getSexText(item.userSex) }}</text>
                </view>
            </view>
        </view>
      </view>
      <u-empty v-if="!loading && list.length === 0" mode="data" text="暂无待审核申请"></u-empty>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyReceivedApplications } from '@/api/adoption'
import dayjs from 'dayjs'

const list = ref([])
const loading = ref(false)

onShow(() => {
    fetchList()
})

const fetchList = async () => {
    loading.value = true
    try {
        const res = await getMyReceivedApplications()
        if (res.data) {
            list.value = res.data
        }
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const getStatusText = (status) => {
    const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
    return map[status] || '未知'
}

const getStatusType = (status) => {
    const map = { 0: 'warning', 1: 'success', 2: 'error', 3: 'info' }
    return map[status] || 'info'
}

const getSexText = (sex) => {
    const map = { 0: '未知', 1: '男', 2: '女' }
    return map[sex] || '未知'
}

const formatTime = (time) => {
    return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : ''
}

const handleDetail = (id) => {
    uni.navigateTo({
        url: `/pages/adoption/audit-detail?id=${id}`
    })
}
</script>

<style lang="scss" scoped>
.container {
    padding: 20rpx;
    background-color: #f5f5f5;
    min-height: 100vh;
}
.list-item {
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    
    .header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20rpx;
        font-size: 24rpx;
        color: #999;
        border-bottom: 1rpx solid #f0f0f0;
        padding-bottom: 10rpx;
    }
    
    .content {
        display: flex;
        
        .cover {
            width: 140rpx;
            height: 140rpx;
            border-radius: 8rpx;
            margin-right: 20rpx;
            background-color: #eee;
        }
        
        .info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-around;
            
            .row {
                font-size: 28rpx;
                color: #333;
                display: flex;
                align-items: center;
                margin-bottom: 6rpx;
                
                .label {
                    color: #666;
                    width: 150rpx;
                }
                .value {
                    font-weight: bold;
                    flex: 1;
                }
            }
        }
    }
}
</style>