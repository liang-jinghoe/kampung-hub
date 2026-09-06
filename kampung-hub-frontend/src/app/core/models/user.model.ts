// FE-Req-10: TypeScript model representing platform User profiles
export interface User {
  userId: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  status: 'ACTIVE' | 'INVITED' | 'INACTIVE';
  createdAt: string;
}
