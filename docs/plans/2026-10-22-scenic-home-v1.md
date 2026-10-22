# Scenic Home v1 Implementation Plan

> **For Hermes:** Use the subagent-driven-development skill to implement this plan task-by-task, with spec and quality review before completion claims.

**Goal:** Build a manually launched, offline, remote-friendly scenic Fire TV app and an installable debug APK; do not alter the TV or redirect its Home key in this phase.

**Architecture:** Single native Java Activity using Android framework views (no WebView, Play Services, network or telemetry). Small pure-Java models manage favorites and daily wallpaper selection; Android adapters handle installed-app discovery, SharedPreferences, images and system actions. Keep Amazon Home available and treat Live TV/Inputs intents as device-dependent, with a visible fallback.

**Tech stack:** Android Gradle Plugin 8.8.2, Gradle 8.10.2 wrapper, JDK 17, compile SDK 35, min/target SDK 28, Java 17 syntax compatible with Android toolchain, JUnit 4.13.2. Native drawable resources and eight locally generated scenic WebP images. No device access required for local build.

**Source of truth:** `docs/scenic-home-design.md`. Work in `rkrusz/FireTVCleanUI`, not the standalone local design draft. License is the repository's GPL-3.0; do not copy CleanFrame source or assets.

## Task 1 — Android build scaffold

**Files:** `settings.gradle`, `build.gradle`, `app/build.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `.gitignore`, `app/src/main/AndroidManifest.xml`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/styles.xml`.

1. Add the Android application plugin version 8.8.2, `google()` and `mavenCentral()`. Set `namespace` and `applicationId` to `dev.krusz.scenichome`; compile 35, min/target 28, and a Java 17 compilation target. Add `testImplementation 'junit:junit:4.13.2'`.
2. Set manifest to `android:usesCleartextTraffic="false"`; do not request INTERNET or storage permission; declare TV leanback support and touchscreen not required. Provide a launcher activity with `MAIN` plus both `LAUNCHER` and `LEANBACK_LAUNCHER` categories and `exported="true"`.
3. Generate wrapper from the pinned Gradle distribution; verify the wrapper JAR is real and executable and the distribution SHA matches the official checksum. Ignore `.gradle/`, `build/`, `local.properties`, `*.apk`, IDE output; never commit the SDK or signing keys.
4. Test: `./gradlew --version` and `./gradlew :app:assembleDebug` (after the Activity is added). Expected: wrapper reports Gradle 8.10.2; `assembleDebug` exits 0.

## Task 2 — Wallpaper selection and assets

**Files:** `app/src/main/java/dev/krusz/scenichome/DailyWallpaper.java`, `app/src/test/java/dev/krusz/scenichome/DailyWallpaperTest.java`, `app/src/main/res/drawable-nodpi/scenic_*.webp`, `docs/assets.md`.

1. Write failing JUnit tests: same local date gives same index; next date rotates; frozen selection remains fixed; invalid saved selection defaults safely. Example: `assertEquals(DailyWallpaper.indexForDay(100, 8), DailyWallpaper.indexForDay(100, 8)); assertNotEquals(DailyWallpaper.indexForDay(100, 8), DailyWallpaper.indexForDay(101, 8));`.
2. Implement `Math.floorMod(epochDay, count)` for automatic rotation and a guarded frozen index; pass the current local date from Activity via `LocalDate.now().toEpochDay()` if Android API supports it, otherwise use a local `Calendar` day key. Avoid UTC-day surprises.
3. Convert the eight original generated landscapes into 16:9 compressed WebP drawable resources (no names, accounts or serials); document provenance and conversion in `docs/assets.md`. Validate width/height and combined size (goal <15 MB).
4. Run: `./gradlew :app:testDebugUnitTest --tests 'dev.krusz.scenichome.DailyWallpaperTest'`. Expected: all tests pass. Commit only after verifying.

## Task 3 — Favorite ordering model and persistence

**Files:** `app/src/main/java/dev/krusz/scenichome/Favorites.java`, `app/src/main/java/dev/krusz/scenichome/FavoritesStore.java`, `app/src/test/java/dev/krusz/scenichome/FavoritesTest.java`.

1. Write failing JUnit tests for empty list, pin/unpin, duplicate rejection, maximum eight, reordering both ways, and sanitized input; e.g. `assertEquals(List.of("a", "b"), Favorites.move(List.of("b", "a"), 0, 1));`.
2. Implement immutable list operations with a max of eight package names. Persist an ordered list in SharedPreferences using a deterministic length-prefixed format or JSON, never arbitrary code execution. Ignore packages no longer installed at display time, but keep pins in storage so temporarily missing apps are recoverable.
3. Run: `./gradlew :app:testDebugUnitTest --tests 'dev.krusz.scenichome.FavoritesTest'`; then full `./gradlew :app:testDebugUnitTest`. Expected: all tests pass.

## Task 4 — Installed-app catalog and safe launch

**Files:** `app/src/main/java/dev/krusz/scenichome/AppCatalog.java`, `app/src/main/java/dev/krusz/scenichome/SystemActions.java`.

1. Query PackageManager for `ACTION_MAIN` + `CATEGORY_LEANBACK_LAUNCHER` and fallback `CATEGORY_LAUNCHER`; sort labels, dedupe package names, exclude this app. Use explicit resolved launch intents, catch `ActivityNotFoundException` and `SecurityException`, and show a nonblocking explanation.
2. Settings: `Settings.ACTION_SETTINGS` with fallback help. Amazon Home: `ACTION_MAIN` + `CATEGORY_HOME`, with a message to press the physical Home button if blocked. Live TV/Inputs: attempt only documented/resolved intents or installed apps; never present a successful action when none resolves; display remote-key or Fire TV settings instructions instead. Do not request Accessibility, root or disable the stock home.
3. Manually inspect intent-resolution branches and run `./gradlew :app:compileDebugJavaWithJavac`. Device-specific behavior remains unverified until an actual TV test.

## Task 5 — Remote-first home screen and menus

**Files:** `app/src/main/java/dev/krusz/scenichome/ScenicHomeActivity.java`, `app/src/main/res/drawable/focus_*.xml`, `app/src/main/res/values/styles.xml`, `app/src/main/res/values/strings.xml`.

1. Render image plus scrim, 12-hour local clock/date upper left, translucent bottom dock, horizontally focusable favorite tiles and fixed system tiles. When no favorites exist, include a clearly focused `Pin apps` tile.
2. Select opens a tile. Menu opens manage view to pin/unpin/move items; make every action possible with arrows/Select/Back even when Menu is absent. Add a wallpaper chooser with Daily and eight still-image choices. Preserve focus on re-render and return to home with Back.
3. Keep clock updated while the Activity is visible and recompute auto wallpaper after midnight/resume. Avoid timers retained beyond lifecycle. Labels have enough contrast and selected elements visibly change.
4. Verify with `./gradlew :app:assembleDebug :app:testDebugUnitTest` and Android manifest/resource inspection. Emulator/TV focus paths still require real interaction tests; don't claim otherwise.

## Task 6 — Build, documentation and handoff

**Files:** `README.md`, optional `.github/workflows/android.yml`, `docs/assets.md`.

1. Document local build, debug APK path, manual sideloading prerequisites, remote controls, offline/privacy behavior, known untested Live TV/Inputs/Home actions, and recovery using the physical Home key. State clearly that no TV installation or redirect was performed.
2. Run `./gradlew clean :app:testDebugUnitTest :app:assembleDebug --no-daemon`, inspect APK manifest and `unzip -t`, check no INTERNET permission, list expected drawable assets. Report actual command output, artifact path and limitations.
3. Review spec and quality independently, fix issues, then commit/push to `main` and fetch remote to verify exact commit and tracked files. Do not commit generated APK/build folders or claim on-device behavior before testing on the user's Fire TV.

## Later phase, not authorized by this plan

On-device ADB/sideload testing and optional Home/boot/wake redirect are separate opt-in steps. Confirm connectivity and recovery with the user before touching the TV. No aggressive debloat or system launcher removal.
