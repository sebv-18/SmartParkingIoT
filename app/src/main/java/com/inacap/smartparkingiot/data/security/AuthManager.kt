package com.inacap.smartparkingiot.data.security

import android.content.Context
import java.security.MessageDigest

class AuthManager(context: Context) {

    private val prefs = context.getSharedPreferences("smart_parking_auth", Context.MODE_PRIVATE)

    init {
        if (!prefs.contains(KEY_ADMIN_USER)) {
            prefs.edit()
                .putString(KEY_ADMIN_USER, "admin")
                .putString(KEY_ADMIN_PASS_HASH, sha256("Admin2026!"))
                .apply()
        }
    }

    fun authenticate(username: String, password: String): Boolean {
        val cleanUser = username.trim()
        val storedUser = prefs.getString(KEY_ADMIN_USER, null) ?: return false
        val storedHash = prefs.getString(KEY_ADMIN_PASS_HASH, null) ?: return false
        return cleanUser == storedUser && sha256(password) == storedHash
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_ADMIN_USER = "admin_user"
        private const val KEY_ADMIN_PASS_HASH = "admin_pass_hash"
    }
}