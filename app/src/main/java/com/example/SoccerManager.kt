package com.example

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object SoccerManager {
    private const val PREFS_NAME = "soccer_manager_prefs"
    private const val KEY_MATCHES = "saved_matches_json"
    private const val KEY_CHANNELS = "admin_channels_json"
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

    private val matchesList = mutableListOf<MatchData>()
    private val adminChannels = mutableListOf<AdminChannel>()
    
    // In-memory prediction vote stores: Map<MatchId, Triple<HomeVotes, DrawVotes, AwayVotes>>
    private val pollVotes = mutableMapOf<String, Triple<Int, Int, Int>>()
    
    // In-memory comment lists: Map<MatchId, List<Triple<Author, CommentText, TimeAgo>>>
    private val matchComments = mutableMapOf<String, List<Triple<String, String, String>>>()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // 1. Load Admin Channels
        val savedChannelsJson = prefs.getString(KEY_CHANNELS, null)
        if (!savedChannelsJson.isNullOrEmpty()) {
            try {
                adminChannels.clear()
                val arr = JSONArray(savedChannelsJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    adminChannels.add(
                        AdminChannel(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            logoUrl = obj.optString("logoUrl", null),
                            streamUrl = obj.getString("streamUrl"),
                            category = obj.optString("category", "رياضة")
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("SoccerManager", "Error parsing saved channels, restoring defaults", e)
                restoreDefaultChannels()
            }
        } else {
            restoreDefaultChannels()
        }

        // 2. Load Matches
        val savedMatchesJson = prefs.getString(KEY_MATCHES, null)
        if (!savedMatchesJson.isNullOrEmpty()) {
            try {
                matchesList.clear()
                val arr = JSONArray(savedMatchesJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    matchesList.add(parseMatchJsonObject(obj))
                }
            } catch (e: Exception) {
                Log.e("SoccerManager", "Error parsing saved matches, restoring defaults", e)
                restoreDefaultMatches()
            }
        } else {
            restoreDefaultMatches()
        }

        // 3. Load Poll Votes
        val savedVotesJson = prefs.getString(KEY_VOTES, null)
        if (!savedVotesJson.isNullOrEmpty()) {
            try {
                val obj = JSONObject(savedVotesJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val tripleObj = obj.getJSONObject(key)
                    pollVotes[key] = Triple(
                        tripleObj.getInt("h"),
                        tripleObj.getInt("d"),
                        tripleObj.getInt("a")
                    )
                }
            } catch (e: Exception) {
                Log.e("SoccerManager", "Error loading votes", e)
            }
        }

        // 4. Load Comments
        val savedCommentsJson = prefs.getString(KEY_COMMENTS, null)
        if (!savedCommentsJson.isNullOrEmpty()) {
            try {
                val obj = JSONObject(savedCommentsJson)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val commentsArr = obj.getJSONArray(key)
                    val cList = mutableListOf<Triple<String, String, String>>()
                    for (i in 0 until commentsArr.length()) {
                        val cObj = commentsArr.getJSONObject(i)
                        cList.add(
                            Triple(
                                cObj.getString("author"),
                                cObj.getString("text"),
                                cObj.getString("time")
                            )
                        )
                    }
                    matchComments[key] = cList
                }
            } catch (e: Exception) {
                Log.e("SoccerManager", "Error loading comments", e)
            }
        }
    }

    private fun restoreDefaultChannels() {
        adminChannels.clear()
        adminChannels.addAll(DEFAULT_CHANNELS)
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

    // --- API & READ-WRITE API HOOKS ---
    
    fun getMatches(): List<MatchData> {
        return matchesList.toList()
    }

    fun getChannels(): List<AdminChannel> {
        return adminChannels.toList()
    }

    fun saveChannel(context: Context, channel: AdminChannel) {
        val idx = adminChannels.indexOfFirst { it.id == channel.id }
        if (idx != -1) {
            adminChannels[idx] = channel
        } else {
            adminChannels.add(channel)
        }
        serializeChannels(context)
    }

    fun deleteChannel(context: Context, channelId: String) {
        adminChannels.removeAll { it.id == channelId }
        serializeChannels(context)
    }

    fun saveMatch(context: Context, match: MatchData) {
        val idx = matchesList.indexOfFirst { it.id == match.id }
        if (idx != -1) {
            matchesList[idx] = match
        } else {
            matchesList.add(match)
        }
        serializeMatches(context)
    }

    fun deleteMatch(context: Context, matchId: String) {
        matchesList.removeAll { it.id == matchId }
        serializeMatches(context)
    }

    fun resetAllData(context: Context) {
        restoreDefaultChannels()
        restoreDefaultMatches()
        pollVotes.clear()
        matchComments.clear()
        
        serializeMatches(context)
        serializeChannels(context)
        serializeVotes(context)
        serializeComments(context)
    }

    // --- Dynamic User Actions ---

    fun getCommentsForMatch(matchId: String): List<Triple<String, String, String>> {
        return matchComments[matchId] ?: emptyList()
    }

    fun addCommentToMatch(context: Context, matchId: String, author: String, text: String) {
        val current = (matchComments[matchId] ?: emptyList()).toMutableList()
        current.add(0, Triple(author, text, "الآن"))
        matchComments[matchId] = current
        serializeComments(context)
    }

    fun getVotesForMatch(matchId: String): Triple<Int, Int, Int> {
        return pollVotes[matchId] ?: Triple(10, 5, 8)
    }

    fun voteForMatch(context: Context, matchId: String, choice: String) {
        val current = getVotesForMatch(matchId)
        val updated = when (choice) {
            "home" -> Triple(current.first + 1, current.second, current.third)
            "draw" -> Triple(current.first, current.second + 1, current.third)
            "away" -> Triple(current.first, current.second, current.third + 1)
            else -> current
        }
        pollVotes[matchId] = updated
        serializeVotes(context)
    }
}
