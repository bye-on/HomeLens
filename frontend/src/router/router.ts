import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/main/HomeView.vue'
import ResultView from '../views/property/ResultView.vue'
import MyPageView from '../views/auth/MyPageView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import NoticeListView from '../views/notice/NoticeListView.vue'
import NoticeDetailView from '../views/notice/NoticeDetailView.vue'
import NoticeWriteView from '../views/notice/NoticeWriteView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminPropertiesView from '../views/admin/AdminPropertiesView.vue'
import AdminNoticesView from '../views/admin/AdminNoticesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/result',
      name: 'result',
      component: ResultView,
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    // 공지사항
    {
      path: '/board',
      name: 'notice-list',
      component: NoticeListView,
    },
    {
      path: '/board/write',
      name: 'notice-write',
      component: NoticeWriteView,
    },
    {
      path: '/board/:id',
      name: 'notice-detail',
      component: NoticeDetailView,
    },
    {
      path: '/board/:id/edit',
      name: 'notice-edit',
      component: NoticeWriteView,
    },
    // 어드민
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: AdminDashboardView,
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsersView,
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/properties',
      name: 'admin-properties',
      component: AdminPropertiesView,
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/notices',
      name: 'admin-notices',
      component: AdminNoticesView,
      meta: { requiresAdmin: true },
    },
  ],
})

export default router
