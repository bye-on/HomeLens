<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref()
const password = ref()
const errorMessage = ref('')
const isLoading = ref(false)

const handleEmailLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }

  errorMessage.value = ''
  isLoading.value = true

  try {
    await authStore.login(email.value, password.value)
    router.push('/')
  } catch (err) {
    if (axios.isAxiosError(err)) {
      errorMessage.value = '이메일 또는 비밀번호가 올바르지 않습니다.'
    }
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col">
    <!-- Header -->
    <AppHeader />

    <!-- Login Content -->
    <main class="flex-1 flex items-center justify-center px-6 py-16">
      <div
        class="w-full flex flex-col gap-4 max-w-md bg-white border border-gray-200 rounded-2xl p-10"
      >
        <!-- Logo -->
        <div
          class="w-full flex justify-center items-center gap-2 mb-6 cursor-pointer"
          @click="router.push('/')"
        >
          <span class="text-3xl font-extrabold text-primary-main">Home</span>
          <span class="text-3xl font-extrabold text-text-mute">Lens</span>
        </div>

        <!-- Title -->
        <div class="text-center mb-8">
          <p class="text-base text-gray-900 font-semibold">로그인하시고</p>
          <p class="text-base text-gray-600">HomeLens와 함께 방 찾기 여정을 떠나보세요!</p>
        </div>

        <!-- Email Form -->
        <form @submit.prevent="handleEmailLogin" class="space-y-4">
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900">이메일</label>
            <input
              v-model="email"
              type="email"
              placeholder="example@email.com"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:border-primary-main transition-colors"
            />
          </div>
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900">비밀번호</label>
            <input
              v-model="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base text-gray-900 placeholder:text-gray-400 focus:outline-none focus:border-primary-main transition-colors"
            />
          </div>

          <p v-if="errorMessage" class="px-4 py-3 bg-red-50 rounded-lg text-sm text-red-600">
            {{ errorMessage }}
          </p>

          <button
            type="submit"
            class="w-full py-4 bg-primary-main rounded-lg text-base font-semibold text-white hover:bg-secondary-main transition-colors disabled:opacity-60 flex items-center justify-center"
            :disabled="authStore.isLoading"
          >
            <svg
              v-if="authStore.isLoading"
              class="w-5 h-5 animate-spin"
              viewBox="0 0 24 24"
              fill="none"
            >
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              ></circle>
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              ></path>
            </svg>
            <span v-else>로그인</span>
          </button>
        </form>

        <div class="flex justify-center gap-3 text-sm">
          <a href="#" class="text-gray-400 hover:text-primary-main">이메일 찾기</a>
          <span class="text-gray-200">|</span>
          <a href="#" class="text-gray-400 hover:text-primary-main">비밀번호 찾기</a>
        </div>

        <!-- Register Link -->
        <div class="text-center mt-8 pt-6 border-t border-gray-200 text-sm text-gray-400">
          <span>아직 회원이 아니신가요?</span>
          <button
            class="ml-1 text-primary-main font-semibold hover:underline"
            @click="goToRegister"
          >
            가입하기
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
