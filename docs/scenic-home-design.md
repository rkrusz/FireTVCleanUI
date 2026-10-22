# Scenic Home for Insignia Fire TV — Approved Design

## Intent and success criteria

Build a quiet, ad-free **custom home app** for the user's Insignia NS-50F301NA22 running Fire OS 7.7.1.5 (Android 9 / API 28).[2] Take visual inspiration from CleanFrame, which targets Samsung Tizen, without porting its Samsung-specific integration.[1] The screen should feel like a full-bleed scenic photograph with an unobtrusive clock and translucent favorites dock. It should work with the Fire TV remote and avoid ads/recommendations *inside our app*. It is not intended to remove advertisements from streaming apps or uninstall Amazon's system home.

User-approved first-version direction: bundled scenic imagery rotating daily; favorites selectable and reorderable by remote; Live TV, Inputs and Settings shortcuts; manual-launch test before an optional Home/wake redirect; an obvious route back to Amazon Home.

## Approaches considered

1. **Native Android TV/Fire OS app (selected).** Fast remote focus/navigation, local settings, installed-app discovery, small memory footprint, and a clear platform-specific boundary. Requires a separate APK and device testing.
2. WebView wrapper around a CleanFrame-like UI. Faster visual iteration but more fragile remote focus and app/system-intent integration on a constrained TV.
3. Off-the-shelf launcher with wallpaper customization. Fastest to install, but less control over the CleanFrame-inspired layout and behavior.

## Visual and interaction design

- One full-screen scenic image, darkened subtly behind text; large but restrained 12-hour clock in an upper corner; small date below. Bundle eight original photo-style scenic 1920×1080 images in compressed format, aiming for under 15 MB total. Change automatically once per local day; let the user choose/freeze an image in Settings. No network dependency for v1.
- A translucent dock near the bottom with installed-app icons and labels, visible focus outline/scale for the selected item, and no autoplay or promoted content. Up to eight pinned favorites appear in a horizontal row with an overflow/manage action. The dock also exposes Live TV, Inputs, Settings and an Amazon Home escape action (subject to device validation for the system shortcuts).
- Remote controls: arrows move focus predictably; Select opens; Back returns to the home view; long-press or Menu opens favorite management where possible. Preferences, favorite ordering, and wallpaper selection are stored locally.
- Never strand the user: the Amazon Home escape remains visible in Settings and a remote-accessible shortcut. If a system shortcut is unavailable, show an informative message and point to the original Fire TV interface rather than failing silently.

## Components and boundaries

- **ScenicHomeActivity:** renders the background, clock and dock using native Android views. Avoid Google Play Services and heavyweight dependencies; Fire OS 7 is based on Android 9.[2] The app requests no network permission and stores no telemetry.
- **AppCatalog:** queries apps with TV/standard launch intents, filters unavailable entries, and launches selected packages. Verify actual installed-app visibility and icon behavior on the TV.
- **FavoritesStore:** local persistence for pins/order and selected wallpaper; defaults are recoverable.
- **SystemActions:** isolated handlers for Live TV, Inputs, Settings and Amazon Home. Availability and exported intents differ by TV model/firmware; validate each action on this Insignia before treating it as complete. Keep the physical remote's Inputs/Live TV keys as fallback.
- **WallpaperStore:** eight bundled original scenic assets, with no external image service in v1.
- **Optional Home bridge:** use a separate, user-controlled Fire OS 7-compatible redirect such as Home on Fire only after manual installation and recovery have been tested. It redirects Home; it does not replace Amazon's protected system launcher. Fire OS 7 may display a short transition/loading screen. It uses Accessibility and may require a one-time ADB permission grant if the TV's Accessibility settings cannot enable it directly.[4]

## Staged delivery and verification

1. Build the UI and remote-navigation tests locally with an Android 9/API 28 emulator or equivalent test harness; preview screenshots and a signed or installable debug APK. Keep asset provenance documented. Verify clock, wallpaper rotation, every focus path, favorites editing, app launch errors and persistence.
2. Confirm the TV's developer/sideloading access with the user. Amazon documents ADB setup for Fire TV, but ADB over network must be probed rather than assumed on this firmware; if unavailable, use a supported on-device/USB installation route or stop.[3] Do not change system packages or disable the stock launcher.
3. Install and manually open the APK on the TV; have the user verify remote focus, image performance, app launches, Live TV, Inputs, Settings and Amazon Home escape. Record which system actions work before promising them in v1.
4. Only if the user wants it and the manual version works, enable the optional Home/boot/wake redirect. Test from a streaming app, cold boot and standby wake, and preserve a reversible path: disable the redirect/Accessibility service or uninstall it to return to Amazon Home. Do not attempt root or aggressive debloat.

## Risks and explicit limits

- CleanFrame is Tizen source, not a Fire OS installable package; use its design language as reference, not its Samsung setup scripts or assets by default.[1]
- Home on Fire's author reports support for Fire OS 7.7.1.5, but this specific Insignia TV and its ADB/Accessibility behavior have not been tested. A manual custom home app is useful even if automatic Home redirection fails.[4]
- An ad-free custom screen hides Amazon's ad-heavy home during normal use; it does not remove Amazon's launcher, system updates, in-app ads, or all bloatware.
- Available storage in the supplied screenshot was about 1.46 GB at capture; keep wallpaper assets and APK modest. Do not put the user's serial number or account details in source, logs, docs or screenshots.

## Open implementation decisions

- Use "Scenic Home" as the working app name and a simple landscape icon; these are editable without changing navigation.
- Exact installed-app and system-intent behavior must be discovered on the device. If a dedicated Inputs action cannot be invoked reliably, retain a labeled fallback to Fire TV's own controls rather than claim a working direct shortcut.
- Keep the design, implementation plan and application source in this FireTVCleanUI repository. Pushing documentation here does not imply an APK has been built or tested. Do not modify the TV until the user approves device steps.

## Sources

[1] https://github.com/nthnclrk/CleanFrame — CleanFrame reference project
[2] https://developer.amazon.com/docs/fire-tv/fire-os-7.html — Fire OS 7 development notes
[3] https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html — Amazon Fire TV ADB setup
[4] https://github.com/toolicious/home-on-fire — Home on Fire compatibility and limitations
