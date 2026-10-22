# Scenic Home Home-button and boot/wake redirect implementation plan

> **For Hermes:** Use the subagent-driven-development skill for independent documentation/review tasks; keep live TV actions with the parent agent and the user. Do not delegate permission grants or physical-button verification.

**Goal:** Configure the user's Insignia Fire OS 7 TV so Home and boot/wake open Scenic Home, preserving an escape to Amazon Home and a reversible rollback.

**Architecture:** Keep Scenic Home and Amazon Home installed. Use the separately published Home on Fire Accessibility helper to launch Scenic Home when Amazon Home appears, then opt into boot/wake after the Home and escape tests succeed. No Scenic Home code changes or system package modifications are planned.

**Tech stack:** Fire OS 7.7.1.5, local Android SDK Platform-Tools (`adb`), Home on Fire release APK, Fire TV Accessibility settings, repository Markdown documentation.

**Approved design:** `docs/superpowers/specs/2026-10-22-home-button-boot-redirect-design.md`. The user approved the written spec. Device behavior remains unverified.

---

### Task 1: Recheck the helper artifact and local baseline (read-only)

**Objective:** Confirm that the planned helper is the current authentic release and that the Scenic Home package still exists locally.

**Files:** None; inspect `app/build/outputs/apk/debug/app-debug.apk` and the official Home on Fire release metadata.

1. Re-read the maintainer's README and GitHub release API at `https://api.github.com/repos/toolicious/home-on-fire/releases/latest`; if the latest release changed from v1.5.2, stop and inspect the new release and its permissions rather than silently using an older pinned plan.
2. Download only the release asset from the maintainer's `toolicious/home-on-fire` GitHub release into the machine's scratch directory. Compare its `sha256sum` against the release API `assets[].digest`. Inspect `aapt dump badging`, `aapt dump permissions`, and `unzip -t` without executing any helper scripts.
3. Expected for the previously checked v1.5.2: package `io.github.toolicious.homeonfire`, SHA-256 `87403c961264cc2e36e53cf571777ec20705eb87f46794cb0ac667135a98d9fe`, minimum API 21, no INTERNET permission; recompute these before installation. If any check differs, stop and investigate.
4. Check `git status`, the local Scenic Home APK, and `adb devices -l`; an emulator may be connected, so every later TV action must use an explicit `adb -s TV_IP:5555` selector.

### Task 2: Document the opt-in setup and rollback (repository write)

**Objective:** Make the separate-helper approach understandable without promising untested TV behavior.

**Files:** Create `docs/home-on-fire-setup.md`; modify `README.md` sections “Remote controls” and “Manual installation and recovery”.

1. Write a concise setup guide: original release location, digest verification with the **fresh** release API, Accessibility access warning, ADB connection and authorization, explicit `-s` target in installation commands, on-TV target selection, Home-first / escape-second / boot-last tests, and reversible rollback. Use placeholders such as `TV_IP`, never a real local IP or a screenshot with identifying details.
2. Update `README.md` to say the user has manually launched Scenic Home on their Insignia TV, but Home interception/boot/wake are provided only by an **optional external helper** and remain unverified until tested. Correct the old emulator-only installation language. Link the new guide; do not claim Scenic Home itself intercepts Home.
3. Inspect `git diff --check` and read both documents for contradictions, permissions overstated as mandatory, and claims of device success. Commit only the documentation; push and read back `origin/main` if the user still wants the repo as the delivery location.

### Task 3: Establish an authorized TV connection and install only the helper

**Objective:** Place the verified APK on the correct TV without changing launcher/system packages.

**Files:** None on the TV other than the installed helper APK; no Git secrets or IPs.

1. Ask the user for the TV's local IP through a suitable channel or have them run the commands themselves; confirm ADB Debugging is enabled and the TV and host share a LAN. Do not scan arbitrary network devices. Reconfirm they are ready to install a separate Accessibility helper.
2. `adb connect TV_IP:5555`; the user accepts the authorization prompt on the TV. Verify `adb -s TV_IP:5555 shell getprop ro.build.version.sdk` and build/firmware properties before any write. Stop if the selected target is not the intended Fire TV.
3. `adb -s TV_IP:5555 install -r /absolute/path/to/verified/home-on-fire.apk`. Read back the **same TV** with `adb -s TV_IP:5555 shell pm path io.github.toolicious.homeonfire` and package/version details. Do not install or disable any other package.
4. Have the user open Home on Fire, select Scenic Home as its target, and enable its Accessibility service through Fire TV Settings. Do not grant `WRITE_SECURE_SETTINGS` routinely. If the TV UI cannot enable the service, **pause and ask explicitly for permission** before considering the one-time `adb -s TV_IP:5555 shell pm grant io.github.toolicious.homeonfire android.permission.WRITE_SECURE_SETTINGS` command. Verify any grant by reading back package permission state.

### Task 4: Verify Home and stock-Home escape before boot/wake

**Objective:** Prove the redirection is usable and recoverable on the user's TV.

**Files:** None.

1. Leave the helper's boot/wake option OFF. User opens a different app and presses the physical Home button once; observe whether Scenic Home appears. Fire OS 7 can take about five seconds; record the actual delay and any loading screen.
2. User long-presses Home and confirms **Amazon Home** appears. If this does not work or an unexpected loop occurs, do not proceed to boot/wake: use the helper UI or Fire TV Accessibility settings to disable the helper. If those are inaccessible, uninstall **only** `io.github.toolicious.homeonfire` from authorized ADB/TV settings, then verify stock Home behavior.
3. Do not use Scenic Home's “Amazon Home” tile as the only escape; it may be redirected back. Have the user explicitly report both the Home and escape observations before Task 5.

### Task 5: Enable and verify boot/wake (conditional)

**Objective:** Enable automatic startup only after a working Home redirect and escape have been observed.

**Files:** None.

1. If Task 4 passes, user enables Home on Fire's **Launch on boot** option. Perform a full restart, confirm Scenic Home and the Home escape; then perform a separate standby/wake, confirm the resulting screen and any Amazon Home flash/delay.
2. If either path fails, disable Launch on boot, leave the working Home redirect only if the user wants it, and record the limitation. Do not claim startup works on the actual Insignia without both observations.
3. After final state-changing settings operations, read back the exact helper configuration in its UI and re-test the physical Home/escape path. No root, replacement launcher registration, or Amazon package disabling.

### Task 6: Document observed behavior and finish

**Objective:** Leave a truthful, reproducible support record without publishing private TV details.

**Files:** Modify `docs/home-on-fire-setup.md` and/or `README.md` only to reflect verified device-specific behavior; avoid storing IP/serial/account information.

1. Record what the user actually observed: Home redirect, approximate delay, long-press escape, cold boot, standby wake, and rollback check. Clearly label any untested item rather than treating it as success.
2. Run `git diff --check`; commit/push any documentation correction and verify the exact files on `origin/main`. If there is no change, do not make an empty commit.
3. Report only verified behavior and any remaining limitations. If TV connectivity or permission consent is unavailable, stop at the preparation/doc stage and give the user the next concrete step.

## Safety and completion gates

- Installation and Accessibility enablement happen only after plan approval/execution choice; `WRITE_SECURE_SETTINGS` needs separate explicit consent if the standard UI fails.
- Home redirect **and** stock-Home escape must pass before Launch on boot is enabled.
- Boot **and** wake must each pass on the actual Insignia before declaring the full goal complete.
- Preserve Amazon Home and never disable or uninstall Amazon packages.
- A successful `adb install` alone is not a verified working redirect; read back the package and obtain physical-button observations from the user.
