// FE-Req-10: TypeScript model representing gate checkpoint logs
export interface AccessLog {
  logId: string;
  neighborhoodId: string;
  plateText: string;
  accessType: 'ENTRY' | 'EXIT' | string;
  visitorPassId?: string;
  membershipId?: string;
  verifiedByGuardId: string;
  timestamp: string;
}

export interface CreateAccessLogDto {
  plateText: string;
  accessType: 'ENTRY' | 'EXIT';
  visitorPassId?: string;
  membershipId?: string;
  neighborhoodId?: string;
}
