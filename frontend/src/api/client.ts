import { useAuthStore } from '@/stores/useAuthStore'
import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import router from '@/router/router'

// axios.d.ts 또는 client.ts
import 'axios'

declare module 'axios' {
  export interface AxiosRequestConfig {
    noAuth?: boolean
  }
}

// API 기본 설정
const client: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
})

// 요청 인터셉터
client.interceptors.request.use(
  (config) => {
    // 토큰이 있으면 헤더에 추가
    const authStore = useAuthStore()
    const accessToken = authStore.accessToken
    const refreshToken = authStore.refreshToken

    if (config.noAuth) {
      return config
    }

    if (!refreshToken) {
      // router.replace('/login')
      return config
    }

    if (accessToken && config.headers) {
      config.headers.Authorization = `${accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 응답 인터셉터
client.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    if (originalRequest.skipAuth) {
      return originalRequest
    }

    // 401 에러 시 로그인 페이지로 리다이렉트 && 무한루프 돌지 않도록 재실행 한번만
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      const authStore = useAuthStore()

      try {
        // 리프레쉬 실행 후
        await authStore.refresh()
        // client로 재요청 -> header 다시 붙음 (재실행한걸ㅀ)
        return client(originalRequest)
      } catch (error) {
        // 리프레쉬 토큰까지 만료됐을 때 logout
        await authStore.logout()
        return Promise.reject(error)
      }
    }
    return Promise.reject(error)
  },
)

export default client
