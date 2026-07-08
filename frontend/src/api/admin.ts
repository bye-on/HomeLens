// api/admin.ts
import client from './client'
import type Response from '@/type/response'

export interface UserDetail {
  userId: number
  userEmail: string
  userName: string
  userTel: string
  userRole: string
  userStatus: string
  userEmailVerified: string
  userCreatedAt: string
}

export interface UserDetailListResponse {
  userList: UserDetail[]
  totalPage: number
  currentPage: number
}

export const adminApi = {
  // 관리자 유저 목록 조회 (토큰 자동 포함)
  getList: async (
    page: number = 1,
    size: number = 10,
  ): Promise<UserDetailListResponse> => {
    const response = await client.get<Response<UserDetailListResponse>>(
      '/admin/user',
      { params: { page, size } },
    )
    return response.data.data
  },

  // 관리자 유저 삭제
  delete: async (userId: number): Promise<void> => {
    await client.post(`/admin/user/${userId}`)
  },
}
