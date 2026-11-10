# Stream Shelf reconstruction

Independent IPTV client using package `org.independent.streambox`. Implemented flows: local M3U import through Android's document picker; basic Xtream-compatible login and retrieval of live, movie, and series catalogs/categories; name/category search; locally stored favorites and catalog; series episode lookup via `get_series_info`; media handoff to an installed Android video player. The app does not embed the reference app's branding, code, resources, or third-party endpoints.

Target compatibility is Android 10 (API 29); the available legacy `dx` tool needs DEX invoke-custom support and therefore the delivered APK minimum is API 26. The Gradle project is configured for normal SDK builds (`gradle :app:assembleDebug`). In this Termux environment a full Android SDK is absent, so `build-local-termux.sh` compiles against the installed `android.jar` and system framework resources to reproduce the APK locally.

Limitations: no runtime access to an IPTV account/server was available, so Xtream behavior is implementation-level, not validated against a provider. EPG/XMLTV, backup/restore, catch-up, parental controls, local media, casting, audio/subtitle selection and built-in in-app playback are not implemented. Playback opens a compatible installed Android app. M3U type detection is heuristic. Visual fidelity was not established because the reference APK was not executed.

Run JVM parser checks with `javac`/`java` as shown in `tests/run-jvm-tests.sh`. Gradle builds need a standard Android SDK installed; the local APK was packaged with Termux tools because that SDK is not available here.
