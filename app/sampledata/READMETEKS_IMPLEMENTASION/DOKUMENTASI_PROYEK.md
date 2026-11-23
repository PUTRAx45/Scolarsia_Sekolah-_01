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
- **Data Persistence:** `./emulator-data` (saved automatically on `docker compose down`)

---

## 🔑 Test Credentials
**Password for guru accounts:** `1g2u3r4u`
**Password for admin accounts:** `1a2d3m4i5n`
**Password for Wali kelas accounts:** `1w2a3l4i`
**Password for siswa accounts:** `1s2i3w4a`
**Password for Orang tua accounts:** `1o2r3a4n5g`


| Role | Email |
|---|---|
| Admin | admin12@scolarsia.ac.id |
| Guru | guru12@scolarsia.ac.id |
| Wali Kelas | walikelas12@scolarsia.ac.id |
| Siswa | siswa01@scolarsia.ac.id |
| Orang Tua | ortu_siswa01@scolarsia.ac.id |

*(Terdapat total 6 siswa dan 6 orang tua dengan pola email yang sama).*

---

## 🚀 Development Commands

Jalankan perintah ini di dalam folder `backend/`:

```bash
# Start backend server (Port 3000)
npm start

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
