<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { KakaoMap, KakaoMapCustomOverlay } from 'vue3-kakao-maps'
import PropertyCard from '@/components/property/PropertyCard.vue'
import PropertyDetail from '@/components/property/PropertyDetail.vue'
import { useAuthStore } from '@/stores/useAuthStore'
import { propertyApi } from '@/api/property'
import type { PropertyData } from '@/type/property'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const isLoading = ref(true)
const searchQuery = ref((route.query.q as string) || '')
const searchMode = ref<'basic' | 'ai'>((route.query.mode as string) === 'ai' ? 'ai' : 'basic')
const properties = ref<PropertyData[]>([])
const selectedProperty = ref<PropertyData | null>(null)
const selectedCluster = ref<PropertyCluster | null>(null)
const detailProperty = ref<PropertyData | null>(null)
const showDetail = ref(false)
const map = ref<kakao.maps.Map>()
const mapLevel = ref(7)
const isMapMode = computed(() => searchMode.value !== 'ai')
const isRefreshingMap = ref(false)
let mapFetchTimer: ReturnType<typeof setTimeout> | null = null
const mapEventHandlers: Array<{ type: string; handler: () => void }> = []

const selectedDo = ref<string | null>((route.query.local1 as string) || null)
const selectedSi = ref<string | null>((route.query.local2 as string) || null)
const selectedDong = ref<string | null>((route.query.local3 as string) || null)
const doList = ref<string[]>([])
const siList = ref<string[]>([])
const dongList = ref<string[]>([])
const openRegionMenu = ref<'do' | 'si' | 'dong' | null>(null)
const isLoadingRegion = ref(false)

interface PropertyCluster {
  id: string
  lat: number
  lng: number
  count: number
  properties: PropertyData[]
}

const resultLabel = computed(() => {
  if (searchMode.value === 'ai') return 'AI 추천 매물'
  if (searchQuery.value) return '지역 검색 매물'
  return '전체 매물 지도'
})

const regionLabel = computed(() => {
  return [selectedDo.value, selectedSi.value, selectedDong.value].filter(Boolean).join(' ')
})

const listedProperties = computed(() => selectedCluster.value?.properties ?? properties.value)

const visibleClusters = computed<PropertyCluster[]>(() => {
  const level = mapLevel.value
  const step =
    level <= 4
      ? 0.0012
      : level <= 6
        ? 0.006
      : level <= 8
          ? 0.02
          : level <= 10
            ? 0.05
            : 0.12

  const clusterMap = new Map<string, PropertyData[]>()

  properties.value.forEach((property) => {
    const latKey = Math.floor(property.lat / step)
    const lngKey = Math.floor(property.lng / step)
    const key = `${latKey}:${lngKey}`
    const bucket = clusterMap.get(key) || []
    bucket.push(property)
    clusterMap.set(key, bucket)
  })

  return Array.from(clusterMap.entries()).map(([id, clusterProperties]) => {
    const lat =
      clusterProperties.reduce((sum, property) => sum + property.lat, 0) / clusterProperties.length
    const lng =
      clusterProperties.reduce((sum, property) => sum + property.lng, 0) / clusterProperties.length

    return {
      id,
      lat,
      lng,
      count: clusterProperties.length,
      properties: clusterProperties,
    }
  })
})

const mapCenter = computed(() => {
  if (selectedProperty.value) {
    return { lat: selectedProperty.value.lat, lng: selectedProperty.value.lng }
  }

  if (properties.value.length > 0) {
    const lat = properties.value.reduce((sum, property) => sum + property.lat, 0) / properties.value.length
    const lng = properties.value.reduce((sum, property) => sum + property.lng, 0) / properties.value.length
    return { lat, lng }
  }

  return { lat: 37.5665, lng: 126.978 }
})

const formatPrice = (deposit: number, rent: number, salesType: string | null) => {
  if (salesType === '전세') {
    if (deposit >= 10000) return `${(deposit / 10000).toFixed(1)}억`
    return `${deposit.toLocaleString()}만`
  }
  return `${deposit.toLocaleString()}/${rent.toLocaleString()}`
}

const setProperties = (nextProperties: PropertyData[]) => {
  properties.value = nextProperties
  selectedCluster.value = null
  selectedProperty.value = nextProperties[0] ?? null
}

const measureRenderAfterData = async (label: string, start: number) => {
  await nextTick()

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      const end = performance.now()
      console.log(`[perf] ${label}: ${(end - start).toFixed(1)}ms`)
    })
  })
}

const fitMapToProperties = () => {
  if (!map.value || properties.value.length === 0) return

  const bounds = new kakao.maps.LatLngBounds()
  properties.value.forEach((property) => {
    bounds.extend(new kakao.maps.LatLng(property.lat, property.lng))
  })
  map.value.setBounds(bounds)
}

const fetchProperties = async () => {
  isLoading.value = true
  try {
    if (searchMode.value === 'ai' && searchQuery.value) {
      const response = await propertyApi.search(searchQuery.value, searchMode.value)
      setProperties([...(response.primary || []), ...(response.regionOnly || [])])
      fitMapToProperties()
      return
    }

    if (map.value) {
      await fetchMapProperties()
      return
    }

    const response = await propertyApi.getMapProperties({
      local1: selectedDo.value || undefined,
      local2: selectedSi.value || undefined,
      local3: selectedDong.value || undefined,
      size: 1000,
    })
    setProperties(response || [])
  } catch (error) {
    console.error('매물 검색 실패:', error)
    setProperties([])
  } finally {
    isLoading.value = false
  }
}

const getMapBoundsParams = () => {
  if (!map.value) return {}

  const bounds = map.value.getBounds()
  const sw = bounds.getSouthWest()
  const ne = bounds.getNorthEast()

  return {
    swLat: sw.getLat(),
    swLng: sw.getLng(),
    neLat: ne.getLat(),
    neLng: ne.getLng(),
  }
}

const fetchMapProperties = async () => {
  if (!isMapMode.value) return

  const requestStart = performance.now()
  isRefreshingMap.value = true
  try {
    const response = await propertyApi.getMapProperties({
      ...getMapBoundsParams(),
      local1: selectedDo.value || undefined,
      local2: selectedSi.value || undefined,
      local3: selectedDong.value || undefined,
      size: 1200,
    })
    const apiEnd = performance.now()
    console.log(`[perf] map api: ${(apiEnd - requestStart).toFixed(1)}ms`)

    setProperties(response || [])
    await measureRenderAfterData('map data to rendered', apiEnd)
  } catch (error) {
    console.error('지도 매물 조회 실패:', error)
    setProperties([])
  } finally {
    isLoading.value = false
    isRefreshingMap.value = false
  }
}

const scheduleMapRefresh = () => {
  if (!isMapMode.value) return

  if (mapFetchTimer) {
    clearTimeout(mapFetchTimer)
  }

  mapFetchTimer = setTimeout(() => {
    mapLevel.value = map.value?.getLevel() ?? mapLevel.value
    fetchMapProperties()
  }, 250)
}

const loadDoList = async () => {
  openRegionMenu.value = openRegionMenu.value === 'do' ? null : 'do'
  if (doList.value.length > 0 || openRegionMenu.value !== 'do') return

  isLoadingRegion.value = true
  try {
    doList.value = await propertyApi.getDo()
  } finally {
    isLoadingRegion.value = false
  }
}

const loadSiList = async () => {
  if (!selectedDo.value) return
  openRegionMenu.value = openRegionMenu.value === 'si' ? null : 'si'
  if (siList.value.length > 0 || openRegionMenu.value !== 'si') return

  isLoadingRegion.value = true
  try {
    siList.value = await propertyApi.getSi(selectedDo.value)
  } finally {
    isLoadingRegion.value = false
  }
}

const loadDongList = async () => {
  if (!selectedDo.value || !selectedSi.value) return
  openRegionMenu.value = openRegionMenu.value === 'dong' ? null : 'dong'
  if (dongList.value.length > 0 || openRegionMenu.value !== 'dong') return

  isLoadingRegion.value = true
  try {
    dongList.value = await propertyApi.getDong(selectedDo.value, selectedSi.value)
  } finally {
    isLoadingRegion.value = false
  }
}

const selectDo = (doName: string) => {
  selectedDo.value = doName
  selectedSi.value = null
  selectedDong.value = null
  siList.value = []
  dongList.value = []
  openRegionMenu.value = null
}

const selectSi = (siName: string) => {
  selectedSi.value = siName
  selectedDong.value = null
  dongList.value = []
  openRegionMenu.value = null
}

const selectDong = (dongName: string) => {
  selectedDong.value = dongName
  openRegionMenu.value = null
}

const applyRegionSearch = () => {
  const query = regionLabel.value.trim()
  router.push({
    path: '/result',
    query: {
      ...(query ? { q: query } : {}),
      mode: 'basic',
      local1: selectedDo.value || undefined,
      local2: selectedSi.value || undefined,
      local3: selectedDong.value || undefined,
    },
  })
}

const clearRegionSearch = () => {
  selectedDo.value = null
  selectedSi.value = null
  selectedDong.value = null
  siList.value = []
  dongList.value = []
  openRegionMenu.value = null
  router.push('/result')
}

const focusProperty = (property: PropertyData, open = false) => {
  selectedProperty.value = property
  map.value?.panTo(new kakao.maps.LatLng(property.lat, property.lng))
  if (open) openDetail(property)
}

const focusCluster = (cluster: PropertyCluster) => {
  if (!map.value) return

  selectedCluster.value = cluster
  selectedProperty.value = cluster.properties[0] ?? null
  map.value.panTo(new kakao.maps.LatLng(cluster.lat, cluster.lng))
}

const clearSelectedCluster = () => {
  selectedCluster.value = null
  selectedProperty.value = properties.value[0] ?? null
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

const onLoadKakaoMap = (mapRef: kakao.maps.Map) => {
  map.value = mapRef
  mapLevel.value = mapRef.getLevel()

  const refreshHandler = () => scheduleMapRefresh()
  kakao.maps.event.addListener(mapRef, 'dragend', refreshHandler)
  kakao.maps.event.addListener(mapRef, 'zoom_changed', refreshHandler)
  mapEventHandlers.push({ type: 'dragend', handler: refreshHandler })
  mapEventHandlers.push({ type: 'zoom_changed', handler: refreshHandler })

  fetchMapProperties()
}

const goHome = () => {
  router.push('/')
}

watch(
  () => route.query,
  () => {
    searchQuery.value = (route.query.q as string) || ''
    searchMode.value = (route.query.mode as string) === 'ai' ? 'ai' : 'basic'
    selectedDo.value = (route.query.local1 as string) || null
    selectedSi.value = (route.query.local2 as string) || null
    selectedDong.value = (route.query.local3 as string) || null
    fetchProperties()
  },
)

onMounted(() => {
  fetchProperties()
})

onUnmounted(() => {
  if (mapFetchTimer) {
    clearTimeout(mapFetchTimer)
  }

  const mapRef = map.value
  if (mapRef) {
    mapEventHandlers.forEach(({ type, handler }) => {
      kakao.maps.event.removeListener(mapRef, type, handler)
    })
  }
})
</script>

<template>
  <div class="flex h-screen flex-col bg-gray-50">
    <header class="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6">
      <div class="flex items-center gap-4">
        <button
          class="flex h-10 w-10 items-center justify-center rounded-lg bg-gray-100 text-gray-500 transition-colors hover:bg-gray-200 hover:text-gray-900"
          aria-label="홈으로 이동"
          @click="goHome"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
        </button>
        <button class="text-left" @click="goHome">
          <span class="text-xl font-extrabold text-primary-main">Home</span>
          <span class="text-xl font-extrabold text-text-mute">Lens</span>
        </button>
      </div>

      <div class="hidden min-w-0 items-center gap-2 rounded-full bg-gray-100 px-4 py-2 text-sm text-gray-600 sm:flex">
        <svg class="h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <span class="truncate">{{ searchQuery || '전체 매물' }}</span>
      </div>

      <div class="flex items-center gap-4">
        <span class="hidden text-sm font-medium text-gray-500 lg:block">
          {{ properties.length.toLocaleString() }}개 매물
          <span v-if="isRefreshingMap" class="ml-1 text-primary-main">갱신 중</span>
        </span>
        <template v-if="authStore.isLoggedIn">
          <div class="relative">
            <button
              class="flex h-9 w-9 items-center justify-center rounded-full bg-primary-main text-sm font-semibold text-white"
              @click="showUserMenu = !showUserMenu"
            >
              {{ authStore.userName.charAt(0) }}
            </button>
            <div
              v-if="showUserMenu"
              class="absolute right-0 top-full z-50 mt-2 w-36 rounded-xl border border-gray-200 bg-white p-2 shadow-lg"
            >
              <button
                class="w-full rounded-lg px-4 py-2.5 text-left text-sm text-gray-600 hover:bg-gray-100"
                @click="router.push('/mypage')"
              >
                마이페이지
              </button>
              <button
                class="w-full rounded-lg px-4 py-2.5 text-left text-sm text-red-500 hover:bg-red-50"
                @click="authStore.logout()"
              >
                로그아웃
              </button>
            </div>
          </div>
        </template>
        <button
          v-else
          class="rounded-lg bg-primary-main px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-secondary-main"
          @click="router.push('/login')"
        >
          로그인
        </button>
      </div>
    </header>

    <main class="relative flex flex-1 overflow-hidden">
      <aside class="z-20 hidden w-[420px] shrink-0 flex-col border-r border-gray-200 bg-white lg:flex">
        <div class="border-b border-gray-100 p-5">
          <p class="text-xs font-semibold uppercase text-primary-main">{{ resultLabel }}</p>
          <h1 class="mt-1 text-xl font-bold text-gray-900">
            {{ searchQuery || '지도에서 매물 둘러보기' }}
          </h1>
          <p class="mt-1 text-sm text-gray-500">
            지도 마커나 매물 카드를 눌러 상세 정보를 확인하세요.
          </p>
        </div>

        <div v-if="isLoading" class="flex flex-1 items-center justify-center text-sm text-gray-500">
          매물을 불러오는 중입니다...
        </div>
        <div v-else-if="listedProperties.length === 0" class="flex flex-1 items-center justify-center p-8 text-center">
          <div>
            <p class="text-lg font-semibold text-gray-900">검색 결과가 없습니다</p>
            <p class="mt-2 text-sm text-gray-500">다른 지역을 선택해 다시 검색해보세요.</p>
          </div>
        </div>
        <div v-else class="flex-1 space-y-4 overflow-y-auto p-4">
          <div
            v-if="selectedCluster"
            class="flex items-center justify-between rounded-lg border border-primary-main/30 bg-primary-main/10 px-3 py-2"
          >
            <span class="text-sm font-semibold text-primary-main">
              선택한 구역 {{ selectedCluster.count }}개 매물
            </span>
            <button
              class="text-sm font-semibold text-gray-500 hover:text-gray-900"
              @click="clearSelectedCluster"
            >
              전체 보기
            </button>
          </div>
          <PropertyCard
            v-for="property in listedProperties"
            :key="property.itemId"
            :property="property"
            :is-selected="selectedProperty?.itemId === property.itemId"
            @select="focusProperty(property)"
            @detail="openDetail(property)"
            @update="handlePropertyUpdate"
          />
        </div>
      </aside>

      <section class="relative flex-1">
        <div class="absolute left-4 right-4 top-4 z-20 flex flex-wrap items-start gap-2 lg:left-6 lg:right-auto">
          <div class="relative">
            <button
              class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-700 shadow-sm hover:border-primary-main hover:text-primary-main"
              :class="selectedDo ? 'border-primary-main bg-primary-main/10 text-primary-main' : ''"
              @click="loadDoList"
            >
              {{ selectedDo || '시/도' }}
            </button>
            <div
              v-if="openRegionMenu === 'do'"
              class="absolute left-0 top-full mt-2 max-h-72 w-44 overflow-y-auto rounded-lg border border-gray-200 bg-white py-2 shadow-lg"
            >
              <p v-if="isLoadingRegion" class="px-4 py-3 text-sm text-gray-500">불러오는 중...</p>
              <button
                v-for="doItem in doList"
                v-else
                :key="doItem"
                class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main"
                @click="selectDo(doItem)"
              >
                {{ doItem }}
              </button>
            </div>
          </div>

          <div class="relative">
            <button
              class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-700 shadow-sm hover:border-primary-main hover:text-primary-main disabled:cursor-not-allowed disabled:opacity-50"
              :class="selectedSi ? 'border-primary-main bg-primary-main/10 text-primary-main' : ''"
              :disabled="!selectedDo"
              @click="loadSiList"
            >
              {{ selectedSi || '시/군/구' }}
            </button>
            <div
              v-if="openRegionMenu === 'si'"
              class="absolute left-0 top-full mt-2 max-h-72 w-44 overflow-y-auto rounded-lg border border-gray-200 bg-white py-2 shadow-lg"
            >
              <p v-if="isLoadingRegion" class="px-4 py-3 text-sm text-gray-500">불러오는 중...</p>
              <button
                v-for="siItem in siList"
                v-else
                :key="siItem"
                class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main"
                @click="selectSi(siItem)"
              >
                {{ siItem }}
              </button>
            </div>
          </div>

          <div class="relative">
            <button
              class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-700 shadow-sm hover:border-primary-main hover:text-primary-main disabled:cursor-not-allowed disabled:opacity-50"
              :class="selectedDong ? 'border-primary-main bg-primary-main/10 text-primary-main' : ''"
              :disabled="!selectedSi"
              @click="loadDongList"
            >
              {{ selectedDong || '읍/면/동' }}
            </button>
            <div
              v-if="openRegionMenu === 'dong'"
              class="absolute left-0 top-full mt-2 max-h-72 w-44 overflow-y-auto rounded-lg border border-gray-200 bg-white py-2 shadow-lg"
            >
              <p v-if="isLoadingRegion" class="px-4 py-3 text-sm text-gray-500">불러오는 중...</p>
              <button
                v-for="dongItem in dongList"
                v-else
                :key="dongItem"
                class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-primary-main/10 hover:text-primary-main"
                @click="selectDong(dongItem)"
              >
                {{ dongItem }}
              </button>
            </div>
          </div>

          <button
            class="rounded-full bg-primary-main px-4 py-2 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-secondary-main disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="!regionLabel"
            @click="applyRegionSearch"
          >
            검색
          </button>
          <button
            v-if="searchQuery || regionLabel"
            class="rounded-full border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-600 shadow-sm hover:border-gray-300 hover:text-gray-900"
            @click="clearRegionSearch"
          >
            초기화
          </button>
        </div>

        <div
          v-if="isLoading"
          class="absolute inset-0 z-10 flex items-center justify-center bg-white/70 text-sm font-medium text-gray-600"
        >
          매물 지도를 준비하는 중입니다...
        </div>

        <KakaoMap
          :lat="mapCenter.lat"
          :lng="mapCenter.lng"
          :level="7"
          width="100%"
          height="100%"
          @on-load-kakao-map="onLoadKakaoMap"
        >
          <template v-for="cluster in visibleClusters" :key="cluster.id">
            <KakaoMapCustomOverlay
              :lat="cluster.lat"
              :lng="cluster.lng"
              :y-anchor="0.5"
            >
              <button
                class="flex h-14 w-14 items-center justify-center rounded-full bg-primary-main/80 text-base font-extrabold text-white shadow-lg ring-4 ring-primary-main/25 transition-transform hover:scale-110"
                :class="selectedCluster?.id === cluster.id ? 'outline outline-2 outline-primary-main ring-primary-main/60' : ''"
                @click="focusCluster(cluster)"
              >
                {{ cluster.count }}
              </button>
            </KakaoMapCustomOverlay>
          </template>

        </KakaoMap>

        <div
          v-if="!isLoading && properties.length > 0"
          class="absolute bottom-4 left-4 right-4 z-20 rounded-xl border border-gray-200 bg-white p-3 shadow-lg lg:hidden"
        >
          <PropertyCard
            v-if="selectedProperty"
            :property="selectedProperty"
            :is-selected="true"
            @select="focusProperty(selectedProperty)"
            @detail="openDetail(selectedProperty)"
            @update="handlePropertyUpdate"
          />
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
