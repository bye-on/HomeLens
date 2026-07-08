<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminLayout from './AdminLayout.vue'
import { adminApi, type UserDetail } from '@/api/admin'

const users = ref<UserDetail[]>([])

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const totalPages = ref(1) // 서버에서 내려주는 totalPage

const isLoading = ref(false)
const showDeleteModal = ref(false)
const selectedUser = ref<UserDetail | null>(null)

const fetchUsers = async () => {
  try {
    isLoading.value = true
    const data = await adminApi.getList(currentPage.value, pageSize.value)

    users.value = data.userList
    totalPages.value = data.totalPage
    currentPage.value = data.currentPage
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchUsers)

// 페이지/사이즈 바뀌면 재조회
watch([currentPage, pageSize], () => {
  fetchUsers()
})

// admin.ts 기준: 검색은 현재 페이지 데이터에만 적용(서버 검색 파라미터가 없어서)
const filteredUsers = computed(() => {
  const q = searchQuery.value.trim()
  if (!q) return users.value

  return users.value.filter(u => {
    const name = u.userName ?? ''
    const email = u.userEmail ?? '' 
    return name.includes(q) || String(email).includes(q)
  })
})

const formatDate = (dateString: string) => {
  const d = new Date(dateString)
  if (Number.isNaN(d.getTime())) return dateString
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const confirmDelete = (user: UserDetail) => {
  selectedUser.value = user
  showDeleteModal.value = true
}

const handleDelete = async () => {
  if (!selectedUser.value) return

  try {
    isLoading.value = true
    await adminApi.delete(selectedUser.value.userId)
    showDeleteModal.value = false
    selectedUser.value = null

    // 삭제 후 현재 페이지 재조회 (현재 페이지가 비면 한 페이지 앞으로)
    await fetchUsers()
    if (users.value.length === 0 && currentPage.value > 1) {
      currentPage.value -= 1
      await fetchUsers()
    }
  } finally {
    isLoading.value = false
  }
}

const canPrev = computed(() => currentPage.value > 1)
const canNext = computed(() => currentPage.value < totalPages.value)
</script>

<template>
  <AdminLayout>
    <div class="p-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">회원 관리</h1>
          <p class="text-sm text-gray-500 mt-1">
            현재 페이지 {{ users.length }}명 / 전체 {{ totalPages }} 페이지
          </p>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white border border-gray-200 rounded-xl p-4 mb-6 flex items-center gap-4">
        <div class="flex-1 relative">
          <svg class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="이름 또는 이메일로 검색"
            class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
          />
        </div>

        <select
          v-model.number="pageSize"
          class="px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
        >
          <option :value="10">10개</option>
          <option :value="20">20개</option>
          <option :value="50">50개</option>
        </select>
      </div>

      <!-- Table -->
      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-16">ID</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase">회원정보</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-40">연락처</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-32">권한</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-32">가입일</th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-24">관리</th>
            </tr>
          </thead>

          <tbody class="divide-y divide-gray-100">
            <tr v-for="user in filteredUsers" :key="user.userId" class="hover:bg-gray-50 transition-colors">
              <td class="px-6 py-4 text-sm text-gray-500">{{ user.userId }}</td>

              <td class="px-6 py-4">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-full bg-gray-200 flex items-center justify-center text-sm font-semibold text-gray-600">
                    {{ user.userName?.charAt(0) }}
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900">{{ user.userName }}</p>
                    <p class="text-xs text-gray-500">{{ (user as any).userEmail }}</p>
                  </div>
                </div>
              </td>

              <td class="px-6 py-4 text-sm text-gray-600">{{ user.userTel }}</td>

              <td class="px-6 py-4">
                <span class="px-2.5 py-1 text-xs font-medium rounded-full bg-gray-100 text-gray-700">
                  {{ user.userRole }}
                </span>
              </td>

              <td class="px-6 py-4 text-sm text-gray-500">
                {{ formatDate(user.userCreatedAt) }}
              </td>

              <td class="px-6 py-4">
                <button
                  class="px-3 py-1.5 text-xs font-medium text-red-500 border border-gray-200 rounded-lg hover:bg-red-50 hover:border-red-200 transition-colors disabled:opacity-50"
                  :disabled="isLoading"
                  @click="confirmDelete(user)"
                >
                  삭제
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="!isLoading && filteredUsers.length === 0" class="py-16 text-center">
          <span class="text-5xl block mb-4">😢</span>
          <p class="text-gray-500">검색 결과가 없습니다.</p>
        </div>

        <div v-if="isLoading" class="py-10 text-center text-sm text-gray-500">
          불러오는 중...
        </div>
      </div>

      <!-- Pagination (server-side) -->
      <div class="flex items-center justify-between mt-6">
        <p class="text-sm text-gray-500">
          {{ currentPage }} / {{ totalPages }} 페이지
        </p>

        <div class="flex items-center gap-1">
          <button
            class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
            :disabled="!canPrev || isLoading"
            @click="currentPage -= 1"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </button>

          <button
            class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
            :disabled="!canNext || isLoading"
            @click="currentPage += 1"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6" />
            </svg>
          </button>
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
        <span class="text-5xl block mb-4">⚠️</span>
        <h3 class="text-lg font-bold text-gray-900 mb-2">회원을 삭제하시겠습니까?</h3>
        <p class="text-sm text-gray-500 mb-6">
          {{ selectedUser?.userName }} ({{ (selectedUser as any)?.userEmail }})
        </p>
        <div class="flex gap-3">
          <button
            class="flex-1 py-3 bg-gray-100 text-gray-600 font-semibold rounded-lg hover:bg-gray-200 transition-colors"
            :disabled="isLoading"
            @click="showDeleteModal = false"
          >
            취소
          </button>
          <button
            class="flex-1 py-3 bg-red-500 text-white font-semibold rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50"
            :disabled="isLoading"
            @click="handleDelete"
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
