package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AdminDashboardPage(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeSubSection by remember { mutableStateOf("matches") } // "matches" or "channels" or "simulator" or "series"

    // Refreshed lists
    var matchesList by remember { mutableStateOf(SoccerManager.getMatches()) }
    var channelsList by remember { mutableStateOf(SoccerManager.getChannels()) }
    var seriesList by remember { mutableStateOf(SoccerManager.getSeries()) }

    // Dialog Control For Adding/Editing Matches
    var showMatchDialog by remember { mutableStateOf(false) }
    var editingMatch by remember { mutableStateOf<SoccerManager.MatchData?>(null) }

    // Dialog Control For Channels
    var showChannelDialog by remember { mutableStateOf(false) }
    var editingChannel by remember { mutableStateOf<SoccerManager.AdminChannel?>(null) }

    // Dialog Control For Series
    var showSeriesDialog by remember { mutableStateOf(false) }
    var editingSeries by remember { mutableStateOf<Series?>(null) }

    // State Variables for Match Inputs
    var mLeague by remember { mutableStateOf("") }
    var mHome by remember { mutableStateOf("") }
    var mAway by remember { mutableStateOf("") }
    var mScore by remember { mutableStateOf("") }
    var mStatus by remember { mutableStateOf("لم تبدأ") } // "لم تبدأ", "مباشر", "انتهت"
    var mHf by remember { mutableStateOf("⚽") }
    var mAf by remember { mutableStateOf("⚽") }
    var mCh by remember { mutableStateOf("beIN Sports HD") }
    var mRef by remember { mutableStateOf("طاقم حكام معتمد") }
    var mStadium by remember { mutableStateOf("ملعب القاهرة الدولي") }
    var mCommentator by remember { mutableStateOf("عصام الشوالي") }
    var mStreamUrl by remember { mutableStateOf("") }
    var mElapsed by remember { mutableStateOf(0) }
    var mRound by remember { mutableStateOf("الأسبوع 1") }
    var mDate by remember { mutableStateOf("2026-05-31") }
    var mTime by remember { mutableStateOf("21:00") }

    // State Variables for Channel Inputs
    var cName by remember { mutableStateOf("") }
    var cLogo by remember { mutableStateOf("") }
    var cStreamUrl by remember { mutableStateOf("") }
    var cCategory by remember { mutableStateOf("رياضة") }

    // State Variables for Series Inputs
    var sTitle by remember { mutableStateOf("") }
    var sSubTitle by remember { mutableStateOf("") }
    var sEp by remember { mutableStateOf(1) }
    var sBadge by remember { mutableStateOf("مترجم") }
    var sAge by remember { mutableStateOf("+13") }
    var sGenre by remember { mutableStateOf("دراما") }
    var sYear by remember { mutableStateOf(2026) }
    var sCountry by remember { mutableStateOf("تركيا") }
    var sStatus by remember { mutableStateOf("يعرض الآن") }
    var sTotalEps by remember { mutableStateOf(10) }
    var sViews by remember { mutableStateOf(100) }
    var sStory by remember { mutableStateOf("") }

    // Re-sync lists from persistence
    fun refreshData() {
        matchesList = SoccerManager.getMatches()
        channelsList = SoccerManager.getChannels()
        seriesList = SoccerManager.getSeries()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(30.dp))

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "تراجع", tint = TextPri)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("لوحة تحكم الإدارة 🔐", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("تحكم كامل في القنوات وبث المباريات والـ API المخصص", color = TextSec, fontSize = 11.sp)
                }
            }

            // TOP HORIZONTAL METRICS STATUS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("مباريات مسجلة", color = TextSec, fontSize = 10.sp)
                        Text(matchesList.size.toString(), color = TealLightColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("قنوات البث المباشر", color = TextSec, fontSize = 10.sp)
                        Text(channelsList.size.toString(), color = GoldColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("مباريات مباشرة حالياً", color = TextSec, fontSize = 10.sp)
                        Text(matchesList.count { it.isLive }.toString() + " بث حي ⚡", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // DASHBOARD SUB-SECTION TOGGLE SELECTOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .background(CardDeepColor, RoundedCornerShape(12.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp)
            ) {
                val subTabs = listOf(
                    Triple("matches", "⚽ المباريات", "إدارة مباريات الدوري والبطولات"),
                    Triple("channels", "📺 قنوات البث", "تعديل وإضافة قنوات IPTV"),
                    Triple("series", "🎬 الأفلام والمسلسلات", "إدارة الأفلام والمسلسلات"),
                    Triple("simulator", "⚡ المحاكي", "محاكاة وتسجيل الأهداف")
                )
                subTabs.forEach { (key, lbl, desc) ->
                    val isSelected = activeSubSection == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CardColor else Color.Transparent)
                            .border(0.5.dp, if (isSelected) BorderColor else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeSubSection = key }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lbl, color = if (isSelected) TealLightColor else TextSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VIEW BASED ON SECTION
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeSubSection) {
                    "matches" -> {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("قائمة المباريات المتاحة", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        editingMatch = null
                                        mLeague = SoccerManager.SUPPORTED_LEAGUES.first()
                                        mHome = ""
                                        mAway = ""
                                        mScore = "0 - 0"
                                        mStatus = "لم تبدأ"
                                        mHf = "⚽"
                                        mAf = "⚽"
                                        mCh = "beIN Sports HD 1"
                                        mRef = "طاقم حكام معتمد"
                                        mStadium = "ملعب القاهرة الدولي"
                                        mCommentator = "عصام الشوالي"
                                        mStreamUrl = ""
                                        mElapsed = 0
                                        mRound = "الأسبوع الأول"
                                        mDate = "2026-05-31"
                                        mTime = "21:00"
                                        showMatchDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إضافة مباراة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(matchesList) { m ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CardColor, RoundedCornerShape(12.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .background(if (m.isLive) Color.Red else Color.Gray, CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(m.league + " • " + m.round, color = TextSec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${m.hf} ${m.home} × ${m.away} ${m.af}", color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("النتيجة: ${m.score} (${m.status})", color = TealLightColor, fontSize = 11.sp)
                                        }

                                        // Actions Buttons
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingMatch = m
                                                    mLeague = m.league
                                                    mHome = m.home
                                                    mAway = m.away
                                                    mScore = m.score
                                                    mStatus = m.status
                                                    mHf = m.hf
                                                    mAf = m.af
                                                    mCh = m.ch
                                                    mRef = m.referee
                                                    mStadium = m.stadium
                                                    mCommentator = m.commentator
                                                    mStreamUrl = m.streamUrlStr ?: ""
                                                    mElapsed = m.elapsed
                                                    mRound = m.round
                                                    mDate = m.date
                                                    mTime = m.time
                                                    showMatchDialog = true
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = GoldColor, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    SoccerManager.deleteMatch(context, m.id)
                                                    refreshData()
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "channels" -> {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إدارة قنوات الـ IPTV الحالية", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        editingChannel = null
                                        cName = ""
                                        cLogo = ""
                                        cStreamUrl = ""
                                        cCategory = "رياضة"
                                        showChannelDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("قناة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(channelsList) { ch ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CardColor, RoundedCornerShape(12.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color.White.copy(0.04f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("📺", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ch.name, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("الرابط: " + ch.streamUrl.take(30) + "...", color = TextSec, fontSize = 9.sp)
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingChannel = ch
                                                    cName = ch.name
                                                    cLogo = ch.logoUrl ?: ""
                                                    cStreamUrl = ch.streamUrl
                                                    cCategory = ch.category
                                                    showChannelDialog = true
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = GoldColor, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    SoccerManager.deleteChannel(context, ch.id)
                                                    refreshData()
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "series" -> {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إدارة الأفلام والمسلسلات بقاعدة البيانات", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        editingSeries = null
                                        sTitle = ""
                                        sSubTitle = ""
                                        sEp = 1
                                        sBadge = "مترجم"
                                        sAge = "+13"
                                        sGenre = "دراما"
                                        sYear = 2026
                                        sCountry = "تركيا"
                                        sStatus = "يعرض الآن"
                                        sTotalEps = 10
                                        sViews = 100
                                        sStory = ""
                                        showSeriesDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مسلسل / فيلم جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(seriesList) { s ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CardColor, RoundedCornerShape(12.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(Color.White.copy(0.04f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎬", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(s.title, color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("${s.country} • ${s.genre} • سنة ${s.year} • (${s.status})", color = TextSec, fontSize = 10.sp)
                                            Text("${s.badge} • دفعة ${s.ep} حلقة • المشاهدات: ${s.views}", color = TealLightColor, fontSize = 10.sp)
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingSeries = s
                                                    sTitle = s.title
                                                    sSubTitle = s.subTitle
                                                    sEp = s.ep
                                                    sBadge = s.badge
                                                    sAge = s.age
                                                    sGenre = s.genre
                                                    sYear = s.year
                                                    sCountry = s.country
                                                    sStatus = s.status
                                                    sTotalEps = s.totalEps
                                                    sViews = s.views
                                                    sStory = s.story
                                                    showSeriesDialog = true
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = GoldColor, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    SoccerManager.deleteSeries(context, s.id)
                                                    refreshData()
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "simulator" -> {
                        // Interactive score/event simulators
                        LazyColumn(
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardColor, RoundedCornerShape(12.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text("أدوات التحكم السريع في المحاكاة ⚡", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("اختر مباراة مباشرة لتحديث نتيجتها في الحال وتسجيل أحداث جديدة بها:", color = TextSec, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(14.dp))

                                        val liveMatches = matchesList.filter { it.isLive }
                                        if (liveMatches.isEmpty()) {
                                            Text("⚠️ لا يوجد أي مباريات جارية مفعّلة في وضع البث المباشر المجهّز حالياً.", color = GoldColor, fontSize = 11.sp)
                                        } else {
                                            liveMatches.forEach { lm ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CardDeepColor, RoundedCornerShape(8.dp))
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("${lm.home} ${lm.score} ${lm.away}", color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text("الدقيقة الحالية: د.${lm.elapsed}", color = TextSec, fontSize = 10.sp)
                                                    }

                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Button(
                                                            onClick = {
                                                                // Home Goal Sim
                                                                val parts = lm.score.split("-").map { it.trim() }
                                                                val hScore = (parts.firstOrNull()?.toIntOrNull() ?: 0) + 1
                                                                val aScore = parts.lastOrNull()?.toIntOrNull() ?: 0
                                                                val newScore = "$hScore - $aScore"
                                                                val updatedEv = lm.events.toMutableList()
                                                                val updatedVideo = lm.videos.toMutableList()
                                                                
                                                                updatedEv.add(0, SoccerManager.MatchEvent("${lm.elapsed}'", "goal_scored", lm.home, "هدف رائع لصالح ${lm.home} 🥅⚽"))
                                                                updatedVideo.add(
                                                                    0,
                                                                    SoccerManager.MatchVideo("هدف مميز لصالح ${lm.home} د.${lm.elapsed}'", "${lm.home} × ${lm.away}", "1:00", "هدف", "125", "الآن")
                                                                )
                                                                
                                                                SoccerManager.saveMatch(context, lm.copy(score = newScore, events = updatedEv, videos = updatedVideo))
                                                                refreshData()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text("+ هدف لصاحب الأرض", fontSize = 9.sp)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                // Away Goal Sim
                                                                val parts = lm.score.split("-").map { it.trim() }
                                                                val hScore = parts.firstOrNull()?.toIntOrNull() ?: 0
                                                                val aScore = (parts.lastOrNull()?.toIntOrNull() ?: 0) + 1
                                                                val newScore = "$hScore - $aScore"
                                                                val updatedEv = lm.events.toMutableList()
                                                                val updatedVideo = lm.videos.toMutableList()
                                                                
                                                                updatedEv.add(0, SoccerManager.MatchEvent("${lm.elapsed}'", "goal_scored", lm.away, "هدف رائع لصالح ${lm.away} 🥅⚽"))
                                                                updatedVideo.add(
                                                                    0,
                                                                    SoccerManager.MatchVideo("هدف مميز لصالح ${lm.away} د.${lm.elapsed}'", "${lm.home} × ${lm.away}", "1:00", "هدف", "95", "الآن")
                                                                )
                                                                
                                                                SoccerManager.saveMatch(context, lm.copy(score = newScore, events = updatedEv, videos = updatedVideo))
                                                                refreshData()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text("+ هدف للضيف", fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardColor, RoundedCornerShape(12.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text("الضبط العام وإعادة التهيئة", color = TextPri, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("أعد كافة التعديلات والتغييرات التي قمت بها للمباريات والقنوات إلى قيمها الافتراضية المصممة:", color = TextSec, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                SoccerManager.resetAllData(context)
                                                refreshData()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🧹 مسح التغييرات التلقائية واستعادة الافتراضية", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- MATCH DIALOG (ADD/EDIT DIALOG) ---
        if (showMatchDialog) {
            AlertDialog(
                onDismissRequest = { showMatchDialog = false },
                title = { Text(if (editingMatch == null) "إضافة مباراة جدولة جديدة" else "تعديل تفاصيل المباراة", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dropdown selection for Leagues
                        Text("البطولة / الدوري", color = TextSec, fontSize = 11.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardDeepColor, RoundedCornerShape(8.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .clickable { /* Default dropdown simulation */ }
                        ) {
                            Text(mLeague, color = TextPri, fontSize = 13.sp)
                        }

                        // Team Inputs
                        OutlinedTextField(
                            value = mHome,
                            onValueChange = { mHome = it },
                            label = { Text("الفريق صاحب الأرض (المضيف)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mAway,
                            onValueChange = { mAway = it },
                            label = { Text("الفريق الضيف", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mScore,
                            onValueChange = { mScore = it },
                            label = { Text("النتيجة (مثال: 2 - 1)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = mRound,
                            onValueChange = { mRound = it },
                            label = { Text("الجولة / الأسبوع", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Status
                        Text("حالة المباراة الحالية", color = TextSec, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("لم تبدأ", "مباشر", "انتهت").forEach { s ->
                                val active = mStatus == s
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { mStatus = s }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(s, color = if (active) TealLightColor else TextPri, fontSize = 10.sp)
                                }
                            }
                        }

                        if (mStatus == "مباشر") {
                            OutlinedTextField(
                                value = mElapsed.toString(),
                                onValueChange = { mElapsed = it.toIntOrNull() ?: 0 },
                                label = { Text("الدقيقة الحالية للمباراة", fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = mCommentator,
                            onValueChange = { mCommentator = it },
                            label = { Text("المعلق الكروي العربي", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = mStadium,
                            onValueChange = { mStadium = it },
                            label = { Text("ملعب المباراة / الاستاد", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = mStreamUrl,
                            onValueChange = { mStreamUrl = it },
                            label = { Text("رابط البث الحي (HLS / m3u8) الاختياري", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = editingMatch?.id ?: "m_custom_${System.currentTimeMillis()}"
                            val newMatch = SoccerManager.MatchData(
                                id = id,
                                league = mLeague,
                                home = mHome,
                                away = mAway,
                                score = mScore,
                                status = mStatus,
                                hf = mHf,
                                af = mAf,
                                isLive = mStatus == "مباشر",
                                ch = mCh,
                                homeLogo = editingMatch?.homeLogo,
                                awayLogo = editingMatch?.awayLogo,
                                elapsed = mElapsed,
                                round = mRound,
                                date = mDate,
                                time = mTime,
                                referee = mRef,
                                commentator = mCommentator,
                                stadium = mStadium,
                                streamUrlStr = if (mStreamUrl.trim().isNotEmpty()) mStreamUrl else null,
                                events = editingMatch?.events ?: emptyList(),
                                videos = editingMatch?.videos ?: emptyList()
                            )
                            SoccerManager.saveMatch(context, newMatch)
                            refreshData()
                            showMatchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                    ) {
                        Text("حفظ البيانات")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMatchDialog = false }) { Text("إلغاء") }
                },
                containerColor = CardColor
            )
        }

        // --- CHANNEL DIALOG ---
        if (showChannelDialog) {
            AlertDialog(
                onDismissRequest = { showChannelDialog = false },
                title = { Text(if (editingChannel == null) "إضافة قناة بث IPTV جديدة" else "تعديل بيانات قناة البث", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cName,
                            onValueChange = { cName = it },
                            label = { Text("اسم القناة", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cStreamUrl,
                            onValueChange = { cStreamUrl = it },
                            label = { Text("رابط البث (HLS / m3u8 format)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cLogo,
                            onValueChange = { cLogo = it },
                            label = { Text("رابط شعار القناة (URL الاختياري)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = editingChannel?.id ?: "ch_custom_${System.currentTimeMillis()}"
                            val newCh = SoccerManager.AdminChannel(
                                id = id,
                                name = cName,
                                logoUrl = if (cLogo.trim().isNotEmpty()) cLogo else null,
                                streamUrl = cStreamUrl,
                                category = cCategory
                            )
                            SoccerManager.saveChannel(context, newCh)
                            refreshData()
                            showChannelDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                    ) {
                        Text("حفظ القناة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChannelDialog = false }) { Text("إلغاء") }
                },
                containerColor = CardColor
            )
        }

        // --- SERIES DIALOG ---
        if (showSeriesDialog) {
            AlertDialog(
                onDismissRequest = { showSeriesDialog = false },
                title = { Text(if (editingSeries == null) "إضافة مسلسل / فيلم جديد" else "تعديل تفاصيل المسلسل / الفيلم", color = TextPri, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = sTitle,
                            onValueChange = { sTitle = it },
                            label = { Text("عنوان العمل (بالعربية)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = sSubTitle,
                            onValueChange = { sSubTitle = it },
                            label = { Text("العنوان الفرعي أو الأصلي (إنجليزي)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Badge: مترجم vs أصلي
                        Text("النوع البرمجي (شارة)", color = TextSec, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("مترجم", "أصلي").forEach { b ->
                                val active = sBadge == b
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { sBadge = b }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(b, color = if (active) TealLightColor else TextPri, fontSize = 11.sp)
                                }
                            }
                        }

                        // Status: يعرض الآن vs مكتمل
                        Text("حالة العرض والإنتاج", color = TextSec, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("يعرض الآن", "مكتمل").forEach { st ->
                                val active = sStatus == st
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { sStatus = st }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(st, color = if (active) TealLightColor else TextPri, fontSize = 11.sp)
                                }
                            }
                        }

                        // Inputs for numbers: year, current ep, total eps, views
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sYear.toString(),
                                onValueChange = { sYear = it.toIntOrNull() ?: 2026 },
                                label = { Text("عام الإنتاج", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sViews.toString(),
                                onValueChange = { sViews = it.toIntOrNull() ?: 0 },
                                label = { Text("عدد المشاهدات", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sEp.toString(),
                                onValueChange = { sEp = it.toIntOrNull() ?: 1 },
                                label = { Text("الحلقة المتاحة", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sTotalEps.toString(),
                                onValueChange = { sTotalEps = it.toIntOrNull() ?: 10 },
                                label = { Text("إجمالي الحلقات", fontSize = 10.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Genres selector
                        Text("تصنيف المحتوى (النوع)", color = TextSec, fontSize = 11.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("أكشن", "دراما", "فانتازيا", "تشويق وإثارة", "كوميديا", "وثائقي", "رومانسي").forEach { g ->
                                val active = sGenre == g
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { sGenre = g }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(g, color = if (active) TealLightColor else TextPri, fontSize = 10.sp)
                                }
                            }
                        }

                        // Countries selector
                        Text("دولة الإنتاج (تحدد القسم بالصفحة الرئيسية)", color = TextSec, fontSize = 11.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("مصر", "تركيا", "كوريا", "أمريكا", "السعودية", "الإمارات", "لبنان", "إسبانيا", "اليابان").forEach { c ->
                                val active = sCountry == c
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { sCountry = c }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(c, color = if (active) TealLightColor else TextPri, fontSize = 10.sp)
                                }
                            }
                        }

                        // Age selector
                        Text("الفئة العمرية مناسبة لـ", color = TextSec, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("الجميع", "+13", "+16", "+18").forEach { a ->
                                val active = sAge == a
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) TealDeepColor else CardDeepColor)
                                        .border(0.5.dp, if (active) TealColor else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { sAge = a }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(a, color = if (active) TealLightColor else TextPri, fontSize = 10.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = sStory,
                            onValueChange = { sStory = it },
                            label = { Text("قصة الفيلم / المسلسل (تفاصيل)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealColor, focusedTextColor = TextPri, unfocusedTextColor = TextPri),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = editingSeries?.id ?: ((System.currentTimeMillis() % 100000).toInt() + 10000)
                            val colVal = when (sCountry) {
                                "مصر" -> 0xFF0E1A2E
                                "تركيا" -> 0xFF351F10
                                "كوريا" -> 0xFF4A0A2F
                                "أمريكا" -> 0xFF0E3320
                                else -> 0xFF0D2E28
                            }
                            val newSer = Series(
                                id = id,
                                title = sTitle,
                                subTitle = sSubTitle,
                                ep = sEp,
                                badge = sBadge,
                                age = sAge,
                                genre = sGenre,
                                year = sYear,
                                country = sCountry,
                                status = sStatus,
                                col = colVal,
                                totalEps = sTotalEps,
                                views = sViews,
                                story = sStory
                            )
                            SoccerManager.saveSeries(context, newSer)
                            refreshData()
                            showSeriesDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                    ) {
                        Text("حفظ الفيلم / المسلسل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSeriesDialog = false }) { Text("إلغاء") }
                },
                containerColor = CardColor
            )
        }
    }
}
