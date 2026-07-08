/**
 * 직방 이미지 URL 변환 함수
 * ic.zigbang.com 도메인은 w/h 파라미터가 필수
 */
export const getZigbangImageUrl = (url?: string, width = 400, height?: number): string => {
  if (!url) return ''

  // ic.zigbang.com 도메인인 경우 파라미터 추가
  if (url.includes('ic.zigbang.com')) {
    const params = height ? `w=${width}&h=${height}` : `w=${width}`
    return `${url}?${params}`
  }

  // 다른 도메인은 그대로 반환
  return url
}

/**
 * 썸네일 이미지 URL (작은 사이즈)
 */
export const getThumbnailUrl = (url?: string): string => {
  return getZigbangImageUrl(url, 300, 200)
}

/**
 * 카드 이미지 URL (중간 사이즈)
 */
export const getCardImageUrl = (url?: string): string => {
  return getZigbangImageUrl(url, 400, 300)
}

/**
 * 상세 이미지 URL (큰 사이즈)
 */
export const getDetailImageUrl = (url?: string): string => {
  return getZigbangImageUrl(url, 800, 600)
}

/**
 * 갤러리 이미지 URL (원본 비율 유지)
 */
export const getGalleryImageUrl = (url?: string): string => {
  return getZigbangImageUrl(url, 1200)
}
