import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('../views/Admin.vue'),
      meta: { requiresAuth: true },
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '控制台' }
        },
        {
          path: 'pet-list',
          name: 'PetList',
          component: () => import('../views/pet/PetList.vue'),
          meta: { title: '宠物信息管理' }
        },
        {
          path: 'user-list',
          name: 'UserList',
          component: () => import('../views/user/UserList.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'adoption-list',
          name: 'AdoptionList',
          component: () => import('../views/adoption/AdoptionList.vue'),
          meta: { title: '全平台领养监管' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/Profile.vue'),
          meta: { title: '个人中心' }
        },
        {
          path: 'article/list',
          name: 'ArticleList',
          component: () => import('../views/article/ArticleList.vue'),
          meta: { title: '内容生态管理' }
        },
        {
          path: 'article/editor',
          name: 'ArticleEditor',
          component: () => import('../views/article/ArticleEditor.vue'),
          meta: { title: '发布文章' }
        },
        {
          path: 'article/editor/:id',
          name: 'ArticleEdit',
          component: () => import('../views/article/ArticleEditor.vue'),
          meta: { title: '编辑文章', hidden: true }
        }
      ]
    },
    {
      path: '/',
      redirect: '/admin'
    }
  ],
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
