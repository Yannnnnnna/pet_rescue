<template>
  <view class="container">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">发布送养</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ marginTop: statusBarHeight + 44 + 'px' }">
      <view class="card">
        <view class="section-header">
          <view class="icon-box">
            <uni-icons type="info" size="18" color="#2E7D32"></uni-icons>
          </view>
          <text class="section-title">基本信息</text>
        </view>
        
        <view class="form-item">
          <view class="label required">昵称</view>
          <input class="input" v-model="form.name" placeholder="请输入宠物昵称" placeholder-class="placeholder-text" />
        </view>
        
        <view class="form-item">
          <view class="label required">种类</view>
          <picker :range="typeActions" range-key="name" @change="selectType" :value="form.type">
            <view class="picker-view">
              <text v-if="typeName" class="picker-text">{{ typeName }}</text>
              <text v-else class="placeholder-text">请选择种类</text>
              <uni-icons type="right" size="14" color="#9CA3AF"></uni-icons>
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <view class="label">品种</view>
          <input class="input" v-model="form.breed" placeholder="请输入品种(如:橘猫)" placeholder-class="placeholder-text" />
        </view>
        
        <view class="form-item">
          <view class="label">性别</view>
          <view class="radio-group">
            <view 
              class="radio-item" 
              :class="{ active: form.sex === 1 }"
              @click="form.sex = 1"
            >
              <view class="radio-circle">
                <view class="radio-dot" v-if="form.sex === 1"></view>
              </view>
              <text>公</text>
            </view>
            <view 
              class="radio-item" 
              :class="{ active: form.sex === 0 }"
              @click="form.sex = 0"
            >
              <view class="radio-circle">
                <view class="radio-dot" v-if="form.sex === 0"></view>
              </view>
              <text>母</text>
            </view>
            <view 
              class="radio-item" 
              :class="{ active: form.sex === 2 }"
              @click="form.sex = 2"
            >
              <view class="radio-circle">
                <view class="radio-dot" v-if="form.sex === 2"></view>
              </view>
              <text>未知</text>
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <view class="label">年龄</view>
          <input class="input" v-model="form.age" placeholder="请输入年龄(如:3个月)" placeholder-class="placeholder-text" />
        </view>
      </view>

      <view class="card">
        <view class="section-header">
          <view class="icon-box bg-yellow">
            <uni-icons type="flag" size="18" color="#F59E0B"></uni-icons>
          </view>
          <text class="section-title">详细信息</text>
        </view>
        
        <view class="form-item switch-item">
          <view class="label">绝育状态</view>
          <switch :checked="form.isSterilized === 1" @change="handleSterilizedChange" color="#2E7D32" style="transform:scale(0.8)" />
        </view>
        
        <view class="form-item switch-item">
          <view class="label">疫苗状态</view>
          <switch :checked="form.isVaccinated === 1" @change="handleVaccinatedChange" color="#2E7D32" style="transform:scale(0.8)" />
        </view>
        
        <view class="form-item">
          <view class="label">标签</view>
          <input class="input" v-model="form.tags" placeholder="多个标签用逗号分隔(如:粘人,安静)" placeholder-class="placeholder-text" />
        </view>
        
        <view class="form-item">
          <view class="label">所在城市</view>
          <picker mode="multiSelector" :range="cityRange" :value="cityIndex" @change="handleCityChange" @columnchange="handleCityColumnChange">
            <view class="picker-view">
              <text v-if="form.province && form.city" class="picker-text">{{ form.province }} {{ form.city }}</text>
              <text v-else class="placeholder-text">请选择所在城市</text>
              <uni-icons type="right" size="14" color="#9CA3AF"></uni-icons>
            </view>
          </picker>
        </view>
        
        <view class="form-item">
          <view class="label">详细地址</view>
          <input class="input" v-model="form.address" placeholder="请输入详细地址" placeholder-class="placeholder-text" />
        </view>

        <view class="form-item vertical">
          <view class="label required">详细介绍</view>
          <textarea class="textarea" v-model="form.description" placeholder="请描述宠物的性格、救助经历等" maxlength="2000" auto-height placeholder-class="placeholder-text"></textarea>
        </view>
      </view>

      <view class="card">
        <view class="section-header">
          <view class="icon-box bg-orange">
            <uni-icons type="image" size="18" color="#E64A19"></uni-icons>
          </view>
          <text class="section-title">照片上传</text>
          <text class="section-tip">第一张为封面</text>
        </view>
        <view class="img-grid">
          <view class="img-item" v-for="(url, index) in fileList" :key="index">
            <image :src="url" mode="aspectFill" class="grid-img"></image>
            <view class="del-btn" @click.stop="deletePic(index)">
              <uni-icons type="close" size="12" color="#fff"></uni-icons>
            </view>
            <view class="cover-tag" v-if="index === 0">封面</view>
          </view>
          <view class="img-item upload-btn" @click="chooseImage" v-if="fileList.length < 9">
            <uni-icons type="plusempty" size="32" color="#9CA3AF"></uni-icons>
            <text class="upload-text">添加照片</text>
          </view>
        </view>
      </view>

      <view class="btn-area">
        <button class="submit-btn" :loading="loading" @click="submit">
          <uni-icons type="paperplane" size="18" color="#fff" style="margin-right: 8rpx;"></uni-icons>
          立即发布
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addPet } from '@/api/pet'
import { uploadImage } from '@/api/user'
import { cityData } from '@/utils/cityData'

const statusBarHeight = ref(44)
const loading = ref(false)
const fileList = ref([])

const cityRange = ref([cityData.map(item => item.name), cityData[0].cities.map(item => item.name)])
const cityIndex = ref([0, 0])

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
})

const goBack = () => {
  uni.navigateBack()
}

const form = ref({
  name: '',
  type: null,
  breed: '',
  sex: 2,
  age: '',
  isSterilized: 0,
  isVaccinated: 0,
  tags: '',
  city: '',
  province: '',
  address: '',
  description: '',
  coverImg: '',
  detailImgList: []
})

const typeActions = [
  { name: '猫', value: 0 },
  { name: '狗', value: 1 },
  { name: '鸟', value: 2 },
  { name: '异宠', value: 3 },
  { name: '其他', value: 4 }
]

const typeName = computed(() => {
  const item = typeActions.find(i => i.value === form.value.type)
  return item ? item.name : ''
})

const selectType = (e) => {
  const index = e.detail.value
  form.value.type = typeActions[index].value
}

const handleSexChange = (e) => {
  form.value.sex = parseInt(e.detail.value)
}

const handleSterilizedChange = (e) => {
  form.value.isSterilized = e.detail.value ? 1 : 0
}

const handleVaccinatedChange = (e) => {
  form.value.isVaccinated = e.detail.value ? 1 : 0
}

const handleCityColumnChange = (e) => {
  const column = e.detail.column
  const row = e.detail.value
  
  if (column === 0) {
    const cities = cityData[row].cities.map(item => item.name)
    cityRange.value[1] = cities
    cityIndex.value = [row, 0]
  }
}

const handleCityChange = (e) => {
  const [provinceIndex, cityIndexValue] = e.detail.value
  form.value.province = cityData[provinceIndex].name
  form.value.city = cityData[provinceIndex].cities[cityIndexValue].name
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - fileList.value.length,
    success: async (res) => {
      uni.showLoading({ title: '上传中' })
      try {
        for (let path of res.tempFilePaths) {
          const url = await uploadImage(path)
          fileList.value.push(url)
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const deletePic = (index) => {
  fileList.value.splice(index, 1)
}

const validate = () => {
  if (!form.value.name) return '请输入昵称'
  if (form.value.type === null) return '请选择种类'
  if (!form.value.province || !form.value.city) return '请选择所在城市'
  if (!form.value.description) return '请输入描述'
  if (fileList.value.length === 0) return '请至少上传一张照片'
  return null
}

const submit = async () => {
  const errorMsg = validate()
  if (errorMsg) {
    uni.showToast({ title: errorMsg, icon: 'none' })
    return
  }

  form.value.coverImg = fileList.value[0]
  form.value.detailImgList = fileList.value

  loading.value = true
  try {
    await addPet(form.value)
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #F8FAF8;
  box-sizing: border-box;
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

.content-area {
  padding: 32rpx;
}

.card {
  background: #fff;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 28rpx;
  
  .icon-box {
    width: 48rpx;
    height: 48rpx;
    border-radius: 14rpx;
    background: #E8F5E9;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
    
    &.bg-yellow {
      background: #FFF9C4;
    }
    
    &.bg-orange {
      background: #FFE0B2;
    }
  }
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #1F2937;
  }
  
  .section-tip {
    font-size: 22rpx;
    color: #9CA3AF;
    margin-left: 16rpx;
  }
}

.form-item {
  margin-bottom: 28rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #F3F4F6;
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }
  
  &.vertical {
    .label {
      margin-bottom: 16rpx;
    }
  }
  
  &.switch-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 24rpx;
    border-bottom: 1rpx solid #F3F4F6;
    
    .label {
      margin-bottom: 0;
    }
  }
  
  .label {
    font-size: 28rpx;
    color: #374151;
    margin-bottom: 16rpx;
    display: block;
    font-weight: 500;
    
    &.required::after {
      content: '*';
      color: #EF4444;
      margin-left: 4rpx;
    }
  }
  
  .input {
    height: 88rpx;
    font-size: 28rpx;
    color: #1F2937;
    width: 100%;
    background: #F9FAFB;
    border-radius: 16rpx;
    padding: 0 24rpx;
    box-sizing: border-box;
  }
  
  .picker-view {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #F9FAFB;
    border-radius: 16rpx;
    padding: 0 24rpx;
    
    .picker-text {
      font-size: 28rpx;
      color: #1F2937;
    }
    
    .placeholder-text {
      font-size: 28rpx;
      color: #9CA3AF;
    }
  }
  
  .textarea {
    width: 100%;
    min-height: 200rpx;
    background: #F9FAFB;
    border-radius: 16rpx;
    padding: 24rpx;
    font-size: 28rpx;
    color: #1F2937;
    box-sizing: border-box;
    line-height: 1.6;
  }
}

.placeholder-text {
  color: #9CA3AF;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 24rpx;
  
  .radio-item {
    display: flex;
    align-items: center;
    padding: 16rpx 28rpx;
    background: #F9FAFB;
    border-radius: 16rpx;
    border: 2rpx solid transparent;
    transition: all 0.2s;
    
    &.active {
      background: #E8F5E9;
      border-color: #2E7D32;
      
      .radio-circle {
        border-color: #2E7D32;
        
        .radio-dot {
          background: #2E7D32;
        }
      }
      
      text {
        color: #2E7D32;
        font-weight: 500;
      }
    }
    
    .radio-circle {
      width: 36rpx;
      height: 36rpx;
      border-radius: 50%;
      border: 2rpx solid #D1D5DB;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12rpx;
      
      .radio-dot {
        width: 20rpx;
        height: 20rpx;
        border-radius: 50%;
        background: #2E7D32;
      }
    }
    
    text {
      font-size: 28rpx;
      color: #6B7280;
    }
  }
}

.img-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  
  .img-item {
    width: 200rpx;
    height: 200rpx;
    position: relative;
    border-radius: 20rpx;
    overflow: hidden;
    
    &.upload-btn {
      background: #F9FAFB;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border: 2rpx dashed #D1D5DB;
      
      .upload-text {
        font-size: 22rpx;
        color: #9CA3AF;
        margin-top: 8rpx;
      }
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
      justify-content: center;
      align-items: center;
    }
    
    .cover-tag {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: linear-gradient(90deg, #2E7D32, #43A047);
      color: #fff;
      font-size: 20rpx;
      text-align: center;
      padding: 6rpx 0;
    }
  }
}

.btn-area {
  margin-top: 48rpx;
  padding-bottom: 60rpx;
  
  .submit-btn {
    background: linear-gradient(135deg, #2E7D32 0%, #43A047 100%);
    color: #fff;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: bold;
    height: 96rpx;
    line-height: 96rpx;
    box-shadow: 0 8rpx 24rpx rgba(46, 125, 50, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    
    &::after {
      border: none;
    }
    
    &:active {
      opacity: 0.9;
      transform: scale(0.98);
    }
  }
}
</style>
