# Scenic Home — Fire TV

A quiet, photo-led home **app** for an Insignia Fire OS 7 TV. It is inspired by CleanFrame's visual direction but is a separate native Android project; no CleanFrame source or assets are included. Scenic Home does **not** replace Amazon's system launcher, intercept the Home button, remove Amazon ads elsewhere, or change any TV packages.

## What v1 does

- Shows eight bundled, original AI-generated scenic backgrounds, rotating once per **local** day or frozen to a chosen scene; works offline.
- Displays a 12-hour clock/date and a translucent dock. Pin and reorder up to eight launchable installed apps using the TV remote; selections persist locally. Temporarily uninstalled favorites are kept in storage but hidden from the dock.
- Offers Settings and Amazon Home shortcuts on a best-effort basis. Live TV and Inputs currently show instructions to use the remote or Fire TV controls: there is no verified portable direct action for this TV. The physical Home key remains the reliable escape.
- Has no network permission, account sign-in, telemetry, autoplay, or recommendations.

## Remote controls

- **Arrows:** move focus; **Select:** open app/action or pin/unpin an app.
- **Manage:** select an app to pin/unpin; use Left/Right while focused on a pinned app to move it in the saved order. **Menu** also opens Manage when available.
- **Wallpapers:** choose Daily rotation or a fixed scene.
- **Back:** returns from Manage/Wallpapers to the scenic screen. On the scenic screen it exits the app. **Home:** returns to the stock Fire TV interface; this app does not intercept it.

## Build locally

Requires JDK 17, an Android SDK with platform `android-35` and build-tools `35.0.0`, and network access *at build time* for the pinned Gradle/Android plugin/test dependencies. The app itself needs no network connection.

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug-signed APK is `app/build/outputs/apk/debug/app-debug.apk`. This is a **development build**, not a production-signed release; do not publish the debug APK as a store release. Gradle outputs, local SDK paths, keystores and built APKs are ignored by Git.

## Manual installation and recovery

The app has been built and exercised on a local **Android TV API 28 emulator**, not installed on the user's Insignia TV. Before attempting installation on the TV, enable only the device's supported developer/sideload setting and confirm its ADB or on-device installation path. Network ADB availability on the Insignia Fire OS 7.7.1.5 firmware has **not** been confirmed. When an authorized ADB connection exists:

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open **Scenic Home** manually from the TV's installed apps. If a shortcut fails, use the remote's Home or Inputs/Live TV controls, or uninstall Scenic Home from Fire TV settings. No Home-key redirect, Accessibility service, root, or system-package disabling is part of this version. Test Settings, Home, Live TV, Inputs, app launches, remote navigation, image performance and persistence on the actual TV before relying on them. See `docs/scenic-home-design.md` and `docs/plans/2026-10-22-scenic-home-v1.md` for scope and testing stages.

## Assets and license

Background and thumbnail provenance is in `docs/assets.md`. The repository includes a GPL-3.0 license in `LICENSE`. The scenes are synthetic images, not photographs of identified places.
