# Scolarsia - Aplikasi Analytics & Penilaian Sekolah

**Scolarsia** (`com.example.kawansekolah`) adalah aplikasi manajemen dan analitik nilai siswa berbasis Android (**Kotlin & Jetpack Compose**) yang mengintegrasikan **AI Analytics** untuk evaluasi nilai sub-topik berbobot, deteksi mata pelajaran lemah, serta rekomendasi belajar cerdas bagi siswa.

---

## 🌟 Fitur Utama

- **Multi-Role Access Control (Akses Berbasis Peran Strict):**
  - **Admin / Super Admin:** Pengelolaan pengguna, jenjang & kelas, mata pelajaran, penugasan guru, dan mode *"Lihat sebagai"* (View As).
  - **Guru Mata Pelajaran:** Penginputan nilai sub-materi berbobot, unggah nilai via CSV, dan dasbor pemantauan performa mengajar.
  - **Wali Kelas:** Rekapitulasi nilai kelas bimbingan, analisis statistik perkembangan siswa, dan rapor digital.
  - **Siswa:** Pemantauan nilai akhir berbobot, analisis sub-materi terlemah, modul pelajaran, dan AI Tutor interaktif.
  - **Orang Tua:** Pemantauan nilai anak yang terhubung, laporan perkembangan akademik, dan komunikasi notifikasi sekolah.
- **AI Grade Analytics & Tutor:** Integrasi AI berbasis OpenRouter API (Gemini / GPT) untuk menghitung analisis kekuatan/kelemahan sub-materi secara otomatis serta menyediakan rekomendasi belajar terpersonalisasi.
- **Manajemen Kelas & Penilaian Multi-Jenjang:** Mendukung jenjang SD (kelas 1–6), SMP (kelas 7–9), dan SMA (kelas 10–12) dengan bobot tugas, UTS, dan UAS yang fleksibel.
- **Impor Data Agregat CSV:** Kemudahan penginputan nilai dan pendaftaran akun siswa/guru dalam jumlah besar via berkas `.csv`.
- **Manajemen Perangkat & Sesi:** Pelacakan perangkat dan sesi aktif serta penguncian navigasi per akun yang ketat untuk keamanan data sekolah.

---

## 🛠️ Tech Stack & Arsitektur Layanan

Aplikasi ini menggunakan arsitektur *hybrid* yang menghubungkan aplikasi Android, Backend REST API Node.js, database relasional PostgreSQL, dan Firebase Emulator Suite.

| Komponen | Teknologi / Framework | Port Default | Deskripsi Fungsi |
| :--- | :--- | :--- | :--- |
| **Mobile App** | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Coroutines | - | Aplikasi Android native (`com.example.kawansekolah`) |
| **Backend API** | Node.js, Express.js, Firebase Admin SDK, Axios | `3000` | Service REST API untuk pengolahan AI Analytics dan admin endpoints |
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
6. **Perangkat Penguji:** Android Emulator (API 26+) atau HP Fisik Android (versi 8.0+) yang terhubung ke jaringan Wi-Fi lokal yang sama.

---

## 🚀 Automasi Layanan Lokal (Gradle Task Hook)

Proyek ini dilengkapi dengan fitur otomasi **Gradle Task Hook** (`startLocalServices`). Ketika diaktifkan, Gradle akan otomatis menyiapkan dan menjalankan seluruh layanan backend (Docker PostgreSQL, Firebase Emulator, dan Server Node.js) setiap kali Anda menekan tombol **Run** atau **Debug** di Android Studio.

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

4. **Mengetahui IP Wi-Fi Laptop (`tampil-ip.bat`):**
   - Jalankan `tampil-ip.bat` di terminal atau klik ganda berkas tersebut untuk menampilkan alamat IPv4 Wi-Fi laptop Anda (misal: `192.168.1.5`).

5. **Konfigurasi Alamat Server pada Aplikasi:**
   - **Android Emulator:** Menggunakan IP khusus `10.0.2.2` secara otomatis.
   - **HP Fisik:** Buka dialog *Konfigurasi Alamat Server* (ikon roda gigi ⚙️ di pojok kanan atas layar Login) dan masukkan IP Wi-Fi laptop Anda.

---

## 💻 Cara Menjalankan Aplikasi

### Opsi A: Otomatis via Android Studio & Skrip Utilitas

1. Pastikan `scolarsia.autostart=true` di `local.properties`.
2. Tekan tombol **Run** (`Shift + F10`) di Android Studio.
3. Gunakan skrip utilitas pendukung sesuai kebutuhan:
   - `buka-firewall.bat` : Membuka izin port di Windows Firewall (Jalankan sebagai Admin).
   - `tampil-ip.bat` : Menampilkan alamat IP Wi-Fi laptop dan MAC Address.
   - `cek-status.bat` : Memeriksa status container Docker dan kesehatan API (`/health`).
   - `stop-all.bat` : Menghentikan layanan backend Node.js dan container Firebase Emulator.

### Opsi B: Manual via Terminal

Jika Anda ingin menjalankan layanan secara terpisah:

```powershell
# 1. Menyalakan Container Docker (PostgreSQL & Firebase Emulator)
cd C:\firebase-docker
docker compose up -d

# 2. Seeding Data PostgreSQL (Port 5434)
cd C:\Users\danip\Downloads\KawanSekolah1-master
docker exec -i firebase-docker-postgres-1 psql -U admin -d postgres < seed_postgres.sql

# 3. Seeding Data Firebase Emulator (Auth & Firestore)
cd seed-firebase
cmd.exe /c "npm install"
$env:FIREBASE_AUTH_EMULATOR_HOST="127.0.0.1:9099"
$env:FIRESTORE_EMULATOR_HOST="127.0.0.1:8081"
$env:GCLOUD_PROJECT="demo-kawansekolah"
node seed.js

# 4. Menyalakan Backend API Node.js (Port 3000)
cd ..\backend
cmd.exe /c "npm install"
npm start
```

---

## 🔑 Akun Uji Coba (Demo Accounts)

Aplikasi menerapkan penguncian hak akses yang ketat sesuai dengan konfigurasi pada `DemoAccounts.kt`. Hanya 5 akun resmi berikut yang diizinkan masuk dalam **Mode Demo**:

| Peran / Hak Akses | Email Target | Password Default | Cakupan Izin & Fitur |
| :--- | :--- | :--- | :--- |
| **Admin / Super Admin** | `admin12@scolarsia.ac.id` | `1a2d3m4i5n` | Access penuh ke modul Admin, kelola akun, data akademik, dan fitur *"Lihat sebagai"* (*Super Admin*). |
| **Guru Mata Pelajaran** | `guru12@scolarsia.ac.id` | `1g2u3r4u` | Input nilai sub-materi, impor CSV nilai, dasbor mengajar, dan beralih ke peran Wali Kelas. |
| **Wali Kelas** | `walikelas12@scolarsia.ac.id` | `1w2a3l4i` | Rekapitulasi nilai kelas bimbingan, analisis statistik perkembangan, dan beralih ke peran Guru. |
| **Siswa** | `siswa01@scolarsia.ac.id` | `1s2i3w4a` | Pemantauan nilai berbobot, analisis sub-materi terlemah, modul pelajaran, dan AI Tutor. |
| **Orang Tua** | `ortu_siswa01@scolarsia.ac.id` | `1o2r3a4n5g` | Pemantauan akademik siswa yang terhubung, laporan perkembangan, dan notifikasi sekolah. |

> **Catatan Keamanan:** Setiap akun non-admin dikunci secara otomatis pada perannya masing-masing. Percobaan navigasi ke rute di luar perannya akan diblokir oleh `RouteGuard` dan dikembalikan ke beranda yang sah.

---

## 📁 Struktur Proyek

```text
Scolarsia_Sekolah-_01/
├── app/                                  # Modul Utama Aplikasi Android
│   ├── build.gradle.kts                  # Konfigurasi Build & Gradle Task Hook
│   ├── google-services.json              # Konfigurasi Firebase Android Client
│   ├── sampledata/
│   │   └── READMETEKS_IMPLEMENTASION/    # Catatan Dokumentasi Pengerjaan & Deploy
│   └── src/
│       ├── main/
│       │   ├── java/com/example/kawansekolah/
│       │   │   ├── data/                 # Models, Repositories, & Network ApiService
│       │   │   ├── demo/                 # DemoAccounts.kt (Sumber Kebenaran Akun)
│       │   │   ├── domain/               # RouteGuard, AccessPolicy, & ScoreValidator
│       │   │   ├── ui/                   # Jetpack Compose Screen & UI Components
│       │   │   │   ├── admin/            # 13 Layar Modul Administrator
│       │   │   │   ├── ai/               # Layar AI Analytics & AI Tutor
│       │   │   │   ├── beranda/          # Layar Beranda Multi-Role
│       │   │   │   ├── components/       # TopBar, BottomBar, Avatar, dsb.
│       │   │   │   ├── feature_*/        # Nilai, Insight, Statistik, Wali Kelas
│       │   │   │   ├── login/            # Role Selection & Login Screen
│       │   │   │   └── profil/           # Profil & Pengaturan Keamanan Sesi
│       │   │   └── utils/                # DevHost, UserSessionManager, NetObserver
│       │   └── res/                      # Drawable, Mipmap, Values, & Layouts
│       └── test/                         # Unit Tests (RouteGuard & Logic Tests)
├── backend/                              # Service REST API Node.js / Express
│   ├── server.js                         # Server Utama & Middleware
│   ├── seed-manager.js                   # Manager Seeding Backend
│   └── package.json                      # Dependencies Backend Node.js
├── seed-firebase/                        # Tools Seeding Firebase Emulator
│   ├── seed.js                           # Script Seeding Auth & Firestore
│   └── package.json
├── seed_postgres.sql                     # Script DDL & Data Seed PostgreSQL
├── docker-compose.yml                    # Konfigurasi Docker Services (Postgres & Firebase)
├── firebase.json                         # Konfigurasi Emulator Ports & Rules
├── firestore.rules                       # Keamanan Firestore Rules
├── buka-firewall.bat                     # Skrip Membuka Port Windows Firewall (Admin)
├── tampil-ip.bat                         # Skrip Menampilkan IP Wi-Fi Laptop
├── cek-status.bat                        # Skrip Mengecek Status Service & Health API
├── stop-all.bat                          # Skrip Menghentikan Layanan Backend & Docker
└── README.md                             # Dokumentasi Proyek
```

---

## 👥 Tim Pengembang

Aplikasi ini dikembangkan oleh **Tim Magang PT Divistant Teknologi Indonesia** (*Program Studi D3 Manajemen Informatika — Universitas Amikom Yogyakarta*):

- 👨‍💻 **Firmansyah Ardhani Putra**
- 👩‍💻 **Anggie Nur Shafira**
- 👩‍💻 **Alifia Nurohmah**
