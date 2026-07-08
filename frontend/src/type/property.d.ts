export type SalesType = '월세' | '전세' | null
export type ServiceType = '원룸' | '오피스텔' | null

export interface DistributionType {
  companyName: string
  kinds: string[]
}

export interface AmenityType {
  title: string
  description: string
}

export interface SubwayType {
  id: number
  name: string
  description: string
}

export interface PointOfInterestDetailType {
  exists: boolean
  distance?: number
  transport?: 'foot' | 'car' | 'bus' | null
  timeTaken?: number
}

export type PointOfInterestType = Record<string, PointOfInterestDetailType | null>

export interface PropertyData {
  itemId: number
  title: string
  salesType: SalesType
  serviceType: ServiceType
  local1: string
  local2: string
  local3: string
  jibunAddress: string
  deposit: number
  rent: number
  manageCost: number
  manageNotIncludes: string | null
  options: string | null
  areaM2: number | null
  areaContractM2: number | null
  floorLevel: number | null
  allFloors: number | null
  lat: number
  lng: number
  subway: SubwayType[] | null
  nbDistribution: DistributionType[] | null
  nbAmenity: AmenityType[] | null
  nbPoi: PointOfInterestType | null
  itemImage: string | null
  likeCount: number
  userLike: boolean
  sold: boolean
}