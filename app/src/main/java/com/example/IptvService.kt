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
     * جلب وتحليل قنوات البث المباشر العربية من iptv-org بشكل متدفق ومحمي للذاكرة (Line-by-Line BufferedReader).
     */
    suspend fun fetchAndParseArabicM3u(): List<IptvChannel> = withContext(Dispatchers.IO) {
        val list = mutableListOf<IptvChannel>()
        try {
            val request = Request.Builder().url(m3uUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use emptyList<IptvChannel>()
                val body = response.body ?: return@use emptyList<IptvChannel>()
                
                // البث السطري لعدم تحميل كامل الملف في ذاكرة الهاتف وتفادي Crashes
                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                var line: String? = reader.readLine()
                
                var currentId = ""
                var currentName = ""
                var currentCategory = "عام"
                var currentLogo: String? = null
                var hasMetadata = false
                
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("#EXTINF:")) {
                        // استخراج tvg-id بأمان تام
                        val rawId = extractRegexGroup(tvgIdRegex, trimmed)
                        currentId = if (rawId.isNotEmpty()) rawId else "ch_${(100000..999999).random()}"
                        
                        // استخراج tvg-logo بأمان تام
                        val rawLogo = extractRegexGroup(tvgLogoRegex, trimmed)
                        currentLogo = if (rawLogo.isNotEmpty()) rawLogo else null
                        
                        // استخراج group-title بأمان تام
                        val rawCategory = extractRegexGroup(groupTitleRegex, trimmed)
                        currentCategory = if (rawCategory.isNotEmpty()) rawCategory else "قنوات عامة"
                        
                        // استخراج الاسم (يكون دائماً بعد الفاصلة الأخيرة في السطر)
                        val commaIdx = trimmed.lastIndexOf(',')
                        currentName = if (commaIdx != -1) {
                            trimmed.substring(commaIdx + 1).trim()
                        } else {
                            "قناة عربية"
                        }
                        
                        hasMetadata = true
                    } else if ((trimmed.startsWith("http://") || trimmed.startsWith("https://")) && hasMetadata) {
                        // تصفية قنوات HLS فقط (.m3u8) وتجاوز بقية القنوات المتوقفة أو البروتوكولات الأخرى
                        val pathPart = trimmed.substringBefore("?").substringBefore("#").lowercase()
                        val isHls = pathPart.endsWith(".m3u8")
                        
                        if (isHls) {
                            list.add(
                                IptvChannel(
                                    channelId = currentId,
                                    channelName = currentName,
                                    category = currentCategory,
                                    logoUrl = currentLogo,
                                    streamUrl = trimmed
                                )
                            )
                        }
                        hasMetadata = false
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            Log.e("IptvService", "Error during parsing live M3U", e)
        }
        return@withContext list
    }
}
