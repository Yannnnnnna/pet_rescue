<template>
  <view class="container">
    <u-form :model="form" ref="uForm" label-width="80">
      
      <!-- 基本信息 -->
      <view class="card">
        <view class="section-title">基本信息</view>
        
        <u-form-item label="昵称" prop="name" required borderBottom>
          <u-input v-model="form.name" placeholder="请输入宠物昵称" border="none"></u-input>
        </u-form-item>
        
        <u-form-item label="类型" prop="type" required borderBottom>
          <u-radio-group v-model="form.type">
            <u-radio :name="0" label="猫"></u-radio>
            <u-radio :name="1" label="狗"></u-radio>
            <u-radio :name="2" label="鸟"></u-radio>
            <u-radio :name="3" label="异宠"></u-radio>
            <u-radio :name="4" label="其他"></u-radio>
          </u-radio-group>
        </u-form-item>
        
        <u-form-item label="品种" prop="breed" borderBottom>
          <u-input v-model="form.breed" placeholder="请输入品种" border="none"></u-input>
        </u-form-item>
        
        <u-form-item label="年龄" prop="age" borderBottom>
          <u-input v-model="form.age" placeholder="例如: 3个月" border="none"></u-input>
        </u-form-item>
        
        <u-form-item label="性别" prop="sex" borderBottom>
          <u-radio-group v-model="form.sex">
            <u-radio :name="1" label="公"></u-radio>
            <u-radio :name="0" label="母"></u-radio>
            <u-radio :name="2" label="未知"></u-radio>
          </u-radio-group>
        </u-form-item>

        <u-form-item label="状态" prop="status" borderBottom>
             <u-radio-group v-model="form.status" disabled>
               <u-radio :name="0" label="待领养" disabled></u-radio>
               <u-radio :name="1" label="申请中" disabled></u-radio>
               <u-radio :name="2" label="已领养" disabled></u-radio>
             </u-radio-group>
             <text class="tips-text">如需修改状态，请在“申请记录”中操作或联系管理员</text>
        </u-form-item>
      </view>
      
      <!-- 医疗状况 -->
      <view class="card">
        <view class="section-title">医疗状况</view>
        <u-form-item label="已绝育" prop="isSterilized" borderBottom>
           <u-switch v-model="isSterilizedBool" @change="handleSterilizedChange" activeColor="#19be6b"></u-switch>
        </u-form-item>
        <u-form-item label="已疫苗" prop="isVaccinated" borderBottom>
           <u-switch v-model="isVaccinatedBool" @change="handleVaccinatedChange" activeColor="#19be6b"></u-switch>
        </u-form-item>
      </view>
      
      <!-- 封面图 -->
      <view class="card">
        <view class="section-title">封面图 (必传)</view>
        <view class="upload-box" @click="chooseCover">
          <image v-if="form.coverImg" :src="form.coverImg" mode="aspectFill" class="preview-img"></image>
          <view v-else class="upload-placeholder">
            <u-icon name="camera-fill" size="30" color="#999"></u-icon>
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
             <view class="del-btn" @click.stop="removeDetailImg(index)">
                <u-icon name="close" color="#fff" size="10"></u-icon>
             </view>
           </view>
           <view class="img-item upload-btn" @click="chooseDetailImg" v-if="detailImgList.length < 9">
              <u-icon name="plus" size="24" color="#999"></u-icon>
           </view>
        </view>
      </view>
      
      <!-- 更多信息 -->
      <view class="card">
        <view class="section-title">更多信息</view>
        
        <u-form-item label="标签" prop="tags" borderBottom>
          <u-input v-model="form.tags" placeholder="使用逗号分隔, 如: 粘人,可爱" border="none"></u-input>
        </u-form-item>
        
        <u-form-item label="地区" prop="addressInfo" borderBottom>
             <picker mode="region" level="city" @change="handleRegionChange" :value="regionValue">
                 <view class="picker-view">
                     <text v-if="form.province">{{ form.province }} {{ form.city }}</text>
                     <text v-else class="placeholder">请选择省市区</text>
                     <u-icon name="arrow-right" color="#ccc" size="14"></u-icon>
                 </view>
             </picker>
        </u-form-item>
         <u-form-item label="详细地址" prop="address" borderBottom>
           <u-input v-model="form.address" placeholder="详细地址" border="none"></u-input>
         </u-form-item>
        
        <u-form-item label="送养人说" prop="description">
          <u-textarea v-model="form.description" placeholder="请输入宠物的详细介绍或救助故事..." count></u-textarea>
        </u-form-item>
      </view>
      
      <view class="submit-btn-box">
        <u-button type="primary" shape="circle" text="保存修改" :loading="submitting" @click="handleSubmit"></u-button>
      </view>
      
    </u-form>
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
  detailImgList: [], // 这里最终传给后端的是JSON字符串或数组，视后端要求。
                     // 接口文档示例: "detailImgList": ["url1", "url2"]
                     // 但请求参数表说是 array string
  tags: '',
  description: '',
  isSterilized: 0,
  isVaccinated: 0,
  province: '',
  city: '',
  address: ''
})

const isSterilizedBool = ref(false)
const isVaccinatedBool = ref(false)
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
          // 如果有区县信息也可以加上，但API只存了province/city
      }
      
      // 处理布尔值开关
      isSterilizedBool.value = form.isSterilized === 1
      isVaccinatedBool.value = form.isVaccinated === 1
      
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

const handleSterilizedChange = (val) => {
  form.isSterilized = val ? 1 : 0
}

const handleVaccinatedChange = (val) => {
  form.isVaccinated = val ? 1 : 0
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
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 100rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  border-left: 8rpx solid #19be6b;
  padding-left: 16rpx;
}

.upload-box {
  width: 100%;
  height: 300rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  
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
}

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
}

.location-inputs {
    display: flex;
    gap: 20rpx;
    
    .loc-input {
        flex: 1;
    }
}

.submit-btn-box {
  margin-top: 40rpx;
}
</style>
