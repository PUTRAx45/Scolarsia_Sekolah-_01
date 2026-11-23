package com.example.kawansekolah

import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AuthRepository
import com.example.kawansekolah.domain.RouteGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGuardMatrixUnitTest {

    private val allRoutes = listOf(
        "beranda", "pelajaran", "ai", "nilai", "profil",
        "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi",
        "guru_input", "unggah_csv", "guru_dashboard",
        "rekap_wali", "statistik", "insight_page",
        "admin_beranda", "admin_pengguna", "admin_akademik",
        "admin_penugasan_guru", "admin_profil", "admin_detail_akun",
        "admin_daftar_akun", "admin_impor_csv"
    )

    @Test
    fun `1 Matrix Test - ADMIN Access`() {
        val allowed = allRoutes.filter { RouteGuard.isRouteAllowedForRole(UserRole.ADMIN, it) }
        val expected = setOf(
            "admin_beranda", "admin_pengguna", "admin_akademik",
            "admin_penugasan_guru", "admin_profil", "admin_detail_akun",
            "admin_daftar_akun", "admin_impor_csv",
            "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
        )
        assertEquals(expected, allowed.toSet())
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.ADMIN, "admin_beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ADMIN, "beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ADMIN, "guru_input"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ADMIN, "rekap_wali"))
    }

    @Test
    fun `2 Matrix Test - GURU Access`() {
        val allowed = allRoutes.filter { RouteGuard.isRouteAllowedForRole(UserRole.GURU, it) }
        val expected = setOf(
            "beranda", "nilai", "insight_page", "profil",
            "guru_input", "unggah_csv", "guru_dashboard",
            "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
        )
        assertEquals(expected, allowed.toSet())
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.GURU, "guru_input"))
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.GURU, "unggah_csv"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.GURU, "admin_beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.GURU, "rekap_wali"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.GURU, "ai"))
    }

    @Test
    fun `3 Matrix Test - WALI_KELAS Access`() {
        val allowed = allRoutes.filter { RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, it) }
        val expected = setOf(
            "beranda", "nilai", "insight_page", "profil",
            "rekap_wali", "statistik",
            "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
        )
        assertEquals(expected, allowed.toSet())
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, "rekap_wali"))
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, "statistik"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, "admin_beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, "guru_input"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.WALI_KELAS, "ai"))
    }

    @Test
    fun `4 Matrix Test - SISWA Access`() {
        val allowed = allRoutes.filter { RouteGuard.isRouteAllowedForRole(UserRole.SISWA, it) }
        val expected = setOf(
            "beranda", "nilai", "ai", "profil", "pelajaran",
            "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
        )
        assertEquals(expected, allowed.toSet())
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.SISWA, "beranda"))
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.SISWA, "ai"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.SISWA, "admin_beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.SISWA, "guru_input"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.SISWA, "rekap_wali"))
    }

    @Test
    fun `5 Matrix Test - ORANG_TUA Access`() {
        val allowed = allRoutes.filter { RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, it) }
        val expected = setOf(
            "beranda", "nilai", "ai", "profil", "pelajaran",
            "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
        )
        assertEquals(expected, allowed.toSet())
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, "beranda"))
        assertTrue(RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, "ai"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, "admin_beranda"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, "rekap_wali"))
        assertFalse(RouteGuard.isRouteAllowedForRole(UserRole.ORANG_TUA, "guru_input"))
    }

    @Test
    fun `6 Logout Scenario Test`() {
        val user = User(uid = "guru_01", role = "GURU", name = "Budi Santoso")
        AuthRepository.setCachedUser(user)
        assertEquals(user, AuthRepository.getCachedUser("guru_01"))

        // Simulate Logout: Clear memori cache dan user active UID
        AuthRepository.clearCache()
        assertNull(AuthRepository.getCachedUser("guru_01"))
    }
}
