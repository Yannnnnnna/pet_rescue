<template>
  <view class="login-page">
    <view class="header-section">
      <view class="logo-wrapper">
        <image class="logo-img" src="/static/宠物救助图标.png" mode="aspectFit"></image>
      </view>
      <text class="title">宠物救助</text>
      <text class="subtitle">{{ getSubtitle }}</text>
    </view>

    <view class="form-area">
      <template v-if="loginType === 'phone'">
        <view class="input-group">
          <text class="input-icon">📱</text>
          <input 
            class="input" 
            v-model="formData.phone" 
            type="number" 
            placeholder="请输入手机号" 
            maxlength="11"
          />
        </view>
        <view class="input-group">
          <text class="input-icon">🔒</text>
          <input 
            class="input" 
            v-model="formData.password" 
            type="password" 
            placeholder="请输入密码" 
          />
        </view>
      </template>

      <template v-if="loginType === 'sms'">
        <view class="input-group">
          <text class="input-icon">📱</text>
          <input 
            class="input" 
            v-model="formData.phone" 
            type="number" 
            placeholder="请输入手机号" 
            maxlength="11"
          />
        </view>
        <view class="input-group code-group">
          <text class="input-icon">🔢</text>
          <input 
            class="input" 
            v-model="formData.code" 
            type="number" 
            placeholder="请输入验证码" 
            maxlength="6"
          />
          <button 
            class="code-btn" 
            :disabled="countdown > 0 || sendingCode"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : (sendingCode ? '发送中...' : '获取验证码') }}
          </button>
        </view>
      </template>
    </view>

    <view class="action-area">
      <button
        class="login-btn"
        :loading="loading"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ getLoginBtnText }}
      </button>
      
      <view class="login-types">
        <text 
          class="type-item" 
          :class="{ active: loginType === 'wechat' }"
          @click="loginType = 'wechat'"
        >微信登录</text>
        <text class="divider">|</text>
        <text 
          class="type-item" 
          :class="{ active: loginType === 'phone' }"
          @click="loginType = 'phone'"
        >密码登录</text>
        <text class="divider">|</text>
        <text 
          class="type-item" 
          :class="{ active: loginType === 'sms' }"
          @click="loginType = 'sms'"
        >验证码登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { miniLogin, phoneLogin, sendSmsCode, smsLogin } from '@/api/auth'

const loading = ref(false)
const sendingCode = ref(false)
const loginType = ref('wechat')
const countdown = ref(0)

const formData = ref({
  phone: '',
  password: '',
  code: ''
})

let timer = null

const getSubtitle = computed(() => {
  const map = {
    wechat: '微信一键登录',
    phone: '账号密码登录',
    sms: '手机验证码登录'
  }
  return map[loginType.value]
})

const getLoginBtnText = computed(() => {
  const map = {
    wechat: '微信一键登录',
    phone: '登录',
    sms: '登录'
  }
  return map[loginType.value]
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
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const redirect = currentPage.$page?.options?.redirect || '/pages/index/index'
    
    uni.reLaunch({
      url: decodeURIComponent(redirect)
    })
  }, 400)
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (sendingCode.value || countdown.value > 0) return
  
  if (!formData.value.phone) {
    return uni.showToast({ title: '请输入手机号', icon: 'none' })
  }
  
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  
  sendingCode.value = true
  try {
    await sendSmsCode(formData.value.phone, 1)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    uni.showToast({
      title: error?.message || '发送失败',
      icon: 'none'
    })
  } finally {
    sendingCode.value = false
  }
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
  
  if (loginType.value === 'sms') {
    if (!formData.value.phone) {
      return uni.showToast({ title: '请输入手机号', icon: 'none' })
    }
    if (!formData.value.code) {
      return uni.showToast({ title: '请输入验证码', icon: 'none' })
    }
  }
  
  loading.value = true
  try {
    let res
    if (loginType.value === 'wechat') {
      const code = await getWxCode()
      res = await miniLogin(code)
    } else if (loginType.value === 'phone') {
      res = await phoneLogin({
        phone: formData.value.phone,
        password: formData.value.password
      })
    } else if (loginType.value === 'sms') {
      res = await smsLogin({
        phone: formData.value.phone,
        code: formData.value.code
      })
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

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8;
}

.header-section {
  background: linear-gradient(180deg, #388E3C 0%, #2E7D32 100%);
  border-radius: 0 0 80rpx 80rpx;
  padding: 120rpx 40rpx 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.logo-wrapper {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.1);
}

.logo-img {
  width: 120rpx;
  height: 120rpx;
}

.title {
  font-size: 44rpx;
  font-weight: 600;
  color: #fff;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.form-area {
  padding: 80rpx 60rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.input-group {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 24rpx 30rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  
  .input-icon {
    font-size: 36rpx;
    margin-right: 16rpx;
  }
  
  .input {
    flex: 1;
    font-size: 28rpx;
  }
}

.code-group {
  .input {
    flex: 1;
  }
  
  .code-btn {
    flex-shrink: 0;
    background: #2E7D32;
    color: #fff;
    font-size: 26rpx;
    padding: 16rpx 24rpx;
    border-radius: 30rpx;
    border: none;
    margin-left: 16rpx;
    line-height: 1.4;
    
    &[disabled] {
      background: #ccc;
      color: #fff;
    }
    
    &::after {
      border: none;
    }
  }
}

.action-area {
  padding: 0 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.login-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(90deg, #388E3C, #2E7D32);
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 45rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(46, 125, 50, 0.3);
  
  &[disabled] {
    background: #ccc;
    box-shadow: none;
  }
  
  &::after {
    border: none;
  }
}

.login-types {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .type-item {
    font-size: 26rpx;
    color: #999;
    
    &.active {
      color: #2E7D32;
      font-weight: 500;
    }
  }
  
  .divider {
    color: #ddd;
    font-size: 24rpx;
  }
}
</style>
