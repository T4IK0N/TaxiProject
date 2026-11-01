# Taxi Project

🇵🇱 [Polska wersja](README.pl.md)

`TaxiProject` is a **monorepo** containing a mobile/desktop app, a backend and a web application.

The web application is currently **under development** and will eventually serve as an administration panel for administrators and people managing the system.

The app for taxi drivers is built with **Kotlin Multiplatform** and **Compose Multiplatform**.
The project shares code across:

* Android
* Desktop (Windows / macOS / Linux)

The app communicates with the backend through a REST API and does not require a local database.

## Technologies

### Application

* Kotlin
* Kotlin Multiplatform
* Compose Multiplatform
* Koin (dependency injection)
* Ktor Client
* Android
* JVM / Desktop
* Gradle

### Backend

* Python
* Django
* Django REST Framework
* PostgreSQL
* JWT

### Infrastructure

* PostgreSQL
* Redis
* Celery
* Docker
* Docker Compose

## Architecture

The project uses shared Kotlin Multiplatform code with separate implementations of platform-dependent components.
The main features of the app include:

* user login,
* driver schedule,
* vehicle information,
* fuel logs,
* driver profile.

The app data is fetched from the backend through the API.
The app does not keep a persistent local copy of business data. Data is fetched from the server whenever a screen is opened.

### Dependency injection

Dependencies are wired with Koin. The shared `App(platformModule)` composable starts Koin with the common modules (`appModules`) plus a **platform module** passed in from each platform's entry point. The platform module provides the pieces that cannot live in shared code, such as the platform-specific `SecretStore` used for storing tokens securely (see [Authentication](#authentication)).

```kotlin
// shared code
@Composable
fun App(platformModule: Module) {
    KoinApplication(application = { modules(appModules + platformModule) }) {
        // ...
    }
}
```

## Backend

The app uses the API at:

`https://api-taxi.gasowski.eu`

The API address is configured in `PlatformConfig` for each platform.

Example configuration:

```kotlin
actual object PlatformConfig {
    actual val apiBaseUrl: String = "https://api-taxi.gasowski.eu"
}
```

> If you run your own backend, you can change the API address in the project configuration.

## Requirements

To work on the project you need:

* Android Studio
* JDK 17 or newer
* Git

Android Studio includes the tools required to work with Kotlin Multiplatform.

### Android

To run the app on Android you need:

* an Android emulator configured in Android Studio
  or:
* a physical device with USB debugging enabled.

### Desktop

Running the desktop version requires no additional tools beyond a Java/JDK environment.

On Linux, secure token storage relies on the system **Secret Service** (e.g. GNOME Keyring or KWallet), so one of them has to be installed and running.

## Download

A ready-to-install Android app can be downloaded directly from this repository in the **Releases** section.

[Download `TaxiProject.apk`](../../releases/latest)

After downloading the `.apk` file you can install the app directly on an Android device.

If you want to build the app yourself, see the **Android** section below.

> **Note:** during installation Android may show a warning about apps from outside Google Play. If you are asked to allow the installation or to confirm the app's safety, accept it provided the app comes from this repository.

## Running the project

Clone the repository:

```bash
git clone https://github.com/T4IK0N/TaxiProject.git
cd TaxiProject
```

Then open the project in Android Studio:

**Android Studio → Open → select the `mobile` directory inside the project directory**

On first launch Gradle may need a few minutes to download dependencies and sync the project.

## Android

The easiest way to run the app is Android Studio.

1. Start an emulator or connect a device.
2. Select the app run configuration.
3. Select the target device.
4. Click **Run**.

> **Possible issue:** on first launch you may see the `SDK location not found` error. It means Android Studio cannot find the Android SDK. Check its location in **Settings → Languages & Frameworks → Android SDK**, then create the `mobile/local.properties` file with the following entry:
>
> ```properties
> sdk.dir=C:/Users/Username/AppData/Local/Android/Sdk
> ```
>
> Use your own path to the Android SDK. Do not commit `local.properties` to the repository.

You can also build and install the app from the terminal:

```bash
./gradlew :composeApp:installDebug
```

The task name may depend on the current project configuration.

## Desktop

The desktop version can be run with Gradle:

```bash
./gradlew :composeApp:run
```

If the task is not available, list the Gradle tasks:

```bash
./gradlew tasks
```

or check the Compose Desktop configuration section in:

```text
composeApp/build.gradle.kts
```

## Project structure

Example structure:

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

Code shared between platforms lives in `commonMain`.
Android-specific code lives in `androidMain`, and Desktop-specific code lives in `desktopMain`.

Platform-dependent parts of token storage are split as follows:

| Source set | Contents |
|---|---|
| `commonMain` | `TokenStorage`, `SecureTokenStorage`, `SecretStore` (interface) |
| `androidMain` | `AndroidKeystoreSecretStore`, `MainActivity` (provides the platform module) |
| `desktopMain` | `DesktopKeyringSecretStore`, desktop entry point (provides the platform module) |

## API configuration

The backend address is defined by `PlatformConfig`.
Thanks to the `expect` / `actual` mechanism each platform can have its own configuration:

```kotlin
expect object PlatformConfig {
    val apiBaseUrl: String
}
```

Example platform implementation:

```kotlin
actual object PlatformConfig {
    actual val apiBaseUrl: String =
        "https://api-taxi.gasowski.eu"
}
```

If you run your own backend environment, change the `apiBaseUrl` value to match your setup.

## App data

The app uses data provided by the backend.
Depending on the available data, the user can use, among others:

* the schedule,
* driver information,
* vehicle information,
* fuel log history,
* the profile.

The data displayed in the app depends on the data available on the configured API server.

## Authentication

Authentication is handled by the backend (JWT).
After logging in, the app uses the received access and refresh tokens to make subsequent API requests.

Tokens are stored in **platform-native secure storage**, behind a common `SecretStore` abstraction:

| Platform | Storage |
|---|---|
| Android | AES-256-GCM encryption with a key held in the **Android Keystore**. The key never leaves the Keystore; only the ciphertext is kept in app-private preferences. |
| Desktop | The operating system credential store (Windows Credential Manager, macOS Keychain, Linux Secret Service), accessed through [java-keyring](https://github.com/javakeyring/java-keyring). |

If the stored tokens cannot be decrypted (for example after a backup restore on another device, or when the Keystore key is lost), they are discarded and the user simply has to log in again.

Do not commit to the repository:

* user passwords,
* tokens,
* API keys,
* secrets,
* private keys,
* production data.

## Known limitations

The project is under active development. Some elements may still need polishing.

### No registration

The KMP app does not offer self-registration.

This follows from the project's assumptions: the mobile/desktop app is intended exclusively for taxi drivers who were previously added to the system by an administrator or a manager.

Adding and managing drivers will be done through the web application, which is currently under development and will eventually act as the system's administration panel.

For this reason the driver app offers **login** only. A driver receives access credentials once their account has been created in the system.

How it works:

1. An administrator or manager adds the driver in the web application.
2. An account is created for the driver in the system.
3. The driver receives the credentials needed to log in.
4. The driver logs in using the mobile or desktop app.
5. After logging in, the app fetches the driver's data from the backend.

This approach restricts access to the app to users who have been verified and added to the system beforehand.

### Token storage

* Tokens previously stored by older versions in plain-text settings are not migrated, so users have to log in once after updating.
* On Linux desktop, a running Secret Service (GNOME Keyring / KWallet) is required.

### Schedule refresh

If the session expires while data is being fetched, the user may have to log in again.

## Project status

The project is under active development.
Some elements of the architecture and the interface may change in future versions.
