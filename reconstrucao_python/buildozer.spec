[app]
title = Independent TV
package.name = independenttv
package.domain = br.com.independenttv
source.dir = .
source.include_exts = py,kv,png,jpg,atlas,json,txt
version = 0.1.0
requirements = python3,kivy==2.3.1,requests
orientation = landscape
fullscreen = 1
android.permissions = INTERNET,ACCESS_NETWORK_STATE,WAKE_LOCK
android.api = 35
android.minapi = 24
android.archs = arm64-v8a, armeabi-v7a
android.accept_sdk_license = True
p4a.bootstrap = sdl2


android.add_src = android_src
android.add_activities = br.com.independenttv.player.PlayerActivity
android.enable_androidx = True
android.gradle_dependencies = androidx.media3:media3-exoplayer:1.11.1,androidx.media3:media3-exoplayer-hls:1.11.1,androidx.media3:media3-exoplayer-dash:1.11.1,androidx.media3:media3-ui:1.11.1

[buildozer]
log_level = 2
warn_on_root = 1
