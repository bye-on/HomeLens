<script setup lang="ts">
import { ref, computed } from 'vue'
import AdminLayout from './AdminLayout.vue'

interface Property {
  id: number
  title: string
  address: string
  price: string
  type: string
  status: 'active' | 'pending' | 'rejected'
  views: number
  createdAt: string
  agentName: string
}

const properties = ref<Property[]>([
  {
    id: 1,
    title: '강남역 도보 5분 신축 오피스텔',
    address: '서울시 강남구 역삼동',
    price: '전세 2.5억',
    type: '오피스텔',
    status: 'active',
    views: 234,
    createdAt: '2025-12-21',
    agentName: '김부동',
  },
  {
    id: 2,
    title: '홍대입구역 깔끔한 원룸',
    address: '서울시 마포구 서교동',
    price: '월세 50/45',
    type: '원룸',
    status: 'pending',
    views: 156,
    createdAt: '2025-12-20',
    agentName: '이중개',
  },
  {
    id: 3,
    title: '마포구청역 풀옵션 투룸',
    address: '서울시 마포구 성산동',
    price: '전세 1.8억',
    type: '투룸',
    status: 'active',
    views: 312,
    createdAt: '2025-12-19',
    agentName: '박공인',
  },
  {
    id: 4,
    title: '신촌역 역세권 오피스텔',
    address: '서울시 서대문구 창천동',
    price: '월세 1000/60',
    type: '오피스텔',
    status: 'active',
    views: 189,
    createdAt: '2025-12-18',
    agentName: '최매물',
  },
  {
    id: 5,
    title: '건대입구역 신축 빌라',
    address: '서울시 광진구 자양동',
    price: '전세 2.0억',
    type: '빌라',
    status: 'rejected',
    views: 87,
    createdAt: '2025-12-17',
    agentName: '정부동',
  },
  {
    id: 6,
    title: '잠실역 대단지 아파트',
    address: '서울시 송파구 잠실동',
    price: '전세 5.5억',
    type: '아파트',
    status: 'active',
    views: 567,
    createdAt: '2025-12-16',
    agentName: '한공인',
  },
])

const searchQuery = ref('')
const statusFilter = ref('all')
const typeFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const showDeleteModal = ref(false)
const selectedProperty = ref<Property | null>(null)

const filteredProperties = computed(() => {
  return properties.value.filter((property) => {
    const matchesSearch =
      property.title.includes(searchQuery.value) || property.address.includes(searchQuery.value)
    const matchesStatus = statusFilter.value === 'all' || property.status === statusFilter.value
    const matchesType = typeFilter.value === 'all' || property.type === typeFilter.value
    return matchesSearch && matchesStatus && matchesType
  })
})

const totalPages = computed(() => Math.ceil(filteredProperties.value.length / pageSize.value))

const getStatusClass = (status: string) => {
  switch (status) {
    case 'active':
      return 'bg-green-100 text-green-700'
    case 'pending':
      return 'bg-yellow-100 text-yellow-700'
    case 'rejected':
      return 'bg-red-100 text-red-700'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'active':
      return '승인됨'
    case 'pending':
      return '대기중'
    case 'rejected':
      return '거절됨'
    default:
      return status
  }
}

const approveProperty = (property: Property) => {
  property.status = 'active'
}

const rejectProperty = (property: Property) => {
  property.status = 'rejected'
}

const confirmDelete = (property: Property) => {
  selectedProperty.value = property
  showDeleteModal.value = true
}

const handleDelete = () => {
  if (selectedProperty.value) {
    properties.value = properties.value.filter((p) => p.id !== selectedProperty.value!.id)
  }
  showDeleteModal.value = false
  selectedProperty.value = null
}
</script>

<template>
  <AdminLayout>
    <div class="p-8">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">매물 관리</h1>
          <p class="text-sm text-gray-500 mt-1">총 {{ properties.length }}개의 매물</p>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-white border border-gray-200 rounded-xl p-4 mb-6 flex items-center gap-4">
        <div class="flex-1 relative">
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
            placeholder="매물명 또는 주소로 검색"
            class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
          />
        </div>
        <select
          v-model="statusFilter"
          class="px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
        >
          <option value="all">전체 상태</option>
          <option value="active">승인됨</option>
          <option value="pending">대기중</option>
          <option value="rejected">거절됨</option>
        </select>
        <select
          v-model="typeFilter"
          class="px-4 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-primary-main"
        >
          <option value="all">전체 유형</option>
          <option value="원룸">원룸</option>
          <option value="투룸">투룸</option>
          <option value="오피스텔">오피스텔</option>
          <option value="아파트">아파트</option>
          <option value="빌라">빌라</option>
        </select>
      </div>

      <!-- Table -->
      <div class="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <table class="w-full">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200">
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase w-16">
                ID
              </th>
              <th class="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase">
                매물정보
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-28">
                가격
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-24">
                유형
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-24">
                상태
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-20">
                조회수
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-28">
                등록일
              </th>
              <th class="px-6 py-4 text-center text-xs font-semibold text-gray-500 uppercase w-40">
                관리
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="property in filteredProperties"
              :key="property.id"
              class="hover:bg-gray-50 transition-colors"
            >
              <td class="px-6 py-4 text-sm text-gray-500">{{ property.id }}</td>
              <td class="px-6 py-4">
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ property.title }}</p>
                  <p class="text-xs text-gray-500">
                    {{ property.address }} · {{ property.agentName }}
                  </p>
                </div>
              </td>
              <td class="px-6 py-4 text-sm font-semibold text-primary-main">
                {{ property.price }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ property.type }}</td>
              <td class="px-6 py-4">
                <span
                  :class="[
                    'px-2 text-xs font-medium rounded-full',
                    getStatusClass(property.status),
                  ]"
                >
                  {{ getStatusLabel(property.status) }}
                </span>
              </td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ property.views.toLocaleString() }}</td>
              <td class="px-6 py-4 text-sm text-gray-500">{{ property.createdAt }}</td>
              <td class="px-6 py-4">
                <div class="flex items-center gap-2">
                  <button
                    v-if="property.status === 'pending'"
                    class="px-3 py-1.5 text-xs font-medium text-green-600 border border-gray-200 rounded-lg hover:bg-green-50 hover:border-green-200 transition-colors"
                    @click="approveProperty(property)"
                  >
                    승인
                  </button>
                  <button
                    v-if="property.status === 'pending'"
                    class="px-3 py-1.5 text-xs font-medium text-yellow-600 border border-gray-200 rounded-lg hover:bg-yellow-50 hover:border-yellow-200 transition-colors"
                    @click="rejectProperty(property)"
                  >
                    거절
                  </button>
                  <button
                    class="px-3 py-1.5 text-xs font-medium text-red-500 border border-gray-200 rounded-lg hover:bg-red-50 hover:border-red-200 transition-colors"
                    @click="confirmDelete(property)"
                  >
                    삭제
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="filteredProperties.length === 0" class="py-16 text-center">
          <span class="text-5xl block mb-4">🏠</span>
          <p class="text-gray-500">검색 결과가 없습니다.</p>
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between mt-6">
        <p class="text-sm text-gray-500">
          총 {{ filteredProperties.length }}개 중 {{ currentPage }} / {{ totalPages || 1 }} 페이지
        </p>
        <div class="flex items-center gap-1">
          <button
            class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
            :disabled="currentPage === 1"
            @click="currentPage--"
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
          <button
            class="w-9 h-9 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-40"
            :disabled="currentPage === totalPages"
            @click="currentPage++"
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
        <h3 class="text-lg font-bold text-gray-900 mb-2">매물을 삭제하시겠습니까?</h3>
        <p class="text-sm text-gray-500 mb-6">{{ selectedProperty?.title }}</p>
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
