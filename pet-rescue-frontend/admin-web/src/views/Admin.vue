<template>
  <el-container class="admin-container">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-left">
        <h2>后台管理系统</h2>
      </div>
      <div class="header-right">
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-main class="admin-main">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>欢迎来到管理页面</span>
          </div>
        </template>
        <div class="welcome-content">
          <p>登录成功！这里是管理页面的内容。</p>
          <p>您可以在这里添加各种管理功能。</p>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 清除 token
    localStorage.removeItem('token')
    
    // 跳转到登录页
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
}

.admin-header {
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
}

.admin-main {
  background-color: #f5f5f5;
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.welcome-content {
  padding: 20px;
  line-height: 2;
  font-size: 16px;
}
</style>
