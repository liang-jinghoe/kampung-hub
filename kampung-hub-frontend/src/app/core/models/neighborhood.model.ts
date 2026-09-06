// FE-Req-10: TypeScript model representing multi-tenant Neighborhood entities
export interface Neighborhood {
  id: string;
  name: string;
  propertyType: 'LANDED' | 'HIGH_RISE' | string;
  subscriptionTier: 'BASIC_LANDED' | 'PREMIUM_LANDED' | string;
  subscriptionStatus: 'ACTIVE' | 'EXPIRED' | string;
  maxAllowedUnits: number;
  createdAt: string;
}

export interface CreateNeighborhoodDto {
  name: string;
  propertyType: string;
  subscriptionTier: string;
  maxAllowedUnits: number;
}
