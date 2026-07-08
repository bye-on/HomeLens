<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { noticeApi, type BoardListItem } from '@/api/notice'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const noticeId = computed(() => Number(route.params.id))
const isLoading = ref(true)
const showDeleteModal = ref(false)

const notice = ref<BoardListItem | null>(null)

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const goToList = () => {
  router.push('/board')
}

const goToEdit = () => {
  router.push(`/board/${noticeId.value}/edit`)
}

const handleDelete = async () => {
  // TODO: API 연동
  await noticeApi.delete(noticeId.value)
  showDeleteModal.value = false
  router.push('/board')
}

const fetchNotice = async () => {
  isLoading.value = true
  try {
    notice.value = await noticeApi.getDetail(noticeId.value)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  // TODO: API 연동
  fetchNotice()
  isLoading.value = false
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

      <!-- Content -->
      <article
        v-else-if="notice"
        class="bg-white border border-gray-200 rounded-2xl overflow-hidden"
      >
        <!-- Header -->
        <div class="px-8 py-6 border-b border-gray-100">
          <h1 class="text-xl font-bold text-gray-900 mb-4">{{ notice.subject }}</h1>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-6 text-sm text-gray-500">
              <span class="flex items-center gap-1.5">
                <svg
                  class="w-4 h-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <circle cx="12" cy="7" r="4" />
                  <path d="M5.5 21a7.5 7.5 0 0115 0" />
                </svg>
                {{ notice.userName }}
              </span>
              <span class="flex items-center gap-1.5">
                <svg
                  class="w-4 h-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="3" y="4" width="18" height="18" rx="2" />
                  <path d="M16 2v4M8 2v4M3 10h18" />
                </svg>
                {{ formatDate(notice.registerTime) }}
              </span>
              <span class="flex items-center gap-1.5">
                <svg
                  class="w-4 h-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <!-- {{ notice.viewCount.toLocaleString() }} -->
              </span>
            </div>

            <!-- Admin Actions -->
            <div v-if="authStore.isAdmin" class="flex items-center gap-2">
              <button
                class="px-4 py-2 text-sm font-medium text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 hover:border-primary-main hover:text-primary-main transition-colors"
                @click="goToEdit"
              >
                수정
              </button>
              <button
                class="px-4 py-2 text-sm font-medium text-red-500 border border-gray-200 rounded-lg hover:bg-red-50 hover:border-red-300 transition-colors"
                @click="showDeleteModal = true"
              >
                삭제
              </button>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="px-8 py-8">
          <div class="prose prose-gray max-w-none">
            <p class="whitespace-pre-line text-gray-700 leading-relaxed">{{ notice.content }}</p>
          </div>
        </div>
      </article>

      <!-- Not Found -->
      <div v-else class="bg-white border border-gray-200 rounded-2xl p-16 text-center">
        <span class="text-5xl block mb-4">😢</span>
        <p class="text-gray-500 mb-6">공지사항을 찾을 수 없습니다.</p>
        <button
          class="px-6 py-3 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
          @click="goToList"
        >
          목록으로 돌아가기
        </button>
      </div>

      <!-- Navigation -->
      <div class="flex justify-center mt-8">
        <button
          class="px-6 py-3 bg-gray-100 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-200 transition-colors"
          @click="goToList"
        >
          목록으로
        </button>
      </div>
    </main>

    <!-- Delete Modal -->
    <div
      v-if="showDeleteModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      @click.self="showDeleteModal = false"
    >
      <div class="w-full max-w-sm bg-white rounded-2xl p-8 text-center">
        <span class="text-5xl block mb-4">🗑️</span>
        <h3 class="text-lg font-bold text-gray-900 mb-2">정말 삭제하시겠습니까?</h3>
        <p class="text-sm text-gray-500 mb-6">삭제된 글은 복구할 수 없습니다.</p>
        <div class="flex gap-3">
          <button
            class="flex-1 py-3 bg-gray-100 text-gray-600 font-semibold rounded-lg hover:bg-gray-200 transition-colors"
            @click="showDeleteModal = false"
          >
            취소
          </button>
          <button
            class="flex-1 py-3 bg-red-500 text-white font-semibold rounded-lg hover:bg-red-600 transition-colors"
            @click="handleDelete"
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
