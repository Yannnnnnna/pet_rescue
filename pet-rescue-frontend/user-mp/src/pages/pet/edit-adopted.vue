<template>
  <view class="page-container">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">编辑宠物信息</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="form-card">
        <view class="section-title">
          <view class="title-icon">🐾</view>
          <text>基本信息</text>
        </view>
        
        <view class="form-item">
          <view class="label required">昵称</view>
          <input class="input" v-model="form.name" placeholder="请输入宠物昵称" />
        </view>
        
        <view class="form-item">
          <view class="label">品种</view>
          <input class="input" v-model="form.breed" placeholder="请输入品种" />
        </view>
        
        <view class="form-item">
          <view class="label">年龄</view>
          <input class="input" v-model="form.age" placeholder="例如: 3个月" />
        </view>
        
        <view class="form-item">
          <view class="label">性别</view>
          <radio-group @change="handleSexChange" class="radio-group">
            <label class="radio-label"><radio :value="1" :checked="form.sex === 1" color="#2E7D32" style="transform:scale(0.8)"/>公</label>
            <label class="radio-label"><radio :value="0" :checked="form.sex === 0" color="#2E7D32" style="transform:scale(0.8)"/>母</label>
            <label class="radio-label"><radio :value="2" :checked="form.sex === 2" color="#2E7D32" style="transform:scale(0.8)"/>未知</label>
          </radio-group>
        </view>
      </view>

      <view class="form-card">
        <view class="section-title">
          <view class="title-icon">💉</view>
          <text>医疗状况</text>
        </view>
        <view class="form-item row-between">
          <view class="label">已绝育</view>
          <switch :checked="form.isSterilized === 1" @change="handleSterilizedChange" color="#2E7D32" style="transform:scale(0.8)" />
        </view>
        <view class="form-item row-between">
          <view class="label">已疫苗</view>
          <switch :checked="form.isVaccinated === 1" @change="handleVaccinatedChange" color="#2E7D32" style="transform:scale(0.8)" />
        </view>
      </view>
      
      <view class="form-card">
        <view class="section-title">
          <view class="title-icon">📷</view>
          <text>宠物照片</text>
        </view>
        <view class="upload-box" @click="chooseCover">
          <image v-if="form.cover" :src="form.cover" mode="aspectFill" class="preview-img"></image>
          <view v-else class="upload-placeholder">
            <uni-icons type="plusempty" size="40" color="#ccc"></uni-icons>
            <text class="upload-text">点击上传封面</text>
          </view>
        </view>
        
        <view class="detail-img-section">
          <view class="detail-title">详情图 (最多9张)</view>
          <view class="img-grid">
            <view class="img-item" v-for="(url, index) in detailImgList" :key="index">
              <image :src="url" mode="aspectFill" class="grid-img"></image>
              <view class="del-btn" @click.stop="removeDetailImg(index)">
                <uni-icons type="closeempty" size="12" color="#fff"></uni-icons>
              </view>
            </view>
            <view class="img-item upload-btn" @click="chooseDetailImg" v-if="detailImgList.length < 9">
              <uni-icons type="plusempty" size="30" color="#ccc"></uni-icons>
            </view>
          </view>
        </view>
      </view>
      
      <view class="form-card">
        <view class="section-title">
          <view class="title-icon">📝</view>
          <text>更多介绍</text>
        </view>
        
        <view class="form-item">
          <view class="label">标签</view>
          <input class="input" v-model="form.tags" placeholder="使用逗号分隔, 如: 粘人,可爱" />
        </view>
        
        <view class="form-item vertical">
          <view class="label">宠物介绍</view>
          <textarea class="textarea" v-model="form.description" placeholder="请输入宠物的详细介绍..." maxlength="500" auto-height></textarea>
        </view>
      </view>
      
      <view class="btn-area">
        <button class="submit-btn" :loading="submitting" @click="handleSubmit">
          <uni-icons type="checkbox-filled" size="18" color="#fff"></uni-icons>
          <text>保存修改</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetDetail, updatePet } from '@/api/pet'
import { uploadImage } from '@/api/user'

const form = reactive({
  id: null,
  name: '',
  breed: '',
  age: '',
  sex: 2,
  cover: '',
  detailImgList: [],
  tags: '',
  description: '',
  isSterilized: 0,
  isVaccinated: 0
})

const detailImgList = ref([])
const submitting = ref(false)
const statusBarHeight = ref(20)

onLoad((options) => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  
  if (options.id) {
    form.id = options.id
    loadPetDetail(options.id)
  }
})

const loadPetDetail = async (id) => {
  uni.showLoading({ title: '加载中' })
  try {
    const res = await getPetDetail(id)
    if (res.data) {
      const data = res.data
      Object.keys(form).forEach(key => {
        if (data[key] !== undefined && data[key] !== null) {
          form[key] = data[key]
        }
      })
      
      if (typeof form.detailImgList === 'string') {
        try {
          detailImgList.value = JSON.parse(form.detailImgList)
        } catch(e) {
          detailImgList.value = []
        }
      } else if (Array.isArray(form.detailImgList)) {
        detailImgList.value = [...form.detailImgList]
      }
    }
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const goBack = () => uni.navigateBack()

const handleSexChange = (e) => {
  form.sex = parseInt(e.detail.value)
}

const handleSterilizedChange = (e) => {
  form.isSterilized = e.detail.value ? 1 : 0
}

const handleVaccinatedChange = (e) => {
  form.isVaccinated = e.detail.value ? 1 : 0
}

const chooseCover = () => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      uni.showLoading({ title: '上传中' })
      try {
        const url = await uploadImage(res.tempFilePaths[0])
        form.cover = url
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const chooseDetailImg = () => {
  uni.chooseImage({
    count: 9 - detailImgList.value.length,
    success: async (res) => {
      uni.showLoading({ title: '上传中' })
      try {
        for (let path of res.tempFilePaths) {
          const url = await uploadImage(path)
          detailImgList.value.push(url)
        }
      } catch (e) {
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const removeDetailImg = (index) => {
  detailImgList.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!form.name) return uni.showToast({ title: '请输入昵称', icon: 'none' })
  
  submitting.value = true
  form.detailImgList = detailImgList.value
  
  if (form.tags) {
    form.tags = form.tags.replace(/，/g, ',')
  }

  try {
    await updatePet(form)
    uni.showToast({ title: '修改成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '修改失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background: #f8f9fa;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 350rpx;
  background: linear-gradient(180deg, #2E7D32 0%, #388E3C 60%, rgba(46, 125, 50, 0) 100%);
  z-index: 0;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: transparent;
  
  .nav-content {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
  }
  
  .back-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(10px);
  }
  
  .placeholder {
    width: 64rpx;
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

.content-area {
  position: relative;
  z-index: 1;
  padding: 24rpx;
  padding-bottom: 60rpx;
}

.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 30rpx;
  color: #333;
  
  .title-icon {
    font-size: 32rpx;
  }
}

.form-item {
  margin-bottom: 28rpx;
  border-bottom: 1rpx solid #f5f5f5;
  padding-bottom: 20rpx;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
  
  &.vertical {
    display: block;
    .label {
      margin-bottom: 16rpx;
    }
  }
  
  &.row-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1rpx solid #f5f5f5;
    padding-bottom: 20rpx;
  }
  
  .label {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 12rpx;
    display: block;
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: #ff4d4f;
      margin-left: 4rpx;
    }
  }
  
  .input {
    height: 80rpx;
    font-size: 28rpx;
    color: #333;
    width: 100%;
  }
  
  .textarea {
    width: 100%;
    min-height: 160rpx;
    background: #f8f8f8;
    border-radius: 16rpx;
    padding: 20rpx;
    font-size: 28rpx;
    color: #333;
    box-sizing: border-box;
    margin-top: 10rpx;
  }
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  
  .radio-label {
    display: flex;
    align-items: center;
    margin-right: 40rpx;
    margin-bottom: 10rpx;
    font-size: 28rpx;
    color: #666;
  }
}

.upload-box {
  width: 100%;
  height: 360rpx;
  background: #f8f8f8;
  border-radius: 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border: 2rpx dashed #ddd;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  
  .upload-text {
    font-size: 26rpx;
    color: #999;
  }
}

.preview-img {
  width: 100%;
  height: 100%;
}

.detail-img-section {
  margin-top: 30rpx;
  
  .detail-title {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 20rpx;
  }
}

.img-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  
  .img-item {
    width: 180rpx;
    height: 180rpx;
    position: relative;
    border-radius: 16rpx;
    overflow: hidden;
    
    &.upload-btn {
      background: #f8f8f8;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 2rpx dashed #ddd;
    }
    
    .grid-img {
      width: 100%;
      height: 100%;
    }
    
    .del-btn {
      position: absolute;
      top: 8rpx;
      right: 8rpx;
      width: 40rpx;
      height: 40rpx;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.btn-area {
  padding: 40rpx 0;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #2E7D32, #388E3C);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(46, 125, 50, 0.3);
  border: none;
  
  &::after {
    border: none;
  }
}
</style>
