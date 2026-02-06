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

    <!-- AI 悬浮球 -->
    <view class="ai-float-btn" @click="handleAnalyzeMatch">
      <view class="ai-icon-wrapper">
        <u-icon name="grid-fill" color="#fff" size="28"></u-icon>
      </view>
      <text class="ai-text">AI匹配分析</text>
    </view>
    
    <!-- AI 匹配报告弹窗 -->
    <u-popup :show="showMatchPopup" @close="showMatchPopup = false" mode="center" :round="16" :customStyle="{width: '640rpx'}">
        <view class="match-report">
            <view class="report-header">
                <text class="title">AI 匹配度报告单</text>
                <u-icon name="close" color="#999" size="20" @click="showMatchPopup = false"></u-icon>
            </view>
            
            <view class="report-content" v-if="matchLoading">
                <u-loading-icon mode="circle" size="40" color="#19be6b" vertical text="AI 正在分析匹配度..."></u-loading-icon>
            </view>
            
            <view class="report-content" v-else-if="matchResult">
                <view class="score-section">
                    <view class="score-circle" :class="getScoreClass(matchResult.matchScore)">
                        <text class="score-num">{{ matchResult.matchScore }}</text>
                        <text class="score-label">匹配分</text>
                    </view>
                    <view class="score-tags">
                        <view class="tag" v-if="matchResult.matchScore >= 80">完美契合</view>
                        <view class="tag" v-else-if="matchResult.matchScore >= 60">值得考虑</view>
                        <view class="tag warning" v-else>需谨慎</view>
                    </view>
                </view>
                
                <view class="analysis-section">
                    <view class="section-title">
                        <u-icon name="file-text-fill" color="#19be6b" size="18"></u-icon>
                        <text>分析详情</text>
                    </view>
                    <scroll-view scroll-y class="analysis-text">
                        <text>{{ matchResult.analysisResult }}</text>
                    </scroll-view>
                </view>
            </view>
            
            <view class="report-footer" v-if="!matchLoading">
                <view class="style-selector">
                    <view 
                        class="style-item" 
                        :class="{active: currentStyle === 'strict'}"
                        @click="changeStyle('strict')"
                    >犀利</view>
                    <view 
                        class="style-item" 
                        :class="{active: currentStyle === 'gentle'}"
                        @click="changeStyle('gentle')"
                    >温柔</view>
                    <view 
                        class="style-item" 
                        :class="{active: currentStyle === 'humorous'}"
                        @click="changeStyle('humorous')"
                    >幽默</view>
                </view>
                <button class="retry-btn" @click="handleAnalyzeMatch(true)">
                    <u-icon name="reload" color="#666" size="16"></u-icon>
                    <text>重新分析</text>
                </button>
            </view>
        </view>
    </u-popup>
    
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
import { analyzeMatch } from '@/api/ai'
import dayjs from 'dayjs'

const id = ref(null)
const detail = ref(null)
const currentUserId = ref(null)
const showRejectPopup = ref(false)
const remark = ref('')

// AI 匹配相关
const showMatchPopup = ref(false)
const matchLoading = ref(false)
const matchResult = ref(null)
const currentStyle = ref('gentle')

onLoad((options) => {
    if (options.id) {
        id.value = options.id
        fetchDetail()
    }
})

const handleAnalyzeMatch = async (refresh = false) => {
    // 处理点击事件传递过来的 event 对象，确保 refresh 是布尔值
    const isRefresh = typeof refresh === 'boolean' ? refresh : false
    
    if (!detail.value) return
    
    showMatchPopup.value = true
    // 如果已有结果且不是强制刷新，直接显示
    if (matchResult.value && !isRefresh) return
    
    matchLoading.value = true
    try {
        const params = {
            petId: detail.value.petId,
            adoptId: detail.value.id,
            userId: detail.value.userId || 0, // 兼容处理
            style: currentStyle.value,
            refresh: isRefresh
        }
        
        const res = await analyzeMatch(params)
        if (res.code === 200 || res.code === 0) {
            matchResult.value = res.data
        } else {
            uni.showToast({ title: res.msg || '分析失败', icon: 'none' })
        }
    } catch (e) {
        console.error(e)
        uni.showToast({ title: '网络请求失败', icon: 'none' })
    } finally {
        matchLoading.value = false
    }
}

const changeStyle = (style) => {
    if (currentStyle.value === style) return
    currentStyle.value = style
    handleAnalyzeMatch(true)
}

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
        }
    } catch (e) {
        console.error(e)
    } finally {
        uni.hideLoading()
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

/* AI 悬浮球 */
.ai-float-btn {
  position: fixed;
  right: 30rpx;
  bottom: 180rpx;
  background: linear-gradient(135deg, #19be6b 0%, #0fa858 100%);
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  box-shadow: 0 8rpx 24rpx rgba(25, 190, 107, 0.4);
  z-index: 99;
  
  .ai-text {
      color: #fff;
      font-size: 26rpx;
      font-weight: bold;
  }
}

/* 匹配报告 */
.match-report {
    padding: 30rpx;
    background: #fff;
    border-radius: 24rpx;
    
    .report-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 30rpx;
        
        .title {
            font-size: 32rpx;
            font-weight: bold;
            color: #333;
        }
    }
    
    .report-content {
        min-height: 300rpx;
        
        .score-section {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-bottom: 40rpx;
            
            .score-circle {
                width: 160rpx;
                height: 160rpx;
                border-radius: 50%;
                border: 12rpx solid #f0f0f0;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                margin-bottom: 20rpx;
                
                .score-num {
                    font-size: 80rpx;
                    font-weight: bold;
                    line-height: 1;
                }
                
                .score-label {
                    font-size: 22rpx;
                    color: #999;
                    margin-top: 4rpx;
                }
                
                &.score-high {
                    border-color: #19be6b;
                    color: #19be6b;
                }
                &.score-medium {
                    border-color: #ff9900;
                    color: #ff9900;
                }
                &.score-low {
                    border-color: #fa3534;
                    color: #fa3534;
                }
            }
            
            .score-tags {
                display: flex;
                gap: 20rpx;
                
                .tag {
                    padding: 6rpx 20rpx;
                    background: #e1f3d8;
                    color: #19be6b;
                    border-radius: 30rpx;
                    font-size: 24rpx;
                    
                    &.warning {
                        background: #fde2e2;
                        color: #f56c6c;
                    }
                }
            }
        }
        
        .analysis-section {
            background: #f8f9fa;
            border-radius: 12rpx;
            padding: 20rpx;
            
            .section-title {
                display: flex;
                align-items: center;
                gap: 10rpx;
                margin-bottom: 16rpx;
                font-size: 28rpx;
                font-weight: bold;
                color: #333;
            }
            
            .analysis-text {
                height: 300rpx;
                font-size: 26rpx;
                color: #666;
                line-height: 1.6;
                text-align: justify;
            }
        }
    }
    
    .report-footer {
        margin-top: 30rpx;
        border-top: 1rpx solid #eee;
        padding-top: 30rpx;
        
        .style-selector {
            display: flex;
            justify-content: center;
            gap: 20rpx;
            margin-bottom: 20rpx;
            
            .style-item {
                padding: 10rpx 24rpx;
                border-radius: 30rpx;
                background: #f5f5f5;
                color: #666;
                font-size: 24rpx;
                
                &.active {
                    background: #19be6b;
                    color: #fff;
                }
            }
        }
        
        .retry-btn {
            background: transparent;
            color: #666;
            font-size: 26rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10rpx;
            
            &::after { border: none; }
        }
    }
}
</style>