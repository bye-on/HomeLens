import kakaoClient from './kakaoClient'

export interface ShortestDistanceData {
  name?: string
  x: number // 경도
  y: number // 위도
  angle?: number // 0 = 북, 90 = 동, 180 = 남, 270 = 서
}

export interface ShortestDistanceRequest {
  origin: ShortestDistanceData
  destination: ShortestDistanceData
  waypoints: ShortestDistanceData[]
}

// 다중 경유지 길찾기
export const getShortestDistance = async (request: ShortestDistanceRequest) => {
  const response = await kakaoClient.post('/waypoints/directions', {
    origin: request.origin,
    destination: request.destination,
    waypoints: request.waypoints,
  })

  return response
}
