<script setup lang="ts">
import type { PropertyData } from '@/type/property'
import { ref, computed, watch } from 'vue'
import { parseJsonArray } from '@/utils/jsonParser'
import { getDetailImageUrl, getGalleryImageUrl } from '@/utils/imageHelper'
import { propertyApi } from '@/api/property'
import { useAuthStore } from '@/stores/useAuthStore'

interface Props {
  property: PropertyData
}

const props = defineProps<Props>()
const authStore = useAuthStore()

const emit = defineEmits<{ 
  close: []
  update: [property: PropertyData]
}>()

const selectedImageIndex = ref(0)

// 로컬 찜 상태 관리 (props를 기반으로 하되, 로컬에서 토글 가능)
const localUserLike = ref(props.property.userLike || false)

// JSON 문자열을 배열로 파싱
const images = computed(() => parseJsonArray<string>(props.property.itemImage))
const options = computed(() => parseJsonArray<string>(props.property.options))
const manageNotIncludes = computed(() => parseJsonArray<string>(props.property.manageNotIncludes))

const formatPrice = (deposit: number, rent: number, salesType: string) => {
  if (salesType === '전세') {
    if (deposit >= 10000) return `전세 ${(deposit / 10000).toFixed(1)}억원`
    return `전세 ${deposit.toLocaleString()}만원`
  }
  return `보증금 ${deposit.toLocaleString()}만 / 월세 ${rent}만`
}

const m2ToPyeong = (m2: number) => (m2 / 3.3058).toFixed(1)

const formatTime = (seconds: number) => {
  if (seconds < 60) return `${seconds}초`
  return `약 ${Math.round(seconds / 60)}분`
}

const formatDistance = (distance: number) => {
  if (distance >= 1000) return `${(distance / 1000).toFixed(1)}km`
  return `${distance}m`
}

// POI 정보를 배열로 변환
const poiList = computed(() => {
  if (!props.property.nbPoi) return []
  return Object.entries(props.property.nbPoi)
    .filter(([, value]) => value?.exists)
    .map(([key, value]) => ({
      name: key,
      ...value,
    }))
})

// 찜 상태 (로컬 상태 사용)
const isLiked = computed(() => localUserLike.value)

// props 변경 시 로컬 상태 동기화
watch(() => props.property.userLike, (newValue) => {
  localUserLike.value = newValue || false
}, { immediate: true })

// 찜 토글
const toggleLike = async () => {
  if (!authStore.isLoggedIn) {
    return
  }

  try {
    await propertyApi.toggleLikeHome(props.property.itemId)
    // 로컬 상태 업데이트
    localUserLike.value = !localUserLike.value
    // 부모 컴포넌트에 변경사항 알림
    const updatedProperty = {
      ...props.property,
      userLike: localUserLike.value,
    }
    emit('update', updatedProperty)
  } catch (err) {
    console.error('찜 토글 실패:', err)
  }
}
</script>

<template>
  <div
    class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex justify-end"
    @click.self="emit('close')"
  >
    <div
      class="w-full max-w-md h-full bg-gray-50 flex flex-col animate-in slide-in-from-right duration-300"
    >
      <!-- Header -->
      <header class="flex justify-between items-center px-6 py-5 bg-white border-b border-gray-200">
        <h2 class="text-lg font-bold text-gray-900">매물 상세</h2>
        <div class="flex items-center gap-2">
          <!-- 찜하기 버튼 -->
          <button
            v-if="authStore.isLoggedIn"
            class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-lg transition-colors hover:bg-gray-200"
            :class="isLiked ? 'text-red-500' : 'text-gray-500'"
            @click="toggleLike"
          >
            <svg
              v-if="isLiked"
              class="w-6 h-6 fill-current"
              viewBox="0 0 24 24"
            >
              <path
                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
              />
            </svg>
            <svg
              v-else
              class="w-6 h-6"
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
          <!-- 닫기 버튼 -->
          <button
            class="w-10 h-10 flex items-center justify-center bg-gray-100 rounded-lg text-gray-500 hover:bg-gray-200 hover:text-gray-900"
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
      </header>

      <!-- Body -->
      <div class="flex-1 overflow-y-auto p-5 space-y-4">
        <!-- Gallery -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <div class="aspect-16/10">
            <img
              v-if="images.length > 0"
              :src="getGalleryImageUrl(images[selectedImageIndex])"
              :alt="property.title"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="w-full h-full bg-gray-100 flex items-center justify-center text-gray-400"
            >
              이미지 없음
            </div>
          </div>
          <div v-if="images.length > 1" class="flex gap-2 p-3 overflow-x-auto">
            <button
              v-for="(img, i) in images.slice(0, 5)"
              :key="i"
              class="w-14 h-11 rounded-md overflow-hidden border-2 transition-colors shrink-0"
              :class="selectedImageIndex === i ? 'border-primary-main' : 'border-transparent'"
              @click="selectedImageIndex = i"
            >
              <img
                :src="`${getDetailImageUrl(img)}`"
                :alt="`${i + 1}`"
                class="w-full h-full object-cover"
              />
            </button>
          </div>
        </div>

        <!-- Price -->
        <section class="bg-white rounded-xl border border-gray-200 p-6 text-center">
          <span
            class="inline-block px-4 py-1.5 rounded-full text-sm font-semibold mb-4"
            :class="
              property.salesType === '전세'
                ? 'bg-primary-main text-white'
                : 'bg-secondary-main text-white'
            "
            >{{ property.salesType }}</span
          >
          <h1 class="text-2xl font-extrabold text-gray-900 mb-3">
            {{ formatPrice(property.deposit, property.rent, property.salesType || '') }}
          </h1>
          <p class="text-base text-gray-600 mb-2">{{ property.title }}</p>
          <p class="text-sm text-gray-400">{{ property.jibunAddress }}</p>
        </section>

        <!-- Info Grid -->
        <section class="bg-white rounded-xl border border-gray-200 p-6">
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
            기본 정보
          </h3>
          <div class="grid grid-cols-2 gap-4">
            <div v-if="property.areaM2">
              <span class="text-xs text-gray-400">전용면적</span>
              <p class="text-sm font-semibold text-gray-900 mt-1">
                {{ property.areaM2 }}㎡ ({{ m2ToPyeong(property.areaM2) }}평)
              </p>
            </div>
            <div v-if="property.floorLevel || property.allFloors">
              <span class="text-xs text-gray-400">층수</span>
              <p class="text-sm font-semibold text-gray-900 mt-1">
                {{ property.floorLevel || '-' }}층 / {{ property.allFloors || '-' }}층
              </p>
            </div>
            <div>
              <span class="text-xs text-gray-400">유형</span>
              <p class="text-sm font-semibold text-gray-900 mt-1">{{ property.serviceType }}</p>
            </div>
            <div v-if="property.manageCost">
              <span class="text-xs text-gray-400">관리비</span>
              <p class="text-sm font-semibold text-gray-900 mt-1">
                월 {{ property.manageCost }}만원
              </p>
            </div>
          </div>
        </section>

        <!-- Management Cost Details -->
        <section
          v-if="manageNotIncludes.length > 0"
          class="bg-white rounded-xl border border-gray-200 p-6"
        >
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
            관리비 별도
          </h3>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="item in manageNotIncludes"
              :key="item"
              class="px-3 py-1.5 bg-yellow-100 text-yellow-700 rounded text-sm"
            >
              {{ item }}
            </span>
          </div>
        </section>

        <!-- Options -->
        <section v-if="options.length > 0" class="bg-white rounded-xl border border-gray-200 p-6">
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">옵션</h3>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="opt in options"
              :key="opt"
              class="px-3.5 py-2 bg-gray-100 text-gray-600 rounded-full text-sm"
            >
              {{ opt }}
            </span>
          </div>
        </section>

        <!-- Amenities -->
        <section
          v-if="property.nbAmenity && property.nbAmenity.length > 0"
          class="bg-white rounded-xl border border-gray-200 p-6"
        >
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
            주변 특징
          </h3>
          <div class="grid grid-cols-2 gap-3">
            <div
              v-for="amenity in property.nbAmenity"
              :key="amenity.title"
              class="flex items-center gap-3 p-3.5 bg-primary-main/5 rounded-lg"
            >
              <span class="text-lg">
                {{
                  amenity.title === '역세권'
                    ? '🚇'
                    : amenity.title === '슬세권'
                      ? '🏪'
                      : amenity.title === '공세권'
                        ? '🌳'
                        : amenity.title === '학세권'
                          ? '📚'
                          : '✨'
                }}
              </span>
              <div>
                <span class="font-semibold text-gray-900 text-sm">{{ amenity.title }}</span>
                <span class="text-xs text-gray-500 block">{{ amenity.description }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Distribution Services -->
        <section
          v-if="property.nbDistribution && property.nbDistribution.length > 0"
          class="bg-white rounded-xl border border-gray-200 p-6"
        >
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
            배송 서비스
          </h3>
          <div class="space-y-3">
            <div
              v-for="dist in property.nbDistribution"
              :key="dist.companyName"
              class="flex items-center justify-between p-3.5 bg-gray-50 rounded-lg"
            >
              <span class="font-semibold text-gray-900">{{ dist.companyName }}</span>
              <div class="flex gap-1.5">
                <span
                  v-for="kind in dist.kinds"
                  :key="kind"
                  class="px-2 py-1 bg-green-100 text-green-700 rounded text-xs"
                >
                  {{ kind }}
                </span>
              </div>
            </div>
          </div>
        </section>

        <!-- Nearby POI -->
        <section v-if="poiList.length > 0" class="bg-white rounded-xl border border-gray-200 p-6">
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
            주변 시설
          </h3>
          <div class="divide-y divide-gray-100">
            <div v-for="poi in poiList" :key="poi.name" class="flex justify-between py-3">
              <span class="font-medium text-gray-900">{{ poi.name }}</span>
              <span class="text-sm text-gray-500">
                {{ formatDistance(poi.distance || 0) }}
                <template v-if="poi.transport === 'foot'">· 도보 </template>
                <template v-else-if="poi.transport === 'car'">· 차량 </template>
                {{ formatTime(poi.timeTaken || 0) }}
              </span>
            </div>
          </div>
        </section>

        <!-- Subway -->
        <section
          v-if="property.subway && property.subway.length > 0"
          class="bg-white rounded-xl border border-gray-200 p-6"
        >
          <h3 class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">역세권</h3>
          <div class="space-y-3">
            <div
              v-for="subway in property.subway"
              :key="subway.id"
              class="flex justify-between items-center p-3.5 bg-gray-100 rounded-lg"
            >
              <span class="font-semibold text-gray-900">{{ subway.name }}</span>
              <span class="text-sm text-gray-500">{{ subway.description }}</span>
            </div>
          </div>
        </section>
      </div>

      <!-- Footer -->
      <div class="p-5 bg-white border-t border-gray-200">
        <button
          class="w-full py-4 bg-primary-main text-white font-semibold rounded-lg hover:bg-secondary-main transition-colors"
        >
          문의하기
        </button>
      </div>
    </div>
  </div>
</template>
