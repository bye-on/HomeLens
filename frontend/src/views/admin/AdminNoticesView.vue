<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import { noticeApi, type BoardListItem } from '@/api/notice'

const router = useRouter()

const notices = ref<BoardListItem[]>([])
const searchQuery = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)

const isLoading = ref(false)
const showDeleteModal = ref(false)
const selectedNotice = ref<BoardListItem | null>(null)

const filteredNotices = computed(() => {
  const q = searchQuery.value.trim()
  if (!q) return notices.value
  return notices.value.filter((n) => (n.subject ?? '').includes(q) || (n.content ?? '').includes(q))
})

const fetchList = async () => {
  isLoading.value = true
  try {
    const res = await noticeApi.getList(currentPage.value, pageSize.value)
    notices.value = res.list
    totalPages.value = Math.max(1, res.totalPage ?? 1)
    currentPage.value = Math.min(Math.max(1, res.currentPage ?? currentPage.value), totalPages.value)
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchList)

// pageSize 바뀌면 1페이지부터 다시
watch(pageSize, async () => {
  currentPage.value = 1
  await fetchList()
})

// currentPage 바뀌면 서버에서 다시 가져오기
watch(currentPage, async () => {
  await fetchList()
})

const goToWrite = () => router.push('/board/write')
const goToEdit = (boardId: number) => router.push(`/board/${boardId}/edit`)

const confirmDelete = (notice: BoardListItem) => {
  selectedNotice.value = notice
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!selectedNotice.value) return

  await noticeApi.delete(selectedNotice.value.boardId)

  // 삭제 후 현재 페이지 재조회 (빈 페이지가 되면 한 페이지 앞으로)
  showDeleteModal.value = false
  selectedNotice.value = null

  await fetchList()
  if (notices.value.length === 0 && currentPage.value > 1) {
    currentPage.value -= 1
    await fetchList()
  }
}
</script>

<template>
  <AdminLayout>
    <div class="p-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">공지사항 관리</h1>
          <p class="text-sm text-gray-500 mt-1">
            현재 페이지 {{ filteredNotices.length }}개 / 전체 {{ currentPage }} / {{ totalPages }} 페이지
          </p>
        </div>

        <button
          class="flex items-center gap-2 px-5 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
          @click="goToWrite"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M5 12h14" />
          </svg>
          새 공지 작성
        </button>
      </div>

      <!-- Search -->
      <div class="bg-white border border-gray-200 rounded-xl p-4 mb-6">
        <div class="relative">
          <svg
            class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="제목 또는 내용으로 검색 (현재 페이지 기준)"
            class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
          />
        </div>
      </div>

      <!-- Table -->
      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-20">ID</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase">제목</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-28">작성자</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-36">작성일</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-40">관리</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-gray-100">
            <tr v-for="notice in filteredNotices" :key="notice.boardId" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 text-sm text-gray-500">{{ notice.boardId }}</td>

              <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-gray-900">{{ notice.subject }}</span>
                </div>
              </td>

              <td class="px-6 py-4 text-sm text-gray-600">{{ notice.userName }}</td>

              <td class="px-6 py-4 text-sm text-gray-500">{{ notice.registerTime }}</td>

              <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                  <button
                    class="px-3 py-1.5 text-xs font-medium text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                    @click="goToEdit(notice.boardId)"
                  >
                    수정
                  </button>
                  <button
                    class="px-3 py-1.5 text-xs font-medium text-red-500 border border-gray-200 rounded-lg hover:bg-red-50 hover:border-red-200 transition-colors"
                    @click="confirmDelete(notice)"
                  >
                    삭제
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="!isLoading && filteredNotices.length === 0" class="py-16 text-center">
          <span class="text-5xl block mb-4">📭</span>
          <p class="text-gray-500">검색 결과가 없습니다.</p>
        </div>

        <div v-if="isLoading" class="py-10 text-center text-gray-500 text-sm">
          불러오는 중...
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between mt-6">
        <p class="text-sm text-gray-500">
          {{ currentPage }} / {{ totalPages }} 페이지
        </p>

        <div class="flex items-center gap-2">
          <select v-model.number="pageSize" class="border border-gray-200 rounded-lg px-3 py-2 text-sm">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>

          <div class="flex items-center gap-1">
            <button
              class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
              :disabled="currentPage === 1 || isLoading"
              @click="currentPage -= 1"
            >
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 18l-6-6 6-6" />
              </svg>
            </button>

            <button
              class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
              :disabled="currentPage === totalPages || isLoading"
              @click="currentPage += 1"
            >
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div
      v-if="showDeleteModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
      @click.self="showDeleteModal = false"
    >
      <div class="w-full max-w-sm bg-white rounded-2xl p-8 text-center">
        <span class="text-5xl block mb-4">🗑️</span>
        <h3 class="text-lg font-bold text-gray-900 mb-2">공지사항을 삭제하시겠습니까?</h3>
        <p class="text-sm text-gray-500 mb-6">{{ selectedNotice?.subject }}</p>
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
  </AdminLayout>
</template>
