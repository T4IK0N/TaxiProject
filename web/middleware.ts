import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { decodeJwtPayload, isTokenExpired } from "@/lib/auth/jwt";
import { ALLOWED_WEB_ROLES, type AccessTokenPayload } from "@/types/auth";

const PUBLIC_PATHS = ["/login"];
const DJANGO_API_URL = process.env.DJANGO_API_URL ?? "http://localhost:8000";

const ACCESS_COOKIE = "tc_access";
const REFRESH_COOKIE = "tc_refresh";

const COOKIE_OPTIONS = {
  httpOnly: true,
  secure: process.env.NODE_ENV === "production",
  sameSite: "lax" as const,
  path: "/",
};

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (PUBLIC_PATHS.some((path) => pathname.startsWith(path))) {
    return NextResponse.next();
  }

  let accessToken = request.cookies.get(ACCESS_COOKIE)?.value;
  let payload = accessToken ? decodeJwtPayload<AccessTokenPayload>(accessToken) : null;

  const refreshToken = request.cookies.get(REFRESH_COOKIE)?.value;
  if ((!payload || isTokenExpired(payload)) && refreshToken) {
    const refreshedAccess = await tryRefreshAccessToken(refreshToken);
    if (refreshedAccess) {
      accessToken = refreshedAccess;
      payload = decodeJwtPayload<AccessTokenPayload>(refreshedAccess);
    }
  }

  const isValidSession =
    payload != null && !isTokenExpired(payload) && ALLOWED_WEB_ROLES.includes(payload.role);

  if (!isValidSession) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", pathname);
    const redirectResponse = NextResponse.redirect(loginUrl);
    redirectResponse.cookies.delete(ACCESS_COOKIE);
    redirectResponse.cookies.delete(REFRESH_COOKIE);
    return redirectResponse;
  }

  const response = NextResponse.next();

  if (accessToken && accessToken !== request.cookies.get(ACCESS_COOKIE)?.value) {
    response.cookies.set(ACCESS_COOKIE, accessToken, COOKIE_OPTIONS);
  }

  return response;
}

async function tryRefreshAccessToken(refreshToken: string): Promise<string | null> {
  try {
    const res = await fetch(`${DJANGO_API_URL}/api/v1/auth/refresh/`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refresh: refreshToken }),
    });
    if (!res.ok) return null;
    const data = (await res.json()) as { access?: string };
    return data.access ?? null;
  } catch {
    return null;
  }
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
