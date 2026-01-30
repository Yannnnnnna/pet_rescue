<template>
  <view class="ai-page">
    <view class="page-header">
      <text class="page-title">🤖 AI助手</text>
    </view>
    
    <view class="content">
      <view class="welcome-card">
        <text class="welcome-text">AI智能助手</text>
        <text class="welcome-desc">AI功能开发中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'

onLoad(() => {
  console.log('AI页加载')
})
</script>

<style lang="scss" scoped>
.ai-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f3ff 0%, #ede9fe 100%);
  padding: 20rpx;
  padding-bottom: 100rpx;
}

.page-header {
  padding: 40rpx 0 30rpx;
  text-align: center;
}

.page-title {
  font-size: 48rpx;
  font-weight: 600;
  color: #7c3aed;
}

.content {
  padding: 20rpx;
}

.welcome-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.welcome-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.welcome-desc {
  font-size: 28rpx;
  color: #666;
}
</style>
