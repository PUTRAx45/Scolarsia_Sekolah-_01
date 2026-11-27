package com.example.kawansekolah.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kawansekolah.data.model.NotificationItem
import com.example.kawansekolah.data.model.NotificationType
import com.example.kawansekolah.data.model.SchoolLevel
import com.example.kawansekolah.data.model.Subject
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.SchoolRepository
import com.example.kawansekolah.domain.RouteGuard
import com.example.kawansekolah.ui.ai.AiScreen
import com.example.kawansekolah.ui.beranda.BerandaScreen
import com.example.kawansekolah.ui.components.BottomNavigationBar
import com.example.kawansekolah.ui.feature_insight.InsightScreen
import com.example.kawansekolah.ui.feature_nilai.InputNilaiScreen
import com.example.kawansekolah.ui.feature_nilai.UnggahCsvScreen
import com.example.kawansekolah.ui.feature_notifikasi.NotificationBottomSheet
import com.example.kawansekolah.ui.feature_statistik.StatistikScreen
import com.example.kawansekolah.ui.feature_wali.RekapNilaiScreen
import com.example.kawansekolah.ui.guru.GuruScreen
import com.example.kawansekolah.ui.nilai.NilaiScreen
import com.example.kawansekolah.ui.pelajaran.PelajaranScreen
import com.example.kawansekolah.ui.profil.ProfilScreen
import com.example.kawansekolah.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String = "Pengguna",
    userRole: UserRole = UserRole.SISWA,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLogout: () -> Unit = {},
    initialNavigateTo: String? = null,
    onNavigateHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val isKeyboardOpen = WindowInsets.isImeVisible
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember(context) { com.example.kawansekolah.utils.UserSessionManager(context) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val loginRole = sessionManager.getLoginRole()
    val allowedRoles = sessionManager.getAllowedRoles()
    val isSuperAdmin = sessionManager.isSuperAdmin()

    val isBerandaRoute = currentRoute == "beranda" || currentRoute == "admin_beranda"
    BackHandler(enabled = isBerandaRoute) {
        if (sessionManager.getLoginRole() == UserRole.ADMIN && userRole != UserRole.ADMIN) {
            sessionManager.clearViewAsRole()
            navController.navigate("admin_beranda") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            Log.d("ScolarsiaAuth", "BackHandler di Beranda aktif: Menahan tombol Back agar tidak keluar dari akun.")
        }
    }

    LaunchedEffect(currentRoute, userRole) {
        if (currentRoute != null) {
            val isAllowed = RouteGuard.isRouteAllowedForAccount(
                loginRole = loginRole,
                activeViewRole = userRole,
                allowedRoles = allowedRoles,
                isSuperAdmin = isSuperAdmin,
                route = currentRoute
            )
            if (!isAllowed) {
                val homeRoute = RouteGuard.getHomeRouteForRole(userRole)
                Log.w("ScolarsiaAuth", "Navigasi Ditolak: User $loginRole (ViewRole: ${userRole.name}) dilarang mengakses route '$currentRoute'. Mengarahkan kembali ke $homeRoute.")
                navController.navigate(homeRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(initialNavigateTo) {
        if (!initialNavigateTo.isNullOrBlank() && RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, initialNavigateTo)) {
            navController.navigate(initialNavigateTo) {
                popUpTo(RouteGuard.getHomeRouteForRole(userRole)) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onNavigateHandled()
        }
    }

    val initialSubjects = remember { SchoolRepository.getInitialSubjects() }
    val initialTasks = remember { SchoolRepository.getInitialTasks() }
    val initialGrades = remember { SchoolRepository.getInitialGrades() }

    var subjects by remember { mutableStateOf(initialSubjects) }
    var tasks by remember { mutableStateOf(initialTasks) }
    var grades by remember { mutableStateOf(initialGrades) }
    var selectedSubjectForDetail by remember { mutableStateOf<Subject?>(null) }

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val startMillis = remember { System.currentTimeMillis() }
    var notifications by remember(userRole) {
        mutableStateOf(
            if (userRole == UserRole.GURU || userRole == UserRole.WALI_KELAS) {
                emptyList<NotificationItem>()
            } else {
                listOf(
                    NotificationItem(
                        id = 1,
                        title = "Login Berhasil",
                        message = "Selamat datang kembali, $userName! Akun ${userRole.label} Anda berhasil terhubung.",
                        timestampMillis = startMillis,
                        type = NotificationType.LOGIN,
                        isRead = false
                    ),
                    NotificationItem(
                        id = 2,
                        title = "Tugas Mendatang",
                        message = "Latihan Soal Aljabar Bab 3 jatuh tempo besok jam 23:59.",
                        timestampMillis = startMillis,
                        type = NotificationType.TASK,
                        relatedTaskId = 1,
                        isRead = false
                    ),
                    NotificationItem(
                        id = 3,
                        title = "Fitur AI Tutor Aktif",
                        message = "Tanyakan materi pelajaran atau latihan soal kapan saja dengan AI Tutor.",
                        timestampMillis = startMillis,
                        type = NotificationType.AI,
                        isRead = false
                    )
                )
            }
        )
    }

    val completedTaskIds = remember(tasks) { tasks.filter { it.isChecked }.map { it.id }.toSet() }

    val activeNotifications = remember(notifications, nowMillis, completedTaskIds) {
        notifications.filter { item ->
            if (item.type == NotificationType.LOGIN && (nowMillis - item.timestampMillis) >= 3_600_000L) {
                return@filter false
            }
            if (item.type == NotificationType.TASK && item.relatedTaskId != null && completedTaskIds.contains(item.relatedTaskId)) {
                return@filter false
            }
            true
        }
    }

    val unreadCount = activeNotifications.count { !it.isRead }
    var showNotificationSheet by remember { mutableStateOf(false) }

    val handleNotificationClick = {
        showNotificationSheet = true
    }

    val clearAllNotifications = {
        notifications = notifications.filter { item ->
            item.type == NotificationType.AI_RUNNING
        }
    }

    val toggleTask: (Int) -> Unit = { taskId ->
        tasks = tasks.map { if (it.id == taskId) it.copy(isChecked = !it.isChecked) else it }
    }

    val updateGrade: (String, Double, Int, String, String, Int, Int, Int, Int) -> Unit = { subjectName, score, semester, conduct, notes, hadir, izin, sakit, alpa ->
        grades = grades.map { item ->
            if (item.subjectName.equals(subjectName, ignoreCase = true) && item.semester == semester && item.level == SchoolLevel.SMP) {
                item.copy(
                    score = score,
                    conductStatus = conduct,
                    teacherNotes = notes,
                    hadirCount = hadir,
                    izinCount = izin,
                    sakitCount = sakit,
                    alpaCount = alpa
                )
            } else {
                item
            }
        }
    }

    val startDest = RouteGuard.getHomeRouteForRole(userRole)

    val roleString = when (userRole) {
        UserRole.GURU -> "GURU_MAPEL"
        UserRole.WALI_KELAS -> "WALI_KELAS"
        UserRole.SISWA -> "SISWA"
        UserRole.ORANG_TUA -> "ORANG_TUA"
        UserRole.ADMIN -> "ADMIN"
    }

    val activeUserId = Firebase.auth.currentUser?.uid ?: "user_active"

    Scaffold(
        bottomBar = {
            if (!isKeyboardOpen && selectedSubjectForDetail == null) {
                BottomNavigationBar(navController, userRole)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // GURU ROUTES
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "guru_input")) {
                composable("guru_input") {
                    InputNilaiScreen(
                        userRole = roleString,
                        userId = activeUserId,
                        onNavigateToCsvUpload = { navController.navigate("unggah_csv") },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "unggah_csv")) {
                composable("unggah_csv") {
                    UnggahCsvScreen(
                        userRole = roleString,
                        userId = activeUserId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "guru_dashboard")) {
                composable("guru_dashboard") {
                    GuruScreen(
                        userName = userName,
                        grades = grades,
                        unreadCount = unreadCount,
                        onNotificationClick = handleNotificationClick,
                        onUpdateGrade = updateGrade
                    )
                }
            }

            // WALI KELAS ROUTES
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "rekap_wali")) {
                composable("rekap_wali") {
                    RekapNilaiScreen(
                        userRole = roleString,
                        userId = activeUserId,
                        onNavigateToStatistik = { navController.navigate("statistik") },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "statistik")) {
                composable("statistik") {
                    StatistikScreen(
                        userRole = roleString,
                        userId = activeUserId
                    )
                }
            }

            // SHARED BETWEEN GURU & WALI KELAS
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "insight_page")) {
                composable("insight_page") {
                    InsightScreen(
                        userRole = roleString,
                        userId = activeUserId
                    )
                }
            }

            // GENERAL USER ROUTES
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "beranda")) {
                composable("beranda") {
                    BerandaScreen(
                        navController = navController,
                        userName = userName,
                        userRole = userRole,
                        subjects = subjects,
                        tasks = tasks,
                        unreadCount = unreadCount,
                        isViewAsMode = sessionManager.getLoginRole() == UserRole.ADMIN && userRole != UserRole.ADMIN,
                        viewAsRoleName = userRole.label,
                        onBackToAdminClick = {
                            sessionManager.clearViewAsRole()
                            navController.navigate("admin_beranda") { popUpTo(0) { inclusive = true } }
                        },
                        onNotificationClick = handleNotificationClick,
                        onToggleTask = toggleTask,
                        onSelectSubject = { selectedSubjectForDetail = it }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "pelajaran")) {
                composable("pelajaran") {
                    PelajaranScreen(
                        userName = userName,
                        subjects = subjects,
                        unreadCount = unreadCount,
                        onNotificationClick = handleNotificationClick,
                        onSelectSubject = { selectedSubjectForDetail = it }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "ai")) {
                composable("ai") {
                    AiScreen(
                        userName = userName,
                        userRole = userRole,
                        unreadCount = unreadCount,
                        onNotificationClick = handleNotificationClick,
                        onNewNotification = { newNotification ->
                            notifications = notifications.filterNot { it.id == newNotification.id } + newNotification
                        },
                        onUpdateGrade = updateGrade
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "nilai")) {
                composable("nilai") {
                    NilaiScreen(
                        userName = userName,
                        userRole = userRole,
                        grades = grades,
                        unreadCount = unreadCount,
                        onNotificationClick = handleNotificationClick
                    )
                }
            }

            // PROFILE & ACCOUNT SETTINGS (ALL ROLES)
            composable("profil") {
                val canSwitchBetweenGuruAndWali = allowedRoles.contains(UserRole.GURU) && allowedRoles.contains(UserRole.WALI_KELAS)
                ProfilScreen(
                    userName = userName,
                    userRole = userRole.name,
                    userEmail = Firebase.auth.currentUser?.email ?: "user@scolarsia.sch.id",
                    isViewAsMode = sessionManager.getLoginRole() == UserRole.ADMIN && userRole != UserRole.ADMIN,
                    viewAsRoleName = userRole.label,
                    showRoleSwitch = canSwitchBetweenGuruAndWali,
                    onBeralihPeran = {
                        val targetRole = if (userRole == UserRole.GURU) UserRole.WALI_KELAS else UserRole.GURU
                        sessionManager.setViewAsRole(targetRole)
                        navController.navigate("beranda") { popUpTo(0) { inclusive = true } }
                    },
                    onBackToAdminClick = {
                        sessionManager.clearViewAsRole()
                        navController.navigate("admin_beranda") { popUpTo(0) { inclusive = true } }
                    },
                    onLogoutClick = onLogout,
                    onUbahKataSandiClick = { navController.navigate("ubah_kata_sandi") },
                    onPrivasiNotifikasiClick = { navController.navigate("privasi_notifikasi") },
                    onPerangkatSesiClick = { navController.navigate("perangkat_sesi") }
                )
            }
            composable("ubah_kata_sandi") {
                com.example.kawansekolah.ui.profil.UbahKataSandiScreen(onBackClick = { navController.popBackStack() })
            }
            composable("privasi_notifikasi") {
                com.example.kawansekolah.ui.profil.PrivasiNotifikasiScreen(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("perangkat_sesi") {
                com.example.kawansekolah.ui.profil.PerangkatSesiScreen(onBackClick = { navController.popBackStack() })
            }

            // ADMIN ROUTES
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_beranda")) {
                composable("admin_beranda") {
                    com.example.kawansekolah.ui.admin.AdminBerandaScreen(
                        userName = userName,
                        unreadCount = unreadCount,
                        onNotificationClick = handleNotificationClick,
                        onNavigateToPengguna = { navController.navigate("admin_pengguna") },
                        onNavigateToAkademik = { navController.navigate("admin_pengguna") }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_pengguna")) {
                composable("admin_pengguna") {
                    com.example.kawansekolah.ui.admin.AdminPenggunaScreen(
                        onBackClick = { navController.popBackStack() },
                        onDaftarkanAkunClick = { navController.navigate("admin_daftar_akun") },
                        onUserClick = { userName -> navController.navigate("admin_detail_akun/$userName") }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_akademik")) {
                composable("admin_akademik") {
                    com.example.kawansekolah.ui.admin.AdminPenggunaScreen(
                        onBackClick = { navController.popBackStack() },
                        onDaftarkanAkunClick = { navController.navigate("admin_daftar_akun") },
                        onUserClick = { userName -> navController.navigate("admin_detail_akun/$userName") }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_penugasan_guru")) {
                composable("admin_penugasan_guru") {
                    com.example.kawansekolah.ui.admin.AdminPenugasanGuruScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_profil")) {
                composable("admin_profil") {
                    com.example.kawansekolah.ui.admin.AdminProfilScreen(
                        adminName = userName,
                        adminEmail = Firebase.auth.currentUser?.email ?: "admin12@scolarsia.ac.id",
                        isSuperAdmin = isSuperAdmin,
                        onLogoutClick = onLogout,
                        onUbahKataSandiClick = { navController.navigate("ubah_kata_sandi") },
                        onPrivasiNotifikasiClick = { navController.navigate("privasi_notifikasi") },
                        onPerangkatSesiClick = { navController.navigate("perangkat_sesi") },
                        onViewAsRole = { targetRole ->
                            sessionManager.setViewAsRole(targetRole)
                            navController.navigate("beranda") { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_detail_akun")) {
                composable("admin_detail_akun") {
                    com.example.kawansekolah.ui.admin.AdminDetailAkunScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("admin_detail_akun/{userName}") {
                    com.example.kawansekolah.ui.admin.AdminDetailAkunScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_daftar_akun")) {
                composable("admin_daftar_akun") {
                    com.example.kawansekolah.ui.admin.AdminDaftarAkunScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
            if (RouteGuard.isRouteAllowedForAccount(loginRole, userRole, allowedRoles, isSuperAdmin, "admin_impor_csv")) {
                composable("admin_impor_csv") {
                    com.example.kawansekolah.ui.admin.AdminImporCsvScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = activeNotifications,
            onDismiss = { showNotificationSheet = false },
            onClearAll = clearAllNotifications
        )
    }
}
