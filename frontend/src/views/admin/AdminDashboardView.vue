<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from './AdminLayout.vue'
import { adminApi, type UserDetail } from '@/api/admin'

// // 더미 통계 데이터
// const stats = ref([
//   { id: 'users', label: '총 회원수', value: '12,847', change: '+12%', isPositive: true, icon: 'users' },
//   { id: 'properties', label: '등록 매물', value: '3,256', change: '+8%', isPositive: true, icon: 'properties' },
//   { id: 'views', label: '오늘 방문자', value: '1,429', change: '-3%', isPositive: false, icon: 'views' },
//   { id: 'inquiries', label: '문의 건수', value: '47', change: '+24%', isPositive: true, icon: 'inquiries' },
// ])

const recentUsers = ref<UserDetail[]>([])

const currentPage = ref(1)
const pageSize = ref(5)
const isLoading = ref(false)

const fetchUsers = async () => {
  try {
    isLoading.value = true
    const data = await adminApi.getList(currentPage.value, pageSize.value)

    recentUsers.value = data.userList
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchUsers)

const recentProperties = ref([
  { id: 1, title: '강남역 도보 5분 신축 오피스텔', salesType: '월세', deposit: '500', rent: '14' },
  { id: 2, title: '홍대입구역 깔끔한 원룸', salesType: '월세', deposit: '200', rent: '14' },
  { id: 3, title: '마포구청역 풀옵션 투룸', salesType: '월세', deposit: '1500', rent: '14' },
  { id: 4, title: '신촌역 역세권 오피스텔', salesType: '전세', deposit: '11500', rent: '0' },
])

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <AdminLayout>
    <div class="p-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">대시보드</h1>
        <p class="text-sm text-gray-500 mt-1">HomeLens 관리자 현황</p>
      </div>

      <!-- Stats Grid -->
      <!-- <div class="grid grid-cols-4 gap-6 mb-8">
        <div
          v-for="stat in stats"
          :key="stat.id"
          class="bg-white border border-gray-200 rounded-xl p-6"
        >
          <div class="flex items-center justify-between mb-4">
            <span class="text-sm font-medium text-gray-500">{{ stat.label }}</span>
            <div class="w-10 h-10 rounded-lg bg-primary-main/10 flex items-center justify-center">
              <svg
                v-if="stat.icon === 'users'"
                class="w-5 h-5 text-primary-main"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="9" cy="7" r="4" />
                <path d="M3 21v-2a4 4 0 014-4h4a4 4 0 014 4v2" />
              </svg>
              <svg
                v-else-if="stat.icon === 'properties'"
                class="w-5 h-5 text-primary-main"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
              </svg>
              <svg
                v-else-if="stat.icon === 'views'"
                class="w-5 h-5 text-primary-main"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg
                v-else-if="stat.icon === 'inquiries'"
                class="w-5 h-5 text-primary-main"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
              </svg>
            </div>
          </div>
          <div class="flex items-baseline gap-2">
            <span class="text-2xl font-bold text-gray-900">{{ stat.value }}</span>
            <span
              class="text-xs font-medium px-1.5 py-0.5 rounded"
              :class="stat.isPositive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
            >
              {{ stat.change }}
            </span>
          </div>
        </div>
      </div> -->

      <div class="grid grid-cols-2 gap-6">
        <!-- Recent Users -->
        <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 class="font-semibold text-gray-900">최근 가입 회원</h2>
            <router-link to="/admin/users" class="text-sm text-primary-main hover:underline">
              전체보기
            </router-link>
          </div>
          <div class="divide-y divide-gray-100">
            <div
              v-for="user in recentUsers"
              :key="user.userId"
              class="px-6 py-4 flex items-center gap-4 hover:bg-gray-50 transition-colors"
            >
              <div
                class="w-9 h-9 rounded-full bg-gray-200 flex items-center justify-center text-sm font-semibold text-gray-600"
              >
                {{ user.userName.charAt(0) }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">{{ user.userName }}</p>
                <p class="text-xs text-gray-500 truncate">{{ user.userEmail }}</p>
              </div>
              <div class="text-right">
                <span
                  class="inline-block px-2 py-1 text-xs font-medium rounded"
                  :class="
                    user.userEmailVerified === 't'
                      ? 'bg-green-100 text-green-700'
                      : 'bg-yellow-100 text-yellow-700'
                  "
                >
                  {{ user.userEmailVerified === 't' ? '활성' : '대기' }}
                </span>
                <p class="text-xs text-gray-400 mt-1">{{ formatDate(user.userCreatedAt) }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Properties -->
        <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <h2 class="font-semibold text-gray-900">최근 등록 매물</h2>
            <router-link to="/admin/properties" class="text-sm text-primary-main hover:underline">
              전체보기
            </router-link>
          </div>
          <div class="divide-y divide-gray-100">
            <div
              v-for="property in recentProperties"
              :key="property.id"
              class="px-6 py-4 flex items-center gap-4 hover:bg-gray-50 transition-colors"
            >
              <div class="w-12 h-12 rounded-lg bg-gray-200 flex items-center justify-center">
                <svg
                  class="w-6 h-6 text-gray-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
                </svg>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900 truncate">{{ property.title }}</p>
                <span>
                  <p class="text-xs text-primary-main font-semibold">
                    {{ property.salesType }} {{ property.deposit }} / {{ property.rent }}
                  </p>
                </span>
              </div>
              <div class="text-right">
                <!-- <span
                  class="inline-block px-2 py-1 text-xs font-medium rounded"
                  :class="property.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'"
                >
                  {{ property.status === 'active' ? '활성' : '대기' }}
                </span> -->
                <!-- <p class="text-xs text-gray-400 mt-1">👁 {{ property.views }}</p> -->
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>
