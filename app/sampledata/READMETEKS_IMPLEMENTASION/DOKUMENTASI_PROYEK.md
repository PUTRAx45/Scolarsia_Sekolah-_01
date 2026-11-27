# Scolarsia Project Documentation

Dokumentasi dan panduan teknis proyek Scolarsia. 

---

## 📌 Architecture

### 1. Android App (`app/`)
- **Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose.
- **Roles:** Admin, Guru, Wali Kelas, Siswa, Orang Tua.
- **UI/UX:** Dynamic Dark Mode (`MaterialTheme.colorScheme`), scalable typography (`.sp`).

### 2. Backend API (`backend/`)
- **Tech Stack:** Node.js, Express.js (Port 3000).
- **AI Integration:** OpenRouter API (`openai/gpt-4o-mini`).
- **Features:** Prompt moderation, greeting detection, curriculum filtering.

### 3. Firebase & Docker (`C:\firebase-docker`)
- **Auth Emulator:** Port 9099
- **Firestore Emulator:** Port 8081
- **Firebase UI:** Port 4000
- **Data Persistence:** Data tersimpan secara permanen di dalam folder `./emulator-data` saat perintah `docker compose down` dijalankan. Apabila suatu saat data Anda hilang (atau container terhapus), Anda dapat membuat ulang 6 akun demo dengan menjalankan `npm run seed:reset` di folder `backend/`.

---

## 🔒 Aturan Akses Mode Demo
Saat ini aplikasi terkunci dalam **Mode Demo** (diaktifkan melalui flag `DEMO_MODE = true` pada file `BuildConfig` Android). 
Sistem login dikunci ketat dengan aturan berikut:
- **Hak Akses Terpisah:** Admin bisa mengakses fitur Guru/Wali, dan Guru/Wali bisa saling mengakses fitur. Namun, Siswa dan Orang Tua terisolasi total pada fiturnya masing-masing.
- Siswa hanya dapat melihat nilainya sendiri, dan Orang Tua hanya dapat melihat data anaknya sendiri tanpa bisa membuka halaman siswa.
- **Batasan Login:** Fitur lupa password, registrasi mandiri, dan login pihak ketiga (Google) dinonaktifkan sepenuhnya. Pengguna hanya dapat login menggunakan satu dari 6 akun yang tercantum di bawah ini.

---

## 🔑 Test Credentials (Demo Mode)

| Role | Email | Password | Roles (Hak Akses) |
|---|---|---|---|
| Admin | admin12@scolarsia.ac.id | 1a2d3m4i5n | ADMIN, GURU, WALI_KELAS |
| Guru | guru12@scolarsia.ac.id | 1g2u3r4u | GURU, WALI_KELAS |
| Wali Kelas | walikelas12@scolarsia.ac.id | 1w2a3l4i | WALI_KELAS, GURU |
| Siswa | siswa01@scolarsia.ac.id | 1s2i3w4a | SISWA |
| Orang Tua | ortu_siswa01@scolarsia.ac.id | 1o2r3a4n5g | ORANG_TUA |
| Akun Semua Role | demo.semua@scolarsia.ac.id | 1s2e3m4u5a | ADMIN, GURU, WALI_KELAS, SISWA, ORANG_TUA |

> **Catatan Penting:** Mode demo hanya mengizinkan 6 akun tunggal di atas untuk login. Peran Siswa dan Orang Tua terpisah total tanpa akses silang. Login dengan email di luar tabel ini akan ditolak otomatis.

---

## 🚀 Development Commands

Jalankan perintah ini di dalam folder `backend/`:

```bash
# Start backend server (Port 3000)
npm  

# Run system health and emulator checks
npm run cek

# Seed dummy data into Firebase Emulator
npm run seed

# Reset (clear and re-seed) dummy data
npm run seed:reset

# Clear only dummy data
npm run seed:clear

# Test Firestore security rules
npm run test-rules

# Test AI endpoints (Chat & Insight)
npm run test-ai
```

---

## 🗺️ Deployment Roadmap

1. **Local Testing:** Pastikan 5 role bisa login di HP fisik dan fitur AI berjalan normal.
2. **Security Check:** Jalankan `npm run test-rules` untuk menguji keamanan database Firestore.
3. **Firebase Setup:** Buat project Firebase asli (Production), unduh `google-services.json`, lalu daftarkan akun Admin pertama.
4. **Backend Deploy:** Deploy folder `backend/` ke cloud hosting (Render / Railway). Jangan lupa set environment variables: `OPENROUTER_API_KEY` & `FIREBASE_SERVICE_ACCOUNT_JSON`.
5. **Release Build:** Build APK versi rilis (tanpa IP config dialog & http cleartext).
6. **Publishing:** Siapkan Privacy Policy untuk rilis ke Google Play Store.
