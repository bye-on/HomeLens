import type { PropertyData } from '@/type/property'
import client from './client'
import type Response from '@/type/response'
import { useAuthStore } from '@/stores/useAuthStore'

export interface PropertySearchParams {
  query?: string
  category?: string
  minDeposit?: number
  maxDeposit?: number
  minRent?: number
  maxRent?: number
  region?: string
  page?: number
  size?: number
}

export interface PropertyListResponse {
  primary: PropertyData[]
  regionOnly: PropertyData[]
}

export interface RecentPropertyResponse {
  recentProperty: PropertyData[]
  totalPage: number
  currentPage: number
}

export interface MapPropertySearchParams {
  swLat?: number
  swLng?: number
  neLat?: number
  neLng?: number
  local1?: string
  local2?: string
  local3?: string
  size?: number
  cursor?: number
}

export interface MapPropertyItemsResponse {
  items: PropertyData[]
  nextCursor: number | null
  hasNext: boolean
}

export type PropertySearchMode = 'basic' | 'ai'

export const propertyApi = {
  getDo: async (): Promise<string[]> => {
    const response = await client.get<Response<{ local: string[] }>>('/properties/local', {
      noAuth: true,
    })
    return response.data.data.local || []
  },

  getSi: async (doCode: string): Promise<string[]> => {
    const response = await client.get<Response<{ local: string[] }>>(
      `/properties/local/${doCode}`,
      {
        noAuth: true,
      },
    )
    return response.data.data.local || []
  },

  getDong: async (doCode: string, siCode: string): Promise<string[]> => {
    const response = await client.get<Response<{ local: string[] }>>(
      `/properties/local/${doCode}/${siCode}`,
      {
        noAuth: true,
      },
    )
    return response.data.data.local || []
  },

  searchByAI: async (query: string): Promise<PropertyListResponse> => {
    const response = await client.post<Response<PropertyListResponse>>('/properties/property-search', {
      query,
      mode: 'ai',
      top_k: 20,
    })
    return response.data.data
  },

  searchBasic: async (query: string): Promise<PropertyListResponse> => {
    const response = await client.post<Response<PropertyListResponse>>(
      '/properties/property-search/basic',
      {
        query,
        mode: 'basic',
        top_k: 50,
      },
    )
    return response.data.data
  },

  search: async (
    query: string,
    mode: PropertySearchMode = 'basic',
  ): Promise<PropertyListResponse> => {
    return mode === 'ai' ? propertyApi.searchByAI(query) : propertyApi.searchBasic(query)
  },

  getRecent: async (page = 1, size = 50): Promise<RecentPropertyResponse> => {
    const response = await client.get<Response<RecentPropertyResponse>>('/properties/recent', {
      params: { page, size },
      noAuth: true,
    })
    return response.data.data
  },

  getMapProperties: async (params: MapPropertySearchParams): Promise<PropertyData[]> => {
    const response = await client.get<Response<PropertyData[]>>('/properties/map', {
      params,
      noAuth: true,
    })
    return response.data.data
  },

  getMapPropertyItems: async (
    params: MapPropertySearchParams,
  ): Promise<MapPropertyItemsResponse> => {
    const response = await client.get<Response<MapPropertyItemsResponse>>('/properties/map/items', {
      params,
      noAuth: true,
    })
    return response.data.data
  },

  getById: async (itemId: number): Promise<PropertyData> => {
    const response = await client.get<Response<PropertyData>>(`/properties/${itemId}`, {
      noAuth: true,
    })
    return response.data.data
  },

  getLikeHomeList: async (): Promise<PropertyData[]> => {
    const response = await client.get<Response<PropertyData[]>>('/like/my')
    return response.data.data
  },

  toggleLikeHome: async (itemId: number): Promise<boolean> => {
    const response = await client.post<Response<boolean>>('/like/toggle', {
      itemId,
    })

    return response.data.data
  },

  getPopularHome: async (): Promise<PropertyData[]> => {
    const authStore = useAuthStore()

    const response = await client.get<Response<PropertyData[]>>(
      '/like/popular/top5',
      authStore.isLoggedIn ? undefined : { noAuth: true },
    )

    return response.data.data
  },
}
