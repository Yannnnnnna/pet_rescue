<template>
  <view class="container">
    <!-- Navbar -->
    <u-navbar
      :autoBack="true"
      bgColor="transparent"
      leftIconColor="#fff"
      :fixed="true"
      :placeholder="false"
      title=""
    ></u-navbar>

    <!-- Hero Section -->
    <view class="hero-section">
      <image class="hero-bg" src="../../static/领养流程背景.png" mode="aspectFill"></image>
      <view class="hero-overlay">
        <view class="tag">GUIDE</view>
        <view class="title-group">
          <text class="title-cn">领养流程说明</text>
          <text class="title-en">Adoption Process</text>
        </view>
      </view>
    </view>

    <!-- Intro -->
    <view class="intro-section">
      <text class="intro-text">每一个生命都值得被温柔以待。通过我们严谨而温馨的6步流程，为您和毛孩子找到彼此的一生挚爱。</text>
    </view>

    <!-- Timeline -->
    <view class="timeline-section">
      <view class="step-item" v-for="(step, index) in steps" :key="index" :class="{ 'last-step': index === steps.length - 1 }">
        <!-- Connector Line -->
        <view class="connector" v-if="index !== steps.length - 1"></view>
        
        <!-- Number/Icon -->
        <view class="step-icon-wrapper" :class="{ 'final-step': index === steps.length - 1 }">
          <text v-if="index !== steps.length - 1" class="step-num">{{ index + 1 }}</text>
          <u-icon v-else name="shouye" custom-prefix="custom-icon" color="#fff" size="24"></u-icon>
        </view>

        <!-- Content Card -->
        <view class="step-card">
          <view class="card-header">
            <text class="card-title">{{ step.title }}</text>
            <u-icon :name="step.icon" custom-prefix="custom-icon" color="#ccc" size="24"></u-icon>
          </view>
          <text class="card-desc">{{ step.desc }}</text>
          <view class="card-tag-wrapper">
            <view class="card-tag">
              <text class="tag-text">{{ step.tag }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- FAQ -->
    <view class="faq-section">
      <view class="section-title">
        <view class="indicator"></view>
        <text class="title-text">常见问题</text>
      </view>
      
      <view class="faq-list">
        <view class="faq-item" v-for="(item, index) in faqs" :key="index" @click="toggleFaq(index)">
          <view class="faq-header">
            <text class="faq-question">{{ item.question }}</text>
            <u-icon :name="item.open ? 'arrow-up' : 'arrow-down'" color="#2E4F2F" size="16"></u-icon>
          </view>
          <view class="faq-answer" v-if="item.open">
            <text>{{ item.answer }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Footer CTA -->
    <view class="footer-cta-placeholder"></view>
    <view class="footer-cta">
      <button class="start-btn" @click="goHome">
        <text class="btn-text">开始寻找伙伴</text>
        <u-icon name="arrow-right" color="#2E4F2F" size="20" bold></u-icon>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const steps = ref([
  {
    title: '在线选宠',
    icon: 'search',
    desc: '浏览我们的宠物数据库，查看照片和详细档案，寻找那个让您心动的伙伴。',
    tag: 'Online Selection'
  },
  {
    title: '提交申请',
    icon: 'shenqing',
    desc: '填写详细的领养问卷，包括您的居住环境、养宠经验和生活习惯。',
    tag: 'Submit Application'
  },
  {
    title: 'AI匹配与审核',
    icon: 'jiqiren',
    desc: '智能系统初步评估匹配度，随后由人工志愿者进行细致审核。',
    tag: 'AI Matching & Review'
  },
  {
    title: '线下见面',
    icon: 'account-fill',
    desc: '审核通过后，预约前往基地或寄养家庭，与宠物进行面对面的亲密接触。',
    tag: 'Offline Meetup'
  },
  {
    title: '签署协议',
    icon: 'edit-pen',
    desc: '双方确认意向，正式签署具有法律效力的领养协议，承诺不离不弃。',
    tag: 'Sign Agreement'
  },
  {
    title: '回访反馈',
    icon: 'heart-fill',
    desc: '领养后我们会定期回访，确保宠物适应新家庭，并提供必要的养宠指导。',
    tag: 'Home Visit'
  }
])

const faqs = ref([
  {
    question: '领养是免费的吗？',
    answer: '领养本身不收取费用，但为了保障宠物的健康，我们可能会收取少量的押金或疫苗绝育费用，具体视宠物情况而定。',
    open: false
  },
  {
    question: '如果是租房可以领养吗？',
    answer: '可以，但需要征得房东同意，并确保居住环境适合宠物生活，如有安装封窗等安全措施。',
    open: false
  },
  {
    question: '如果领养后不合适怎么办？',
    answer: '我们有试养期（通常为7-14天）。如果在试养期间发现确实不合适，可以将宠物退回基地，不可私自转送。',
    open: false
  }
])

const toggleFaq = (index) => {
  faqs.value[index].open = !faqs.value[index].open
}

const goHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f8f8f5;
  padding-bottom: 40rpx;
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
}

/* Hero Section */
.hero-section {
  position: relative;
  width: 100%;
  height: 500rpx;
  border-bottom-left-radius: 48rpx;
  border-bottom-right-radius: 48rpx;
  overflow: hidden;
  background-color: #2E4F2F;
  box-shadow: 0 8rpx 30rpx rgba(46, 79, 47, 0.2);
}

.hero-bg {
  width: 100%;
  height: 100%;
  opacity: 0.8;
  mix-blend-mode: overlay;
}

.hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 60rpx 40rpx;
  background: linear-gradient(to top, #2E4F2F, transparent);
}

.tag {
  display: inline-block;
  padding: 6rpx 16rpx;
  background-color: #f9cc06;
  color: #2E4F2F;
  font-size: 24rpx;
  font-weight: bold;
  border-radius: 100rpx;
  margin-bottom: 20rpx;
  letter-spacing: 2rpx;
}

.title-group {
  display: flex;
  flex-direction: column;
}

.title-cn {
  color: #fff;
  font-size: 56rpx;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 8rpx;
}

.title-en {
  color: #f9cc06;
  font-size: 36rpx;
  font-weight: 500;
}

/* Intro Section */
.intro-section {
  padding: 40rpx 48rpx;
}

.intro-text {
  font-size: 28rpx;
  color: #5d6d60;
  line-height: 1.6;
}

/* Timeline Section */
.timeline-section {
  padding: 0 48rpx;
}

.step-item {
  position: relative;
  padding-left: 80rpx;
  padding-bottom: 40rpx;
  
  &.last-step {
    padding-bottom: 0;
  }
}

.connector {
  position: absolute;
  left: 28rpx; /* (60rpx icon width / 2) - 2rpx width */
  top: 60rpx;
  bottom: -20rpx;
  width: 4rpx;
  border-left: 4rpx dashed rgba(46, 79, 47, 0.3);
}

.step-icon-wrapper {
  position: absolute;
  left: 0;
  top: 0;
  width: 60rpx;
  height: 60rpx;
  background-color: #f9cc06;
  border: 6rpx solid #f8f8f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
  
  .step-num {
    color: #2E4F2F;
    font-size: 32rpx;
    font-weight: bold;
  }
  
  &.final-step {
    background-color: #2E4F2F;
    border-color: #f8f8f5;
  }
}

.step-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
  border: 1px solid #eee;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2E4F2F;
}

.card-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: block;
  margin-bottom: 20rpx;
}

.card-tag-wrapper {
  display: flex;
}

.card-tag {
  background-color: rgba(249, 204, 6, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  
  .tag-text {
    font-size: 22rpx;
    color: #2E4F2F;
    font-weight: 600;
  }
}

/* FAQ Section */
.faq-section {
  padding: 60rpx 48rpx;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 40rpx;
  
  .indicator {
    width: 10rpx;
    height: 40rpx;
    background-color: #f9cc06;
    border-radius: 10rpx;
    margin-right: 20rpx;
  }
  
  .title-text {
    font-size: 36rpx;
    font-weight: bold;
    color: #2E4F2F;
  }
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.faq-item {
  background-color: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
  border: 1px solid #eee;
}

.faq-header {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.faq-question {
  font-size: 28rpx;
  font-weight: bold;
  color: #2E4F2F;
  flex: 1;
  padding-right: 20rpx;
}

.faq-answer {
  padding: 0 30rpx 30rpx 30rpx;
  
  text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
}

/* Footer CTA */
.footer-cta-placeholder {
  height: 160rpx;
}

.footer-cta {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  box-sizing: border-box;
  background-color: rgba(248, 248, 245, 0.9);
  backdrop-filter: blur(10px);
  padding: 30rpx 48rpx 60rpx 48rpx;
  z-index: 100;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.start-btn {
  width: 100%;
  margin: 0;
  background-color: #f9cc06;
  color: #2E4F2F;
  border-radius: 100rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 8rpx 20rpx rgba(249, 204, 6, 0.3);
  border: none;
  
  &:active {
    transform: scale(0.98);
  }
  
  .btn-text {
    font-size: 32rpx;
    font-weight: 800;
  }
}
</style>