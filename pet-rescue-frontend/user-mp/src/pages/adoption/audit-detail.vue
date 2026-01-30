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
             <text class="name">{{ detail.realName || '未填真实姓名' }}</text>
             <text class="nickname">({{ detail.userNickname }})</text>
          </view>
          <view class="contact-row">
             <u-icon name="phone" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.phone }}</text>
          </view>
          <view class="address-row">
             <u-icon name="map" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.address }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 详细资料 -->
    <view class="card">
       <view class="card-title">详细资料</view>
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
       <button class="btn reject" @click="handleReject">拒绝领养</button>
       <button class="btn approve" @click="handleApprove">同意领养</button>
    </view>
    
    <!-- 拒绝原因弹窗 -->
    <u-popup :show="showRejectPopup" @close="showRejectPopup = false" mode="center" :round="10">
        <view class="popup-box">
            <view class="popup-title">请输入拒绝原因</view>
            <textarea v-model="remark" placeholder="请输入原因..." class="remark-input"></textarea>
            <view class="popup-btns">
                <u-button size="small" @click="showRejectPopup = false" text="取消" style="margin-right: 20rpx;"></u-button>
                <u-button size="small" type="error" @click="confirmReject" text="确认拒绝"></u-button>
            </view>
        </view>
    </u-popup>
    
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionApplicationDetail, auditAdoptionApplication } from '@/api/adoption'
import { getMyInfo } from '@/api/user'

const id = ref(null)
const detail = ref(null)
const currentUserId = ref(null)
const showRejectPopup = ref(false)
const remark = ref('')

onLoad((options) => {
    if (options.id) {
        id.value = options.id
        fetchDetail()
    }
})

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
        }
    } catch (e) {
        console.error(e)
    } finally {
        uni.hideLoading()
    }
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

const handleApprove = () => {
    uni.showModal({
        title: '确认同意',
        content: '确定同意该用户的领养申请吗？',
        success: async (res) => {
            if (res.confirm) {
                submitAudit(true)
            }
        }
    })
}

const handleReject = () => {
    remark.value = ''
    showRejectPopup.value = true
}

const confirmReject = () => {
    if (!remark.value.trim()) {
        uni.showToast({ title: '请输入拒绝原因', icon: 'none' })
        return
    }
    submitAudit(false, remark.value)
}

const submitAudit = async (pass, r = '') => {
    if (!currentUserId.value) {
        await fetchUserInfo()
    }
    
    uni.showLoading({ title: '提交中' })
    try {
        await auditAdoptionApplication({
            id: id.value,
            operatorId: currentUserId.value,
            pass: pass,
            remark: r
        })
        uni.showToast({ title: '操作成功', icon: 'success' })
        showRejectPopup.value = false
        setTimeout(() => {
            fetchDetail()
        }, 1500)
    } catch (e) {
        console.error(e)
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
            .name { font-size: 32rpx; font-weight: bold; margin-right: 10rpx; }
            .nickname { font-size: 24rpx; color: #999; }
        }
        
        .contact-row, .address-row {
            display: flex;
            align-items: center;
            font-size: 26rpx;
            color: #666;
            margin-bottom: 6rpx;
            
            .text { margin-left: 10rpx; }
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
    justify-content: space-around;
    box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
    box-sizing: border-box;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    
    .btn {
        width: 45%;
        border-radius: 40rpx;
        font-size: 30rpx;
        line-height: 80rpx;
        margin: 0;
        
        &.reject {
            background: #f56c6c;
            color: #fff;
        }
        &.approve {
            background: #19be6b;
            color: #fff;
        }
    }
}

.popup-box {
    width: 600rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 12rpx;
    
    .popup-title {
        font-size: 32rpx;
        font-weight: bold;
        text-align: center;
        margin-bottom: 30rpx;
    }
    
    .remark-input {
        width: 100%;
        height: 200rpx;
        background: #f5f5f5;
        padding: 20rpx;
        border-radius: 8rpx;
        margin-bottom: 30rpx;
        box-sizing: border-box;
        font-size: 28rpx;
    }
    
    .popup-btns {
        display: flex;
        justify-content: space-between;
    }
}
</style>