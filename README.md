# BaT Cars for Projectivy

**This provider is experimental. Use it at your own risk.** Bring a Trailer has no official public API for featured auctions, so changes to the site can break the provider. If it breaks, you keep both pieces.

A Projectivy wallpaper provider for Android 9 and newer. The provider reads the Featured Auctions carousel on Bring a Trailer’s archive page for the current year and returns one cover per auction. It requests 1920-pixel images without the thumbnail crop. Each wallpaper includes the auction title. The provider refreshes the listing every six hours. If the page is unavailable, it reuses the last successful list, if available. Remote photos need a connection unless Projectivy already cached them. Projectivy controls wallpaper rotation.

## Build and install
With JDK 17, Android SDK Platform 34 and Build Tools 34.0.0 installed, set ANDROID_HOME to the SDK directory and run:

    ./gradlew assembleDebug
    adb -s DEVICE_SERIAL install -r build/outputs/apk/debug/cars-projectivy-provider-debug.apk

Choose **Settings → Appearance → Wallpaper → Launcher wallpaper → BaT Cars** in Projectivy. Use **Show wallpapers** to preview the collection. The APK contains no native code and supports both 32-bit and 64-bit ARM devices.

## Sources
The AIDL and Parcelable definitions in projectivy-api come from [Projectivy's plugin API](https://github.com/spocky/projectivy-plugin-wallpaper-provider/tree/60ec7b4306ab16dac83ea10ed9b81f5670b51ee3/api), under the included Apache 2.0 license.
