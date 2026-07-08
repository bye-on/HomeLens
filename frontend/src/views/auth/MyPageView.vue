<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { KakaoMap, KakaoMapMarker, KakaoMapPolyline } from 'vue3-kakao-maps'
import { useAuthStore } from '@/stores/useAuthStore'
import AppHeader from '@/components/common/AppHeader.vue'
import PropertyDetail from '@/components/property/PropertyDetail.vue'
import RoutePlanModal from '@/components/route/RoutePlanModal.vue'
import { propertyApi } from '@/api/property'
import type { PropertyData, SalesType } from '@/type/property'
import { parseJsonArray } from '@/utils/jsonParser'
import { getCardImageUrl } from '@/utils/imageHelper'

interface RouteProperty {
  property: PropertyData
  role: 'origin' | 'destination' | 'waypoint' | 'excluded'
}

interface SavedRoute {
  properties: RouteProperty[]
  routeData: any
  summary: any
  createdAt: string
}

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('profile')
const isEditing = ref(false)
const editForm = ref({ name: '', email: '', phone: '' })

const properties = ref<PropertyData[]>([])
const isLoading = ref(false)
const selectedProperty = ref<PropertyData | null>(null)
const showDetail = ref(false)

// 경로 관련
const showRouteModal = ref(false)
const savedRoute = ref<SavedRoute | null>(null)
const mapKey = ref(0)

const STORAGE_KEY = 'savedRoute'

onMounted(() => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  if (authStore.user) {
    editForm.value = {
      name: authStore.user.userName,
      email: authStore.user.userEmail,
      phone: authStore.user.userTel || '',
    }
  }
  loadSavedRoute()
})

watch(activeTab, (newTab) => {
  if (newTab === 'favorites') {
    handleLikeHome()
  }
})

// 저장된 경로 불러오기
const loadSavedRoute = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      savedRoute.value = JSON.parse(stored)
      mapKey.value++
    }
  } catch (error) {
    console.error('저장된 경로 불러오기 실패:', error)
  }
}

const startEditing = () => {
  isEditing.value = true
}
const cancelEditing = () => {
  if (authStore.user)
    editForm.value = {
      name: authStore.user.userName,
      email: authStore.user.userEmail,
      phone: authStore.user.userTel || '',
    }
  isEditing.value = false
}
const saveProfile = () => {
  authStore.updateProfile({ userName: editForm.value.name, userTel: editForm.value.phone })
  isEditing.value = false
}
const handleLogout = () => {
  authStore.logout()
  router.push('/')
}

const handleLikeHome = async () => {
  isLoading.value = true
  try {
    const data = await propertyApi.getLikeHomeList()
    properties.value = data.map((property) => ({
      ...property,
      userLike: true,
    }))
  } catch (err) {
    console.error('찜한 매물 조회 실패:', err)
    properties.value = []
  } finally {
    isLoading.value = false
  }
}

const formatPrice = (deposit: number, rent: number, salesType: SalesType) => {
  if (salesType === '전세') {
    if (deposit >= 10000) {
      return `${(deposit / 10000).toFixed(1)}억`
    }
    return `${deposit.toLocaleString()}만`
  }
  return `${deposit.toLocaleString()}/${rent}`
}

const getFirstImage = (property: PropertyData) => {
  const images = parseJsonArray<string>(property.itemImage)
  if (images.length > 0) {
    return getCardImageUrl(images[0])
  }
  return '/placeholder-image.jpg'
}

const getAddress = (property: PropertyData) => {
  return `${property.local2} ${property.local3}`
}

const toggleLike = async (itemId: number) => {
  try {
    await propertyApi.toggleLikeHome(itemId)
    const property = properties.value.find((p) => p.itemId === itemId)
    if (property) {
      property.userLike = !property.userLike
    }
  } catch (err) {
    console.error('찜 토글 실패:', err)
  }
}

const openDetail = (property: PropertyData) => {
  selectedProperty.value = property
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
  selectedProperty.value = null
}

const handlePropertyUpdate = (updatedProperty: PropertyData) => {
  if (selectedProperty.value) {
    selectedProperty.value = updatedProperty
  }
  const index = properties.value.findIndex((p) => p.itemId === updatedProperty.itemId)
  if (index !== -1) {
    properties.value[index] = updatedProperty
  }
}

const favoritesCount = computed(() => properties.value.filter((p) => p.userLike).length)

const handleTabClick = (tabId: string) => {
  if (tabId === 'favorites' && activeTab.value === 'favorites') {
    handleLikeHome()
  } else {
    activeTab.value = tabId
  }
}

// 경로 모달 열기
const openRouteModal = async () => {
  if (properties.value.length < 2) {
    try {
      const response = await propertyApi.getLikeHomeList()
      properties.value = response
    } catch (err) {
      return Promise.reject(err)
    }
  }
  showRouteModal.value = true
}

// 경로 저장
const handleRouteSave = (route: SavedRoute) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(route))
    savedRoute.value = route
    mapKey.value++
    alert('경로가 저장되었습니다.')
  } catch (error) {
    console.error('경로 저장 실패:', error)
    alert('경로 저장에 실패했습니다.')
  }
}

const map = ref<kakao.maps.Map>()

// 버튼을 클릭하면 아래 배열의 좌표들이 모두 보이게 지도 범위를 재설정합니다
// 지도를 재설정할 범위정보를 가지고 있을 LatLngBounds 객체를 생성합니다
let bounds: kakao.maps.LatLngBounds

const onLoadKakaoMap = (mapRef: kakao.maps.Map) => {
  map.value = mapRef
  bounds = new kakao.maps.LatLngBounds()
  let marker: kakao.maps.Marker
  let point: kakao.maps.LatLng

  if (savedRoute.value === null) return

  savedRoute.value.properties.forEach((markerInfo) => {
    // 배열의 좌표들이 잘 보이게 마커를 지도에 추가합니다
    point = new kakao.maps.LatLng(markerInfo.property.lat, markerInfo.property.lng)

    marker = new kakao.maps.Marker({ position: point })
    if (map.value !== undefined) {
      marker.setMap(map.value)
    }

    // LatLngBounds 객체에 좌표를 추가합니다
    bounds.extend(point)
  })

  if (map.value !== undefined) {
    map.value.setBounds(bounds)
  }
}

// 지도 중심 계산
const routeMapCenter = computed(() => {
  if (!savedRoute.value) return { lat: 37.5665, lng: 126.978 }

  const included = savedRoute.value.properties.filter((rp) => rp.role !== 'excluded')
  if (included.length === 0) return { lat: 37.5665, lng: 126.978 }

  const avgLat = included.reduce((sum, rp) => sum + rp.property.lat, 0) / included.length
  const avgLng = included.reduce((sum, rp) => sum + rp.property.lng, 0) / included.length
  return { lat: avgLat, lng: avgLng }
})

// 경로 폴리라인 좌표 추출
const getPolylineCoordinates = () => {
  if (!savedRoute.value?.routeData?.routes?.[0]?.sections) return []

  const coordinates: Array<{ lat: number; lng: number }[]> = []

  savedRoute.value.routeData.routes[0].sections.forEach((section: any) => {
    if (!section.roads) return

    section.roads.forEach((road: any) => {
      if (!road.vertexes || road.vertexes.length < 2) return

      const path: Array<{ lat: number; lng: number }> = []
      for (let i = 0; i < road.vertexes.length; i += 2) {
        path.push({
          lng: road.vertexes[i],
          lat: road.vertexes[i + 1],
        })
      }
      if (path.length > 0) {
        coordinates.push(path)
      }
    })
  })

  return coordinates
}

const getRoleLabel = (role: string) => {
  switch (role) {
    case 'origin':
      return '출발지'
    case 'destination':
      return '도착지'
    case 'waypoint':
      return '경유지'
    default:
      return role
  }
}

const getRoleColor = (role: string) => {
  switch (role) {
    case 'origin':
      return 'bg-green-500'
    case 'destination':
      return 'bg-red-500'
    case 'waypoint':
      return 'bg-blue-500'
    default:
      return 'bg-gray-400'
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <AppHeader />

    <main class="px-6 py-10">
      <div class="max-w-3xl mx-auto">
        <!-- Profile Header -->
        <div class="flex items-center gap-5 p-8 bg-primary-main rounded-2xl mb-6">
          <div
            class="w-16 h-16 rounded-full bg-white/20 text-white flex items-center justify-center text-3xl font-extrabold"
          >
            {{ authStore.userName.charAt(0) }}
          </div>
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-white mb-1">{{ authStore.userName }}</h1>
            <p class="text-white/70">{{ authStore.user?.userEmail }}</p>
          </div>
          <button
            class="px-5 py-2.5 bg-white/15 border border-white/30 rounded-lg text-white font-semibold text-sm hover:bg-white/25"
            @click="handleLogout"
          >
            로그아웃
          </button>
        </div>

        <!-- Tabs -->
        <div class="flex gap-2 p-2 bg-white border border-gray-200 rounded-xl mb-6">
          <button
            v-for="tab in [
              { id: 'profile', label: '내 정보' },
              { id: 'favorites', label: '찜한 매물', count: favoritesCount },
              { id: 'route', label: '최단 거리' },
            ]"
            :key="tab.id"
            class="flex-1 flex items-center justify-center gap-2 py-3.5 rounded-lg text-sm font-semibold transition-colors"
            :class="
              activeTab === tab.id
                ? 'bg-primary-main text-white'
                : 'text-gray-500 hover:bg-gray-100'
            "
            @click="handleTabClick(tab.id)"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Profile -->
        <div v-if="activeTab === 'profile'" class="bg-white border border-gray-200 rounded-2xl p-7">
          <div class="flex justify-between items-center mb-6">
            <h2 class="text-lg font-bold text-gray-900">내 정보 관리</h2>
            <button
              v-if="!isEditing"
              class="px-4 py-2 bg-gray-100 rounded-lg text-sm font-semibold text-gray-600 hover:bg-gray-200"
              @click="startEditing"
            >
              수정
            </button>
          </div>
          <div class="space-y-5">
            <div class="space-y-2">
              <label class="text-sm font-semibold text-gray-900">이름</label>
              <input
                v-model="editForm.name"
                type="text"
                :disabled="!isEditing"
                class="w-full px-4 py-3.5 border rounded-lg text-base"
                :class="isEditing ? 'border-primary-main bg-white' : 'border-gray-200 bg-gray-100'"
              />
            </div>
            <div class="space-y-2">
              <label class="text-sm font-semibold text-gray-900">이메일</label>
              <input
                v-model="editForm.email"
                type="email"
                disabled
                class="w-full px-4 py-3.5 border border-gray-200 bg-gray-100 rounded-lg text-base text-gray-400"
              />
              <span class="text-xs text-gray-400">이메일은 변경할 수 없습니다.</span>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-semibold text-gray-900">연락처</label>
              <input
                v-model="editForm.phone"
                type="tel"
                placeholder="010-0000-0000"
                :disabled="!isEditing"
                class="w-full px-4 py-3.5 border rounded-lg text-base"
                :class="isEditing ? 'border-primary-main bg-white' : 'border-gray-200 bg-gray-100'"
              />
            </div>
            <div v-if="isEditing" class="flex justify-end gap-3 pt-2">
              <button
                class="px-6 py-3 bg-gray-100 rounded-lg font-semibold text-gray-600 hover:bg-gray-200"
                @click="cancelEditing"
              >
                취소
              </button>
              <button
                class="px-6 py-3 bg-primary-main rounded-lg font-semibold text-white hover:bg-primary-main"
                @click="saveProfile"
              >
                저장
              </button>
            </div>
          </div>
        </div>

        <!-- Favorites -->
        <div
          v-if="activeTab === 'favorites'"
          class="bg-white border border-gray-200 rounded-2xl p-7"
        >
          <div class="flex justify-between items-center mb-6">
            <h2 class="text-lg font-bold text-gray-900">찜한 매물</h2>
            <span class="text-sm text-gray-500">총 {{ favoritesCount }}개</span>
          </div>
          <div v-if="isLoading" class="text-center py-12">
            <div
              class="inline-block w-8 h-8 border-4 border-primary-main border-t-transparent rounded-full animate-spin"
            ></div>
            <p class="text-gray-500 mt-4">로딩 중...</p>
          </div>
          <div v-else-if="properties.length === 0" class="text-center py-12">
            <span class="text-5xl block mb-4">💝</span>
            <p class="text-gray-500 mb-5">찜한 매물이 없어요</p>
            <router-link
              to="/"
              class="inline-block px-6 py-3 bg-primary-main text-white rounded-lg font-semibold hover:bg-secondary-main transition-colors"
              >매물 검색하기</router-link
            >
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="property in properties"
              :key="property.itemId"
              class="flex items-center gap-4 p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer"
              @click="openDetail(property)"
            >
              <img
                :src="getFirstImage(property)"
                :alt="property.title"
                class="w-24 h-18 object-cover rounded-lg shrink-0"
              />
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-900 mb-1.5 truncate">{{ property.title }}</h3>
                <p class="text-primary-main font-bold text-sm mb-1">
                  {{ formatPrice(property.deposit, property.rent, property.salesType) }}
                </p>
                <p class="text-sm text-gray-500 truncate">{{ getAddress(property) }}</p>
              </div>
              <button
                class="w-10 h-10 flex items-center justify-center bg-white border border-gray-200 rounded-lg transition-colors shrink-0"
                :class="
                  property.userLike
                    ? 'text-red-500 hover:bg-red-50 hover:border-red-200'
                    : 'text-gray-400 hover:bg-gray-50 hover:border-gray-300'
                "
                @click.stop="toggleLike(property.itemId)"
              >
                <svg v-if="property.userLike" class="w-5 h-5 fill-current" viewBox="0 0 24 24">
                  <path
                    d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                  />
                </svg>
                <svg
                  v-else
                  class="w-5 h-5"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Route (최단 거리) -->
        <div v-if="activeTab === 'route'" class="bg-white border border-gray-200 rounded-2xl p-7">
          <div class="flex justify-between items-center mb-6">
            <h2 class="text-lg font-bold text-gray-900">최단 거리</h2>
            <span v-if="!!savedRoute">
              <button
                class="px-4 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors"
                @click="openRouteModal"
              >
                경로 수정
              </button>
            </span>
            <span v-else> </span>
          </div>

          <!-- 저장된 경로가 없을 때 -->
          <div v-if="!savedRoute" class="text-center py-12">
            <span class="text-5xl block mb-4">🗺️</span>
            <p class="text-gray-500 mb-5">저장된 경로가 없어요</p>
            <button
              class="inline-block px-6 py-3 bg-primary-main text-white rounded-lg font-semibold hover:bg-secondary-main transition-colors"
              @click="openRouteModal"
            >
              경로 추가하기
            </button>
          </div>

          <!-- 저장된 경로가 있을 때 -->
          <div v-else class="space-y-4">
            <!-- 지도 -->
            <div class="h-96 rounded-xl overflow-hidden border border-gray-200">
              <KakaoMap
                :key="mapKey"
                :lat="routeMapCenter.lat"
                :lng="routeMapCenter.lng"
                :level="6"
                width="100%"
                height="100%"
                @on-load-kakao-map="onLoadKakaoMap"
              >
                <!-- 매물 마커 -->
                <template
                  v-for="rp in savedRoute.properties.filter((p) => p.role !== 'excluded')"
                  :key="`marker-${rp.property.itemId}`"
                >
                  <KakaoMapMarker :lat="rp.property.lat" :lng="rp.property.lng">
                    <div
                      class="px-3 py-1.5 rounded-lg text-white text-xs font-semibold whitespace-nowrap"
                      :class="getRoleColor(rp.role)"
                    >
                      {{ getRoleLabel(rp.role) }}
                    </div>
                  </KakaoMapMarker>
                </template>

                <!-- 경로 폴리라인 -->
                <template
                  v-for="(path, index) in getPolylineCoordinates()"
                  :key="`polyline-${index}`"
                >
                  <KakaoMapPolyline
                    :lat-lng-list="path"
                    :stroke-color="'#326CF9'"
                    :stroke-opacity="0.8"
                    :stroke-weight="4"
                  />
                </template>
              </KakaoMap>
            </div>

            <!-- 경유 매물 리스트 -->
            <div class="space-y-2">
              <h3 class="text-sm font-semibold text-gray-900 mb-2">경유 매물</h3>
              <div
                v-for="rp in savedRoute.properties.filter((p) => p.role !== 'excluded')"
                :key="rp.property.itemId"
                class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
              >
                <span
                  class="px-2 py-1 text-xs font-semibold text-white rounded shrink-0"
                  :class="getRoleColor(rp.role)"
                >
                  {{ getRoleLabel(rp.role) }}
                </span>
                <img
                  :src="getFirstImage(rp.property)"
                  :alt="rp.property.title"
                  class="w-16 h-12 object-cover rounded shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <h4 class="text-sm font-semibold text-gray-900 truncate">
                    {{ rp.property.title }}
                  </h4>
                  <p class="text-xs text-primary-main font-semibold">
                    {{ formatPrice(rp.property.deposit, rp.property.rent, rp.property.salesType) }}
                  </p>
                  <p class="text-xs text-gray-500 truncate">{{ getAddress(rp.property) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Property Detail Modal -->
    <PropertyDetail
      v-if="showDetail && selectedProperty"
      :property="selectedProperty"
      @close="closeDetail"
      @update="handlePropertyUpdate"
    />

    <!-- Route Plan Modal -->
    <RoutePlanModal
      v-if="showRouteModal"
      :properties="properties"
      @close="showRouteModal = false"
      @save="handleRouteSave"
    />
  </div>
</template>
