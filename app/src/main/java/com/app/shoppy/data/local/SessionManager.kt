package com.app.shoppy.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shoppy_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun getUserAvatar(): String? {
        return prefs.getString("user_avatar", null)
    }

    fun isTwoFactorEnabled(): Boolean {
        return prefs.getBoolean("two_factor_enabled", false)
    }

    fun saveUserSession(email: String, name: String, avatar: String? = null, twoFactorEnabled: Boolean = false) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_email", email)
            putString("user_name", name)
            if (avatar != null) {
                putString("user_avatar", avatar)
            }
            putBoolean("two_factor_enabled", twoFactorEnabled)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
