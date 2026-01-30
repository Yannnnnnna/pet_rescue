<template>
  <view class="chat-container">
    <scroll-view scroll-y class="msg-list" :scroll-top="scrollTop" :scroll-with-animation="true" :style="{ paddingBottom: footerHeight + 'px' }">
      <view class="loading-more" v-if="loading">
        <u-loading-icon mode="circle"></u-loading-icon>
      </view>
      
      <view v-for="(msg, index) in msgList" :key="index" class="msg-group">
        <view class="msg-row" :class="isMsgMe(msg) ? 'right' : 'left'">
          <view class="time-header">{{ formatTime(msg.time) }}</view>
          <view class="msg-body">
             <image v-if="!isMsgMe(msg)" :src="msg.userAvatar" class="avatar"></image>
             
             <view class="content-wrapper">
               <text class="nickname">{{ msg.userNickname }}</text>
               <view class="bubble">
                 <text class="text" v-if="msg.content">{{ msg.content }}</text>
                 <view class="img-grid" v-if="msg.images && msg.images.length">
                    <image 
                      v-for="(img, i) in msg.images" 
                      :key="i"
                      :src="img" 
                      mode="aspectFill" 
                      class="msg-img"
                      @click="previewImage(msg.images, i)"
                    ></image>
                 </view>
               </view>
             </view>
             
             <image v-if="isMsgMe(msg)" :src="msg.userAvatar" class="avatar"></image>
          </view>
        </view>
      </view>
      
      <u-empty v-if="!loading && msgList.length === 0" mode="message" text="暂无咨询记录"></u-empty>
    </scroll-view>

    <!-- 底部输入框 -->
    <view class="chat-footer" id="chatFooter">
      <view class="input-area">
        <view class="icon-btn" @click="handleChooseImage">
          <u-icon name="photo" size="28" color="#606266"></u-icon>
        </view>
        <view class="input-box">
          <input 
            v-model="inputText" 
            type="text" 
            placeholder="请输入内容..." 
            confirm-type="send"
            @confirm="handleSend"
            class="chat-input"
          />
        </view>
        <view class="send-btn" :class="{ 'disabled': !inputText.trim() }" @click="handleSend">
          <text>发送</text>
        </view>
      </view>
      
      <!-- 预览待发送的图片 -->
      <view class="upload-preview" v-if="tempImage">
         <image :src="tempImage" mode="aspectFill" class="preview-img"></image>
         <view class="del-btn" @click="tempImage = ''">
           <u-icon name="close" color="#fff" size="12"></u-icon>
         </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getConsultationHistory, askConsultation, replyConsultation, sendReturnVisit } from '@/api/consultation'
import { getMyInfo, uploadImage } from '@/api/user'
import dayjs from 'dayjs'

const petId = ref(null)
const applicantId = ref(null) // 咨询发起人ID
const currentUserId = ref(null) // 当前登录用户ID

const msgList = ref([]) // 这里存放扁平化后的消息列表
// 原始的成对数据，用于回复时查找上一条消息ID
const rawPairList = ref([]) 

const scrollTop = ref(0)
const loading = ref(false)
const userCache = ref({}) 
const footerHeight = ref(60)

const inputText = ref('')
const tempImage = ref('')

const chatType = ref('') // 聊天类型，如 'return_visit'

onLoad((options) => {
  if (options.petId) {
    petId.value = options.petId
  }
  // 过滤无效的 applicantId 字符串
  if (options.applicantId && options.applicantId !== 'null' && options.applicantId !== 'undefined') {
    applicantId.value = options.applicantId
  }
  if (options.chatType) {
    chatType.value = options.chatType
  }
})

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
.chat-container {
  min-height: 100vh;
  background-color: #ededed;
  display: flex;
  flex-direction: column;
}

.msg-list {
  flex: 1;
  padding: 20rpx 0;
  box-sizing: border-box;
}

.msg-group {
  margin-bottom: 30rpx;
}

.time-header {
  text-align: center;
  font-size: 24rpx;
  color: #b2b2b2;
  margin: 20rpx 0;
}

.msg-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 20rpx;
  padding: 0 20rpx;
  
  .msg-body {
    display: flex;
    align-items: flex-start;
  }

  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 10rpx;
    flex-shrink: 0;
  }
  
  .content-wrapper {
    display: flex;
    flex-direction: column;
    max-width: 70%;
  }

  .nickname {
    font-size: 24rpx;
    color: #b2b2b2;
    margin-bottom: 6rpx;
  }

  .bubble {
    padding: 20rpx;
    border-radius: 10rpx;
    font-size: 30rpx;
    color: #333;
    line-height: 1.5;
    position: relative;
    word-break: break-all;
    min-height: 40rpx;
  }
  
  .img-grid {
    margin-top: 10rpx;
    
    .msg-img {
      width: 200rpx;
      height: 200rpx;
      border-radius: 8rpx;
    }
  }

  /* Left Style (Others) */
  &.left {
    .msg-body {
      flex-direction: row;
    }
    
    .avatar {
      margin-right: 20rpx;
    }
    
    .nickname {
      margin-left: 0;
      text-align: left;
    }
    
    .bubble {
      background: #fff;
      border: 1rpx solid #ededed;
      
      &::before {
        content: '';
        position: absolute;
        left: -16rpx;
        top: 26rpx;
        border: 8rpx solid transparent;
        border-right-color: #fff;
      }
    }
  }

  /* Right Style (Me) */
  &.right {
    .msg-body {
      flex-direction: row;
      justify-content: flex-end; /* Align content to right */
    }
    
    .avatar {
      margin-left: 20rpx;
    }
    
    .content-wrapper {
      align-items: flex-end; /* Align nickname and bubble to right */
    }
    
    .nickname {
      margin-right: 0;
      text-align: right;
    }

    .bubble {
      background: #95ec69;
      
      &::before {
        content: '';
        position: absolute;
        right: -16rpx;
        top: 26rpx;
        border: 8rpx solid transparent;
        border-left-color: #95ec69;
       }
    }
  }
}

.chat-footer {
  background: #f7f7f7;
  padding: 20rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1rpx solid #e5e5e5;
  
  .input-area {
    display: flex;
    align-items: center;
    gap: 20rpx;
    
    .icon-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .input-box {
      flex: 1;
      background: #fff;
      border-radius: 40rpx;
      padding: 0 30rpx;
      height: 70rpx;
      display: flex;
      align-items: center;
      
      .chat-input {
        width: 100%;
        height: 100%;
        font-size: 28rpx;
        border: none;
        outline: none;
      }
    }
    
    .send-btn {
      background: #07c160;
      color: #fff;
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      font-size: 28rpx;
      flex-shrink: 0;
      
      &.disabled {
        background: #c0c4cc;
        opacity: 0.6;
      }
    }
  }
  
  .upload-preview {
    position: relative;
    display: inline-block;
    margin-top: 20rpx;
    
    .preview-img {
      width: 120rpx;
      height: 120rpx;
      border-radius: 8rpx;
    }
    
    .del-btn {
      position: absolute;
      top: -10rpx;
      right: -10rpx;
      background: rgba(0,0,0,0.5);
      border-radius: 50%;
      width: 32rpx;
      height: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>
