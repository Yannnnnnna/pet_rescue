<template>
  <view class="container" v-if="detail">
    <!-- 状态栏 -->
    <view class="status-card" :class="'status-' + detail.status">
      <view class="status-text">{{ getStatusText(detail.status) }}</view>
      <view class="status-desc" v-if="detail.status === 2 && detail.adminRemark">驳回原因：{{ detail.adminRemark }}</view>
    </view>

    <!-- 申请人信息 -->
    <view class="card">
      <view class="card-title">申请人信息</view>
      <view class="user-info">
        <image :src="detail.userAvatar || '/static/logo.png'" class="avatar" mode="aspectFill"></image>
        <view class="info-right">
          <view class="name-row">
             <text class="name">{{ detail.realName || detail.userNickname || '用户' }}</text>
             <u-icon v-if="detail.gender === 1" name="man" color="#2979ff" size="18"></u-icon>
             <u-icon v-if="detail.gender === 2" name="woman" color="#fa3534" size="18"></u-icon>
             <text class="age" v-if="detail.age">{{ detail.age }}岁</text>
          </view>
          <view class="contact-row">
             <u-icon name="phone" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.phone }}</text>
          </view>
          <view class="address-row">
             <u-icon name="map" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.address || '未填写地址' }}</text>
          </view>
          <view class="time-row">
             <u-icon name="clock" size="16" color="#999"></u-icon>
             <text class="text">申请时间：{{ formatDate(detail.createTime) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 详细资料 -->
    <view class="card">
       <view class="card-title">详细资料</view>
       <view class="info-item">
          <view class="label">领养理由</view>
          <view class="value reason-text">{{ detail.reason || '未填写理由' }}</view>
       </view>
       <view class="info-item">
          <view class="label">养宠经验</view>
          <view class="value">{{ detail.experience || '无' }}</view>
       </view>
       <view class="info-item">
          <view class="label">住房情况</view>
          <view class="value">{{ detail.housingCondition || '无' }}</view>
       </view>
       <view class="info-item">
          <view class="label">工作状况</view>
          <view class="value">{{ detail.jobStatus || '无' }}</view>
       </view>
    </view>

    <!-- AI 匹配结果 -->
    <view class="card" v-if="detail.matchScore">
       <view class="card-title">AI 匹配分析</view>
       <view class="match-result-inline">
          <view class="score-row">
             <text class="label">匹配度：</text>
             <text class="score" :class="getScoreClass(detail.matchScore)">{{ detail.matchScore }}分</text>
             <view class="tag" v-if="detail.matchScore >= 80">完美契合</view>
             <view class="tag" v-else-if="detail.matchScore >= 60">值得考虑</view>
             <view class="tag warning" v-else>需谨慎</view>
          </view>
          <view class="analysis-content">
             <text class="text">{{ detail.analysisResult || '暂无详细分析' }}</text>
          </view>
       </view>
    </view>

    <!-- 宠物信息 -->
    <view class="card">
       <view class="card-title">申请领养的宠物</view>
       <view class="pet-info" @click="goPetDetail">
          <image :src="detail.petCover" class="pet-cover" mode="aspectFill"></image>
          <view class="pet-name">{{ detail.petName }}</view>
          <view class="arrow">
             <u-icon name="arrow-right" color="#ccc"></u-icon>
          </view>
       </view>
    </view>

    <view class="placeholder-footer" v-if="detail.status === 0"></view>
    <!-- 底部操作栏 -->
    <view class="footer-bar" v-if="detail.status === 0">
       <button class="btn cancel" @click="handleCancel">取消申请</button>
    </view>
    
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionApplicationDetail, cancelAdoptionApplication } from '@/api/adoption'
import { getMyInfo } from '@/api/user'
import { analyzeMatch } from '@/api/ai'
import dayjs from 'dayjs'

const id = ref(null)
const detail = ref(null)
const currentUserId = ref(null)
const aiLoading = ref(false)

onLoad((options) => {
    if (options.id) {
        id.value = options.id
        fetchDetail()
    }
})

const getScoreClass = (score) => {
    if (score >= 80) return 'score-high'
    if (score >= 60) return 'score-medium'
    return 'score-low'
}

onMounted(() => {
    fetchUserInfo()
})

const fetchUserInfo = async () => {
    try {
        const res = await getMyInfo()
        if (res.data) currentUserId.value = res.data.id
    } catch(e){}
}

const fetchDetail = async () => {
    uni.showLoading({ title: '加载中' })
    try {
        const res = await getAdoptionApplicationDetail(id.value)
        if (res.data) {
            detail.value = res.data
            // 获取详情后，尝试获取AI分析结果
            fetchAiResult()
        }
    } catch (e) {
        console.error(e)
    } finally {
        uni.hideLoading()
    }
}

const fetchAiResult = async () => {
    if (!detail.value) return
    
    // 如果详情中已经有了分数，就不需要再请求了
    if (detail.value.matchScore) return

    aiLoading.value = true
    try {
        const params = {
            petId: detail.value.petId,
            adoptId: detail.value.id,
            userId: detail.value.userId || 0,
            style: 'gentle', // 默认风格
            refresh: false   // 不强制刷新，获取已有结果
        }
        
        const res = await analyzeMatch(params)
        if (res.code === 200 && res.data) {
            // 将结果合并到 detail 中
            detail.value.matchScore = res.data.matchScore
            detail.value.analysisResult = res.data.analysisResult
        }
    } catch (e) {
        console.error('获取AI分析失败', e)
        // 静默失败，不提示用户
    } finally {
        aiLoading.value = false
    }
}

const formatDate = (time) => {
    if (!time) return ''
    return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status) => {
    const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
    return map[status] || '未知'
}

const goPetDetail = () => {
    if (detail.value && detail.value.petId) {
        uni.navigateTo({
            url: `/pages/pet/detail?id=${detail.value.petId}`
        })
    }
}

const handleCancel = () => {
    uni.showModal({
        title: '确认取消',
        content: '确定取消该领养申请吗？',
        success: async (res) => {
            if (res.confirm) {
                submitCancel()
            }
        }
    })
}

const submitCancel = async () => {
    uni.showLoading({ title: '提交中' })
    try {
        const res = await cancelAdoptionApplication(id.value)
        if (res.code === 200 || res.code === 0) {
            uni.showToast({ title: '取消成功', icon: 'success' })
            setTimeout(() => {
                fetchDetail()
            }, 1500)
        } else {
            uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
        }
    } catch (e) {
        console.error(e)
        uni.showToast({ title: '网络请求失败', icon: 'none' })
    } finally {
        uni.hideLoading()
    }
}
</script>

<style lang="scss" scoped>
.container {
    padding: 20rpx;
    background-color: #f5f5f5;
    min-height: 100vh;
    padding-bottom: 40rpx;
}

.status-card {
    background: #fff;
    padding: 30rpx;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    text-align: center;
    
    .status-text {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
    }
    .status-desc {
        font-size: 26rpx;
        color: #fa3534;
        margin-top: 10rpx;
    }
    
    &.status-0 .status-text { color: #ff9900; }
    &.status-1 .status-text { color: #19be6b; }
    &.status-2 .status-text { color: #fa3534; }
    &.status-3 .status-text { color: #999; }
}

.card {
    background: #fff;
    border-radius: 12rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    
    .card-title {
        font-size: 30rpx;
        font-weight: bold;
        margin-bottom: 20rpx;
        border-left: 8rpx solid #19be6b;
        padding-left: 16rpx;
    }
}

.user-info {
    display: flex;
    align-items: center;
    
    .avatar {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50%;
        margin-right: 20rpx;
    }
    
    .info-right {
        flex: 1;
        
        .name-row {
            margin-bottom: 10rpx;
            display: flex;
            align-items: center;
            
            .name { font-size: 32rpx; font-weight: bold; margin-right: 10rpx; }
            .nickname { font-size: 24rpx; color: #999; }
            .age { font-size: 24rpx; color: #666; margin-left: 10rpx; background: #f0f0f0; padding: 2rpx 8rpx; border-radius: 6rpx; }
        }
        
        .contact-row, .address-row, .time-row {
            display: flex;
            align-items: center;
            font-size: 26rpx;
            color: #666;
            margin-bottom: 8rpx;
            
            .text { margin-left: 10rpx; }
        }
        
        .time-row {
            color: #999;
            font-size: 24rpx;
            margin-top: 10rpx;
        }
    }
}

.info-item {
    margin-bottom: 20rpx;
    
    .label {
        font-size: 26rpx;
        color: #999;
        margin-bottom: 8rpx;
    }
    .value {
        font-size: 28rpx;
        color: #333;
        line-height: 1.5;
    }
}

.pet-info {
    display: flex;
    align-items: center;
    background: #f9f9f9;
    padding: 20rpx;
    border-radius: 8rpx;
    
    .pet-cover {
        width: 80rpx;
        height: 80rpx;
        border-radius: 8rpx;
        margin-right: 20rpx;
    }
    .pet-name {
        flex: 1;
        font-size: 30rpx;
        color: #333;
    }
}

.placeholder-footer {
    height: 120rpx;
}

.footer-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background: #fff;
    padding: 20rpx;
    display: flex;
    justify-content: center;
    box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
    box-sizing: border-box;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    
    .btn {
        width: 80%;
        border-radius: 40rpx;
        font-size: 30rpx;
        line-height: 80rpx;
        margin: 0;
        
        &.cancel {
            background: #fff;
            color: #666;
            border: 2rpx solid #ccc;
        }
    }
}

.reason-text {
    font-size: 30rpx;
    color: #333;
    font-weight: 500;
    line-height: 1.6;
    padding: 20rpx;
    background: #f8fcf8;
    border-radius: 8rpx;
    border-left: 6rpx solid #19be6b;
    margin-top: 10rpx;
}

/* AI 匹配结果 */
.match-result-inline {
    background: #f8fcf8;
    border-radius: 8rpx;
    padding: 20rpx;
    
    .score-row {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        
        .label {
            font-size: 28rpx;
            color: #666;
        }
        
        .score {
            font-size: 36rpx;
            font-weight: bold;
            margin: 0 16rpx 0 8rpx;
            
            &.score-high { color: #19be6b; }
            &.score-medium { color: #ff9900; }
            &.score-low { color: #fa3534; }
        }
        
        .tag {
            padding: 4rpx 16rpx;
            background: #e1f3d8;
            color: #19be6b;
            border-radius: 20rpx;
            font-size: 22rpx;
            
            &.warning {
                background: #fde2e2;
                color: #f56c6c;
            }
        }
    }
    
    .analysis-content {
        .label {
            font-size: 26rpx;
            color: #333;
            font-weight: bold;
            margin-bottom: 10rpx;
        }
        
        .text {
            font-size: 26rpx;
            color: #666;
            line-height: 1.6;
            text-align: justify;
        }
    }
}
</style>