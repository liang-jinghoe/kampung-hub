// FE-Req-1, 4 & 10: TypeScript model representing whitelisted resident vehicles
export interface ResidentVehicle {
  vehicleId: string;
  neighborhoodId: string;
  ownerMembershipId: string;
  unitNumber: string;
  plateText: string;
  model: string;
  color: string;
  zoneMask: string;
  status: 'ACTIVE' | 'SUSPENDED' | string;
  updatedAt: string;
  badgeImageUrl?: string;
}

export interface CreateVehicleDto {
  plateText: string;
  model: string;
  color: string;
  zoneMask?: string;
  neighborhoodId?: string;
  ownerMembershipId?: string;
  unitNumber?: string;
}

export interface UpdateVehicleDto {
  model: string;
  color: string;
  zoneMask?: string;
  status: string;
}
