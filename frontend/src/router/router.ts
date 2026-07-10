import { createRouter, createWebHistory } from 'vue-router'

const HomeView = () => import('../views/main/HomeView.vue')
const ResultView = () => import('../views/property/ResultView.vue')
const MyPageView = () => import('../views/auth/MyPageView.vue')
const LoginView = () => import('../views/auth/LoginView.vue')
const RegisterView = () => import('../views/auth/RegisterView.vue')
const NoticeListView = () => import('../views/notice/NoticeListView.vue')
const NoticeDetailView = () => import('../views/notice/NoticeDetailView.vue')
const NoticeWriteView = () => import('../views/notice/NoticeWriteView.vue')
const AdminDashboardView = () => import('../views/admin/AdminDashboardView.vue')
const AdminUsersView = () => import('../views/admin/AdminUsersView.vue')
const AdminPropertiesView = () => import('../views/admin/AdminPropertiesView.vue')
const AdminNoticesView = () => import('../views/admin/AdminNoticesView.vue')

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
