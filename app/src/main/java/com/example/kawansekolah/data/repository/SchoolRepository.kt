package com.example.kawansekolah.data.repository

import com.example.kawansekolah.data.model.GradeItem
import com.example.kawansekolah.data.model.Subject
import com.example.kawansekolah.data.model.Task

object SchoolRepository {

    fun getInitialSubjects(): List<Subject> {
        return emptyList()
    }

    fun getInitialTasks(): List<Task> {
        return emptyList()
    }

    fun getInitialGrades(): List<GradeItem> {
        return emptyList()
    }

    val teacherNuptkList = listOf(
        "9452758659200032", "1845763665300021", "5041761662200013", "3756749652200023", "8234751653300012"
    )

    val studentNisnList = listOf(
        "8933022425", "4812922059", "4406044393", "7388684281", "7963562696"
    )

    fun isValidTeacherNuptkOrPassword(input: String): Boolean {
        val clean = input.trim()
        if (clean == "12345" || clean == "54321") return true
        return teacherNuptkList.contains(clean) || clean.length == 16
    }

    fun isValidStudentNisn(input: String): Boolean {
        val clean = input.trim()
        if (clean == "0052025100" || clean == "20251004") return true
        return studentNisnList.contains(clean) || (clean.length == 10 && clean.all { it.isDigit() })
    }

    fun getAiResponse(query: String): String {
        val trimmed = query.trim()
        return when {
            trimmed.contains("aljabar", ignoreCase = true) || trimmed.contains("matematika", ignoreCase = true) ->
                "Untuk materi Aljabar, prinsip utamanya adalah menyederhanakan variabel. Contoh: 2x + 5 = 15, maka 2x = 10, sehingga x = 5. Apakah kamu ingin mencoba soal latihan Aljabar?"
            trimmed.contains("fisika", ignoreCase = true) || trimmed.contains("newton", ignoreCase = true) ->
                "Hukum Newton II menyatakan F = m × a (Gaya = massa × percepatan). Jika massa benda 2 kg dan percepatan 3 m/s², maka Gayanya adalah 6 Newton!"
            trimmed.contains("soal", ignoreCase = true) || trimmed.contains("latihan", ignoreCase = true) ->
                "Ini soal latihan untukmu:\n1. Hitung nilai x dari persamaan 3x - 9 = 0.\n2. Sebutkan 3 contoh zat padat, cair, dan gas!\nCoba jawab di sini ya!"
            trimmed.contains("rangkum", ignoreCase = true) || trimmed.contains("ringkas", ignoreCase = true) ->
                "Rangkuman singkat: Fokus belajar minggu ini mencakup persamaan dasar Aljabar, Hukum Newton, dan keanekaragaman hayati Biologi. Pastikan menyelesaikan modul sebelum kuis!"
            else ->
                "Pertanyaan yang bagus! Berdasarkan materi di Scolarsia, kamu bisa memperdalam pemahaman dengan membaca modul interaktif atau mencoba latihan soal di tab Pelajaran."
        }
    }
}
