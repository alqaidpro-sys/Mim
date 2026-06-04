package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object DataSeeder {
    private const val TAG = "DataSeeder"
    private val db get() = FirebaseFirestore.getInstance()

    fun seedIfNeeded(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SyncCheck", "=== Firestore Connection ===")
                Log.d("SyncCheck", "Project: mimpro-3b84d")

                // 1. تحقق من القنوات (channels)
                val channelsSnapshot = db.collection("channels").limit(1).get().await()
                if (channelsSnapshot.isEmpty) {
                    Log.d(TAG, "جاري استيراد القنوات الافتراضية...")
                    seedDefaultChannels()
                } else {
                    Log.d(TAG, "القنوات موجودة مسبقاً")
                }

                // 2. تحقق من المباريات (matches)
                val matchesSnapshot = db.collection("matches").limit(1).get().await()
                if (matchesSnapshot.isEmpty) {
                    Log.d(TAG, "جاري استيراد المباريات الافتراضية...")
                    seedDefaultMatches()
                } else {
                    Log.d(TAG, "المباريات موجودة مسبقاً")
                }

                // 3. تحقق من المسلسلات (series)
                val seriesSnapshot = db.collection("series").limit(1).get().await()
                if (seriesSnapshot.isEmpty) {
                    Log.d(TAG, "جاري استيراد المسلسلات الافتراضية...")
                    seedDefaultSeries()
                } else {
                    Log.d(TAG, "المسلسلات موجودة مسبقاً")
                }

                // 4. تحقق من البنرات (banners)
                val bannersSnapshot = db.collection("banners").limit(1).get().await()
                if (bannersSnapshot.isEmpty) {
                    Log.d(TAG, "جاري استيراد البنرات الافتراضية...")
                    seedDefaultBanners()
                } else {
                    Log.d(TAG, "البنرات موجودة مسبقاً")
                }

                // 5. تحقق من الإعدادات (settings/appSettings)
                val settingsDoc = db.collection("settings").document("appSettings").get().await()
                if (!settingsDoc.exists()) {
                    Log.d(TAG, "جاري إعداد مستند الإعدادات...")
                    seedDefaultSettings()
                } else {
                    Log.d(TAG, "مستند الإعدادات موجود مسبقاً")
                }

                Log.d(TAG, "تمت عملية فحص وهيكلة قاعدة البيانات بنجاح ✓")
            } catch (e: Exception) {
                Log.e(TAG, "خطأ أثناء محاولة هيكلة البيانات: ${e.message}", e)
            }
        }
    }

    private suspend fun seedDefaultChannels() {
        Log.d("SyncCheck", "جاري تهيئة قنوات البث في Firestore...")
        val channels = listOf(
            hashMapOf(
                "id" to "ch_bein1",
                "name" to "beIN Sports HD 1",
                "cat" to "رياضة",
                "country" to "عربي",
                "logo" to "https://img.icons8.com/color/96/bein-sports.png",
                "streamUrl" to "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8",
                "active" to true,
                "featured" to true
            ),
            hashMapOf(
                "id" to "ch_bein2",
                "name" to "beIN Sports HD 2",
                "cat" to "رياضة",
                "country" to "عربي",
                "logo" to "https://img.icons8.com/color/96/bein-sports.png",
                "streamUrl" to "https://dzair-one.com:8081/Bein_Sports_2/index.m3u8",
                "active" to true,
                "featured" to false
            ),
            hashMapOf(
                "id" to "ch_ssc1",
                "name" to "SSC HD 1 Saudi",
                "cat" to "رياضة",
                "country" to "السعودية",
                "logo" to "https://img.icons8.com/color/96/stadium.png",
                "streamUrl" to "https://live.alkass.net/alkass/alkass_one/playlist.m3u8",
                "active" to true,
                "featured" to true
            ),
            hashMapOf(
                "id" to "ch_ontime1",
                "name" to "ON Time Sports 1",
                "cat" to "رياضة",
                "country" to "مصر",
                "logo" to "https://img.icons8.com/color/96/arena.png",
                "streamUrl" to "https://live.alkass.net/alkass/alkass_two/playlist.m3u8",
                "active" to true,
                "featured" to false
            ),
            hashMapOf(
                "id" to "ch_alkass1",
                "name" to "Al Kass HD 1 Qatari",
                "cat" to "رياضة",
                "country" to "قطر",
                "logo" to "https://img.icons8.com/color/96/stadium.png",
                "streamUrl" to "https://live.alkass.net/alkass/alkass_one/playlist.m3u8",
                "active" to true,
                "featured" to false
            )
        )
        for (ch in channels) {
            db.collection("channels").document(ch["id"] as String).set(ch).await()
        }
    }

    private suspend fun seedDefaultMatches() {
        Log.d("SyncCheck", "جاري تهيئة مباريات اليوم في Firestore...")
        val matches = listOf(
            hashMapOf(
                "id" to "m_rm_cc",
                "league" to "دوري أبطال أوروبا",
                "home" to "ريال مدريد",
                "away" to "مانشستر سيتي",
                "score" to "2 - 1",
                "status" to "مباشر",
                "hFlag" to "👑",
                "aFlag" to "🩵",
                "isLive" to true,
                "ch" to "beIN Sports HD 1",
                "homeLogo" to "",
                "awayLogo" to "",
                "elapsed" to 74,
                "round" to "نصف النهائي - إياب",
                "date" to "2026-05-31",
                "time" to "21:00",
                "stadium" to "ملعب سانتياغو برنابيو",
                "referee" to "سيمون مارسينياك",
                "commentator" to "حفيظ دراجي",
                "streamUrlStr" to "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8",
                "links" to listOf(
                    mapOf("name" to "رابط مباشر HD", "url" to "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8")
                ),
                "events" to listOf(
                    mapOf("minute" to "12'", "type" to "goal_scored", "player" to "ريال مدريد", "detail" to "هدف رائع بتسديدة من فينيسيوس ⚽"),
                    mapOf("minute" to "55'", "type" to "goal_scored", "player" to "مانشستر سيتي", "detail" to "هدف التعادل عن طريق هالاند ⚽"),
                    mapOf("minute" to "71'", "type" to "goal_scored", "player" to "ريال مدريد", "detail" to "رأسية بيلينجهام في الشباك ⚽")
                )
            ),
            hashMapOf(
                "id" to "m_ahly_zamalek",
                "league" to "الدوري المصري الممتاز",
                "home" to "الأهلي",
                "away" to "الزمالك",
                "score" to "1 - 0",
                "status" to "مباشر",
                "hFlag" to "🦅",
                "aFlag" to "🏹",
                "isLive" to true,
                "ch" to "ON Time Sports 1",
                "homeLogo" to "",
                "awayLogo" to "",
                "elapsed" to 52,
                "round" to "الأسبوع 28",
                "date" to "2026-05-31",
                "time" to "20:00",
                "stadium" to "ملعب استاد القاهرة الدولي",
                "referee" to "بهاء أبو السعود",
                "commentator" to "مدحت شلبي",
                "streamUrlStr" to "https://live.alkass.net/alkass/alkass_two/playlist.m3u8",
                "links" to listOf(
                    mapOf("name" to "رابط ON Time", "url" to "https://live.alkass.net/alkass/alkass_two/playlist.m3u8")
                ),
                "events" to listOf(
                    mapOf("minute" to "44'", "type" to "goal_scored", "player" to "الأهلي", "detail" to "هدف وسام أبو علي الأول ⚽")
                )
            ),
            hashMapOf(
                "id" to "m_hilal_nassr",
                "league" to "دوري روشن السعودي",
                "home" to "الهلال",
                "away" to "النصر",
                "score" to "0 - 0",
                "status" to "لم تبدأ",
                "hFlag" to "🔵",
                "aFlag" to "🟡",
                "isLive" to false,
                "ch" to "SSC HD 1",
                "homeLogo" to "",
                "awayLogo" to "",
                "elapsed" to 0,
                "round" to "الأسبوع 32",
                "date" to "2026-05-31",
                "time" to "21:00",
                "stadium" to "ملعب مدينة الملك عبد الله الرياضية (الجوهرة)",
                "referee" to "خافيير تيلو",
                "commentator" to "فهد العتيبي",
                "streamUrlStr" to "https://live.alkass.net/alkass/alkass_one/playlist.m3u8",
                "links" to emptyList<Map<String, String>>(),
                "events" to emptyList<Map<String, String>>()
            )
        )
        for (m in matches) {
            db.collection("matches").document(m["id"] as String).set(m).await()
        }
    }

    private suspend fun seedDefaultSeries() {
        Log.d("SyncCheck", "جاري تهيئة المسلسلات في Firestore...")
        val series = listOf(
            hashMapOf(
                "id" to 1,
                "title" to "مغامرات تشان تشاو",
                "subTitle" to "Chan Chao Adventures",
                "ep" to 12,
                "badge" to "مترجم",
                "age" to "+16",
                "genre" to "دراما",
                "year" to 2025,
                "country" to "كوريا",
                "status" to "يعرض الآن",
                "col" to 0xFF0D2E28L,
                "totalEps" to 12,
                "views" to 1863,
                "story" to "قصة دراما كورية مشوقة ومميزة."
            ),
            hashMapOf(
                "id" to 20,
                "title" to "المؤسس عثمان",
                "subTitle" to "Kurulus Osman",
                "ep" to 15,
                "badge" to "مترجم",
                "age" to "+13",
                "genre" to "أكشن",
                "year" to 2025,
                "country" to "تركيا",
                "status" to "يعرض الآن",
                "col" to 0xFF351F10L,
                "totalEps" to 40,
                "views" to 3940,
                "story" to "تدور أحداث المسلسل حول الغازي عثمان بن أرطغرل مؤسس الدولة العثمانية."
            ),
            hashMapOf(
                "id" to 11,
                "title" to "الحشاشين",
                "subTitle" to "The Assassins",
                "ep" to 30,
                "badge" to "أصلي",
                "age" to "+16",
                "genre" to "دراما",
                "year" to 2024,
                "country" to "مصر",
                "status" to "مكتمل",
                "col" to 0xFF103A15L,
                "totalEps" to 30,
                "views" to 4500,
                "story" to "طائفة الحشاشين وقائدها حسن الصباح."
            ),
            hashMapOf(
                "id" to 40,
                "title" to "الدحيح - الموسم الجديد",
                "subTitle" to "El Daheeh",
                "ep" to 4,
                "badge" to "أصلي",
                "age" to "الجميع",
                "genre" to "وثائقي",
                "year" to 2025,
                "country" to "مصر",
                "status" to "يعرض الآن",
                "col" to 0xFF0A2B60L,
                "totalEps" to 24,
                "views" to 5210,
                "story" to "أحمد الغندور يسطر معلومات علمية ممتعة وأفكار استثنائية بطريقة مبسطة."
            )
        )
        for (s in series) {
            db.collection("series").document(s["id"].toString()).set(s).await()
        }
    }

    private suspend fun seedDefaultBanners() {
        Log.d("SyncCheck", "جاري تهيئة البنرات في Firestore...")
        val banners = listOf(
            hashMapOf(
                "id" to "banner_welcome",
                "type" to "ترويجي",
                "icon" to "stadium",
                "title" to "أهلاً بك في MIM PRO 🌟",
                "subtitle" to "تابع أقوى المباريات وأجود قنوات البث المباشر مجاناً بدون انقطاع.",
                "url" to "https://alqaidpro-mimplus.web.app",
                "bgColor" to "#0D2E28",
                "order" to 1,
                "active" to true
            ),
            hashMapOf(
                "id" to "banner_bein",
                "type" to "توجيهي لقناة",
                "icon" to "tv",
                "title" to "قنوات beIN Sports HD ⚽",
                "subtitle" to "البث المباشر بجودات متعددة تضمن لك مشاهدة سلسة وممتعة.",
                "url" to "ch_bein1",
                "bgColor" to "#1a3c34",
                "order" to 2,
                "active" to true
            )
        )
        for (b in banners) {
            db.collection("banners").document(b["id"] as String).set(b).await()
        }
    }

    private suspend fun seedDefaultSettings() {
        Log.d("SyncCheck", "جاري تهيئة الإعدادات في Firestore...")
        val settings = hashMapOf(
            "appName" to "MIM Plus",
            "maintenance" to false,
            "contactEmail" to "alqaidpro@gmail.com",
            "news" to "بث حي للمباريات والقنوات الرياضية بجودة عالية ✓"
        )
        db.collection("settings").document("appSettings").set(settings).await()
    }
}
