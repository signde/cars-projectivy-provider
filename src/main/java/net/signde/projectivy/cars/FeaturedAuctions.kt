package net.signde.projectivy.cars

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.time.Year
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper

/** The featured carousel is server-rendered. One cover image represents each auction. */
internal class FeaturedAuctions(context: Context) {
    private val cache = context.getSharedPreferences("featured_auctions", Context.MODE_PRIVATE)

    @Synchronized
    fun load(): List<Wallpaper> {
        val now = System.currentTimeMillis()
        val previous = readCache()
        if (previous.isNotEmpty() && now - cache.getLong("fetched", 0) in 0 until SIX_HOURS) {
            return previous
        }
        if (now - cache.getLong("attempt", 0) in 0 until RETRY_DELAY) return previous

        cache.edit().putLong("attempt", now).apply()
        return try {
            val page = "https://bringatrailer.com/${Year.now().value}/0/"
            val document = Jsoup.connect(page).userAgent("Cars-Projectivy/0.3 (Android TV)")
                .timeout(7000).maxBodySize(4 * 1024 * 1024).get()
            val cards = document.select(".featured-items-swiper .featured-listing-card")
            val seenAuctions = mutableSetOf<Uri>()
            val listings = cards.mapNotNull { card ->
                val link = card.selectFirst(".thumbnail a.image-overlay[href]") ?: return@mapNotNull null
                val title = link.attr("title").trim().ifEmpty { card.selectFirst("h3")?.text().orEmpty() }
                val image = card.selectFirst(".thumbnail img[src]") ?: return@mapNotNull null
                val photo = Uri.parse(image.absUrl("src"))
                val auction = Uri.parse(link.absUrl("href"))
                if (title.isEmpty() || !isBatUrl(photo, "/wp-content/uploads/") ||
                    !isBatUrl(auction, "/listing/") || !seenAuctions.add(auction)) return@mapNotNull null
                val cover = photo.buildUpon().clearQuery().appendQueryParameter("w", "1920").build()
                wallpaper(cover.toString(), title)
            }
            if (listings.isEmpty()) throw IOException("No featured auction covers on the page")
            val json = JSONArray()
            for (item in listings) {
                json.put(JSONObject().put("title", item.title).put("image", item.uri))
            }
            cache.edit().putString("list", json.toString()).putLong("fetched", now).apply()
            Log.i(TAG, "Fetched ${listings.size} featured auction covers")
            listings
        } catch (error: IOException) {
            Log.w(TAG, "Featured auctions unavailable; using the previous list", error)
            previous
        }
    }

    private fun wallpaper(image: String, title: String) = Wallpaper(image, title = title, source = "Bring a Trailer")

    private fun isBatUrl(uri: Uri, prefix: String): Boolean =
        uri.scheme == "https" && uri.host == "bringatrailer.com" && uri.path?.startsWith(prefix) == true

    private fun readCache(): List<Wallpaper> {
        val stored = cache.getString("list", null) ?: return emptyList()
        return try {
            val json = JSONArray(stored)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                wallpaper(item.getString("image"), item.getString("title"))
            }
        } catch (error: JSONException) {
            emptyList()
        }
    }

    private companion object {
        const val TAG = "CarsFeaturedAuctions"
        const val SIX_HOURS = 6 * 60 * 60 * 1000L
        const val RETRY_DELAY = 15 * 60 * 1000L
    }
}
