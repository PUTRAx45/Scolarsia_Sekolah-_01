package com.example.kawansekolah.ui.login

import android.content.Context
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.ui.theme.PrimaryBlue
import com.example.kawansekolah.utils.DevHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramSocket
import java.net.InetAddress

fun restartAppProcess(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Process.killProcess(Process.myPid())
}

private fun getHpIpAddress(targetHost: String): String {
    return try {
        val socket = DatagramSocket()
        val dest = if (targetHost.isNotBlank() && targetHost != "10.0.2.2") {
            InetAddress.getByName(targetHost)
        } else {
            InetAddress.getByName("8.8.8.8")
        }
        socket.connect(dest, 53)
        val ip = socket.localAddress.hostAddress ?: ""
        socket.close()
        ip
    } catch (_: Exception) {
        "127.0.0.1"
    }
}

@Composable
fun ServerConfigDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var serverHostInput by remember { mutableStateOf(DevHost.getHost(context)) }
    var testResultMsg by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }

    val currentHpIp = remember(serverHostInput) { getHpIpAddress(serverHostInput.trim()) }
    val targetHost = serverHostInput.trim().ifBlank { "10.0.2.2" }

    val isSameSubnet = remember(currentHpIp, targetHost) {
        if (currentHpIp.contains(".") && targetHost.contains(".") && !currentHpIp.startsWith("127.") && targetHost != "10.0.2.2") {
            currentHpIp.substringBeforeLast(".") == targetHost.substringBeforeLast(".")
        } else true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Konfigurasi Alamat Server", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Masukkan IP Wi-Fi laptop (hanya angka & titik, misal: 192.168.x.x) atau 10.0.2.2 untuk Android Emulator:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = serverHostInput,
                    onValueChange = { input ->
                        serverHostInput = input.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("IP Laptop / Host") },
                    placeholder = { Text("192.168.x.x") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Subnet & IP Info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📱 IP HP: $currentHpIp | 💻 Server: $targetHost",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isSameSubnet) {
                            Text(
                                text = "⚠️ IP HP ($currentHpIp) dan IP Server ($targetHost) tidak dalam satu subnet /24. Pastikan HP dan laptop terhubung ke jaringan Wi-Fi yang sama.",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                if (testResultMsg.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (testResultMsg.contains("✅")) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResultMsg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testResultMsg.contains("✅")) Color(0xFF166534) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        isTestingConnection = true
                        testResultMsg = "⏳ Memeriksa 3 layanan (Backend 3000, Auth 9099, Firestore 8081)..."
                        coroutineScope.launch {
                            val hostToTest = serverHostInput.trim().ifBlank { "10.0.2.2" }

                            val resMsg = withContext(Dispatchers.IO) {
                                fun testSocketService(host: String, port: Int, timeoutMs: Int = 5000): String {
                                    var lastCause = ""
                                    for (attempt in 1..2) {
                                        val start = System.currentTimeMillis()
                                        try {
                                            val s = java.net.Socket()
                                            s.connect(java.net.InetSocketAddress(host, port), timeoutMs)
                                            s.close()
                                            val duration = System.currentTimeMillis() - start
                                            return "OK (${duration}ms)"
                                        } catch (e: java.net.SocketTimeoutException) {
                                            lastCause = "Timeout (${timeoutMs / 1000}s)"
                                        } catch (e: java.net.ConnectException) {
                                            lastCause = "Ditolak (Port $port)"
                                        } catch (e: java.net.UnknownHostException) {
                                            return "Tidak ada jaringan ($host)"
                                        } catch (e: Exception) {
                                            lastCause = "Gagal (${e.message})"
                                        }
                                    }
                                    return lastCause
                                }

                                fun testBackendService(host: String, timeoutMs: Int = 5000): String {
                                    var lastCause = ""
                                    for (attempt in 1..2) {
                                        val start = System.currentTimeMillis()
                                        try {
                                            val url = java.net.URL("http://$host:3000/health")
                                            val conn = url.openConnection() as java.net.HttpURLConnection
                                            conn.connectTimeout = timeoutMs
                                            conn.readTimeout = timeoutMs
                                            conn.connect()
                                            val code = conn.responseCode
                                            val duration = System.currentTimeMillis() - start
                                            return if (code == 200) "OK (${duration}ms)" else "Respons bukan 200 (HTTP $code)"
                                        } catch (e: java.net.SocketTimeoutException) {
                                            lastCause = "Timeout (${timeoutMs / 1000}s)"
                                        } catch (e: java.net.ConnectException) {
                                            lastCause = "Ditolak (Port 3000)"
                                        } catch (e: java.net.UnknownHostException) {
                                            return "Tidak ada jaringan ($host)"
                                        } catch (e: Exception) {
                                            lastCause = "Gagal (${e.message})"
                                        }
                                    }
                                    return lastCause
                                }

                                val backendRes = testBackendService(hostToTest, 5000)
                                val authRes = testSocketService(hostToTest, 9099, 5000)
                                val firestoreRes = testSocketService(hostToTest, 8081, 5000)

                                val backendOk = backendRes.startsWith("OK")
                                val authOk = authRes.startsWith("OK")
                                val firestoreOk = firestoreRes.startsWith("OK")

                                if (backendOk && authOk && firestoreOk) {
                                    "✅ SEMUA LAYANAN TERKONEKSI (OK):\n• Backend (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                } else if (!backendOk && !authOk && !firestoreOk) {
                                    "Semua layanan gagal pada $hostToTest. IP laptop mungkin berubah atau alamat ini dipakai perangkat lain. Jalankan tampil-ip.bat di laptop.\n\n• Backend (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                } else {
                                    "❌ STATUS KONEKSI LAYANAN:\n• Backend API (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                }
                            }

                            testResultMsg = resMsg
                            isTestingConnection = false
                        }
                    },
                    enabled = !isTestingConnection,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isTestingConnection) "Memeriksa..." else "Tes Koneksi (3 Layanan)", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanHost = serverHostInput.trim().removePrefix("http://").removePrefix("https://").substringBefore(":")
                    val oldHost = DevHost.getSavedDynamicHost(context)
                    DevHost.saveDynamicHost(context, cleanHost)

                    if (cleanHost == oldHost && FirebaseEmulatorHelper.isEmulatorConfigured) {
                        onDismiss()
                    } else {
                        Toast.makeText(context, "✅ Alamat server disimpan ($cleanHost). Memuat ulang aplikasi...", Toast.LENGTH_SHORT).show()
                        restartAppProcess(context)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan & Terapkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
