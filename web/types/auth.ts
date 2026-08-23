export const Role = {
  ADMIN: "admin",
  DISPATCHER: "dispatcher",
  ACCOUNTANT: "accountant",
  DRIVER: "driver",
} as const;

export type Role = (typeof Role)[keyof typeof Role];

export const ALLOWED_WEB_ROLES: Role[] = [Role.ADMIN, Role.DISPATCHER, Role.ACCOUNTANT];

export interface AccessTokenPayload {
  token_type: "access";
  exp: number;
  iat: number;
  jti: string;
  user_id: string;
  role: Role;
  full_name: string;
  driver_id: string | null;
}

export interface SessionUser {
  id: string;
  role: Role;
  fullName: string;
}
