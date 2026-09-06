import { MembershipContext } from './membership.model';

// FE-Req-10 & 15: Auth request/response interfaces
export interface LoginRequest {
  email: string;
  password: string;
  preferredMembershipId?: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  fullName: string;
  activeMembership: MembershipContext;
  memberships: MembershipContext[];
}

export interface SwitchContextRequest {
  userId: string;
  membershipId: string;
}
