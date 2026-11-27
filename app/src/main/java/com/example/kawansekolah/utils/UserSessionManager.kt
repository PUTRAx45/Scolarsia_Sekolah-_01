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
    }

    fun saveSession(userName: String, role: UserRole, className: String = "", isSuperAdmin: Boolean = false) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, userName)
            putString(KEY_LOGIN_ROLE, role.name)
            putString(KEY_VIEW_AS_ROLE, role.name)
            putString(KEY_USER_CLASS, className)
            putBoolean(KEY_IS_SUPER_ADMIN, isSuperAdmin)
            commit() // Synchronous save
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
        prefs.edit().apply {
            putString(KEY_VIEW_AS_ROLE, role.name)
            commit()
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
            putStringSet("allowed_roles", roles.map { it.name }.toSet())
            commit()
        }
    }

    fun getAllowedRoles(): List<UserRole> {
        val set = prefs.getStringSet("allowed_roles", emptySet()) ?: emptySet()
        return set.mapNotNull { 
            try { UserRole.valueOf(it) } catch(e:Exception) { null } 
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            clear()
            commit() // Synchronous clear
        }
    }
}
