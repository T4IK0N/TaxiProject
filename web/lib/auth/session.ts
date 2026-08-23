import "server-only";
import { cookies } from "next/headers";
import { decodeJwtPayload, isTokenExpired } from "./jwt";
import type { AccessTokenPayload, SessionUser } from "@/types/auth";

export const ACCESS_COOKIE = "tc_access";
export const REFRESH_COOKIE = "tc_refresh";

const COOKIE_OPTIONS = {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax" as const,
  path: "/",
};

export async function setSessionTokens(access: string, refresh: string) {
  const store = await cookies();
  store.set(ACCESS_COOKIE, access, COOKIE_OPTIONS);
  store.set(REFRESH_COOKIE, refresh, COOKIE_OPTIONS);
}

export async function updateAccessToken(access: string) {
  const store = await cookies();
  store.set(ACCESS_COOKIE, access, COOKIE_OPTIONS);
}

export async function clearSessionTokens() {
  const store = await cookies();
  store.delete(ACCESS_COOKIE);
  store.delete(REFRESH_COOKIE);
}

export async function getAccessToken(): Promise<string | null> {
  const store = await cookies();
  return store.get(ACCESS_COOKIE)?.value ?? null;
}

export async function getRefreshToken(): Promise<string | null> {
  const store = await cookies();
  return store.get(REFRESH_COOKIE)?.value ?? null;
}

export async function getSessionUser(): Promise<SessionUser | null> {
  const token = await getAccessToken();
  if (!token) return null;

  const payload = decodeJwtPayload<AccessTokenPayload>(token);
  if (!payload || isTokenExpired(payload)) return null;

  return {
    id: payload.user_id,
    role: payload.role,
    fullName: payload.full_name,
  };
}
