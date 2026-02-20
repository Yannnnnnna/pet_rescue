<template>
  <view class="publish-page">
    <view class="header-bg"></view>
    
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="close-btn" @click="goBack">
          <uni-icons type="close" size="24" color="#333"></uni-icons>
        </view>
        <text class="nav-title">{{ isEdit ? '编辑日记' : '发布日记' }}</text>
        <view class="placeholder"></view>
      </view>
    </view>

    <view class="content-area" :style="{ paddingTop: (statusBarHeight + 44) + 'px' }">
      <view class="form-section">
        <textarea 
          class="content-input" 
          v-model="content" 
          placeholder="分享你和毛孩子的故事，这一刻的想法..." 
          maxlength="500"
        ></textarea>
      </view>

      <view class="image-section">
        <view class="image-grid">
          <view class="image-item" v-for="(item, index) in fileList" :key="index">
            <image :src="item.url" mode="aspectFill" class="preview-img"></image>
            <view class="delete-btn" @click="deletePic(index)">
              <uni-icons type="close" size="14" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="add-image-btn" v-if="fileList.length < 9" @click="chooseImage">
            <uni-icons type="plusempty" size="36" color="#999"></uni-icons>
            <text>照片/视频</text>
          </view>
        </view>
      </view>

      <view class="option-section">
        <view class="option-item" @click="showPetPicker = true">
          <view class="option-left">
            <uni-icons type="heart-filled" size="22" color="#2E7D32"></uni-icons>
            <text class="option-label">关联宠物</text>
          </view>
          <view class="option-right">
            <text class="option-value">{{ petName || '选择宠物' }}</text>
            <uni-icons type="right" size="18" color="#ccc"></uni-icons>
          </view>
        </view>
      </view>

      <view class="submit-area">
        <view class="submit-btn" :class="{ disabled: loading }" @click="submit">
          <text>{{ isEdit ? '保存修改' : '发布' }}</text>
          <uni-icons type="paperplane" size="20" color="#333"></uni-icons>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addAdoptionPost, getAdoptionPostDetail, updateAdoptionPost } from '@/api/adoption-post'

const statusBarHeight = ref(20)
const loading = ref(false)
const isEdit = ref(false)
const postId = ref('')
const petId = ref('')
const petName = ref('')
const content = ref('')
const fileList = ref([])
const showPetPicker = ref(false)

onLoad((options) => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  
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
      petName.value = data.petName || '关联宠物'
      
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
        status: 'success'
      }))
    }
  } catch (e) {
    console.error(e)
    uni.showToast({ title: '加载详情失败', icon: 'none' })
  }
}

const goBack = () => uni.navigateBack()

const deletePic = (index) => {
  fileList.value.splice(index, 1)
}

const chooseImage = () => {
  const maxCount = 9 - fileList.value.length
  uni.chooseImage({
    count: maxCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePaths = res.tempFilePaths
      for (let i = 0; i < tempFilePaths.length; i++) {
        fileList.value.push({
          url: tempFilePaths[i],
          status: 'uploading'
        })
        try {
          const uploadedUrl = await uploadFile(tempFilePaths[i])
          const idx = fileList.value.findIndex(f => f.url === tempFilePaths[i])
          if (idx !== -1) {
            fileList.value[idx].url = uploadedUrl
            fileList.value[idx].status = 'success'
          }
        } catch (e) {
          const idx = fileList.value.findIndex(f => f.url === tempFilePaths[i])
          if (idx !== -1) {
            fileList.value.splice(idx, 1)
          }
        }
      }
    }
  })
}

const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    const baseURL = import.meta.env.VITE_APP_BASE_URL || 'http://localhost:8080'
    uni.uploadFile({
      url: baseURL + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        satoken: uni.getStorageSync('token')
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200 || data.code === 0) {
            resolve(data.data)
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

const submit = async () => {
  if (loading.value) return
  
  if (!content.value && fileList.value.length === 0) {
    return uni.showToast({ title: '写点什么吧~', icon: 'none' })
  }
  
  loading.value = true
  
  const images = fileList.value.map(item => item.url)
  
  try {
    const postData = {
      petId: petId.value,
      content: content.value,
      images: images.join(',')
    }
    
    let res
    if (isEdit.value) {
      res = await updateAdoptionPost({ id: postId.value, ...postData })
    } else {
      res = await addAdoptionPost(postData)
    }
    
    if (res.code === 200 || res.code === 0) {
      uni.showToast({ title: isEdit.value ? '修改成功' : '发布成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    } else {
      uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
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
  background: #f8f9fa;
  position: relative;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 300rpx;
  background: linear-gradient(180deg, rgba(46, 125, 50, 0.08) 0%, transparent 100%);
  z-index: 0;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: #f8f9fa;
  
  .nav-content {
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
  }
  
  .close-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .placeholder {
    width: 64rpx;
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }
}

.content-area {
  position: relative;
  z-index: 1;
  padding: 30rpx;
  padding-bottom: 200rpx;
}

.form-section {
  margin-bottom: 30rpx;
  
  .content-input {
    width: 100%;
    min-height: 240rpx;
    font-size: 32rpx;
    line-height: 1.6;
    color: #333;
    padding: 20rpx 0;
  }
}

.image-section {
  margin-bottom: 40rpx;
  
  .image-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  
  .image-item {
    width: calc(33.33% - 12rpx);
    aspect-ratio: 1;
    border-radius: 24rpx;
    overflow: hidden;
    position: relative;
    
    .preview-img {
      width: 100%;
      height: 100%;
    }
    
    .delete-btn {
      position: absolute;
      top: 12rpx;
      right: 12rpx;
      width: 44rpx;
      height: 44rpx;
      background: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .add-image-btn {
    width: calc(33.33% - 12rpx);
    aspect-ratio: 1;
    border-radius: 24rpx;
    border: 2rpx dashed #ddd;
    background: #fafafa;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    
    text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.option-section {
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  
  .option-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 30rpx;
    
    .option-left {
      display: flex;
      align-items: center;
      gap: 20rpx;
      
      .option-label {
        font-size: 30rpx;
        color: #333;
        font-weight: 500;
      }
    }
    
    .option-right {
      display: flex;
      align-items: center;
      gap: 12rpx;
      
      .option-value {
        font-size: 28rpx;
        color: #999;
      }
    }
  }
}

.submit-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
  background: rgba(248, 249, 250, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1rpx solid #f0f0f0;
  
  .submit-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #FFC107, #FFD54F);
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    box-shadow: 0 8rpx 24rpx rgba(255, 193, 7, 0.3);
    
    text {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
    
    &.disabled {
      opacity: 0.6;
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
}
</style>
