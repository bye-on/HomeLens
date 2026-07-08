<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KakaoMap, KakaoMapMarker, KakaoMapCustomOverlay } from 'vue3-kakao-maps'
import PropertyCard from '@/components/property/PropertyCard.vue'
import PropertyDetail from '@/components/property/PropertyDetail.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { propertyApi } from '@/api/property'
import type { PropertyData } from '@/type/property'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const searchQuery = ref((route.query.q as string) || '')
const searchMode = ref<'basic' | 'ai'>((route.query.mode as string) === 'ai' ? 'ai' : 'basic')
const isLoading = ref(true)
const properties = ref<PropertyData[]>([])
const selectedProperty = ref<PropertyData | null>(null)
const detailProperty = ref<PropertyData | null>(null)
const showDetail = ref(false)
const map = ref<kakao.maps.Map>()

const modeLabel = computed(() => (searchMode.value === 'ai' ? 'AI 검색' : '일반 검색'))
const loadingTitle = computed(() =>
  searchMode.value === 'ai' ? 'AI가 매물을 찾고 있어요' : '매물을 검색하고 있어요',
)
const resultTitle = computed(() =>
  searchMode.value === 'ai' ? 'AI 추천 결과' : '일반 검색 결과',
)

const fetchProperties = async () => {
  isLoading.value = true
  try {
    const response = await propertyApi.search(searchQuery.value, searchMode.value)
    properties.value = [...(response.primary || []), ...(response.regionOnly || [])]
    selectedProperty.value = properties.value[0] ?? null
  } catch (error) {
    console.error('매물 검색 실패:', error)
    properties.value = []
    selectedProperty.value = null
  } finally {
    isLoading.value = false
  }
}

const onLoadKakaoMap = (mapRef: kakao.maps.Map) => {
  map.value = mapRef
  if (properties.value.length === 0) return

  const bounds = new kakao.maps.LatLngBounds()
  properties.value.forEach((property) => {
    bounds.extend(new kakao.maps.LatLng(property.lat, property.lng))
  })
  map.value.setBounds(bounds)
}

const mapCenter = computed(() => {
  if (selectedProperty.value) {
    return {
      lat: selectedProperty.value.lat,
      lng: selectedProperty.value.lng,
    }
  }
  if (properties.value.length > 0) {
    const avgLat = properties.value.reduce((sum, property) => sum + property.lat, 0) / properties.value.length
    const avgLng = properties.value.reduce((sum, property) => sum + property.lng, 0) / properties.value.length
    return { lat: avgLat, lng: avgLng }
  }
  return { lat: 37.5665, lng: 126.978 }
})

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
  const index = properties.value.findIndex((property) => property.itemId === updatedProperty.itemId)
  if (index !== -1) {
    properties.value[index] = updatedProperty
  }
}

const goBack = () => {
  router.push('/')
}

const formatPrice = (deposit: number, rent: number, salesType: string | null) => {
  if (salesType === '전세') {
    if (deposit >= 10000) return `${(deposit / 10000).toFixed(1)}억`
    return `${deposit.toLocaleString()}만`
  }
  return `${deposit.toLocaleString()}/${rent.toLocaleString()}`
}

onMounted(() => {
  fetchProperties()
})
</script>

<template>
  <div class="flex flex-col h-screen bg-gray-50">
    <header class="flex items-center gap-2 justify-between px-6 h-16 bg-white border-b border-gray-200">
      <div class="flex items-center gap-4">
        <button
          class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 hover:text-gray-900 transition-colors"
          aria-label="뒤로가기"
          @click="goBack"
        >
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <div class="cursor-pointer" @click="goBack">
          <span class="text-xl font-extrabold text-primary-main">Home</span>
          <span class="text-xl font-extrabold text-text-mute">Lens</span>
        </div>
      </div>

      <div class="hidden sm:flex items-center gap-2 px-4 py-2 bg-gray-100 rounded-full text-sm text-gray-600 min-w-0">
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <span class="flex-1 truncate min-w-0">{{ searchQuery || modeLabel }}</span>
      </div>

      <div class="flex items-center gap-4">
        <span class="hidden lg:block text-sm text-gray-500 font-medium min-w-auto truncate">
          {{ properties.length }}개 매물
        </span>
        <template v-if="authStore.isLoggedIn">
          <div class="relative">
            <button
              class="w-9 h-9 rounded-full bg-primary-main text-white flex items-center justify-center font-semibold text-sm"
              @click="showUserMenu = !showUserMenu"
            >
              {{ authStore.userName.charAt(0) }}
            </button>
            <div
              v-if="showUserMenu"
              class="absolute top-full right-0 mt-2 w-36 bg-white border border-gray-200 rounded-xl shadow-lg p-2 z-50"
            >
              <button
                class="w-full px-4 py-2.5 text-left text-sm text-gray-600 rounded-lg hover:bg-gray-100"
                @click="router.push('/mypage')"
              >
                마이페이지
              </button>
              <button
                class="w-full px-4 py-2.5 text-left text-sm text-red-500 rounded-lg hover:bg-red-50"
                @click="authStore.logout()"
              >
                로그아웃
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <button
            class="px-5 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
            @click="router.push('/login')"
          >
            로그인
          </button>
        </template>
      </div>
    </header>

    <div v-if="isLoading" class="flex-1 flex items-center justify-center bg-gray-50">
      <div class="flex flex-col items-center gap-6">
        <div class="relative">
          <div class="w-24 h-24 bg-primary-main/10 rounded-full flex items-center justify-center animate-pulse">
            <svg class="w-12 h-12 text-primary-main" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
              <polyline points="9,22 9,12 15,12 15,22" />
            </svg>
          </div>
          <div class="absolute inset-0 animate-spin" style="animation-duration: 2s">
            <div class="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1 w-3 h-3 bg-primary-main rounded-full"></div>
          </div>
        </div>

        <div class="text-center">
          <h2 class="text-xl font-bold text-gray-900 mb-2">{{ loadingTitle }}</h2>
          <p class="text-gray-500">
            "{{ searchQuery || '추천' }}" 조건에 맞는 매물을 불러오는 중입니다...
          </p>
        </div>

        <div class="w-64 h-1.5 bg-gray-200 rounded-full overflow-hidden">
          <div class="h-full bg-primary-main rounded-full animate-progress"></div>
        </div>
      </div>
    </div>

    <main v-else class="flex-1 flex overflow-hidden">
      <div v-if="properties.length === 0" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <div class="w-20 h-20 mx-auto mb-6 bg-gray-100 rounded-full flex items-center justify-center">
            <svg class="w-10 h-10 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
              <path d="M8 8l6 6M14 8l-6 6" />
            </svg>
          </div>
          <h2 class="text-xl font-bold text-gray-900 mb-2">검색 결과가 없습니다</h2>
          <p class="text-gray-500 mb-6">다른 조건으로 다시 검색해보세요.</p>
          <button
            class="px-6 py-3 bg-primary-main text-white font-semibold rounded-lg hover:bg-secondary-main"
            @click="goBack"
          >
            다시 검색하기
          </button>
        </div>
      </div>

      <template v-else>
        <aside class="w-105 bg-white border-r border-gray-200 flex flex-col">
          <div class="p-5 border-b border-gray-100">
            <div class="flex gap-3 p-4 bg-gray-100 rounded-xl">
              <span class="text-2xl">⌕</span>
              <div>
                <strong class="block text-gray-900 text-sm mb-1">{{ resultTitle }}</strong>
                <p class="text-sm text-gray-500">
                  "{{ searchQuery || '추천' }}" 조건의 매물입니다.
                </p>
              </div>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-4 space-y-4">
            <PropertyCard
              v-for="property in properties"
              :key="property.itemId"
              :property="property"
              :is-selected="selectedProperty?.itemId === property.itemId"
              @select="selectProperty(property)"
              @detail="openDetail(property)"
              @update="handlePropertyUpdate"
            />
          </div>
        </aside>

        <div class="flex-1 relative">
          <KakaoMap
            :lat="mapCenter.lat"
            :lng="mapCenter.lng"
            :level="7"
            width="100%"
            height="100%"
            @on-load-kakao-map="onLoadKakaoMap"
          >
            <template v-for="property in properties" :key="property.itemId">
              <KakaoMapMarker
                :lat="property.lat"
                :lng="property.lng"
                :clickable="true"
                @onClickKakaoMapMarker="selectProperty(property)"
              />
              <KakaoMapCustomOverlay
                v-if="selectedProperty?.itemId === property.itemId"
                :lat="property.lat"
                :lng="property.lng"
                :y-anchor="1.4"
              >
                <div
                  class="flex items-center gap-2 px-4 py-2.5 bg-white rounded-lg shadow-lg cursor-pointer hover:scale-105 transition-transform"
                  @click="openDetail(property)"
                >
                  <span
                    class="px-2 py-1 rounded text-[10px] font-semibold"
                    :class="
                      selectedProperty.salesType === '전세'
                        ? 'bg-primary-main text-white'
                        : 'bg-secondary-main text-white'
                    "
                  >
                    {{ property.salesType }}
                  </span>
                  <span class="text-base font-extrabold text-gray-900">
                    {{ formatPrice(property.deposit, property.rent, property.salesType) }}
                  </span>
                </div>
              </KakaoMapCustomOverlay>
            </template>
          </KakaoMap>
        </div>
      </template>
    </main>

    <PropertyDetail
      v-if="showDetail && detailProperty"
      :property="detailProperty"
      @close="closeDetail"
      @update="handlePropertyUpdate"
    />
  </div>
</template>

<style scoped>
@keyframes progress {
  0% {
    width: 0%;
    margin-left: 0%;
  }
  50% {
    width: 60%;
    margin-left: 20%;
  }
  100% {
    width: 0%;
    margin-left: 100%;
  }
}

.animate-progress {
  animation: progress 1.5s ease-in-out infinite;
}
</style>