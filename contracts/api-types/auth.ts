export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: 'Bearer';
}

export interface ApiKeyCreateRequest {
  name: string;
  expires_at?: string;
}

export interface ApiKeyResponse {
  key_id: string;
  name: string;
  api_key: string;
  tenant_id: string;
  created_at: string;
  expires_at?: string;
}

export interface TenantCreateRequest {
  name: string;
}

export interface TenantInfo {
  tenant_id: string;
  name: string;
  created_at: string;
}

export interface UserInfo {
  user_id: string;
  email: string;
  tenant_id: string;
  roles: Role[];
}

export type Role = 'ADMIN' | 'MEMBER' | 'VIEWER';
