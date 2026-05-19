# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

CrewUp — Android social events app built with Jetpack Compose and Material 3.

- Package: `com.crewup.app`
- Single module (`:app`)
- Kotlin 2.0.21 · AGP 9.0.1 · compileSdk 36 · minSdk 26 · Java 11

## Build Commands

```bash
# Windows
gradlew.bat assembleDebug
gradlew.bat installDebug

# Unit tests
gradlew.bat test

# Instrumented tests (requires connected device/emulator)
gradlew.bat connectedAndroidTest

# Single unit test class
gradlew.bat testDebugUnitTest --tests "com.crewup.app.ExampleUnitTest"
```

## Architecture — MVVM

Target architecture: **MVVM with ViewModel + StateFlow**.

- Each screen gets a `ViewModel` that holds UI state as `StateFlow<UiState>`
- Composables collect state with `collectAsStateWithLifecycle()` and emit events to the ViewModel — no business logic in composables
- ViewModels are not yet added (early stage); introduce them screen by screen

## Navigation

Routes are defined as a sealed class `Screen` in `AppNavigation.kt`. Add new routes there, not inline in composables. Current route flow:

```
Welcome → Accueil → Login / Register → SetupProfile → Home
```

## Dependencies

All versions are managed via the TOML version catalog at `gradle/libs.versions.toml`. Always add new dependencies through the catalog — do not hardcode version strings in `app/build.gradle.kts`.

## Backend — Firebase

Planned backend: Firebase (Authentication, Firestore, Storage). Not yet integrated. When adding Firebase:
- Use the Firebase BOM for consistent versions
- Add the `google-services` plugin in `build.gradle.kts` and the `google-services.json` to `app/` (do not commit the JSON)

## Theming

- Material 3 with custom `CrewUpTheme` (see `ui/theme/`)
- Light mode only for now — no dark mode variant
- Custom color palette in `Color.kt`; use named tokens, not raw hex values

## Code Style

- `kotlin.code.style=official` (enforced in `gradle.properties`)
- detekt is configured for static analysis — run with `gradlew.bat detekt`
- Config at `detekt.yml` (relaxed rules for Compose: magic numbers and PascalCase function names are allowed)

## Commit Messages

Write commit messages in **French**. Follow the existing style:
> `initialisation de l'architecture et creation des pages acceuil et login/register`
