import "server-only";

const DJANGO_API_URL = process.env.DJANGO_API_URL ?? "http://localhost:8000";

export interface TokenPair {
  access: string;
  refresh: string;
}

export class InvalidCredentialsError extends Error {}

export async function loginWithDjango(username: string, password: string): Promise<TokenPair> {
  const response = await fetch(`${DJANGO_API_URL}/api/v1/auth/login/`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
    cache: "no-store",
  });

  if (response.status === 401) {
    throw new InvalidCredentialsError("Błędny login lub hasło.");
  }
  if (!response.ok) {
    throw new Error(`Logowanie nie powiodło się (status ${response.status}).`);
  }

  return response.json();
}

export async function refreshDjangoToken(refreshToken: string): Promise<string> {
  const response = await fetch(`${DJANGO_API_URL}/api/v1/auth/refresh/`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refresh: refreshToken }),
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error("Nie udało się odświeżyć sesji.");
  }

  const data = (await response.json()) as { access: string };
  return data.access;
}
