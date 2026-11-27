package com.example.kawansekolah.data

import android.util.Log
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object SeedDataHelper {

    suspend fun seedDatabase() {
        val db = Firebase.firestore

        try {
            Log.d("SeedDataHelper", "Memulai Seed Data Akun Terdaftar ke Firebase Emulator...")

            val teacherUid = "guru_smp_01"
            val studentUid = "siswa_12345"
            val parentUid = "orangtua_01"

            val teacherUser = User(
                uid = teacherUid,
                name = "Drs. Budi Raharjo",
                email = "guru@kawansekolah.sch.id",
                role = "TEACHER",
                teacherId = teacherUid
            )
            val studentUser = User(
                uid = studentUid,
                name = "Ahmad Budi",
                email = "12345@kawansekolah.local",
                role = "STUDENT",
                teacherId = teacherUid,
                schoolLevel = "SMP",
                grade = 9,
                className = "9-A"
            )
            val parentUser = User(
                uid = parentUid,
                name = "Siti Rahma (Orang Tua Ahmad)",
                email = "ortu.ahmad@gmail.com",
                role = "PARENT",
                teacherId = teacherUid,
                studentIds = listOf(studentUid)
            )

            db.collection("users").document(teacherUid).set(teacherUser).await()
            db.collection("users").document(studentUid).set(studentUser).await()
            db.collection("users").document(parentUid).set(parentUser).await()

            val teacherProfile = TeacherProfile(
                teacherId = teacherUid,
                fullName = "Drs. Budi Raharjo",
                schoolName = "SMP Negeri 1 Jakarta",
                schoolLevel = "SMP",
                classesTaught = listOf("9-A", "9-B"),
                profileComplete = true
            )
            db.collection("teaching_profiles").document(teacherUid).set(teacherProfile).await()

            Log.d("SeedDataHelper", "✅ Seed Data Akun Terdaftar berhasil dibuat!")
        } catch (e: Exception) {
            Log.e("SeedDataHelper", "❌ Gagal membuat Seed Data: ${e.message}")
        }
    }
}
