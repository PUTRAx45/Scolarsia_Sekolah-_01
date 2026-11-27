# Scolarsia - Aplikasi Analytics & Penilaian Sekolah

**Scolarsia** (`com.example.kawansekolah`) adalah aplikasi manajemen dan analitik nilai siswa berbasis Android (**Kotlin & Jetpack Compose**) yang terhubung dengan layanan **AI Analytics & AI Tutor** (Ollama lokal / OpenRouter Cloud) untuk mengevaluasi nilai sub-topik berbobot, mendeteksi mata pelajaran yang lemah, dan menghasilkan rekomendasi belajar siswa secara otomatis.

---

## 🌟 Fitur Utama

- **Multi-Role Access Control (13 Modul Admin & Akses Berbasis Peran Strict):**
  - **Admin / Super Admin:** Pengelolaan penuh pada 13 modul administrator (pengguna, sekolah, penugasan guru, konfigurasi bobot nilai, pendaftaran akun, impor CSV, relasi keluarga) serta fitur khusus *"Lihat sebagai"* (*View As*) untuk meninjau antarmuka peran lain.
  - **Guru Mata Pelajaran:** Penginputan nilai sub-materi berbobot, unggah nilai via CSV, dan dasbor pemantauan performa mengajar.
  - **Wali Kelas:** Rekapitulasi nilai kelas bimbingan, analisis statistik perkembangan siswa, dan rapor digital.
  - **Siswa:** Pemantauan nilai akhir berbobot, analisis sub-materi terlemah, modul pelajaran (bagian A–D), dan AI Tutor interaktif.
  - **Orang Tua:** Pemantauan nilai anak yang terhubung (*Relasi Anak*), laporan perkembangan akademik, dan komunikasi notifikasi sekolah.
- **AI Grade Analytics & AI Tutor Interaktif:** Integrasi cerdas dengan engine LLM untuk menganalisis nilai sub-topik berbobot, mengidentifikasi materi terlemah, memberikan rekomendasi belajar terpersonalisasi, serta menyediakan fitur tanya jawab AI Tutor yang dilengkapi dengan **Filter Moderasi Konten** (*bad words json* & detektor salam).
- **Manajemen Kelas & Penilaian Multi-Jenjang:** Mendukung jenjang SD (kelas 1–6), SMP (kelas 7–9), dan SMA (kelas 10–12 dengan pembagian rombel/bagian A–D) beserta konfigurasi bobot tugas, UTS, dan UAS yang fleksibel.
- **Impor Data Agregat CSV:** Kemudahan penginputan nilai dan pendaftaran akun siswa/guru dalam jumlah besar via berkas `.csv`.
- **Manajemen Perangkat & Sesi:** Pelacakan perangkat dan sesi aktif (*Perangkat & Sesi*) serta penguncian navigasi per akun yang ketat untuk keamanan data sekolah.

---

## 🛠️ Tech Stack & Arsitektur Layanan

Aplikasi ini menggunakan arsitektur *hybrid* yang menghubungkan aplikasi Android, Backend REST API Node.js, Engine AI (Ollama Local / OpenRouter Cloud), database relasional PostgreSQL, dan Firebase Emulator Suite.

| Komponen | Teknologi / Framework | Port Default | Deskripsi Fungsi |
| :--- | :--- | :--- | :--- |
| **Mobile App** | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit2, Coroutines | - | Aplikasi Android native (`com.example.kawansekolah`) |
| **Backend API** | Node.js, Express.js, Firebase Admin SDK, Axios | `3000` | Service REST API untuk pengolahan AI Analytics, moderasi, dan admin endpoints |
| **AI Engine (Primary)** | Ollama Local LLM (`llama3.2:latest`) | `11434` | Engine AI lokal utama (`http://127.0.0.1:11434/v1/chat/completions`) |
| **AI Engine (Fallback)** | OpenRouter Cloud API (`openai/gpt-4o-mini`) | `443` (HTTPS) | Backup Cloud AI API via `OPENROUTER_API_KEY` di `backend/.env` |
| **Relational Database** | PostgreSQL 17 (Docker Container) | `5434` / `5432` | Penyimpanan data relasional backend (`users`, `sekolah`, `kelas`, `mapel`, `nilai`, dsb.) |
| **Auth Emulator** | Firebase Auth Emulator | `9099` | Layanan autentikasi identitas akun lokal |
| **Firestore Emulator** | Cloud Firestore Emulator | `8081` | Database dokumen NoSQL lokal untuk profil dan histori akademik |
| **Emulator Dashboard** | Firebase Emulator Suite UI | `4000` | Antarmuka web pemantauan emulator lokal |

---

## 📋 Prasyarat Sistem (Prerequisites)

Sebelum menjalankan aplikasi, pastikan perangkat pengembangan Anda telah terpasang:

1. **Android Studio** (Versi 2024.2+ / Jellyfish / Ladybug atau yang lebih baru)
2. **Java Development Kit (JDK 17)**
3. **Node.js** (Versi 18+ LTS) & `npm`
4. **Docker Desktop** (dengan WSL2 / Hyper-V aktif pada Windows)
5. **Firebase CLI** (`npm install -g firebase-tools`)
6. **Runtime / Koneksi AI Engine:**
   - **Pilihan 1 (Lokal - Default):** Terpasang **Ollama** di komputer (`ollama run llama3.2`) berjalan di Port `11434`.
   - **Pilihan 2 (Cloud Fallback):** Memiliki **OpenRouter API Key** (`OPENROUTER_API_KEY` di berkas `backend/.env`).
7. **Perangkat Penguji:** Android Emulator (API 26+) atau HP Fisik Android (versi 8.0+) yang terhubung ke jaringan Wi-Fi lokal yang sama.

---

## 🚀 Automasi Layanan Lokal (Gradle Task Hook)

Proyek ini dilengkapi dengan fitur otomasi **Gradle Task Hook** (`startLocalServices`). Ketika diaktifkan, Gradle akan otomatis menjalankan skrip `siapkan-lokal.bat` untuk menyiapkan seluruh layanan backend (Docker PostgreSQL, Firebase Emulator, dan Server Node.js) setiap kali Anda menekan tombol **Run** atau **Debug** di Android Studio (`installDebug` / `installPhysicalDebug`).

### ⚙️ Pengaturan Auto-Start (`local.properties`)

Pengaturan disimpan pada berkas `local.properties` (diabaikan oleh Git):

```properties
# Untuk MENGAKTIFKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=true

# Untuk MEMATIKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=false
```

---

## 🛠️ Langkah Inisialisasi Satu Kali (One-Time Setup)

1. **Clone Repositori:**
   ```bash
   git clone https://github.com/PUTRAx45/Scolarsia_Sekolah-_01.git
   cd Scolarsia_Sekolah-_01
   ```

2. **Buka Port Windows Firewall (`buka-firewall.bat`):**
   - Klik kanan pada berkas `buka-firewall.bat` -> pilih **Run as Administrator**.
   - Langkah ini mendaftarkan aturan *Inbound TCP* untuk Port `3000`, `8081`, `9099`, dan `4000` agar HP Fisik pada jaringan Wi-Fi dapat mengakses server laptop Anda.

3. **Konfigurasi Docker Desktop:**
   - Buka **Docker Desktop** -> **Settings (⚙️)** -> **General**.
   - Centang opsi **Start Docker Desktop when you sign in** agar *engine* Docker selalu siap di latar belakang.

4. **Penyiapan Layanan AI Engine:**
   - **Lokal (Ollama):** Unduh model dengan menjalankan perintah `ollama pull llama3.2` pada terminal Anda. Pastikan Ollama aktif di port `11434`.
   - **Cloud (OpenRouter):** Salin `backend/.env.example` menjadi `backend/.env`, lalu isikan `OPENROUTER_API_KEY=sk-or-v1-...` jika ingin menggunakan provider cloud.

5. **Mengetahui IP Wi-Fi Laptop (`tampil-ip.bat`):**
   - Jalankan `tampil-ip.bat` di terminal untuk menampilkan alamat IPv4 Wi-Fi laptop Anda (misal: `192.168.1.5`).

6. **Konfigurasi Alamat Server pada Aplikasi:**
   - **Android Emulator:** Menggunakan IP khusus `10.0.2.2` secara otomatis.
   - **HP Fisik:** Buka dialog *Konfigurasi Alamat Server* (ikon roda gigi ⚙️ di pojok kanan atas layar Login) dan masukkan IP Wi-Fi laptop Anda.

---

## 💻 Cara Menjalankan Aplikasi

### Opsi A: Otomatis via Android Studio & Skrip Utilitas

1. Pastikan `scolarsia.autostart=true` di `local.properties`.
2. Tekan tombol **Run** (`Shift + F10`) di Android Studio.
3. Gunakan skrip utilitas `.bat` pendukung sesuai kebutuhan:
   - `buka-firewall.bat` : Membuka izin port di Windows Firewall (Jalankan sebagai Admin).
   - `tampil-ip.bat` : Menampilkan alamat IP Wi-Fi laptop dan MAC Address.
   - `siapkan-lokal.bat` : Memulai seluruh layanan lokal secara otomatis (dipanggil Gradle Task Hook).
   - `start-all.bat` : Pemicu manual penyalaan otomatis ekosistem layanan lokal.
   - `cek-status.bat` : Memeriksa status container Docker dan kesehatan API (`/health`).
   - `stop-all.bat` : Menghentikan layanan backend Node.js dan container Firebase Emulator.

### Opsi B: Manual via Terminal

Jika Anda ingin menjalankan layanan secara terpisah dari direktori proyek (`<PATH_PROYEK>`):

```powershell
# 1. Menyalakan Ollama AI Engine (jika menggunakan AI lokal)
ollama serve

# 2. Menyalakan Container Docker (PostgreSQL & Firebase Emulator)
cd <PATH_FIREBASE_DOCKER>
docker compose up -d

# 3. Seeding Data PostgreSQL (Port 5434)
cd <PATH_PROYEK>
docker exec -i firebase-docker-postgres-1 psql -U admin -d postgres < seed_postgres.sql

# 4. Seeding Data Firebase Emulator (Auth & Firestore)
cd seed-firebase
npm install
$env:FIREBASE_AUTH_EMULATOR_HOST="127.0.0.1:9099"
$env:FIRESTORE_EMULATOR_HOST="127.0.0.1:8081"
$env:GCLOUD_PROJECT="demo-kawansekolah"
node seed.js

# 5. Menyalakan Backend API Node.js (Port 3000)
cd ..\backend
npm install
npm start
```

---

## 🔑 Akun Uji Coba (Demo Accounts)

Aplikasi menerapkan penguncian hak akses yang ketat sesuai dengan konfigurasi pada `DemoAccounts.kt`. Hanya 5 akun resmi berikut yang diizinkan masuk dalam **Mode Demo**:

| Peran / Hak Akses | Email Target | Password Default | Hak Akses & Fitur |
| :--- | :--- | :--- | :--- |
| **Admin / Super Admin** | `admin12@scolarsia.ac.id` | `1a2d3m4i5n` | Access penuh ke 13 modul Admin, kelola akun, data akademik, dan fitur *"Lihat sebagai"* (*Super Admin*). |
| **Guru Mata Pelajaran** | `guru12@scolarsia.ac.id` | `1g2u3r4u` | Input nilai sub-materi, impor CSV nilai, dasbor mengajar, dan beralih ke peran Wali Kelas. |
| **Wali Kelas** | `walikelas12@scolarsia.ac.id` | `1w2a3l4i` | Rekapitulasi nilai kelas bimbingan, analisis statistik perkembangan, dan beralih ke peran Guru. |
| **Siswa** | `siswa01@scolarsia.ac.id` | `1s2i3w4a` | Pemantauan nilai berbobot, analisis sub-materi terlemah, modul pelajaran (A–D), dan AI Tutor. |
| **Orang Tua** | `ortu_siswa01@scolarsia.ac.id` | `1o2r3a4n5g` | Pemantauan akademik siswa yang terhubung (*Relasi Anak*), laporan perkembangan, dan notifikasi. |

> **Catatan Keamanan:** Setiap akun non-admin dikunci secara otomatis pada perannya masing-masing. Percobaan navigasi ke rute di luar perannya akan diblokir oleh `RouteGuard` dan dikembalikan ke beranda yang sah.

---

## 📁 Struktur Proyek (Detail & Lengkap)

```text
Scolarsia_Sekolah-_01/
├── app/                                      # Modul Utama Aplikasi Android
│   ├── build.gradle.kts                      # Konfigurasi Build & Gradle Task Hook startLocalServices
│   ├── google-services.json                  # Konfigurasi Firebase Android Client
│   ├── proguard-rules.pro                    # Aturan ProGuard / R8
│   ├── sampledata/
│   │   └── READMETEKS_IMPLEMENTASION/        # Berkas Dokumentasi Tambahan
│   │       ├── DEPLOY_BACKEND.md             # Panduan Deploy Backend
│   │       ├── DOKUMENTASI_PROYEK.md         # Dokumentasi Lengkap Proyek
│   │       └── Ringkasan_Pengerjaan_Admin_Auth.md # Catatan Pengerjaan Modul Admin & Auth
│   └── src/
│       ├── androidTest/                      # Instrumentated UI Tests
│       ├── test/                             # Unit Tests (RouteGuard, ScoreValidator, dsb.)
│       └── main/
│           ├── java/com/example/kawansekolah/
│           │   ├── FirebaseEmulatorHelper.kt # Inisialisasi Emulator Auth & Firestore
│           │   ├── MainActivity.kt           # Activity Utama Android
│           │   ├── ScolarsiaApp.kt           # Application Class
│           │   ├── data/                     # Data Layer (Models, Repositories, Network)
│           │   │   ├── model/                # Data Classes (User, Subject, GradeItem, dsb.)
│           │   │   ├── network/              # Retrofit ApiService Client Interface
│           │   │   ├── repository/           # Repositori (Auth, Academic, Ai, School, dsb.)
│           │   │   └── SeedDataHelper.kt     # Helper Seeding Data Lokal
│           │   ├── demo/                     # DemoAccounts.kt (Sumber Kebenaran Akun)
│           │   ├── domain/                   # Business Logic & Guard
│           │   │   ├── AccessPolicy.kt       # Kebijakan Izin Fitur Akademik
│           │   │   ├── RouteGuard.kt         # Matrix Pemblokir Navigasi Antar Peran
│           │   │   ├── ScoreValidator.kt     # Validasi Input Nilai Sub-materi
│           │   │   └── StatisticsCalculator.kt # Kalkulasi Rata-rata & Bobot Nilai
│           │   ├── ui/                       # UI Presentation Layer (Jetpack Compose)
│           │   │   ├── MainApp.kt            # Entry Composable & State Sesi
│           │   │   ├── MainScreen.kt         # Scaffold, BottomBar, & NavHost Central
│           │   │   ├── admin/                # 13 Layar Modul Administrator (Beranda, Pengguna, dsb.)
│           │   │   ├── ai/                   # Layar AI Analytics & AI Tutor Interaktif
│           │   │   ├── beranda/              # Layar Beranda Multi-Role
│           │   │   ├── components/           # Komponen Reusable (TopBar, BottomBar, dsb.)
│           │   │   ├── feature_insight/      # Layar Insight AI Akademik
│           │   │   ├── feature_nilai/        # Layar Input Nilai & Unggah CSV
│           │   │   ├── feature_notifikasi/   # Bottom Sheet Notifikasi Akademik
│           │   │   ├── feature_statistik/    # Layar Grafik Statistik Perkembangan
│           │   │   ├── feature_wali/         # Layar Rekapitulasi & Rapor Siswa Wali Kelas
│           │   │   ├── guru/                 # Layar Dasbor Guru
│           │   │   ├── loading/              # Layar Loading Progress Bar
│           │   │   ├── login/                # Layar Role Selection & Login
│           │   │   ├── nilai/                # Layar Daftar Nilai & Detail Sub-materi
│           │   │   ├── pelajaran/            # Layar Modul Pelajaran
│           │   │   ├── profil/               # Layar Profil, Password, Sesi, Privasi
│           │   │   ├── splash/               # Layar Splash Screen
│           │   │   └── theme/                # Tema Color, Typography, & Dark Theme
│           │   └── utils/                    # Utility Helpers
│           │       ├── DevHost.kt            # Pengatur IP Host & Port Dynamic
│           │       ├── DeviceSessionManager.kt # Pelacak Sesi Perangkat Aktif
│           │       ├── NetworkObserver.kt    # Pemantau Koneksi Internet
│           │       ├── NotificationHelper.kt # Sistem Notifikasi Lokal
│           │       └── UserSessionManager.kt # Pengelola Sesi SharedPreferences
│           └── res/                          # Android Resources (Drawable, Mipmap, Values, Xml)
├── backend/                                  # Service REST API Node.js / Express
│   ├── server.js                             # Server Utama, Router AI (Ollama/OpenRouter)
│   ├── greeting.js                           # Detektor Salam & Penangan Kasus Sederhana
│   ├── moderation.js                         # Filter Moderasi Konten AI
│   ├── badwords.json                         # Kamus Kata Terlarang Moderasi
│   ├── seed-manager.js                       # Manager Seeding Data Backend
│   ├── Dockerfile                            # Konfigurasi Container Backend Docker
│   ├── .env.example                          # Template Variabel Lingkungan
│   └── package.json                          # Dependencies Backend Node.js
├── seed-firebase/                            # Tools Seeding Firebase Emulator
│   ├── seed.js                               # Script Seeding Auth & Firestore
│   └── package.json
├── seed_postgres.sql                         # Script DDL & Data Seed PostgreSQL
├── docker-compose.yml                        # Konfigurasi Docker Services (Postgres & Firebase)
├── firebase.json                             # Konfigurasi Emulator Ports & Rules
├── firestore.rules                           # Keamanan Firestore Security Rules
├── firestore.indexes.json                    # Indeks Query Firestore
├── buka-firewall.bat                         # Membuka Port Firewall (3000, 8081, 9099, 4000)
├── tampil-ip.bat                             # Menampilkan IP Wi-Fi Laptop & MAC Address
├── siapkan-lokal.bat                         # Otomasi Penyiapan Layanan Lokal (Gradle Hook)
├── start-all.bat                             # Pemicu Penyalaan Seluruh Ekosistem Lokal
├── cek-status.bat                            # Mengecek Status Docker Container & API Health
├── stop-all.bat                              # Menghentikan Layanan Backend & Container
└── README.md                                 # Berkas Dokumentasi Utama Proyek
```

---

## 👥 Tim Pengembang

Aplikasi ini dikembangkan oleh **Tim Magang PT Divistant Teknologi Indonesia** (*Program Studi D3 Manajemen Informatika — Universitas Amikom Yogyakarta*):

- 👨‍💻 **Firmansyah Ardhani Putra**
- 👩‍💻 **Anggie Nur Shafira**
- 👩‍💻 **Alifia Nurohmah**
