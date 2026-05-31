package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

// ═══════════════════════════════════════════════════════
// DATA CLASSES MATCHING THE TS SERVICE
// ═══════════════════════════════════════════════════════
data class TmdbTrendingItem(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val rating: Double,
    val category: String,
    val releaseYear: Int,
    val mediaType: String // "movie" or "tv"
)

data class TmdbCastMember(
    val id: Int,
    val name: String,
    val role: String,
    val profileUrl: String?
)

data class TmdbWorkDetails(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val rating: Double,
    val releaseYear: Int,
    val category: String,
    val seasonsCount: Int?, // null for movie
    val episodesCount: Int?, // null for movie
    val cast: List<TmdbCastMember>,
    val directors: List<TmdbCastMember>
)

class TmdbService(private val apiKey: String) {
    private val client = OkHttpClient()
    private val baseUrl = "https://api.themoviedb.org/3"
    private val imageBaseUrl = "https://image.tmdb.org/t/p"

    private val genreMap = mapOf(
        28 to "أكشن",
        12 to "مغامرة",
        16 to "رسوم متحركة",
        35 to "كوميديا",
        80 to "جريمة",
        99 to "وثائقي",
        18 to "دراما",
        10751 to "عائلي",
        14 to "فانتازيا",
        36 to "تاريخ",
        27 to "رعب",
        10402 to "موسيقى",
        9648 to "غموض",
        10749 to "رومانسي",
        878 to "خيال علمي",
        10770 to "تلفزيوني",
        53 to "إثارة",
        10752 to "حرب",
        37 to "غرب أمريكي",
        10759 to "أكشن ومغامرة",
        10762 to "أطفال",
        10763 to "أخبار",
        10764 to "واقعي",
        10765 to "خيال وفانتازيا",
        10766 to "أوبرا صابونية",
        10767 to "برنامج حواري",
        10768 to "حرب وسياسة"
    )

    private fun getPosterUrl(path: String?, size: String = "w500"): String {
        return if (!path.isNullOrEmpty() && path != "null") {
            "$imageBaseUrl/$size$path"
        } else {
            "https://placehold.co/500x750/091e1a/ffffff?text=No+Poster"
        }
    }

    private fun getProfileUrl(path: String?): String? {
        return if (!path.isNullOrEmpty() && path != "null") {
            "$imageBaseUrl/w185$path"
        } else {
            null
        }
    }

    suspend fun fetchTrendingContent(
        mediaType: String = "all", // "all", "movie", "tv"
        page: Int = 1
    ): List<TmdbTrendingItem> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/trending/$mediaType/week?api_key=$apiKey&page=$page&language=ar"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to load TMDB trending: raw code ${response.code}")
                val bodyString = response.body?.string() ?: return@use emptyList()
                val json = JSONObject(bodyString)
                val results = json.optJSONArray("results") ?: return@use emptyList()
                val list = mutableListOf<TmdbTrendingItem>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val id = item.optInt("id")
                    
                    val title = item.optString("title").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("name").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("original_title").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("original_name").takeIf { it.isNotEmpty() && it != "null" }
                        ?: "عنوان غير معروف"
                        
                    val releaseDate = item.optString("release_date").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("first_air_date").takeIf { it.isNotEmpty() && it != "null" }
                        ?: ""
                    val releaseYear = if (releaseDate.length >= 4) {
                        try { releaseDate.substring(0, 4).toInt() } catch (e: Exception) { 0 }
                    } else {
                        0
                    }

                    val genreIds = item.optJSONArray("genre_ids")
                    val firstGenreId = if (genreIds != null && genreIds.length() > 0) genreIds.getInt(0) else -1
                    val category = genreMap[firstGenreId] ?: "منوع"

                    val posterPath = item.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" }
                    val rating = item.optDouble("vote_average", 0.0)
                    val itemMediaType = item.optString("media_type", if (mediaType == "all") "movie" else mediaType)

                    list.add(
                        TmdbTrendingItem(
                            id = id,
                            title = title,
                            posterUrl = getPosterUrl(posterPath, "w500"),
                            rating = String.format("%.1f", rating).toDoubleOrNull() ?: 0.0,
                            category = category,
                            releaseYear = releaseYear,
                            mediaType = itemMediaType
                        )
                    )
                }
                return@use list
            }
        } catch (e: Exception) {
            Log.e("TmdbService", "Error in fetchTrendingContent", e)
            throw e
        }
    }

    suspend fun searchContent(
        query: String,
        page: Int = 1
    ): List<TmdbTrendingItem> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/search/multi?api_key=$apiKey&query=$encodedQuery&page=$page&language=ar"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to search TMDB: raw code ${response.code}")
                val bodyString = response.body?.string() ?: return@use emptyList()
                val json = JSONObject(bodyString)
                val results = json.optJSONArray("results") ?: return@use emptyList()
                val list = mutableListOf<TmdbTrendingItem>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val id = item.optInt("id")
                    
                    val itemMediaType = item.optString("media_type", "movie")
                    if (itemMediaType != "movie" && itemMediaType != "tv") continue // Skip people
                    
                    val title = item.optString("title").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("name").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("original_title").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("original_name").takeIf { it.isNotEmpty() && it != "null" }
                        ?: "عنوان غير معروف"
                        
                    val releaseDate = item.optString("release_date").takeIf { it.isNotEmpty() && it != "null" }
                        ?: item.optString("first_air_date").takeIf { it.isNotEmpty() && it != "null" }
                        ?: ""
                    val releaseYear = if (releaseDate.length >= 4) {
                        try { releaseDate.substring(0, 4).toInt() } catch (e: Exception) { 0 }
                    } else {
                        0
                    }

                    val genreIds = item.optJSONArray("genre_ids")
                    val firstGenreId = if (genreIds != null && genreIds.length() > 0) genreIds.getInt(0) else -1
                    val category = genreMap[firstGenreId] ?: "منوع"

                    val posterPath = item.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" }
                    val rating = item.optDouble("vote_average", 0.0)

                    list.add(
                        TmdbTrendingItem(
                            id = id,
                            title = title,
                            posterUrl = getPosterUrl(posterPath, "w500"),
                            rating = String.format("%.1f", rating).toDoubleOrNull() ?: 0.0,
                            category = category,
                            releaseYear = releaseYear,
                            mediaType = itemMediaType
                        )
                    )
                }
                return@use list
            }
        } catch (e: Exception) {
            Log.e("TmdbService", "Error in searchContent", e)
            throw e
        }
    }

    suspend fun fetchDetails(
        mediaType: String, // "movie" or "tv"
        id: Int
    ): TmdbWorkDetails = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/$mediaType/$id?api_key=$apiKey&append_to_response=credits&language=ar"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to load details for $id: raw code ${response.code}")
                val bodyString = response.body?.string() ?: throw IOException("Empty body")
                val data = JSONObject(bodyString)

                val title = data.optString("title").takeIf { it.isNotEmpty() && it != "null" }
                    ?: data.optString("name").takeIf { it.isNotEmpty() && it != "null" }
                    ?: data.optString("original_title").takeIf { it.isNotEmpty() && it != "null" }
                    ?: data.optString("original_name").takeIf { it.isNotEmpty() && it != "null" }
                    ?: "عمل فني"

                val releaseDate = data.optString("release_date").takeIf { it.isNotEmpty() && it != "null" }
                    ?: data.optString("first_air_date").takeIf { it.isNotEmpty() && it != "null" }
                    ?: ""
                val releaseYear = if (releaseDate.length >= 4) {
                    try { releaseDate.substring(0, 4).toInt() } catch (e: Exception) { 0 }
                } else {
                    0
                }

                // Get category from genres array
                val genresArr = data.optJSONArray("genres")
                val category = if (genresArr != null && genresArr.length() > 0) {
                    genresArr.getJSONObject(0).optString("name", "منوع")
                } else {
                    "منوع"
                }

                val castList = mutableListOf<TmdbCastMember>()
                val directList = mutableListOf<TmdbCastMember>()

                val credits = data.optJSONObject("credits")
                if (credits != null) {
                    val castArr = credits.optJSONArray("cast")
                    if (castArr != null) {
                        val limit = minOf(castArr.length(), 10)
                        for (i in 0 until limit) {
                            val actor = castArr.getJSONObject(i)
                            castList.add(
                                TmdbCastMember(
                                    id = actor.optInt("id"),
                                    name = actor.optString("name", "غير معروف"),
                                    role = actor.optString("character", "ممثل"),
                                    profileUrl = getProfileUrl(actor.optString("profile_path").takeIf { it.isNotEmpty() && it != "null" })
                                )
                            )
                        }
                    }

                    val crewArr = credits.optJSONArray("crew")
                    if (crewArr != null) {
                        for (i in 0 until crewArr.length()) {
                            val crew = crewArr.getJSONObject(i)
                            val job = crew.optString("job")
                            val dept = crew.optString("department")
                            if (job == "Director" || job == "Executive Producer" || dept == "Directing") {
                                directList.add(
                                    TmdbCastMember(
                                        id = crew.optInt("id"),
                                        name = crew.optString("name", "غير معروف"),
                                        role = job.takeIf { it.isNotEmpty() } ?: "مخرج",
                                        profileUrl = getProfileUrl(crew.optString("profile_path").takeIf { it.isNotEmpty() && it != "null" })
                                    )
                                )
                            }
                        }
                    }
                }

                val seasonsCount = if (mediaType == "tv") data.optInt("number_of_seasons", 1) else null
                val episodesCount = if (mediaType == "tv") data.optInt("number_of_episodes", 12) else null

                val posterPath = data.optString("poster_path").takeIf { it.isNotEmpty() && it != "null" }
                val rating = data.optDouble("vote_average", 0.0)

                return@use TmdbWorkDetails(
                    id = data.optInt("id"),
                    title = title,
                    overview = data.optString("overview", "لا يوجد وصف متوفر باللغة العربية لهذا العمل حالياً."),
                    posterUrl = getPosterUrl(posterPath, "original"),
                    rating = String.format("%.1f", rating).toDoubleOrNull() ?: 0.0,
                    releaseYear = releaseYear,
                    category = category,
                    seasonsCount = seasonsCount,
                    episodesCount = episodesCount,
                    cast = castList,
                    directors = directList.take(3)
                )
            }
        } catch (e: Exception) {
            Log.e("TmdbService", "Error in fetchDetails", e)
            throw e
        }
    }
}
