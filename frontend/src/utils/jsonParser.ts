/**
 * JSON 문자열을 배열로 파싱하는 헬퍼 함수
 * @param jsonString - JSON 문자열 (예: "[\"item1\", \"item2\"]")
 * @returns 파싱된 배열 또는 빈 배열
 */
export const parseJsonArray = <T = string>(jsonString: string | null | undefined): T[] => {
  if (!jsonString) return []
  
  try {
    const parsed = JSON.parse(jsonString)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    console.warn('JSON 파싱 실패:', error, jsonString)
    return []
  }
}

