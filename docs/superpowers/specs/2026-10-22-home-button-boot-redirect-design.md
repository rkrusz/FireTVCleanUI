# Scenic Home boot/wake and Home-button redirect — Design

## Intent and acceptance

The user wants the existing Scenic Home screen to appear after pressing the Fire TV remote's Home button and after boot/wake, while retaining a reliable way to reach Amazon Home. The existing APK already launches manually on the user's Insignia Fire OS 7.7.1.5 TV; actual redirect behavior is not yet tested there. This is an opt-in device configuration, not a system-launcher replacement or removal of Amazon software. Amazon documents Home as a system event that ordinary apps cannot intercept.[3]

Success on the **actual TV** means: pressing Home from another app opens Scenic Home; a cold boot and a standby wake lead to Scenic Home after enabling that option; long-press Home reaches Amazon Home; and turning the helper off restores stock behavior. Test these separately, because emulator success cannot establish Fire TV firmware behavior.

## Approaches and decision

1. **Selected: separate Home on Fire helper.** It watches for Amazon Home becoming foreground through an Accessibility service, then opens the selected launcher. It has optional boot/wake launch and a long-press Home escape; its maintainer reports Fire OS 7.7.1.4–7.7.1.5 compatibility. This is a redirect, not an OS-level launcher replacement.[1]
2. Build equivalent Accessibility redirection into Scenic Home. This retains all source in one repository but needs new sensitive permissions, more maintenance, and testing of code that Home on Fire already implements. Not selected.
3. Root, disable Amazon's launcher, or rely on older launcher-hijack exploits. Not selected because this needlessly risks the TV and its recovery route.

The user selected option 1 and approved the staged Home-first, boot/wake-second approach. No changes to Scenic Home source are needed for this design; document the external helper and revise only if actual-device testing shows an integration defect.

## Artifact and trust boundary

Use the maintainer's published **Home on Fire v1.5.2** APK, package `io.github.toolicious.homeonfire`, not an unrelated mirror.[2] The GitHub release API reports SHA-256 `87403c961264cc2e36e53cf571777ec20705eb87f46794cb0ac667135a98d9fe`; the downloaded release APK was checked locally against that digest, passed ZIP integrity inspection, declares min API 21, and has no INTERNET permission. It declares Accessibility binding, boot-receive, and secure-settings permissions.[1][5]

Accessibility access is powerful even without network permission. The user must intentionally enable the service on the TV. If Fire TV's Settings → Accessibility page works, use that to enable Home on Fire and **do not grant** `WRITE_SECURE_SETTINGS`. Only if the settings page cannot enable it, ask the user before issuing the one-time ADB secure-settings grant that lets the helper toggle its service.[1]

Do not fetch or execute the helper's build scripts; use its release APK as a reviewed external artifact. Do not copy its code into the Scenic Home repository. Leave the stock launcher and Scenic Home installed and leave the physical Home escape documented.

## Setup and control flow

1. Confirm the TV still runs Fire OS 7.7.1.5 and Scenic Home still opens manually. Confirm a supported installation path and user control of Accessibility settings. Amazon's ADB guide documents enabling debugging, identifying the TV's IP under My Fire TV → About → Network, connecting over the same LAN, and accepting the on-screen authorization prompt.[4]
2. Install only the checksum-verified helper APK. Open it, set **Scenic Home** as the target app, and enable Home on Fire's Accessibility service through the TV settings if available. If ADB is required for that service toggle, seek explicit permission for the specific `WRITE_SECURE_SETTINGS` grant before executing it.[1]
3. Enable Home-button redirection but **not** boot/wake yet. From a different streaming app, press Home once and observe Scenic Home. Expect a possible loading animation or delay of up to about five seconds on Fire OS 7. Test long-press Home as the escape to Amazon Home. If Home is already on Amazon's launcher, the helper's redirect may not fire again; follow its documented Back/OK route or open Scenic Home manually.[1]
4. Only after Home and escape pass, enable Home on Fire's **Launch on boot** option. Test a full restart and a separate standby/wake cycle. Record whether the TV resumes directly, shows Amazon Home briefly, or delays. Do not claim reliable wake behavior until both tests pass on this Insignia.[1]
5. Keep a rollback path: disable Home on Fire's redirect toggle or Accessibility service; if its UI is inaccessible, uninstall only `io.github.toolicious.homeonfire` via Fire TV Settings or authorized ADB. Do **not** remove Scenic Home or disable any Amazon package as part of rollback. Its settings may be lost on uninstall.

## Constraints and device-specific uncertainties

- The normal Scenic Home app's **Amazon Home** tile may be immediately redirected back to Scenic Home when the helper is active. The reliable escape is Home on Fire's configured long-press Home behavior, not that tile. Confirm the escape on the TV before enabling boot/wake.[1]
- Fire OS 7 can impose an app-switch delay after Home. The helper covers this with a loading animation; the delay is not proof of failure and should not be 'fixed' by root or system changes.[1]
- The TV's Accessibility page, the helper's Home detection, boot/wake behavior, and the exact escape interaction on this Insignia are **not yet verified**. Stop and restore stock behavior if the Home escape fails or an unexpected loop occurs.
- No TV IP address, serial number, account details, or ADB authorization data belong in Git or screenshots. No silent update or external network service is added to Scenic Home.

## Sources

[1] https://github.com/toolicious/home-on-fire — Home on Fire README
[2] https://github.com/toolicious/home-on-fire/releases/tag/v1.5.2 — Home on Fire v1.5.2 release
[3] https://developer.amazon.com/docs/fire-tv/remote-input.html — Amazon Fire TV remote input
[4] https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html — Amazon ADB connection instructions
[5] https://api.github.com/repos/toolicious/home-on-fire/releases/latest — GitHub API latest Home on Fire release metadata
