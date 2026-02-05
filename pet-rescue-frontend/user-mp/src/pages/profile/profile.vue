<template>
  <view class="profile-page">
    <!-- 顶部装饰背景 -->
    <view class="header-bg"></view>

    <!-- 内容区域 -->
    <view class="content-wrapper">
      <!-- 1. 用户信息卡片 (悬浮样式) -->
      <view class="user-card">
        <view class="user-main" @click="handleEditInfo">
          <view class="avatar-wrapper">
            <image 
              class="avatar" 
              :src="userInfo.avatar || '/static/logo.png'" 
              mode="aspectFill"
              @click.stop="handleAvatarClick"
            />
            <view class="edit-badge">
              <u-icon name="camera-fill" color="#fff" size="12"></u-icon>
            </view>
          </view>
          
          <view class="user-meta">
            <view class="name-row">
              <text class="nickname">{{ userInfo.nickname || '登录/注册' }}</text>
              <u-icon name="edit-pen" color="#fff" size="18" v-if="userInfo.nickname"></u-icon>
            </view>
            <text class="phone-text">{{ userInfo.phone || '点击完善个人信息' }}</text>
          </view>

          <view class="score-pill">
            <u-icon name="gift-fill" color="#ff9c00" size="16"></u-icon>
            <text class="score-val">{{ userInfo.score || 0 }} 积分</text>
          </view>
        </view>

        <!-- 装饰波浪 -->
        <view class="card-wave"></view>
      </view>

      <!-- 2. 领养中心 -->
      <view class="panel-card">
        <view class="panel-header">
          <text class="panel-title">领养中心</text>
          <text class="panel-tag">我是领养人</text>
        </view>
        <view class="grid-box">
          <view class="grid-item" @click="handleApplyHistory">
            <view class="icon-circle blue">
              <u-icon name="file-text-fill" color="#2979ff" size="28"></u-icon>
            </view>
            <text class="grid-label">申请记录</text>
          </view>
          <view class="grid-item" @click="handleMyAdoptedPets">
            <view class="icon-circle orange">
              <u-icon name="home-fill" color="#ff9c00" size="28"></u-icon>
            </view>
            <text class="grid-label">我的宠物</text>
          </view>
          <view class="grid-item" @click="handleMyChats">
            <view class="icon-circle green">
              <u-icon name="chat-fill" color="#19be6b" size="28"></u-icon>
            </view>
            <text class="grid-label">咨询足迹</text>
          </view>
          <view class="grid-item" @click="handleMyFavorites">
            <view class="icon-circle pink">
              <u-icon name="star-fill" color="#ff4d4f" size="28"></u-icon>
            </view>
            <text class="grid-label">我的收藏</text>
          </view>
        </view>
      </view>

      <!-- 3. 送养管理 -->
      <view class="panel-card">
        <view class="panel-header">
          <text class="panel-title">送养管理</text>
          <text class="panel-tag orange">我是送养人</text>
        </view>
        <view class="grid-box left-align">
          <view class="grid-item" @click="handleMyPets">
            <view class="icon-circle purple">
              <u-icon name="grid-fill" color="#a0cfff" size="28"></u-icon>
            </view>
            <text class="grid-label">我发布的</text>
          </view>
          <view class="grid-item" @click="handleTodoAudit">
            <view class="icon-circle cyan">
              <u-icon name="checkmark-circle-fill" color="#19be6b" size="28"></u-icon>
            </view>
            <text class="grid-label">待办审核</text>
          </view>
        </view>
      </view>

      <!-- 4. 常用功能 -->
      <view class="panel-card menu-card">
        <view class="menu-list">
          <view class="menu-item" @click="handleSettings">
            <view class="menu-left">
              <u-icon name="setting-fill" color="#909399" size="24"></u-icon>
              <text class="menu-text">账号设置 (修改密码)</text>
            </view>
            <u-icon name="arrow-right" color="#c0c4cc" size="14"></u-icon>
          </view>
          <view class="menu-item" @click="handleAbout">
            <view class="menu-left">
              <u-icon name="info-circle-fill" color="#909399" size="24"></u-icon>
              <text class="menu-text">关于我们</text>
            </view>
            <u-icon name="arrow-right" color="#c0c4cc" size="14"></u-icon>
          </view>
          <view class="menu-item no-border" @click="handleLogout">
            <view class="menu-left">
              <u-icon name="backspace" color="#fa3534" size="24"></u-icon>
              <text class="menu-text" style="color: #fa3534;">退出登录</text>
            </view>
            <u-icon name="arrow-right" color="#c0c4cc" size="14"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 弹窗部分保持不变 -->
    <u-popup :show="showEditPopup" @close="showEditPopup = false" mode="center" :round="20">
      <view class="edit-popup">
        <view class="popup-title">编辑资料</view>
        <view class="form-item">
          <text class="form-label">昵称</text>
          <input class="form-input" v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20"/>
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" v-model="editForm.phone" placeholder="请输入手机号" type="number" maxlength="11"/>
        </view>
        <view class="popup-buttons">
          <button class="popup-btn cancel" @click="showEditPopup = false">取消</button>
          <button class="popup-btn confirm" @click="handleSaveEdit">保存</button>
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
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyInfo, updateUserInfo, updatePassword, uploadImage } from '@/api/user'
import { miniLogin, phoneLogin, logout } from '@/api/auth'
import { checkLogin } from '@/utils/auth'

// 用户信息
const userInfo = ref({
  nickname: '',
  avatar: '',
  phone: '',
  score: 0,
  hasPassword: false
})

// 编辑表单
const editForm = ref({
  nickname: '',
  phone: '',
  avatar: ''
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 弹窗状态
const showEditPopup = ref(false)
const showPasswordPopup = ref(false)

// 修改/设置密码
const handleChangePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showPasswordPopup.value = true
}

// 保存密码
const handleSavePassword = async () => {
  // 只有已设置过密码才需要验证旧密码
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

// 获取个人信息
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

// 点击头像
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

// 编辑资料
const handleEditInfo = () => {
  editForm.value = {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    avatar: userInfo.value.avatar || ''
  }
  showEditPopup.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editForm.value.nickname) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  
  if (editForm.value.phone && !/^1[3-9]\d{9}$/.test(editForm.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
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

// 退出登录
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

// Placeholder functions
// 申请记录
const handleApplyHistory = () => {
  uni.navigateTo({ url: '/pages/adoption/my-history' })
}

// 我的已领养宠物
const handleMyAdoptedPets = () => {
  // 跳转到专属的"我的爱宠"页面
  uni.navigateTo({ url: '/pages/pet/my-adopted' })
}

// 我的收藏
const handleMyFavorites = () => {
  uni.navigateTo({ url: '/pages/pet/my-favorite' })
}

// 我发布的宠物
const handleMyPets = () => {
  uni.navigateTo({ url: '/pages/pet/my-list?type=published' })
}

// 我沟通过的宠物
const handleMyChats = () => {
  uni.navigateTo({ url: '/pages/pet/my-list?type=chatted' })
}

// 待办审核
const handleTodoAudit = () => {
  uni.navigateTo({ url: '/pages/adoption/audit-list' })
}
const handleSettings = () => handleChangePassword()
const handleAbout = () => uni.showToast({ title: '关于我们开发中...', icon: 'none' })

onMounted(() => {
  if (!checkLogin('/pages/profile/profile')) return
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 480rpx;
  background: linear-gradient(180deg, #19be6b 0%, #19be6b 60%, rgba(25, 190, 107, 0) 100%);
  z-index: 0;
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 0 30rpx;
  padding-top: 150rpx; /* 给头部留出空间 */
  padding-bottom: 50rpx;
}

/* 用户卡片 */
.user-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 12rpx 32rpx rgba(25, 190, 107, 0.15);
  margin-bottom: 30rpx;
  position: relative;
  overflow: hidden;
  
  .user-main {
    display: flex;
    align-items: center;
    position: relative;
    z-index: 2;
  }
}

.card-wave {
  position: absolute;
  bottom: -50rpx;
  right: -50rpx;
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(25, 190, 107, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  z-index: 1;
}

.avatar-wrapper {
  position: relative;
  margin-right: 30rpx;
  
  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
  }
  
  .edit-badge {
    position: absolute;
    bottom: 0;
    right: 0;
    background: #19be6b;
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid #fff;
  }
}

.user-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  
  .name-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
    
    .nickname {
      font-size: 36rpx;
      font-weight: bold;
      color: #303133;
    }
  }
  
  .phone-text {
    font-size: 24rpx;
    color: #909399;
  }
}

.score-pill {
  background: #fff7e6;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  
  .score-val {
    font-size: 24rpx;
    color: #ff9c00;
    font-weight: 600;
  }
}

/* 面板卡片 */
.panel-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
  
  .panel-header {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;
    
    .panel-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #303133;
      margin-right: 16rpx;
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        left: 0;
        bottom: -4rpx;
        width: 100%;
        height: 6rpx;
        background: rgba(25, 190, 107, 0.2);
        border-radius: 4rpx;
      }
    }
    
    .panel-tag {
      font-size: 20rpx;
      color: #19be6b;
      background: rgba(25, 190, 107, 0.1);
      padding: 4rpx 10rpx;
      border-radius: 6rpx;
      
      &.orange {
        color: #ff9c00;
        background: rgba(255, 156, 0, 0.1);
      }
    }
  }
}

/* Grid 样式 */
.grid-box {
  display: flex;
  flex-wrap: wrap;
  
  &.left-align {
    justify-content: flex-start;
    gap: 40rpx;
    
    .grid-item {
      width: auto;
      min-width: 120rpx;
    }
  }
  
  .grid-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    min-width: 25%;
    
    &:active {
      opacity: 0.8;
      transform: scale(0.98);
    }
  }
}

.icon-circle {
  width: 88rpx;
  height: 88rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4rpx;
  transition: all 0.3s;
  
  &.blue { background: #ecf5ff; }
  &.orange { background: #fdf6ec; }
  &.green { background: #dbf1e1; }
  &.pink { background: #fef0f0; }
  &.purple { background: #f4f4f5; } // Using gray for now, or mix a light purple
  &.cyan { background: #e8fcf7; }
}

.grid-label {
  font-size: 26rpx;
  color: #606266;
  font-weight: 500;
}

/* 菜单列表 */
.menu-list {
  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 0;
    border-bottom: 1rpx solid #f5f7fa;
    
    &:last-child, &.no-border {
      border-bottom: none;
    }
    
    .menu-left {
      display: flex;
      align-items: center;
      gap: 20rpx;
      
      .menu-text {
        font-size: 28rpx;
        color: #303133;
      }
    }
    
    &:active {
      background-color: #f9fafc;
      margin: 0 -30rpx;
      padding: 32rpx 30rpx;
    }
  }
}

/* 弹窗样式 */
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
}

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
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #303133;
  box-sizing: border-box;
}

.popup-buttons {
  display: flex;
  gap: 24rpx;
  margin-top: 48rpx;
}

.popup-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 500;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.cancel {
    background: #f5f7fa;
    color: #909399;
  }
  
  &.confirm {
    background: linear-gradient(90deg, #19be6b, #2bd380);
    color: #fff;
    box-shadow: 0 6rpx 16rpx rgba(25, 190, 107, 0.3);
  }
  
  &::after { border: none; }
}
</style>
