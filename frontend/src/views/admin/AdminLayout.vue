<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/useAuthStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { id: 'dashboard', label: '대시보드', icon: 'dashboard', path: '/admin' },
  { id: 'users', label: '회원 관리', icon: 'users', path: '/admin/users' },
  { id: 'properties', label: '매물 관리', icon: 'properties', path: '/admin/properties' },
  { id: 'notices', label: '공지사항 관리', icon: 'notices', path: '/admin/notices' },
]

const isActive = (path: string) => {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

const goHome = () => router.push('/')
const handleLogout = () => {
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  if (!authStore.isAdmin) {
    router.push('/')
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-gray-900 text-white flex flex-col">
      <!-- Logo -->
      <div class="px-6 py-5 border-b border-gray-800">
        <div class="cursor-pointer" @click="goHome">
          <span class="text-xl font-extrabold text-primary-main"> Home </span>
          <span class="text-xl font-extrabold text-text-mute"> Lens </span>
          <span class="block text-xs text-gray-500 mt-1">Admin Panel</span>
        </div>
      </div>

      <!-- Menu -->
      <nav class="flex-1 px-4 py-6 space-y-1">
        <router-link
          v-for="item in menuItems"
          :key="item.id"
          :to="item.path"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors',
            isActive(item.path)
              ? 'bg-primary-main text-white'
              : 'text-gray-400 hover:bg-gray-800 hover:text-white',
          ]"
        >
          <!-- Dashboard -->
          <svg
            v-if="item.icon === 'dashboard'"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <rect x="3" y="3" width="7" height="7" rx="1" />
            <rect x="14" y="3" width="7" height="7" rx="1" />
            <rect x="3" y="14" width="7" height="7" rx="1" />
            <rect x="14" y="14" width="7" height="7" rx="1" />
          </svg>
          <!-- Users -->
          <svg
            v-else-if="item.icon === 'users'"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="9" cy="7" r="4" />
            <path d="M3 21v-2a4 4 0 014-4h4a4 4 0 014 4v2" />
            <circle cx="19" cy="7" r="3" />
            <path d="M21 21v-1.5a3.5 3.5 0 00-3-3.5" />
          </svg>
          <!-- Properties -->
          <svg
            v-else-if="item.icon === 'properties'"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
            <polyline points="9,22 9,12 15,12 15,22" />
          </svg>
          <!-- Notices -->
          <svg
            v-else-if="item.icon === 'notices'"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
            <polyline points="14,2 14,8 20,8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
          {{ item.label }}
        </router-link>
      </nav>

      <!-- User -->
      <div class="px-4 py-4 border-t border-gray-800">
        <div class="flex items-center gap-3 px-4 py-3">
          <div
            class="w-9 h-9 rounded-full bg-primary-main flex items-center justify-center text-sm font-semibold"
          >
            {{ authStore.userName.charAt(0) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ authStore.userName }}</p>
            <p class="text-xs text-gray-500">관리자</p>
          </div>
        </div>
        <button
          class="w-full mt-2 px-4 py-2.5 text-sm font-medium text-gray-400 rounded-lg hover:bg-gray-800 hover:text-white transition-colors text-left flex items-center gap-2"
          @click="handleLogout"
        >
          <svg
            class="w-4 h-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
            <polyline points="16,17 21,12 16,7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          로그아웃
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 overflow-auto">
      <slot />
    </main>
  </div>
</template>
