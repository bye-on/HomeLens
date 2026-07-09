<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const showUserMenu = ref(false)

const isLoginPage = computed(() => route.path === '/login')
const isRegisterPage = computed(() => route.path === '/register')

const goHome = () => {
  router.push('/')
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const goToMyPage = async () => {
  await authStore.getProfile()
  showUserMenu.value = false
  router.push('/mypage')
}

const handleLogout = () => {
  showUserMenu.value = false
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-white border-b border-gray-200">
    <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
      <!-- Logo -->
      <div class="cursor-pointer" @click="goHome">
        <span class="text-2xl font-extrabold text-primary-main"> Home </span>
        <span class="text-2xl font-extrabold text-text-mute"> Lens </span>
      </div>

      <!-- Nav -->
      <nav class="hidden md:flex gap-8">
        <router-link
          to="/result"
          class="text-sm font-medium text-gray-600 hover:text-primary-main transition-colors"
        >
          매물검색
        </router-link>
        <router-link
          to="/board"
          class="text-sm font-medium text-gray-600 hover:text-primary-main transition-colors"
        >
          공지사항
        </router-link>
        <router-link
          v-if="authStore.isAdmin"
          to="/admin"
          class="text-sm font-medium text-gray-600 hover:text-primary-main transition-colors"
        >
          관리자
        </router-link>
      </nav>

      <!-- Actions -->
      <div class="flex items-center gap-2">
        <template v-if="authStore.isLoggedIn">
          <div class="relative">
            <button
              class="flex items-center gap-2 px-1.5 pr-3 py-1.5 bg-gray-100 border border-gray-200 rounded-full hover:border-primary-main transition-colors"
              @click="showUserMenu = !showUserMenu"
            >
              <span
                class="w-8 h-8 rounded-full bg-primary-main text-white flex items-center justify-center text-sm font-semibold"
              >
                {{ authStore.userName.charAt(0) }}
              </span>
              <span class="text-sm font-medium text-gray-900">{{ authStore.userName }}님</span>
              <svg
                class="w-4 h-4 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 9l-7 7-7-7"
                />
              </svg>
            </button>

            <div
              v-if="showUserMenu"
              class="absolute top-full right-0 mt-2 w-40 bg-white border border-gray-200 rounded-xl shadow-lg p-2 animate-in fade-in slide-in-from-top-2 duration-150"
            >
              <button
                class="w-full px-4 py-2.5 text-left text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors"
                @click="goToMyPage"
              >
                마이페이지
              </button>
              <button
                class="w-full px-4 py-2.5 text-left text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100 hover:text-gray-900 transition-colors"
                @click="goToMyPage"
              >
                관심목록
              </button>
              <div class="h-px bg-gray-200 my-2"></div>
              <button
                class="w-full px-4 py-2.5 text-left text-sm font-medium text-red-500 rounded-lg hover:bg-red-50 transition-colors"
                @click="handleLogout"
              >
                로그아웃
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <button
            :class="[
              'hidden md:block px-4 py-2.5 text-sm font-medium rounded-lg transition-colors',
              isLoginPage
                ? 'text-primary-main bg-primary-main/5'
                : 'text-gray-600 hover:text-primary-main',
            ]"
            @click="goToLogin"
          >
            로그인
          </button>
          <button
            :class="[
              'hidden md:block px-4 py-2.5 text-sm font-medium rounded-lg transition-colors',
              isRegisterPage
                ? 'text-primary-main bg-primary-main/5'
                : 'text-gray-600 hover:text-primary-main',
            ]"
            @click="goToRegister"
          >
            회원가입
          </button>
        </template>
      </div>
    </div>
  </header>
</template>
