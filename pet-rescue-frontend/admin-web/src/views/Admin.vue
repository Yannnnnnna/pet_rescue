<template>
  <el-container class="admin-container">
    <el-header class="admin-header">
      <div class="header-left">
        <div class="logo-wrapper">
          <img src="../assets/logo.png" alt="logo" class="logo-img" />
        </div>
        <h2 class="logo-text">青檐安舍——宠物救助领养平台</h2>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <div class="user-info-wrapper">
            <div class="user-avatar">
              <el-icon><User /></el-icon>
            </div>
            <span class="user-name">管理员</span>
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/admin/profile')">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <el-aside width="260px" class="admin-aside">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
        >
          <el-menu-item index="/admin/dashboard" class="menu-item-dashboard">
            <el-icon><DataLine /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/pet-list">
            <img src="../assets/pet-icon.png" alt="pet" class="menu-icon-img" />
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
          
          <el-sub-menu index="content" class="sub-menu-item">
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
          
          <el-sub-menu index="community" class="sub-menu-item">
            <template #title>
              <el-icon><ChatDotRound /></el-icon>
              <span>社区管理</span>
            </template>
            <el-menu-item index="/admin/community/diary-audit">
              <el-icon><EditPen /></el-icon>
              <span>领养日记审核</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
        
        <div class="sidebar-footer">
          <div class="admin-card">
            <div class="admin-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div class="admin-info">
              <p class="admin-name">Admin</p>
              <p class="admin-role">超级管理员</p>
            </div>
            <el-button class="logout-btn" link @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </div>
        </div>
      </el-aside>
      
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
import { ArrowDown, User, Document, Checked, ChatDotRound, EditPen, Picture, SwitchButton, DataLine } from '@element-plus/icons-vue'
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
  }
}
</script>

<style scoped>
.admin-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
}

.admin-header {
  background: linear-gradient(135deg, #10b981 0%, #84cc16 100%);
  border-bottom: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-wrapper {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-icon {
  font-size: 24px;
  color: white;
  transform: rotate(-10deg);
}

.logo-text {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  transition: all 0.3s ease;
}

.user-info-wrapper:hover {
  background: rgba(255, 255, 255, 0.25);
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #34d399 0%, #a3e635 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.user-avatar .el-icon {
  font-size: 18px;
  color: white;
}

.user-name {
  color: white;
  font-weight: 500;
  font-size: 14px;
}

.dropdown-arrow {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.admin-aside {
  background: white;
  border-right: 1px solid #e2e8f0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

.sidebar-menu {
  border-right: none;
  padding: 16px 12px;
  flex: 1;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  margin-bottom: 4px;
  color: #64748b;
  font-weight: 500;
  transition: all 0.2s ease;
}

.sidebar-menu .el-menu-item .menu-icon-img {
  width: 18px;
  height: 18px;
  object-fit: contain;
  margin-right: 8px;
  filter: brightness(0) saturate(100%) invert(44%) sepia(12%) saturate(726%) hue-rotate(176deg) brightness(96%) contrast(89%);
  transition: filter 0.2s ease;
}

.sidebar-menu .el-menu-item:hover .menu-icon-img {
  filter: brightness(0) saturate(100%) invert(57%) sepia(56%) saturate(467%) hue-rotate(108deg) brightness(92%) contrast(91%);
}

.sidebar-menu .el-menu-item.is-active .menu-icon-img {
  filter: brightness(0) saturate(100%) invert(100%);
}

.sidebar-menu .el-menu-item:hover {
  background-color: #f1f5f9;
  color: #10b981;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #10b981 0%, #84cc16 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: white;
}

.sidebar-menu .el-sub-menu {
  margin-bottom: 4px;
}

.sidebar-menu .el-sub-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  border-radius: 12px;
  color: #64748b;
  font-weight: 500;
  transition: all 0.2s ease;
}

.sidebar-menu .el-sub-menu :deep(.el-sub-menu__title:hover) {
  background-color: #f1f5f9;
  color: #10b981;
}

.sidebar-menu .el-sub-menu.is-active :deep(.el-sub-menu__title) {
  color: #10b981;
}

.sidebar-menu .el-sub-menu .el-menu-item {
  min-width: auto;
  padding-left: 52px !important;
  height: 42px;
  line-height: 42px;
  font-size: 13px;
}

.sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background: #ecfdf5;
  color: #10b981;
  box-shadow: none;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
}

.admin-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #d1fae5 0%, #dcfce7 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-avatar .el-icon {
  font-size: 20px;
  color: #10b981;
}

.admin-info {
  flex: 1;
  min-width: 0;
}

.admin-name {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.admin-role {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

.logout-btn {
  color: #94a3b8;
  padding: 8px;
}

.logout-btn:hover {
  color: #ef4444;
}

.admin-main {
  background-color: #f1f5f9;
  padding: 24px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
