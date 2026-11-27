---
name: android-skills
description: Official Android developer skills from github.com/android/skills covering Jetpack Compose performance optimization, adaptive Material 3 layouts, edge-to-edge system bars and IME insets, Android permissions & IPC security, and ANR static audit & trace diagnosis.
---

# Android Official Skills (`android/skills`)

Source: `https://github.com/android/skills`

Use this index to route Android tasks in **FitCast (`com.fitcast.cuacagaya`)** to the official modular Android skills below:

## Installed Sub-Skills
1. **Jetpack Compose Performance** (`jetpack-compose/jetpack-compose-performance/SKILL.md`)
   - Recomposition optimization, phase deferral (`Modifier.graphicsLayer`, `Modifier.offset`), `derivedStateOf` / `computedStateOf`, Strong Skipping Mode stability trade-offs, `LazyColumn`/`LazyVerticalGrid` `key` & `contentType`, lifecycle-aware Flow collection (`collectAsStateWithLifecycle`), Coil `AsyncImage`, and `Modifier.Node`.
2. **Adaptive UI & State Preservation** (`jetpack-compose/adaptive/SKILL.md`)
   - Responsive phone/foldable/tablet layouts, adaptive grids (`GridCells.Adaptive`), `rememberSaveable` / `rememberLazyListState` state preservation across configuration changes, and form-factor `@Preview` annotations.
3. **Edge-to-Edge & IME Insets** (`system/edge-to-edge/SKILL.md`)
   - `enableEdgeToEdge()`, `android:windowSoftInputMode="adjustResize"`, `Scaffold` `innerPadding` + `consumeWindowInsets`, `LazyColumn` `contentPadding`, IME keyboard handling without double padding, and light/dark system bar legibility.
4. **Android Permissions & IPC Security** (`security/android-permissions-security/SKILL.md`)
   - 3-state runtime permission flow (`checkSelfPermission`, `shouldShowRequestPermissionRationale`, `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`), zero-permission Photo Picker (`PickVisualMedia`), sequential foreground/background location requests, stateless permission checks + `SecurityException` guards, and least-privilege manifest components.
5. **ANR Audit & Fix** (`performance/anr-audit-and-fix/SKILL.md`)
   - Static codebase ANR risk audit (detecting main-thread blocking I/O, disk/database reads, or lock contention) and trace-driven ANR diagnosis.

## Usage
Always open and follow the matching sub-skill's `SKILL.md` before modifying Compose screens, runtime permissions (`Location`, `Camera`, `POST_NOTIFICATIONS`), Room/WorkManager background work, or edge-to-edge insets.
