<template>
  <view class="container" v-if="detail">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">申请详情</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <!-- 状态栏 -->
    <view class="status-card" :class="'status-' + detail.status" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
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
             <u-icon v-if="detail.gender === 1" name="nan" custom-prefix="custom-icon" color="#2979ff" size="18"></u-icon>
             <u-icon v-if="detail.gender === 2" name="nv" custom-prefix="custom-icon" color="#fa3534" size="18"></u-icon>
             <text class="age" v-if="detail.age">{{ detail.age }}岁</text>
          </view>
          <view class="contact-row">
             <u-icon name="dianhua" custom-prefix="custom-icon" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.phone }}</text>
          </view>
          <view class="address-row">
             <u-icon name="dizhi" custom-prefix="custom-icon" size="16" color="#666"></u-icon>
             <text class="text">{{ detail.address || '未填写地址' }}</text>
          </view>
          <view class="time-row">
             <u-icon name="lishi" custom-prefix="custom-icon" size="16" color="#999"></u-icon>
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
    <view class="card match-card" v-if="detail.matchScore">
       <view class="match-header">
          <view class="match-title">
             <uni-icons type="fire" size="18" color="#2E7D32"></uni-icons>
             <text class="title-text">AI 匹配分析</text>
          </view>
          <view class="match-badge">
             <uni-icons type="checkbox-filled" size="12" color="#fff"></uni-icons>
             <text class="badge-text">{{ detail.matchScore }}% 匹配</text>
          </view>
       </view>
       
       <!-- 五边形雷达图 -->
       <view class="radar-container">
          <canvas 
            canvas-id="radarCanvas" 
            id="radarCanvas"
            class="radar-canvas"
          ></canvas>
          <view class="radar-labels">
             <text class="radar-label label-top">生活方式</text>
             <text class="radar-label label-right">精力程度</text>
             <text class="radar-label label-bottom-right">居住空间</text>
             <text class="radar-label label-bottom-left">养宠经验</text>
             <text class="radar-label label-left">预算规划</text>
          </view>
          <view class="radar-center">
             <text class="center-score">{{ detail.matchScore }}</text>
             <text class="center-unit">%</text>
          </view>
       </view>
       
       <!-- 详细指标 -->
       <view class="match-metrics">
          <view class="metric-item">
             <view class="metric-icon lifestyle">
                <uni-icons type="home" size="14" color="#2E7D32"></uni-icons>
             </view>
             <view class="metric-content">
                <view class="metric-header">
                   <text class="metric-label">生活环境</text>
                   <text class="metric-value" :class="getMetricClass(detail.matchScore)">{{ getMetricText(detail.matchScore, 'lifestyle') }}</text>
                </view>
                <view class="metric-bar">
                   <view class="metric-progress" :style="{ width: getMetricWidth(detail.matchScore, 'lifestyle') }" :class="getMetricClass(detail.matchScore)"></view>
                </view>
             </view>
          </view>
          <view class="metric-item">
             <view class="metric-icon schedule">
                <uni-icons type="calendar" size="14" color="#FFC107"></uni-icons>
             </view>
             <view class="metric-content">
                <view class="metric-header">
                   <text class="metric-label">作息匹配</text>
                   <text class="metric-value schedule">{{ getMetricText(detail.matchScore, 'schedule') }}</text>
                </view>
                <view class="metric-bar">
                   <view class="metric-progress schedule" :style="{ width: getMetricWidth(detail.matchScore, 'schedule') }"></view>
                </view>
             </view>
          </view>
       </view>
       
       <!-- 分析结果 -->
       <view class="analysis-content">
          <text class="analysis-text">{{ detail.analysisResult || '暂无详细分析' }}</text>
       </view>
    </view>

    <!-- 宠物信息 -->
    <view class="card">
       <view class="card-title">申请领养的宠物</view>
       <view class="pet-info" @click="goPetDetail">
          <image :src="detail.petCover" class="pet-cover" mode="aspectFill"></image>
          <view class="pet-name">{{ detail.petName }}</view>
          <view class="arrow">
             <u-icon name="gengduo" custom-prefix="custom-icon" color="#ccc"></u-icon>
          </view>
       </view>
    </view>

    <!-- 协议签署状态 -->
    <view class="card" v-if="detail.status === 1">
       <view class="card-title">领养协议</view>
       <view class="agreement-status">
          <view class="status-row">
             <u-icon :name="detail.agreementStatus === 1 ? 'duigou' : 'tishi'" 
                     custom-prefix="custom-icon"
                     :color="detail.agreementStatus === 1 ? '#19be6b' : '#ff9900'" size="24"></u-icon>
             <text class="status-text" :class="{ signed: detail.agreementStatus === 1 }">
               {{ detail.agreementStatus === 1 ? '已签署' : '待签署' }}
             </text>
          </view>
          <view class="sign-time" v-if="detail.agreementStatus === 1 && detail.signTime">
             <text>签署时间：{{ formatDate(detail.signTime) }}</text>
          </view>
          <view class="signature-preview" v-if="detail.agreementStatus === 1 && detail.signatureImg">
             <image :src="detail.signatureImg" mode="aspectFit" class="signature-img" @click="previewSignature"></image>
          </view>
       </view>
    </view>

    <view class="placeholder-footer" v-if="detail.status === 0 || detail.status === 1 || detail.status === 4"></view>
    <!-- 底部操作栏 -->
    <view class="footer-bar" v-if="detail.status === 0">
       <button class="btn cancel" @click="handleCancel">取消申请</button>
    </view>
    
    <!-- 已通过或已领养状态 -->
    <view class="footer-bar" v-if="(detail.status === 1 || detail.status === 4 || detail.status === '1' || detail.status === '4') && detail.agreementStatus === 1">
       <button class="btn outline" @click="handleViewAgreement">查看领养协议</button>
    </view>
    <view class="footer-bar" v-else-if="(detail.status === 1 || detail.status === '1') && detail.agreementStatus !== 1">
       <button class="btn primary" @click="handleSignAgreement">签署领养协议</button>
    </view>
    
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAdoptionApplicationDetail, cancelAdoptionApplication } from '@/api/adoption'
import { getMyInfo } from '@/api/user'
import { analyzeMatch } from '@/api/ai'
import dayjs from 'dayjs'

const statusBarHeight = ref(44)
const id = ref(null)
const detail = ref(null)
const currentUserId = ref(null)
const aiLoading = ref(false)

onLoad((options) => {
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight || 44
    
    if (options.id) {
        id.value = options.id
        fetchDetail()
    }
})

watch(() => detail.value?.matchScore, (newVal) => {
    if (newVal) {
        nextTick(() => {
            drawRadarChart(newVal)
        })
    }
})

const drawRadarChart = (score) => {
    const ctx = uni.createCanvasContext('radarCanvas')
    const canvasSize = 160
    const centerX = canvasSize / 2
    const centerY = canvasSize / 2
    const maxRadius = 60
    
    ctx.clearRect(0, 0, canvasSize, canvasSize)
    
    const sides = 5
    const angles = []
    for (let i = 0; i < sides; i++) {
        angles.push((Math.PI * 2 * i / sides) - Math.PI / 2)
    }
    
    ctx.setStrokeStyle('#E5E7EB')
    ctx.setLineWidth(1)
    
    const outerRadius = maxRadius
    ctx.beginPath()
    for (let i = 0; i < sides; i++) {
        const x = centerX + outerRadius * Math.cos(angles[i])
        const y = centerY + outerRadius * Math.sin(angles[i])
        if (i === 0) {
            ctx.moveTo(x, y)
        } else {
            ctx.lineTo(x, y)
        }
    }
    ctx.closePath()
    ctx.stroke()
    
    const innerRadius = maxRadius * 0.5
    ctx.beginPath()
    for (let i = 0; i < sides; i++) {
        const x = centerX + innerRadius * Math.cos(angles[i])
        const y = centerY + innerRadius * Math.sin(angles[i])
        if (i === 0) {
            ctx.moveTo(x, y)
        } else {
            ctx.lineTo(x, y)
        }
    }
    ctx.closePath()
    ctx.stroke()
    
    const scoreRadius = maxRadius * (score / 100)
    ctx.beginPath()
    ctx.setFillStyle('rgba(46, 125, 50, 0.7)')
    ctx.setStrokeStyle('#2E7D32')
    ctx.setLineWidth(2)
    for (let i = 0; i < sides; i++) {
        const x = centerX + scoreRadius * Math.cos(angles[i])
        const y = centerY + scoreRadius * Math.sin(angles[i])
        if (i === 0) {
            ctx.moveTo(x, y)
        } else {
            ctx.lineTo(x, y)
        }
    }
    ctx.closePath()
    ctx.fill()
    ctx.stroke()
    
    ctx.draw()
}

const goBack = () => {
    uni.navigateBack()
}

const getScoreClass = (score) => {
    if (score >= 80) return 'score-high'
    if (score >= 60) return 'score-medium'
    return 'score-low'
}

const getMetricClass = (score) => {
    if (score >= 80) return 'excellent'
    if (score >= 60) return 'good'
    return 'fair'
}

const getMetricText = (score, type) => {
    if (type === 'lifestyle') {
        if (score >= 80) return '完美'
        if (score >= 60) return '良好'
        return '一般'
    }
    if (type === 'schedule') {
        if (score >= 80) return '良好'
        if (score >= 60) return '适中'
        return '需调整'
    }
    return '一般'
}

const getMetricWidth = (score, type) => {
    if (type === 'lifestyle') {
        return Math.min(95, Math.max(50, score)) + '%'
    }
    if (type === 'schedule') {
        return Math.min(80, Math.max(40, score * 0.8)) + '%'
    }
    return '50%'
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
    if (detail.value.matchScore) return

    aiLoading.value = true
    try {
        const params = {
            petId: detail.value.petId,
            adoptId: detail.value.id,
            userId: detail.value.userId || 0,
            style: 'gentle',
            refresh: false
        }
        
        const res = await analyzeMatch(params)
        if (res.code === 200 && res.data) {
            detail.value.matchScore = res.data.matchScore
            detail.value.analysisResult = res.data.analysisResult
        }
    } catch (e) {
        console.error('获取AI分析失败', e)
    } finally {
        aiLoading.value = false
    }
}

const formatDate = (time) => {
    if (!time) return ''
    return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getStatusText = (status) => {
    const map = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消', 4: '已领养' }
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

const handleSignAgreement = () => {
    uni.navigateTo({
        url: `/pages/adoption/sign-agreement?applicationId=${id.value}&petId=${detail.value.petId}`
    })
}

const handleViewAgreement = () => {
    uni.navigateTo({
        url: `/pages/adoption/sign-agreement?applicationId=${id.value}&petId=${detail.value.petId}&mode=view`
    })
}

const previewSignature = () => {
    if (detail.value && detail.value.signatureImg) {
        uni.previewImage({
            urls: [detail.value.signatureImg],
            current: detail.value.signatureImg
        })
    }
}
</script>

<style lang="scss" scoped>
.container {
    background-color: #F9FAFB;
    min-height: 100vh;
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

.status-card {
    background: #fff;
    padding: 30rpx;
    border-radius: 16rpx;
    margin-bottom: 20rpx;
    margin-left: 24rpx;
    margin-right: 24rpx;
    text-align: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
    
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
    
    &.status-0 .status-text { color: #FFC107; }
    &.status-1 .status-text { color: #2E7D32; }
    &.status-2 .status-text { color: #fa3534; }
    &.status-3 .status-text { color: #999; }
}

.card {
    background: #fff;
    border-radius: 16rpx;
    padding: 28rpx;
    margin-bottom: 20rpx;
    margin-left: 24rpx;
    margin-right: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
    
    .card-title {
        font-size: 30rpx;
        font-weight: bold;
        margin-bottom: 20rpx;
        color: #1F2937;
        display: flex;
        align-items: center;
        
        &::before {
            content: '';
            width: 6rpx;
            height: 28rpx;
            background: #2E7D32;
            border-radius: 3rpx;
            margin-right: 12rpx;
        }
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
        border: 4rpx solid #E8F5E9;
    }
    
    .info-right {
        flex: 1;
        
        .name-row {
            margin-bottom: 10rpx;
            display: flex;
            align-items: center;
            
            .name { font-size: 32rpx; font-weight: 600; margin-right: 10rpx; color: #1F2937; }
            .nickname { font-size: 24rpx; color: #999; }
            .age { font-size: 24rpx; color: #6B7280; margin-left: 10rpx; background: #F3F4F6; padding: 2rpx 12rpx; border-radius: 8rpx; }
        }
        
        .contact-row, .address-row, .time-row {
            display: flex;
            align-items: center;
            font-size: 26rpx;
            color: #6B7280;
            margin-bottom: 8rpx;
            
            .text { margin-left: 10rpx; }
        }
        
        .time-row {
            color: #9CA3AF;
            font-size: 24rpx;
            margin-top: 10rpx;
        }
    }
}

.info-item {
    margin-bottom: 20rpx;
    
    .label {
        font-size: 26rpx;
        color: #6B7280;
        margin-bottom: 8rpx;
    }
    .value {
        font-size: 28rpx;
        color: #1F2937;
        line-height: 1.5;
    }
}

.pet-info {
    display: flex;
    align-items: center;
    background: #F9FAFB;
    padding: 20rpx;
    border-radius: 12rpx;
    
    .pet-cover {
        width: 80rpx;
        height: 80rpx;
        border-radius: 12rpx;
        margin-right: 20rpx;
    }
    .pet-name {
        flex: 1;
        font-size: 30rpx;
        color: #1F2937;
        font-weight: 500;
    }
}

.placeholder-footer {
    height: 140rpx;
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
    box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.04);
    box-sizing: border-box;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    z-index: 100;
    
    .btn {
        width: 80%;
        border-radius: 24rpx;
        font-size: 30rpx;
        line-height: 88rpx;
        margin: 0;
        font-weight: 600;
        
        &.cancel {
            background: #FF5A5F;
            color: #fff;
            border: none;
        }
        
        &.primary {
            background: #2E7D32;
            color: #fff;
            border: none;
            box-shadow: 0 8rpx 24rpx rgba(46, 125, 50, 0.3);
        }
        
        &.outline {
            background: #fff;
            color: #2E7D32;
            border: 2rpx solid #2E7D32;
        }
    }
}

.agreement-status {
    .status-row {
        display: flex;
        align-items: center;
        
        .status-text {
            font-size: 28rpx;
            color: #FFC107;
            margin-left: 10rpx;
            font-weight: 500;
            
            &.signed {
                color: #2E7D32;
            }
        }
    }
    
    .sign-time {
        margin-top: 16rpx;
        
        text {
            font-size: 24rpx;
            color: #9CA3AF;
        }
    }
    
    .signature-preview {
        margin-top: 20rpx;
        padding: 20rpx;
        background: #F9FAFB;
        border-radius: 12rpx;
        text-align: center;
        
        .signature-img {
            width: 300rpx;
            height: 150rpx;
        }
    }
}

.reason-text {
    font-size: 28rpx;
    color: #1F2937;
    font-weight: 500;
    line-height: 1.6;
    padding: 20rpx;
    background: #E8F5E9;
    border-radius: 12rpx;
    margin-top: 10rpx;
}

/* AI 匹配结果卡片 */
.match-card {
    position: relative;
    overflow: hidden;
    
    &::before {
        content: '';
        position: absolute;
        top: -60rpx;
        right: -60rpx;
        width: 200rpx;
        height: 200rpx;
        background: rgba(46, 125, 50, 0.05);
        border-radius: 50%;
        filter: blur(40rpx);
    }
    
    &::after {
        content: '';
        position: absolute;
        bottom: -60rpx;
        left: -60rpx;
        width: 160rpx;
        height: 160rpx;
        background: rgba(255, 193, 7, 0.1);
        border-radius: 50%;
        filter: blur(40rpx);
    }
}

.match-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
    position: relative;
    z-index: 1;
    
    .match-title {
        display: flex;
        align-items: center;
        
        .title-text {
            font-size: 30rpx;
            font-weight: bold;
            color: #1F2937;
            margin-left: 8rpx;
        }
    }
    
    .match-badge {
        display: flex;
        align-items: center;
        background: #2E7D32;
        padding: 8rpx 16rpx;
        border-radius: 24rpx;
        box-shadow: 0 4rpx 12rpx rgba(46, 125, 50, 0.3);
        
        .badge-text {
            font-size: 24rpx;
            font-weight: bold;
            color: #fff;
            margin-left: 6rpx;
        }
    }
}

/* 五边形雷达图 */
.radar-container {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 24rpx;
    position: relative;
    z-index: 1;
    height: 320rpx;
}

.radar-canvas {
    width: 160px;
    height: 160px;
}

.radar-labels {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
}

.radar-label {
    position: absolute;
    font-size: 20rpx;
    color: #6B7280;
    font-weight: 600;
    white-space: nowrap;
    
    &.label-top {
        top: 0;
        left: 50%;
        transform: translateX(-50%);
    }
    
    &.label-right {
        top: 50%;
        right: 0;
        transform: translateY(-50%);
    }
    
    &.label-bottom-right {
        bottom: 40rpx;
        right: 20rpx;
    }
    
    &.label-bottom-left {
        bottom: 40rpx;
        left: 20rpx;
    }
    
    &.label-left {
        top: 50%;
        left: 0;
        transform: translateY(-50%);
    }
}

.radar-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 72rpx;
    height: 72rpx;
    background: #fff;
    border: 4rpx solid #2E7D32;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: row;
    z-index: 2;
    
    .center-score {
        font-size: 28rpx;
        font-weight: bold;
        color: #2E7D32;
    }
    
    .center-unit {
        font-size: 16rpx;
        color: #2E7D32;
        margin-left: 2rpx;
    }
}

/* 匹配指标 */
.match-metrics {
    border-top: 1rpx solid #F3F4F6;
    padding-top: 20rpx;
    margin-bottom: 20rpx;
    position: relative;
    z-index: 1;
}

.metric-item {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
    
    .metric-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;
        
        &.lifestyle {
            background: #E8F5E9;
        }
        
        &.schedule {
            background: #FFFDE7;
        }
    }
    
    .metric-content {
        flex: 1;
    }
    
    .metric-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8rpx;
        
        .metric-label {
            font-size: 24rpx;
            font-weight: 500;
            color: #374151;
        }
        
        .metric-value {
            font-size: 24rpx;
            font-weight: bold;
            
            &.excellent {
                color: #2E7D32;
            }
            
            &.good {
                color: #2E7D32;
            }
            
            &.fair {
                color: #6B7280;
            }
            
            &.schedule {
                color: #FFC107;
            }
        }
    }
    
    .metric-bar {
        width: 100%;
        height: 10rpx;
        background: #F3F4F6;
        border-radius: 8rpx;
        overflow: hidden;
        
        .metric-progress {
            height: 100%;
            border-radius: 8rpx;
            transition: width 0.5s ease;
            
            &.excellent {
                background: #2E7D32;
            }
            
            &.good {
                background: #2E7D32;
            }
            
            &.fair {
                background: #9CA3AF;
            }
            
            &.schedule {
                background: #FFC107;
            }
        }
    }
}

.analysis-content {
    position: relative;
    z-index: 1;
    
    .analysis-text {
        font-size: 26rpx;
        color: #4B5563;
        line-height: 1.6;
    }
}
</style>
