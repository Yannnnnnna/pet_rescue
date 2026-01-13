<template>
  <view class="login-page">
    <view class="logo-area">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="title">宠物救助</text>
      <text class="subtitle">微信一键登录</text>
    </view>

    <u-button
      type="success"
      :loading="loading"
      :disabled="loading"
      shape="circle"
      text="微信一键登录"
      @click="handleLogin"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { miniLogin } from '@/api/auth'

const loading = ref(false)

const getWxCode = () => {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) {
          resolve(res.code)
        } else {
          reject(new Error('未获取到微信登录 code'))
        }
      },
      fail: (err) => reject(err)
    })
  })
}

const handleLogin = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const code = await getWxCode()

    const res = await miniLogin(code)
    const token = res?.data

    if (!token) {
      throw new Error('登录成功但未返回 token')
    }

    uni.setStorageSync('token', token)
    uni.showToast({
      title: res?.msg || '登录成功',
      icon: 'success'
    })

    setTimeout(() => {
      // 跳转到首页
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }, 400)
  } catch (error) {
    console.error('login error:', error)
    uni.showToast({
      title: error?.message || '登录失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f5fff9 0%, #e8f9f0 100%);
  gap: 60rpx;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.logo {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.08);
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1aad19;
}

.subtitle {
  font-size: 26rpx;
  color: #666;
}

u-button {
  width: 80%;
}
</style>
