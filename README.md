# Scolarsia - Aplikasi Analytics & Penilaian Sekolah

**Scolarsia** adalah aplikasi manajemen dan analitik nilai siswa berbasis Android (Kotlin & Jetpack Compose) yang dirancang untuk membantu sekolah memantau perkembangan akademik secara terukur. Aplikasi ini mengintegrasikan **AI Analytics** untuk mengevaluasi nilai sub-topik berbobot, mendeteksi mata pelajaran yang lemah, serta memberikan rekomendasi belajar yang dipersonalisasi bagi siswa.

---

## ✨ Fitur Utama

* **Multi-Role Access Control:** Sistem hak akses terpisah dan tervalidasi ketat untuk **Admin / Super Admin**, **Guru Mata Pelajaran**, **Wali Kelas**, **Siswa**, dan **Orang Tua**.
* **AI Grade Analytics:** Analisis otomatis performa akademik berdasarkan bobot sub-topik untuk mendeteksi tren penurunan dan materi yang perlu diperbaiki.
* **Manajemen Kelas & Penilaian:** Pengelolaan jadwal, input nilai harian/ujian, impor data via CSV, serta pemetaan kelas jenjang SD (1–6), SMP (7–9), hingga SMA (10–12).
* **Monitoring Orang Tua & Wali Kelas:** Laporan perkembangan belajar siswa secara real-time, rekap kehadiran, dan ringkasan evaluasi semester.
* **Keamanan Sesi & Perangkat:** Pemantauan perangkat dan sesi aktif (*Perangkat & Sesi*) yang terhubung pada akun pengguna.

---

## 🛠️ Tech Stack & Arsitektur Layanan

Aplikasi ini berjalan dengan arsitektur *client-server* lokal menggunakan beberapa layanan terintegrasi:

| Komponen | Teknologi | Port Default | Keterangan |
| :--- | :--- | :---: | :--- |
| **Mobile App** | Kotlin, Jetpack Compose, Material 3, Retrofit | - | Aplikasi Android (`com.example.kawansekolah`) |
| **Backend API** | Node.js, Express | `3000` | REST API utama & logika bisnis |
| **Database Utama** | PostgreSQL (Docker) | `5434` | Penyimpanan data relasional sekolah & nilai (`seed_postgres.sql`) |
| **Auth Service** | Firebase Auth Emulator | `9099` | Autentikasi multi-role lokal (`seed-firebase`) |
| **NoSQL / Realtime** | Cloud Firestore Emulator | `8081` | Sinkronisasi data sesi & notifikasi |

---

## 📋 Prasyarat Sistem (Prerequisites)

Pastikan perangkat pengembangan sudah terpasang perangkat lunak berikut:
* **Android Studio** (versi terbaru dengan JDK 17+)
* **Node.js** (v18 atau lebih baru) & `npm`
* **Docker Desktop** (untuk menjalankan kontainer PostgreSQL)
* **Firebase CLI** (`npm install -g firebase-tools`)
* **Perangkat Android** (Emulator atau HP Fisik dalam jaringan Wi-Fi/LAN yang sama)

---

## 🚀 Automasi Layanan Lokal (Gradle Task Hook)

Proyek ini mendukung penyalaan otomatis seluruh layanan lokal (**Docker Desktop**, **Firebase Emulator Auth & Firestore**, **Data Seed 15 Akun**, dan **Backend API Node.js**) setiap kali menekan tombol **Run** atau **Debug** dari Android Studio.

### ⚙️ Cara Mengaktifkan / Mematikan Auto-Start

Pengaturan ini disimpan di dalam file `local.properties` (file ini diabaikan oleh Git / `.gitignore`):

```properties
# Untuk MENGAKTIFKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=true

# Untuk MEMATIKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=false
