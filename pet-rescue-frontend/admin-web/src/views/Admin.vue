<template>
  <el-container class="admin-container">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-left">
        <div class="logo">🐾</div>
        <h2>萌宠救援后台</h2>
      </div>
      <div class="header-right">
        <el-dropdown>
          <span class="el-dropdown-link">
            管理员 <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/admin/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="220px" class="admin-aside">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          router
        >
          <el-menu-item index="/admin/pet-list">
            <el-icon><List /></el-icon>
            <span>宠物信息管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user-list">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/adoption-list">
            <el-icon><Checked /></el-icon>
            <span>全平台领养监管</span>
          </el-menu-item>
          <el-sub-menu index="/admin/content">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容生态管理</span>
            </template>
            <el-menu-item index="/admin/article/list">
              <el-icon><EditPen /></el-icon>
              <span>文章管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/content/banner">
              <el-icon><Picture /></el-icon>
              <span>轮播图管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="/admin/community">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>社区管理</span>
            </template>
            <el-menu-item index="/admin/community/diary-audit">
              <el-icon><EditPen /></el-icon>
              <span>领养日记审核</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 预留其他菜单 -->
        </el-menu>
      </el-aside>
      
      <!-- 主体内容 -->
      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, List, User, Document, Checked, ChatDotRound, EditPen, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 24px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.admin-aside {
  background-color: #fff;
  border-right: 1px solid #dcdfe6;
  overflow-y: auto;
}

.el-menu-vertical {
  border-right: none;
}

.admin-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

/* Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
