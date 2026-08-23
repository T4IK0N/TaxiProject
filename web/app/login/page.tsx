"use client";

import { useActionState } from "react";
import { useSearchParams } from "next/navigation";
import { loginAction, type LoginFormState } from "@/lib/auth/actions";

const initialState: LoginFormState = { error: null };

export default function LoginPage() {
  const searchParams = useSearchParams();
  const next = searchParams.get("next") ?? "/";

  const [state, formAction, isPending] = useActionState(loginAction, initialState);

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <form
        action={formAction}
        className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm"
      >
        <h1 className="mb-1 text-xl font-semibold text-slate-900">Taxi Company</h1>
        <p className="mb-6 text-sm text-slate-500">
          Panel administracyjny - logowanie
        </p>

        <input type="hidden" name="next" value={next} />

        <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="username">
          Nazwa użytkownika
        </label>
        <input
          id="username"
          name="username"
          type="text"
          required
          disabled={isPending}
          autoComplete="username"
          className="mb-4 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:opacity-60"
        />

        <label className="mb-1 block text-sm font-medium text-slate-700" htmlFor="password">
          Hasło
        </label>
        <input
          id="password"
          name="password"
          type="password"
          required
          disabled={isPending}
          autoComplete="current-password"
          className="mb-4 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-slate-500 disabled:opacity-60"
        />

        {state.error && (
          <p className="mb-4 text-sm text-red-600" role="alert">
            {state.error}
          </p>
        )}

        <button
          type="submit"
          disabled={isPending}
          className="w-full rounded-md bg-slate-900 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:opacity-60"
        >
          {isPending ? "Logowanie..." : "Zaloguj się"}
        </button>
      </form>
    </main>
  );
}
