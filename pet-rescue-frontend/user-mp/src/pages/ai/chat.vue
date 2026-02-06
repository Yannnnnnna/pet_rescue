
<template>
  <view class="chat-page">
    <scroll-view 
      class="msg-list" 
      scroll-y 
      :scroll-top="scrollTop" 
      :scroll-into-view="scrollIntoView"
      @scrolltoupper="loadMoreHistory"
    >
      <view class="msg-item" v-for="(msg, index) in msgList" :key="msg.id || index" :id="'msg-' + index">
        <!-- AI 消息 -->
        <view v-if="msg.role === 'assistant' || msg.role === 'system'" class="msg-row msg-left">
          <view class="avatar ai-avatar">🤖</view>
          <view class="msg-content ai-content">
            <up-parse :content="renderMarkdown(msg.content)" :selectable="true"></up-parse>
            <view v-if="msg.isThinking" class="thinking-dots">
              <text>.</text><text>.</text><text>.</text>
            </view>
          </view>
        </view>

        <!-- 用户消息 -->
        <view v-else class="msg-row msg-right">
          <view class="msg-content user-content">
            <text user-select>{{ msg.content }}</text>
          </view>
          <image :src="userInfo.avatar || '/static/default-avatar.png'" class="avatar user-avatar" mode="aspectFill"></image>
        </view>
      </view>
      
      <!-- 底部占位，防止输入框遮挡 -->
      <view class="bottom-spacer" id="bottom-anchor"></view>
    </scroll-view>

    <!-- 宠物引用标签 -->
    <view v-if="selectedPet" class="pet-reference-tag">
      <view class="pet-tag-content">
        <image :src="selectedPet.cover || '/static/default-pet.png'" class="pet-tag-img" mode="aspectFill" @error="handleImageError"></image>
        <text class="pet-tag-name">正在咨询：{{ selectedPet.name }}</text>
        <view class="pet-tag-close" @click="clearPet">×</view>
      </view>
    </view>

    <!-- 底部输入区域 -->
    <view class="input-area">
      <view class="add-pet-btn" @click="showPetPicker">
        <text class="add-icon">+</text>
      </view>
      <view class="input-box">
        <textarea 
          v-model="inputContent" 
          auto-height 
          :maxlength="500"
          placeholder="请输入您的问题..." 
          class="input-field"
          @confirm="handleSend"
          :disabled="isGenerating"
        />
      </view>
      <button 
        class="send-btn" 
        :class="{ disabled: !inputContent.trim() || isGenerating }" 
        @click="handleSend"
        :disabled="!inputContent.trim() || isGenerating"
      >
        <text v-if="!isGenerating">发送</text>
        <view v-else class="loading-spinner"></view>
      </button>
    </view>

    <!-- 宠物选择底部弹窗 -->
    <view class="pet-picker-mask" v-if="showPetPickerFlag" @click="hidePetPicker">
      <view class="pet-picker-content" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择咨询的宠物</text>
          <text class="picker-close" @click="hidePetPicker">×</text>
        </view>
        <scroll-view scroll-y class="pet-list-scroll">
          <view class="pet-list">
            <view 
              v-for="pet in myPets" 
              :key="pet.id" 
              class="pet-item"
              :class="{ active: selectedPet && selectedPet.id === pet.id }"
              @click="selectPet(pet)"
            >
              <image :src="pet.cover || '/static/default-pet.png'" class="pet-item-img" mode="aspectFill" @error="handleImageError"></image>
              <view class="pet-item-info">
                <text class="pet-item-name">{{ pet.name }}</text>
                <text class="pet-item-breed">{{ pet.breed || '未知品种' }}</text>
              </view>
              <view v-if="selectedPet && selectedPet.id === pet.id" class="pet-item-check">
                <text>✓</text>
              </view>
            </view>
            <view v-if="myPets.length === 0" class="pet-empty">
              <text>暂无领养的宠物</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { streamRequest } from '@/utils/StreamRequest'
import { getAiMessageList } from '@/api/ai'
import { getMyInfo } from '@/api/user'
import { getMyAdoptedPets } from '@/api/pet'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true
})

const sessionId = ref('')
const msgList = ref([])
const inputContent = ref('')
const isGenerating = ref(false)
const scrollTop = ref(0)
const scrollIntoView = ref('')
const userInfo = ref({})
const requestTask = ref(null)
const selectedPet = ref(null)
const myPets = ref([])
const showPetPickerFlag = ref(false)

const renderMarkdown = (text) => {
  return text ? md.render(text) : ''
}

onLoad(async (options) => {
  if (options.sessionId) {
    sessionId.value = options.sessionId
  }
  
  if (options.title) {
    uni.setNavigationBarTitle({
      title: decodeURIComponent(options.title)
    })
  }

  loadUserInfo()
  loadMyPets()
  if (sessionId.value) {
    loadHistory()
  } else {
    msgList.value.push({
      role: 'assistant',
      content: '你好！我是你的AI助手，有什么可以帮你的吗？',
      createTime: new Date().toISOString()
    })
  }
})

onUnload(() => {
  if (requestTask.value) {
    requestTask.value.abort()
  }
})

const loadUserInfo = async () => {
  try {
    const res = await getMyInfo()
    userInfo.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const loadMyPets = async () => {
  try {
    const res = await getMyAdoptedPets()
    console.log('领养的宠物数据:', res)
    if (res.code === 200 || res.code === 0) {
      myPets.value = res.data || []
      console.log('myPets:', myPets.value)
    }
  } catch (e) {
    console.error('加载宠物列表失败', e)
  }
}

const showPetPicker = () => {
  showPetPickerFlag.value = true
}

const hidePetPicker = () => {
  showPetPickerFlag.value = false
}

const selectPet = (pet) => {
  selectedPet.value = pet
  hidePetPicker()
}

const clearPet = () => {
  selectedPet.value = null
}

const handleImageError = (e) => {
  console.log('图片加载失败', e)
}

const loadHistory = async () => {
  uni.showLoading({ title: '加载中...' })
  try {
    const res = await getAiMessageList(sessionId.value)
    if ((res.code === 200 || res.code === 0) && res.data) {
      // 这里的列表通常是倒序还是正序需要确认，假设是时间正序
      // 如果后端返回的是倒序，需要 .reverse()
      // 假设后端返回按时间正序
      msgList.value = res.data
      scrollToBottom()
    }
  } catch (e) {
    console.error(e)
  } finally {
    uni.hideLoading()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollIntoView.value = 'bottom-anchor'
    // 为了确保滚动到底部，有时需要手动设置 scrollTop
    // 这里简单用 scrollIntoView
    // 如果不生效，可以延时重试
    setTimeout(() => {
        scrollIntoView.value = 'bottom-anchor'
    }, 100)
  })
}

const handleSend = () => {
  if (!inputContent.value.trim() || isGenerating.value) return

  const question = inputContent.value.trim()
  inputContent.value = ''

  msgList.value.push({
    role: 'user',
    content: question,
    createTime: new Date().toISOString()
  })
  scrollToBottom()

  const aiMsgIndex = msgList.value.length
  msgList.value.push({
    role: 'assistant',
    content: '',
    isThinking: true
  })
  
  isGenerating.value = true
  
  const params = {
    sessionId: sessionId.value,
    question: question,
    enableThinking: true
  }

  if (selectedPet.value) {
    params.petId = selectedPet.value.id
  }

  let fullText = ''
  const baseURL = import.meta.env.VITE_APP_BASE_URL || 'http://192.168.10.135:8080'
  const url = baseURL + '/ai/chat'

  requestTask.value = streamRequest(
    url,
    params,
    (text, isDone) => {
      if (isDone) {
        isGenerating.value = false
        msgList.value[aiMsgIndex].isThinking = false
        requestTask.value = null
        return
      }

      fullText += text
      msgList.value[aiMsgIndex].content = fullText
      scrollToBottom()
    }
  )
}
</script>

<style lang="scss" scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.msg-list {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
  height: 0; // 关键
}

.msg-item {
  margin-bottom: 30rpx;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  max-width: 100%;
}

.msg-left {
  flex-direction: row;
}

.msg-right {
  flex-direction: row;
  justify-content: flex-end;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}

.ai-avatar {
  background-color: #7c3aed;
  color: #fff;
}

.user-avatar {
  background-color: #ddd;
}

.msg-content {
  padding: 24rpx;
  border-radius: 20rpx;
  font-size: 30rpx;
  line-height: 1.6;
  max-width: calc(100% - 160rpx);
  word-break: break-all;
  position: relative;
}

.ai-content {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 4rpx;
}

.user-content {
  background-color: #7c3aed;
  color: #fff;
  border-top-right-radius: 4rpx;
}

.thinking-dots {
  display: inline-block;
  margin-left: 10rpx;
  
  text {
    display: inline-block;
    animation: dot 1.4s infinite ease-in-out both;
    margin: 0 2rpx;
  }
  
  text:nth-child(1) { animation-delay: -0.32s; }
  text:nth-child(2) { animation-delay: -0.16s; }
}

@keyframes dot {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.bottom-spacer {
  height: 20rpx;
}

.pet-reference-tag {
  background-color: #fff;
  padding: 16rpx 20rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .pet-tag-content {
    display: flex;
    align-items: center;
    gap: 16rpx;
    background: #f3e8ff;
    padding: 12rpx 20rpx;
    border-radius: 40rpx;
  }
  
  .pet-tag-img {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
  }
  
  .pet-tag-name {
    flex: 1;
    font-size: 26rpx;
    color: #7c3aed;
    font-weight: 500;
  }
  
  .pet-tag-close {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: #999;
    
    &:active {
      color: #666;
    }
  }
}

.input-area {
  background-color: #fff;
  padding: 20rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.add-pet-btn {
  width: 72rpx;
  height: 72rpx;
  background: #f3e8ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  .add-icon {
    font-size: 40rpx;
    color: #7c3aed;
    font-weight: 300;
    line-height: 1;
  }
  
  &:active {
    background: #e9d5ff;
  }
}

.input-box {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;
  min-height: 40rpx;
}

.input-field {
  width: 100%;
  font-size: 30rpx;
  max-height: 200rpx;
}

.send-btn {
  width: 120rpx;
  height: 72rpx;
  background-color: #7c3aed;
  color: #fff;
  border-radius: 36rpx;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
  
  &.disabled {
    background-color: #d8b4fe;
    opacity: 0.7;
  }
  
  &:active:not(.disabled) {
    transform: scale(0.96);
  }
}

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid #fff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.pet-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.pet-picker-content {
  width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.picker-header {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
  
  .picker-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }
  
  .picker-close {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    color: #999;
    
    &:active {
      color: #666;
    }
  }
}

.pet-list-scroll {
  flex: 1;
  height: 0;
}

.pet-list {
  padding: 20rpx;
}

.pet-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  margin-bottom: 16rpx;
  background: #f8f9fa;
  transition: all 0.2s;
  
  &.active {
    background: #f3e8ff;
    border: 2rpx solid #7c3aed;
  }
  
  &:active {
    transform: scale(0.98);
  }
  
  .pet-item-img {
    width: 100rpx;
    height: 100rpx;
    border-radius: 16rpx;
    flex-shrink: 0;
    background-color: #e9d5ff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .pet-item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    overflow: hidden;
  }
  
  .pet-item-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
  }
  
  .pet-item-breed {
    font-size: 24rpx;
    color: #999;
  }
  
  .pet-item-check {
    width: 48rpx;
    height: 48rpx;
    background: #7c3aed;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28rpx;
    font-weight: bold;
  }
}

.pet-empty {
  padding: 100rpx 0;
  text-align: center;
  
  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
