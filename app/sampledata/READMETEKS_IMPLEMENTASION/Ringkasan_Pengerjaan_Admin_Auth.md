# Ringkasan Pekerjaan: Modul Admin & Sinkronisasi Autentikasi Demo

Tugas ini berfokus pada penyelesaian dua masalah utama:
1. Menyempurnakan pembuatan desain antarmuka (UI) dari **13 Layar Admin** yang persis menyerupai referensi gambar (dengan instruksi spesifik menggunakan data kosong/dummy).
2. Memperbaiki logika kegagalan *Login* untuk `DemoAccounts` yang tidak tersinkronisasi di lingkungan *Firebase Auth Emulator*.

---

## 1. Pembuatan Tampilan (UI) Modul Admin (Data Dummy)
Semua file Compose Screen di dalam _package_ `ui/admin` telah dirancang ulang agar menyerupai pedoman desain yang diberikan, dan diintegrasikan dengan jalur rute (_routing_) pada `MainScreen.kt`.
Daftar file baru/diperbarui:
- **`AdminMasukScreen.kt`**: UI untuk Halaman Login spesifik Admin.
- **`AdminDaftarSekolahScreen.kt`**: UI Formulir pendaftaran instansi sekolah.
- **`AdminBerandaScreen.kt`**: Tampilan dasbor statistik utama.
- **`AdminProfilScreen.kt`**: Menu pengaturan akun, *privasi*, serta fitur peralihan hak akses (*View As*).
- **`AdminPenggunaScreen.kt` & `AdminDaftarAkunScreen.kt`**: Layar untuk menampilkan daftar seluruh pengguna beserta _chip_ filter perannya dan formulir _dropdown_ pengisian akun baru (relasi *Parent-Student* diletakkan berdampingan).
- **`AdminRelasiAnakScreen.kt` & `AdminKelolaPeranScreen.kt`**: Layar pengaturan _Roles_ akun individu, pencarian/asosiasi NIS anak dengan akun orang tua.
- **`AdminDetailAkunScreen.kt`**: Ringkasan status aktif satu akun berserta _chips_ label dari `UserCardItem`.
- **`AdminNotifikasiScreen.kt`**: _List_ riwayat peristiwa akademik dan pemberitahuan _unseen_.
- **`AdminImporCsvScreen.kt`**: Antarmuka unggah berkas agregasi `.csv`.

## 2. Penyelesaian Isu Sinkronisasi Autentikasi Login (AuthRepository & Firebase)

### Akar Masalah:
Kegagalan pesan `ERROR_USER_NOT_FOUND` pada awal *Login* terjadi karena `DemoAccounts.kt` di sisi _client_ (Android) **tidak disinkronkan** ke *database* emulator Firebase. Sebaliknya, saat pengguna mencoba *Login*, `AuthRepository` berusaha menghubungkan diri langsung ke _Emulator Auth 9099_ untuk meminta Token ID — padahal akun tersebut belum eksis di _backend_ lokal.

Di sisi lain, jika aplikasi memaksa pembuatan instan (_create user via client_), Firebase Android SDK akan menimpa (meng-_override_) sesi Admin/pengguna sebelumnya dan memicu *bug logout* secara prematur.

### Solusi & Implementasi Teknis:
- **`backend/seed-manager.js`:** Diperbarui agar memuat 5 profil absolut dengan kata sandinya (Admin, Guru, Wali, Siswa, Ortu). Di saat bersamaan, semua *script* menanamkan identitas (_custom claims_ `role` dan `isSuperAdmin`) agar backend bisa langsung tahu level izin _user_.
- **`backend/server.js`:** 
  - Ditambahkan proteksi pada _middleware_ `verifyAdminToken` agar *endpoint* krusial (seperti `POST /admin/users` atau *reset password*) menolak permintaan (mengembalikan 401/403) jika token tersebut bukan dari _Super Admin_ atau akun dengan level _Role_ `ADMIN`.
  - Fungsi untuk mereset kata sandi dan membuat akun _via_ Backend _Endpoint_ menggunakan `Firebase Admin SDK`, mencegah pergantian sesi _Login_ secara paksa di perangkat penguji (Android).
- **`app/.../AuthRepository.kt`:**
  - Fungsi `loginWithEmailOrNis` dimodifikasi agar menyambung mulus dengan kebijakan verifikasi ketat.
- **`app/.../AuthViewModel.kt`**: Pengecekan hak masuk (_contoh: Guru tak bisa akses Dasbor Admin_) di_handle_ dan dievaluasi di `Repository` dan `RouteGuard`.

## 3. Tata Kelola Lingkungan Pengembangan (Clean-Up)
File-file _redundant_ (bekas log _patch_, uji coba manual, _health check curl_, dsb.) yang memperbesar repositori telah dibersihkan sepenuhnya via *Command Line*.
