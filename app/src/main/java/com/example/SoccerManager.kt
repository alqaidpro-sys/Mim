package com.example

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

object SoccerManager {
    private const val PREFS_NAME = "soccer_manager_prefs"
    private const val KEY_MATCHES = "saved_matches_json"
    private const val KEY_CHANNELS = "admin_channels_json"
    private const val KEY_SERIES = "saved_series_json"
    private const val KEY_VOTES = "user_votes_json"
    private const val KEY_COMMENTS = "user_comments_json"

    // Custom Channel model that Admin can control
    data class AdminChannel(
        val id: String,
        val name: String,
        val logoUrl: String?,
        val streamUrl: String,
        val category: String = "رياضة"
    )

    // Detailed Event model for the Match Timeline (timeline tab)
    data class MatchEvent(
        val minute: String,
        val type: String, // "goal_scored", "goal_missed", "card_yellow", "card_red", "substitution"
        val player: String,
        val detail: String // e.g. "ركلة جزاء مسجلة", "بطاقة صفراء", "بديل مع Vitinha"
    )

    // Video Moment/Goal Clip (videos tab)
    data class MatchVideo(
        val title: String,
        val description: String,
        val duration: String,
        val tag: String, // "هدف" or "shots"
        val views: String,
        val timeAgo: String
    )

    // Match class featuring robust details
    data class MatchData(
        val id: String,
        val league: String,
        val home: String,
        val away: String,
        val score: String,
        val status: String, // "مباشر", "انتهت", "لم تبدأ"
        val hf: String = "⚽",
        val af: String = "⚽",
        val isLive: Boolean = false,
        val ch: String = "البث المباشر المدمج",
        val homeLogo: String? = null,
        val awayLogo: String? = null,
        val elapsed: Int = 0,
        val round: String = "الدور المجموعات",
        val date: String = "2026-05-31",
        val time: String = "21:00",
        val stadium: String = "ملعب القاهرة الدولي",
        val referee: String = "إبراهيم نور الدين",
        val commentator: String = "عصام الشوالي",
        val hPen: Int = -1,
        val aPen: Int = -1,
        val streamUrlStr: String? = null,
        val events: List<MatchEvent> = emptyList(),
        val videos: List<MatchVideo> = emptyList()
    )

    // Standard pre-defined Egyptian, Saudi, Emirates, Qatari, UCL, WC leagues
    val SUPPORTED_LEAGUES = listOf(
        "الدوري المصري الممتاز",
        "دوري روشن السعودي",
        "دوري أدنوك للمحترفين",
        "دوري نجوم قطر",
        "دوري أبطال أوروبا",
        "الدوري الإنجليزي الممتاز",
        "الدوري الإسباني",
        "الدوري الإيطالي",
        "الدوري الألماني",
        "الدوري الفرنسي",
        "كأس العالم",
        "السوبر الأوروبي"
    )

    val DEFAULT_CHANNELS = listOf(
        AdminChannel("ch_bein1", "beIN Sports HD 1", "https://img.icons8.com/color/96/bein-sports.png", "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8"),
        AdminChannel("ch_bein2", "beIN Sports HD 2", "https://img.icons8.com/color/96/bein-sports.png", "https://dzair-one.com:8081/Bein_Sports_2/index.m3u8"),
        AdminChannel("ch_ssc1", "SSC HD 1 Saudi", "https://img.icons8.com/color/96/stadium.png", "https://live.alkass.net/alkass/alkass_one/playlist.m3u8"),
        AdminChannel("ch_ontime1", "ON Time Sports 1", "https://img.icons8.com/color/96/arena.png", "https://live.alkass.net/alkass/alkass_two/playlist.m3u8"),
        AdminChannel("ch_alkass1", "Al Kass HD 1 Qatari", "https://img.icons8.com/color/96/stadium.png", "https://live.alkass.net/alkass/alkass_one/playlist.m3u8")
    )

    val matchesListState = mutableStateOf<List<MatchData>>(emptyList())
    val adminChannelsState = mutableStateOf<List<AdminChannel>>(emptyList())
    val seriesListState = mutableStateOf<List<Series>>(emptyList())
    val pollVotesState = mutableStateOf<Map<String, Triple<Int, Int, Int>>>(emptyMap())
    val matchCommentsState = mutableStateOf<Map<String, List<Triple<String, String, String>>>>(emptyMap())

    private val matchesList = mutableListOf<MatchData>()
    private val adminChannels = mutableListOf<AdminChannel>()
    private val seriesList = mutableListOf<Series>()
    private val pollVotes = mutableMapOf<String, Triple<Int, Int, Int>>()
    private val matchComments = mutableMapOf<String, List<Triple<String, String, String>>>()

    fun initialize(context: Context) {
        val db = FirebaseFirestore.getInstance()

        // 1. Listen to Channels
        db.collection("channels").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SoccerManager", "Error listening to channels", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed defaults if empty
                    for (ch in DEFAULT_CHANNELS) {
                        saveChannel(context, ch)
                    }
                } else {
                    val channels = mutableListOf<AdminChannel>()
                    for (doc in snapshot.documents) {
                        try {
                            channels.add(
                                AdminChannel(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    logoUrl = doc.getString("logoUrl"),
                                    streamUrl = doc.getString("streamUrl") ?: "",
                                    category = doc.getString("category") ?: "رياضة"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("SoccerManager", "Error parsing channel: ${doc.id}", e)
                        }
                    }
                    adminChannels.clear()
                    adminChannels.addAll(channels)
                    adminChannelsState.value = adminChannels.toList()
                }
            }
        }

        // 2. Listen to Matches
        db.collection("matches").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SoccerManager", "Error listening to matches", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed defaults if empty
                    restoreDefaultMatches()
                    for (m in matchesList) {
                        saveMatch(context, m)
                    }
                } else {
                    val matches = mutableListOf<MatchData>()
                    for (doc in snapshot.documents) {
                        try {
                            val eventsList = mutableListOf<MatchEvent>()
                            val rawEvents = doc.get("events")
                            if (rawEvents is List<*>) {
                                for (item in rawEvents) {
                                    if (item is Map<*, *>) {
                                        eventsList.add(
                                            MatchEvent(
                                                minute = (item["minute"] ?: "").toString(),
                                                type = (item["type"] ?: "").toString(),
                                                player = (item["player"] ?: "").toString(),
                                                detail = (item["detail"] ?: "").toString()
                                            )
                                        )
                                    }
                                }
                            }
                            val videosList = mutableListOf<MatchVideo>()
                            val rawVideos = doc.get("videos")
                            if (rawVideos is List<*>) {
                                for (item in rawVideos) {
                                    if (item is Map<*, *>) {
                                        videosList.add(
                                            MatchVideo(
                                                title = (item["title"] ?: "").toString(),
                                                description = (item["description"] ?: "").toString(),
                                                duration = (item["duration"] ?: "").toString(),
                                                tag = (item["tag"] ?: "").toString(),
                                                views = (item["views"] ?: "").toString(),
                                                timeAgo = (item["timeAgo"] ?: "").toString()
                                            )
                                        )
                                    }
                                }
                            }

                            matches.add(
                                MatchData(
                                    id = doc.id,
                                    league = doc.getString("league") ?: "",
                                    home = doc.getString("home") ?: "",
                                    away = doc.getString("away") ?: "",
                                    score = doc.getString("score") ?: "",
                                    status = doc.getString("status") ?: "",
                                    hf = doc.getString("hf") ?: doc.getString("hFlag") ?: "⚽",
                                    af = doc.getString("af") ?: doc.getString("aFlag") ?: "⚽",
                                    isLive = doc.getBoolean("isLive") ?: doc.getBoolean("live") ?: false,
                                    ch = doc.getString("ch") ?: "",
                                    homeLogo = doc.getString("homeLogo"),
                                    awayLogo = doc.getString("awayLogo"),
                                    elapsed = doc.getLong("elapsed")?.toInt() ?: 0,
                                    round = doc.getString("round") ?: "",
                                    date = doc.getString("date") ?: when (doc.getString("day")) {
                                        "أمس" -> "2026-05-30"
                                        "اليوم" -> "2026-05-31"
                                        "غداً" -> "2026-06-01"
                                        else -> "2026-05-31"
                                    },
                                    time = doc.getString("time") ?: "",
                                    stadium = doc.getString("stadium") ?: "",
                                    referee = doc.getString("referee") ?: "",
                                    commentator = doc.getString("commentator") ?: "",
                                    hPen = doc.getLong("hPen")?.toInt() ?: -1,
                                    aPen = doc.getLong("aPen")?.toInt() ?: -1,
                                    streamUrlStr = doc.getString("streamUrlStr"),
                                    events = eventsList,
                                    videos = videosList
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("SoccerManager", "Error parsing match: ${doc.id}", e)
                        }
                    }
                    matchesList.clear()
                    matchesList.addAll(matches)
                    matchesListState.value = matchesList.toList()
                }
            }
        }

        // 3. Listen to Series
        db.collection("series").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SoccerManager", "Error listening to series", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed defaults if empty
                    restoreDefaultSeries()
                    for (s in seriesList) {
                        saveSeries(context, s)
                    }
                } else {
                    val series = mutableListOf<Series>()
                    for (doc in snapshot.documents) {
                        try {
                            series.add(
                                Series(
                                    id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                    title = doc.getString("title") ?: "",
                                    subTitle = doc.getString("subTitle") ?: doc.getString("subtitle") ?: "",
                                    ep = doc.getLong("ep")?.toInt() ?: 1,
                                    badge = doc.getString("badge") ?: "مترجم",
                                    age = doc.getString("age") ?: "+13",
                                    genre = doc.getString("genre") ?: "دراما",
                                    year = doc.getLong("year")?.toInt() ?: 2026,
                                    country = doc.getString("country") ?: "تركيا",
                                    status = doc.getString("status") ?: "يعرض الآن",
                                    col = doc.getLong("col") ?: 0xFF0D2E28L,
                                    totalEps = doc.getLong("totalEps")?.toInt() ?: 10,
                                    views = doc.getLong("views")?.toInt() ?: 0,
                                    story = doc.getString("story") ?: ""
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("SoccerManager", "Error parsing series: ${doc.id}", e)
                        }
                    }
                    seriesList.clear()
                    seriesList.addAll(series)
                    seriesListState.value = seriesList.toList()
                }
            }
        }

        // 4. Listen to Votes
        db.collection("votes").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SoccerManager", "Error listening to votes", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val updatedMap = mutableMapOf<String, Triple<Int, Int, Int>>()
                for (doc in snapshot.documents) {
                    val h = doc.getLong("home")?.toInt() ?: 10
                    val d = doc.getLong("draw")?.toInt() ?: 5
                    val a = doc.getLong("away")?.toInt() ?: 8
                    updatedMap[doc.id] = Triple(h, d, a)
                }
                pollVotes.clear()
                pollVotes.putAll(updatedMap)
                pollVotesState.value = pollVotes.toMap()
            }
        }

        // 5. Listen to Comments
        db.collection("comments").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("SoccerManager", "Error listening to comments", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val updatedMap = mutableMapOf<String, List<Triple<String, String, String>>>()
                for (doc in snapshot.documents) {
                    val list = mutableListOf<Triple<String, String, String>>()
                    val rawList = doc.get("commentList")
                    if (rawList is List<*>) {
                        for (item in rawList) {
                            if (item is Map<*, *>) {
                                list.add(
                                    Triple(
                                        (item["author"] ?: "").toString(),
                                        (item["text"] ?: "").toString(),
                                        (item["time"] ?: "الآن").toString()
                                    )
                                )
                            }
                        }
                    }
                    updatedMap[doc.id] = list
                }
                matchComments.clear()
                matchComments.putAll(updatedMap)
                matchCommentsState.value = matchComments.toMap()
            }
        }
    }

    private fun restoreDefaultChannels() {
        adminChannels.clear()
        adminChannels.addAll(DEFAULT_CHANNELS)
    }

    private fun restoreDefaultSeries() {
        seriesList.clear()
        seriesList.addAll(
            listOf(
                Series(1, "مغامرات تشان تشاو", "Chan Chao Adventures", 12, "مترجم", "+16", "دراما", 2025, "كوريا", "يعرض الآن", 0xFF0D2E28, 12, 1863),
                Series(2, "العناصر الأربعة للهواء", "Four Elements", 8, "مترجم", "+13", "فانتازيا", 2026, "أمريكا", "يعرض الآن", 0xFF0A2420, 8, 1240),
                Series(3, "أفكار سيئة", "Bad Thoughts", 6, "مترجم", "+16", "تشويق وإثارة", 2025, "أمريكا", "يعرض الآن", 0xFF0E3320, 6, 2540),
                Series(4, "المتعة القصوى المضمونة", "MPG Guaranteed", 2, "مترجم", "+16", "كوميديا", 2024, "ألمانيا", "مكتمل", 0xFF091E1A, 10, 320),
                Series(5, "جيمس رودريغيز", "James Rodriguez", 3, "مترجم", "+13", "وثائقي", 2025, "إسبانيا", "يعرض الآن", 0xFF142E1A, 6, 876),
                Series(6, "سكاي ميد", "SkyMed", 8, "مترجم", "+13", "دراما", 2024, "كندا", "مكتمل", 0xFF1A2E10, 8, 654),
                Series(7, "ترنيمة الساموراي", "Samurai Chant", 2, "مترجم", "+16", "أكشن", 2025, "اليابان", "يعرض الآن", 0xFF1E280A, 12, 432),
                Series(8, "ملف تعريفي مزيف", "Fake Profile", 10, "مترجم", "+16", "رومانسي", 2024, "كولومبيا", "مكتمل", 0xFF0A1E28, 10, 780),
                Series(10, "الفرنساوي", "The Frenchman", 1, "أصلي", "+16", "دراما", 2026, "مصر", "يعرض الآن", 0xFF0E1A2E, 30, 2100),
                Series(14, "ليل", "Layl", 1, "أصلي", "+16", "دراما", 2025, "لبنان", "يعرض الآن", 0xFF091E1A, 30, 1157, 
                    "يروي العمل قصة حب تجمع بين ابنة سفير ورجل فقير، تفرق بينهما الظروف، قبل أن يجتمعا مجدداً بعد سنوات ويتجدد حبهما."),
                Series(20, "المؤسس عثمان", "Kurulus Osman", 15, "مترجم", "+13", "أكشن", 2025, "تركيا", "يعرض الآن", 0xFF351F10, 40, 3940, "تدور أحداث المسلسل حول الغازي عثمان بن أرطغرل مؤسس الدولة العثمانية."),
                Series(21, "طائر الرفراف", "Yali Capkini", 21, "مترجم", "+16", "رومانسي", 2024, "تركيا", "مكتمل", 0xFF101B3A, 36, 1850, "قصة حب مليئة بالتحديات والمؤامرات الأسرية."),
                Series(22, "حبات اللؤلؤ", "Inci Taneleri", 5, "مترجم", "+13", "دراما", 2025, "تركيا", "يعرض الآن", 0xFF1C2225, 20, 1205, "مسلسل تركي شيق مليء بالدراما الإنسانية."),
                Series(11, "الحشاشين", "The Assassins", 30, "أصلي", "+16", "دراما", 2024, "مصر", "مكتمل", 0xFF103A15, 30, 4500, "طائفة الحشاشين وقائدها حسن الصباح."),
                Series(12, "خيوط المعازيب", "Khyout Al Ma'azeeb", 6, "أصلي", "+13", "دراما", 2025, "السعودية", "يعرض الآن", 0xFF3C1F0A, 15, 2980, "دراما تراثية سعودية مميزة للغاية."),
                Series(13, "سكة سفر 3", "Sikat Safar 3", 10, "أصلي", "+13", "كوميديا", 2025, "السعودية", "يعرض الآن", 0xFF2A2015, 30, 2220, "مغامرات كوميدية لثلاثة أشقاء في السعودية."),
                Series(30, "لعبة الحبار 2", "Squid Game 2", 1, "مترجم", "+18", "تشويق وإثارة", 2026, "كوريا", "يعرض الآن", 0xFF4A0A2F, 9, 8700, "الموسم الثاني من اللعبة الأكثر إثارة وتشويقاً على الإطلاق."),
                Series(31, "قدري أن أحبك", "Fated to Love You", 16, "مترجم", "+13", "رومانسي", 2024, "كوريا", "مكتمل", 0xFF2D1050, 20, 1540),
                Series(40, "الدحيح - الموسم الجديد", "El Daheeh", 4, "أصلي", "الجميع", "وثائقي", 2025, "مصر", "يعرض الآن", 0xFF0A2B60, 24, 5210, "أحمد الغندور يسطر معلومات علمية ممتعة وأفكار استثنائية بطريقة مبسطة."),
                Series(41, "سين 2", "Seen 2", 12, "أصلي", "الجميع", "وثائقي", 2024, "السعودية", "مكتمل", 0xFF023C3E, 30, 4390, "أحمد الشقيري يبحث عن حلول وممارسات مميزة حول العالم."),
                Series(42, "قلبي اطمأن 8", "Qalby Etma'an 8", 2, "أصلي", "الجميع", "وثائقي", 2025, "الإمارات", "يعرض الآن", 0xFF4A340A, 30, 3100, "رحلة غيث لنشر الخير ومساعدة المحتاجين حول العالم العربي.")
            )
        )
    }

    private fun restoreDefaultMatches() {
        matchesList.clear()

        // Match 1: Yesterday UEFA Champions League game in screenshots (PSG vs Arsenal)
        val psgArsEvents = listOf(
            MatchEvent("106'", "substitution", "Lucas Beraldo", "بديل مع Vitinha"),
            MatchEvent("118'", "card_yellow", "Nuno Mendes", "بطاقة صفراء"),
            MatchEvent("120+1'", "goal_scored", "Gonçalo Ramos", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+1'", "goal_scored", "Viktor Gyökeres", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+2'", "goal_scored", "Désiré Doué", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+2'", "goal_missed", "Eberechi Eze", "ركلة جزاء ضائعة ❌"),
            MatchEvent("120+3'", "goal_missed", "Nuno Mendes", "ركلة جزاء ضائعة ❌"),
            MatchEvent("120+3'", "goal_scored", "Declan Rice", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+4'", "goal_scored", "Achraf Hakimi", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+4'", "goal_scored", "Gabriel Martinelli", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+5'", "goal_scored", "Lucas Beraldo", "ركلة جزاء مسجلة ⚽"),
            MatchEvent("120+5'", "goal_missed", "Gabriel Magalhães", "ركلة جزاء ضائعة ❌")
        )

        val psgArsVideos = listOf(
            MatchVideo(
                "ملخص وركلات ترجيح مباراة باريس سان جيرمان وارسنال في نهائي دوري ابطال اوروبا",
                "باريس سان جيرمان × ارسنال",
                "18:28",
                "هدف",
                "99",
                "منذ 18 س"
            ),
            MatchVideo(
                "ركلات الترجيح كاملة بين باريس والارسنال بتعليق عصام الشوالي",
                "باريس سان جيرمان × ارسنال",
                "9:20",
                "shots",
                "101",
                "منذ 19 س"
            ),
            MatchVideo(
                "اهداف مباراة ارسنال وباريس سان جيرمان (1 - 1) في نهائي دوري ابطال اوروبا",
                "باريس سان جيرمان × ارسنال",
                "3:25",
                "هدف",
                "78",
                "منذ 20 س"
            ),
            MatchVideo(
                "د65' | هدف التعادل لـ باريس سان جيرمان ضد آرسنال [1-1]",
                "باريس سان جيرمان × ارسنال",
                "0:15",
                "هدف",
                "80",
                "منذ 21 س"
            )
        )

        val matchPsgArs = MatchData(
            id = "m_psg_ars",
            league = "دوري أبطال أوروبا",
            home = "باريس سان جيرمان",
            away = "أرسنال",
            score = "1 - 1",
            status = "انتهت",
            hf = "🗼",
            af = "🔫",
            isLive = false,
            ch = "beIN Sports HD 1, beIN Sports HD 2",
            homeLogo = null,
            awayLogo = null,
            elapsed = 120,
            round = "النهائي",
            date = "2026-05-30",
            time = "19:00",
            stadium = "Ferenc Puskás Stadium",
            referee = "Daniel Siebert, Germany",
            commentator = "حسن العيدروس، عصام الشوالي",
            hPen = 4,
            aPen = 3,
            streamUrlStr = null,
            events = psgArsEvents,
            videos = psgArsVideos
        )

        // Track standard comments
        matchComments["m_psg_ars"] = listOf(
            Triple("Abdulrahman Gamal", "باريس فريق محظوظ ومعه التحكيم", "منذ 18 ساعة"),
            Triple("الحج حمود", "ان شاء الله باريس", "منذ 21 ساعة"),
            Triple("كريم الجارحي", "مباراة للتاريخ بصراحة وحارس باريس رائع", "منذ 22 ساعة")
        )

        pollVotes["m_psg_ars"] = Triple(115, 27, 53) // 56%, 14%, 30%

        // Match 2: Real Madrid vs Man City (Playing LIVE today!)
        val rmCcEvents = listOf(
            MatchEvent("12'", "goal_scored", "ريال مدريد", "هدف رائع بتسديدة من فينيسيوس ⚽"),
            MatchEvent("38'", "card_yellow", "روبرتسون", "تدخل عنيف في منتصف الملعب"),
            MatchEvent("55'", "goal_scored", "مانشستر سيتي", "هدف التعادل عن طريق هالاند ⚽"),
            MatchEvent("71'", "goal_scored", "ريال مدريد", "رأسية بيلينجهام في الشباك ⚽")
        )

        val rmCcVideos = listOf(
            MatchVideo("هدف بيلينجهام الرائع في شباك مانشستر سيتي د71'", "ريال مدريد × مانشستر سيتي", "1:12", "هدف", "431", "مباشر"),
            MatchVideo("هدف هالاند د55' والتعادل للسيتيزينز", "ريال مدريد × مانشستر سيتي", "1:05", "هدف", "390", "مباشر"),
            MatchVideo("تسديدة قوية من فالفيردي ترتطم بالعارضة د30'", "ريال مدريد × مانشستر سيتي", "0:45", "shots", "210", "مباشر")
        )

        val matchRmCc = MatchData(
            id = "m_rm_cc",
            league = "دوري أبطال أوروبا",
            home = "ريال مدريد",
            away = "مانشستر سيتي",
            score = "2 - 1",
            status = "مباشر",
            hf = "👑",
            af = "🩵",
            isLive = true,
            ch = "beIN Sports HD 1",
            homeLogo = null,
            awayLogo = null,
            elapsed = 74,
            round = "نصف النهائي - إياب",
            date = "2026-05-31",
            time = "21:00",
            stadium = "ملعب سانتياغو برنابيو",
            referee = "سيمون مارسينياك",
            commentator = "حفيظ دراجي",
            streamUrlStr = "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8",
            events = rmCcEvents,
            videos = rmCcVideos
        )
        pollVotes["m_rm_cc"] = Triple(340, 110, 210)

        // Match 3: Al Ahly vs Zamalek (Live Today - Egyptian Premier League)
        val ahlyZamalekEvents = listOf(
            MatchEvent("25'", "card_yellow", "إمام عاشور", "بطاقة صفراء للإعتراض"),
            MatchEvent("44'", "goal_scored", "الأهلي", "هدف وسام أبو علي الأول ⚽")
        )
        val matchAhlyZamalek = MatchData(
            id = "m_ahly_zamalek",
            league = "الدوري المصري الممتاز",
            home = "الأهلي",
            away = "الزمالك",
            score = "1 - 0",
            status = "مباشر",
            hf = "🦅",
            af = "🏹",
            isLive = true,
            ch = "ON Time Sports 1",
            homeLogo = null,
            awayLogo = null,
            elapsed = 52,
            round = "الأسبوع 28",
            date = "2026-05-31",
            time = "20:00",
            stadium = "ملعب استاد القاهرة الدولي",
            referee = "بهاء أبو السعود",
            commentator = "مدحت شلبي",
            streamUrlStr = "https://live.alkass.net/alkass/alkass_two/playlist.m3u8",
            events = ahlyZamalekEvents
        )
        pollVotes["m_ahly_zamalek"] = Triple(820, 150, 430)

        // Match 4: Al Hilal vs Al Nassr (Upcoming Today - Saudi Pro League)
        val matchHilalNassr = MatchData(
            id = "m_hilal_nassr",
            league = "دوري روشن السعودي",
            home = "الهلال",
            away = "النصر",
            score = "0 - 0",
            status = "لم تبدأ",
            hf = "🔵",
            af = "🟡",
            isLive = false,
            ch = "SSC HD 1",
            round = "الأسبوع 32",
            date = "2026-05-31",
            time = "21:00",
            stadium = "ملعب مدينة الملك عبد الله الرياضية (الجوهرة)",
            referee = "خافيير تيلو",
            commentator = "فهد العتيبي",
            streamUrlStr = "https://live.alkass.net/alkass/alkass_one/playlist.m3u8"
        )
        pollVotes["m_hilal_nassr"] = Triple(410, 180, 520)

        // Match 5: Al Sadd vs Al Duhail (Upcoming Today - Qatari Stars League)
        val matchSaddDuhail = MatchData(
            id = "m_sadd_duhail",
            league = "دوري نجوم قطر",
            home = "السد",
            away = "الدحيل",
            score = "- : -",
            status = "لم تبدأ",
            hf = "🏁",
            af = "🔴",
            isLive = false,
            ch = "Al Kass HD 1 Qatari",
            round = "نهائي كأس الأمير",
            date = "2026-05-31",
            time = "22:15",
            stadium = "ملعب أحمد بن علي المونديالي",
            referee = "عبدالرحمن الجاسم",
            commentator = "خليل البلوشي",
            streamUrlStr = "https://live.alkass.net/alkass/alkass_one/playlist.m3u8"
        )

        // Match 6: World Cup Tomorrow (Brazil vs France)
        val matchBrazilFrance = MatchData(
            id = "m_brazil_france",
            league = "كأس العالم",
            home = "البرازيل",
            away = "فرنسا",
            score = "- : -",
            status = "لم تبدأ",
            hf = "🇧🇷",
            af = "🇫🇷",
            isLive = false,
            ch = "beIN Sports MAX 1",
            round = "ربع النهائي",
            date = "2026-06-01",
            time = "18:00",
            stadium = "ملعب لوسيل المونديالي",
            referee = "أنتوني تايلور",
            commentator = "حفيظ دراجي"
        )

        // Match 7: UEFA Super Cup (Real Madrid vs Atalanta)
        val matchMadridAtalanta = MatchData(
            id = "m_madrid_atalanta",
            league = "السوبر الأوروبي",
            home = "ريال مدريد",
            away = "أتالانتا",
            score = "- : -",
            status = "لم تبدأ",
            hf = "👑",
            af = "🔵",
            isLive = false,
            ch = "beIN Sports HD 1",
            round = "النهائي العظيم",
            date = "2026-06-01",
            time = "21:45",
            stadium = "National Stadium, Warsaw",
            referee = "ساندرو شيرر",
            commentator = "عصام الشوالي"
        )

        matchesList.addAll(
            listOf(
                matchPsgArs,
                matchRmCc,
                matchAhlyZamalek,
                matchHilalNassr,
                matchSaddDuhail,
                matchBrazilFrance,
                matchMadridAtalanta
            )
        )
    }

    private fun parseMatchJsonObject(obj: JSONObject): MatchData {
        val eventsArr = obj.optJSONArray("events") ?: JSONArray()
        val evList = mutableListOf<MatchEvent>()
        for (i in 0 until eventsArr.length()) {
            val evObj = eventsArr.getJSONObject(i)
            evList.add(
                MatchEvent(
                    minute = evObj.getString("minute"),
                    type = evObj.getString("type"),
                    player = evObj.getString("player"),
                    detail = evObj.getString("detail")
                )
            )
        }

        val vidArr = obj.optJSONArray("videos") ?: JSONArray()
        val vidList = mutableListOf<MatchVideo>()
        for (i in 0 until vidArr.length()) {
            val vObj = vidArr.getJSONObject(i)
            vidList.add(
                MatchVideo(
                    title = vObj.getString("title"),
                    description = vObj.getString("description"),
                    duration = vObj.getString("duration"),
                    tag = vObj.getString("tag"),
                    views = vObj.getString("views"),
                    timeAgo = vObj.getString("timeAgo")
                )
            )
        }

        return MatchData(
            id = obj.getString("id"),
            league = obj.getString("league"),
            home = obj.getString("home"),
            away = obj.getString("away"),
            score = obj.getString("score"),
            status = obj.getString("status"),
            hf = obj.optString("hf", "⚽"),
            af = obj.optString("af", "⚽"),
            isLive = obj.optBoolean("isLive", false),
            ch = obj.optString("ch", "البث المباشر المدمج"),
            homeLogo = obj.optString("homeLogo", null).let { if (it == "null" || it.isNullOrEmpty()) null else it },
            awayLogo = obj.optString("awayLogo", null).let { if (it == "null" || it.isNullOrEmpty()) null else it },
            elapsed = obj.optInt("elapsed", 0),
            round = obj.optString("round", "الدور المجموعات"),
            date = obj.optString("date", "2026-05-31"),
            time = obj.optString("time", "21:00"),
            stadium = obj.optString("stadium", "ملعب القاهرة الدولي"),
            referee = obj.optString("referee", "إبراهيم نور الدين"),
            commentator = obj.optString("commentator", "عصام الشوالي"),
            hPen = obj.optInt("hPen", -1),
            aPen = obj.optInt("aPen", -1),
            streamUrlStr = obj.optString("streamUrlStr", null).let { if (it == "null" || it.isNullOrEmpty()) null else it },
            events = evList,
            videos = vidList
        )
    }

    private fun serializeMatches(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (m in matchesList) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("league", m.league)
            obj.put("home", m.home)
            obj.put("away", m.away)
            obj.put("score", m.score)
            obj.put("status", m.status)
            obj.put("hf", m.hf)
            obj.put("af", m.af)
            obj.put("isLive", m.isLive)
            obj.put("ch", m.ch)
            obj.put("homeLogo", m.homeLogo)
            obj.put("awayLogo", m.awayLogo)
            obj.put("elapsed", m.elapsed)
            obj.put("round", m.round)
            obj.put("date", m.date)
            obj.put("time", m.time)
            obj.put("stadium", m.stadium)
            obj.put("referee", m.referee)
            obj.put("commentator", m.commentator)
            obj.put("hPen", m.hPen)
            obj.put("aPen", m.aPen)
            obj.put("streamUrlStr", m.streamUrlStr)

            val evArr = JSONArray()
            for (ev in m.events) {
                val evO = JSONObject()
                evO.put("minute", ev.minute)
                evO.put("type", ev.type)
                evO.put("player", ev.player)
                evO.put("detail", ev.detail)
                evArr.put(evO)
            }
            obj.put("events", evArr)

            val vidArr = JSONArray()
            for (v in m.videos) {
                val vO = JSONObject()
                vO.put("title", v.title)
                vO.put("description", v.description)
                vO.put("duration", v.duration)
                vO.put("tag", v.tag)
                vO.put("views", v.views)
                vO.put("timeAgo", v.timeAgo)
                vidArr.put(vO)
            }
            obj.put("videos", vidArr)

            arr.put(obj)
        }
        prefs.edit().putString(KEY_MATCHES, arr.toString()).apply()
    }

    private fun serializeChannels(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (c in adminChannels) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("logoUrl", c.logoUrl)
            obj.put("streamUrl", c.streamUrl)
            obj.put("category", c.category)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_CHANNELS, arr.toString()).apply()
    }

    private fun serializeVotes(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val obj = JSONObject()
        for ((mId, triple) in pollVotes) {
            val tO = JSONObject()
            tO.put("h", triple.first)
            tO.put("d", triple.second)
            tO.put("a", triple.third)
            obj.put(mId, tO)
        }
        prefs.edit().putString(KEY_VOTES, obj.toString()).apply()
    }

    private fun serializeComments(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val obj = JSONObject()
        for ((mId, cList) in matchComments) {
            val arr = JSONArray()
            for (c in cList) {
                val cO = JSONObject()
                cO.put("author", c.first)
                cO.put("text", c.second)
                cO.put("time", c.third)
                arr.put(cO)
            }
            obj.put(mId, arr)
        }
        prefs.edit().putString(KEY_COMMENTS, obj.toString()).apply()
    }

    private fun serializeSeries(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (s in seriesList) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("subTitle", s.subTitle)
            obj.put("ep", s.ep)
            obj.put("badge", s.badge)
            obj.put("age", s.age)
            obj.put("genre", s.genre)
            obj.put("year", s.year)
            obj.put("country", s.country)
            obj.put("status", s.status)
            obj.put("col", s.col)
            obj.put("totalEps", s.totalEps)
            obj.put("views", s.views)
            obj.put("story", s.story)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_SERIES, arr.toString()).apply()
    }

    // --- API & READ-WRITE API HOOKS ---
    
    fun getSeries(): List<Series> {
        return seriesListState.value
    }

    fun saveSeries(context: Context, item: Series) {
        try {
            val db = FirebaseFirestore.getInstance()
            val map = hashMapOf(
                "id" to item.id,
                "title" to item.title,
                "subTitle" to item.subTitle,
                "ep" to item.ep,
                "badge" to item.badge,
                "age" to item.age,
                "genre" to item.genre,
                "year" to item.year,
                "country" to item.country,
                "status" to item.status,
                "col" to item.col,
                "totalEps" to item.totalEps,
                "views" to item.views,
                "story" to item.story,
                "sorting_date" to System.currentTimeMillis().toString()
            )
            db.collection("series").document(item.id.toString()).set(map)
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error saving series to Firestore", e)
        }
        val list = seriesListState.value.toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }
        if (idx != -1) {
            list[idx] = item
        } else {
            list.add(item)
        }
        seriesListState.value = list
    }

    fun deleteSeries(context: Context, id: Int) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("series").document(id.toString()).delete()
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error deleting series", e)
        }
        val list = seriesListState.value.toMutableList()
        list.removeAll { it.id == id }
        seriesListState.value = list
    }
    
    fun getMatches(): List<MatchData> {
        return matchesListState.value
    }

    fun getChannels(): List<AdminChannel> {
        return adminChannelsState.value
    }

    fun saveChannel(context: Context, channel: AdminChannel) {
        try {
            val db = FirebaseFirestore.getInstance()
            val map = hashMapOf(
                "id" to channel.id,
                "name" to channel.name,
                "logoUrl" to channel.logoUrl,
                "streamUrl" to channel.streamUrl,
                "category" to channel.category
            )
            db.collection("channels").document(channel.id).set(map)
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error saving channel to Firestore", e)
        }
        val list = adminChannelsState.value.toMutableList()
        val idx = list.indexOfFirst { it.id == channel.id }
        if (idx != -1) {
            list[idx] = channel
        } else {
            list.add(channel)
        }
        adminChannelsState.value = list
    }

    fun deleteChannel(context: Context, channelId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("channels").document(channelId).delete()
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error deleting channel", e)
        }
        val list = adminChannelsState.value.toMutableList()
        list.removeAll { it.id == channelId }
        adminChannelsState.value = list
    }

    fun saveMatch(context: Context, match: MatchData) {
        try {
            val db = FirebaseFirestore.getInstance()
            val eventsMapList = match.events.map {
                mapOf("minute" to it.minute, "type" to it.type, "player" to it.player, "detail" to it.detail)
            }
            val videosMapList = match.videos.map {
                mapOf("title" to it.title, "description" to it.description, "duration" to it.duration, "tag" to it.tag, "views" to it.views, "timeAgo" to it.timeAgo)
            }
            val map = hashMapOf(
                "id" to match.id,
                "league" to match.league,
                "home" to match.home,
                "away" to match.away,
                "score" to match.score,
                "status" to match.status,
                "hf" to match.hf,
                "af" to match.af,
                "isLive" to match.isLive,
                "ch" to match.ch,
                "homeLogo" to match.homeLogo,
                "awayLogo" to match.awayLogo,
                "elapsed" to match.elapsed,
                "round" to match.round,
                "date" to match.date,
                "time" to match.time,
                "stadium" to match.stadium,
                "referee" to match.referee,
                "commentator" to match.commentator,
                "hPen" to match.hPen,
                "aPen" to match.aPen,
                "streamUrlStr" to match.streamUrlStr,
                "events" to eventsMapList,
                "videos" to videosMapList
            )
            db.collection("matches").document(match.id).set(map)
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error saving match to Firestore", e)
        }
        val list = matchesListState.value.toMutableList()
        val idx = list.indexOfFirst { it.id == match.id }
        if (idx != -1) {
            list[idx] = match
        } else {
            list.add(match)
        }
        matchesListState.value = list
    }

    fun deleteMatch(context: Context, matchId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("matches").document(matchId).delete()
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error deleting match", e)
        }
        val list = matchesListState.value.toMutableList()
        list.removeAll { it.id == matchId }
        matchesListState.value = list
    }

    fun resetAllData(context: Context) {
        restoreDefaultChannels()
        for (ch in DEFAULT_CHANNELS) {
            saveChannel(context, ch)
        }
        restoreDefaultMatches()
        for (m in matchesList) {
            saveMatch(context, m)
        }
        restoreDefaultSeries()
        for (s in seriesList) {
            saveSeries(context, s)
        }
    }

    // --- Dynamic User Actions ---

    fun getCommentsForMatch(matchId: String): List<Triple<String, String, String>> {
        return matchCommentsState.value[matchId] ?: emptyList()
    }

    fun addCommentToMatch(context: Context, matchId: String, author: String, text: String) {
        val current = (getCommentsForMatch(matchId)).toMutableList()
        current.add(0, Triple(author, text, "الآن"))
        try {
            val db = FirebaseFirestore.getInstance()
            val listMap = current.map {
                mapOf(
                    "author" to it.first,
                    "text" to it.second,
                    "time" to it.third
                )
            }
            val map = hashMapOf(
                "commentList" to listMap
            )
            db.collection("comments").document(matchId).set(map)
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error saving comment to Firestore", e)
        }
        val map = matchCommentsState.value.toMutableMap()
        map[matchId] = current
        matchCommentsState.value = map
    }

    fun getVotesForMatch(matchId: String): Triple<Int, Int, Int> {
        return pollVotesState.value[matchId] ?: Triple(10, 5, 8)
    }

    fun voteForMatch(context: Context, matchId: String, choice: String) {
        val current = getVotesForMatch(matchId)
        val updated = when (choice) {
            "home" -> Triple(current.first + 1, current.second, current.third)
            "draw" -> Triple(current.first, current.second + 1, current.third)
            "away" -> Triple(current.first, current.second, current.third + 1)
            else -> current
        }
        try {
            val db = FirebaseFirestore.getInstance()
            val map = hashMapOf(
                "home" to updated.first,
                "draw" to updated.second,
                "away" to updated.third
            )
            db.collection("votes").document(matchId).set(map)
        } catch (e: Exception) {
            Log.e("SoccerManager", "Error saving votes to Firestore", e)
        }
        val map = pollVotesState.value.toMutableMap()
        map[matchId] = updated
        pollVotesState.value = map
    }
}
