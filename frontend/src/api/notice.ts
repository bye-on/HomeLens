import client from './client'
import type Response from '@/type/response'

// 공지사항 타입
// export interface Notice {
//   noticeId: number
//   title: string
//   content: string
//   authorName: string
//   viewCount: number
//   createdAt: string
//   updatedAt: string
// }

export interface BoardListItem {
  boardId: number
  subject: string
  content: string
  publisherId: number
  userName: string
  registerTime: string
}

export interface BoardListResponse {
  list: BoardListItem[]
  totalPage: number
  currentPage: number
}

// 공지사항 목록 응답 타입
// export interface NoticeListResponse {
//   notices: Notice[]
//   totalCount: number
//   currentPage: number
//   totalPages: number
// }

// 공지사항 작성/수정 요청 타입
export interface NoticeRequest {
  subject: string
  content: string
}

// 공지사항 API
export const noticeApi = {
  // 목록 조회
  getList: async (page: number = 1, size: number = 10): Promise<BoardListResponse> => {
    const response = await client.get<Response<BoardListResponse>>('/board/list', {
      params: { page, size },
      noAuth: true,
    })
    return response.data.data
  },

  // 상세 조회
  getDetail: async (noticeId: number): Promise<BoardListItem> => {
    const response = await client.get<Response<BoardListItem>>(`/board/view/${noticeId}`, {
      noAuth: true,
    })
    return response.data.data
  },

  // 작성 (admin only)
  create: async (data: NoticeRequest): Promise<BoardListItem> => {
    const response = await client.post<Response<BoardListItem>>('/board/write', data)
    return response.data.data
  },

  // 수정 (admin only)
  update: async (noticeId: number, data: NoticeRequest): Promise<BoardListItem> => {
    const response = await client.put<Response<BoardListItem>>(`/board/edit/${noticeId}`, data)
    return response.data.data
  },

  // 삭제 (admin only)
  delete: async (noticeId: number): Promise<void> => {
    await client.delete(`/board/delete/${noticeId}`)
  },
}
