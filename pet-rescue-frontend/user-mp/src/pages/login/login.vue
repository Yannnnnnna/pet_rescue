<template>
  <view class="login-page">
    <view class="logo-area">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="title">宠物救助</text>
      <text class="subtitle">{{ loginType === 'wechat' ? '微信一键登录' : '账号密码登录' }}</text>
    </view>

    <!-- 手机号登录表单 -->
    <view class="form-area" v-if="loginType === 'phone'">
      <view class="input-group">
        <u-icon name="phone" color="#999" size="24"></u-icon>
        <input 
          class="input" 
          v-model="formData.phone" 
          type="number" 
          placeholder="请输入手机号" 
          maxlength="11"
        />
      </view>
      <view class="input-group">
        <u-icon name="lock" color="#999" size="24"></u-icon>
        <input 
          class="input" 
          v-model="formData.password" 
          type="password" 
          placeholder="请输入密码" 
        />
      </view>
    </view>

    <u-button
      type="success"
      :loading="loading"
      :disabled="loading"
      shape="circle"
      :text="loginType === 'wechat' ? '微信一键登录' : '登录'"
      @click="handleLogin"
    />
    
    <view class="switch-type" @click="loginType = loginType === 'wechat' ? 'phone' : 'wechat'">
      {{ loginType === 'wechat' ? '使用手机号密码登录' : '使用微信一键登录' }}
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { miniLogin, phoneLogin } from '@/api/auth'

const loading = ref(false)
const loginType = ref('wechat') // wechat | phone

const formData = ref({
  phone: '',
  password: ''
})

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

// 登录成功处理
const handleLoginSuccess = (res) => {
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
    // 获取跳转路径
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const redirect = currentPage.$page?.options?.redirect || '/pages/index/index'
    
    // 跳转到指定页面或首页
    uni.reLaunch({
      url: decodeURIComponent(redirect)
    })
  }, 400)
}

const handleLogin = async () => {
  if (loading.value) return
  
  if (loginType.value === 'phone') {
    if (!formData.value.phone) {
      return uni.showToast({ title: '请输入手机号', icon: 'none' })
    }
    if (!formData.value.password) {
      return uni.showToast({ title: '请输入密码', icon: 'none' })
    }
  }
  
  loading.value = true
  try {
    let res
    if (loginType.value === 'wechat') {
      const code = await getWxCode()
      res = await miniLogin(code)
    } else {
      res = await phoneLogin(formData.value)
    }
    
    handleLoginSuccess(res)
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

.form-area {
  width: 80%;
  margin-bottom: 20rpx;
}

.input-group {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx 30rpx;
  border-radius: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  
  .input {
    flex: 1;
    margin-left: 20rpx;
    font-size: 28rpx;
  }
}

u-button {
  width: 80%;
}

.switch-type {
  font-size: 28rpx;
  color: #576b95;
  margin-top: 20rpx;
}
</style>
