# Taxi App

`TaxiProject` jest **monorepo** zawierającym aplikację mobilną/desktopową, backend oraz aplikację webową.

Aplikacja webowa jest obecnie **w trakcie rozwoju** i docelowo będzie służyć jako panel administracyjny dla administratorów oraz osób zarządzających systemem.

Aplikacja dla kierowców taxi zbudowana w **Kotlin Multiplatform** i **Compose Multiplatform**.
Projekt wykorzystuje wspólny kod dla:

* Android
* Desktop (Windows / macOS / Linux)

Aplikacja komunikuje się z backendem poprzez REST API i nie wymaga lokalnej bazy danych.

## Technologie

### Aplikacja

* Kotlin
* Kotlin Multiplatform
* Compose Multiplatform
* Android
* JVM / Desktop
* Gradle

### Backend

* Python
* Django
* Django REST Framework
* PostgreSQL
* JWT

### Infrastruktura

* PostgreSQL
* Redis
* Celery
* Docker
* Docker Compose

## Architektura

Projekt wykorzystuje wspólny kod Kotlin Multiplatform z osobnymi implementacjami elementów zależnych od platformy.
Główne elementy aplikacji obejmują m.in.:

* logowanie użytkownika,
* grafik kierowcy,
* informacje o pojeździe,
* tankowania,
* profil kierowcy.

Dane aplikacji są pobierane z backendu poprzez API.
Aplikacja nie przechowuje trwałej lokalnej kopii danych biznesowych. Po wejściu na poszczególne ekrany dane są pobierane z serwera.

## Backend

Aplikacja korzysta z API:

`https://api-taxi.gasowski.eu`

Adres API jest konfigurowany w `PlatformConfig` dla poszczególnych platform.

Przykładowa konfiguracja:

```kotlin
actual object PlatformConfig {
    actual val apiBaseUrl: String = "https://api-taxi.gasowski.eu"
}
```

> W przypadku uruchamiania własnego backendu adres API można zmienić w konfiguracji projektu.

## Wymagania

Do pracy nad projektem potrzebujesz:

* Android Studio
* JDK 17 lub nowszego
* Git

Android Studio zawiera odpowiednie narzędzia potrzebne do pracy z Kotlin Multiplatform.

### Android

Do uruchomienia aplikacji na Androidzie potrzebujesz:

* emulatora Androida skonfigurowanego w Android Studio
  lub:
* fizycznego urządzenia z włączonym debugowaniem USB.

### Desktop

Do uruchomienia wersji desktopowej nie są wymagane dodatkowe narzędzia poza środowiskiem Java/JDK.

## Uruchomienie projektu

Sklonuj repozytorium:

```bash
git clone https://github.com/T4IK0N/TaxiProject.git
cd TaxiProject
```

Następnie otwórz projekt w Android Studio:

**Android Studio → Open → wybierz katalog 'mobile' z katalogu projektu**

Przy pierwszym uruchomieniu Gradle może potrzebować kilku minut na pobranie zależności i synchronizację projektu.

## Pobranie aplikacji

Gotową aplikację na Androida można pobrać bezpośrednio z tego repozytorium w sekcji **Releases**.

👉 [Pobierz `TaxiProject.apk`](../../releases/latest)

Po pobraniu pliku `.apk` można zainstalować aplikację bezpośrednio na urządzeniu z Androidem.

Jeżeli chcesz samodzielnie zbudować aplikację, przejdź do sekcji **Android** poniżej.

> **Uwaga:** podczas instalacji Android może wyświetlić ostrzeżenie dotyczące aplikacji spoza Google Play. Jeśli pojawi się pytanie o zezwolenie na instalację lub potwierdzenie bezpieczeństwa aplikacji, należy zaakceptować je, jeśli aplikacja pochodzi z tego repozytorium.

## Android

Najprostszym sposobem uruchomienia aplikacji jest Android Studio.

1. Uruchom emulator lub podłącz urządzenie.
2. Wybierz konfigurację aplikacji.
3. Wybierz urządzenie docelowe.
4. Kliknij **Run**.

> **Możliwy problem:** przy pierwszym uruchomieniu może pojawić się błąd `SDK location not found`. Oznacza to, że Android Studio nie może znaleźć Android SDK. Sprawdź jego lokalizację w **Settings → Languages & Frameworks → Android SDK**, a następnie utwórz plik `mobile/local.properties` z wpisem:
>
> ```properties
> sdk.dir=C:/Users/Username/AppData/Local/Android/Sdk
> ```
>
> Podstaw własną ścieżkę do Android SDK. Pliku `local.properties` nie należy commitować do repozytorium.

Aplikację można również zbudować i zainstalować z terminala:

```bash
./gradlew :composeApp:installDebug
```

Nazwa taska może zależeć od aktualnej konfiguracji projektu.

## Desktop

Wersję desktopową można uruchomić z Gradle:

```bash
./gradlew :composeApp:run
```

Jeżeli task nie jest dostępny, sprawdź zadania Gradle:

```bash
./gradlew tasks
```

lub sekcję konfiguracji Compose Desktop w:

```text
composeApp/build.gradle.kts
```

## Struktura projektu

Przykładowa struktura:

```text
.
├── composeApp/
│   ├── src/
│   │   ├── commonMain/
│   │   ├── androidMain/
│   │   └── desktopMain/
│   └── build.gradle.kts
├── gradle/
├── settings.gradle.kts
└── README.md
```

Kod współdzielony pomiędzy platformami znajduje się w `commonMain`.
Kod specyficzny dla Androida znajduje się w `androidMain`, natomiast kod platformy Desktop w `desktopMain`.

## Konfiguracja API

Adres backendu jest definiowany przez `PlatformConfig`.
Dzięki wykorzystaniu mechanizmu `expect` / `actual` poszczególne platformy mogą posiadać własną konfigurację:

```kotlin
expect object PlatformConfig {
    val apiBaseUrl: String
}
```

Przykładowa implementacja platformowa:

```kotlin
actual object PlatformConfig {
    actual val apiBaseUrl: String =
        "https://api-taxi.gasowski.eu"
```

Jeżeli uruchamiasz własne środowisko backendowe, zmień wartość `apiBaseUrl` odpowiednio do swojej konfiguracji.

## Dane aplikacji

Aplikacja korzysta z danych dostarczanych przez backend.
W zależności od dostępnych danych użytkownik może korzystać m.in. z:

* grafiku,
* informacji dotyczących kierowcy,
* informacji dotyczących pojazdu,
* historii tankowań,
* profilu.

Dane wyświetlane w aplikacji zależą od danych dostępnych na skonfigurowanym serwerze API.

## Uwierzytelnianie

Aplikacja korzysta z uwierzytelniania po stronie backendu.
Po zalogowaniu aplikacja wykorzystuje otrzymane dane uwierzytelniające do wykonywania kolejnych zapytań do API.
Nie należy umieszczać w repozytorium:

* haseł użytkowników,
* tokenów,
* kluczy API,
* sekretów,
* kluczy prywatnych,
* danych produkcyjnych.

## Znane ograniczenia

Projekt jest aktywnie rozwijany. Niektóre elementy mogą jeszcze wymagać dopracowania.

### Zmiana orientacji Androida

Zmiana konfiguracji aplikacji, np. obrót ekranu, może powodować ponowne utworzenie stanu niektórych ekranów.

### Przechowywanie danych uwierzytelniających

Obecna implementacja przechowywania danych sesji jest rozwiązaniem przeznaczonym przede wszystkim do celów rozwojowych i testowych.
Docelowo mechanizm ten będzie zastąpiony rozwiązaniem wykorzystującym bezpieczny magazyn systemowy.

### Odświeżanie grafiku

W przypadku wygaśnięcia sesji podczas pobierania danych może być konieczne ponowne zalogowanie użytkownika.

## Status projektu

Projekt jest w aktywnym rozwoju.
Niektóre elementy architektury i interfejsu mogą ulec zmianie w kolejnych wersjach.
