// FE-Req-10 & 15: TypeScript model representing a tenant membership context
export interface Membership {
  membershipId: string;
  userId: string;
  neighborhoodId: string;
  neighborhoodName?: string;
  fullName?: string;
  email?: string;
  unitNumber: string;
  phoneNumber?: string;
  roles: string[];
  status: 'ACTIVE' | 'INVITED' | 'INACTIVE';
  invitationToken?: string;
  createdAt: string;
}

export interface MembershipContext {
  membershipId: string;
  neighborhoodId: string;
  neighborhoodName: string;
  unitNumber: string;
  roles: string[];
}
