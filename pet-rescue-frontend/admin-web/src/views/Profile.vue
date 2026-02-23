<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="profile-header">
        <div class="header-content">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img v-if="infoForm.avatar" :src="infoForm.avatar" class="user-avatar" />
              <div v-else class="avatar-placeholder">
                <el-icon><User /></el-icon>
              </div>
            </div>
            <div class="user-info">
              <h2 class="user-name">{{ infoForm.nickname || username || '管理员' }}</h2>
              <p class="user-role">系统管理员</p>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-body">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="个人信息" name="info">
            <div class="info-content">
              <el-form
                ref="infoFormRef"
                :model="infoForm"
                :rules="infoRules"
                label-width="100px"
                class="info-form"
              >
                <el-form-item label="头像" prop="avatar">
                  <el-upload
                    class="avatar-uploader"
                    action="/api/file/upload"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                    :headers="uploadHeaders"
                  >
                    <img v-if="infoForm.avatar" :src="infoForm.avatar" class="avatar-preview" />
                    <div v-else class="avatar-upload-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>上传头像</span>
                    </div>
                  </el-upload>
                  <div class="upload-tip">支持 JPG、PNG 格式，大小不超过 20MB</div>
                </el-form-item>
                
                <el-form-item label="用户名">
                  <el-input v-model="username" disabled class="form-input" />
                </el-form-item>

                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" class="form-input" />
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号" class="form-input" />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="submitInfo" class="btn-save">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="修改密码" name="password">
            <div class="password-content">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                class="password-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入旧密码"
                    show-password
                    class="form-input"
                  />
                </el-form-item>

                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    show-password
                    class="form-input"
                  />
                </el-form-item>

                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                    class="form-input"
                  />
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="submitPassword" class="btn-save">修改密码</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyInfo, updateUserInfo, updatePassword } from '../api/user'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const activeTab = ref('info')
const loading = ref(false)
const username = ref('')

// --- 上传相关 ---
const uploadHeaders = {
  token: userStore.token // 假设后端需要token鉴权，通常放在header中
}

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像图片必须是 JPG 或 PNG 格式!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 20) {
    ElMessage.error('头像图片大小不能超过 20MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = (response, uploadFile) => {
  // 根据实际接口返回调整，通常 response.data 是 url
  if (response.code === 200 || response.code === 0) {
      infoForm.avatar = response.data
  } else {
      ElMessage.error('上传失败')
  }
}

// --- 个人信息相关 ---
const infoFormRef = ref(null)
const infoForm = reactive({
  nickname: '',
  phone: '',
  avatar: ''
})

const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const fetchUserInfo = async () => {
  try {
    const res = await getMyInfo()
    if (res.code === 200) {
      const data = res.data
      username.value = data.username
      infoForm.nickname = data.nickname
      infoForm.phone = data.phone
      infoForm.avatar = data.avatar
    }
  } catch (error) {
    console.error(error)
  }
}

const submitInfo = async () => {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await updateUserInfo(infoForm)
        if (res.code === 200) {
          ElMessage.success('修改成功')
          fetchUserInfo()
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// --- 修改密码相关 ---
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const submitPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await updatePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
          passwordFormRef.value.resetFields()
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        console.error(error)
        ElMessage.error('修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f1f5f9;
  overflow: hidden;
  width: 100%;
  max-width: 800px;
}

.profile-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  padding: 30px 40px;
}

.header-content {
  display: flex;
  align-items: center;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 32px;
}

.user-info {
  color: white;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.user-role {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.profile-body {
  padding: 20px 40px 30px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background-color: #10b981;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  padding: 0 20px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #10b981;
}

.profile-tabs :deep(.el-tabs__item:hover) {
  color: #10b981;
}

.info-content, .password-content {
  max-width: 500px;
}

.info-form, .password-form {
  margin-top: 10px;
}

.form-input :deep(.el-input__wrapper) {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.2s ease;
}

.form-input :deep(.el-input__wrapper:focus-within) {
  border-color: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
}

.form-input :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e2e8f0;
}

.avatar-upload-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px dashed #e2e8f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-upload-placeholder:hover {
  border-color: #10b981;
  color: #10b981;
}

.avatar-upload-placeholder .el-icon {
  font-size: 20px;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

.btn-save {
  background: #10b981;
  border-color: #10b981;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
}

.btn-save:hover {
  background: #059669;
  border-color: #059669;
}
</style>
