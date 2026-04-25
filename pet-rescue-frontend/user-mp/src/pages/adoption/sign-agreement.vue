<template>
  <view class="container">
    <view class="nav-bar" :class="{ 'scrolled': isScrolled }" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" :class="{ 'transparent': isScrolled }" @click="goBack">
          <uni-icons type="left" size="20" :color="isScrolled ? '#fff' : '#333'"></uni-icons>
        </view>
        <text class="nav-title" :class="{ 'hidden': isScrolled }">领养协议</text>
        <view class="placeholder"></view>
      </view>
    </view>
    <scroll-view 
      scroll-y 
      class="agreement-scroll" 
      :scroll-top="scrollTop"
      :scroll-with-animation="false"
      @scroll="onScroll"
      :style="{ marginTop: statusBarHeight + 44 + 'px' }"
    >
      <view class="paper-wrapper">
        <view class="paper">
          <view class="paper-decoration top-left"></view>
          <view class="paper-decoration top-right"></view>
          <view class="paper-decoration bottom-left"></view>
          <view class="paper-decoration bottom-right"></view>
          
          <view class="agreement-content">
            <view class="title-section">
              <view class="title-line"></view>
              <text class="agreement-title">宠物领养协议</text>
              <view class="title-line"></view>
            </view>
            
            <view class="contract-number">
              <text>合同编号：PET{{ Date.now() }}</text>
            </view>
            
            <view class="info-section">
              <view class="info-row">
                <text class="label">甲方（送养方/平台）：</text>
                <text class="value underline">流浪动物救助平台</text>
              </view>
              <view class="info-row">
                <text class="label">乙方（领养方）：</text>
                <text class="value underline">{{ userInfo.realName || userInfo.nickname || '________' }}</text>
              </view>
              <view class="info-row">
                <text class="label">联系电话：</text>
                <text class="value underline">{{ userInfo.phone || '________' }}</text>
              </view>
            </view>
            
            <view class="pet-section">
              <view class="section-header">
                <view class="section-icon">🐾</view>
                <text class="section-title">领养宠物信息</text>
              </view>
              <view class="pet-info-grid">
                <view class="pet-info-item">
                  <text class="pet-label">宠物昵称</text>
                  <text class="pet-value">{{ petInfo.name || '未知' }}</text>
                </view>
                <view class="pet-info-item">
                  <text class="pet-label">宠物品种</text>
                  <text class="pet-value">{{ petInfo.breed || '未知' }}</text>
                </view>
                <view class="pet-info-item">
                  <text class="pet-label">宠物性别</text>
                  <text class="pet-value">{{ petInfo.sex === 0 ? '母' : '公' }}</text>
                </view>
              </view>
            </view>
            
            <view class="main-content">
              <text class="intro-text">为了保证宠物得到妥善的照顾，赋予它们一个温暖、安全的家，甲乙双方本着平等自愿、诚实信用的原则，就宠物领养事宜达成以下协议：</text>
              
              <view class="article">
                <view class="article-header">
                  <view class="article-num">一</view>
                  <text class="article-title">领养方（乙方）的承诺与义务</text>
                </view>
                <view class="clause">
                  <text class="clause-num">1.</text>
                  <text class="clause-text"><text class="highlight">科学喂养</text>：承诺为宠物提供充足、清洁的饮水及适合的宠物主粮，不喂食对宠物有害的食物。提供遮风挡雨、安全卫生的居住环境。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">2.</text>
                  <text class="clause-text"><text class="highlight warning">绝不遗弃与虐待</text>：承诺视宠物为家庭成员，无论发生任何情况（如搬家、怀孕、生病等），<text class="warning">绝不遗弃、不虐待、不将宠物作为商业用途或食用</text>。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">3.</text>
                  <text class="clause-text"><text class="highlight">医疗与防疫</text>：承诺每年按时为宠物接种疫苗并进行常规驱虫。当宠物生病或受伤时，必须及时送往正规宠物医院进行治疗。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">4.</text>
                  <text class="clause-text"><text class="highlight">适龄绝育</text>：若领养时宠物未绝育，乙方承诺在宠物达到适宜年龄（或身体条件允许时）自费为其进行绝育手术，以保障宠物健康。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">5.</text>
                  <text class="clause-text"><text class="highlight">文明养宠</text>：承诺遵守当地养犬/养宠管理条例。养狗做到出门牵狗绳、清理粪便；养猫做到家中阳台、窗户安装牢固的防护网（封窗），防止宠物坠楼或走失。</text>
                </view>
              </view>
              
              <view class="article">
                <view class="article-header">
                  <view class="article-num">二</view>
                  <text class="article-title">回访与监督</text>
                </view>
                <view class="clause">
                  <text class="clause-num">1.</text>
                  <text class="clause-text">乙方同意接受甲方的定期回访（主要通过微信视频、照片、聊天记录等线上形式），如实反映宠物的生活和健康状况。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">2.</text>
                  <text class="clause-text">在极特殊情况下，经双方提前沟通协商，甲方可进行非接触式或短暂的上门探访。</text>
                </view>
              </view>
              
              <view class="article">
                <view class="article-header">
                  <view class="article-num">三</view>
                  <text class="article-title">退回与转让限制</text>
                </view>
                <view class="clause">
                  <text class="clause-num">1.</text>
                  <text class="clause-text">乙方承诺不私自将宠物转交、赠予、售卖给第三方。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">2.</text>
                  <text class="clause-text">若乙方因不可抗力因素（如严重疾病等）确实无法继续饲养该宠物，必须第一时间联系甲方，将宠物安全退回给甲方，由甲方重新寻找领养家庭。</text>
                </view>
              </view>
              
              <view class="article">
                <view class="article-header">
                  <view class="article-num">四</view>
                  <text class="article-title">违约责任</text>
                </view>
                <view class="clause">
                  <text class="clause-num">1.</text>
                  <text class="clause-text">若乙方违反上述条款（如遗弃、虐待、私自转让、拒绝回访等），甲方有权单方面无条件收回该宠物，并追究乙方的相关责任。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">2.</text>
                  <text class="clause-text">平台将视违约情节严重程度，对乙方账号进行永久封禁，并保留向相关动物保护组织或执法部门通报的权利。</text>
                </view>
              </view>
              
              <view class="article">
                <view class="article-header">
                  <view class="article-num">五</view>
                  <text class="article-title">附则</text>
                </view>
                <view class="clause">
                  <text class="clause-num">1.</text>
                  <text class="clause-text">本协议自乙方在平台进行电子签名并提交之日起生效。</text>
                </view>
                <view class="clause">
                  <text class="clause-num">2.</text>
                  <text class="clause-text">乙方确认：在签署本协议前，已充分阅读并理解上述所有条款，愿意承担领养宠物的全部责任。</text>
                </view>
              </view>
            </view>
            
            <view class="sign-section">
              <view class="sign-date">
                <text class="date-label">签署日期：</text>
                <text class="date-value">{{ signTime || currentDate }}</text>
              </view>
              
              <!-- 签署模式 -->
              <view class="sign-area" v-if="!isViewMode">
                <view class="sign-header">
                  <view class="sign-label">
                    <view class="sign-icon">✍️</view>
                    <text class="sign-title">乙方签名</text>
                  </view>
                  <view class="clear-btn" @click="clearSignature">
                    <u-icon name="zhongzhi" custom-prefix="custom-icon" size="14" color="#8b7355"></u-icon>
                    <text>重新签名</text>
                  </view>
                </view>
                <view class="canvas-wrapper">
                  <canvas 
                    canvas-id="signatureCanvas" 
                    class="signature-canvas"
                    id="signatureCanvas"
                    disable-scroll="true"
                    @touchstart="touchStart"
                    @touchmove.stop.prevent="touchMove"
                    @touchend="touchEnd"
                  ></canvas>
                  <view class="canvas-placeholder" v-if="!hasSignature">
                    <text>请在此处签名</text>
                  </view>
                  <view class="signature-line"></view>
                </view>
              </view>
              
              <!-- 查看模式：显示已签署的签名 -->
              <view class="sign-area view-mode" v-else>
                <view class="sign-header">
                  <view class="sign-label">
                    <view class="sign-icon">✍️</view>
                    <text class="sign-title">乙方签名</text>
                  </view>
                </view>
                <view class="signature-display">
                  <image v-if="signatureImg" :src="signatureImg" mode="aspectFit" class="signature-img" @click="previewSignature"></image>
                  <text v-else class="no-signature">暂无签名</text>
                </view>
              </view>
            </view>
            
            <view class="stamp-area">
              <view class="stamp" v-if="isViewMode || hasSignature">
                <text>已签署</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="scroll-placeholder" v-if="!isViewMode"></view>
    </scroll-view>
    
    <view class="footer-bar" v-if="!isViewMode">
      <view class="footer-tips">
        <u-icon name="tishi" custom-prefix="custom-icon" size="16" color="#999"></u-icon>
        <text>签署后协议即刻生效，请认真阅读条款</text>
      </view>
      <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
        <u-icon v-if="!submitting" name="duigou" custom-prefix="custom-icon" size="20" color="#fff" style="margin-right: 10rpx;"></u-icon>
        <text>{{ submitting ? '提交中...' : '确认签署' }}</text>
      </button>
    </view>
    
    <view class="footer-bar view-footer" v-if="isViewMode">
      <button class="download-btn" @click="handleDownload">
        <u-icon name="xiazai" custom-prefix="custom-icon" size="20" color="#8b7355" style="margin-right: 10rpx;"></u-icon>
        <text>保存协议</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getMyInfo } from '@/api/user'
import { getPetDetail } from '@/api/pet'
import { signAgreement, getAdoptionApplicationDetail, getMyAdoptionApplications } from '@/api/adoption'
import { uploadImage } from '@/api/user'
import dayjs from 'dayjs'

const applicationId = ref(null)
const petId = ref(null)
const mode = ref('sign')
const isViewMode = ref(false)
const userInfo = ref({})
const petInfo = ref({})
const applicationDetail = ref(null)
const currentDate = ref(dayjs().format('YYYY年MM月DD日'))
const signTime = ref('')
const signatureImg = ref('')
const submitting = ref(false)
const scrollTop = ref(0)
const currentScrollTop = ref(0)
const statusBarHeight = ref(20)
const isScrolled = ref(false)

let ctx = null
let isDrawing = false
let startX = 0
let startY = 0
let hasSignature = ref(false)

onLoad((options) => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20
  
  if (options.applicationId) {
    applicationId.value = options.applicationId
  }
  if (options.petId) {
    petId.value = options.petId
  }
  if (options.mode === 'view') {
    isViewMode.value = true
    const agreementData = getApp().globalData.agreementData
    if (agreementData) {
      signatureImg.value = agreementData.signatureImg || ''
      if (agreementData.signTime) {
        signTime.value = dayjs(agreementData.signTime).format('YYYY年MM月DD日')
      }
      getApp().globalData.agreementData = null
    }
  }
  fetchData()
})

onMounted(() => {
  nextTick(() => {
    initCanvas()
  })
})

const fetchData = async () => {
  try {
    const userRes = await getMyInfo()
    if (userRes.data) {
      userInfo.value = userRes.data
    }
    
    if (petId.value) {
      const petRes = await getPetDetail(petId.value)
      if (petRes.data) {
        petInfo.value = petRes.data
      }
    }
    
    if (isViewMode.value && !signatureImg.value) {
      if (applicationId.value) {
        const detailRes = await getAdoptionApplicationDetail(applicationId.value)
        console.log('detailRes', detailRes)
        if (detailRes.data) {
          signatureImg.value = detailRes.data.signatureImg || ''
          if (detailRes.data.signTime) {
            signTime.value = dayjs(detailRes.data.signTime).format('YYYY年MM月DD日')
          }
          applicationDetail.value = detailRes.data
        }
      } else if (petId.value) {
        const myAppsRes = await getMyAdoptionApplications({ petId: petId.value })
        console.log('myAppsRes', myAppsRes)
        let apps = myAppsRes.data
        if (apps && apps.records) {
          apps = apps.records
        }
        console.log('apps array', apps)
        if (apps && Array.isArray(apps)) {
          const passedApp = apps.find(app => 
            String(app.petId) === String(petId.value) && 
            (app.status === 1 || app.status === 4) && 
            app.agreementStatus === 1
          )
          console.log('passedApp', passedApp)
          if (passedApp) {
            applicationId.value = passedApp.id
            signatureImg.value = passedApp.signatureImg || ''
            if (passedApp.signTime) {
              signTime.value = dayjs(passedApp.signTime).format('YYYY年MM月DD日')
            }
            applicationDetail.value = passedApp
          }
        }
      }
    }
  } catch (e) {
    console.error('获取数据失败', e)
  }
}

const initCanvas = () => {
  ctx = uni.createCanvasContext('signatureCanvas')
  ctx.setLineWidth(4)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
  ctx.setStrokeStyle('#1a1a1a')
}

const onScroll = (e) => {
  currentScrollTop.value = e.detail.scrollTop
  isScrolled.value = e.detail.scrollTop > 50
}

const goBack = () => {
  uni.navigateBack()
}

const touchStart = (e) => {
  isDrawing = true
  hasSignature.value = true
  startX = e.changedTouches[0].x
  startY = e.changedTouches[0].y
  ctx.beginPath()
  ctx.moveTo(startX, startY)
}

const touchMove = (e) => {
  if (!isDrawing) return
  
  const currentX = e.changedTouches[0].x
  const currentY = e.changedTouches[0].y
  
  ctx.lineTo(currentX, currentY)
  ctx.stroke()
  ctx.draw(true)
  
  startX = currentX
  startY = currentY
}

const touchEnd = () => {
  isDrawing = false
}

const clearSignature = () => {
  ctx.clearRect(0, 0, 1000, 500)
  ctx.draw()
  hasSignature.value = false
}

const handleSubmit = async () => {
  if (!hasSignature.value) {
    uni.showToast({ title: '请先签名', icon: 'none' })
    return
  }
  
  submitting.value = true
  
  try {
    const signatureUrl = await getSignatureImage()
    
    if (!signatureUrl) {
      uni.showToast({ title: '签名上传失败', icon: 'none' })
      submitting.value = false
      return
    }
    
    const res = await signAgreement({
      adoptionId: applicationId.value,
      signatureImg: signatureUrl
    })
    
    if (res.code === 200 || res.code === 0) {
      uni.showToast({ title: '签署成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || '签署失败', icon: 'none' })
    }
  } catch (e) {
    console.error('签署失败', e)
    uni.showToast({ title: '签署失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

const getSignatureImage = () => {
  return new Promise((resolve, reject) => {
    uni.canvasToTempFilePath({
      canvasId: 'signatureCanvas',
      fileType: 'png',
      quality: 1,
      success: async (res) => {
        try {
          const url = await uploadImage(res.tempFilePath)
          resolve(url)
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        console.error('canvasToTempFilePath 失败', err)
        reject(err)
      }
    })
  })
}

const previewSignature = () => {
  if (signatureImg.value) {
    uni.previewImage({
      urls: [signatureImg.value],
      current: signatureImg.value
    })
  }
}

const handleDownload = async () => {
  uni.showActionSheet({
    itemList: ['保存签名图片', '截屏保存完整协议'],
    success: (res) => {
      if (res.tapIndex === 0) {
        saveSignatureImage()
      } else if (res.tapIndex === 1) {
        uni.showToast({ title: '请使用手机截屏功能保存', icon: 'none' })
      }
    }
  })
}

const saveSignatureImage = async () => {
  if (!signatureImg.value) {
    uni.showToast({ title: '暂无签名图片', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '保存中...' })
  
  try {
    const res = await new Promise((resolve, reject) => {
      uni.downloadFile({
        url: signatureImg.value,
        success: resolve,
        fail: reject
      })
    })
    
    if (res.statusCode === 200) {
      await new Promise((resolve, reject) => {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({ title: '已保存到相册', icon: 'success' })
            resolve()
          },
          fail: (err) => {
            if (err.errMsg.includes('auth deny')) {
              uni.hideLoading()
              uni.showModal({
                title: '提示',
                content: '需要您授权保存图片到相册',
                confirmText: '去授权',
                success: (res) => {
                  if (res.confirm) {
                    uni.openSetting()
                  }
                }
              })
            } else {
              uni.hideLoading()
              uni.showToast({ title: '保存失败', icon: 'none' })
            }
            reject(err)
          }
        })
      })
    }
  } catch (e) {
    uni.hideLoading()
    console.error('保存失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f0e8 0%, #e8e0d5 100%);
  display: flex;
  flex-direction: column;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #f5f0e8 0%, #e8e0d5 100%);
  transition: all 0.3s;
  
  &.scrolled {
    background: transparent;
  }
  
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
    transition: all 0.3s;
    
    &.transparent {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(10px);
    }
  }
  
  .nav-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    transition: all 0.3s;
    
    &.hidden {
      opacity: 0;
    }
  }
  
  .placeholder {
    width: 60rpx;
  }
}

.agreement-scroll {
  flex: 1;
}

.scroll-placeholder {
  height: 180rpx;
}

.paper-wrapper {
  padding: 30rpx;
}

.paper {
  background: linear-gradient(180deg, #fffef8 0%, #faf6ed 100%);
  border-radius: 8rpx;
  box-shadow: 
    0 4rpx 20rpx rgba(139, 115, 85, 0.15),
    0 0 0 1rpx rgba(139, 115, 85, 0.1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 58rpx,
      rgba(139, 115, 85, 0.03) 58rpx,
      rgba(139, 115, 85, 0.03) 60rpx
    );
    pointer-events: none;
  }
}

.paper-decoration {
  position: absolute;
  width: 60rpx;
  height: 60rpx;
  border: 3rpx double #c4a76c;
  
  &.top-left {
    top: 20rpx;
    left: 20rpx;
    border-right: none;
    border-bottom: none;
  }
  
  &.top-right {
    top: 20rpx;
    right: 20rpx;
    border-left: none;
    border-bottom: none;
  }
  
  &.bottom-left {
    bottom: 20rpx;
    left: 20rpx;
    border-right: none;
    border-top: none;
  }
  
  &.bottom-right {
    bottom: 20rpx;
    right: 20rpx;
    border-left: none;
    border-top: none;
  }
}

.agreement-content {
  padding: 50rpx 40rpx;
  position: relative;
  z-index: 1;
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  padding-bottom: 30rpx;
  border-bottom: 2rpx solid #d4c4a8;
}

.title-line {
  flex: 1;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #c4a76c, transparent);
}

.agreement-title {
  font-size: 44rpx;
  font-weight: bold;
  color: #5c4a2a;
  padding: 0 30rpx;
  letter-spacing: 8rpx;
  font-family: 'SimSun', 'STSong', serif;
}

.contract-number {
  text-align: center;
  margin-bottom: 30rpx;
  
  text {
    font-size: 22rpx;
    color: #999;
    font-family: 'Courier New', monospace;
  }
}

.info-section {
  background: rgba(196, 167, 108, 0.08);
  padding: 25rpx;
  border-radius: 8rpx;
  margin-bottom: 30rpx;
  border-left: 6rpx solid #c4a76c;
}

.info-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 16rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    font-size: 26rpx;
    color: #8b7355;
    flex-shrink: 0;
    width: 240rpx;
  }
  
  .value {
    font-size: 26rpx;
    color: #333;
    flex: 1;
    
    &.underline {
      border-bottom: 1rpx solid #c4a76c;
      padding-bottom: 4rpx;
    }
  }
}

.pet-section {
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  .section-icon {
    font-size: 32rpx;
    margin-right: 10rpx;
  }
  
  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #5c4a2a;
  }
}

.pet-info-grid {
  display: flex;
  background: rgba(196, 167, 108, 0.08);
  border-radius: 8rpx;
  overflow: hidden;
}

.pet-info-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 10rpx;
  border-right: 1rpx solid rgba(196, 167, 108, 0.2);
  
  &:last-child {
    border-right: none;
  }
  
  .pet-label {
    display: block;
    font-size: 22rpx;
    color: #8b7355;
    margin-bottom: 8rpx;
  }
  
  .pet-value {
    display: block;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }
}

.main-content {
  margin-top: 30rpx;
}

.intro-text {
  display: block;
  font-size: 26rpx;
  color: #5c4a2a;
  line-height: 1.8;
  margin-bottom: 30rpx;
  text-indent: 2em;
  text-align: justify;
}

.article {
  margin-bottom: 30rpx;
}

.article-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.article-num {
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, #c4a76c, #a8894a);
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 16rpx;
}

.article-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #5c4a2a;
}

.clause {
  display: flex;
  margin-bottom: 14rpx;
  line-height: 1.8;
  padding-left: 60rpx;
}

.clause-num {
  font-size: 26rpx;
  color: #8b7355;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.clause-text {
  font-size: 26rpx;
  color: #5c4a2a;
  line-height: 1.8;
  text-align: justify;
}

.highlight {
  font-weight: bold;
  color: #5c4a2a;
}

.warning {
  color: #c45656;
  font-weight: 500;
}

.sign-section {
  margin-top: 40rpx;
  padding-top: 30rpx;
  border-top: 2rpx dashed #d4c4a8;
}

.sign-date {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 30rpx;
  
  .date-label {
    font-size: 26rpx;
    color: #8b7355;
  }
  
  .date-value {
    font-size: 26rpx;
    color: #5c4a2a;
    font-weight: 500;
  }
}

.sign-area {
  margin-top: 20rpx;
}

.sign-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.sign-label {
  display: flex;
  align-items: center;
  
  .sign-icon {
    font-size: 28rpx;
    margin-right: 8rpx;
  }
  
  .sign-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #5c4a2a;
  }
}

.clear-btn {
  display: flex;
  align-items: center;
  padding: 10rpx 20rpx;
  background: rgba(196, 167, 108, 0.15);
  border-radius: 24rpx;
  
  text {
    font-size: 24rpx;
    color: #8b7355;
    margin-left: 6rpx;
  }
}

.canvas-wrapper {
  width: 100%;
  height: 280rpx;
  background: #fffef8;
  border: 2rpx solid #d4c4a8;
  border-radius: 8rpx;
  position: relative;
  overflow: hidden;
}

.signature-canvas {
  width: 100%;
  height: 100%;
}

.canvas-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  
  text {
    font-size: 28rpx;
    color: #c4b896;
  }
}

.signature-line {
  position: absolute;
  bottom: 60rpx;
  left: 40rpx;
  right: 40rpx;
  height: 2rpx;
  background: repeating-linear-gradient(
    90deg,
    #d4c4a8,
    #d4c4a8 10rpx,
    transparent 10rpx,
    transparent 20rpx
  );
  pointer-events: none;
}

.sign-area.view-mode {
  .signature-display {
    width: 100%;
    height: 280rpx;
    background: #fffef8;
    border: 2rpx solid #d4c4a8;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .signature-img {
      width: 80%;
      height: 80%;
    }
    
    .no-signature {
      font-size: 28rpx;
      color: #c4b896;
    }
  }
}

.stamp-area {
  position: absolute;
  bottom: 100rpx;
  right: 60rpx;
  
  .stamp {
    width: 120rpx;
    height: 120rpx;
    border: 4rpx solid #c45656;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transform: rotate(-15deg);
    opacity: 0.8;
    
    text {
      font-size: 24rpx;
      color: #c45656;
      font-weight: bold;
    }
  }
}

.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(180deg, rgba(255,254,248,0.95), rgba(250,246,237,0.98));
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(139, 115, 85, 0.1);
  box-sizing: border-box;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}


.footer-bar.view-footer {
  position: relative;
  background: #fff;
  box-shadow: none;
  margin-top: 40rpx;
  padding-bottom: 20rpx; /* Adjusted padding as it's not fixed anymore */
}

.footer-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  
  text {
    font-size: 22rpx;
    color: #999;
    margin-left: 8rpx;
  }
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #c4a76c, #a8894a);
  color: #fff;
  font-size: 32rpx;
  border-radius: 44rpx;
  border: none;
  box-shadow: 0 4rpx 15rpx rgba(196, 167, 108, 0.3);
  
  &[disabled] {
    background: #ccc;
    box-shadow: none;
  }
}

.view-footer {
  background: linear-gradient(180deg, rgba(255,254,248,0.98), rgba(250,246,237,1));
}

.download-btn {
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: #8b7355;
  font-size: 32rpx;
  border-radius: 44rpx;
  border: 2rpx solid #d4c4a8;
  box-shadow: 0 4rpx 15rpx rgba(139, 115, 85, 0.1);
  
  text {
    color: #8b7355;
  }
}
</style>
