package com.example.kawansekolah.data.repository

import android.content.Context
import android.util.Log
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.data.model.FirestoreCollections
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.demo.DemoAccounts
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.utils.DeviceSessionManager
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

object AuthRepository {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val firestore: FirebaseFirestore get() = Firebase.firestore



    // In-Memory User Data Cache
    private val userMemoryCache = mutableMapOf<String, User>()
    private var activeUserUid: String? = null

    fun getCachedUser(uid: String): User? = userMemoryCache[uid]

    fun setCachedUser(user: User) {
        if (user.uid.isNotBlank()) {
            userMemoryCache[user.uid] = user
            activeUserUid = user.uid
        }
    }

    fun clearCache() {
        userMemoryCache.clear()
        activeUserUid = null
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid ?: activeUserUid

    suspend fun getUserProfile(uid: String): User? {
        userMemoryCache[uid]?.let {
            if (BuildConfig.DEBUG) Log.d("LoginTiming", "[getUserProfile] Mengambil data user dari Cache Memori (0ms)")
            return it
        }

        return try {
            var userDoc: User? = null

            // Try pengguna collection first
            val doc1 = firestore.collection(FirestoreCollections.PENGGUNA).document(uid).get().await()
            if (doc1.exists()) {
                val parsed = doc1.toObject(User::class.java)
                val namaVal = doc1.getString("namaLengkap") ?: doc1.getString("nama") ?: parsed?.name ?: ""
                val emailVal = doc1.getString("email") ?: parsed?.email ?: ""
                val roleVal = doc1.getString("role") ?: parsed?.role ?: "SISWA"
                val isAktif = doc1.getBoolean("aktif") ?: doc1.getBoolean("isActive") ?: parsed?.aktif ?: true
                val isSuperAdmin = doc1.getBoolean("isSuperAdmin") ?: parsed?.isSuperAdmin ?: false

                userDoc = User(
                    uid = uid,
                    name = namaVal,
                    email = emailVal,
                    role = roleVal,
                    aktif = isAktif,
                    roles = doc1.get("roles") as? List<String> ?: emptyList(),
                    isSuperAdmin = isSuperAdmin
                )
            } else {
                // Fallback to users collection
                val doc2 = firestore.collection("users").document(uid).get().await()
                if (doc2.exists()) {
                    val parsed = doc2.toObject(User::class.java)
                    val namaVal = doc2.getString("namaLengkap") ?: doc2.getString("nama") ?: parsed?.name ?: ""
                    val emailVal = doc2.getString("email") ?: parsed?.email ?: ""
                    val roleVal = doc2.getString("role") ?: parsed?.role ?: "SISWA"
                    val isAktif = doc2.getBoolean("aktif") ?: doc2.getBoolean("isActive") ?: parsed?.aktif ?: true
                    val isSuperAdmin = doc2.getBoolean("isSuperAdmin") ?: parsed?.isSuperAdmin ?: false

                    userDoc = User(
                        uid = uid,
                        name = namaVal,
                        email = emailVal,
                        role = roleVal,
                        aktif = isAktif,
                        roles = doc2.get("roles") as? List<String> ?: emptyList(),
                        isSuperAdmin = isSuperAdmin
                    )
                }
            }

            userDoc?.let { setCachedUser(it) }
            userDoc
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user profile: ${e.message}")
            throw e
        }
    }

    fun normalizeRole(roleStr: String): String {
        return when (roleStr.lowercase().trim()) {
            "admin", "administrator" -> "ADMIN"
            "guru", "teacher", "guru_mapel" -> "GURU"
            "wali_kelas", "walikelas", "homeroom_teacher" -> "WALI_KELAS"
            "siswa", "student" -> "SISWA"
            "orang_tua", "orangtua", "parent" -> "ORANG_TUA"
            else -> roleStr.uppercase()
        }
    }

    suspend fun loginWithEmailOrNis(
        identifier: String,
        pass: String,
        expectedRole: UserRole,
        context: Context? = null
    ): User {
        return withTimeout(10_000L) {
            val normalizedIdentifier = identifier.trim().lowercase()
            val email = if (normalizedIdentifier.contains("@")) {
                normalizedIdentifier
            } else {
                "${normalizedIdentifier}@scolarsia.ac.id"
            }

            val appOptions = try { FirebaseApp.getInstance().options } catch (_: Exception) { null }
            val activeProjectId = appOptions?.projectId ?: "Unknown"
            val apiKeyPrefix = (appOptions?.apiKey ?: "").take(6)
            val currentHost = DevHost.getHost(context)
            val isApplied = FirebaseEmulatorHelper.isEmulatorConfigured

            Log.i("ScolarsiaAuth", "======================================================")
            Log.i("ScolarsiaAuth", "🔑 MEMULAI SIGN IN FIREBASE AUTH:")
            Log.i("ScolarsiaAuth", "   Email Target : $email")
            Log.i("ScolarsiaAuth", "   Project ID   : '$activeProjectId'")
            Log.i("ScolarsiaAuth", "   API Key 6-char: '$apiKeyPrefix...'")
            Log.i("ScolarsiaAuth", "   Host Target  : '$currentHost:9099'")
            Log.i("ScolarsiaAuth", "   useEmulator  : $isApplied")
            Log.i("ScolarsiaAuth", "======================================================")

            // Block login immediately if emulator is NOT configured on Debug physical device
            if (BuildConfig.DEBUG && BuildConfig.USE_EMULATOR && !isApplied) {
                if (DevHost.isPhysicalDevice() && currentHost.isBlank()) {
                    Log.w("ScolarsiaAuth", "Login DIBLOKIR: Alamat server belum diatur pada HP Fisik.")
                    throw IllegalStateException("Alamat server belum diatur. Buka ikon roda gigi ⚙️ di pojok kanan atas lalu isi IP Wi-Fi laptop Anda.")
                }
            }

            // Quick TCP check (3 seconds) to Auth port 9099 before signInWithEmailAndPassword
            if (BuildConfig.DEBUG && BuildConfig.USE_EMULATOR) {
                val targetHostCheck = currentHost.ifBlank { if (DevHost.isPhysicalDevice()) "" else "10.0.2.2" }
                if (targetHostCheck.isNotBlank()) {
                    val isAuthReachable = withContext(Dispatchers.IO) {
                        try {
                            java.net.Socket().use { socket ->
                                socket.connect(java.net.InetSocketAddress(targetHostCheck, 9099), 3000)
                                true
                            }
                        } catch (_: Exception) {
                            false
                        }
                    }

                    if (!isAuthReachable) {
                        Log.e("ScolarsiaAuth", "TCP check 3s gagal ke $targetHostCheck:9099")
                        throw IllegalStateException("Tidak bisa menjangkau server di $targetHostCheck:9099. Periksa IP laptop dan Wi-Fi.")
                    }
                }
            }

            // 1. Firebase Auth signInWithEmailAndPassword
            val signInResult = try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: FirebaseAuthException) {
                val excName = e.javaClass.simpleName
                val errorCode = e.errorCode
                val msg = e.message ?: ""
                val stackSnippet = e.stackTrace.take(3).joinToString("\n   at ")

                Log.e("ScolarsiaAuth", "❌ [FirebaseAuthException] Class: $excName | Code: $errorCode | Message: $msg")
                Log.e("ScolarsiaAuth", "   ProjectID: '$activeProjectId' | APIKey: '$apiKeyPrefix...' | Host: '$currentHost:9099' | emulatorApplied: $isApplied")
                Log.e("ScolarsiaAuth", "   Stack: at $stackSnippet")

                if (errorCode == "ERROR_CONFIGURATION_NOT_FOUND" || msg.contains("CONFIGURATION_NOT_FOUND")) {
                    throw IllegalStateException("Aplikasi tidak memakai emulator. Periksa Alamat Server lalu tutup total aplikasi dan buka lagi.")
                }

                throw IllegalStateException("Gagal masuk Auth ($errorCode): $msg")
            } catch (e: FirebaseAuthInvalidUserException) {
                Log.w("ScolarsiaAuth", "Login gagal: Email/NISN '$email' tidak ditemukan pada Auth Emulator.")
                throw IllegalStateException("Akun belum terdaftar")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w("ScolarsiaAuth", "Login gagal: Kata sandi untuk '$email' salah.")
                throw IllegalStateException("Email atau kata sandi salah")
            } catch (e: TimeoutCancellationException) {
                val targetHost = DevHost.getHost(context).ifBlank { "10.0.2.2" }
                Log.e("ScolarsiaAuth", "Login timeout: Tidak bisa menjangkau server di $targetHost:9099")
                throw IllegalStateException("Tidak bisa menjangkau server di $targetHost:9099. Periksa IP laptop dan Wi-Fi.")
            } catch (e: Exception) {
                val excName = e.javaClass.simpleName
                val msg = e.message ?: ""
                Log.e("ScolarsiaAuth", "❌ [Login Exception] Class: $excName | Message: $msg")
                if (e is java.net.SocketTimeoutException || e is java.net.ConnectException || e is java.net.UnknownHostException || e is java.io.IOException || msg.contains("network") || msg.contains("connection")) {
                    val targetHost = DevHost.getHost(context).ifBlank { "10.0.2.2" }
                    throw IllegalStateException("Tidak bisa menjangkau server di $targetHost:9099. Periksa IP laptop dan Wi-Fi.")
                }
                throw IllegalStateException("Gagal masuk Auth: $msg")
            }

            val uid = signInResult.user?.uid ?: throw IllegalStateException("User ID tidak ditemukan pada Firebase Auth.")

            // 2. Fetch profile from Firestore
            val userProfile = try {
                getUserProfile(uid)
            } catch (e: FirebaseFirestoreException) {
                auth.signOut()
                val targetHost = DevHost.getHost(context).ifBlank { "10.0.2.2" }
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw IllegalStateException("Server belum siap")
                } else {
                    throw IllegalStateException("Gagal membaca Firestore: ${e.message} (Kode: ${e.code})")
                }
            }

            if (userProfile == null) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "UID: $uid | Dokumen profil tidak ditemukan pada koleksi pengguna/users.")
                throw IllegalStateException("Dokumen profil tidak ditemukan (UID: $uid). Akun belum didaftarkan oleh admin sekolah.")
            }

            // 3. Verify Active Status
            if (!userProfile.aktif) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "UID: $uid | Status akun nonaktif (aktif=false).")
                throw IllegalStateException("Akun dinonaktifkan. Silakan hubungi administrator sekolah.")
            }

            // 4. Verify Allowed Role
            if (!userProfile.roles.contains(expectedRole.name)) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "Login DIBLOKIR: Akun tidak punya peran ${expectedRole.name}.")
                throw IllegalStateException("Akun ini tidak memiliki akses sebagai ${expectedRole.label}.")
            }

            // 5. Track device session in Firestore if context is available
            context?.let { ctx ->
                DeviceSessionManager.saveCurrentSession(ctx, uid)
            }
            
            val actualRoleNorm = normalizeRole(userProfile.role)
            Log.i("ScolarsiaAuth", "UID: $uid | Role Firestore: $actualRoleNorm | Keputusan: SUCCESS")
            setCachedUser(userProfile)
            userProfile
        }
    }

    suspend fun registerTeacher(
        name: String,
        email: String,
        pass: String,
        schoolLevel: String,
        grade: Int,
        className: String,
        context: Context
    ): User {
        return createAccountViaBackend(name, email, pass, "GURU", context)
    }

    suspend fun createParentAccount(
        context: Context,
        name: String,
        email: String,
        pass: String,
        linkedStudentIds: List<String>
    ): User {
        return createAccountViaBackend(name, email, pass, "ORANG_TUA", context)
    }

    suspend fun createStudentAccount(
        context: Context,
        name: String,
        nis: String,
        pass: String,
        schoolLevel: String,
        grade: Int,
        className: String
    ): User {
        val syntheticEmail = "${nis.trim()}@scolarsia.ac.id"
        return createAccountViaBackend(name, syntheticEmail, pass, "SISWA", context)
    }
    
    private suspend fun createAccountViaBackend(
        name: String,
        email: String,
        pass: String,
        role: String,
        context: Context
    ): User {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure current user is an admin
                val currentUser = auth.currentUser ?: throw IllegalStateException("Anda harus login sebagai admin untuk membuat akun.")
                val token = currentUser.getIdToken(true).await().token ?: throw IllegalStateException("Gagal mendapatkan token autentikasi.")
                
                val baseUrl = DevHost.getBackendBaseUrl()
                val url = java.net.URL(baseUrl + "admin/users")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true
                
                val jsonInputString = """
                    {
                        "name": "$name",
                        "email": "$email",
                        "pass": "$pass",
                        "role": "$role"
                    }
                """.trimIndent()
                
                conn.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }
                
                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    // Success, parse if needed, but we can just construct a User object
                    User(
                        uid = "new_uid", // Backend handles real UID. This is just for local immediate state if needed
                        name = name,
                        email = email,
                        role = role,
                        aktif = true
                    )
                } else {
                    val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    throw IllegalStateException("Gagal membuat akun di server (HTTP $responseCode): $errorStream")
                }
            } catch (e: Exception) {
                throw IllegalStateException("Gagal membuat akun: ${e.message}")
            }
        }
    }
}
