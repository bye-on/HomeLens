import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import axios from 'axios'

const kakaoClient: AxiosInstance = axios.create({
  baseURL: 'v1',
  withCredentials: true,
})

kakaoClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    config.headers.Authorization = `KakaoAK ${import.meta.env.VITE_KAKAO_REST_API_KEY}`
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

export default kakaoClient