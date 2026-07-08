<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import PropertyCard from '@/components/property/PropertyCard.vue'
import PropertyDetail from '@/components/property/PropertyDetail.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { noticeApi, type BoardListItem } from '@/api/notice'
import { propertyApi } from '@/api/property'
import type { PropertyData } from '@/type/property'

const router = useRouter()
const authStore = useAuthStore()

const searchMode = ref<'basic' | 'ai'>('basic')
const keyword = ref('')
const recentQueries = ref<string[]>([])
const recommendedQueries = [
  '강남역 근처 원룸 추천해줘',
  '전세 2억 이하 마포구 매물',
  '역세권 도보 5분 이내',
  '대학교 근처 월세 50만원 이하',
  '신축 오피스텔 추천',
  '반려동물 가능한 전세',
]

const selectedDo = ref<string | null>(null)
const selectedSi = ref<string | null>(null)
const selectedDong = ref<string | null>(null)
const doList = ref<string[]>([])
const siList = ref<string[]>([])
const dongList = ref<string[]>([])
const showDoDropdown = ref(false)
const showSiDropdown = ref(false)
const showDongDropdown = ref(false)
const isLoadingDo = ref(false)
const isLoadingSi = ref(false)
const isLoadingDong = ref(false)

const popularProperties = ref<PropertyData[]>([])
const notices = ref<BoardListItem[]>([])
const selectedProperty = ref<PropertyData | null>(null)
const detailProperty = ref<PropertyData | null>(null)
const showDetail = ref(false)

const loadDoList = async () => {
  isLoadingDo.value = true
  try {
    doList.value = await propertyApi.getDo()
  } catch (error) {
    console.error('시/도 목록 로드 실패:', error)
    doList.value = []
  } finally {
    isLoadingDo.value = false
  }
}

const loadSiList = async (doName: string) => {
  isLoadingSi.value = true
  try {
    siList.value = await propertyApi.getSi(doName)
  } catch (error) {
    console.error('시/군/구 목록 로드 실패:', error)
    siList.value = []
  } finally {
    isLoadingSi.value = false
  }
}

const loadDongList = async (doName: string, siName: string) => {
  isLoadingDong.value = true
  try {
    dongList.value = await propertyApi.getDong(doName, siName)
  } catch (error) {
    console.error('읍/면/동 목록 로드 실패:', error)
    dongList.value = []
  } finally {
    isLoadingDong.value = false
  }
}

const handleDoClick = async () => {
  showDoDropdown.value = true
  await loadDoList()
}

const handleSiClick = async () => {
  if (!selectedDo.value) return
  showSiDropdown.value = true
  await loadSiList(selectedDo.value)
}

const handleDongClick = async () => {
  if (!selectedDo.value || !selectedSi.value) return
  showDongDropdown.value = true
  await loadDongList(selectedDo.value, selectedSi.value)
}

const selectDo = (doName: string) => {
  selectedDo.value = doName
  selectedSi.value = null
  selectedDong.value = null
  siList.value = []
  dongList.value = []
  showDoDropdown.value = false
}

const selectSi = (siName: string) => {
  selectedSi.value = siName
  selectedDong.value = null
  dongList.value = []
  showSiDropdown.value = false
}

const selectDong = (dongName: string) => {
  selectedDong.value = dongName
  showDongDropdown.value = false
}

const clearRegion = (level: 'do' | 'si' | 'dong') => {
  if (level === 'do') {
    selectedDo.value = null
    selectedSi.value = null
    selectedDong.value = null
    siList.value = []
    dongList.value = []
  } else if (level === 'si') {
    selectedSi.value = null
    selectedDong.value = null
    dongList.value = []
  } else {
    selectedDong.value = null
  }
}

const buildSearchQuery = () => {
  const parts = [selectedDo.value, selectedSi.value, selectedDong.value, keyword.value]
  return parts.filter(Boolean).join(' ').trim()
}

const handleSearch = () => {
  const query = buildSearchQuery()
  if (!query) return

  if (authStore.isLoggedIn) {
    const queries = JSON.parse(localStorage.getItem('recentQueries') || '[]')
    const nextQueries = [query, ...queries.filter((item: string) => item !== query)].slice(0, 5)
    localStorage.setItem('recentQueries', JSON.stringify(nextQueries))
    recentQueries.value = nextQueries
  }

  router.push({
    path: '/result',
    query: {
      q: query,
      mode: searchMode.value,
    },
  })
}

const insertQuery = (query: string) => {
  keyword.value = query
}

const loadRecentQueries = () => {
  if (authStore.isLoggedIn) {
    recentQueries.value = JSON.parse(localStorage.getItem('recentQueries') || '[]')
  }
}

const fetchPopularProperties = async () => {
  try {
    popularProperties.value = await propertyApi.getPopularHome()
  } catch (error) {
    console.error('인기 매물 조회 실패:', error)
    popularProperties.value = []
  }
}

const fetchNotices = async () => {
  try {
    const response = await noticeApi.getList(1, 5)
    notices.value = response.list
  } catch (error) {
    console.error('공지사항 조회 실패:', error)
    notices.value = []
  }
}

const selectProperty = (property: PropertyData) => {
  selectedProperty.value = property
}

const openDetail = (property: PropertyData) => {
  detailProperty.value = property
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
  detailProperty.value = null
}

const handlePropertyUpdate = (updatedProperty: PropertyData) => {
  if (detailProperty.value?.itemId === updatedProperty.itemId) {
    detailProperty.value = updatedProperty
  }
  if (selectedProperty.value?.itemId === updatedProperty.itemId) {
    selectedProperty.value = updatedProperty
  }
  const index = popularProperties.value.findIndex(
    (property) => property.itemId === updatedProperty.itemId,
  )
  if (index !== -1) {
    popularProperties.value[index] = updatedProperty
  }
}

onMounted(() => {
  loadRecentQueries()
  fetchPopularProperties()
  fetchNotices()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <AppHeader />

    <main class="flex-1 max-w-7xl mx-auto px-6 w-full py-8">
      <section class="mb-8">
        <div class="max-w-2xl mx-auto">
          <div class="flex justify-center mb-3">
            <div
              class="inline-flex items-center rounded-lg border border-gray-200 bg-white p-1 shadow-sm"
            >
              <button
                type="button"
                class="px-4 py-2 text-sm font-semibold rounded-md transition-colors"
                :class="
                  searchMode === 'basic'
                    ? 'bg-primary-main text-white'
                    : 'text-gray-600 hover:text-primary-main'
                "
                @click="searchMode = 'basic'"
              >
                일반 검색
              </button>
              <button
                type="button"
                class="px-4 py-2 text-sm font-semibold rounded-md transition-colors"
                :class="
                  searchMode === 'ai'
                    ? 'bg-primary-main text-white'
                    : 'text-gray-600 hover:text-primary-main'
                "
                @click="searchMode = 'ai'"
              >
                AI 검색
              </button>
            </div>
          </div>

          <div
            class="flex items-center gap-3 px-6 py-4 bg-white border-2 rounded-xl focus-within:border-primary-main focus-within:ring-2 focus-within:ring-primary-main/20 border-gray-200 transition-all shadow-sm"
          >
            <svg
              class="w-6 h-6 text-gray-400 shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>

            <div class="flex items-center gap-2 shrink-0 border-r border-gray-200 pr-3">
              <div class="relative">
                <button
                  type="button"
                  class="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:border-primary-main hover:text-primary-main transition-colors whitespace-nowrap"
                  :class="
                    selectedDo
                      ? 'bg-primary-main/10 border-primary-main text-primary-main'
                      : 'bg-white text-gray-700'
                  "
                  @click.stop="handleDoClick"
                >
                  {{ selectedDo || '시/도' }}
                </button>
                <div
                  v-if="showDoDropdown"
                  class="absolute top-full left-0 mt-1 w-40 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto"
                  @mouseenter="showDoDropdown = true"
                  @mouseleave="showDoDropdown = false"
                >
                  <div v-if="isLoadingDo" class="p-4 text-center text-sm text-gray-500">
                    로딩 중...
                  </div>
                  <button
                    v-for="doItem in doList"
                    v-else
                    :key="doItem"
                    type="button"
                    class="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main transition-colors"
                    @click="selectDo(doItem)"
                  >
                    {{ doItem }}
                  </button>
                  <div
                    v-if="!isLoadingDo && doList.length === 0"
                    class="p-4 text-center text-sm text-gray-500"
                  >
                    목록이 없습니다
                  </div>
                </div>
              </div>

              <div v-if="selectedDo" class="relative">
                <button
                  type="button"
                  class="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:border-primary-main hover:text-primary-main transition-colors whitespace-nowrap"
                  :class="
                    selectedSi
                      ? 'bg-primary-main/10 border-primary-main text-primary-main'
                      : 'bg-white text-gray-700'
                  "
                  @click.stop="handleSiClick"
                >
                  {{ selectedSi || '시/군/구' }}
                  <span class="ml-1">⌄</span>
                </button>
                <div
                  v-if="showSiDropdown"
                  class="absolute top-full left-0 mt-1 w-40 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto"
                  @mouseenter="showSiDropdown = true"
                  @mouseleave="showSiDropdown = false"
                >
                  <div v-if="isLoadingSi" class="p-4 text-center text-sm text-gray-500">
                    로딩 중...
                  </div>
                  <button
                    v-for="siItem in siList"
                    v-else
                    :key="siItem"
                    type="button"
                    class="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main transition-colors"
                    @click="selectSi(siItem)"
                  >
                    {{ siItem }}
                  </button>
                  <div
                    v-if="!isLoadingSi && siList.length === 0"
                    class="p-4 text-center text-sm text-gray-500"
                  >
                    목록이 없습니다
                  </div>
                </div>
              </div>

              <div v-if="selectedSi" class="relative">
                <button
                  type="button"
                  class="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:border-primary-main hover:text-primary-main transition-colors whitespace-nowrap"
                  :class="
                    selectedDong
                      ? 'bg-primary-main/10 border-primary-main text-primary-main'
                      : 'bg-white text-gray-700'
                  "
                  @click.stop="handleDongClick"
                >
                  {{ selectedDong || '읍/면/동' }}
                  <span class="ml-1">⌄</span>
                </button>
                <div
                  v-if="showDongDropdown"
                  class="absolute top-full left-0 mt-1 w-40 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto"
                  @mouseenter="showDongDropdown = true"
                  @mouseleave="showDongDropdown = false"
                >
                  <div v-if="isLoadingDong" class="p-4 text-center text-sm text-gray-500">
                    로딩 중...
                  </div>
                  <button
                    v-for="dongItem in dongList"
                    v-else
                    :key="dongItem"
                    type="button"
                    class="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main transition-colors"
                    @click="selectDong(dongItem)"
                  >
                    {{ dongItem }}
                  </button>
                  <div
                    v-if="!isLoadingDong && dongList.length === 0"
                    class="p-4 text-center text-sm text-gray-500"
                  >
                    목록이 없습니다
                  </div>
                </div>
              </div>
            </div>

            <input
              v-model="keyword"
              type="text"
              class="flex-1 min-w-0 outline-none text-base text-gray-900 placeholder:text-gray-400 bg-transparent"
              placeholder="지역, 지하철, 매물 조건 또는 매물번호를 입력하세요."
              @keydown.enter="handleSearch"
            />

            <button
              type="button"
              class="px-4 py-2 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
              @click="handleSearch"
            >
              검색
            </button>
          </div>

          <div v-if="selectedDo || selectedSi || selectedDong" class="flex flex-wrap gap-2 mt-3">
            <button
              v-if="selectedDo"
              type="button"
              class="px-3 py-1 bg-primary-main/10 text-primary-main rounded-full text-sm"
              @click="clearRegion('do')"
            >
              {{ selectedDo }} ×
            </button>
            <button
              v-if="selectedSi"
              type="button"
              class="px-3 py-1 bg-primary-main/10 text-primary-main rounded-full text-sm"
              @click="clearRegion('si')"
            >
              {{ selectedSi }} ×
            </button>
            <button
              v-if="selectedDong"
              type="button"
              class="px-3 py-1 bg-primary-main/10 text-primary-main rounded-full text-sm"
              @click="clearRegion('dong')"
            >
              {{ selectedDong }} ×
            </button>
          </div>
        </div>
      </section>

      <section v-if="authStore.isLoggedIn && recentQueries.length > 0" class="mb-8">
        <div class="max-w-2xl mx-auto">
          <h3 class="text-sm font-semibold text-gray-700 mb-3">최근 검색</h3>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="(query, index) in recentQueries"
              :key="index"
              class="px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm text-gray-700 hover:border-primary-main hover:text-primary-main transition-colors"
              @click="insertQuery(query)"
            >
              {{ query }}
            </button>
          </div>
        </div>
      </section>

      <section class="mb-12">
        <div class="max-w-2xl mx-auto">
          <h3 class="text-sm font-semibold text-gray-700 mb-3">추천 검색어</h3>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="(query, index) in recommendedQueries"
              :key="index"
              class="px-4 py-2.5 bg-white border border-gray-200 rounded-lg text-sm text-gray-700 hover:border-primary-main hover:text-primary-main transition-colors"
              @click="insertQuery(query)"
            >
              {{ query }}
            </button>
          </div>
        </div>
      </section>

      <section class="mb-12">
        <div class="max-w-7xl mx-auto">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900">인기 매물</h2>
          </div>
          <div
            v-if="popularProperties.length > 0"
            class="flex flex-row flex-nowrap snap-x snap-mandatory scrollbar-hide overflow-x-auto scroll-smooth pb-4 gap-4"
          >
            <PropertyCard
              v-for="property in popularProperties"
              :key="property.itemId"
              class="snap-start shrink-0 w-72"
              :property="property"
              :is-selected="selectedProperty?.itemId === property.itemId"
              @select="selectProperty(property)"
              @detail="openDetail(property)"
              @update="handlePropertyUpdate"
            />
          </div>
          <div v-else class="text-center py-12 text-gray-500">인기 매물을 불러오는 중입니다...</div>
        </div>
      </section>

      <section class="mb-12">
        <div class="max-w-7xl mx-auto bg-white border border-gray-200 rounded-2xl p-8">
          <h2 class="text-2xl font-bold text-gray-900 mb-4 text-center">서비스 소개</h2>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="text-center">
              <div
                class="w-16 h-16 bg-primary-main/10 rounded-full flex items-center justify-center mx-auto mb-4"
              >
                <svg
                  class="w-8 h-8 text-primary-main"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
                  />
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">AI 매물 추천</h3>
              <p class="text-sm text-gray-600">
                자연어로 원하는 조건을 입력하면 AI가 조건에 맞는 매물을 추천합니다.
              </p>
            </div>
            <div class="text-center">
              <div
                class="w-16 h-16 bg-primary-main/10 rounded-full flex items-center justify-center mx-auto mb-4"
              >
                <svg
                  class="w-8 h-8 text-primary-main"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">지역 기반 검색</h3>
              <p class="text-sm text-gray-600">
                시/군/구와 동을 선택해 원하는 지역의 매물을 빠르게 확인할 수 있습니다.
              </p>
            </div>
            <div class="text-center">
              <div
                class="w-16 h-16 bg-primary-main/10 rounded-full flex items-center justify-center mx-auto mb-4"
              >
                <svg
                  class="w-8 h-8 text-primary-main"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                  />
                </svg>
              </div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">찜한 매물 관리</h3>
              <p class="text-sm text-gray-600">
                관심 있는 매물을 저장하고 나중에 다시 확인할 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section class="mb-8">
        <div class="max-w-7xl mx-auto bg-white border border-gray-200 rounded-2xl overflow-hidden">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <h2 class="text-xl font-bold text-gray-900">공지사항</h2>
            <button
              class="text-sm font-medium text-primary-main hover:text-secondary-main transition-colors"
              @click="router.push('/board')"
            >
              더보기
            </button>
          </div>
          <table class="w-full">
            <thead class="w-full bg-gray-50">
              <tr>
                <th class="px-8 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                  제목
                </th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                  작성자
                </th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                  작성일
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr
                v-for="notice in notices"
                :key="notice.boardId"
                class="hover:bg-gray-50 cursor-pointer transition-colors"
                @click="router.push(`/board/${notice.boardId}`)"
              >
                <td class="px-8 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-gray-900">{{ notice.subject }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-600">{{ notice.userName }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-600">
                    {{ new Date(notice.registerTime).toLocaleDateString('ko-KR') }}
                  </div>
                </td>
              </tr>
              <tr v-if="notices.length === 0">
                <td colspan="3" class="px-6 py-8 text-center text-gray-500">
                  공지사항이 없습니다.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <PropertyDetail
      v-if="showDetail && detailProperty"
      :property="detailProperty"
      @close="closeDetail"
      @update="handlePropertyUpdate"
    />
  </div>
</template>
