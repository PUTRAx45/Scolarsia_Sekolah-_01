package com.example.kawansekolah.demo

import com.example.kawansekolah.data.model.UserRole

object DemoAccounts {
    val ALLOWED_ACCOUNTS = mapOf(
        "admin12@scolarsia.ac.id" to DemoAccountInfo(
            password = "1a2d3m4i5n",
            allowedRoles = listOf(UserRole.ADMIN),
            isSuperAdmin = true
        ),
        "guru12@scolarsia.ac.id" to DemoAccountInfo(
            password = "1g2u3r4u",
            allowedRoles = listOf(UserRole.GURU, UserRole.WALI_KELAS)
        ),
        "walikelas12@scolarsia.ac.id" to DemoAccountInfo(
            password = "1w2a3l4i",
            allowedRoles = listOf(UserRole.WALI_KELAS, UserRole.GURU)
        ),
        "siswa01@scolarsia.ac.id" to DemoAccountInfo(
            password = "1s2i3w4a",
            allowedRoles = listOf(UserRole.SISWA)
        ),
        "ortu_siswa01@scolarsia.ac.id" to DemoAccountInfo(
            password = "1o2r3a4n5g",
            allowedRoles = listOf(UserRole.ORANG_TUA)
        )
    )
}

data class DemoAccountInfo(
    val password: String,
    val allowedRoles: List<UserRole>,
    val isSuperAdmin: Boolean = false
)
