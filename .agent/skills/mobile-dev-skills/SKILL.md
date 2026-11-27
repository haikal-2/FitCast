---
name: mobile-dev-skills
description: Production-grade Android & cross-platform mobile development skills, agents, and prompts from SalmanMudah/mobile-dev-skills. Covers Jetpack Compose screen building, Compose recomposition/UI review, Android Kotlin code review, crash/ANR root-cause analysis, OWASP Mobile Top 10 security scanning, and WCAG 2.2 mobile accessibility auditing.
---

# Mobile Dev Skills (Android & Cross-Platform)

Source: `https://github.com/SalmanMudah/mobile-dev-skills`

Use this skill index to activate focused Android and cross-platform mobile engineering workflows for **FitCast (`com.fitcast.cuacagaya`)**.

## Included Modules

### Android Skills
- `skills/android/code-review.md` — Coroutine safety (`viewModelScope`, exception handling), Kotlin idioms (`StateFlow` + `collectAsStateWithLifecycle`, sealed states, null safety), Dependency Injection, Clean Architecture boundaries, and Compose side-effect hygiene.

### Android Agents
- `agents/android/compose-screen-builder/agent.md` — End-to-end Jetpack Compose + Material 3 screen generation (sealed `UiState`, `StateFlow` ViewModel, loading/empty/error states, `@Preview` light/dark, 48dp touch targets).
- `agents/android/compose-ui-reviewer/agent.md` — Deep Compose internals review: recomposition scope, `remember` / `derivedStateOf`, `LazyColumn`/`LazyGrid` `key` & `contentType`, side effects (`LaunchedEffect`, `DisposableEffect`), state hoisting, and slot APIs.
- `agents/android/android-crash-analyzer/agent.md` — Structured 9-section root-cause analysis for Android logcat stack traces, ANR dumps, Crashlytics events, coroutine crashes, OOM/bitmap memory issues, and LeakCanary reports.

### Shared Mobile Quality Gates
- `skills/shared/accessibility-audit.md` — WCAG 2.2 mobile accessibility checks (`contentDescription`, 48x48dp touch targets, TalkBack reading order, dynamic announcements, theme contrast).
- `skills/shared/security-scan.md` — OWASP Mobile Top 10 checks (no hardcoded API keys/secrets, secure storage, deep link validation, TLS/WebView safety, minimal runtime permissions).
- `skills/shared/crash-analysis.md` — Fast 5-step crash log & stack trace triage protocol.

### Android Prompts
- `prompts/android/compose-layout.md` — Material 3 responsive Compose component & layout generation template with Light/Dark `@Preview` and semantics.

## Activation Guidance
1. When building or refactoring Compose screens/components: load `agents/android/compose-screen-builder/agent.md` and `prompts/android/compose-layout.md`.
2. When reviewing Compose or Kotlin code: load `skills/android/code-review.md` and `agents/android/compose-ui-reviewer/agent.md`.
3. When debugging crashes, ANRs, or OOM/bitmap issues: load `skills/shared/crash-analysis.md` and `agents/android/android-crash-analyzer/agent.md`.
4. Before releasing or finalizing UI/security-sensitive flows: run `skills/shared/accessibility-audit.md` and `skills/shared/security-scan.md`.
