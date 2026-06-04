package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object MyFirebaseManager {
    private const val TAG = "MyFirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyAKuXowKNURE_Zmvp0GLU3Qlf74sbhJ_Pw")
                .setApplicationId("1:68938107461:web:578e606097d09a0afb712c") // Using the web credentials in Android programmatic mode
                .setProjectId("mimpro-3b84d")
                .setStorageBucket("mimpro-3b84d.firebasestorage.app")
                .build()

            val apps = try {
                FirebaseApp.getApps(context)
            } catch (t: Throwable) {
                emptyList()
            }

            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "Firebase pre-configured and initialized successfully.")
            } else {
                Log.d(TAG, "Firebase App already exists.")
            }
            isInitialized = true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize custom FirebaseApp: ${e.message}", e)
        }
    }

    fun getAuth(): FirebaseAuth? {
        return if (isInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "FirebaseAuth.getInstance() failed. Check dependencies.", e)
                null
            }
        } else {
            null
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return getAuth()?.currentUser
    }

    fun isUserAdmin(): Boolean {
        val user = getCurrentUser() ?: return false
        val email = user.email ?: return false
        return isEmailAdmin(email)
    }

    fun isEmailAdmin(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        // Admin email list: alqaidpro@gmail.com or admin@mimplus.com
        return cleanEmail == "alqaidpro@gmail.com" || cleanEmail == "admin@mimplus.com"
    }

    fun logout() {
        try {
            getAuth()?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Failed signing out user.", e)
        }
    }
}
