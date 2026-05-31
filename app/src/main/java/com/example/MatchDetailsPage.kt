package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun MatchDetailsPage(
    matchId: String,
    onBack: () -> Unit,
    onWatchChannel: (IptvChannel) -> Unit
) {
    val context = LocalContext.current
    val allMatches = SoccerManager.getMatches()
    val match = allMatches.firstOrNull { it.id == matchId } ?: SoccerManager.getMatches().first()

    // Interactive States
    var selectedTab by remember { mutableStateOf("فيديوهات") }
    var userVoteChoice by remember { mutableStateOf<String?>(null) }
    var commentText by remember { mutableStateOf("") }
    var selectedLineupTeam by remember { mutableStateOf("home") } // "home" or "away"
    var commentsList by remember { mutableStateOf(SoccerManager.getCommentsForMatch(match.id)) }
    var votesState by remember { mutableStateOf(SoccerManager.getVotesForMatch(match.id)) }

    // Navigation Tabs List
    val tabs = listOf("فيديوهات", "تفاصيل", "أحداث", "تشكيلة", "إحصائيات", "الجمهور")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(30.dp))

            // TOP NAVIGATION HEADER (MATCH BRIEF)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "عودة",
                        tint = TextPri
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.league,
                        color = TextPri,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = match.round,
                        color = TextSec,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(40.dp)) // symmetry Balance
            }

            // SCORE & CREST DISPLAY CARD (MATCH CARD DESIGN MATCHING SCREENSHOT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CardColor, BgColor)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(0.5.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Home Team Info
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White.copy(0.04f), CircleShape)
                                    .border(1.dp, BorderColor, CircleShape)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!match.homeLogo.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = match.homeLogo,
                                        contentDescription = match.home,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(match.hf, fontSize = 34.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.home,
                                color = TextPri,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // SCORE / CENTER INFORMATION
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = match.score,
                                color = TextPri,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            
                            // Penalties score if present
                            if (match.hPen >= 0 && match.aPen >= 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ركلات ", color = TextSec, fontSize = 11.sp)
                                    Text("${match.aPen} - ${match.hPen}", color = Color(0xFFD94F4F), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Match Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (match.status) {
                                            "مباشر" -> Color.Red.copy(0.2f)
                                            "انتهت" -> Color.White.copy(0.08f)
                                            else -> BorderColor
                                        }
                                    )
                                    .border(
                                        0.5.dp,
                                        when (match.status) {
                                            "مباشر" -> Color.Red.copy(0.6f)
                                            else -> Color.Transparent
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (match.isLive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.Red, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = if (match.isLive && match.elapsed > 0) "انتهت (ركلات)" else if (match.status == "انتهت" && match.hPen >= 0) "انتهت (ركلات)" else match.status,
                                        color = if (match.status == "مباشر") Color.Red else TextPri,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${match.time}  •  ${match.date}",
                                color = TextSec,
                                fontSize = 11.sp
                            )
                        }

                        // Away Team Info
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White.copy(0.04f), CircleShape)
                                    .border(1.dp, BorderColor, CircleShape)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!match.awayLogo.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = match.awayLogo,
                                        contentDescription = match.away,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text(match.af, fontSize = 34.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.away,
                                color = TextPri,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🏟️ ${match.stadium}  •  👤 ${match.referee}",
                        color = TextSec,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )

                    // LIVE BROADCAST LINK BUTTON If Match is Live or Has Stream URL!
                    val streamUrl = match.streamUrlStr ?: "https://dzair-one.com:8081/Bein_Sports_1/index.m3u8"
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val liveCh = IptvChannel(
                                channelId = "temp_${match.id}",
                                channelName = "البث المباشر: ${match.home} × ${match.away}",
                                category = "مباشر",
                                logoUrl = "https://img.icons8.com/color/96/play-live.png",
                                streamUrl = streamUrl
                            )
                            onWatchChannel(liveCh)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (match.isLive) Color.Red else TealColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (match.isLive) "مشاهدة البث المباشر 🎥 مباشر الآن" else "تصفح القناة الناقلة 📺 البث المعتمد",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HORIZONTAL GLASS-TAB SELECTOR
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { t ->
                    val isSelected = selectedTab == t
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CardColor else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) TealLightColor.copy(0.4f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = t }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = t,
                            color = if (isSelected) TealLightColor else TextSec,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))

            // TAB DETAIL CONTENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    "فيديوهات" -> VideosTab(match)
                    "تفاصيل" -> DetailsTab(match)
                    "أحداث" -> EventsTab(match)
                    "تشكيلة" -> LineupTab(
                        match = match,
                        currentSelectedTeam = selectedLineupTeam,
                        onTeamChange = { selectedLineupTeam = it }
                    )
                    "إحصائيات" -> StatsTab(match)
                    "الجمهور" -> FanClubTab(
                        match = match,
                        commentText = commentText,
                        onCommentChange = { commentText = it },
                        commentsList = commentsList,
                        votesInfo = votesState,
                        userVote = userVoteChoice,
                        onVoteChoice = { choice ->
                            SoccerManager.voteForMatch(context, match.id, choice)
                            votesState = SoccerManager.getVotesForMatch(match.id)
                            userVoteChoice = choice
                        },
                        onPostComment = {
                            if (commentText.trim().isNotEmpty()) {
                                SoccerManager.addCommentToMatch(context, match.id, "أنت (مشجع)", commentText)
                                commentsList = SoccerManager.getCommentsForMatch(match.id)
                                commentText = ""
                            }
                        }
                    )
                }
            }
        }
    }
}

// 🎬 VIDEOS TAB COMPONENT
@Composable
fun VideosTab(match: SoccerManager.MatchData) {
    val clips = match.videos.ifEmpty {
        listOf(
            SoccerManager.MatchVideo(
                title = "ملخص وأبرز لقطات مباراة ${match.home} ضد ${match.away}",
                description = "${match.home} × ${match.away}",
                duration = "10:15",
                tag = "هدف",
                views = "240",
                timeAgo = "منذ 3 ساعات"
            ),
            SoccerManager.MatchVideo(
                title = "الأهداف الكاملة وتغطية الجماهير الحية للمباراة",
                description = "${match.home} × ${match.away}",
                duration = "5:30",
                tag = "هدف",
                views = "182",
                timeAgo = "منذ 4 ساعات"
            )
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(clips) { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(12.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video visual thumbnail representation with overlay
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 74.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Styled overlay details representing YouTube / Player
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▶️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Red.copy(0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(c.tag.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Timestamp tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(c.duration, color = Color.White, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Detail descriptive text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = c.title,
                        color = TextPri,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = c.description,
                        color = TextSec,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👁️ ${c.views} مشاهدة", color = TextSec, fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🕒 ${c.timeAgo}", color = TextSec, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

// 📚 DETAILS TAB COMPONENT
@Composable
fun DetailsTab(match: SoccerManager.MatchData) {
    val items = listOf(
        Pair("البطولة", match.league),
        Pair("الجولة", match.round),
        Pair("الفريق المضيف", match.home),
        Pair("الفريق الضيف", match.away),
        Pair("التاريخ", match.date),
        Pair("الساعة", match.time),
        Pair("الملعب", match.stadium),
        Pair("الحكم", match.referee),
        Pair("القنوات الناقلة", match.ch),
        Pair("المعلق الكروي", match.commentator)
    )

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { (lbl, valStr) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(10.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(lbl, color = TextSec, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(valStr, color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ⚡ TIMELINE EVENTS TAB COMPONENT
@Composable
fun EventsTab(match: SoccerManager.MatchData) {
    val timeline = match.events.ifEmpty {
        listOf(
            SoccerManager.MatchEvent("90'", "goal_scored", match.home, "هدف قاتل للمباراة بالتعادل د.90 ⚽"),
            SoccerManager.MatchEvent("72'", "card_yellow", match.away, "بطاقة صفراء للإلتحام القوي 🟨"),
            SoccerManager.MatchEvent("45'", "substitution", match.home, "بديل في الاستراحة بين الشوطين 🔄"),
            SoccerManager.MatchEvent("15'", "goal_scored", match.away, "أول أهداف اللقاء د.15 لـ ${match.away} ⚽")
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(timeline) { ev ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minute Circle Tag
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CardColor)
                        .border(1.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ev.minute, color = TealLightColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                // Styled Connection Timeline Line
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .drawBehind {
                            drawLine(
                                color = BorderColor,
                                start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                )

                // Event description box
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(CardColor, RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(ev.player, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(ev.detail, color = TextSec, fontSize = 9.sp)
                    }
                    // Visual graphics representation depending on type
                    Box {
                        when (ev.type) {
                            "goal_scored" -> Text("⚽", fontSize = 18.sp)
                            "goal_missed" -> Text("❌⚽", fontSize = 14.sp)
                            "card_yellow" -> Box(modifier = Modifier.size(width = 12.dp, height = 18.dp).background(Color(0xFFFFD166), RoundedCornerShape(1.dp)))
                            "card_red" -> Box(modifier = Modifier.size(width = 12.dp, height = 18.dp).background(Color.Red, RoundedCornerShape(1.dp)))
                            "substitution" -> Text("🔄", fontSize = 16.sp)
                            else -> Text("⚡", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// 🟩 FIELD ROSTER LINEUPS TAB COMPONENT
@Composable
fun LineupTab(
    match: SoccerManager.MatchData,
    currentSelectedTeam: String,
    onTeamChange: (String) -> Unit
) {
    // Formation rosters (representing PSG or Arsenal/etc.)
    val homeLineup = listOf(
        Triple("بيلينجهام", "10", "وسط"),
        Triple("فينيسيوس جونيور", "7", "هجوم"),
        Triple("فالفيردي", "15", "وسط"),
        Triple("كامافينجا", "12", "دفاع"),
        Triple("رودريجو", "11", "هجوم"),
        Triple("تشواميني", "18", "وسط"),
        Triple("روديجر", "22", "دفاع"),
        Triple("ميليتاو", "3", "دفاع"),
        Triple("ميندي", "23", "دفاع"),
        Triple("كارفاخال", "2", "دفاع"),
        Triple("كورتوا", "1", "حارس")
    )

    val awayLineup = listOf(
        Triple("هالاند", "9", "هجوم"),
        Triple("دي بروين", "17", "وسط"),
        Triple("فودين", "47", "هجوم"),
        Triple("بيرناردو سيلفا", "20", "وسط"),
        Triple("رودري", "16", "وسط"),
        Triple("كوفاسيتش", "8", "وسط"),
        Triple("جفارديول", "24", "دفاع"),
        Triple("روبن دياز", "3", "دفاع"),
        Triple("جون ستونز", "5", "دفاع"),
        Triple("أكانجي", "25", "دفاع"),
        Triple("إيدرسون", "31", "حارس")
    )

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Toggle Buttons Home/Away
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDeepColor, RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (currentSelectedTeam == "home") CardColor else Color.Transparent)
                    .border(0.5.dp, if (currentSelectedTeam == "home") BorderColor else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onTeamChange("home") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(match.home, color = if (currentSelectedTeam == "home") TealLightColor else TextSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (currentSelectedTeam == "away") CardColor else Color.Transparent)
                    .border(0.5.dp, if (currentSelectedTeam == "away") BorderColor else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onTeamChange("away") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(match.away, color = if (currentSelectedTeam == "away") TealLightColor else TextSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Green Pitch Representation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF143026), Color(0xFF0D211A))
                    )
                )
                .border(1.5.dp, Color(0xFF1F4D3C), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Lines & Circles Drawing Representing Pitch
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Outer Box Line
                drawRect(Color(0xFF1F4D3C).copy(0.4f), style = Stroke(2f))
                // Center Line
                drawLine(
                    color = Color(0xFF1F4D3C).copy(0.5f),
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 2f
                )
                // Center circle
                drawCircle(
                    color = Color(0xFF1F4D3C).copy(0.5f),
                    radius = 36.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
                    style = Stroke(2f)
                )
            }

            // Quick Interactive Lineup preview list
            val currentRoster = if (currentSelectedTeam == "home") homeLineup else awayLineup
            Row(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row displays GK, 2 Defs, 2 Midfields, 1 Fwd
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerBadge("حارس", "1")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PlayerBadge("دفاع", "3")
                    PlayerBadge("دفاع", "4")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PlayerBadge("وسط", "8")
                    PlayerBadge("وسط", "10")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerBadge("هجوم", "9")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Complete detailed Roster list view below
        Text("قائمة اللاعبين (الأساسي)", color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        val completeList = if (currentSelectedTeam == "home") homeLineup else awayLineup
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(completeList) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(TealDeepColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(p.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(p.first, color = TextPri, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(p.third, color = TextSec, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PlayerBadge(name: String, nr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Brush.linearGradient(listOf(TealColor, TealDeepColor)), CircleShape)
                .border(1.dp, Color.White.copy(0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(nr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(name, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Medium)
    }
}

// 🎚️ STATS TAB COMPONENT WITH RED PROGRESS BARS
@Composable
fun StatsTab(match: SoccerManager.MatchData) {
    // Exact statistics fields mirroring screenshots
    val statsList = listOf(
        Triple("الاستحواذ", 75, 25), // PSG 75% vs Arsenal 25% (Second match was 25% vs 75% in the third screenshot!)
        Triple("إجمالي التسديدات", 21, 7),
        Triple("تسديدات على المرمى", 4, 1),
        Triple("تسديدات خارج المرمى", 12, 1),
        Triple("تسديدات محجوبة", 5, 5),
        Triple("تسديدات داخل المنطقة", 12, 5),
        Triple("تسديدات خارج المنطقة", 9, 2),
        Triple("الركنيات", 11, 3)
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(statsList) { (title, valHome, valAway) ->
            val total = valHome + valAway
            val valHPercentage = if (total > 0) (valHome.toFloat() / total.toFloat()) else 0.5f
            val valAPercentage = if (total > 0) (valAway.toFloat() / total.toFloat()) else 0.5f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home value (e.g., PSG)
                    Text(
                        text = if (title == "الاستحواذ") "$valHome%" else valHome.toString(),
                        color = TextPri,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Title
                    Text(title, color = TextSec, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    
                    // Away value (e.g., Arsenal)
                    Text(
                        text = if (title == "الاستحواذ") "$valAway%" else valAway.toString(),
                        color = TextPri,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dual progress representing values (Sleek red progress bars inside screenshots!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left Progress Bar (Representing Home side)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BorderColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(valHPercentage)
                                .background(Color.Red)
                                .align(Alignment.CenterEnd) // Fill right-to-center
                        )
                    }

                    // Right Progress Bar (Representing Away side)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(BorderColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(valAPercentage)
                                .background(BorderColor.copy(0.4f)) // Away grey bar or red if custom
                                .background(Color.Red.copy(0.3f))
                        )
                    }
                }
            }
        }
    }
}

// 🗳️ FANS CORNER & COMMENTS TAB COMPONENT (الجمهور)
@Composable
fun FanClubTab(
    match: SoccerManager.MatchData,
    commentText: String,
    onCommentChange: (String) -> Unit,
    commentsList: List<Triple<String, String, String>>,
    votesInfo: Triple<Int, Int, Int>,
    userVote: String?,
    onVoteChoice: (String) -> Unit,
    onPostComment: () -> Unit
) {
    val totalVotes = votesInfo.first + votesInfo.second + votesInfo.third
    val pctHome = if (totalVotes > 0) (votesInfo.first * 100 / totalVotes) else 50
    val pctDraw = if (totalVotes > 0) (votesInfo.second * 100 / totalVotes) else 15
    val pctAway = if (totalVotes > 0) (votesInfo.third * 100 / totalVotes) else 35

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // MATCH PREDICTION VOTE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(12.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("توقعك للمباراة", color = TextPri, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("صوّت لتوقعك لمجريات هذه المواجهة الحماسية!", color = TextSec, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (userVote == null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Home Vote
                            Button(
                                onClick = { onVoteChoice("home") },
                                colors = ButtonDefaults.buttonColors(containerColor = CardDeepColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            ) {
                                Text(match.home, color = TextPri, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }

                            // Draw Vote
                            Button(
                                onClick = { onVoteChoice("draw") },
                                colors = ButtonDefaults.buttonColors(containerColor = CardDeepColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            ) {
                                Text("تعادل", color = TextPri, fontSize = 10.sp)
                            }

                            // Away Vote
                            Button(
                                onClick = { onVoteChoice("away") },
                                colors = ButtonDefaults.buttonColors(containerColor = CardDeepColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            ) {
                                Text(match.away, color = TextPri, fontSize = 10.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        // Voted State - Show Sleek Colored Percentage bars
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(TealColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تم التصويت بنجاح!", color = TealColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom triple slider bar (Representing poll output ratio)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BorderColor),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Home %
                                Box(
                                    modifier = Modifier
                                        .weight(pctHome.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$pctHome%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                // Draw %
                                Box(
                                    modifier = Modifier
                                        .weight(pctDraw.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(Color(0xFFFFD166)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$pctDraw%", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                // Away %
                                Box(
                                    modifier = Modifier
                                        .weight(pctAway.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(TealColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$pctAway%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔴 ${match.home}: $pctHome%", color = TextSec, fontSize = 9.sp)
                                Text("🟡 تعادل: $pctDraw%", color = TextSec, fontSize = 9.sp)
                                Text("🟢 ${match.away}: $pctAway%", color = TextSec, fontSize = 9.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("إجمالي التصويت: $totalVotes صوت", color = TextSec, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        // COMMENTS LIST WITH CUSTOM INPUT ROW
        item {
            Text(
                text = "تعليقات الجمهور (${commentsList.size})",
                color = TextPri,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Custom Post Comment Input Fields
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(12.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentChange,
                    placeholder = { Text("اكتب تعليقك الآن لتشارك الجمهور...", color = TextSec, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onPostComment,
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("نشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Styled historical fans comment list view
        items(commentsList) { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardColor, RoundedCornerShape(12.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Styled hexagonal avatar shape
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(TealDeepColor, Color(0xFF0D1C16)))),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (c.first.isNotEmpty()) c.first.substring(0, 1) else "م"
                    Text(initial, color = TealLightColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(c.first, color = TextPri, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(c.third, color = TextSec, fontSize = 8.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(c.second, color = TextPri, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    // Reply tag button inside screenshot
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("↩️ رد", color = TextSec, fontSize = 8.sp, modifier = Modifier.clickable { })
                    }
                }
            }
        }
    }
}
