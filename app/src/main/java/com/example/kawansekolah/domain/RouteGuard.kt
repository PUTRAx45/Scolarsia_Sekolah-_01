package com.example.kawansekolah.domain

import com.example.kawansekolah.data.model.UserRole

object RouteGuard {

    val adminRoutes = setOf(
        "admin_beranda", "admin_pengguna", "admin_akademik",
        "admin_penugasan_guru", "admin_profil", "admin_detail_akun",
        "admin_daftar_akun", "admin_impor_csv"
    )

    val sharedAccountRoutes = setOf(
        "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
    )

    fun isRouteAllowedForRole(role: UserRole, route: String): Boolean {
        if (sharedAccountRoutes.contains(route)) return true

        val isAdminRoute = adminRoutes.contains(route)

        return when (role) {
            UserRole.ADMIN -> isAdminRoute
            UserRole.GURU -> !isAdminRoute && route in setOf("beranda", "nilai", "insight_page", "profil", "guru_input", "unggah_csv", "guru_dashboard")
            UserRole.WALI_KELAS -> !isAdminRoute && route in setOf("beranda", "nilai", "insight_page", "profil", "rekap_wali", "statistik")
            UserRole.SISWA -> !isAdminRoute && route in setOf("beranda", "nilai", "ai", "profil", "pelajaran")
            UserRole.ORANG_TUA -> !isAdminRoute && route in setOf("beranda", "nilai", "ai", "profil", "pelajaran")
        }
    }

    fun isRouteAllowedForAccount(
        loginRole: UserRole,
        activeViewRole: UserRole,
        allowedRoles: List<UserRole>,
        isSuperAdmin: Boolean,
        route: String
    ): Boolean {
        if (sharedAccountRoutes.contains(route)) return true

        val isAdminRoute = adminRoutes.contains(route)
        if (isAdminRoute) {
            return isSuperAdmin && loginRole == UserRole.ADMIN
        }

        if (isSuperAdmin && loginRole == UserRole.ADMIN) {
            return isRouteAllowedForRole(activeViewRole, route)
        }

        if (!allowedRoles.contains(activeViewRole)) {
            return false
        }

        return isRouteAllowedForRole(activeViewRole, route)
    }

    fun getHomeRouteForRole(role: UserRole): String {
        return when (role) {
            UserRole.ADMIN -> "admin_beranda"
            else -> "beranda"
        }
    }
}
