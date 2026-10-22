# Optional Home on Fire setup (Fire TV)

Scenic Home opens manually on the user's Insignia Fire TV. Its Home-button redirect and boot/wake behavior have **not** been tested on that TV. Scenic Home itself cannot intercept the system Home key. [Home on Fire](https://github.com/toolicious/home-on-fire) is a **separate, optional Accessibility helper** that redirects from Amazon Home to a selected app; it does not replace or remove Amazon Home. Leave both launchers installed. If the helper does not work on your firmware, Scenic Home remains usable by opening it manually.

## Get and verify the helper

Use the maintainer's [v1.5.2 release](https://github.com/toolicious/home-on-fire/releases/tag/v1.5.2), not a mirror. The official asset is `home-on-fire.apk` (`io.github.toolicious.homeonfire`). At the time this guide was written, the [latest-release API](https://api.github.com/repos/toolicious/home-on-fire/releases/latest) reported v1.5.2 and asset SHA-256 `87403c961264cc2e36e53cf571777ec20705eb87f46794cb0ac667135a98d9fe`. **Before installing**, recheck the current release tag and its asset `digest` in that API, download the corresponding official release asset, and run `sha256sum home-on-fire.apk`. Stop if the reported digest and local checksum do not match; if the release changed, review the new version and its permissions rather than applying this old checksum to it. A matching digest confirms the download's identity, not that third-party software is safe.

**Accessibility warning:** this service can observe screen changes and interact with the TV. Enable it only if you trust the helper and accept that access. Prefer the TV's Settings → Accessibility → Home on Fire switch. `WRITE_SECURE_SETTINGS` is **not** a routine prerequisite: if the standard Accessibility UI fails, stop and request explicit permission before considering an ADB grant that allows the helper to toggle its service. Do not grant it preemptively.

## Preflight on the TV

Before setting up the helper, confirm the TV still runs **Fire OS 7.7.1.5**, **Scenic Home** still opens manually, and you can access both a supported installation path (on-device or authorized ADB sideloading) and the TV's **Settings → Accessibility** controls. If any check fails, stop and resolve it before installing or enabling the helper.

## Connect and configure (only if you choose to)

If sideloading by ADB, enable ADB debugging on the TV, find its address under Settings → My Fire TV → About → Network, and connect from a computer on the same LAN. Accept the authorization prompt on the **intended TV** and verify that `TV_IP:5555` is the intended device before installation. Replace `TV_IP` locally; never commit an actual IP, device serial, account detail or authorization screenshot. If you installed through the TV's supported on-device path instead, you do not need ADB for the standard Accessibility path.

```bash
adb connect TV_IP:5555
adb devices
adb -s TV_IP:5555 install -r home-on-fire.apk
adb -s TV_IP:5555 shell pm path io.github.toolicious.homeonfire
```

All TV-targeted ADB actions after connecting must use `-s TV_IP:5555`, especially if an emulator or another device is attached. The `pm path` result checks that the helper installed on the selected TV; it does not prove redirection works. Open Home on Fire on the TV, choose **Scenic Home** as the target app, and enable Home on Fire's Accessibility service in the TV's settings. If that page cannot enable the service, **pause and ask for permission** before any `WRITE_SECURE_SETTINGS` grant; the maintainer documents that fallback, but this guide intentionally does not treat it as automatic.

## Test in stages; keep an escape

1. Leave **Launch on boot** off. Enable the helper's Home redirect. From a different app, press Home once and check whether Scenic Home opens. Fire OS 7 may delay the switch by about five seconds. If Amazon Home is already foreground, Home may not trigger a new redirect; use the helper's documented Back/OK route or manually open Scenic Home instead.
2. **Before boot/wake**, ensure the helper's long-press Home escape is enabled, then long-press Home and confirm that **Amazon Home** is reachable. It may briefly show Quick Settings; Back dismisses that. Scenic Home's Amazon Home tile may be redirected back, so do not rely on that tile as the only escape. If the escape fails or a loop occurs, disable the helper and stop.
3. Only after **both** the Home redirect and long-press escape work on your TV, enable the helper's **Launch on boot** option. Test a full restart and a separate standby/wake. Note any delay or brief Amazon Home screen. Do not assume that success on one path proves the other.

**Rollback:** turn off the redirect in Home on Fire or disable its Accessibility service in TV Settings. If the helper UI is inaccessible, uninstall **only** Home on Fire (`io.github.toolicious.homeonfire`) using Fire TV Settings or, from an authorized connection, `adb -s TV_IP:5555 uninstall io.github.toolicious.homeonfire`. Uninstalling the helper may lose your Home on Fire settings. Confirm the stock Home button works again. Do not uninstall Scenic Home or disable/uninstall Amazon packages.

References: [maintainer setup and controls](https://github.com/toolicious/home-on-fire#installation); [Amazon's ADB connection guide](https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html).
