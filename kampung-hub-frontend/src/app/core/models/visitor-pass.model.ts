// FE-Req-10: TypeScript model representing pre-registered visitor passes
export interface VisitorPass {
  passId: string;
  neighborhoodId: string;
  unitNumber: string;
  requesterMembershipId: string;
  visitorName: string;
  visitorPlateText: string;
  passToken: string;
  validFrom: string;
  validUntil: string;
  status: 'ACTIVE' | 'USED' | 'EXPIRED' | 'CANCELLED' | string;
}

export interface CreateVisitorPassDto {
  visitorName: string;
  visitorPlateText: string;
  validFrom: string;
  validUntil: string;
  neighborhoodId?: string;
  unitNumber?: string;
}
