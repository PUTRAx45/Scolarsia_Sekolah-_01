package com.example.kawansekolah.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.kawansekolah.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "kawan_sekolah_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LOGIN_ROLE = "login_role"
        private const val KEY_VIEW_AS_ROLE = "view_as_role"
        private const val KEY_USER_CLASS = "user_class"
        private const val KEY_IS_SUPER_ADMIN = "is_super_admin"
        private const val KEY_ALLOWED_ROLES = "allowed_roles"
        private const val KEY_EMAIL_HISTORY = "login_email_history"
    }

    fun saveSession(userName: String, role: UserRole, className: String = "", isSuperAdmin: Boolean = false) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, userName)
            putString(KEY_LOGIN_ROLE, role.name)
            putString(KEY_VIEW_AS_ROLE, role.name)
            putString(KEY_USER_CLASS, className)
            putBoolean(KEY_IS_SUPER_ADMIN, isSuperAdmin)
            commit()
        }
    }

    fun getUserClass(): String {
        return prefs.getString(KEY_USER_CLASS, "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && Firebase.auth.currentUser != null
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Pengguna") ?: "Pengguna"
    }

    fun getUserRole(): UserRole {
        val roleName = prefs.getString(KEY_VIEW_AS_ROLE, prefs.getString(KEY_LOGIN_ROLE, UserRole.SISWA.name))
        return try {
            UserRole.valueOf(roleName ?: UserRole.SISWA.name)
        } catch (_: Exception) {
            UserRole.SISWA
        }
    }

    fun getLoginRole(): UserRole {
        val roleName = prefs.getString(KEY_LOGIN_ROLE, UserRole.SISWA.name)
        return try {
            UserRole.valueOf(roleName ?: UserRole.SISWA.name)
        } catch (_: Exception) {
            UserRole.SISWA
        }
    }

    fun setViewAsRole(role: UserRole) {
        val loginRole = getLoginRole()
        val allowed = getAllowedRoles()
        val isSuper = isSuperAdmin()

        if ((isSuper && loginRole == UserRole.ADMIN) || allowed.contains(role)) {
            prefs.edit().apply {
                putString(KEY_VIEW_AS_ROLE, role.name)
                commit()
            }
        }
    }

    fun clearViewAsRole() {
        prefs.edit().apply {
            val loginRole = prefs.getString(KEY_LOGIN_ROLE, UserRole.SISWA.name)
            putString(KEY_VIEW_AS_ROLE, loginRole)
            commit()
        }
    }

    fun isSuperAdmin(): Boolean {
        return prefs.getBoolean(KEY_IS_SUPER_ADMIN, false)
    }

    fun saveAllowedRoles(roles: List<UserRole>) {
        prefs.edit().apply {
            putStringSet(KEY_ALLOWED_ROLES, roles.map { it.name }.toSet())
            commit()
        }
    }

    fun getAllowedRoles(): List<UserRole> {
        val set = prefs.getStringSet(KEY_ALLOWED_ROLES, emptySet()) ?: emptySet()
        val result = set.mapNotNull {
            try { UserRole.valueOf(it) } catch (e: Exception) { null }
        }
        return if (result.isNotEmpty()) result else listOf(getLoginRole())
    }

    fun saveEmailHistory(email: String) {
        if (email.isBlank()) return
        val currentSet = prefs.getStringSet(KEY_EMAIL_HISTORY, emptySet()) ?: emptySet()
        val updatedSet = currentSet.toMutableSet().apply { add(email.trim().lowercase()) }
        prefs.edit().apply {
            putStringSet(KEY_EMAIL_HISTORY, updatedSet)
            commit()
        }
    }

    fun getEmailHistory(): List<String> {
        val set = prefs.getStringSet(KEY_EMAIL_HISTORY, emptySet()) ?: emptySet()
        return set.toList()
    }

    fun clearSession() {
        val emailHistory = prefs.getStringSet(KEY_EMAIL_HISTORY, emptySet()) ?: emptySet()
        prefs.edit().apply {
            clear()
            putStringSet(KEY_EMAIL_HISTORY, emailHistory)
            commit()
        }
    }
}
