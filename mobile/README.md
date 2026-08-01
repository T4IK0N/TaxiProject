# Taxi Company App - Kotlin Multiplatform

Projekt KMP (Android + Desktop) ze wspolnym UI w Compose Multiplatform.
Konsumuje REST API z `backend/` (Django).

## Wymagania

- **Android Studio** (najnowszy stabilny, z wtyczka Kotlin Multiplatform Mobile)
- **JDK 17+** (Android Studio zazwyczaj ma wlasne wbudowane)

## Pierwsze uruchomienie

1. Otworz folder `mobile/` w Android Studio: `File -> Open`
2. Android Studio wykryje `settings.gradle.kts` i zaproponuje sync - zgodz sie.

## Uruchomienie poszczegolnych platform

**Android** (z Android Studio):
- Wybierz konfiguracje `composeApp` i urzadzenie/emulator -> Run

**Desktop** (Windows/macOS/Linux, z terminala w folderze `mobile/`):
```
.\gradlew.bat :composeApp:run    (Windows)
./gradlew :composeApp:run        (macOS/Linux)
```

## Polaczenie z backendem Django

Backend musi byc odpalony (patrz `backend/README.md` lub `docker-compose.yml`
w katalogu glownym projektu) na `http://localhost:8000`.

Adres bazowy API jest skonfigurowany per platforma w `PlatformConfig`
(`commonMain` deklaruje `expect`, `androidMain`/`desktopMain` dostarczaja
`actual`):
- **Android Emulator**: `10.0.2.2` (specjalny alias na hosta, NIE `localhost`)
- **Desktop**: `localhost` (to ten sam komputer co Docker)
- **Urzadzenie fizyczne Android**: trzeba recznie podac adres IP komputera
  w sieci lokalnej w `PlatformConfig.android.kt` (np. `192.168.1.50`)

## Struktura projektu

```
composeApp/
├── build.gradle.kts        <- definicja targetow (android/desktop) i zaleznosci
└── src/
    ├── commonMain/          <- 90% kodu: UI, logika biznesowa, siec
    │   └── kotlin/com/taxicompany/app/
    │       ├── core/         <- HttpClient, TokenStorage, PlatformConfig
    │       ├── data/
    │       │   ├── remote/   <- DTO + klasy *Api (Ktor)
    │       │   └── repository/
    │       └── di/           <- moduly Koin
    ├── androidMain/          <- MainActivity, AndroidManifest.xml
    └── desktopMain/          <- main() dla okna desktopowego
```

## Status

Warstwa sieciowa (Ktor + JWT z auto-refresh) jest gotowa dla modulu
`auth` (logowanie, profil). Kolejne kroki:
1. Repozytoria + API dla `drivers`, `vehicles`, `scheduling`, `fuel`
   (ten sam wzorzec co `AuthApi`/`AuthRepository`)
2. Ekrany UI: logowanie, lista kierowcow/pojazdow, grafik, tankowania
3. Nawigacja miedzy ekranami
