<template>
  <view class="chat-page">
    <!-- 顶部导航 -->
    <view class="chat-header">
      <view class="back-btn" @click="navigateBack">
        <u-icon name="arrow-left" size="22" color="#333"></u-icon>
      </view>
      <text class="nickname">{{ chatTitle }}</text>
      <view class="placeholder"></view> <!-- 占位，使标题居中 -->
    </view>

    <!-- 聊天内容 -->
    <scroll-view 
      scroll-y 
      class="msg-list"
      :scroll-top="scrollTop"
      :style="{ paddingBottom: footerHeight + 'px' }"
    >
      <view v-for="(msg, index) in msgList" :key="index" class="msg-group">
        <view class="time-header" v-if="shouldShowTime(msg, index)">{{ formatTime(msg.time) }}</view>
        <view class="msg-row" :class="isMsgMe(msg) ? 'right' : 'left'">
          <image v-if="!isMsgMe(msg)" :src="msg.userAvatar" class="avatar"></image>
          <view class="bubble">
            <text class="text" v-if="msg.content">{{ msg.content }}</text>
            <image 
              v-if="msg.images && msg.images.length"
              :src="msg.images[0]" 
              mode="aspectFill" 
              class="msg-img"
              @click="previewImage(msg.images, 0)"
            ></image>
          </view>
          <image v-if="isMsgMe(msg)" :src="msg.userAvatar" class="avatar"></image>
        </view>
      </view>
      <u-empty v-if="!loading && msgList.length === 0" mode="message" text="开始你们的对话吧"></u-empty>
    </scroll-view>

    <!-- 底部输入框 -->
    <view class="chat-footer" :style="{ bottom: keyboardHeight + 'px' }" id="chatFooter">
      <view class="input-area">
        <u-icon name="photo" size="28" color="#333" @click="handleChooseImage"></u-icon>
        <input 
          v-model="inputText" 
          type="text" 
          placeholder="说点什么..." 
          class="chat-input"
          :adjust-position="false"
          @focus="onFocus"
          @blur="onBlur"
        />
        <button class="send-btn" :disabled="!inputText.trim() && !tempImage" @click="handleSend">发送</button>
      </view>
      <view class="upload-preview" v-if="tempImage">
        <image :src="tempImage" mode="aspectFill" class="preview-img"></image>
        <view class="del-btn" @click="tempImage = ''">
          <u-icon name="close" color="#fff" size="10"></u-icon>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { getConsultationHistory, askConsultation, replyConsultation, sendReturnVisit } from '@/api/consultation'
import { getMyInfo, getUserDetail, uploadImage } from '@/api/user' // 引入 getUserDetail
import dayjs from 'dayjs'

const petId = ref(null)
const applicantId = ref(null)
const currentUserId = ref(null)

const msgList = ref([])
const rawPairList = ref([]) 

const scrollTop = ref(0)
const loading = ref(false)
const userCache = ref({})
const footerHeight = ref(60)
const keyboardHeight = ref(0)

const inputText = ref('')
const tempImage = ref('')

const chatType = ref('')
const chatTitle = ref('聊天') // 新增：聊天窗口标题

onLoad((options) => {
  if (options.petId) {
    petId.value = options.petId
  }
  if (options.applicantId && options.applicantId !== 'null' && options.applicantId !== 'undefined') {
    applicantId.value = options.applicantId
  }
  if (options.chatType) {
    chatType.value = options.chatType
  }
  if (options.title) {
    chatTitle.value = decodeURIComponent(options.title)
  }
  
  uni.onKeyboardHeightChange(res => {
    keyboardHeight.value = res.height
    if (res.height > 0) {
      scrollToBottom()
    }
  })
})

onUnload(() => {
  uni.offKeyboardHeightChange(() => {})
})

// 新增：返回上一页
const navigateBack = () => {
  uni.navigateBack()
}

// 新增：判断是否显示时间
const shouldShowTime = (message, index) => {
  if (index === 0) return true
  const prevMessage = msgList.value[index - 1]
  const prevTime = dayjs(prevMessage.time)
  const currentTime = dayjs(message.time)
  return currentTime.diff(prevTime, 'minute') > 5
}

// 新增：输入框聚焦
const onFocus = (e) => {
  keyboardHeight.value = e.detail.height
  scrollToBottom()
}

// 新增：输入框失焦
const onBlur = () => {
  keyboardHeight.value = 0
  scrollToBottom()
}

onMounted(async () => {
  await fetchUserInfo()
  // 如果没有传入 applicantId，说明可能是发起新的咨询，默认为当前用户
  // 但如果是回访模式，applicantId 应该是对方的ID，不能默认为自己
  if (!applicantId.value && chatType.value !== 'return_visit') {
    applicantId.value = currentUserId.value
  }
  
  // 再次检查，确保 applicantId 有效才请求
  if (petId.value && applicantId.value) {
    await fetchHistory()
  } else {
    console.warn('缺少必要参数: petId 或 applicantId')
  }
})

// 计算当前角色是否为发起人
const isApplicant = computed(() => {
  return String(currentUserId.value) === String(applicantId.value)
})

// 根据消息类型和角色判断是否是我发的消息
const isMsgMe = (msg) => {
  // 更准确的方式是通过 userId 判断
  if (msg.userId && currentUserId.value) {
      return String(msg.userId) === String(currentUserId.value)
  }
  
  // 降级策略（兼容旧逻辑）
  if (msg.type === 'ask') {
    // 提问消息：如果是申请人，则是我发的；如果是送养人，则是对方发的
    return isApplicant.value
  } else {
    // 回复消息：如果是申请人，则是对方发的；如果是送养人，则是我发的
    return !isApplicant.value
  }
}

const fetchUserInfo = async () => {
  try {
    const res = await getMyInfo()
    if (res.data) {
       currentUserId.value = res.data.id
       // 缓存自己的信息
       userCache.value[res.data.id] = res.data
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const fetchHistory = async () => {
  loading.value = true
  try {
    let dataList = []
    
    if (chatType.value === 'return_visit') {
      // 回访模式下，需要同时获取：
      // 1. 领养人向我发起的咨询 (applicantId = 领养人ID)
      // 2. 我向领养人发起的回访 (applicantId = 我自己的ID)
      // 这样才能看到完整的双向对话
      const [resAdopter, resMe] = await Promise.all([
        getConsultationHistory({
          petId: petId.value,
          applicantId: applicantId.value
        }),
        getConsultationHistory({
          petId: petId.value,
          applicantId: currentUserId.value
        })
      ])
      
      const list1 = resAdopter.data || []
      const list2 = resMe.data || []
      
      // 合并列表，根据ID去重（理论上不会重复，但为了安全）
      const map = new Map()
      list1.forEach(item => map.set(item.id, item))
      list2.forEach(item => map.set(item.id, item))
      
      dataList = Array.from(map.values())
      
    } else {
      // 普通模式
      const res = await getConsultationHistory({
        petId: petId.value,
        applicantId: applicantId.value
      })
      dataList = res.data || []
    }
    
    if (dataList.length > 0 || dataList.length === 0) { // 只要进入了这里就处理，哪怕是空数组也要清空列表
      rawPairList.value = dataList
      
      // 扁平化处理
      let flatMessages = []
      
      dataList.forEach(m => {
        // 缓存用户信息
        if (m.askUserId) {
          userCache.value[m.askUserId] = {
            id: m.askUserId,
            nickname: m.askUserNickname || getUserName(m.askUserId),
            avatar: m.askUserAvatar || getUserAvatar(m.askUserId)
          }
        }
        if (m.replyUserId) {
           userCache.value[m.replyUserId] = {
            id: m.replyUserId,
            nickname: m.replyUserNickname || getUserName(m.replyUserId),
            avatar: m.replyUserAvatar || getUserAvatar(m.replyUserId)
          }
        }
        
        // 1. 提问消息
        if (m.question) {
          flatMessages.push({
            id: m.id, // 用该ID作为唯一标识
            pairId: m.id, // 关联的问答对ID
            type: 'ask',
            content: m.question,
            images: m.askImgList || [],
            time: m.createTime,
            userId: m.askUserId,
            userNickname: m.askUserNickname || getUserName(m.askUserId),
            userAvatar: m.askUserAvatar || getUserAvatar(m.askUserId)
          })
        }
        
        // 2. 回复消息 (如果有)
        if (m.status === 1 || m.answer) {
          flatMessages.push({
            id: m.id + '_reply', // 虚拟ID
            pairId: m.id,
            type: 'reply',
            content: m.answer,
            images: m.replyImgList || [],
            time: m.replyTime || m.createTime, // 如果没有replyTime，兜底
            userId: m.replyUserId,
            userNickname: m.replyUserNickname || getUserName(m.replyUserId),
            userAvatar: m.replyUserAvatar || getUserAvatar(m.replyUserId)
          })
        }
      })
      
      // 按时间正序排序
      flatMessages.sort((a, b) => {
        return new Date(a.time).getTime() - new Date(b.time).getTime()
      })
      
      msgList.value = flatMessages
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取聊天记录失败', error)
  } finally {
    loading.value = false
  }
}

// 图片选择
const handleChooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const size = res.tempFiles[0].size
      if (size > 20 * 1024 * 1024) {
        uni.showToast({ title: '图片不能超过20MB', icon: 'none' })
        return
      }
      tempImage.value = res.tempFilePaths[0]
    }
  })
}

// 发送消息
const handleSend = async () => {
  if (!inputText.value.trim() && !tempImage.value) {
    return
  }
  
  uni.showLoading({ title: '发送中...' })
  try {
    let imgUrl = ''
    if (tempImage.value) {
      imgUrl = await uploadImage(tempImage.value)
    }
    
    const images = imgUrl ? [imgUrl] : []
    
    // 记录发送内容用于本地立即显示
    const content = inputText.value
    // 如果上传了图片，images数组里是服务器地址，可以直接用来显示
    
    if (chatType.value === 'return_visit') {
      // 回访模式：送养人主动发起回访
      // 接口参数: petId, question, images
      await sendReturnVisit({
        petId: String(petId.value),
        question: content,
        images: images
      })
      
      // 本地立即追加消息
      msgList.value.push({
        id: 'local_' + Date.now(),
        type: 'ask',
        content: content,
        images: images,
        time: new Date(),
        userId: currentUserId.value,
        userNickname: getUserName(currentUserId.value),
        userAvatar: getUserAvatar(currentUserId.value)
      })
      
    } else if (isApplicant.value) {
      // 我是领养人 -> 发起提问 (Ask)
      await askConsultation({
        petId: String(petId.value),
        question: content,
        images: images
      })
      
      // 本地立即追加消息
      msgList.value.push({
        id: 'local_' + Date.now(),
        type: 'ask',
        content: content,
        images: images,
        time: new Date(),
        userId: currentUserId.value,
        userNickname: getUserName(currentUserId.value),
        userAvatar: getUserAvatar(currentUserId.value)
      })
      
    } else {
      // 我是送养人 -> 回复提问 (Reply)
      // 需要找到最近的一条未回复的消息进行回复
      // 在 rawPairList 中查找 status=0 的
      
      const unrepliedMsg = rawPairList.value.find(m => m.status === 0)
      if (!unrepliedMsg) {
        uni.hideLoading()
        uni.showToast({ title: '当前没有待回复的消息', icon: 'none' })
        return
      }
      
      await replyConsultation({
        id: unrepliedMsg.id,
        answer: content,
        images: images
      })
      
      // 本地立即追加回复
      msgList.value.push({
        id: unrepliedMsg.id + '_reply',
        pairId: unrepliedMsg.id,
        type: 'reply',
        content: content,
        images: images,
        time: new Date(),
        userId: currentUserId.value,
        userNickname: getUserName(currentUserId.value),
        userAvatar: getUserAvatar(currentUserId.value)
      })
      
      // 标记为已回复，避免重复
      unrepliedMsg.status = 1
    }
    
    // 发送成功，清空
    inputText.value = ''
    tempImage.value = ''
    
    scrollToBottom()
    
    // 延时刷新，确保后端数据落库
    setTimeout(() => {
      fetchHistory()
    }, 500)
    
  } catch (error) {
    console.error('发送失败', error)
    uni.showToast({ title: '发送失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

const getUserAvatar = (uid) => {
  return (userCache.value[uid] && userCache.value[uid].avatar) || '/static/logo.png'
}

const getUserName = (uid) => {
  return (userCache.value[uid] && userCache.value[uid].nickname) || '用户'
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const previewImage = (urls, current) => {
  uni.previewImage({
    urls,
    current
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = 99999
  })
}
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f4f4f4;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: var(--status-bar-height);
  background-color: #fff;
  border-bottom: 1rpx solid #e5e5e5;
  position: sticky;
  top: 0;
  z-index: 10;

  .back-btn, .placeholder {
    width: 60rpx;
  }

  .nickname {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
  }
}

.msg-list {
  flex: 1;
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

.time-header {
  text-align: center;
  font-size: 24rpx;
  color: #999;
  margin: 20rpx 0;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 40rpx;
  gap: 20rpx;

  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .bubble {
    max-width: 70%;
    padding: 20rpx 24rpx;
    border-radius: 20rpx;
    font-size: 30rpx;
    line-height: 1.5;
    word-break: break-all;
  }

  .msg-img {
    width: 300rpx;
    height: 300rpx;
    border-radius: 16rpx;
  }

  &.left {
    .bubble {
      background-color: #fff;
      color: #333;
      border-top-left-radius: 0;
    }
  }

  &.right {
    flex-direction: row-reverse;
    .bubble {
      background-color: #005339; // 主题绿色
      color: #fff;
      border-top-right-radius: 0;
    }
  }
}

.chat-footer {
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #e5e5e5;
  transition: bottom 0.2s ease-in-out;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;

  .input-area {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  .chat-input {
    flex: 1;
    background-color: #f4f4f4;
    border-radius: 40rpx;
    padding: 18rpx 30rpx;
    font-size: 28rpx;
    height: 76rpx;
    box-sizing: border-box;
  }

  .send-btn {
    background-color: #005339;
    color: #fff;
    border: none;
    border-radius: 40rpx;
    padding: 0 40rpx;
    height: 76rpx;
    font-size: 28rpx;
    line-height: 76rpx;
    flex-shrink: 0;

    &[disabled] {
      background-color: #a3d4c4;
      color: #fff;
    }
    
    &::after {
      border: none;
    }
  }
  
  .upload-preview {
    position: relative;
    display: inline-block;
    margin-top: 20rpx;
    
    .preview-img {
      width: 140rpx;
      height: 140rpx;
      border-radius: 16rpx;
    }
    
    .del-btn {
      position: absolute;
      top: -10rpx;
      right: -10rpx;
      background: rgba(0,0,0,0.6);
      border-radius: 50%;
      width: 36rpx;
      height: 36rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>
