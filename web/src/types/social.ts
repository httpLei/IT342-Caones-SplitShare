export interface UserConnectionDto {
  id: number;
  email: string;
  firstname: string;
  lastname: string;
  following: boolean;
  followedBy: boolean;
  mutual: boolean;
}

export interface UserActivityDto {
  id: number;
  desc: string;
  sub: string;
  amount: number;
  share: number;
  positive: boolean;
  createdAt: string;
}