<script setup lang="ts">
import type { PropertyData } from '@/type/property'
import { computed, ref, watch } from 'vue'
import { parseJsonArray } from '@/utils/jsonParser'
import { getCardImageUrl } from '@/utils/imageHelper'
import { propertyApi } from '@/api/property'
import { useAuthStore } from '@/stores/useAuthStore'

interface Props {
  property: PropertyData
  isSelected?: boolean
  imagePriority?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isSelected: false,
  imagePriority: false,
})

const authStore = useAuthStore()

const emit = defineEmits<{
  select: []
  detail: []
  update: [property: PropertyData]
}>()

const formatPrice = (deposit: number, rent: number, salesType: string | null) => {
  if (salesType === '전세') {
    if (deposit >= 10000) return `${(deposit / 10000).toFixed(1)}억`
    return `${deposit.toLocaleString()}만`
  }
  return `${deposit.toLocaleString()}/${rent.toLocaleString()}`
}

const m2ToPyeong = (m2: number) => {
  return (m2 / 3.3058).toFixed(1)
}

const images = computed(() => parseJsonArray<string>(props.property.itemImage))

const getImageUrl = () => {
  if (images.value.length > 0) {
    return getCardImageUrl(images.value[0])
  }
  return '/placeholder-image.jpg'
}

const localUserLike = ref(props.property.userLike || false)
const isLiked = computed(() => localUserLike.value)

watch(
  () => props.property.userLike,
  (newValue) => {
    localUserLike.value = newValue || false
  },
  { immediate: true },
)

const toggleLike = async () => {
  if (!authStore.isLoggedIn) return

  try {
    await propertyApi.toggleLikeHome(props.property.itemId)
    localUserLike.value = !localUserLike.value
    emit('update', {
      ...props.property,
      userLike: localUserLike.value,
    })
  } catch (err) {
    console.error('찜하기 실패:', err)
  }
}
</script>

<template>
  <article
    class="bg-white border rounded-xl overflow-hidden cursor-pointer transition-all hover:border-primary-main hover:shadow-lg hover:shadow-primary-main/10"
    :class="isSelected ? 'border-primary-main shadow-lg shadow-primary-main/15' : 'border-gray-200'"
    @click="emit('select')"
  >
    <div class="relative aspect-16/10 overflow-hidden">
      <img
        :src="getImageUrl()"
        :alt="property.title"
        width="400"
        height="250"
        :loading="imagePriority ? 'eager' : 'lazy'"
        :fetchpriority="imagePriority ? 'high' : 'auto'"
        decoding="async"
        class="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
      />
      <div class="absolute top-3 left-3 flex gap-1.5">
        <span
          class="px-3 py-1.5 rounded text-xs font-semibold"
          :class="
            property.salesType === '전세'
              ? 'bg-primary-main text-white'
              : 'bg-secondary-main text-white'
          "
        >
          {{ property.salesType }}
        </span>
        <span class="px-3 py-1.5 bg-white/95 text-gray-600 rounded text-xs font-semibold">
          {{ property.serviceType }}
        </span>
      </div>
      <button
        v-if="authStore.isLoggedIn"
        class="absolute top-3 right-3 w-8 h-8 bg-white/90 rounded-full flex items-center justify-center transition-colors hover:bg-white"
        :class="isLiked ? 'text-red-500' : 'text-gray-400'"
        aria-label="찜하기"
        @click.stop="toggleLike"
      >
        <svg v-if="isLiked" class="w-5 h-5 fill-current" viewBox="0 0 24 24">
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

    <div class="p-5">
      <div class="flex items-baseline gap-1 mb-2">
        <span class="text-2xl font-extrabold text-gray-900">
          {{ formatPrice(property.deposit, property.rent, property.salesType) }}
        </span>
        <span v-if="property.salesType === '월세'" class="text-sm text-gray-500">만원</span>
      </div>

      <h3 class="text-base font-medium text-gray-600 mb-3 line-clamp-2">
        {{ property.title }}
      </h3>

      <div class="flex flex-wrap items-center gap-1.5 text-sm text-gray-500 mb-4">
        <span>{{ property.local2 }} {{ property.local3 }}</span>
        <template v-if="property.areaM2">
          <span class="text-gray-300">·</span>
          <span>{{ property.areaM2 }}㎡({{ m2ToPyeong(property.areaM2) }}평)</span>
        </template>
        <template v-if="property.floorLevel && property.allFloors">
          <span class="text-gray-300">·</span>
          <span>{{ property.floorLevel }}/{{ property.allFloors }}층</span>
        </template>
      </div>

      <div
        v-if="property.nbAmenity && property.nbAmenity.length > 0"
        class="flex flex-wrap gap-2 mb-4"
      >
        <span
          v-for="amenity in property.nbAmenity.slice(0, 3)"
          :key="amenity.title"
          class="px-3 py-1.5 bg-primary-main/10 text-primary-main rounded-full text-xs"
        >
          {{ amenity.title }}
        </span>
      </div>

      <button
        class="w-full flex items-center justify-center gap-2 py-3.5 bg-primary-main text-white rounded-lg text-sm font-semibold hover:bg-secondary-main transition-colors"
        @click.stop="emit('detail')"
      >
        상세보기
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7" />
        </svg>
      </button>
    </div>
  </article>
</template>
