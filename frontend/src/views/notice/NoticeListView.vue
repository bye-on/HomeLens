<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { noticeApi, type BoardListItem } from '@/api/notice'

const router = useRouter()
const authStore = useAuthStore()

// 임시 더미 데이터
const notices = ref<BoardListItem[]>([])

const totalCount = ref(23)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

const goToDetail = (noticeId: number) => {
  router.push(`/board/${noticeId}`)
}

const goToWrite = () => {
  router.push('/board/write')
}

const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// 페이지네이션 버튼 생성
const pageNumbers = computed(() => {
  const pages: number[] = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  const end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const fetchNotices = async () => {
  const data = await noticeApi.getList(currentPage.value, pageSize.value)
  notices.value = data.list
  totalCount.value = data.totalPage
  currentPage.value = data.currentPage
}

onMounted(() => {
  // TODO: API 연동
  fetchNotices()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <AppHeader />

    <main class="max-w-5xl mx-auto px-6 py-10">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">공지사항</h1>
          <p class="text-sm text-gray-500 mt-1">HomeLens의 새로운 소식을 확인하세요.</p>
        </div>
        <button
          v-if="authStore.isAdmin"
          class="flex items-center gap-2 px-5 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
          @click="goToWrite"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M12 5v14M5 12h14" />
          </svg>
          글쓰기
        </button>
      </div>

      <!-- Table -->
      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th
                class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-20"
              >
                번호
              </th>
              <th
                class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider"
              >
                제목
              </th>
              <th
                class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-28"
              >
                작성자
              </th>
              <th
                class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-28"
              >
                작성일
              </th>
              <!-- <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider w-24">
                조회수
              </th> -->
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="notice in notices"
              :key="notice.boardId"
              class="hover:bg-gray-50 cursor-pointer transition-colors"
              @click="goToDetail(notice.boardId)"
            >
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ notice.boardId }}
              </td>
              <td class="px-6 py-4">
                <span
                  class="text-sm font-medium text-gray-900 hover:text-primary-main transition-colors"
                >
                  {{ notice.subject }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ notice.userName }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">
                {{ formatDate(notice.registerTime) }}
              </td>
              <!-- <td class="px-6 py-4 text-sm text-gray-500">
                {{ notice.viewCount.toLocaleString() }}
              </td> -->
            </tr>
          </tbody>
        </table>

        <!-- Empty State -->
        <div v-if="notices.length === 0" class="py-16 text-center">
          <span class="text-5xl block mb-4">📭</span>
          <p class="text-gray-500">등록된 공지사항이 없습니다.</p>
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-center gap-1 mt-8">
        <!-- First -->
        <button
          class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="currentPage === 1"
          @click="changePage(1)"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M11 17l-5-5 5-5M18 17l-5-5 5-5" />
          </svg>
        </button>

        <!-- Prev -->
        <button
          class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>

        <!-- Page Numbers -->
        <button
          v-for="page in pageNumbers"
          :key="page"
          class="w-9 h-9 flex items-center justify-center rounded-lg text-sm font-medium transition-colors"
          :class="
            page === currentPage ? 'bg-primary-main text-white' : 'text-gray-600 hover:bg-gray-100'
          "
          @click="changePage(page)"
        >
          {{ page }}
        </button>

        <!-- Next -->
        <button
          class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M9 18l6-6-6-6" />
          </svg>
        </button>

        <!-- Last -->
        <button
          class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed"
          :disabled="currentPage === totalPages"
          @click="changePage(totalPages)"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M13 17l5-5-5-5M6 17l5-5-5-5" />
          </svg>
        </button>
      </div>

      <!-- Page Info -->
      <div class="text-center mt-4 text-sm text-gray-500">
        총 {{ totalCount }}개 중 {{ currentPage }} / {{ totalPages }} 페이지
      </div>
    </main>
  </div>
</template>
