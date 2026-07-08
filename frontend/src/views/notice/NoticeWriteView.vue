<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { noticeApi } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const noticeId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEditMode = computed(() => !!noticeId.value)
const isLoading = ref(false)
const isSaving = ref(false)

const subject = ref('')
const content = ref('')
const errorMessage = ref('')

const goToList = () => {
  router.push('/board')
}

const validateForm = () => {
  if (!subject.value.trim()) {
    errorMessage.value = '제목을 입력해주세요.'
    return false
  }
  if (subject.value.length > 100) {
    errorMessage.value = '제목은 100자 이내로 입력해주세요.'
    return false
  }
  if (!content.value.trim()) {
    errorMessage.value = '내용을 입력해주세요.'
    return false
  }
  return true
}

const handleSubmit = async () => {
  errorMessage.value = ''

  if (!validateForm()) return

  isSaving.value = true

  try {
    if (isEditMode.value) {
      // TODO: API 연동
      await noticeApi.update(noticeId.value!, { subject: subject.value, content: content.value })
      console.log('Update:', { subject: subject.value, content: content.value })
    } else {
      // TODO: API 연동
      await noticeApi.create({ subject: subject.value, content: content.value })
      console.log('Create:', { subject: subject.value, content: content.value })
    }
    router.push('/board')
  } catch {
    errorMessage.value = '저장 중 오류가 발생했습니다.'
  } finally {
    isSaving.value = false
  }
}

const fetchNotice = async () => {
  if (!noticeId.value) return

  isLoading.value = true
  try {
    // TODO: API 연동
    const data = await noticeApi.getDetail(noticeId.value)
    // 더미 데이터
    // const data: Notice = {
    //   noticeId: noticeId.value,
    //   subject: '[공지] HomeLens 서비스 오픈 안내',
    //   content: '안녕하세요. HomeLens 서비스가 정식 오픈되었습니다.',
    //   authorName: 'admin',
    //   viewCount: 1234,
    //   createdAt: '2025-12-20T10:00:00',
    //   updatedAt: '2025-12-20T10:00:00',
    // }
    subject.value = data.subject
    content.value = data.content
  } catch {
    errorMessage.value = '데이터를 불러오는 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  // admin이 아니면 접근 불가
  if (!authStore.isAdmin) {
    router.push('/board')
    return
  }

  if (isEditMode.value) {
    fetchNotice()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader />

    <main class="max-w-4xl mx-auto px-6 py-10">
      <!-- Back Button -->
      <button
        class="flex items-center gap-2 text-sm text-gray-500 hover:text-primary-main mb-6 transition-colors"
        @click="goToList"
      >
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        목록으로
      </button>

      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">
          {{ isEditMode ? '공지사항 수정' : '공지사항 작성' }}
        </h1>
        <p class="text-sm text-gray-500 mt-1">
          {{ isEditMode ? '공지사항 내용을 수정합니다.' : '새로운 공지사항을 작성합니다.' }}
        </p>
      </div>

      <!-- Loading -->
      <div v-if="isLoading" class="flex items-center justify-center py-20">
        <svg class="w-8 h-8 animate-spin text-primary-main" viewBox="0 0 24 24" fill="none">
          <circle
            class="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            stroke-width="4"
          />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
          />
        </svg>
      </div>

      <!-- Form -->
      <form
        v-else
        class="bg-white border border-gray-200 rounded-2xl p-8"
        @submit.prevent="handleSubmit"
      >
        <!-- subject -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-900 mb-2">
            제목 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="subject"
            type="text"
            placeholder="제목을 입력하세요"
            maxlength="100"
            class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-primary-main transition-colors"
          />
          <div class="flex justify-end mt-1">
            <span class="text-xs text-gray-400">{{ subject.length }}/100</span>
          </div>
        </div>

        <!-- Content -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-900 mb-2">
            내용 <span class="text-red-500">*</span>
          </label>
          <textarea
            v-model="content"
            placeholder="내용을 입력하세요"
            rows="15"
            class="w-full px-4 py-3.5 border border-gray-200 rounded-lg text-base resize-none focus:outline-none focus:border-primary-main transition-colors"
          ></textarea>
        </div>

        <!-- Error -->
        <div
          v-if="errorMessage"
          class="mb-6 px-4 py-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600"
        >
          {{ errorMessage }}
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-3">
          <button
            type="button"
            class="px-6 py-3 bg-gray-100 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-200 transition-colors"
            @click="goToList"
          >
            취소
          </button>
          <button
            type="submit"
            class="px-6 py-3 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors disabled:opacity-60 flex items-center gap-2"
            :disabled="isSaving"
          >
            <svg v-if="isSaving" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              />
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
              />
            </svg>
            {{ isEditMode ? '수정 완료' : '등록하기' }}
          </button>
        </div>
      </form>
    </main>
  </div>
</template>
