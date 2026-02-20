<template>
  <view class="profile-page">
    <scroll-view 
      class="scroll-container" 
      scroll-y 
      :scroll-top="scrollTop"
      @scroll="onScroll"
    >
      <view class="header-section">
        <view class="header-pattern"></view>
        <view class="header-decor decor-1"></view>
        <view class="header-decor decor-2"></view>
        
        <view class="user-info-area">
          <view class="avatar-box" @click.stop="handleAvatarClick">
            <image 
              class="avatar" 
              :src="userInfo.avatar || '/static/logo.png'" 
              mode="aspectFill"
            />
            <view class="avatar-edit-btn">
              <uni-icons type="camera-filled" size="14" color="#2E7D32"></uni-icons>
            </view>
          </view>
          
          <view class="user-name">{{ userInfo.nickname || '登录/注册' }}</view>
          <view class="user-tags">
            <view class="tag-item primary" v-if="userInfo.nickname">
              <uni-icons type="checkbox-filled" size="14" color="#FFD600"></uni-icons>
              <text>爱心领养人</text>
            </view>
            <view class="tag-item plain">
              <text>{{ joinYear }}年加入</text>
            </view>
          </view>
        </view>
      </view>

      <view class="content-area">
      <view class="stats-card">
        <view class="stat-item" @click="handleMyPets">
          <text class="stat-num">{{ publishedCount }}</text>
          <text class="stat-label">我的发布</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item highlight" @click="handleMyAdoptedPets">
          <text class="stat-num">{{ adoptedCount }}</text>
          <text class="stat-label">已领养</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ userInfo.coin || 0 }}</text>
          <text class="stat-label">小鱼干</text>
        </view>
      </view>

      <view class="section-block">
        <view class="section-header">
          <view class="section-indicator"></view>
          <text class="section-title">领养中心</text>
          <text class="section-tag">我是领养人</text>
        </view>
        
        <view class="service-grid">
          <view class="service-item" @click="handleApplyHistory">
            <view class="service-icon orange">
              <u-icon name="shenqing" custom-prefix="custom-icon" size="22" color="#E65100"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">领养申请</text>
              <text class="service-desc">查看历史记录</text>
            </view>
          </view>
          
          <view class="service-item" @click="handleMyAdoptedPets">
            <view class="service-icon blue">
              <u-icon name="wodechongwu" custom-prefix="custom-icon" size="22" color="#1976D2"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">我的宠物</text>
              <text class="service-desc">已领养的宝贝</text>
            </view>
          </view>
          
          <view class="service-item" @click="handleMyChats">
            <view class="service-icon green">
              <u-icon name="paw" custom-prefix="custom-icon" size="22" color="#2E7D32"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">咨询足迹</text>
              <text class="service-desc">聊天记录</text>
            </view>
          </view>
          
          <view class="service-item" @click="handleMyFavorites">
            <view class="service-icon purple">
              <u-icon name="shoucang" custom-prefix="custom-icon" size="22" color="#7B1FA2"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">我的收藏</text>
              <text class="service-desc">收藏的萌宠</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-block">
        <view class="section-header">
          <view class="section-indicator orange"></view>
          <text class="section-title">送养管理</text>
          <text class="section-tag orange">我是送养人</text>
        </view>
        
        <view class="service-grid two-col">
          <view class="service-item" @click="handleMyPets">
            <view class="service-icon cyan">
              <u-icon name="wofabude" custom-prefix="custom-icon" size="22" color="#00838F"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">我发布的</text>
              <text class="service-desc">送养信息</text>
            </view>
          </view>
          
          <view class="service-item" @click="handleTodoAudit">
            <view class="service-icon pink">
              <u-icon name="daibanshenhe" custom-prefix="custom-icon" size="22" color="#C2185B"></u-icon>
            </view>
            <view class="service-info">
              <text class="service-name">待办审核</text>
              <text class="service-desc">处理申请</text>
            </view>
          </view>
        </view>
      </view>

      <view class="achievement-card" v-if="adoptedCount > 0">
        <view class="achievement-icon">
          <uni-icons type="medal-filled" size="28" color="#2E7D32"></uni-icons>
        </view>
        <view class="achievement-content">
          <text class="achievement-title">您的公益足迹</text>
          <text class="achievement-desc">您已帮助 <text class="highlight-num">{{ adoptedCount }}</text> 只宠物找到新家！</text>
        </view>
        <view class="achievement-arrow">
          <uni-icons type="right" size="18" color="#E65100"></uni-icons>
        </view>
        <view class="achievement-bg-icon">
          <uni-icons type="heart-filled" size="80" color="#FFD600"></uni-icons>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-item" @click="handleBindPhone">
          <view class="menu-icon">
            <uni-icons type="phone-filled" size="20" color="#909399"></uni-icons>
          </view>
          <text class="menu-text">{{ userInfo.phone ? '修改手机号' : '绑定手机号' }}</text>
          <view class="menu-right">
            <text class="menu-value">{{ userInfo.phone || '未绑定' }}</text>
            <uni-icons type="right" size="16" color="#c0c4cc"></uni-icons>
          </view>
        </view>
        
        <view class="menu-item" @click="handleSettings">
          <view class="menu-icon">
            <u-icon name="shezhi" custom-prefix="custom-icon" size="20" color="#909399"></u-icon>
          </view>
          <text class="menu-text">账号设置</text>
          <uni-icons type="right" size="16" color="#c0c4cc"></uni-icons>
        </view>
        
        <view class="menu-item" @click="handleAbout">
          <view class="menu-icon">
            <u-icon name="guanyu" custom-prefix="custom-icon" size="20" color="#909399"></u-icon>
          </view>
          <text class="menu-text">关于我们</text>
          <uni-icons type="right" size="16" color="#c0c4cc"></uni-icons>
        </view>
        
        <view class="menu-item logout" @click="handleLogout">
          <view class="menu-icon">
            <u-icon name="tuichu" custom-prefix="custom-icon" size="20" color="#fa3534"></u-icon>
          </view>
          <text class="menu-text" style="color: #fa3534;">退出登录</text>
          <uni-icons type="right" size="16" color="#c0c4cc"></uni-icons>
        </view>
      </view>

        <view class="version-info">
          <text>Version 1.0.0 · Made with love for pets</text>
        </view>
      </view>
    </scroll-view>

    <u-popup :show="showEditPopup" @close="showEditPopup = false" mode="center" :round="20">
      <view class="edit-popup">
        <view class="popup-title">编辑资料</view>
        <view class="form-item">
          <text class="form-label">昵称</text>
          <input class="form-input" v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20"/>
        </view>
        <view class="popup-buttons">
          <button class="popup-btn cancel" @click="showEditPopup = false">取消</button>
          <button class="popup-btn confirm" @click="handleSaveEdit">保存</button>
        </view>
      </view>
    </u-popup>

    <u-popup :show="showPhonePopup" @close="closePhonePopup" mode="center" :round="20">
      <view class="edit-popup phone-popup">
        <view class="popup-title">{{ userInfo.phone ? '修改手机号' : '绑定手机号' }}</view>
        <view class="form-item">
          <text class="form-label">新手机号</text>
          <input class="form-input" v-model="phoneForm.phone" placeholder="请输入新手机号" type="number" maxlength="11"/>
        </view>
        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="code-input-row">
            <input class="form-input code-input" v-model="phoneForm.code" placeholder="请输入验证码" type="number" maxlength="6"/>
            <button 
              class="code-btn" 
              :disabled="phoneCountdown > 0 || sendingPhoneCode"
              @click="handleSendPhoneCode"
            >
              {{ phoneCountdown > 0 ? `${phoneCountdown}s` : (sendingPhoneCode ? '发送中...' : '获取验证码') }}
            </button>
          </view>
        </view>
        <view class="popup-buttons">
          <button class="popup-btn cancel" @click="closePhonePopup">取消</button>
          <button class="popup-btn confirm" @click="handleSavePhone">确认</button>
        </view>
      </view>
    </u-popup>

    <u-popup :show="showPasswordPopup" @close="showPasswordPopup = false" mode="center" :round="20">
      <view class="edit-popup">
        <view class="popup-title">{{ userInfo.hasPassword ? '修改密码' : '设置密码' }}</view>
        <view class="form-item" v-if="userInfo.hasPassword">
          <text class="form-label">旧密码</text>
          <input class="form-input" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" password/>
        </view>
        <view class="form-item">
          <text class="form-label">新密码</text>
          <input class="form-input" v-model="passwordForm.newPassword" placeholder="请输入新密码" password/>
        </view>
        <view class="form-item">
          <text class="form-label">确认密码</text>
          <input class="form-input" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" password/>
        </view>
        <view class="popup-buttons">
          <button class="popup-btn cancel" @click="showPasswordPopup = false">取消</button>
          <button class="popup-btn confirm" @click="handleSavePassword">确认</button>
        </view>
      </view>
    </u-popup>
    <my-tabbar :current="3"></my-tabbar>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyInfo, updateUserInfo, updatePassword, uploadImage } from '@/api/user'
import { sendSmsCode, logout } from '@/api/auth'
import { checkLogin } from '@/utils/auth'

const userInfo = ref({
  nickname: '',
  avatar: '',
  phone: '',
  coin: 0,
  hasPassword: false,
  createTime: ''
})

const joinYear = computed(() => {
  if (userInfo.value.createTime) {
    return new Date(userInfo.value.createTime).getFullYear()
  }
  return new Date().getFullYear()
})

const publishedCount = ref(0)
const adoptedCount = ref(0)

const editForm = ref({
  nickname: '',
  avatar: ''
})

const phoneForm = ref({
  phone: '',
  code: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showEditPopup = ref(false)
const showPhonePopup = ref(false)
const showPasswordPopup = ref(false)

const scrollTop = ref(0)
const onScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
}

const phoneCountdown = ref(0)
const sendingPhoneCode = ref(false)
let phoneTimer = null

const handleChangePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showPasswordPopup.value = true
}

const handleSavePassword = async () => {
  if (userInfo.value.hasPassword && !passwordForm.value.oldPassword) {
    uni.showToast({ title: '请输入旧密码', icon: 'none' })
    return
  }
  
  if (!passwordForm.value.newPassword) {
    uni.showToast({ title: '请输入新密码', icon: 'none' })
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: userInfo.value.hasPassword ? '修改中...' : '设置中...' })
    await updatePassword({
      oldPassword: userInfo.value.hasPassword ? passwordForm.value.oldPassword : '',
      newPassword: passwordForm.value.newPassword
    })
    showPasswordPopup.value = false
    uni.showToast({
      title: userInfo.value.hasPassword ? '密码修改成功' : '密码设置成功',
      icon: 'success'
    })
    userInfo.value.hasPassword = true
    loadUserInfo()
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    uni.hideLoading()
  }
}

const loadUserInfo = async () => {
  try {
    const res = await getMyInfo()
    if (res.data) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uni.showLoading({ title: '上传中...' })
      try {
        const imageUrl = await uploadImage(tempFilePath)
        await updateUserInfo({ avatar: imageUrl })
        userInfo.value.avatar = imageUrl
        uni.showToast({ title: '头像更新成功', icon: 'success' })
      } catch (error) {
        console.error('上传头像失败:', error)
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const handleEditInfo = () => {
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    avatar: userInfo.value.avatar || ''
  }
  showEditPopup.value = true
}

const handleSaveEdit = async () => {
  if (!editForm.value.nickname) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    await updateUserInfo(editForm.value)
    showEditPopup.value = false
    uni.showToast({ title: '保存成功', icon: 'success' })
    await loadUserInfo()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    uni.hideLoading()
  }
}

const handleBindPhone = () => {
  phoneForm.value = {
    phone: '',
    code: ''
  }
  showPhonePopup.value = true
}

const closePhonePopup = () => {
  showPhonePopup.value = false
  if (phoneTimer) {
    clearInterval(phoneTimer)
    phoneTimer = null
  }
  phoneCountdown.value = 0
}

const startPhoneCountdown = () => {
  phoneCountdown.value = 60
  phoneTimer = setInterval(() => {
    phoneCountdown.value--
    if (phoneCountdown.value <= 0) {
      clearInterval(phoneTimer)
      phoneTimer = null
    }
  }, 1000)
}

const handleSendPhoneCode = async () => {
  if (sendingPhoneCode.value || phoneCountdown.value > 0) return
  
  if (!phoneForm.value.phone) {
    return uni.showToast({ title: '请输入手机号', icon: 'none' })
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.value.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  
  sendingPhoneCode.value = true
  try {
    await sendSmsCode(phoneForm.value.phone, 2)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    startPhoneCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    uni.showToast({
      title: error?.message || '发送失败',
      icon: 'none'
    })
  } finally {
    sendingPhoneCode.value = false
  }
}

const handleSavePhone = async () => {
  if (!phoneForm.value.phone) {
    return uni.showToast({ title: '请输入手机号', icon: 'none' })
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.value.phone)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  
  if (!phoneForm.value.code) {
    return uni.showToast({ title: '请输入验证码', icon: 'none' })
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    await updateUserInfo({
      phone: phoneForm.value.phone,
      code: phoneForm.value.code
    })
    showPhonePopup.value = false
    uni.showToast({ title: '手机号绑定成功', icon: 'success' })
    await loadUserInfo()
  } catch (error) {
    console.error('绑定失败:', error)
  } finally {
    uni.hideLoading()
  }
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await logout()
        } catch (e) {
          console.error('退出失败', e)
        } finally {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/login' })
        }
      }
    }
  })
}

const handleApplyHistory = () => {
  uni.navigateTo({ url: '/pages/adoption/my-history' })
}

const handleMyAdoptedPets = () => {
  uni.navigateTo({ url: '/pages/pet/my-adopted' })
}

const handleMyFavorites = () => {
  uni.navigateTo({ url: '/pages/pet/my-favorite' })
}

const handleMyPets = () => {
  uni.navigateTo({ url: '/pages/pet/my-list?type=published' })
}

const handleMyChats = () => {
  uni.navigateTo({ url: '/pages/pet/my-list?type=chatted' })
}

const handleTodoAudit = () => {
  uni.navigateTo({ url: '/pages/adoption/audit-list' })
}
const handleSettings = () => handleChangePassword()
const handleAbout = () => uni.showToast({ title: '关于我们开发中...', icon: 'none' })

onMounted(() => {
  if (!checkLogin('/pages/profile/profile')) return
  loadUserInfo()
})

onShow(() => {
  uni.hideTabBar()
})

onUnmounted(() => {
  if (phoneTimer) {
    clearInterval(phoneTimer)
    phoneTimer = null
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.scroll-container {
  flex: 1;
  height: 100%;
}

.header-section {
  position: relative;
  background: linear-gradient(135deg, #2E7D32 0%, #1B5E20 100%);
  padding: 80rpx 40rpx 100rpx;
  border-radius: 0 0 60rpx 60rpx;
  overflow: hidden;
}

.header-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.08) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.5;
}

.header-decor {
  position: absolute;
  border-radius: 50%;
  
  &.decor-1 {
    width: 200rpx;
    height: 200rpx;
    background: rgba(255,255,255,0.1);
    top: 40rpx;
    right: -40rpx;
    filter: blur(40rpx);
  }
  
  &.decor-2 {
    width: 160rpx;
    height: 160rpx;
    background: rgba(255, 214, 0, 0.15);
    bottom: 40rpx;
    left: -40rpx;
    filter: blur(30rpx);
  }
}

.user-info-area {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-box {
  position: relative;
  margin-bottom: 20rpx;
  
  .avatar {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    border: 6rpx solid rgba(255,255,255,0.3);
    box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.2);
  }
  
  .avatar-edit-btn {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 44rpx;
    height: 44rpx;
    background: #FFD600;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.15);
  }
}

.user-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.2);
}

.user-tags {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .tag-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 20rpx;
    border-radius: 30rpx;
    font-size: 22rpx;
    font-weight: 500;
    
    &.primary {
      background: rgba(255,255,255,0.2);
      color: #fff;
      backdrop-filter: blur(10px);
      border: 1rpx solid rgba(255,255,255,0.1);
    }
    
    &.plain {
      background: rgba(0,0,0,0.1);
      color: rgba(255,255,255,0.8);
    }
  }
}

.content-area {
  position: relative;
  padding: 0 30rpx;
  padding-bottom: 180rpx;
  margin-top: -60rpx;
  z-index: 3;
}

.stats-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 8rpx 32rpx rgba(46, 125, 50, 0.12);
  margin-bottom: 30rpx;
  border: 1rpx solid rgba(0,0,0,0.03);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  
  .stat-num {
    font-size: 44rpx;
    font-weight: bold;
    color: #303133;
    font-family: 'DIN Alternate', sans-serif;
  }
  
  .stat-label {
    font-size: 22rpx;
    color: #909399;
    margin-top: 8rpx;
    font-weight: 500;
  }
  
  &.highlight {
    .stat-num {
      color: #2E7D32;
    }
    .stat-label {
      color: #2E7D32;
      opacity: 0.8;
    }
  }
}

.stat-divider {
  width: 1rpx;
  height: 50rpx;
  background: #ebeef5;
}

.section-block {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx;
}

.section-indicator {
  width: 8rpx;
  height: 36rpx;
  background: #2E7D32;
  border-radius: 4rpx;
  margin-right: 16rpx;
  
  &.orange {
    background: #E65100;
  }
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #303133;
  margin-right: 16rpx;
}

.section-tag {
  font-size: 20rpx;
  color: #2E7D32;
  background: rgba(46, 125, 50, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-weight: 500;
  
  &.orange {
    color: #E65100;
    background: rgba(230, 81, 0, 0.1);
  }
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  
  &.two-col {
    grid-template-columns: repeat(2, 1fr);
  }
}

.service-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 24rpx;
  background: #fafafa;
  border-radius: 20rpx;
  border: 1rpx solid rgba(0,0,0,0.03);
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
    background: #f5f5f5;
  }
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  &.orange {
    background: #fff3e0;
  }
  
  &.blue {
    background: #e3f2fd;
  }
  
  &.green {
    background: #e8f5e9;
  }
  
  &.purple {
    background: #f3e5f5;
  }
  
  &.cyan {
    background: #e0f7fa;
  }
  
  &.pink {
    background: #fce4ec;
  }
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  
  .service-name {
    font-size: 28rpx;
    font-weight: bold;
    color: #303133;
  }
  
  .service-desc {
    font-size: 22rpx;
    color: #909399;
  }
}

.achievement-card {
  background: linear-gradient(135deg, #fffde7 0%, #fff8e1 100%);
  border: 2rpx solid #ffecb3;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 24rpx;
  position: relative;
  overflow: hidden;
}

.achievement-icon {
  width: 64rpx;
  height: 64rpx;
  background: #FFD600;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 214, 0, 0.3);
  z-index: 2;
}

.achievement-content {
  flex: 1;
  z-index: 2;
  
  .achievement-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #303133;
    display: block;
  }
  
  .achievement-desc {
    font-size: 24rpx;
    color: #606266;
    margin-top: 4rpx;
    display: block;
    
    .highlight-num {
      color: #E65100;
      font-weight: bold;
    }
  }
}

.achievement-arrow {
  z-index: 2;
}

.achievement-bg-icon {
  position: absolute;
  right: -20rpx;
  bottom: -20rpx;
  opacity: 0.15;
  transform: rotate(15deg);
}

.menu-card {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
  margin-bottom: 24rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 30rpx;
  border-bottom: 1rpx solid #f5f7fa;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #f9fafc;
  }
  
  .menu-icon {
    width: 56rpx;
    height: 56rpx;
    background: #f5f7fa;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;
  }
  
  .menu-text {
    flex: 1;
    font-size: 28rpx;
    font-weight: 500;
    color: #303133;
  }
  
  .menu-right {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .menu-value {
      font-size: 26rpx;
      color: #909399;
    }
  }
  
  &.logout {
    .menu-icon {
      background: #fef0f0;
    }
  }
}

.version-info {
  text-align: center;
  padding: 40rpx 0 20rpx;
  
  text {
    font-size: 22rpx;
    color: #c0c4cc;
    font-weight: 500;
    letter-spacing: 1rpx;
  }
}

.edit-popup {
  width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.popup-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #303133;
  text-align: center;
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
  
  .form-label {
    display: block;
    font-size: 28rpx;
    color: #606266;
    margin-bottom: 16rpx;
  }
  
  .form-input {
    width: 100%;
    height: 88rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #303133;
  }
}

.code-input-row {
  display: flex;
  gap: 20rpx;
  
  .code-input {
    flex: 1;
  }
  
  .code-btn {
    width: 200rpx;
    height: 88rpx;
    background: #2E7D32;
    color: #fff;
    font-size: 26rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &[disabled] {
      background: #c0c4cc;
    }
  }
}

.popup-buttons {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;
  
  .popup-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 16rpx;
    font-size: 30rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.cancel {
      background: #f5f7fa;
      color: #606266;
    }
    
    &.confirm {
      background: #2E7D32;
      color: #fff;
    }
  }
}
</style>
