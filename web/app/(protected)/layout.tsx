import type { ReactNode } from "react";
import { getSessionUser } from "@/lib/auth/session";
import { logoutAction } from "@/lib/auth/actions";
import { Role } from "@/types/auth";

export default async function ProtectedLayout({ children }: { children: ReactNode }) {
  const user = await getSessionUser();

  if (!user) {
    const { redirect } = await import("next/navigation");
    redirect("/login");
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
        <div className="flex items-center gap-6">
          <span className="font-semibold text-slate-900">Taxi Company</span>
          <nav className="flex gap-4 text-sm text-slate-600">
            <a href="/schedule" className="hover:text-slate-900">
              Grafik
            </a>
            {/* Ksiegowosc na razie nie potrzebuje dyspozycji kierowcami itd. */}
            {(user.role === Role.ADMIN || user.role === Role.DISPATCHER) && (
              <>
                <a href="/drivers" className="hover:text-slate-900">
                  Kierowcy
                </a>
                <a href="/vehicles" className="hover:text-slate-900">
                  Pojazdy
                </a>
              </>
            )}
            {(user.role === Role.ADMIN || user.role === Role.ACCOUNTANT) && (
              <a href="/billing" className="hover:text-slate-900">
                Rozliczenia
              </a>
            )}
            {user.role === Role.ADMIN && (
              <a href="/users" className="hover:text-slate-900">
                Użytkownicy
              </a>
            )}
          </nav>
        </div>

        <div className="flex items-center gap-4 text-sm text-slate-600">
          <span>
            {user.fullName} <span className="text-slate-400">({roleLabel(user.role)})</span>
          </span>
          <form action={logoutAction}>
            <button type="submit" className="text-slate-500 hover:text-slate-900">
              Wyloguj
            </button>
          </form>
        </div>
      </header>

      <main className="p-6">{children}</main>
    </div>
  );
}

function roleLabel(role: Role): string {
  switch (role) {
    case Role.ADMIN:
      return "Administrator";
    case Role.DISPATCHER:
      return "Dyspozytor";
    case Role.ACCOUNTANT:
      return "Księgowość";
    case Role.DRIVER:
      return "Kierowca";
  }
}
