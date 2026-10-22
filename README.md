# Scenic Home — Fire TV

A quiet, photo-led home **app** for an Insignia Fire OS 7 TV. It is inspired by CleanFrame's visual direction but is a separate native Android project; no CleanFrame source or assets are included. The user reports Scenic Home opens manually on the actual Insignia TV. Scenic Home does **not** replace Amazon's system launcher, intercept the Home button, remove Amazon ads elsewhere, or change any TV packages. Home-button and boot/wake redirection are untested on this TV and require a separate, optional helper.

## What v1 does

- Shows eight bundled, original AI-generated scenic backgrounds, rotating once per **local** day or frozen to a chosen scene; works offline.
- Displays a 12-hour clock/date and a translucent dock. Pin and reorder up to eight launchable installed apps using the TV remote; selections persist locally. Temporarily uninstalled favorites are kept in storage but hidden from the dock.
- Offers Settings and Amazon Home shortcuts on a best-effort basis. Live TV and Inputs currently show instructions to use the remote or Fire TV controls: there is no verified portable direct action for this TV. Without the optional helper, the physical Home key returns to the stock interface; with the helper enabled, test its long-press Home escape before enabling boot/wake.
- Has no network permission, account sign-in, telemetry, autoplay, or recommendations.

## Remote controls

- **Arrows:** move focus; **Select:** open app/action or pin/unpin an app.
- **Manage:** select an app to pin/unpin; use Left/Right while focused on a pinned app to move it in the saved order. **Menu** also opens Manage when available.
- **Wallpapers:** choose Daily rotation or a fixed scene.
- **Back:** returns from Manage/Wallpapers to the scenic screen. On the scenic screen it exits the app. **Home:** normally returns to the stock Fire TV interface; this app does not intercept it. A separately installed, optional helper can redirect Home; see [the untested helper setup and recovery guide](docs/home-on-fire-setup.md).

## Build locally

Requires JDK 17, an Android SDK with platform `android-35` and build-tools `35.0.0`, and network access *at build time* for the pinned Gradle/Android plugin/test dependencies. The app itself needs no network connection.

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug-signed APK is `app/build/outputs/apk/debug/app-debug.apk`. This is a **development build**, not a production-signed release; do not publish the debug APK as a store release. Gradle outputs, local SDK paths, keystores and built APKs are ignored by Git.

## Manual installation and recovery

The app has been built and exercised on a local **Android TV API 28 emulator**; the user also reports opening Scenic Home manually on the actual Insignia TV. This does not verify Home-button redirection or boot/wake. For any ADB installation, first confirm the TV's supported developer/sideload settings and ADB path; network ADB availability on this Insignia Fire OS 7.7.1.5 firmware has **not** been confirmed. The following example is for an authorized **network ADB** connection to the intended TV (replace `TV_IP` locally):

```bash
adb devices
adb -s TV_IP:5555 install -r app/build/outputs/apk/debug/app-debug.apk
```

For a USB-connected TV, substitute the device serial shown by `adb devices` for `TV_IP:5555` in the `adb -s` command.

Open **Scenic Home** manually from the TV's installed apps. If a shortcut fails, use the remote's Home or Inputs/Live TV controls, or uninstall Scenic Home from Fire TV settings. No in-app Home-key redirect, Accessibility service, root, or system-package disabling is part of Scenic Home. An [optional, reversible **Home on Fire** helper](docs/home-on-fire-setup.md) can be configured separately; its Home redirect, long-press escape, and boot/wake behavior remain **untested on this Insignia**. Test Settings, Home, Live TV, Inputs, app launches, remote navigation, image performance and persistence on the actual TV before relying on them. See `docs/scenic-home-design.md` and `docs/plans/2026-10-22-scenic-home-v1.md` for scope and testing stages.

## Assets and license

Background and thumbnail provenance is in `docs/assets.md`. The repository includes a GPL-3.0 license in `LICENSE`. The scenes are synthetic images, not photographs of identified places.
