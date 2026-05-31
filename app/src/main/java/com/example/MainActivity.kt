package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// ═══════════════════════════════════════════════════════
// GLOBAL FILTERS & CONFIGURATIONS
// ═══════════════════════════════════════════════════════
val SER_TABS = listOf("الكل", "عربية", "تركية", "أجنبية", "آسيوية", "برامج")
val GENRES = listOf("الكل", "أكشن", "أوبرا صابونية", "دراما", "فانتازيا", "تشويق وإثارة", "كوميديا", "وثائقي", "رومانسي")
val COUNTRIES = listOf("الكل", "مصر", "السعودية", "الإمارات", "كوريا", "تركيا", "أمريكا", "لبنان", "إسبانيا", "ألمانيا", "اليابان", "الصين", "كندا", "كولومبيا")
val QUALITIES = listOf("1080p", "720p", "480p", "360p")

// ═══════════════════════════════════════════════════════
// DATA MODELS
// ═══════════════════════════════════════════════════════
data class Series(
    val id: Int,
    val title: String,
    val subTitle: String = "",
    val ep: Int,
    val badge: String, // "مترجم" or "أصلي"
    val age: String,
    val genre: String,
    val year: Int,
    val country: String,
    val status: String, // "يعرض الآن", "مكتمل"
    val col: Long,
    val totalEps: Int,
    val views: Int,
    val story: String = ""
)

data class Channel(
    val id: Int,
    val name: String,
    val cat: String,
    val clr: Long,
    val abbr: String
)

data class Match(
    val league: String,
    val home: String,
    val away: String,
    val score: String,
    val status: String,
    val extra: String? = null,
    val hf: String = "⚽",
    val af: String = "⚽",
    val isLive: Boolean = false,
    val ch: String? = null,
    val homeLogo: String? = null,
    val awayLogo: String? = null,
    val elapsed: Int = 0
)

// ═══════════════════════════════════════════════════════
// DATASETS
// ═══════════════════════════════════════════════════════
val SERIES_DATA = listOf(
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
    // Turkish Series (تركيا)
    Series(20, "المؤسس عثمان", "Kurulus Osman", 15, "مترجم", "+13", "أكشن", 2025, "تركيا", "يعرض الآن", 0xFF351F10, 40, 3940, "تدور أحداث المسلسل حول الغازي عثمان بن أرطغرل مؤسس الدولة العثمانية."),
    Series(21, "طائر الرفراف", "Yali Capkini", 21, "مترجم", "+16", "رومانسي", 2024, "تركيا", "مكتمل", 0xFF101B3A, 36, 1850, "قصة حب مليئة بالتحديات والمؤامرات الأسرية."),
    Series(22, "حبات اللؤلؤ", "Inci Taneleri", 5, "مترجم", "+13", "دراما", 2025, "تركيا", "يعرض الآن", 0xFF1C2225, 20, 1205, "مسلسل تركي شيق مليء بالدراما الإنسانية."),
    // Additional Arabic Series (عربية)
    Series(11, "الحشاشين", "The Assassins", 30, "أصلي", "+16", "دراما", 2024, "مصر", "مكتمل", 0xFF103A15, 30, 4500, "طائفة الحشاشين وقائدها حسن الصباح."),
    Series(12, "خيوط المعازيب", "Khyout Al Ma'azeeb", 6, "أصلي", "+13", "دراما", 2025, "السعودية", "يعرض الآن", 0xFF3C1F0A, 15, 2980, "دراما تراثية سعودية مميزة للغاية."),
    Series(13, "سكة سفر 3", "Sikat Safar 3", 10, "أصلي", "+13", "كوميديا", 2025, "السعودية", "يعرض الآن", 0xFF2A2015, 30, 2220, "مغامرات كوميدية لثلاثة أشقاء في السعودية."),
    // Asian Series (آسيوية)
    Series(30, "لعبة الحبار 2", "Squid Game 2", 1, "مترجم", "+18", "تشويق وإثارة", 2026, "كوريا", "يعرض الآن", 0xFF4A0A2F, 9, 8700, "الموسم الثاني من اللعبة الأكثر إثارة وتشويقاً على الإطلاق."),
    Series(31, "قدري أن أحبك", "Fated to Love You", 16, "مترجم", "+13", "رومانسي", 2024, "كوريا", "مكتمل", 0xFF2D1050, 20, 1540),
    // Programs/Shows (برامج / وثائقي)
    Series(40, "الدحيح - الموسم الجديد", "El Daheeh", 4, "أصلي", "الجميع", "وثائقي", 2025, "مصر", "يعرض الآن", 0xFF0A2B60, 24, 5210, "أحمد الغندور يسطر معلومات علمية ممتعة وأفكار استثنائية بطريقة مبسطة."),
    Series(41, "سين 2", "Seen 2", 12, "أصلي", "الجميع", "وثائقي", 2024, "السعودية", "مكتمل", 0xFF023C3E, 30, 4390, "أحمد الشقيري يبحث عن حلول وممارسات مميزة حول العالم."),
    Series(42, "قلبي اطمأن 8", "Qalby Etma'an 8", 2, "أصلي", "الجميع", "وثائقي", 2025, "الإمارات", "يعرض الآن", 0xFF4A340A, 30, 3100, "رحلة غيث لنشر الخير ومساعدة المحتاجين حول العالم العربي.")
)

val CHANNELS_DATA = listOf(
    Channel(1, "beIN Sports MAX 1", "رياضة", 0xFF0A1A30, "bM1"),
    Channel(2, "beIN Sports MAX 2", "رياضة", 0xFF0A1A30, "bM2"),
    Channel(3, "beIN Sports MAX 3", "رياضة", 0xFF0A1A30, "bM3"),
    Channel(4, "beIN Sports 4", "رياضة", 0xFF0A1428, "bS4"),
    Channel(7, "MBC 1", "عام", 0xFF1A0A0A, "MBC"),
    Channel(8, "MBC 2", "أفلام", 0xFF1A0A0A, "M2"),
    Channel(9, "MBC Drama", "مسلسلات", 0xFF200A10, "Drm"),
    Channel(10, "ON Sport", "رياضة", 0xFF0A2010, "ON"),
    Channel(11, "Al Arabiya", "أخبار", 0xFF0A1020, "Arb")
)

val MATCHES_DATA = listOf(
    Match("دوري أبطال أفريقيا", "الجيش الملكي", "صن داونز", "1-1", "انتهت", hf = "🏆", af = "🏅", ch = "beIN Sports 2"),
    Match("كأس إفريقيا 17 سنة", "المغرب 17", "الكاميرون 17", "1-0", "انتهت", hf = "🇲🇦", af = "🇨🇲"),
    Match("كأس إفريقيا 17 سنة", "كوت ديفوار 17", "مصر 17", "4-1", "انتهت", hf = "🇨🇮", af = "🇪🇬"),
    Match("الدوري الإنجليزي", "تشيلسي", "أرسنال", "1-0", "مباشر", hf = "🔵", af = "🔴", isLive = true, ch = "beIN Sports 1")
)

// ═══════════════════════════════════════════════════════
// MAIN COMPONENT & EDGE-TO-EDGE
// ═══════════════════════════════════════════════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoccerManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainRouter()
                }
            }
        }
    }
}

@Composable
fun MainRouter() {
    var ready by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var currentTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("home") }
    var sidebarOpen by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var selectedSeries by remember { mutableStateOf<Series?>(null) }
    var watchData by remember { mutableStateOf<Pair<Series, Int>?>(null) }

    // TMDB States & Variables
    var tmdbApiKey by remember { mutableStateOf("84f183e20e8d6411ab1b80c108169123") }
    var selectedTmdbWorkId by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var watchTmdbData by remember { mutableStateOf<Pair<TmdbWorkDetails, Int>?>(null) }

    // IPTV States
    var selectedIptvChannel by remember { mutableStateOf<IptvChannel?>(null) }

    // Unified live sport and admin configurations
    var selectedMatchId by remember { mutableStateOf<String?>(null) }
    var isAdminDashboardOpen by remember { mutableStateOf(false) }

    if (!ready) {
        SplashScreen(onFinished = { ready = true })
    } else {
        Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
            if (isAdminDashboardOpen) {
                AdminDashboardPage(onBack = { isAdminDashboardOpen = false })
            } else if (selectedMatchId != null) {
                MatchDetailsPage(
                    matchId = selectedMatchId!!,
                    onBack = { selectedMatchId = null },
                    onWatchChannel = { selectedIptvChannel = it }
                )
            } else if (watchData != null) {
                WatchPage(series = watchData!!.first, epNum = watchData!!.second, onBack = { watchData = null })
            } else if (watchTmdbData != null) {
                TmdbWatchPage(details = watchTmdbData!!.first, epNum = watchTmdbData!!.second, onBack = { watchTmdbData = null })
            } else if (selectedIptvChannel != null) {
                IptvWatchPage(channel = selectedIptvChannel!!, onBack = { selectedIptvChannel = null })
            } else if (selectedSeries != null) {
                SeriesDetailPage(
                    series = selectedSeries!!,
                    onBack = { selectedSeries = null },
                    onWatch = { s, ep -> watchData = Pair(s, ep) }
                )
            } else if (selectedTmdbWorkId != null) {
                TmdbDetailPage(
                    mediaType = selectedTmdbWorkId!!.first,
                    id = selectedTmdbWorkId!!.second,
                    apiKey = tmdbApiKey,
                    onBack = { selectedTmdbWorkId = null },
                    onWatch = { details, ep -> watchTmdbData = Pair(details, ep) }
                )
            } else {
                Scaffold(
                    bottomBar = {
                        BottomBar(activeTab = currentTab, onTabSelect = { currentTab = it })
                    },
                    containerColor = BgColor
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentTab) {
                            "home" -> HomeScreen(
                                onSeriesSelect = { selectedSeries = it },
                                onOpenDrawer = { sidebarOpen = true },
                                onChannelSelect = { selectedIptvChannel = it }
                            )
                            "series" -> SeriesCatalogScreen(onSeriesSelect = { selectedSeries = it })
                            "channels" -> ChannelsScreen(onChannelSelect = { selectedIptvChannel = it })
                            "matches" -> MatchesScreen(
                                onMatchSelect = { selectedMatchId = it },
                                onOpenAdmin = { isAdminDashboardOpen = true }
                            )
                            "fav" -> FavoritesScreen(onSeriesSelect = { selectedSeries = it })
                            "search" -> TmdbSearchScreen(
                                apiKey = tmdbApiKey,
                                onApiKeyChange = { tmdbApiKey = it },
                                onWorkSelect = { type, id -> selectedTmdbWorkId = Pair(type, id) }
                            )
                        }
                    }
                }
            }

            // Custom sliding sidebar drawer
            AnimatedVisibility(
                visible = sidebarOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                SidebarDrawer(onClose = { sidebarOpen = false })
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// SPLASH & BRANDING UTILS
// ═══════════════════════════════════════════════════════
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0E2A22), BgColor),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MiMLogo(size = 120f)
            Spacer(modifier = Modifier.height(16.dp))
            MiMLogoText(scale = 1.3f)
            Spacer(modifier = Modifier.height(30.dp))
            CircularProgressIndicator(color = TealColor, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun MiMLogo(size: Float = 100f) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic layered design representing modern neon logo rings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, GoldColor.copy(alpha = 0.3f), CircleShape)
                .padding(6.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TealLightColor, TealColor, TealDeepColor)
                    ),
                    shape = CircleShape
                )
                .border(1.dp, GoldColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "م",
                color = Color.White,
                fontSize = (size * 0.35f).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(y = (-2).dp)
            )
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-4).dp, x = (-4).dp)
                .size((size * 0.3f).dp)
                .background(CardColor, shape = CircleShape)
                .border(1.dp, GoldColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "+",
                color = GoldColor,
                fontSize = (size * 0.18f).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}

@Composable
fun MiMLogoText(scale: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("MiM", fontSize = (32 * scale).sp, fontWeight = FontWeight.Black, color = TealLightColor, fontFamily = FontFamily.Serif)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Plus", fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold, color = GoldColor, fontFamily = FontFamily.Serif)
        }
        Text("البث الذكي", fontSize = (11 * scale).sp, color = TextSec, letterSpacing = 2.sp)
    }
}

// ═══════════════════════════════════════════════════════
// SHARED WIDGETS
// ═══════════════════════════════════════════════════════
@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(Brush.verticalGradient(listOf(TealLightColor, TealDeepColor)), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = TextPri, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Badge(text: String, isOriginal: Boolean = false) {
    Box(
        modifier = Modifier
            .background(
                if (isOriginal) Color(0x3300A896) else Color(0x2E4EAED4),
                RoundedCornerShape(4.dp)
            )
            .border(0.5.dp, if (isOriginal) TealLightColor.copy(0.4f) else Color(0x884EAED4), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = if (isOriginal) TealLightColor else Color(0xFF4EAED4), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadgeWidget(status: String) {
    val (bg, fg) = when (status) {
        "يعرض الآن", "جاري العرض" -> Pair(Color(0x3300FF99), Color(0xFF4ECDA0))
        "مكتمل" -> Pair(Color(0x334E96D4), Color(0xFF6EB6D4))
        "مباشر" -> Pair(Color(0x33FF3333), Color(0xFFFF5252))
        else -> Pair(Color(0x22FFFFFF), TextSec)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(status, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ═══════════════════════════════════════════════════════
// HIGH-FIDELITY ITEM CARD
// ═══════════════════════════════════════════════════════
@Composable
fun PosterSeriesCard(s: Series, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.verticalGradient(listOf(Color(s.col), BgColor)))
                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
        ) {
            // Gradient base shadows
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f))))
            )
            
            // Subtitles indicators
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("${s.ep} ▶", color = TextSec, fontSize = 8.sp)
            }

            // Translation system status
            Box(modifier = Modifier.align(Alignment.TopStart).padding(5.dp)) {
                Badge(s.badge, isOriginal = s.badge == "أصلي")
            }

            // Age tag
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp)
                    .background(RedColor.copy(0.2f), RoundedCornerShape(4.dp))
                    .border(0.5.dp, RedColor.copy(0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(s.age, color = Color(0xFFFF9090), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            s.title,
            color = TextPri,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════
// SIDEBAR DRAWER WIDGET
// ═══════════════════════════════════════════════════════
@Composable
fun SidebarDrawer(onClose: () -> Unit) {
    var darkTheme by remember { mutableStateOf(true) }
    var receiveNotifications by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )

        // Sidebar container (sliding on right)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(Color(0xFF090E0C))
                .border(2.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .align(Alignment.TopEnd)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MiMLogo(size = 72f)
            }
            Spacer(modifier = Modifier.height(14.dp))

            // User Box Profile
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Brush.linearGradient(listOf(TealColor, TealDeepColor)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ض", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("ضيف", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("ID: #48291", color = TextSec, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⭐ اشترك في VIP", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderColor)

            val menuList = listOf(
                Pair("🏠", "الرئيسية"),
                Pair("🎬", "المسلسلات"),
                Pair("📺", "القنوات"),
                Pair("⚽", "مباريات اليوم"),
                Pair("🔍", "البحث والتصفح"),
                Pair("♡", "المفضلة")
            )

            menuList.forEach { (ic, label) ->
                TextButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ic, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Divider(color = BorderColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Switch selections
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الوضع المظلم 🌙", color = TextPri, fontSize = 13.sp)
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { darkTheme = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TealColor, checkedTrackColor = TealDeepColor)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("استقبال الإشعارات 🔔", color = TextPri, fontSize = 13.sp)
                Switch(
                    checked = receiveNotifications,
                    onCheckedChange = { receiveNotifications = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TealColor, checkedTrackColor = TealDeepColor)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// ADVERTISING & SUB-ROW MODULES
// ═══════════════════════════════════════════════════════
@Composable
fun AdBannerWidget(adIndex: Int) {
    val gradientColors = when (adIndex) {
        1 -> listOf(Color(0xFF1B0B2E), Color(0xFF4A148C))
        2 -> listOf(Color(0xFF071B20), Color(0xFF00796B))
        3 -> listOf(Color(0xFF1E0A0A), Color(0xFFB71C1C))
        4 -> listOf(Color(0xFF23081A), Color(0xFFE91E63))
        else -> listOf(Color(0xFF071C2E), Color(0xFF0288D1))
    }
    
    val badgeText = when (adIndex) {
        1 -> "بريميوم ✨"
        2 -> "تلفاز حُر 📺"
        3 -> "كورة مباشر ⚽"
        4 -> "حصري 🔥"
        else -> "تليجرام 🍿"
    }

    val titleText = when (adIndex) {
        1 -> "باقات MiM Plus المميزة 🍿"
        2 -> "البث المباشر المفتوح جاهز! ✨"
        3 -> "جدول المباريات الحية لا يفوتك!"
        4 -> "إنتاجات MiM Plus الأصلية"
        else -> "شاركنا ذوقك أو اطلب مسلسلك 💬"
    }

    val descText = when (adIndex) {
        1 -> "اشترك الآن للتخلص من الإعلانات تماماً ومشاهدة حصرية بجودة 4K فائقة الوضوح!"
        2 -> "تابع أكثر من 200 قناة عربية وعالمية مباشرة وبأقل استهلاك لبيانات الهاتف!"
        3 -> "تابع مباريات دوري أبطال أوروبا والبطولات العربية بث مباشر أولاً بأول!"
        4 -> "شاهد الآن أحدث البرامج الوثائقية والحصرية والمسلسلات العربية فقط لدينا!"
        else -> "انضم لقناة الدعم الفني وتوصية المحتوى عبر تليجرام واطلب مسلسلك المفضل!"
    }

    val actionText = when (adIndex) {
        1 -> "اشترك الآن"
        2 -> "افتح البث الحي"
        3 -> "تابع المباريات"
        4 -> "شاهد الحصريات"
        else -> "انضم الآن"
    }

    Box(
        modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.radialGradient(gradientColors, radius = 900f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badgeText, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text("إعلان مموّل", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(titleText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(descText, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(actionText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EpisodeRow(items: List<Series>, onSeriesSelect: (Series) -> Unit) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("قريباً 🎬", color = TextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { s ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onSeriesSelect(s) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.verticalGradient(listOf(Color(s.col), BgColor)))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .border(1.dp, TealLightColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = TealLightColor, fontSize = 11.sp, modifier = Modifier.offset(x = 1.dp))
                        }
                        Text(
                            "الحلقة ${s.ep}",
                            color = TextSec,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Color.Black.copy(0.7f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        s.title,
                        color = TextPri,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PosterRow(items: List<Series>, onSeriesSelect: (Series) -> Unit) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("قريباً 🎬", color = TextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { s ->
                Box(modifier = Modifier.width(100.dp)) {
                    PosterSeriesCard(s = s, onClick = { onSeriesSelect(s) })
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// HOME SCREEN COMPONENT
// ═══════════════════════════════════════════════════════
@Composable
fun HomeScreen(
    onSeriesSelect: (Series) -> Unit,
    onOpenDrawer: () -> Unit,
    onChannelSelect: (IptvChannel) -> Unit
) {
    var heroIndex by remember { mutableStateOf(0) }
    val featured = SERIES_DATA.take(5)

    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            heroIndex = (heroIndex + 1) % featured.size
        }
    }

    val currentHero = featured[heroIndex]

    LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // TOP HEADER CAROUSEL PANEL
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(currentHero.col))
            ) {
                // Background dark gradients simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, BgColor.copy(alpha = 0.95f))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(currentHero.col).copy(alpha = 0.4f), Color.Transparent),
                                radius = 700f
                            )
                        )
                )

                // Top Actions Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 14.dp, end = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Text("★", color = TextPri, fontSize = 18.sp)
                    }
                    MiMLogoText(scale = 0.85f)
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Text("☰", color = TextPri, fontSize = 18.sp)
                    }
                }

                // Title info details aligned bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        featured.forEachIndexed { i, _ ->
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(if (i == heroIndex) 18.dp else 4.dp)
                                    .background(if (i == heroIndex) TealLightColor else Color.White.copy(0.35f), CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        currentHero.title,
                        color = TextPri,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusBadgeWidget(currentHero.status)
                        Text("${currentHero.genre} • ${currentHero.year}", color = TextSec, fontSize = 11.sp)
                        Text("⭐ 4.8", color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. أحدث الحلقات (يتم عرض جميع الحلقات ال اي شي فيها حسب العرض)
        item {
            SectionHeader("أحدث الحلقات")
            EpisodeRow(items = SERIES_DATA.take(8), onSeriesSelect = onSeriesSelect)
        }

        // 2. بعدها القنوات الأكثر مشاهدة
        item {
            SectionHeader("القنوات الأكثر مشاهدة")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CHANNELS_DATA) { ch ->
                    Card(
                        modifier = Modifier
                            .size(54.dp)
                            .clickable {
                                val streamUrl = when (ch.id) {
                                    7 -> "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"
                                    else -> "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"
                                }
                                onChannelSelect(
                                    IptvChannel(
                                        channelId = "ch_${ch.id}",
                                        channelName = ch.name,
                                        category = ch.cat,
                                        logoUrl = null,
                                        streamUrl = streamUrl
                                    )
                                )
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(ch.clr)),
                        border = BorderStroke(0.5.dp, BorderColor)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(ch.abbr, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. بعدها الاكثر مشاهده
        item {
            SectionHeader("الأكثر مشاهدة 🔥")
            PosterRow(items = SERIES_DATA.sortedByDescending { it.views }.take(8), onSeriesSelect = onSeriesSelect)
        }

        // 4. بعدها مكان زي مربع اضع فيها تصميم اعلاني ال اي شي (إعلان 1)
        item {
            AdBannerWidget(adIndex = 1)
        }

        // 5. قسم المسلسلات العربية
        // أ. احدث الحلقات العربيه
        item {
            SectionHeader("أحدث الحلقات العربية")
            val arabicList = SERIES_DATA.filter { it.country in listOf("مصر", "لبنان", "السعودية", "الإمارات") }
            EpisodeRow(items = arabicList.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ب. احدث المسلسلات العربيه
        item {
            SectionHeader("أحدث المسلسلات العربية")
            val arabicList = SERIES_DATA.filter { it.country in listOf("مصر", "لبنان", "السعودية", "الإمارات") }
            PosterRow(items = arabicList.sortedByDescending { it.year }.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ج. مسلسلات عربيه تعرض الآن
        item {
            SectionHeader("مسلسلات عربية تعرض الآن 🔴")
            val arabicAiring = SERIES_DATA.filter { 
                it.country in listOf("مصر", "لبنان", "السعودية", "الإمارات") && 
                (it.status == "يعرض الآن" || it.status == "جاري العرض")
            }
            PosterRow(items = arabicAiring, onSeriesSelect = onSeriesSelect)
        }

        // 6. بعدها مكان زي مربع اضع فيها تصميم اعلاني ال اي شي (إعلان 2)
        item {
            AdBannerWidget(adIndex = 2)
        }

        // 7. قسم المسلسلات الأجنبية
        // أ. احدث الحلقات الاجنبيه
        item {
            SectionHeader("أحدث الحلقات الأجنبية")
            val foreignList = SERIES_DATA.filter { 
                it.country !in listOf("مصر", "لبنان", "السعودية", "الإمارات", "تركيا", "كوريا", "اليابان", "الصين") 
            }
            EpisodeRow(items = foreignList.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ب. احدث مسلسلات اجنبيه تعرض الآن
        item {
            SectionHeader("أحدث المسلسلات الأجنبية التي تعرض الآن 🔴")
            val foreignAiring = SERIES_DATA.filter { 
                it.country !in listOf("مصر", "لبنان", "السعودية", "الإمارات", "تركيا", "كوريا", "اليابان", "الصين") && 
                it.status == "يعرض الآن"
            }
            PosterRow(items = foreignAiring, onSeriesSelect = onSeriesSelect)
        }

        // 8. بعدها مكان زي مربع اضع فيها تصميم اعلاني ال اي شي (إعلان 3)
        item {
            AdBannerWidget(adIndex = 3)
        }

        // 9. قسم المسلسلات التركية
        // أ. احدث حلقات المسلسلات التركي
        item {
            SectionHeader("أحدث حلقات المسلسلات التركية")
            val turkishList = SERIES_DATA.filter { it.country == "تركيا" }
            EpisodeRow(items = turkishList.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ب. احدث ما يعرض من مسلسلات تركي
        item {
            SectionHeader("أحدث ما يعرض من المسلسلات التركية 🔴")
            val turkishAiring = SERIES_DATA.filter { it.country == "تركيا" && it.status == "يعرض الآن" }
            PosterRow(items = turkishAiring, onSeriesSelect = onSeriesSelect)
        }

        // 10. بعدها مكان زي مربع اضع فيها تصميم اعلاني ال اي شي (إعلان 4)
        item {
            AdBannerWidget(adIndex = 4)
        }

        // 11. قسم المسلسلات الآسيوية
        // أ. احدث مسلسلات اسيويه
        item {
            SectionHeader("أحدث المسلسلات الآسيوية")
            val asianList = SERIES_DATA.filter { it.country in listOf("كوريا", "اليابان", "الصين") }
            PosterRow(items = asianList.sortedByDescending { it.year }.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ب. احدث ما يعرض من مسلسلات اسيويه
        item {
            SectionHeader("أحدث ما يعرض من مسلسلات آسيوية 🔴")
            val asianAiring = SERIES_DATA.filter { it.country in listOf("كوريا", "اليابان", "الصين") && it.status == "يعرض الآن" }
            PosterRow(items = asianAiring, onSeriesSelect = onSeriesSelect)
        }

        // 12. بعدها مكان زي مربع اضع فيها تصميم اعلاني ال اي شي (إعلان 5)
        item {
            AdBannerWidget(adIndex = 5)
        }

        // 13. قسم البرامج
        // أ. احدث حلقات البرامج
        item {
            SectionHeader("أحدث حلقات البرامج")
            val programsList = SERIES_DATA.filter { it.genre == "وثائقي" }
            EpisodeRow(items = programsList.take(8), onSeriesSelect = onSeriesSelect)
        }

        // ب. احدث البرامج التي تعرض لأن
        item {
            SectionHeader("أحدث البرامج التي تعرض الآن 🔴")
            val programsAiring = SERIES_DATA.filter { it.genre == "وثائقي" && it.status == "يعرض الآن" }
            PosterRow(items = programsAiring, onSeriesSelect = onSeriesSelect)
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ═══════════════════════════════════════════════════════
// CATALOG WITH HORIZONTAL ACCENT FILTERS
// ═══════════════════════════════════════════════════════
@Composable
fun FilterChipItem(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) TealColor.copy(0.18f) else CardColor)
            .border(
                width = 0.5.dp,
                color = if (isActive) TealLightColor else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = if (isActive) TealLightColor else TextSec,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "▾",
                color = if (isActive) TealLightColor else TextSec.copy(0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SeriesCatalogScreen(onSeriesSelect: (Series) -> Unit) {
    var activeTab by remember { mutableStateOf("الكل") }
    var selectedSort by remember { mutableStateOf("الأحدث") }
    var selectedCountry by remember { mutableStateOf("الكل") }
    var selectedGenre by remember { mutableStateOf("الكل") }
    var selectedYear by remember { mutableStateOf("الكل") }
    var selectedBadge by remember { mutableStateOf("الكل") }
    var selectedStatus by remember { mutableStateOf("الكل") }
    var selectedAge by remember { mutableStateOf("الكل") }
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog/BottomSheet triggers
    var openFilterType by remember { mutableStateOf<String?>(null) } // "sort", "country", "genre", "year", "badge", "status", "age"

    val displayedSeries = SERIES_DATA.filter { s ->
        // 1. Sliding Tabs Filter logic matches
        val tabMatch = when (activeTab) {
            "الكل" -> true
            "عربية" -> s.country in listOf("مصر", "لبنان", "السعودية", "الإمارات")
            "تركية" -> s.country == "تركيا"
            "أجنبية" -> s.country !in listOf("مصر", "لبنان", "السعودية", "الإمارات", "تركيا", "كوريا", "اليابان", "الصين") && s.genre != "وثائقي"
            "آسيوية" -> s.country in listOf("كوريا", "اليابان", "الصين")
            "برامج" -> s.genre == "وثائقي"
            else -> true
        }
        
        // 2. Auxiliary Category Filters
        val genreMatch = selectedGenre == "الكل" || s.genre == selectedGenre
        val countryMatch = selectedCountry == "الكل" || s.country == selectedCountry
        val yearMatch = selectedYear == "الكل" || s.year.toString() == selectedYear
        val badgeMatch = selectedBadge == "الكل" || s.badge == selectedBadge
        val statusMatch = selectedStatus == "الكل" || s.status == selectedStatus
        val ageMatch = selectedAge == "الكل" || s.age == selectedAge
        
        // 3. Search Matching
        val searchMatch = searchQuery.isEmpty() || 
                s.title.contains(searchQuery, ignoreCase = true) || 
                s.subTitle.contains(searchQuery, ignoreCase = true)

        tabMatch && genreMatch && countryMatch && yearMatch && badgeMatch && statusMatch && ageMatch && searchMatch
    }.let { list ->
        // Apply Sort algorithm strictly based on choices
        when (selectedSort) {
            "الأحدث" -> list.sortedWith(compareByDescending<Series> { it.year }.thenByDescending { it.id })
            "أحدث حلقة" -> list.sortedByDescending { it.ep }
            "الأكثر مشاهدة" -> list.sortedByDescending { it.views }
            "الأعلى تقييماً" -> list.sortedWith(compareByDescending<Series> { it.views }.thenByDescending { it.year })
            "الاسم أ-ي" -> list.sortedBy { it.title }
            "الاسم ي-أ" -> list.sortedByDescending { it.title }
            else -> list
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(30.dp))
            
            // Header with search bar integration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(CardColor, RoundedCornerShape(18.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍", fontSize = 14.sp, color = TealLightColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = TextPri, fontSize = 12.sp),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("بحث عن مسلسل...", color = TextSec, fontSize = 11.sp)
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = TextSec,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إلغاء",
                        color = TextSec,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { searchQuery = ""; isSearchActive = false }
                            .padding(horizontal = 4.dp)
                    )
                } else {
                    Text("جميع مسلسلات MiM Plus", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("🔍", fontSize = 18.sp, color = TealLightColor)
                    }
                }
            }

            // Sliding Tabs (Row 1)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(SER_TABS) { t ->
                    val isSelected = activeTab == t
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) TealColor else CardColor)
                            .border(0.5.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(20.dp))
                            .clickable { activeTab = t }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(t, color = if (isSelected) Color.White else TextSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Second Level Horizontal Filter chips row (Row 2) - Exactly matching screenshot scrolling!
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Sort Chip
                item {
                    FilterChipItem(
                        label = if (selectedSort == "الأحدث") "الأحدث" else "ترتيب: $selectedSort",
                        isActive = selectedSort != "الأحدث",
                        onClick = { openFilterType = "sort" }
                    )
                }
                // 2. Country Chip
                item {
                    FilterChipItem(
                        label = if (selectedCountry == "الكل") "البلد" else "البلد: $selectedCountry",
                        isActive = selectedCountry != "الكل",
                        onClick = { openFilterType = "country" }
                    )
                }
                // 3. Genre Chip
                item {
                    FilterChipItem(
                        label = if (selectedGenre == "الكل") "النوع" else "النوع: $selectedGenre",
                        isActive = selectedGenre != "الكل",
                        onClick = { openFilterType = "genre" }
                    )
                }
                // 4. Year Chip
                item {
                    FilterChipItem(
                        label = if (selectedYear == "الكل") "السنة" else "السنة: $selectedYear",
                        isActive = selectedYear != "الكل",
                        onClick = { openFilterType = "year" }
                    )
                }
                // 5. Content Badge Chip (Translation)
                item {
                    FilterChipItem(
                        label = if (selectedBadge == "الكل") "المحتوى" else "المحتوى: $selectedBadge",
                        isActive = selectedBadge != "الكل",
                        onClick = { openFilterType = "badge" }
                    )
                }
                // 6. Status Chip
                item {
                    FilterChipItem(
                        label = if (selectedStatus == "الكل") "الحالة" else "الحالة: $selectedStatus",
                        isActive = selectedStatus != "الكل",
                        onClick = { openFilterType = "status" }
                    )
                }
                // 7. Age Target Rating Chip
                item {
                    FilterChipItem(
                        label = if (selectedAge == "الكل") "التصنيف العمري" else "التصنيف: $selectedAge",
                        isActive = selectedAge != "الكل",
                        onClick = { openFilterType = "age" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayedSeries.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد مسلسلات مطابقة للفلاتر", color = TextSec, fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayedSeries) { s ->
                        PosterSeriesCard(s = s, onClick = { onSeriesSelect(s) })
                    }
                }
            }
        }

        // Luxurious custom slide-up bottom sheet with dark translucent fade matching screenshot!
        AnimatedVisibility(
            visible = openFilterType != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6f))
                    .clickable { openFilterType = null }
            ) {
                // Sheet container sliding from the bottom
                AnimatedVisibility(
                    visible = openFilterType != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clickable(enabled = false) { }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(CardDeepColor)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(bottom = 24.dp)
                    ) {
                        // Grab Handle at the top of the sheet
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 12.dp)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(BorderColor)
                        )

                        // Title translations matching selections
                        val sheetTitle = when (openFilterType) {
                            "sort" -> "ترتيب حسب"
                            "country" -> "اختر البلد"
                            "genre" -> "اختر التصنيف"
                            "year" -> "اختر سنة الإنتاج"
                            "badge" -> "اختر نوع المحتوى"
                            "status" -> "اختر حالة المسلسل"
                            "age" -> "اختر التصنيف العمري"
                            else -> ""
                        }

                        Text(
                            text = sheetTitle,
                            color = TextPri,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            textAlign = TextAlign.Center
                        )

                        // Populate options lists
                        val list = when (openFilterType) {
                            "sort" -> listOf("الأحدث", "أحدث حلقة", "الأكثر مشاهدة", "الأعلى تقييماً", "الاسم أ-ي", "الاسم ي-أ")
                            "country" -> COUNTRIES
                            "genre" -> GENRES
                            "year" -> listOf("الكل", "2026", "2025", "2024", "2023", "2022")
                            "badge" -> listOf("الكل", "مترجم", "أصلي")
                            "status" -> listOf("الكل", "يعرض الآن", "مكتمل")
                            "age" -> listOf("الكل", "الجميع", "+13", "+16", "+18")
                            else -> emptyList()
                        }

                        val currentSelectedOption = when (openFilterType) {
                            "sort" -> selectedSort
                            "country" -> selectedCountry
                            "genre" -> selectedGenre
                            "year" -> selectedYear
                            "badge" -> selectedBadge
                            "status" -> selectedStatus
                            "age" -> selectedAge
                            else -> ""
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp)
                        ) {
                            items(list) { option ->
                                val isOptionSelected = option == currentSelectedOption
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            when (openFilterType) {
                                                "sort" -> selectedSort = option
                                                "country" -> selectedCountry = option
                                                "genre" -> selectedGenre = option
                                                "year" -> selectedYear = option
                                                "badge" -> selectedBadge = option
                                                "status" -> selectedStatus = option
                                                "age" -> selectedAge = option
                                            }
                                            openFilterType = null
                                        }
                                        .padding(horizontal = 20.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = option,
                                        color = if (isOptionSelected) TealLightColor else TextPri,
                                        fontSize = 13.sp,
                                        fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // Checkmark styled in teal (TealLightColor) on the left of item!
                                    if (isOptionSelected) {
                                        Text(
                                            text = "✓",
                                            color = TealLightColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Divider(
                                    color = BorderColor.copy(0.2f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// LIVE SPORTS SCORES SCREEN (STATEFUL & DYNAMIC INTEGRATED)
// ═══════════════════════════════════════════════════════
@Composable
fun MatchesScreen(
    onMatchSelect: (String) -> Unit,
    onOpenAdmin: () -> Unit
) {
    var matchesList by remember { mutableStateOf(SoccerManager.getMatches()) }
    var selectedDate by remember { mutableStateOf("اليوم") }

    LaunchedEffect(Unit) {
        while (true) {
            matchesList = SoccerManager.getMatches()
            delay(5000) // Fast refresh matching dynamic states
        }
    }

    val displayedMatches = remember(matchesList, selectedDate) {
        when (selectedDate) {
            "أمس" -> matchesList.filter { m -> m.date == "2026-05-30" || m.status == "انتهت" }
            "اليوم" -> matchesList.filter { m -> m.date == "2026-05-31" || m.status == "مباشر" }
            "غداً" -> matchesList.filter { m -> m.date == "2026-06-01" || (m.status == "لم تبدأ" && m.date != "2026-05-31" && m.date != "2026-05-30") }
            else -> matchesList
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Spacer(modifier = Modifier.height(30.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("المباريات والنتائج المباشرة ⚽", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(TealLightColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بث حي وتحديث لحظي للأحداث والنتائج ⚡", color = TextSec, fontSize = 10.sp)
                }
            }

            // ADMIN CONTROL ACCESS BUTTON INSIDE MAIN BAR
            Button(
                onClick = onOpenAdmin,
                colors = ButtonDefaults.buttonColors(containerColor = CardColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(0.5.dp, BorderColor, RoundedCornerShape(8.dp)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("المشرف 🔐", color = TealLightColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Quick date picker buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = listOf("أمس", "اليوم", "غداً")
            list.forEach { d ->
                val isSelected = selectedDate == d
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CardColor else Color.Transparent)
                        .border(1.dp, if (isSelected) TealColor.copy(0.4f) else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { selectedDate = d }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(d, color = if (isSelected) TealLightColor else TextSec, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "تغطية كاملة للدوريات الخمسة الكبرى، والدوري المصري، والسعودي، والخليجي 🌍",
            color = TextSec,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
        )

        if (displayedMatches.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚽", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لا توجد مباريات مجدولة لهذا اليوم حالياً.", color = TextSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayedMatches) { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardColor, RoundedCornerShape(12.dp))
                            .border(0.5.dp, if (m.isLive) Color.Red.copy(0.4f) else BorderColor, RoundedCornerShape(12.dp))
                            .clickable { onMatchSelect(m.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Team
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color.White.copy(0.04f), CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!m.homeLogo.isNullOrEmpty() && m.homeLogo.startsWith("http")) {
                                    AsyncImage(
                                        model = m.homeLogo,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(m.hf, fontSize = 24.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(m.home, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        // Match Center info
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(m.league, color = TextSec, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(m.score, color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Badge with Status Indicator
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (m.status) {
                                            "مباشر" -> Color.Red.copy(0.2f)
                                            "انتهت" -> Color.White.copy(0.1f)
                                            else -> BorderColor
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (m.isLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color.Red, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (m.isLive && m.elapsed > 0) "مباشر د.${m.elapsed}" else m.status,
                                    color = when (m.status) {
                                        "مباشر" -> Color.Red
                                        "انتهت" -> TextSec
                                        else -> TextPri
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (m.ch.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("📺 " + m.ch, color = TealLightColor, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        // Away Team
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color.White.copy(0.04f), CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!m.awayLogo.isNullOrEmpty() && m.awayLogo.startsWith("http")) {
                                    AsyncImage(
                                        model = m.awayLogo,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(m.af, fontSize = 24.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(m.away, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TV LIVE GRID
// ═══════════════════════════════════════════════════════
@Composable
fun ChannelsScreen(onChannelSelect: (IptvChannel) -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rawChannels by remember { mutableStateOf<List<IptvChannel>>(emptyList()) }
    var filteredChannels by remember { mutableStateOf<List<IptvChannel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var categoriesList by remember { mutableStateOf<List<String>>(listOf("الكل")) }

    val service = remember { IptvService() }

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val fetched = service.fetchAndParseArabicM3u()
            rawChannels = fetched
            
            // Extract distinct categories
            val distinctCats = fetched.map { it.category }.distinct().filter { it.isNotEmpty() }
            categoriesList = listOf("الكل") + distinctCats
            
            filteredChannels = fetched
        } catch (e: Exception) {
            errorMessage = "عدم القدرة على الإتصال بالبث الحي لـ iptv-org حالياً."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(searchQuery, selectedCategory, rawChannels) {
        var temp = rawChannels
        if (selectedCategory != "الكل") {
            temp = temp.filter { it.category == selectedCategory }
        }
        if (searchQuery.trim().isNotEmpty()) {
            temp = temp.filter { 
                it.channelName.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
        filteredChannels = temp
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Spacer(modifier = Modifier.height(30.dp))
        
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("قنوات البث المباشر العربية 📺", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("بث حي وديناميكي فائق السرعة عبر iptv-org", color = TextSec, fontSize = 11.sp)
            }
        }

        // Live Channel Search Input
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .fillMaxWidth()
                .background(CardColor, RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp, color = TealColor)
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = TextPri, fontSize = 13.sp),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("ابحث عن اسم القناة، الجزيرة، MBC، الرياضية...", color = TextSec, fontSize = 11.sp)
                    }
                    innerTextField()
                }
            )
            if (searchQuery.isNotEmpty()) {
                Text(
                    "×",
                    color = TextSec,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { searchQuery = "" }
                        .padding(horizontal = 4.dp)
                )
            }
        }

        // Categories selector horizontally scrollable rows
        if (categoriesList.size > 1) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesList) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) TealColor else CardColor)
                            .border(0.5.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(cat, color = if (isSelected) Color.White else TextSec, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid contents
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TealColor, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("جاري تحميل قنوات البث المباشر العربية الحية...", color = TextSec, fontSize = 11.sp)
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = RedColor, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else if (filteredChannels.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد قنوات مطابقة للتصفية حالياً", color = TextSec, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredChannels) { ch ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChannelSelect(ch) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.radialGradient(listOf(TealDeepColor, CardDeepColor)))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!ch.logoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ch.logoUrl,
                                    contentDescription = ch.channelName,
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0x2200A896), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ch.channelName.take(2).uppercase(),
                                        color = TealLightColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            // HLS flag indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .background(Color(0xB3000000), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("HLS", color = GoldColor, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ch.channelName,
                            color = TextPri,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = ch.category,
                            color = TextSec,
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// HEART FAVORITES SCREEN
// ═══════════════════════════════════════════════════════
@Composable
fun FavoritesScreen(onSeriesSelect: (Series) -> Unit) {
    val favs = SERIES_DATA.take(4) // Mock user favorites

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("قائمتي المفضلة", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))

        if (favs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", fontSize = 48.sp, color = TextSec)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("قائمتك فارغة حالياً", color = TextSec, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(favs) { s ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PosterSeriesCard(s = s, onClick = { onSeriesSelect(s) })
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = (4).dp)
                                .background(Color.Black.copy(0.6f), CircleShape)
                                .padding(4.dp)
                        ) {
                            Text("♥", color = TealLightColor, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// LOCAL SEARCH SCREEN
// ═══════════════════════════════════════════════════════
@Composable
fun SearchScreen(onSeriesSelect: (Series) -> Unit) {
    var query by remember { mutableStateOf("") }
    val matched = if (query.trim().length > 1) {
        SERIES_DATA.filter { it.title.contains(query, ignoreCase = true) || it.subTitle.contains(query, ignoreCase = true) }
    } else {
        emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("البحث السريع", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))

        // Search edit box
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .background(CardColor, RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp, color = TealColor)
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = TextPri, fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("اسم المسلسل، البلد أو المخرج...", color = TextSec, fontSize = 12.sp)
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Text(
                    "×",
                    color = TextSec,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { query = "" }
                        .padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (query.trim().length <= 1) {
            SectionHeader("الأكثر بحثاً")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(SERIES_DATA.take(3)) { s ->
                    PosterSeriesCard(s = s, onClick = { onSeriesSelect(s) })
                }
            }
        } else if (matched.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد مادة تطابق بحثك", color = TextSec, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(matched) { s ->
                    PosterSeriesCard(s = s, onClick = { onSeriesSelect(s) })
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// DETAILS PAGE COMPONENT
// ═══════════════════════════════════════════════════════
@Composable
fun SeriesDetailPage(series: Series, onBack: () -> Unit, onWatch: (Series, Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // Hero top image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(series.col))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, BgColor)))
                )

                // Top actions back
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 14.dp, end = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(0.4f), CircleShape)
                            .size(36.dp)
                    ) {
                        Text("←", color = TextPri, fontSize = 18.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🔔", "♡", "📤").forEach { ic ->
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .background(Color.Black.copy(0.4f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Text(ic, color = TextPri, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Inset title
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp, 120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardColor)
                            .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(series.title, color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(series.subTitle, color = TextSec, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Badge(series.badge, series.badge == "أصلي")
                            StatusBadgeWidget(series.status)
                            Text("⭐ 4.9", color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Story description synopsis
        item {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (series.story.isEmpty()) "شاهد جميع حلقات مسلسل ${series.title} الحصري والجديد في الحال بجودات مختلفة وسيرفرات تحميل ومقاطع مشاهدة من ميم بلس البث الذكي." else series.story,
                    color = TextSec,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(12.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onWatch(series, 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("▶ شاهد الآن الحلقة 1", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Actors Cast List
        item {
            SectionHeader("طاقم التمثيل")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val cast = listOf("كارلوس ميراندا", "جينا ديفيس", "ألفري وودارد", "ألفريد مولينا")
                items(cast) { name ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(CardDeepColor, CircleShape)
                                .border(1.dp, BorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👤", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name, color = TextPri, fontSize = 9.sp, maxLines = 1, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Episodes block
        item {
            SectionHeader("قائمة حلقات المسلسل")
        }

        items(series.totalEps) { index ->
            val ep = index + 1
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .clickable { onWatch(series, ep) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video scene simulated
                Box(
                    modifier = Modifier
                        .size(100.dp, 60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.verticalGradient(listOf(TealDeepColor, Color.Black))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", color = TealLightColor, fontSize = 14.sp)
                    Text("52:12", color = TextSec, fontSize = 8.sp, modifier = Modifier.align(Alignment.BottomStart).padding(2.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("الحلقة $ep - كاملة حصرياً", color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("تاريخ الإضافة: 2026-05-31", color = TextSec, fontSize = 9.sp)
                }
                Text("تحميل", color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ═══════════════════════════════════════════════════════
// CUSTOM SIMULATED PLAYER SCREEN WITH SUB DIRECT LINKS
// ═══════════════════════════════════════════════════════
@Composable
fun WatchPage(series: Series, epNum: Int, onBack: () -> Unit) {
    var playStatus by remember { mutableStateOf(true) }
    var userPosition by remember { mutableStateOf(35f) } // slider range

    LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // TOP CINEMATE BLOCK
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
                    .background(Color.Black)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎬 شاشة العرض والتشغيل والاتصال", color = TextSec, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("⏮", color = TextSec, fontSize = 24.sp)
                        IconButton(
                            onClick = { playStatus = !playStatus },
                            modifier = Modifier
                                .background(Color.White.copy(0.2f), CircleShape)
                                .size(50.dp)
                        ) {
                            Text(if (playStatus) "⏸" else "▶", color = Color.White, fontSize = 22.sp)
                        }
                        Text("⏭", color = TextSec, fontSize = 24.sp)
                    }
                }

                // Header back
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 24.dp, start = 14.dp)
                        .background(Color.Black.copy(0.5f), CircleShape)
                        .size(36.dp)
                ) {
                    Text("←", color = TextPri, fontSize = 18.sp)
                }
            }
        }

        // SEEK PROGRESS ROW
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDeepColor)
                    .padding(14.dp)
            ) {
                Slider(
                    value = userPosition,
                    onValueChange = { userPosition = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = TealColor, activeTrackColor = TealColor)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مباشر • 18:24", color = LiveColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("52:12", color = TextSec, fontSize = 11.sp)
                }
            }
        }

        // DIRECT DIRECT CHANNELS STREAMING LINKS
        item {
            SectionHeader("سيرفرات المشاهدة المباشرة")
            val servers = listOf("سيرفر رئيسي B2", "سيرفر احتياطي سريع HD", "سيرفر متعدد الجودات Auto")
            servers.forEach { srv ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .clickable {}
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(srv, color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (q in listOf("1080p", "720p")) {
                            Box(
                                modifier = Modifier
                                    .background(GoldDimColor, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, GoldColor.copy(0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(q, color = GoldColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // QUICK OFFLINE DOWNLOAD SYSTEMS
        item {
            SectionHeader("روابط التحميل المباشر")
            val sizes = listOf("452 MB", "230 MB")
            sizes.forEachIndexed { i, size ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⬇", color = TealLightColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("رابط تحميل مباشر HD", color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("الحجم المقدر: $size", color = TextSec, fontSize = 9.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300A896), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("تحميل مجاني", color = TealLightColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ═══════════════════════════════════════════════════════
// CUSTOM NAVIGATION BAR WIDGET
// ═══════════════════════════════════════════════════════
@Composable
fun BottomBar(activeTab: String, onTabSelect: (String) -> Unit) {
    val items = listOf(
        Triple("home", "🏠", "الرئيسية"),
        Triple("series", "🎬", "المسلسلات"),
        Triple("channels", "📺", "القنوات"),
        Triple("matches", "⚽", "المباريات"),
        Triple("fav", "♥", "المفضلة"),
        Triple("search", "🔍", "البحث الحي")
    )

    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .background(Color(0xFC080F0D))
            .border(0.5.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { (key, ic, lbl) ->
            val isActive = activeTab == key
            Column(
                modifier = Modifier
                    .clickable { onTabSelect(key) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = ic,
                    fontSize = 18.sp,
                    color = if (isActive) TealLightColor else TextSec,
                    modifier = Modifier.alpha(if (isActive) 1f else 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lbl,
                    color = if (isActive) TealLightColor else TextSec,
                    fontSize = 9.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TMDB INTEGRATED LIVE CONTROLLERS & VIEWS
// ═══════════════════════════════════════════════════════

@Composable
fun TmdbSearchScreen(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onWorkSelect: (mediaType: String, id: Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var activeType by remember { mutableStateOf("all") } // "all", "movie", "tv"
    var page by remember { mutableStateOf(1) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var itemsList by remember { mutableStateOf<List<TmdbTrendingItem>>(emptyList()) }
    var showKeyEditor by remember { mutableStateOf(false) }
    var tempKey by remember { mutableStateOf(apiKey) }

    val service = remember(apiKey) { TmdbService(apiKey) }

    // Fetch trending or query answers dynamically
    LaunchedEffect(query, activeType, page, apiKey) {
        isLoading = true
        errorMessage = null
        try {
            if (query.trim().length > 1) {
                val results = service.searchContent(query.trim(), page)
                itemsList = results
            } else {
                val results = service.fetchTrendingContent(activeType, page)
                itemsList = results
            }
        } catch (e: Exception) {
            errorMessage = "فشل الاتصال بخوادم TMDB الحية. يرجى التحقق من اتصال الإنترنت أو رمز الـ API Key."
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Spacer(modifier = Modifier.height(30.dp))
        
        // Premium Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("بوابة TMDB العالمية 🍿", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("البث والبحث الحي المباشر دون زيف", color = TextSec, fontSize = 11.sp)
            }
            
            // API CONFIG KEY TOGGLE INDICATOR
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (apiKey == "84f183e20e8d6411ab1b80c108169123") CardColor else Color(0x3300A896))
                    .border(0.5.dp, if (apiKey == "84f183e20e8d6411ab1b80c108169123") BorderColor else TealColor, RoundedCornerShape(8.dp))
                    .clickable { showKeyEditor = !showKeyEditor }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(if (apiKey == "84f183e20e8d6411ab1b80c108169123") "🔑 مفتاح تجريبي" else "🟢 مفتاح مخصص", color = TextPri, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Custom API Key Editor Overlay Panel
        if (showKeyEditor) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .fillMaxWidth()
                    .background(CardDeepColor, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("رمز اتصال TMDB API Key:", color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = TextPri, fontSize = 13.sp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (tempKey.trim().isNotEmpty()) {
                                onApiKeyChange(tempKey.trim())
                            }
                            showKeyEditor = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ وتحديث الاتصال", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            tempKey = "84f183e20e8d6411ab1b80c108169123"
                            onApiKeyChange("84f183e20e8d6411ab1b80c108169123")
                            showKeyEditor = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardColor),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("افتراضي", color = TextSec, fontSize = 11.sp)
                    }
                }
            }
        }

        // Search Input Bar
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth()
                .background(CardColor, RoundedCornerShape(12.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp, color = TealColor)
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = { 
                    query = it 
                    page = 1 // reset page on rewrite
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = TextPri, fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("ابحث عن أي فيلم ومسلسل عربي، أجنبي أو كوري...", color = TextSec, fontSize = 12.sp)
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Text(
                    "×",
                    color = TextSec,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { query = "" }
                        .padding(horizontal = 4.dp)
                )
            }
        }

        // Switch filter only available when query is empty (representing Trending explore menu)
        if (query.trim().length <= 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mediaFilters = listOf(
                    "all" to "الكل الرائج 🔥",
                    "movie" to "أفلام سينما 🎬",
                    "tv" to "مسلسلات تلفزيونية 📺"
                )
                mediaFilters.forEach { (key, title) ->
                    val isSelected = activeType == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) TealColor else CardColor)
                            .border(0.5.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(20.dp))
                            .clickable { 
                                activeType = key
                                page = 1
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(title, color = if (isSelected) Color.White else TextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Paging Dynamics panel at upper core
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("النتائج الحية صفحة $page", color = TextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(if (page > 1) CardColor else CardColor.copy(0.3f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, if (page > 1) BorderColor else BorderColor.copy(0.3f), RoundedCornerShape(8.dp))
                        .clickable(enabled = page > 1) { page-- }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("← السابق", color = if (page > 1) TextPri else TextSec.copy(0.5f), fontSize = 11.sp)
                }
                
                Box(
                    modifier = Modifier
                        .background(CardColor, RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .clickable { page++ }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("التالي →", color = TextPri, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Center content area
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TealColor, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("جاري الاتصال والتحصيل من TMDB...", color = TextSec, fontSize = 11.sp)
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage!!, color = RedColor, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { page = 1; query = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إعادة التهيئة والمحاولة", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        } else if (itemsList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لم يتم العثور على نتائج للتصفية المحددة", color = TextSec, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(itemsList) { s ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWorkSelect(s.mediaType, s.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardColor)
                                .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        ) {
                            // Loaded dynamically from tmdb via coil AsyncImage
                            AsyncImage(
                                model = s.posterUrl,
                                contentDescription = s.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Average Rating Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .background(Color(0xE60D1612), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐", fontSize = 8.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${s.rating}", color = GoldColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // MediaType Indicator Right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(if (s.mediaType == "movie") TealDeepColor else Color(0x99521252), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(if (s.mediaType == "movie") "فيلم" else "مسلسل", color = TextPri, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            // Release Year Label
                            if (s.releaseYear > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("${s.releaseYear}", color = TextPri, fontSize = 8.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = s.title,
                            color = TextPri,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = s.category,
                            color = TextSec,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TmdbDetailPage(
    mediaType: String,
    id: Int,
    apiKey: String,
    onBack: () -> Unit,
    onWatch: (TmdbWorkDetails, Int) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf<TmdbWorkDetails?>(null) }
    
    // Tab controller
    var activeTabSub by remember { mutableStateOf("info") } // "info" or "cast"

    val service = remember(apiKey) { TmdbService(apiKey) }

    LaunchedEffect(id, mediaType) {
        isLoading = true
        errorMessage = null
        try {
            val response = service.fetchDetails(mediaType, id)
            details = response
        } catch (e: Exception) {
            errorMessage = "عذراً! واجهنا صعوبة في جلب تفاصيل هذا العمل من TMDB."
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(BgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = TealColor, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("جاري سحب تفاصيل العمل والطاقم بالكامل...", color = TextSec, fontSize = 11.sp)
            }
        }
    } else if (errorMessage != null || details == null) {
        Box(modifier = Modifier.fillMaxSize().background(BgColor), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                Text("⚠️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(errorMessage ?: "حدث خطأ غير متوقع", color = RedColor, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = TealColor)) {
                    Text("الرجوع للخلف", color = Color.White)
                }
            }
        }
    } else {
        val detailWork = details!!
        
        LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
            // Hero Backdrop Image Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Actual dynamic premium tall poster inside background
                    AsyncImage(
                        model = detailWork.posterUrl,
                        contentDescription = detailWork.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, BgColor)))
                    )

                    // Navigation Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 14.dp, end = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .size(38.dp)
                        ) {
                            Text("←", color = TextPri, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("⭐ ${detailWork.rating}", color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Floating Card detail details over backdrop
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(75.dp, 110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardColor)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = detailWork.posterUrl,
                                contentDescription = "",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(detailWork.title, color = TextPri, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(TealGlowColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(detailWork.category, color = TealLightColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(CardColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(if (mediaType == "movie") "فيلم سينمائي" else "مسلسل تي في", color = TextSec, fontSize = 9.sp)
                                }
                                if (detailWork.releaseYear > 0) {
                                    Text("${detailWork.releaseYear}", color = TextSec, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Tabs toggle: Overview vs Cast
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val subTabs = listOf(
                        "info" to "قصة العمل والوصف 📖",
                        "cast" to "طاقم العمل والمبدعين 👥"
                    )
                    subTabs.forEach { (key, name) ->
                        val isSelected = activeTabSub == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) TealColor else CardColor, RoundedCornerShape(10.dp))
                                .border(0.5.dp, if (isSelected) Color.Transparent else BorderColor, RoundedCornerShape(10.dp))
                                .clickable { activeTabSub = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (isSelected) Color.White else TextSec, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dynamic Render based on Tab
            if (activeTabSub == "info") {
                // Overview Text
                item {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (detailWork.overview.isEmpty()) "لا يتوفر حالياً ملخص باللغة العربية." else detailWork.overview,
                            color = TextSec,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardColor, RoundedCornerShape(12.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        )

                        // Sub stats for seasons / episode counts (Requested details field for series vs movie!)
                        if (mediaType == "tv") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardDeepColor, RoundedCornerShape(10.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المواسم المعتمدة", color = TextSec, fontSize = 9.sp)
                                    Text("${detailWork.seasonsCount ?: 1} مواسم", color = TealLightColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("إجمالي الحلقات الحية", color = TextSec, fontSize = 9.sp)
                                    Text("${detailWork.episodesCount ?: 12} حلقة", color = GoldColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Trigger player
                        Button(
                            onClick = { onWatch(detailWork, 1) },
                            colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (mediaType == "movie") "▶ تشغيل ومشاهدة الفيلم فوراً" else "▶ عرض الحلقة الأولى الآن",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                // Cast and Crew List
                item {
                    SectionHeader("الإخراج وصناع العمل 🎬")
                }
                
                if (detailWork.directors.isEmpty()) {
                    item {
                        Text("مخرج العمل غير مدرج باللغة العربية", color = TextSec, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp))
                    }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(detailWork.directors) { d ->
                                Row(
                                    modifier = Modifier
                                        .background(CardColor, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(CardDeepColor)
                                    ) {
                                        if (d.profileUrl != null) {
                                            AsyncImage(
                                                model = d.profileUrl,
                                                contentDescription = d.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text("🎬", fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(d.name, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(d.role, color = TextSec, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SectionHeader("طاقم التمثيل البارز 👥")
                }

                if (detailWork.cast.isEmpty()) {
                    item {
                        Text("لا توجد تفاصيل طاقم تمثيل في TMDB لهذه اللغة", color = TextSec, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 14.dp))
                    }
                } else {
                    items(detailWork.cast) { actor ->
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                                .fillMaxWidth()
                                .background(CardColor, RoundedCornerShape(10.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CardDeepColor)
                                    .border(1.dp, BorderColor, CircleShape)
                            ) {
                                if (actor.profileUrl != null) {
                                    AsyncImage(
                                        model = actor.profileUrl,
                                        contentDescription = actor.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("👤", fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(actor.name, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("بدور: ${actor.role}", color = TextSec, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            // If it is a show, display episode blocks!
            if (mediaType == "tv" && activeTabSub == "info") {
                item {
                    SectionHeader("قائمة الحلقات الحية المتوفرة")
                }
                
                val episodesCount = detailWork.episodesCount ?: 12
                items(episodesCount) { index ->
                    val ep = index + 1
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .fillMaxWidth()
                            .background(CardColor, RoundedCornerShape(8.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable { onWatch(detailWork, ep) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp, 56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.verticalGradient(listOf(TealDeepColor, Color.Black))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = TealLightColor, fontSize = 14.sp)
                            Text("45:00", color = TextSec, fontSize = 8.sp, modifier = Modifier.align(Alignment.BottomStart).padding(2.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الحلقة $ep - كاملة عالية الدقة", color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("البث المباشر عبر السيرفر الذكي", color = TextSec, fontSize = 8.sp)
                        }
                        Text("مشاهدة", color = GoldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TmdbWatchPage(details: TmdbWorkDetails, epNum: Int, onBack: () -> Unit) {
    var playStatus by remember { mutableStateOf(true) }
    var userPosition by remember { mutableStateOf(10f) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // TOP CINEMATE BLOCKPLAYER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
                    .background(Color.Black)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎬 شاشة العرض والتشغيل والاتصال الذكي من ميم بلس", color = TextSec, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (details.seasonsCount != null) "${details.title} - الحلقة $epNum" else details.title,
                        color = TextPri,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("⏮", color = TextSec, fontSize = 24.sp)
                        IconButton(
                            onClick = { playStatus = !playStatus },
                            modifier = Modifier
                                .background(Color.White.copy(0.2f), CircleShape)
                                .size(50.dp)
                        ) {
                            Text(if (playStatus) "⏸" else "▶", color = Color.White, fontSize = 22.sp)
                        }
                        Text("⏭", color = TextSec, fontSize = 24.sp)
                    }
                }

                // Header back
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 24.dp, start = 14.dp)
                        .background(Color.Black.copy(0.5f), CircleShape)
                        .size(36.dp)
                ) {
                    Text("←", color = TextPri, fontSize = 18.sp)
                }
            }
        }

        // Seek Progress
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDeepColor)
                    .padding(14.dp)
            ) {
                Slider(
                    value = userPosition,
                    onValueChange = { userPosition = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = TealColor, activeTrackColor = TealColor)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مباشر • 45:00", color = LiveColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("HD Multi-Server", color = TextSec, fontSize = 11.sp)
                }
            }
        }

        // Live Direct Channels Streaming Links
        item {
            SectionHeader("سيرفرات البث واستقبال الإشارة الحية 📡")
            val servers = listOf("سيرفر ذكي رئيسي F1", "سيرفر البث العربي دبلجة", "سيرفر السحابة السريع CDN")
            servers.forEach { srv ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .clickable {}
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(srv, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (q in listOf("1080p", "720p", "4K")) {
                            Box(
                                modifier = Modifier
                                    .background(GoldDimColor, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, GoldColor.copy(0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(q, color = GoldColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Direct Download Links
        item {
            SectionHeader("روابط التحميل المباشر للأرشفة")
            val sizes = listOf("780 MB", "420 MB")
            sizes.forEach { size ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⬇", color = TealLightColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("رابط مباشر TMDB-HD Stream", color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("الحجم: $size", color = TextSec, fontSize = 9.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300A896), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("تحميل سريع", color = TealLightColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun useVideoPlayerEnhancements(
    context: android.content.Context,
    isFullscreen: Boolean,
    onOrientationChanged: (Boolean) -> Unit
): Float {
    // Helper function to safely find the Activity in ContextWrapper chain
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) break
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    // 1. Screen Wake Lock (Keep Screen On)
    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 2. Discover device orientation (auto landscape to fullscreen)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    LaunchedEffect(configuration.orientation) {
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        onOrientationChanged(isLandscape)
    }

    // 3. Programmatic requested orientation for fullscreen lock
    LaunchedEffect(isFullscreen, activity) {
        activity?.let {
            if (isFullscreen) {
                it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // 4. Load persisted volume (Local Storage / SharedPreferences)
    val sharedPrefs = remember(context) { context.getSharedPreferences("video_player_prefs", android.content.Context.MODE_PRIVATE) }
    return remember { sharedPrefs.getFloat("last_volume", 0.8f) }
}

@Composable
fun IptvWatchPage(channel: IptvChannel, onBack: () -> Unit) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var playStatus by remember { mutableStateOf(true) }
    
    // Support switching active channel dynamically for the Related Channels section
    var currentActiveChannel by remember(channel) { mutableStateOf(channel) }
    
    // Call Custom Hook to handle screen wake lock, orientation discovery and load last saved volume
    val savedVolume = useVideoPlayerEnhancements(
        context = context,
        isFullscreen = isFullscreen,
        onOrientationChanged = { isLandscape ->
            isFullscreen = isLandscape
        }
    )
    
    var userVolume by remember(savedVolume) { mutableStateOf(savedVolume) } // 0f to 1f
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorOccurred by remember { mutableStateOf(false) }

    // Multi-quality server links mapping (the plain URLs are completely hidden from the user UI)
    val streamUrl = currentActiveChannel.streamUrl
    val serversList = remember(streamUrl) {
        listOf(
            QualityServer("سيرفر مباشر 🚀 UHD", "البث الرئيسي بدقة خارقة للتردد", "4K", streamUrl),
            QualityServer("سيرفر سريع ⚡ FHD", "بث فائق الجودة متوافق مع كافة الشاشات", "1080p", 
                if (streamUrl.contains("bipbop")) "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
                else streamUrl
            ),
            QualityServer("سيرفر احتياطي 🛡️ HD", "بث متوسط الجودة وموفر للبيانات", "720p", "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"),
            QualityServer("سيرفر الجوال 📱 SD", "بث منخفض الاستهلاك ومثالي للشبكات الضعيفة", "480p", "https://playertest.longtailvideo.com/adaptive/elephants_dream/elephants_dream.m3u8")
        )
    }
    
    var selectedServerIndex by remember(streamUrl) { mutableIntStateOf(0) }
    val selectedPlayUrl = remember(serversList, selectedServerIndex) {
        serversList.getOrNull(selectedServerIndex)?.url ?: streamUrl
    }

    // High performance ExoPlayer, kept alive cross quality transitions by re-preparing instead of recreating!
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setTrackSelector(androidx.media3.exoplayer.trackselection.DefaultTrackSelector(context))
            .build().apply {
                playWhenReady = true
            }
    }

    // Connect to lifecycle & dynamic stream loading
    DisposableEffect(selectedPlayUrl) {
        val mediaItem = MediaItem.Builder()
            .setUri(selectedPlayUrl)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                playStatus = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                errorOccurred = state == Player.STATE_IDLE && exoPlayer.playerError != null
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                errorOccurred = true
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Release player on absolute screen exit
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Sync volume slider back directly with ExoPlayer API & persist in Local Storage
    LaunchedEffect(userVolume) {
        exoPlayer.volume = userVolume
        val sharedPrefs = context.getSharedPreferences("video_player_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putFloat("last_volume", userVolume).apply()
    }

    // Auto-hide controls effect
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    // Main layout container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Upper Spacer (if not fullscreen)
        if (!isFullscreen) {
            Spacer(modifier = Modifier.height(30.dp))
        }

        // Beautiful Video Section
        val videoModifier = if (isFullscreen) {
            Modifier.fillMaxSize().background(Color.Black)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.7f)
                .background(Color.Black)
        }

        Box(
            modifier = videoModifier
                .clickable { showControls = !showControls }
        ) {
            // Android ExoPlayer View container
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Custom transparent compose controls used instead!
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Spinner
            if (isBuffering) {
                CircularProgressIndicator(
                    color = TealLightColor,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .align(Alignment.Center)
                )
            }

            // Error Message Overlay
            if (errorOccurred) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "عذراً، فشل الاتصال بمصدر البث المباشر المختار حالياً.",
                        color = Color.Red,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Custom Translucent & Floating Controls overlay (Dark Mode matching)
            if (showControls || isBuffering || errorOccurred) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(0.7f),
                                    Color.Transparent,
                                    Color.Black.copy(0.7f)
                                )
                            )
                        )
                ) {
                    // Top Bar controls (Back, Channel details, Live stream)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 14.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (isFullscreen) {
                                    isFullscreen = false
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .size(36.dp)
                        ) {
                            Text("←", color = TextPri, fontSize = 16.sp)
                        }

                        // Title metadata
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = currentActiveChannel.channelName,
                                color = TextPri,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "دقة تلقائية: ${serversList[selectedServerIndex].badge} ⚙️",
                                color = TealLightColor,
                                fontSize = 9.sp
                            )
                        }

                        // Live Badge status
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مباشر 🔴", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Center big Play/Pause toggle
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.5f))
                            .clickable {
                                if (playStatus) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            }
                            .size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (playStatus) "⏸" else "▶",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }

                    // Bottom Bar controls: volume, seek progress (simulated seek since it is live), full screen toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Volume control & slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 12.dp)
                        ) {
                            Text(if (userVolume > 0f) "🔊" else "🔇", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = userVolume,
                                onValueChange = { userVolume = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = TealLightColor,
                                    activeTrackColor = TealLightColor,
                                    inactiveTrackColor = Color.White.copy(0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Full Screen control Button
                        IconButton(
                            onClick = { isFullscreen = !isFullscreen },
                            modifier = Modifier
                                .background(Color.Black.copy(0.4f), CircleShape)
                                .size(36.dp)
                        ) {
                            Text(
                                text = if (isFullscreen) "⛶" else "📺",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // If not fullscreen, show ordinary channels details & other specs under the player!
        if (!isFullscreen) {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                
                // Beautiful ambient channel display banner matching the screenshots
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(CardDeepColor, BgColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient trophy/sports background drawing
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = TealLightColor.copy(alpha = 0.04f),
                                radius = size.width * 0.45f,
                                center = Offset(size.width / 2, size.height * 0.4f)
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            // Glowing central logo card
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Brush.radialGradient(listOf(TealColor.copy(0.2f), Color.Transparent)))
                                    .border(1.dp, Brush.horizontalGradient(listOf(TealLightColor, GoldColor)), RoundedCornerShape(18.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!currentActiveChannel.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentActiveChannel.logoUrl,
                                        contentDescription = currentActiveChannel.channelName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(
                                        text = "🏆",
                                        fontSize = 32.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = "MiM Plus Premium Live Broadcast",
                                color = GoldColor.copy(alpha = 0.82f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Channel Name Headline
                item {
                    Text(
                        text = currentActiveChannel.channelName,
                        color = TextPri,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Decorative Statistics pills matching the screenshot layout exactly!
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Viewer counter pill: "22K 👁️"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardColor)
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("👁️", fontSize = 10.sp)
                                Text("22K", color = TextSec, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. Origin country pill: "قطر 🇶🇦"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardColor)
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📍", fontSize = 10.sp)
                                Text(
                                    text = if (currentActiveChannel.category.contains("الرياضة") || currentActiveChannel.category.contains("رياضة") || currentActiveChannel.channelName.contains("beIN")) "دولة قطر 🇶🇦" else "عربي 🌍", 
                                    color = TextSec, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3. Category pill: e.g., "رياضة"
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardColor)
                                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentActiveChannel.category.ifEmpty { "قنوات عامة" }, 
                                color = TextSec, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Row for "سيرفرات البث" header with custom red/themed vertical accent line on the side!
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سيرفرات البث",
                                color = TextPri,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "بث كلو جودة مختلفة ⚡",
                            color = TealLightColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Modern multi-quality server select grid items!
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        serversList.forEachIndexed { index, server ->
                            val isActive = selectedServerIndex == index
                            val cardBg = if (isActive) {
                                Brush.horizontalGradient(listOf(TealColor, TealDeepColor))
                            } else {
                                Brush.horizontalGradient(listOf(CardColor, CardColor))
                            }
                            val borderColor = if (isActive) TealLightColor else BorderColor
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cardBg)
                                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable { selectedServerIndex = index }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isActive) GoldColor else BorderColor)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = server.badge,
                                            color = if (isActive) BgColor else TextPri,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Title & description details
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = server.name,
                                            color = if (isActive) Color.White else TextPri,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = server.note,
                                            color = if (isActive) Color.White.copy(0.7f) else TextSec,
                                            fontSize = 10.sp
                                        )
                                    }
                                    
                                    // Trigger/Play icon on the right
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color.White else Color.White.copy(0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isActive && playStatus) "⏸" else "▶",
                                            color = if (isActive) TealColor else TextPri,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Related channels section matching the screenshot layout!
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "قنوات مرتبطة",
                            color = TextPri,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    // Filter matching/related channels dynamically to fill the horizontal scroll row
                    val relatedChannels = CHANNELS_DATA.filter { it.name != currentActiveChannel.channelName }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(relatedChannels) { ch ->
                            Column(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clickable {
                                        // Load the custom selected channel
                                        currentActiveChannel = IptvChannel(
                                            channelId = "ch_${ch.id}",
                                            channelName = ch.name,
                                            category = ch.cat,
                                            logoUrl = null,
                                            streamUrl = if (ch.id == 7) "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"
                                                       else "https://playertest.longtailvideo.com/adaptive/bipbop/bipbop.m3u8"
                                        )
                                        selectedServerIndex = 0
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CardColor)
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = ch.abbr,
                                            color = TealLightColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "LIVE",
                                            color = Color.Red,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ch.name,
                                    color = TextPri,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = ch.cat,
                                    color = TextSec,
                                    fontSize = 8.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Interactive Comments container matching the screenshot layout base!
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    var showCommentsSheet by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardDeepColor)
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable { showCommentsSheet = true }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("💬", fontSize = 14.sp)
                            Text("التعليقات والمناقشة الحية (8)", color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Interactive dialogue overlay showing comments dynamically in a premium bubble layout!
                    if (showCommentsSheet) {
                        AlertDialog(
                            onDismissRequest = { showCommentsSheet = false },
                            title = {
                                Text(
                                    text = "التعليقات والمناقشة الحية 💬",
                                    color = TextPri,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val mockComments = listOf(
                                        "أبو صهيب" to "البث شغال بصورة ممتازة وجودة UHD رائعة جداً! 👍🇸🇦",
                                        "كريم المصري" to "ألف شكر لتطبيق MiM Plus، أفضل بث مباشر بدون شاشات تقطيع مريح جداً.",
                                        "خالد العتيبي" to "سيرفر الـ 4K شغال طلقة للمباراة! حماس كبير 🔥🏆",
                                        "امين_77" to "جودة ممتازة والبث خالي من التقطيع والتشويش الفني.",
                                        "نور الهدى" to "أفضل تطبيق لمتابعة القنوات مباشر، تصميم رائع ومتناسق للغاية."
                                    )
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.height(250.dp)
                                    ) {
                                        items(mockComments) { (user, comment) ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(CardColor, RoundedCornerShape(8.dp))
                                                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(user, color = TealLightColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("الآن 🟢", color = LiveColor, fontSize = 8.sp)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(comment, color = TextPri, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { showCommentsSheet = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("إغلاق", color = Color.White, fontSize = 12.sp)
                                }
                            },
                            containerColor = CardDeepColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(84.dp)) }
            }
        }
    }
}

// Helper model to support multi-servers within watch screen
data class QualityServer(
    val name: String,
    val note: String,
    val badge: String,
    val url: String
)



