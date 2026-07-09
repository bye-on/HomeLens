<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { KakaoMap, KakaoMapMarker, KakaoMapPolyline } from 'vue3-kakao-maps'
import type { PropertyData } from '@/type/property'
import { getShortestDistance, type ShortestDistanceRequest } from '@/api/shortestDistance'
import { getCardImageUrl } from '@/utils/imageHelper'
import { parseJsonArray } from '@/utils/jsonParser'

interface Props {
  properties: PropertyData[]
}

type RouteRole = 'origin' | 'destination' | 'waypoint' | 'excluded'

interface RouteProperty {
  property: PropertyData
  role: RouteRole
}

interface SavedRoute {
  properties: RouteProperty[]
  routeData: any
  summary: any
  createdAt: string
}

const props = defineProps<Props>()
const emit = defineEmits<{ close: []; save: [route: SavedRoute] }>()

const routeProperties = ref<RouteProperty[]>([])
const isLoading = ref(false)
const routeData = ref<any>(null)
const mapKey = ref(0) // 지도 강제 리렌더링용

const map = ref<kakao.maps.Map>()
const mapContainer = ref<HTMLElement | null>(null)
let resizeObserver: ResizeObserver | null = null

// 지도 relayout (크기 재조정)
const relayoutMap = () => {
  if (map.value) {
    // 약간의 지연을 두고 relayout 호출 (렌더링 완료 대기)
    setTimeout(() => {
      map.value?.relayout()
    }, 100)
  }
}

// 지도 bounds 업데이트 함수
const updateMapBounds = () => {
  if (!map.value) return

  const included = routeProperties.value.filter((rp) => rp.role !== 'excluded')
  if (included.length === 0) {
    // 매물이 없으면 기본 중심점으로 설정
    map.value.setCenter(new kakao.maps.LatLng(mapCenter.value.lat, mapCenter.value.lng))
    relayoutMap()
    return
  }

  const bounds = new kakao.maps.LatLngBounds()
  included.forEach((markerInfo) => {
    const point = new kakao.maps.LatLng(markerInfo.property.lat, markerInfo.property.lng)
    bounds.extend(point)
  })

  map.value.setBounds(bounds)
  relayoutMap()
}

const onLoadKakaoMap = (mapRef: kakao.maps.Map) => {
  map.value = mapRef
  // 지도가 로드되면 bounds 업데이트 및 relayout
  nextTick(() => {
    updateMapBounds()
    // 모달이 열린 직후 지도가 제대로 렌더링되도록 추가 relayout
    setTimeout(() => {
      relayoutMap()
    }, 300)
  })
}

// 지도 중심 계산
const mapCenter = computed(() => {
  const included = routeProperties.value.filter((rp) => rp.role !== 'excluded')
  if (included.length === 0) return { lat: 37.5665, lng: 126.978 }

  const avgLat = included.reduce((sum, rp) => sum + rp.property.lat, 0) / included.length
  const avgLng = included.reduce((sum, rp) => sum + rp.property.lng, 0) / included.length
  return { lat: avgLat, lng: avgLng }
})

// 초기화: 찜한 매물을 경유지로 설정
onMounted(() => {
  routeProperties.value = props.properties.map((p, index) => ({
    property: p,
    role:
      index === 0
        ? 'origin'
        : index === props.properties.length - 1
          ? 'destination'
          : 'waypoint',
  }))
})

// routeProperties 변경 시 지도 bounds 업데이트
watch(
  routeProperties,
  () => {
    if (map.value) {
      nextTick(() => {
        updateMapBounds()
      })
    }
  },
  { deep: true },
)

// 컨테이너 크기 변경 감지
onMounted(() => {
  // ResizeObserver로 컨테이너 크기 변경 감지
  if (mapContainer.value) {
    resizeObserver = new ResizeObserver(() => {
      relayoutMap()
    })
    resizeObserver.observe(mapContainer.value)
  }
})

onUnmounted(() => {
  if (resizeObserver && mapContainer.value) {
    resizeObserver.unobserve(mapContainer.value)
    resizeObserver.disconnect()
    resizeObserver = null
  }
})

// 역할 순환: excluded -> waypoint -> origin -> destination -> excluded
const toggleRole = (index: number) => {
  const current = routeProperties.value[index]
  if (!current) return

  const roles: RouteRole[] = [
    'excluded',
    'waypoint',
    'origin',
    'destination',
  ]
  const currentIndex = roles.indexOf(current.role)
  const nextIndex = (currentIndex + 1) % roles.length
  const nextRole = roles[nextIndex]
  if (!nextRole) return

  current.role = nextRole

  // origin과 destination은 각각 하나만 유지
  if (current.role === 'origin' || current.role === 'destination') {
    routeProperties.value.forEach((rp, i) => {
      if (i !== index && rp.role === current.role) {
        rp.role = 'waypoint'
      }
    })
  }
}

// 경로 계산
const calculateRoute = async () => {
  const included = routeProperties.value.filter((rp) => rp.role !== 'excluded')
  if (included.length < 2) {
    alert('출발지와 목적지가 필요합니다.')
    return
  }

  const origin = routeProperties.value.find((rp) => rp.role === 'origin')
  const destination = routeProperties.value.find((rp) => rp.role === 'destination')

  if (!origin || !destination) {
    alert('출발지와 목적지를 설정해주세요.')
    return
  }

  const waypoints = routeProperties.value
    .filter((rp) => rp.role === 'waypoint')
    .map((rp) => ({
      name: rp.property.title,
      x: rp.property.lng,
      y: rp.property.lat,
    }))

  isLoading.value = true
  try {
    const request: ShortestDistanceRequest = {
      origin: {
        name: origin.property.title,
        x: origin.property.lng,
        y: origin.property.lat,
      },
      destination: {
        name: destination.property.title,
        x: destination.property.lng,
        y: destination.property.lat,
      },
      waypoints: waypoints,
    }

    const response = await getShortestDistance(request)
    routeData.value = response.data

    // 지도 리렌더링
    mapKey.value++
  } catch (error) {
    console.error('경로 계산 실패:', error)
    alert('경로 계산에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

// 경로 저장
const saveRoute = () => {
  if (!routeData.value) {
    alert('먼저 경로를 계산해주세요.')
    return
  }

  const savedRoute: SavedRoute = {
    properties: routeProperties.value,
    routeData: routeData.value,
    summary: routeData.value.routes[0]?.summary,
    createdAt: new Date().toISOString(),
  }

  emit('save', savedRoute)
  emit('close')
}

// 경로 폴리라인 좌표 추출
const getPolylineCoordinates = () => {
  if (!routeData.value?.routes?.[0]?.sections) return []

  const coordinates: Array<{ lat: number; lng: number }[]> = []

  routeData.value.routes[0].sections.forEach((section: any) => {
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
    case 'excluded':
      return '제외'
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
    case 'excluded':
      return 'bg-gray-400'
    default:
      return 'bg-gray-400'
  }
}

const formatPrice = (deposit: number, rent: number, salesType: string | null) => {
  if (salesType === '전세') {
    if (deposit >= 10000) return `${(deposit / 10000).toFixed(1)}억`
    return `${deposit.toLocaleString()}만`
  }
  return `${deposit.toLocaleString()}/${rent}`
}

const getFirstImage = (property: PropertyData) => {
  const images = parseJsonArray<string>(property.itemImage)
  if (images.length > 0) return getCardImageUrl(images[0])
  return '/placeholder-image.jpg'
}
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
    @click.self="emit('close')"
  >
    <div class="w-full max-w-6xl bg-white rounded-2xl overflow-hidden flex flex-col max-h-[90vh]">
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-bold text-gray-900">경로 설정</h2>
        <button
          class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 transition-colors"
          @click="emit('close')"
        >
          <svg
            class="w-6 h-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="flex-1 overflow-hidden flex">
        <!-- Left: 매물 리스트 -->
        <div class="w-96 border-r border-gray-200 flex flex-col overflow-hidden">
          <div class="p-4 border-b border-gray-200">
            <p class="text-sm text-gray-600 mb-3">
              매물을 클릭하여 역할을 변경하세요 (제외 → 경유지 → 출발지 → 도착지)
            </p>
            <button
              class="w-full px-4 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors disabled:opacity-50"
              :disabled="isLoading"
              @click="calculateRoute"
            >
              {{ isLoading ? '경로 계산 중...' : '경로 계산' }}
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-4 space-y-3">
            <div
              v-for="(rp, index) in routeProperties"
              :key="rp.property.itemId"
              class="p-3 bg-gray-50 rounded-lg border-2 cursor-pointer transition-all"
              :class="
                rp.role === 'origin'
                  ? 'border-green-500 bg-green-50'
                  : rp.role === 'destination'
                    ? 'border-red-500 bg-red-50'
                    : rp.role === 'waypoint'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 bg-gray-100 opacity-50'
              "
              @click="toggleRole(index)"
            >
              <div class="flex items-start gap-3">
                <img
                  :src="getFirstImage(rp.property)"
                  :alt="rp.property.title"
                  class="w-16 h-12 object-cover rounded shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1">
                    <span
                      class="px-2 py-0.5 text-xs font-semibold text-white rounded"
                      :class="getRoleColor(rp.role)"
                    >
                      {{ getRoleLabel(rp.role) }}
                    </span>
                  </div>
                  <h3 class="text-sm font-semibold text-gray-900 truncate mb-1">
                    {{ rp.property.title }}
                  </h3>
                  <p class="text-xs text-primary-main font-semibold mb-1">
                    {{ formatPrice(rp.property.deposit, rp.property.rent, rp.property.salesType) }}
                  </p>
                  <p class="text-xs text-gray-500 truncate">
                    {{ rp.property.local2 }} {{ rp.property.local3 }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: 지도 -->
        <div ref="mapContainer" class="flex-1 relative min-h-0">
          <KakaoMap
            :key="mapKey"
            :lat="mapCenter.lat"
            :lng="mapCenter.lng"
            :level="6"
            width="100%"
            height="100%"
            @on-load-kakao-map="onLoadKakaoMap"
          >
            <!-- 매물 마커 -->
            <template v-for="rp in routeProperties" :key="`marker-${rp.property.itemId}`">
              <KakaoMapMarker
                v-if="rp.role !== 'excluded'"
                :lat="rp.property.lat"
                :lng="rp.property.lng"
              >
                <div
                  class="px-3 py-1.5 rounded-lg text-white text-xs font-semibold whitespace-nowrap"
                  :class="getRoleColor(rp.role)"
                >
                  {{ getRoleLabel(rp.role) }}
                </div>
              </KakaoMapMarker>
            </template>

            <!-- 경로 폴리라인 -->
            <template v-for="(path, index) in getPolylineCoordinates()" :key="`polyline-${index}`">
              <KakaoMapPolyline
                :lat-lng-list="path"
                :stroke-color="'#326CF9'"
                :stroke-opacity="0.8"
                :stroke-weight="4"
              />
            </template>
          </KakaoMap>

          <!-- 경로 정보 -->
          <div
            v-if="routeData?.routes?.[0]?.summary"
            class="absolute bottom-4 left-4 right-4 bg-white rounded-lg shadow-lg p-4 border border-gray-200"
          >
            <div class="grid grid-cols-3 gap-4 text-center">
              <div>
                <p class="text-xs text-gray-500 mb-1">총 거리</p>
                <p class="text-lg font-bold text-gray-900">
                  {{ (routeData.routes[0].summary.distance / 1000).toFixed(1) }}km
                </p>
              </div>
              <div>
                <p class="text-xs text-gray-500 mb-1">예상 시간</p>
                <p class="text-lg font-bold text-gray-900">
                  {{ Math.round(routeData.routes[0].summary.duration / 60) }}분
                </p>
              </div>
              <div>
                <p class="text-xs text-gray-500 mb-1">통행료</p>
                <p class="text-lg font-bold text-gray-900">
                  {{ routeData.routes[0].summary.fare.toll.toLocaleString() }}원
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-200">
        <button
          class="px-6 py-2.5 bg-gray-100 text-gray-600 text-sm font-semibold rounded-lg hover:bg-gray-200 transition-colors"
          @click="emit('close')"
        >
          취소
        </button>
        <button
          class="px-6 py-2.5 bg-primary-main text-white text-sm font-semibold rounded-lg hover:bg-secondary-main transition-colors disabled:opacity-50"
          :disabled="!routeData"
          @click="saveRoute"
        >
          경로 저장
        </button>
      </div>
    </div>
  </div>
</template>
