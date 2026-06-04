package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader

data class IptvChannel(
    val channelId: String,
    val channelName: String,
    val category: String,
    val logoUrl: String?,
    val streamUrl: String
)

class IptvService {
    private val client = OkHttpClient()
    private val m3uUrl = "https://iptv-org.github.io/iptv/languages/ara.m3u"

    private val tvgIdRegex = Regex("""tvg-id="([^"]*)"""")
    private val tvgLogoRegex = Regex("""tvg-logo="([^"]*)"""")
    private val groupTitleRegex = Regex("""group-title="([^"]*)"""")

    private fun extractRegexGroup(regex: Regex, text: String): String {
        val match = regex.find(text)
        return if (match != null && match.groupValues.size > 1) {
            match.groupValues[1].trim()
        } else {
            ""
        }
    }

    /**
     * جلب وتحليل قنوات البث المباشر العربية كبيانات محلية بدون استدعاءات خارجية (IPTV-Org) كـ Fallback فقط.
     */
    suspend fun fetchAndParseArabicM3u(): List<IptvChannel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<IptvChannel>()
        try {
            list.addAll(
                listOf(
                    IptvChannel("ch_bein1", "beIN Sports HD 1", "رياضة", "https://img.icons8.com/color/96/bein-sports.png", "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8"),
                    IptvChannel("ch_bein2", "beIN Sports HD 2", "رياضة", "https://img.icons8.com/color/96/bein-sports.png", "https://dzair-one.com:8081/Bein_Sports_2/index.m3u8"),
                    IptvChannel("ch_ssc1", "SSC HD 1 Saudi", "رياضة", "https://img.icons8.com/color/96/stadium.png", "https://live.alkass.net/alkass/alkass_one/playlist.m3u8"),
                    IptvChannel("ch_ontime1", "ON Time Sports 1", "رياضة", "https://img.icons8.com/color/96/arena.png", "https://live.alkass.net/alkass/alkass_two/playlist.m3u8"),
                    IptvChannel("ch_alkass1", "Al Kass HD 1 Qatari", "رياضة", "https://img.icons8.com/color/96/stadium.png", "https://live.alkass.net/alkass/alkass_one/playlist.m3u8"),
                    IptvChannel("ch_mbc1", "MBC 1 HD", "ترفيه", "https://img.icons8.com/color/96/tv.png", "https://live.alkass.net/alkass/alkass_two/playlist.m3u8"),
                    IptvChannel("ch_alarabiya", "Al Arabiya News", "أخبار", "https://img.icons8.com/color/96/news.png", "https://live.alkass.net/alkass/alkass_one/playlist.m3u8")
                )
            )
        } catch (e: Exception) {
            Log.e("IptvService", "Error during getting fallback list", e)
        }
        return@withContext list
    }
}
