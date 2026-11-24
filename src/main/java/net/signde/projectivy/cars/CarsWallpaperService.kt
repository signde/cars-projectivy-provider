package net.signde.projectivy.cars

import android.app.Service
import android.content.Intent
import android.os.IBinder
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper

class CarsWallpaperService : Service() {
    private val featured by lazy { FeaturedAuctions(this) }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> =
            if (event is Event.TimeElapsed) featured.load() else emptyList()

        override fun getPreferences() = "{}"
        override fun setPreferences(params: String?) {}
    }

    override fun onBind(intent: Intent): IBinder = binder
}
