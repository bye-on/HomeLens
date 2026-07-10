import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { useKakao } from 'vue3-kakao-maps/@utils'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

import App from './App.vue'
import router from './router/router'

// 카카오맵 API 키 설정 (실제 키로 교체 필요)
useKakao(import.meta.env.VITE_KAKAO_MAP_KEY, ['services', 'clusterer'])

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
const queryClient = new QueryClient()

createApp(App).use(pinia).use(router).use(VueQueryPlugin, { queryClient }).mount('#app')
