import type Response from '@/type/response'
import client from './client'
import axios from 'axios'

// auth client
const authClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  withCredentials: true,
})

// 사용자 정보 타입
export interface UserInfo {
  userId: number
  userEmail: string
  userName: string
  userTel?: string
  userRole: string
  userEmailVerified: string
  userCreatedAt: string
}

// 로그인 응답 타입
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userName: string
}

export interface refreshResponse {
  newAccessToken: string
  newRefreshToken: string
}

// 인증 API
export const authApi = {
  // 로그인
  login: async (email: string, password: string): Promise<LoginResponse> => {
    const response = await client.post<LoginResponse>('/auth/login', {
      email: email,
      password: password,
    })
    return response.data
  },

  // 회원가입
  register: async (userEmail: string, userPw: string, userName: string, userTel?: string) => {
    const response = await client.post('/user', {
      userEmail: userEmail,
      userPw: userPw,
      userName: userName,
      userTel: userTel,
    })
    return response.data
  },

  emailVerify: async () => {
    const response = await client.post('/auth/email')
    return response.data
  },

  // 로그아웃
  logout: async () => {
    const response = await client.post('/auth/logout', {
      skipAuth: true,
    })

    return response.data
  },

  // 내 정보 조회
  getMe: async (): Promise<UserInfo> => {
    const response = await client.get<Response<UserInfo>>('/user/me')
    return response.data.data
  },

  // 내 정보 수정
  updateMe: async (userName?: string, userTel?: string) => {
    const response = await client.post('/user/me/detail', {
      userName: userName,
      userTel: userTel,
    })
    return response.data
  },

  // 토큰 갱신
  refreshToken: async (refreshToken: string | null): Promise<refreshResponse> => {
    // client의 request 타지 않기 위해 axios로 호출
    const response = await authClient.post<Response<refreshResponse>>(
      '/auth/refresh',
      {},
      {
        headers: {
          'Refresh-Token': refreshToken,
        },
      },
    )
    return response.data.data
  },
}
