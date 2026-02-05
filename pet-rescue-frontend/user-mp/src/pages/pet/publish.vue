<template>
  <view class="container">
    <view class="page-title">发布送养</view>
    
    <!-- 基本信息 -->
    <view class="card">
      <view class="section-title">基本信息</view>
      
      <view class="form-item">
        <view class="label required">昵称</view>
        <input class="input" v-model="form.name" placeholder="请输入宠物昵称" />
      </view>
      
      <view class="form-item">
        <view class="label required">种类</view>
        <picker :range="typeActions" range-key="name" @change="selectType" :value="form.type">
          <view class="picker-view">
            <text v-if="typeName" class="picker-text">{{ typeName }}</text>
            <text v-else class="placeholder">请选择种类</text>
            <text class="arrow">></text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <view class="label">品种</view>
        <input class="input" v-model="form.breed" placeholder="请输入品种(如:橘猫)" />
      </view>
      
      <view class="form-item">
        <view class="label">性别</view>
        <radio-group @change="handleSexChange" class="radio-group">
          <label class="radio-label">
            <radio :value="1" :checked="form.sex === 1" color="#19be6b" style="transform:scale(0.8)" /> 公
          </label>
          <label class="radio-label">
            <radio :value="0" :checked="form.sex === 0" color="#19be6b" style="transform:scale(0.8)" /> 母
          </label>
          <label class="radio-label">
            <radio :value="2" :checked="form.sex === 2" color="#19be6b" style="transform:scale(0.8)" /> 未知
          </label>
        </radio-group>
      </view>
      
      <view class="form-item">
        <view class="label">年龄</view>
        <input class="input" v-model="form.age" placeholder="请输入年龄(如:3个月)" />
      </view>
    </view>

    <!-- 详细信息 -->
    <view class="card">
      <view class="section-title">详细信息</view>
      
      <view class="form-item row-between">
        <view class="label">绝育状态</view>
        <switch :checked="form.isSterilized === 1" @change="handleSterilizedChange" color="#19be6b" style="transform:scale(0.8)" />
      </view>
      
      <view class="form-item row-between">
        <view class="label">疫苗状态</view>
        <switch :checked="form.isVaccinated === 1" @change="handleVaccinatedChange" color="#19be6b" style="transform:scale(0.8)" />
      </view>
      
      <view class="form-item">
        <view class="label">标签</view>
        <input class="input" v-model="form.tags" placeholder="多个标签用逗号分隔(如:粘人,安静)" />
      </view>
      
      <view class="form-item">
        <view class="label">所在城市</view>
        <picker mode="multiSelector" :range="cityRange" :value="cityIndex" @change="handleCityChange" @columnchange="handleCityColumnChange">
          <view class="picker-view">
            <text v-if="form.province && form.city" class="picker-text">{{ form.province }} {{ form.city }}</text>
            <text v-else class="placeholder">请选择所在城市</text>
            <text class="arrow">></text>
          </view>
        </picker>
      </view>
      
      <view class="form-item">
        <view class="label">详细地址</view>
        <input class="input" v-model="form.address" placeholder="请输入详细地址" />
      </view>

      <view class="form-item vertical">
        <view class="label required">详细介绍</view>
        <textarea class="textarea" v-model="form.description" placeholder="请描述宠物的性格、救助经历等" maxlength="2000" auto-height></textarea>
      </view>
    </view>

    <!-- 图片上传 -->
    <view class="card">
      <view class="section-title">照片上传 (第一张为封面)</view>
      <view class="img-grid">
         <view class="img-item" v-for="(url, index) in fileList" :key="index">
           <image :src="url" mode="aspectFill" class="grid-img"></image>
           <view class="del-btn" @click.stop="deletePic(index)">×</view>
         </view>
         <view class="img-item upload-btn" @click="chooseImage" v-if="fileList.length < 9">
            <text class="plus">+</text>
         </view>
      </view>
    </view>

    <view class="btn-area">
      <button class="submit-btn" :loading="loading" @click="submit">立即发布</button>
    </view>

  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addPet } from '@/api/pet'
import { uploadImage } from '@/api/user'
import { cityData } from '@/utils/cityData'

const loading = ref(false)
const fileList = ref([]) // 存储图片URL字符串

const cityRange = ref([cityData.map(item => item.name), cityData[0].cities.map(item => item.name)])
const cityIndex = ref([0, 0])

const form = ref({
  name: '',
  type: null,
  breed: '',
  sex: 2, // 默认未知
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

// 图片上传处理
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

  // 提取图片
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
  background-color: #f5f7fa;
  padding: 30rpx;
  box-sizing: border-box;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  padding-left: 10rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  border-left: 8rpx solid #19be6b;
  padding-left: 16rpx;
  color: #333;
}

.form-item {
  margin-bottom: 30rpx;
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
  
  .picker-view {
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .picker-text {
      font-size: 28rpx;
      color: #333;
    }
    
    .placeholder {
      font-size: 28rpx;
      color: #999;
    }
    
    .arrow {
      color: #ccc;
      font-size: 24rpx;
    }
  }
  
  .textarea {
    width: 100%;
    min-height: 160rpx;
    background: #f8f8f8;
    border-radius: 12rpx;
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
    font-size: 28rpx;
    color: #666;
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
    border-radius: 12rpx;
    overflow: hidden;
    
    &.upload-btn {
      background: #f8f8f8;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px dashed #ddd;
      
      .plus {
        font-size: 60rpx;
        color: #ccc;
        font-weight: 300;
      }
    }
    
    .grid-img {
      width: 100%;
      height: 100%;
    }
    
    .del-btn {
      position: absolute;
      top: 0;
      right: 0;
      width: 40rpx;
      height: 40rpx;
      background: rgba(0,0,0,0.5);
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      border-bottom-left-radius: 12rpx;
      font-size: 24rpx;
    }
  }
}

.btn-area {
  margin-top: 60rpx;
  padding-bottom: 60rpx;
  
  .submit-btn {
    background: linear-gradient(90deg, #19be6b, #20d67a);
    color: #fff;
    border-radius: 45rpx;
    font-size: 32rpx;
    font-weight: bold;
    box-shadow: 0 8rpx 20rpx rgba(25, 190, 107, 0.3);
    
    &::after {
      border: none;
    }
    
    &:active {
      opacity: 0.9;
    }
  }
}
</style>
