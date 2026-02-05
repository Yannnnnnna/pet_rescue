<template>
  <view class="container">
    <view class="page-title">编辑信息</view>
    
    <!-- 基本信息 -->
    <view class="card">
      <view class="section-title">基本信息</view>
      
      <view class="form-item">
        <view class="label required">昵称</view>
        <input class="input" v-model="form.name" placeholder="请输入宠物昵称" />
      </view>
      
      <view class="form-item">
        <view class="label required">类型</view>
        <radio-group @change="handleTypeChange" class="radio-group">
           <label class="radio-label"><radio :value="0" :checked="form.type === 0" color="#19be6b" style="transform:scale(0.8)"/>猫</label>
           <label class="radio-label"><radio :value="1" :checked="form.type === 1" color="#19be6b" style="transform:scale(0.8)"/>狗</label>
           <label class="radio-label"><radio :value="2" :checked="form.type === 2" color="#19be6b" style="transform:scale(0.8)"/>鸟</label>
           <label class="radio-label"><radio :value="3" :checked="form.type === 3" color="#19be6b" style="transform:scale(0.8)"/>异宠</label>
           <label class="radio-label"><radio :value="4" :checked="form.type === 4" color="#19be6b" style="transform:scale(0.8)"/>其他</label>
        </radio-group>
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
          <label class="radio-label"><radio :value="1" :checked="form.sex === 1" color="#19be6b" style="transform:scale(0.8)"/>公</label>
          <label class="radio-label"><radio :value="0" :checked="form.sex === 0" color="#19be6b" style="transform:scale(0.8)"/>母</label>
          <label class="radio-label"><radio :value="2" :checked="form.sex === 2" color="#19be6b" style="transform:scale(0.8)"/>未知</label>
        </radio-group>
      </view>

      <view class="form-item">
           <view class="label">状态</view>
           <radio-group class="radio-group" disabled>
             <label class="radio-label"><radio :value="0" :checked="form.status === 0" disabled style="transform:scale(0.8)"/>待领养</label>
             <label class="radio-label"><radio :value="1" :checked="form.status === 1" disabled style="transform:scale(0.8)"/>申请中</label>
             <label class="radio-label"><radio :value="2" :checked="form.status === 2" disabled style="transform:scale(0.8)"/>已领养</label>
           </radio-group>
           <view class="tips-text">如需修改状态，请在“申请记录”中操作或联系管理员</view>
      </view>
    </view>
    
    <!-- 医疗状况 -->
    <view class="card">
      <view class="section-title">医疗状况</view>
      <view class="form-item row-between">
         <view class="label">已绝育</view>
         <switch :checked="form.isSterilized === 1" @change="handleSterilizedChange" color="#19be6b" style="transform:scale(0.8)" />
      </view>
      <view class="form-item row-between">
         <view class="label">已疫苗</view>
         <switch :checked="form.isVaccinated === 1" @change="handleVaccinatedChange" color="#19be6b" style="transform:scale(0.8)" />
      </view>
    </view>
    
    <!-- 封面图 -->
    <view class="card">
      <view class="section-title">封面图 (必传)</view>
      <view class="upload-box" @click="chooseCover">
        <image v-if="form.coverImg" :src="form.coverImg" mode="aspectFill" class="preview-img"></image>
        <view v-else class="upload-placeholder">
          <text class="plus">+</text>
          <text class="upload-text">点击上传封面</text>
        </view>
      </view>
    </view>
    
    <!-- 详情图 -->
    <view class="card">
      <view class="section-title">详情图 (最多9张)</view>
      <view class="img-grid">
         <view class="img-item" v-for="(url, index) in detailImgList" :key="index">
           <image :src="url" mode="aspectFill" class="grid-img"></image>
           <view class="del-btn" @click.stop="removeDetailImg(index)">×</view>
         </view>
         <view class="img-item upload-btn" @click="chooseDetailImg" v-if="detailImgList.length < 9">
            <text class="plus">+</text>
         </view>
      </view>
    </view>
    
    <!-- 更多信息 -->
    <view class="card">
      <view class="section-title">更多信息</view>
      
      <view class="form-item">
        <view class="label">标签</view>
        <input class="input" v-model="form.tags" placeholder="使用逗号分隔, 如: 粘人,可爱" />
      </view>
      
      <view class="form-item">
           <view class="label">地区</view>
           <picker mode="region" level="city" @change="handleRegionChange" :value="regionValue">
               <view class="picker-view">
                   <text v-if="form.province" class="picker-text">{{ form.province }} {{ form.city }}</text>
                   <text v-else class="placeholder">请选择省市区</text>
                   <text class="arrow">></text>
               </view>
           </picker>
      </view>
       <view class="form-item">
         <view class="label">详细地址</view>
         <input class="input" v-model="form.address" placeholder="详细地址" />
       </view>
      
      <view class="form-item vertical">
        <view class="label">送养人说</view>
        <textarea class="textarea" v-model="form.description" placeholder="请输入宠物的详细介绍或救助故事..." maxlength="500" auto-height></textarea>
      </view>
    </view>
    
    <view class="btn-area">
      <button class="submit-btn" :loading="submitting" @click="handleSubmit">保存修改</button>
    </view>
    
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPetDetail, updatePet } from '@/api/pet'
import { uploadImage } from '@/api/user'

const form = reactive({
  id: null,
  name: '',
  type: 0,
  breed: '',
  age: '',
  sex: 2,
  status: 0,
  coverImg: '',
  detailImgList: [], 
  tags: '',
  description: '',
  isSterilized: 0,
  isVaccinated: 0,
  province: '',
  city: '',
  address: ''
})

const detailImgList = ref([]) // 本地维护的数组
const submitting = ref(false)
const regionValue = ref([]) // 省市区选择器的绑定值

onLoad((options) => {
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
      // 填充表单
      Object.keys(form).forEach(key => {
        if (data[key] !== undefined && data[key] !== null) {
          form[key] = data[key]
        }
      })
      
      // 设置地区选择器的初始值
      if (form.province && form.city) {
          regionValue.value = [form.province, form.city]
      }
      
      // 处理详情图
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

const handleTypeChange = (e) => {
    form.type = parseInt(e.detail.value)
}

const handleSexChange = (e) => {
    form.sex = parseInt(e.detail.value)
}

const handleSterilizedChange = (e) => {
  form.isSterilized = e.detail.value ? 1 : 0
}

const handleVaccinatedChange = (e) => {
  form.isVaccinated = e.detail.value ? 1 : 0
}

const handleRegionChange = (e) => {
    const values = e.detail.value 
    if (values && values.length >= 2) {
        // picker返回: [省, 市]
        form.province = values[0]
        form.city = values[1]
    }
}

// 上传封面
const chooseCover = () => {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      uni.showLoading({ title: '上传中' })
      try {
        const url = await uploadImage(res.tempFilePaths[0])
        form.coverImg = url
      } catch (e) {
        // error handled in uploadImage
      } finally {
        uni.hideLoading()
      }
    }
  })
}

// 上传详情图
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
  if (!form.coverImg) return uni.showToast({ title: '请上传封面', icon: 'none' })
  
  submitting.value = true
  
  // 更新 detailImgList 到 form
  form.detailImgList = detailImgList.value
  
  // 处理 tags: 替换中文逗号为英文逗号
  if (form.tags) {
      form.tags = form.tags.replace(/，/g, ',')
  }

  try {
    await updatePet(form)
    uni.showToast({ title: '修改成功', icon: 'success' })
    setTimeout(() => {
      // 返回并刷新
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
    margin-right: 30rpx;
    margin-bottom: 10rpx;
    font-size: 28rpx;
    color: #666;
  }
}

.tips-text {
    font-size: 24rpx;
    color: #999;
    margin-top: 10rpx;
    background: #f8f8f8;
    padding: 10rpx;
    border-radius: 8rpx;
}

.upload-box {
  width: 100%;
  height: 360rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border: 1px dashed #ddd;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .plus {
      font-size: 80rpx;
      color: #ccc;
      font-weight: 200;
  }
  
  .upload-text {
    font-size: 24rpx;
    color: #999;
    margin-top: 10rpx;
  }
}

.preview-img {
  width: 100%;
  height: 100%;
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
      background: rgba(0,0,0,0.5);
      width: 40rpx;
      height: 40rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      border-bottom-left-radius: 12rpx;
      color: #fff;
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
