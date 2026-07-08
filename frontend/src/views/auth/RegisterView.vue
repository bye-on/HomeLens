<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'
import AppHeader from '@/components/common/AppHeader.vue'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const passwordConfirm = ref('')
const name = ref('')
const phone = ref('')

const emailErrorMessage = ref('')
const passwordErrorMessage = ref('')
const passwordConfirmErrorMessage = ref('')

// 에러 메시지 초기화
const clearErrors = () => {
  emailErrorMessage.value = ''
  passwordErrorMessage.value = ''
  passwordConfirmErrorMessage.value = ''
}

// 유효성 검사
const validate = (): boolean => {
  clearErrors()
  let isValid = true

  if (!email.value) {
    emailErrorMessage.value = '이메일을 입력해주세요.'
    isValid = false
  } else {
    // 이메일 형식 검사
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email.value)) {
      emailErrorMessage.value = '올바른 이메일 형식이 아닙니다.'
      isValid = false
    }
  }

  if (!password.value) {
    passwordErrorMessage.value = '비밀번호를 입력해주세요.'
    isValid = false
  } else if (password.value.length < 8) {
    passwordErrorMessage.value = '비밀번호는 8자 이상이어야 합니다.'
    isValid = false
  }

  if (password.value !== passwordConfirm.value) {
    passwordConfirmErrorMessage.value = '비밀번호가 일치하지 않습니다.'
    isValid = false
  }

  return isValid
}

// 회원가입 처리
const handleJoin = async () => {
  if (!validate()) return

  const success = await authStore.join(email.value, password.value, name.value, phone.value)

  if (success) {
    alert('회원가입이 완료되었습니다. 로그인해주세요.')
    router.push('/login')
  } else {
    emailErrorMessage.value = '회원가입에 실패했습니다. 다시 시도해주세요.'
  }
}

const goToLogin = () => router.push('/login')
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col">
    <!-- Header -->
    <AppHeader />

    <!-- Register Content -->
    <main class="flex-1 flex items-center justify-center px-6 py-10">
      <div
        class="w-full flex flex-col gap-2 max-w-md bg-white border border-gray-200 rounded-2xl p-10"
      >
        <!-- Logo -->
        <div class="w-full flex justify-center items-center gap-2 mb-6 cursor-pointer">
          <span class="text-2xl font-extrabold text-primary-main">Home</span>
          <span class="text-2xl font-extrabold text-text-mute">Lens</span>
        </div>

        <!-- Title -->
        <div class="text-center mb-6">
          <p class="text-xl font-bold text-gray-900">회원가입</p>
          <p class="text-sm text-gray-500 mt-2">HomeLens와 함께 완벽한 보금자리를 찾아보세요!</p>
        </div>

        <!-- Account Info Form -->
        <form id="join" @submit.prevent="handleJoin" class="flex flex-col gap-2">
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900"
              >이메일 <span class="text-red-500">*</span></label
            >
            <input
              v-model="email"
              type="email"
              placeholder="example@email.com"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main"
            />
          </div>
          <p v-if="emailErrorMessage" class="px-4 py-3 bg-red-50 rounded-lg text-sm text-red-600">
            {{ emailErrorMessage }}
          </p>
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900"
              >비밀번호 <span class="text-red-500">*</span></label
            >
            <input
              v-model="password"
              type="password"
              placeholder="8자 이상 입력하세요"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main"
            />
          </div>
          <p
            v-if="passwordErrorMessage"
            class="px-4 py-3 bg-red-50 rounded-lg text-sm text-red-600"
          >
            {{ passwordErrorMessage }}
          </p>
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900"
              >비밀번호 확인 <span class="text-red-500">*</span></label
            >
            <input
              v-model="passwordConfirm"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main"
            />
          </div>
          <p
            v-if="passwordConfirmErrorMessage"
            class="px-4 py-3 bg-red-50 rounded-lg text-sm text-red-600"
          >
            {{ passwordConfirmErrorMessage }}
          </p>
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900">이름</label>
            <input
              v-model="name"
              type="text"
              placeholder="이름을 입력하세요 (선택)"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main"
            />
          </div>
          <div class="space-y-2">
            <label class="text-sm font-semibold text-gray-900">전화번호</label>
            <input
              v-model="phone"
              type="tel"
              placeholder="전화번호를 입력하세요 (선택)"
              class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main"
            />
          </div>

          <button
            type="submit"
            :disabled="authStore.isLoading"
            class="w-full flex items-center justify-center gap-2 py-4 bg-primary-main rounded-lg text-base font-semibold text-white hover:bg-secondary-main mt-4 disabled:opacity-60"
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
            <template v-else>
              계정 생성
              <svg
                class="w-4 h-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            </template>
          </button>
        </form>

        <!-- Login Link -->
        <div class="text-center mt-6 pt-6 border-t border-gray-200 text-sm text-gray-400">
          <span>이미 회원이신가요?</span>
          <button class="ml-1 text-primary-main font-semibold hover:underline" @click="goToLogin">
            로그인
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
