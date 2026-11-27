package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.ui.components.EmptyStateView
import com.example.kawansekolah.ui.components.LinkRowItem
import com.example.kawansekolah.ui.components.TopBarArea
import com.example.kawansekolah.ui.theme.*

@Composable
fun AdminBerandaScreen(
    userName: String = "Admin Sekolah",
    schoolName: String = "Scolarsia",
    unreadCount: Int = 0,
    totalUsers: Int = 0,
    totalGuru: Int = 0,
    totalSiswa: Int = 0,
    totalOrtu: Int = 0,
    totalKelas: Int = 0,
    activities: List<String> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onNavigateToPengguna: () -> Unit = {},
    onNavigateToAkademik: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(
            userName = "Pak Arif",
            unreadCount = 2,
            onNotificationClick = onNotificationClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Blue Hero Summary Card (#1C64F2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SISWA AKTIF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite.copy(alpha = 0.85f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ColorWhite.copy(alpha = 1f)
                        ) {
                            Text(
                                text = "26 guru",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "384",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "siswa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorWhite,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = ColorWhite.copy(alpha = 1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("KELAS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("18", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = ColorWhite.copy(alpha = 1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("MAPEL", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("12", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Quick Access Section ("Akses Cepat")
            Text(
                text = "Akses cepat",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinkRowItem(
                title = "Kelola pengguna",
                subtitle = "Daftarkan dan atur akun sekolah",
                icon = Icons.Default.Group,
                onClick = onNavigateToPengguna
            )

            LinkRowItem(
                title = "Data akademik",
                subtitle = "Atur kelas, mapel, dan bobot",
                icon = Icons.Default.School,
                onClick = onNavigateToAkademik
            )

            // Recent Activity / System Log Section
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aktivitas terbaru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bobot diperbarui • Kelas IX A ditambahkan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan akun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "26 guru • 384 siswa • 310 orang tua aktif",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
