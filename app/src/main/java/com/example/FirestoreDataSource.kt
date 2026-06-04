package com.example

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class FirestoreChannel(
    val id: String = "",
    val name: String = "",
    val cat: String = "",
    val country: String = "",
    val logo: String = "",
    val streamUrl: String = "",
    val active: Boolean = true,
    val featured: Boolean = false
)

data class FirestoreMatch(
    val id: String = "",
    val league: String = "",
    val home: String = "",
    val away: String = "",
    val score: String = "",
    val time: String = "",
    val day: String = "",
    val status: String = "",
    val isLive: Boolean = false,
    val hFlag: String = "⚽",
    val aFlag: String = "⚽",
    val links: List<Map<String, String>> = emptyList()
)

object FirestoreDataSource {
    private const val TAG = "FirestoreDataSource"
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    // ═══════════════════════════════════
    // القنوات من Firestore (مرة واحدة)
    // ═══════════════════════════════════
    
    suspend fun getChannels(): List<FirestoreChannel> {
        return try {
            val snapshot = db.collection("channels")
                .whereEqualTo("active", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                FirestoreChannel(
                    id = doc.getString("id") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    cat = doc.getString("cat") ?: "",
                    country = doc.getString("country") ?: "",
                    logo = doc.getString("logo") ?: "",
                    streamUrl = doc.getString("streamUrl") ?: "",
                    active = doc.getBoolean("active") ?: true,
                    featured = doc.getBoolean("featured") ?: false
                )
            }.also {
                Log.d(TAG, "جلبت ${it.size} قناة من Firestore ✓")
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في جلب القنوات: ${e.message}")
            emptyList()
        }
    }

    // ═══════════════════════════════════
    // المباريات من Firestore (مرة واحدة)
    // ═══════════════════════════════════
    
    suspend fun getMatches(): List<FirestoreMatch> {
        return try {
            val snapshot = db.collection("matches")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                @Suppress("UNCHECKED_CAST")
                val linksData = doc.get("links") as? List<Map<String, String>> ?: emptyList()
                
                FirestoreMatch(
                    id = doc.getString("id") ?: doc.id,
                    league = doc.getString("league") ?: "",
                    home = doc.getString("home") ?: "",
                    away = doc.getString("away") ?: "",
                    score = doc.getString("score") ?: "- : -",
                    time = doc.getString("time") ?: "",
                    day = doc.getString("day") ?: "",
                    status = doc.getString("status") ?: "لم تبدأ",
                    isLive = doc.getBoolean("isLive") ?: false,
                    hFlag = doc.getString("hFlag") ?: "⚽",
                    aFlag = doc.getString("aFlag") ?: "⚽",
                    links = linksData
                )
            }.also {
                Log.d(TAG, "جلبت ${it.size} مباراة من Firestore ✓")
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في جلب المباريات: ${e.message}")
            emptyList()
        }
    }

    // ═══════════════════════════════════
    // القنوات - تحديث تلقائي (Real-time)
    // ═══════════════════════════════════
    
    fun getChannelsRealtime(onUpdate: (List<FirestoreChannel>) -> Unit) {
        db.collection("channels")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "خطأ في المستمع: ${error.message}")
                    return@addSnapshotListener
                }
                
                val channels = snapshot?.documents?.mapNotNull { doc ->
                    FirestoreChannel(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        cat = doc.getString("cat") ?: "",
                        logo = doc.getString("logo") ?: "",
                        streamUrl = doc.getString("streamUrl") ?: "",
                        active = doc.getBoolean("active") ?: true,
                        featured = doc.getBoolean("featured") ?: false,
                        country = doc.getString("country") ?: ""
                    )
                } ?: emptyList()
                
                onUpdate(channels)
            }
    }

    // ═══════════════════════════════════
    // المباريات - تحديث تلقائي (Real-time)
    // ═══════════════════════════════════
    
    fun getMatchesRealtime(onUpdate: (List<FirestoreMatch>) -> Unit) {
        db.collection("matches")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "خطأ في المستمع: ${error.message}")
                    return@addSnapshotListener
                }
                
                val matches = snapshot?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val linksData = doc.get("links") as? List<Map<String, String>> ?: emptyList()
                    
                    FirestoreMatch(
                        id = doc.getString("id") ?: doc.id,
                        league = doc.getString("league") ?: "",
                        home = doc.getString("home") ?: "",
                        away = doc.getString("away") ?: "",
                        score = doc.getString("score") ?: "",
                        time = doc.getString("time") ?: "",
                        day = doc.getString("day") ?: "",
                        status = doc.getString("status") ?: "",
                        isLive = doc.getBoolean("isLive") ?: false,
                        hFlag = doc.getString("hFlag") ?: "⚽",
                        aFlag = doc.getString("aFlag") ?: "⚽",
                        links = linksData
                    )
                } ?: emptyList()
                
                onUpdate(matches)
            }
    }
}
