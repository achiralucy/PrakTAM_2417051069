package com.example.praktam_2417051069.auth

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun register(name: String, email: String, password: String): Boolean {
        if (prefs.contains("user_email_$email")) return false
        prefs.edit()
            .putString("user_email_$email", email)
            .putString("user_password_$email", password)
            .putString("user_name_$email", name)
            .apply()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val storedPassword = prefs.getString("user_password_$email", null)
        return if (storedPassword == password) {
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("current_user_email", email)
                .apply()
            true
        } else false
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("current_user_email")
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun getCurrentUserEmail(): String = prefs.getString("current_user_email", "") ?: ""

    fun getCurrentUserName(): String {
        val email = getCurrentUserEmail()
        return prefs.getString("user_name_$email", "User") ?: "User"
    }

    fun updateName(newName: String) {
        val email = getCurrentUserEmail()
        prefs.edit().putString("user_name_$email", newName).apply()
    }

    fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val email = getCurrentUserEmail()
        val stored = prefs.getString("user_password_$email", null)
        return if (stored == oldPassword) {
            prefs.edit().putString("user_password_$email", newPassword).apply()
            true
        } else false
    }
}
