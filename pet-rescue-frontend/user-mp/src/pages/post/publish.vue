<template>
  <view class="publish-page">
    <u-navbar :title="isEdit ? '编辑日记' : '发布日记'" :autoBack="true" bgColor="#fff" leftIconColor="#333" titleStyle="color: #333; font-weight: 600;"></u-navbar>
    
    <view class="content-area" :style="{ paddingTop: navHeight + 'px' }">
      <!-- 关联宠物 -->
      <view class="form-card">
        <view class="card-title">关联宠物</view>
        <view class="pet-info-row">
          <u-icon name="chongwuzhaozhu" custom-prefix="custom-icon" color="#19be6b" size="20"></u-icon>
          <text class="pet-name">{{ petName }}</text>
          <u-tag text="已领养" type="success" plain size="mini" shape="circle" class="status-tag"></u-tag>
        </view>
      </view>

      <!-- 内容输入 -->
      <view class="form-card">
        <view class="card-title">记录美好生活</view>
        <u-textarea 
          v-model="content" 
          placeholder="记录下它的萌趣瞬间吧... (限500字)" 
          count 
          maxlength="500" 
          height="200"
          border="none"
          customStyle="background-color: #f8f8f8; padding: 20rpx; border-radius: 12rpx;"
        ></u-textarea>
      </view>

      <!-- 图片上传 -->
      <view class="form-card">
        <view class="card-title">添加照片 (最多9张)</view>
        <view class="upload-box">
          <u-upload
            :fileList="fileList"
            @afterRead="afterRead"
            @delete="deletePic"
            multiple
            :maxCount="9"
            width="200"
            height="200"
          ></u-upload>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="btn-area">
        <u-button 
          :text="isEdit ? '保存修改' : '发布日记'" 
          color="linear-gradient(to right, #19be6b, #4cd964)" 
          shape="circle" 
          :loading="loading" 
          @click="submit"
        ></u-button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addAdoptionPost, getAdoptionPostDetail, updateAdoptionPost } from '@/api/adoption-post'

const navHeight = 44 + (uni.getSystemInfoSync().statusBarHeight || 0)
const loading = ref(false)
const isEdit = ref(false)
const postId = ref('')
const petId = ref('')
const petName = ref('')
const content = ref('')
const fileList = ref([])

onLoad((options) => {
  if (options.id) {
    isEdit.value = true
    postId.value = options.id
    fetchDetail(options.id)
  } else if (options.petId) {
    petId.value = options.petId
    petName.value = options.petName || '未知宠物'
  }
})

const fetchDetail = async (id) => {
  try {
    const res = await getAdoptionPostDetail(id)
    if (res.data) {
      const data = res.data
      content.value = data.content
      petId.value = data.petId
      petName.value = data.petName || '关联宠物' // 后端可能未返回petName
      
      // 处理图片回显
      let imageUrls = []
      if (data.images) {
        if (typeof data.images === 'string') {
          try {
            imageUrls = JSON.parse(data.images)
          } catch (e) {
            imageUrls = data.images.split(',')
          }
        } else if (Array.isArray(data.images)) {
          imageUrls = data.images
        }
      }
      
      fileList.value = imageUrls.map(url => ({
        url: url,
        status: 'success',
        message: ''
      }))
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载详情失败', icon: 'none' })
  }
}

// 删除图片
const deletePic = (event) => {
  fileList.value.splice(event.index, 1)
}

// 新增图片
const afterRead = async (event) => {
  let lists = [].concat(event.file)
  let fileListLen = fileList.value.length
  lists.map((item) => {
    fileList.value.push({
      ...item,
      status: 'uploading',
      message: '上传中'
    })
  })
  
  for (let i = 0; i < lists.length; i++) {
    const result = await uploadFilePromise(lists[i].url)
    let item = fileList.value[fileListLen]
    fileList.value.splice(fileListLen, 1, Object.assign(item, {
      status: 'success',
      message: '',
      url: result
    }))
    fileListLen++
  }
}

// 上传文件 Promise
const uploadFilePromise = (url) => {
  return new Promise((resolve, reject) => {
    const baseURL = import.meta.env.VITE_APP_BASE_URL || 'http://localhost:8080'
    uni.uploadFile({
      url: baseURL + '/file/upload', 
      filePath: url,
      name: 'file',
      header: {
        satoken: uni.getStorageSync('token')
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200 || data.code === 0) {
            resolve(data.data) // 假设后端直接返回url字符串
          } else {
            uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
            reject(data.msg)
          }
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 提交发布
const submit = async () => {
  if (!content.value && fileList.value.length === 0) {
    return uni.showToast({ title: '写点什么吧~', icon: 'none' })
  }
  
  loading.value = true
  
  // 提取图片URL
  const images = fileList.value.map(item => item.url)
  
  try {
    let res;
    const postData = {
      petId: petId.value,
      content: content.value,
      images: images.join(',') // API要求逗号分隔的字符串
    }
    
    if (isEdit.value) {
      res = await updateAdoptionPost({
        id: postId.value,
        ...postData
      })
    } else {
      res = await addAdoptionPost(postData)
    }
    
    if (res.code === 200 || res.code === 0) {
      uni.showToast({ title: isEdit.value ? '修改成功' : '发布成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || (isEdit.value ? '修改失败' : '发布失败'), icon: 'none' })
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '系统错误', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #e6f2e6, #f5f5f5);
}

.content-area {
  padding: 30rpx;
}

.form-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 32rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 16rpx rgba(0,0,0,0.05);
  backdrop-filter: blur(10px);
  
  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
    position: relative;
    padding-left: 20rpx;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 6rpx;
      height: 24rpx;
      background: #19be6b;
      border-radius: 4rpx;
    }
  }
}

.pet-info-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #e6f2e6;
  border-radius: 12rpx;
  
  .pet-name {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .status-tag {
    margin-left: auto;
  }
}

.upload-box {
  padding: 10rpx 0;
}

.btn-area {
  margin-top: 60rpx;
  padding: 0 20rpx;
}
</style>