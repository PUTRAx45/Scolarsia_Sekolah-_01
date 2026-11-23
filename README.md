# Scolarsia - Aplikasi Analytics & Penilaian Sekolah

## 🚀 Automasi Layanan Lokal (Gradle Task Hook)

Proyek ini mendukung penyalaan otomatis seluruh layanan lokal (Docker Desktop, Firebase Emulator Auth & Firestore, Data Seed 15 Akun, dan Backend API Node.js) setiap kali menekan **Run** atau **Debug** dari Android Studio.

### ⚙️ Cara Mengaktifkan / Mematikan Auto-Start

Pengaturan ini disimpan di file `local.properties` (tidak dimasukkan ke repositori Git):

```properties
# Untuk MENGAKTIFKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=true

# Untuk MEMATIKAN otomasi penyalaan layanan lokal:
scolarsia.autostart=false
```

---

### 🛠️ Langkah Inisialisasi Satu Kali (One-Time Setup)

Agar HP Fisik dan Emulator dapat terhubung tanpa hambatan:

1. **Jalankan `buka-firewall.bat` sebagai Administrator**:
   - Klik kanan pada `buka-firewall.bat` -> **Run as Administrator**.
   - Langkah ini membuka port 3000 (Backend), 8081 (Firestore), dan 9099 (Auth) di Windows Firewall untuk akses LAN Wi-Fi.

2. **Aktifkan "Start Docker Desktop when you sign in"**:
   - Buka **Docker Desktop** -> **Settings (⚙️)** -> **General**.
   - Centang opsi **Start Docker Desktop when you sign in** agar Docker selalu siap di latar belakang.
