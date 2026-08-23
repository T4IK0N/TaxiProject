import type { AccessTokenPayload } from "@/types/auth";

export function decodeJwtPayload<T = AccessTokenPayload>(token: string): T | null {
  try {
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) return null;
    const normalized = payloadBase64.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(normalized);
    return JSON.parse(json) as T;
  } catch {
    return null;
  }
}

export function isTokenExpired(payload: { exp: number }): boolean {
  return payload.exp * 1000 - 5000 < Date.now();
}
