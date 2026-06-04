package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SoccerService {
    private val client = OkHttpClient()
    private val feedUrl = "https://www.scorebat.com/video-api/v3/feed/"

    suspend fun fetchTodayMatches(): List<Match> = withContext(Dispatchers.IO) {
        return@withContext getFallbackMatches()
    }

    private fun getTeamEmoji(team: String): String {
        val t = team.lowercase(Locale.US)
        return when {
            t.contains("madrid") -> "👑"
            t.contains("barcelona") -> "🔵"
            t.contains("manchester united") || t.contains("man utd") -> "😈"
            t.contains("manchester city") || t.contains("man city") -> "🩵"
            t.contains("arsenal") -> "🔫"
            t.contains("chelsea") -> "🦁"
            t.contains("liverpool") -> "🔴"
            t.contains("bayern") -> "🇩🇪"
            t.contains("dortmund") -> "🟡"
            t.contains("psg") || t.contains("paris") -> "🗼"
            t.contains("juventus") -> "🦓"
            t.contains("milan") -> "⚫"
            t.contains("inter") -> "🔷"
            t.contains("ahly") -> "🦅"
            t.contains("zamalek") -> "🏹"
            t.contains("hilal") -> "🔵"
            t.contains("nassr") -> "🟡"
            t.contains("ittihad") -> "🐯"
            else -> "⚽"
        }
    }

    private fun translateLeague(league: String): String {
        val l = league.uppercase(Locale.US)
        return when {
            l.contains("PREMIER LEAGUE") || l.contains("ENGLAND") -> "الدوري الإنجليزي الممتاز"
            l.contains("CHAMPIONS LEAGUE") || l.contains("UEFA") -> "دوري أبطال أوروبا"
            l.contains("LALIGA") || l.contains("SPAIN") || l.contains("PRIMERA DIVISION") -> "الدوري الإسباني"
            l.contains("SERIE A") || l.contains("ITALY") -> "الدوري الإيطالي"
            l.contains("BUNDESLIGA") || l.contains("GERMANY") -> "الدوري الألماني"
            l.contains("LIGUE 1") || l.contains("FRANCE") -> "الدوري الفرنسي"
            l.contains("EGYPT") -> "الدوري المصري الممتاز"
            l.contains("SAUDI") || l.contains("PRO LEAGUE") -> "دوري روشن السعودي"
            else -> league
        }
    }

    private fun translateTeam(team: String): String {
        return when (team) {
            "Arsenal" -> "أرسنال"
            "Chelsea" -> "تشيلسي"
            "Liverpool" -> "ليفربول"
            "Manchester United" -> "مانشستر يونايتد"
            "Manchester City" -> "مانشستر سيتي"
            "Tottenham" -> "توتنهام"
            "Real Madrid" -> "ريال مدريد"
            "Barcelona" -> "برشلونة"
            "Atletico Madrid" -> "أتلتيكو مدريد"
            "Bayern Munich" -> "بايرن ميونخ"
            "Borussia Dortmund" -> "بوروسيا دورتموند"
            "PSG" -> "باريس سان جيرمان"
            "Juventus" -> "يوفنتوس"
            "AC Milan" -> "ميلان"
            "Inter" -> "إنتر ميلان"
            "Roma" -> "روما"
            "Napoli" -> "نابولي"
            "Al Ahly" -> "الأهلي"
            "Zamalek" -> "الزمالك"
            "Al Hilal" -> "الهلال"
            "Al Nassr" -> "النصر"
            "Al Ittihad" -> "الاتحاد"
            else -> team
        }
    }

    fun getFallbackMatches(): List<Match> {
        return listOf(
            Match(
                league = "دوري أبطال أوروبا",
                home = "ريال مدريد",
                away = "مانشستر سيتي",
                score = "2 - 1",
                status = "مباشر",
                extra = "الشوط الثاني ⚡",
                hf = "👑",
                af = "🩵",
                isLive = true,
                ch = "beIN Sports HD 1",
                elapsed = 72
            ),
            Match(
                league = "الدوري الإنجليزي الممتاز",
                home = "ليفربول",
                away = "أرسنال",
                score = "0 - 0",
                status = "لم تبدأ",
                extra = "عرض الليلة 18:30",
                hf = "🔴",
                af = "🔫",
                isLive = false,
                ch = "beIN Sports HD 2"
            ),
            Match(
                league = "دوري روشن السعودي",
                home = "الهلال",
                away = "النصر",
                score = "3 - 1",
                status = "انتهت",
                extra = "مكتملة 🔥",
                hf = "🔵",
                af = "🟡",
                isLive = false,
                ch = "SSC HD 1"
            )
        )
    }
}
