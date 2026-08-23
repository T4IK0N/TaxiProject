import "server-only";
import { getAccessToken, getRefreshToken, updateAccessToken } from "@/lib/auth/session";
import { refreshDjangoToken } from "@/lib/auth/api";

const DJANGO_API_URL = process.env.DJANGO_API_URL ?? "http://localhost:8000";

export class UnauthorizedError extends Error {}

export async function authorizedFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const access = await getAccessToken();
  if (!access) throw new UnauthorizedError("Brak sesji.");

  const doFetch = (token: string) =>
    fetch(`${DJANGO_API_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init.headers,
        Authorization: `Bearer ${token}`,
      },
      cache: "no-store",
    });

  let response = await doFetch(access);

  if (response.status === 401) {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) {
      throw new UnauthorizedError("Sesja wygasła.");
    }

    let newAccess: string;
    try {
      newAccess = await refreshDjangoToken(refreshToken);
    } catch {
      throw new UnauthorizedError("Sesja wygasła.");
    }

    await updateAccessToken(newAccess);
    response = await doFetch(newAccess);

    if (response.status === 401) {
      throw new UnauthorizedError("Sesja wygasła.");
    }
  }

  return response;
}
