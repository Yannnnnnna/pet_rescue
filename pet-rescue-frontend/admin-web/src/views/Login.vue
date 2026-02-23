<template>
  <div class="login-container">
    <div class="login-bg-overlay"></div>
    
    <div class="login-box">
      <div class="login-header">
        <div class="logo-icons">
          <img src="../assets/logo.png" alt="logo" class="login-logo-img" />
        </div>
        <h1 class="logo-text">青檐安舍</h1>
        <p class="sub-text">Welcome Back!</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            class="login-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            class="login-input"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码长度不能少于4位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm)
        if (success) {
          ElMessage.success('登录成功')
          router.push('/admin')
        }
      } catch (error) {
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('https://images.unsplash.com/photo-1450778869180-41d0601e046e?ixlib=rb-4.0.3&auto=format&fit=crop&w=1950&q=80');
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.login-bg-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(2px);
}

.login-box {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 40px;
  margin: 20px;
  animation: fadeInZoom 0.3s ease-out;
}

@keyframes fadeInZoom {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icons {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.login-logo-img {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.sub-text {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 300;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-input :deep(.el-input__wrapper) {
  background: #f8fafc;
  border: none;
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.login-input :deep(.el-input__wrapper:focus-within) {
  background: white;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.3);
}

.login-input :deep(.el-input__inner) {
  color: #334155;
  font-size: 14px;
}

.login-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

.input-icon {
  color: #94a3b8;
  font-size: 18px;
  transition: color 0.3s ease;
}

.login-input:focus-within .input-icon {
  color: #10b981;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 2px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #84cc16 0%, #10b981 100%);
  box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.4);
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-btn:hover {
  background: linear-gradient(135deg, #82d616 0%, #0fd991 100%);
  box-shadow: 0 15px 30px -5px rgba(16, 185, 129, 0.5);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.is-loading {
  background: linear-gradient(135deg, #84cc16 0%, #10b981 100%);
}

@media (max-width: 480px) {
  .login-box {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .logo-text {
    font-size: 20px;
  }
}
</style>
