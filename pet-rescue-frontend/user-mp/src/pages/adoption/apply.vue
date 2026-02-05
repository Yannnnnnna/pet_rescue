<template>
  <view class="container">
    <view class="page-title">领养申请</view>
    
    <view class="card">
      <view class="pet-info" v-if="petInfo">
        <text class="pet-label">申请领养：</text>
        <text class="pet-name">{{ petInfo.name }}</text>
      </view>
      <view class="pet-info" v-else>
         <text>正在加载宠物信息...</text>
      </view>

      <view class="form-item">
        <view class="label required">真实姓名</view>
        <input class="input" v-model="form.realName" placeholder="请输入您的真实姓名" />
      </view>
      
      <view class="form-item">
        <view class="label required">联系电话</view>
        <input class="input" v-model="form.phone" type="number" placeholder="请输入联系电话" maxlength="11" />
      </view>
      
      <view class="form-item">
        <view class="label required">居住地址</view>
        <input class="input" v-model="form.address" placeholder="请输入您的居住地址" />
      </view>

      <view class="form-item">
        <view class="label">养宠经验</view>
        <radio-group @change="handleExperienceChange" class="radio-group">
          <label class="radio-label" v-for="(item, index) in experienceOptions" :key="index">
            <radio :value="item" :checked="form.experience === item" color="#19be6b" style="transform:scale(0.8)" />
            <text>{{ item }}</text>
          </label>
        </radio-group>
      </view>
      
      <view class="form-item">
        <view class="label">住房情况</view>
        <input class="input" v-model="form.housingCondition" placeholder="例如：自有住房/租房/有阳台等" />
      </view>

      <view class="form-item vertical">
        <view class="label required">领养理由</view>
        <textarea class="textarea" v-model="form.reason" placeholder="请简述您的领养理由和照顾计划" maxlength="200" auto-height></textarea>
      </view>
    </view>

    <view class="btn-area">
      <button class="submit-btn" :loading="loading" @click="submit">提交申请</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { applyForAdoption } from '@/api/adoption'
import { getPetDetail } from '@/api/pet'

const loading = ref(false)
const petId = ref(null)
const petInfo = ref(null)

const form = reactive({
  petId: '',
  realName: '',
  phone: '',
  address: '',
  experience: '无经验',
  housingCondition: '',
  reason: ''
})

const experienceOptions = ['无经验', '有经验', '目前有宠']

const handleExperienceChange = (e) => {
  form.experience = e.detail.value
}

onLoad((options) => {
  if (options.petId) {
    petId.value = options.petId
    form.petId = options.petId
    loadPetInfo()
  }
})

const loadPetInfo = async () => {
  try {
    const res = await getPetDetail(petId.value)
    if (res.data) {
      petInfo.value = res.data
    }
  } catch (error) {
    console.error(error)
    uni.showToast({ title: '获取宠物信息失败', icon: 'none' })
  }
}

const validate = () => {
  if (!form.realName) return '请输入真实姓名'
  if (!form.phone) return '请输入联系电话'
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return '手机号格式不正确'
  if (!form.address) return '请输入居住地址'
  if (!form.reason) return '请输入领养理由'
  return null
}

const submit = async () => {
  const errorMsg = validate()
  if (errorMsg) {
    uni.showToast({ title: errorMsg, icon: 'none' })
    return
  }

  loading.value = true
  try {
    await applyForAdoption(form)
    uni.showToast({ title: '申请已提交', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    console.error(error)
    // error handled by interceptor usually, but safe fallback
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
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}

.pet-info {
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
  display: flex;
  align-items: center;
  
  .pet-label {
    color: #666;
    font-size: 28rpx;
  }
  
  .pet-name {
    font-size: 32rpx;
    font-weight: bold;
    color: #19be6b;
  }
}

.form-item {
  margin-bottom: 30rpx;
  
  &.vertical {
    display: block;
    .label {
      margin-bottom: 16rpx;
    }
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
    background: #f8f8f8;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333;
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
  }
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  
  .radio-label {
    display: flex;
    align-items: center;
    margin-right: 30rpx;
    margin-bottom: 10rpx;
    font-size: 28rpx;
    color: #666;
  }
}

.btn-area {
  margin-top: 60rpx;
  
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
