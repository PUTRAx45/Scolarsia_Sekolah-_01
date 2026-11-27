package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.ui.components.DangerButton
import com.example.kawansekolah.ui.components.DarkProfileHeaderCard
import com.example.kawansekolah.ui.components.SecondaryButton
import com.example.kawansekolah.ui.components.SettingRowItem
import com.example.kawansekolah.ui.theme.*
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfilScreen(
    adminName: String = "Arif Setiawan",
    adminEmail: String = "admin12@scolarsia.ac.id",
    isSuperAdmin: Boolean = true,
    onLogoutClick: () -> Unit = {},
    onUbahKataSandiClick: () -> Unit = {},
    onPrivasiNotifikasiClick: () -> Unit = {},
    onPerangkatSesiClick: () -> Unit = {},
    onViewAsRole: (com.example.kawansekolah.data.model.UserRole) -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val activeAuthEmail = remember { Firebase.auth.currentUser?.email ?: adminEmail }
    val activeName = if (adminName.isNotBlank()) adminName else "Arif Setiawan"
    val initials = if (activeName.isNotBlank()) activeName.split(" ").map { it.take(1) }.take(2).joinToString("") else "AS"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profil saya",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Navy Profile Header Card
            DarkProfileHeaderCard(
                title = activeName,
                subtitle = "Administrator • SMP Nusantara",
                email = activeAuthEmail,
                initials = initials
            )

            if (isSuperAdmin) {
                Text(
                    text = "Lihat sebagai",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorderStroke)
                ) {
                    Column {
                        SettingRowItem(
                            icon = Icons.Default.CoPresent,
                            title = "Lihat sebagai Guru",
                            subtitle = "Lihat tampilan guru",
                            onClick = { onViewAsRole(com.example.kawansekolah.data.model.UserRole.GURU) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp, end = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingRowItem(
                            icon = Icons.Default.SupervisedUserCircle,
                            title = "Lihat sebagai Wali Kelas",
                            subtitle = "Lihat tampilan wali kelas",
                            onClick = { onViewAsRole(com.example.kawansekolah.data.model.UserRole.WALI_KELAS) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp, end = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingRowItem(
                            icon = Icons.Default.Face,
                            title = "Lihat sebagai Siswa",
                            subtitle = "Lihat tampilan siswa",
                            onClick = { onViewAsRole(com.example.kawansekolah.data.model.UserRole.SISWA) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 52.dp, end = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        SettingRowItem(
                            icon = Icons.Default.FamilyRestroom,
                            title = "Lihat sebagai Orang Tua",
                            subtitle = "Lihat tampilan orang tua",
                            onClick = { onViewAsRole(com.example.kawansekolah.data.model.UserRole.ORANG_TUA) }
                        )
                    }
                }
            }

            // Administrasi Sekolah Section
            Text(
                text = "Administrasi sekolah",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kelola pengguna & peran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Akun dan relasi keluarga", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = androidx.compose.ui.graphics.Color(0xFFEFF6FF)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kelola data akademik", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Periode, kelas, mapel, penugasan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Keamanan & Privasi Section
            Text(
                text = "Keamanan & privasi akun",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingRowItem(
                        icon = Icons.Default.Lock,
                        title = "Ubah kata sandi",
                        subtitle = "Jaga keamanan akun pribadi",
                        onClick = onUbahKataSandiClick
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingRowItem(
                        icon = Icons.Default.Security,
                        title = "Privasi & Notifikasi",
                        subtitle = "Atur izin dan notifikasi",
                        onClick = onPrivasiNotifikasiClick
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingRowItem(
                        icon = Icons.Default.Phonelink,
                        title = "Perangkat & sesi",
                        subtitle = "Kelola perangkat yang masuk",
                        badgeText = "1 aktif",
                        onClick = onPerangkatSesiClick
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "Akses nilai siswa mengikuti kewenangan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari akun", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Keluar dari akun?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Kamu perlu masuk lagi untuk mengelola akun sekolah.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Keluar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
