"use server";

import { redirect } from "next/navigation";
import { InvalidCredentialsError, loginWithDjango } from "./api";
import { clearSessionTokens, setSessionTokens } from "./session";
import { decodeJwtPayload } from "./jwt";
import { ALLOWED_WEB_ROLES, type AccessTokenPayload } from "@/types/auth";

export interface LoginFormState {
  error: string | null;
}

export async function loginAction(
  _prevState: LoginFormState,
  formData: FormData,
): Promise<LoginFormState> {
  const username = String(formData.get("username") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const next = String(formData.get("next") ?? "/") || "/";

  if (!username || !password) {
    return { error: "Podaj login i hasło." };
  }

  let access: string;
  let refresh: string;

  try {
    const tokens = await loginWithDjango(username, password);
    access = tokens.access;
    refresh = tokens.refresh;
  } catch (err) {
    if (err instanceof InvalidCredentialsError) {
      return { error: err.message };
    }
    return { error: "Błąd logowania. Spróbuj ponownie." };
  }

  const payload = decodeJwtPayload<AccessTokenPayload>(access);

  if (!payload || !ALLOWED_WEB_ROLES.includes(payload.role)) {
    return { error: "To konto nie ma dostępu do panelu administracyjnego." };
  }

  await setSessionTokens(access, refresh);
  redirect(next);
}

export async function logoutAction() {
  await clearSessionTokens();
  redirect("/login");
}
