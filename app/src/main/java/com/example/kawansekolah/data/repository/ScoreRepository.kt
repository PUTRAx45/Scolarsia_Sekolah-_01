package com.example.kawansekolah.data.repository

import com.example.kawansekolah.data.model.Kelas
import com.example.kawansekolah.data.model.Kompetensi
import com.example.kawansekolah.data.model.Penugasan
import com.example.kawansekolah.data.model.Periode
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.model.Student
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.ScoreValidator
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object ScoreRepository {

    private val firestore by lazy { Firebase.firestore }

    var isDemoMode: Boolean = true

    private val localScores = mutableListOf<Score>()
    private val localPenugasan = mutableListOf<Penugasan>()

    suspend fun saveScore(
        userRole: String,
        userId: String,
        assignedTeacherId: String,
        score: Score
    ): Result<String> {
        if (!AccessPolicy.canInputScore(userRole, userId, assignedTeacherId)) {
            return Result.failure(IllegalStateException("Akses ditolak: Anda tidak memiliki wewenang menginput nilai pada mata pelajaran ini."))
        }

        if (!ScoreValidator.validateScore(score.scoreValue)) {
            return Result.failure(IllegalArgumentException("Format/Rentang nilai ${score.scoreValue} tidak valid (harus 0.0 - 100.0)."))
        }

        return try {
            val docRef = if (score.id.isBlank()) firestore.collection("scores").document() else firestore.collection("scores").document(score.id)
            val scoreToSave = score.copy(id = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(scoreToSave).await()
            localScores.removeAll { it.id == scoreToSave.id }
            localScores.add(scoreToSave)
            Result.success(docRef.id)
        } catch (e: Exception) {
            if (isDemoMode) {
                val scoreId = if (score.id.isBlank()) "sc_${System.currentTimeMillis()}" else score.id
                val scoreToSave = score.copy(id = scoreId, timestamp = System.currentTimeMillis())
                localScores.removeAll { it.id == scoreToSave.id }
                localScores.add(scoreToSave)
                Result.success(scoreId)
            } else {
                Result.failure(IllegalStateException("Gagal menyimpan nilai ke Firestore: ${e.message}", e))
            }
        }
    }

    suspend fun saveScoresBatch(
        userRole: String,
        userId: String,
        assignedTeacherId: String,
        scores: List<Score>
    ): Result<Int> {
        if (!AccessPolicy.canUploadCsv(userRole, userId, assignedTeacherId)) {
            return Result.failure(IllegalStateException("Akses ditolak: Hanya Guru Mapel bersangkutan yang dapat mengunggah file nilai."))
        }

        val invalidScores = scores.filter { !ScoreValidator.validateScore(it.scoreValue) }
        if (invalidScores.isNotEmpty()) {
            return Result.failure(IllegalArgumentException("Terdapat ${invalidScores.size} baris nilai di luar rentang 0.0 - 100.0."))
        }

        var savedCount = 0
        for (score in scores) {
            val res = saveScore(userRole, userId, assignedTeacherId, score)
            if (res.isSuccess) {
                savedCount++
            } else {
                return Result.failure(res.exceptionOrNull() ?: IllegalStateException("Gagal menyimpan batch nilai."))
            }
        }
        return Result.success(savedCount)
    }

    suspend fun getScoresForClassAndSubject(
        userRole: String,
        userId: String,
        classId: String,
        subjectId: String,
        classHomeroomTeacherId: String,
        assignedTeacherId: String
    ): List<Score> {
        if (!AccessPolicy.canViewClassScores(userRole, userId, classHomeroomTeacherId, assignedTeacherId)) {
            return emptyList()
        }

        return try {
            val snapshot = firestore.collection("scores")
                .whereEqualTo("classId", classId)
                .whereEqualTo("subjectId", subjectId)
                .get()
                .await()
            val list = snapshot.toObjects(Score::class.java)
            if (list.isNotEmpty()) list else if (isDemoMode) localScores.filter { it.classId == classId && it.subjectId == subjectId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localScores.filter { it.classId == classId && it.subjectId == subjectId } else emptyList()
        }
    }

    suspend fun getScoresForClass(
        userRole: String,
        userId: String,
        classId: String,
        classHomeroomTeacherId: String
    ): List<Score> {
        if (AccessPolicy.isWaliKelas(userRole)) {
            if (userId.isBlank() || userId != classHomeroomTeacherId) {
                return emptyList()
            }
        } else if (AccessPolicy.isGuruMapel(userRole)) {
            val assignedSubjectIds = getAssignedSubjectIdsForTeacherAndClass(userId, classId)
            if (assignedSubjectIds.isEmpty()) {
                return emptyList()
            }
            val allClassScores = fetchClassScoresInternal(classId)
            return allClassScores.filter { it.subjectId in assignedSubjectIds }
        } else {
            return emptyList()
        }

        return fetchClassScoresInternal(classId)
    }

    private suspend fun fetchClassScoresInternal(classId: String): List<Score> {
        return try {
            val snapshot = firestore.collection("scores")
                .whereEqualTo("classId", classId)
                .get()
                .await()
            val list = snapshot.toObjects(Score::class.java)
            if (list.isNotEmpty()) list else if (isDemoMode) localScores.filter { it.classId == classId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localScores.filter { it.classId == classId } else emptyList()
        }
    }

    suspend fun getAssignedSubjectIdsForTeacherAndClass(teacherId: String, classId: String): List<String> {
        return try {
            val snapshot = firestore.collection("penugasan")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("classId", classId)
                .get()
                .await()
            val list = snapshot.toObjects(Penugasan::class.java)
            val ids = list.map { it.subjectId }
            if (ids.isNotEmpty()) ids else if (isDemoMode) localPenugasan.filter { it.teacherId == teacherId && it.classId == classId }.map { it.subjectId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localPenugasan.filter { it.teacherId == teacherId && it.classId == classId }.map { it.subjectId } else emptyList()
        }
    }

    suspend fun getPenugasanForTeacher(teacherId: String): Penugasan? {
        if (teacherId.isBlank()) return null

        return try {
            val snapshot = firestore.collection("penugasan")
                .whereEqualTo("teacherId", teacherId)
                .limit(1)
                .get()
                .await()
            val list = snapshot.toObjects(Penugasan::class.java)
            if (list.isNotEmpty()) list.first() else if (isDemoMode) localPenugasan.firstOrNull { it.teacherId == teacherId } else null
        } catch (_: Exception) {
            if (isDemoMode) localPenugasan.firstOrNull { it.teacherId == teacherId } else null
        }
    }

    fun getDummyKelasList(): List<Kelas> {
        return emptyList()
    }

    fun getDummyStudentsList(classId: String = ""): List<Student> {
        return emptyList()
    }

    fun getDummySubjectsList(): List<SubjectEntity> {
        return emptyList()
    }

    fun getDummyKompetensiList(subjectId: String = ""): List<Kompetensi> {
        return emptyList()
    }

    fun getDummyPeriodeList(): List<Periode> {
        return emptyList()
    }
}
