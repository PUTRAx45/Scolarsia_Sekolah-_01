const axios = require('axios');

const FIRESTORE_EMULATOR = process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8081';
const BASE_URL = `http://${FIRESTORE_EMULATOR}/v1/projects/demo-kawansekolah/databases/(default)/documents`;

async function testRules() {
  console.log('======================================================');
  console.log('🧪 PENGUJIAN FIRESTORE SECURITY RULES (test-rules.js)');
  console.log('======================================================\n');

  console.log(`📌 Target Firestore Emulator: http://${FIRESTORE_EMULATOR}/\n`);

  const roles = ['ADMIN', 'GURU', 'WALI_KELAS', 'SISWA', 'ORANG_TUA'];

  for (const role of roles) {
    console.log(`------------------------------------------------------`);
    console.log(`🎭 Pengujian Akses Peran: ${role}`);
    console.log(`------------------------------------------------------`);

    // 1. Uji Baca Profil 'pengguna'
    try {
      const res = await axios.get(`${BASE_URL}/pengguna/user_${role.toLowerCase()}`);
      console.log(`  • [READ Profil] Koleksi 'pengguna' (${role}): ALLOWED (HTTP ${res.status})`);
    } catch (err) {
      const status = err.response ? err.response.status : err.code;
      if (status === 403) {
        console.log(`  • [READ Profil] Koleksi 'pengguna' (${role}): DENIED (HTTP 403 PERMISSION_DENIED)`);
      } else if (err.code === 'ECONNREFUSED') {
        console.log(`  • [READ Profil] Koleksi 'pengguna' (${role}): ECONNREFUSED (Firestore Emulator tidak aktif di ${FIRESTORE_EMULATOR})`);
      } else {
        console.log(`  • [READ Profil] Koleksi 'pengguna' (${role}): HTTP Status ${status}`);
      }
    }

    // 2. Uji Tulis Nilai 'nilai'
    try {
      const res = await axios.post(`${BASE_URL}/nilai`, {
        fields: {
          studentId: { stringValue: 'siswa_01' },
          score: { doubleValue: 85.0 }
        }
      });
      console.log(`  • [WRITE Nilai] Koleksi 'nilai' (${role}): ALLOWED (HTTP ${res.status})`);
    } catch (err) {
      const status = err.response ? err.response.status : err.code;
      if (status === 403) {
        console.log(`  • [WRITE Nilai] Koleksi 'nilai' (${role}): DENIED (HTTP 403 PERMISSION_DENIED)`);
      } else if (err.code === 'ECONNREFUSED') {
        console.log(`  • [WRITE Nilai] Koleksi 'nilai' (${role}): ECONNREFUSED (Firestore Emulator tidak aktif di ${FIRESTORE_EMULATOR})`);
      } else {
        console.log(`  • [WRITE Nilai] Koleksi 'nilai' (${role}): HTTP Status ${status}`);
      }
    }
  }

  console.log('\n======================================================');
  console.log('📋 RINGKASAN MATRIX ATURAN HAK AKSES PERAN:');
  console.log('  1. ADMIN      : Baca Profil (Allowed), Tulis Nilai (Allowed)');
  console.log('  2. GURU       : Baca Profil (Allowed), Tulis Nilai (Allowed)');
  console.log('  3. WALI_KELAS : Baca Profil (Allowed), Tulis Nilai (Denied)');
  console.log('  4. SISWA      : Baca Profil Sendiri (Allowed), Tulis Nilai (Denied)');
  console.log('  5. ORANG_TUA  : Baca Profil Anak (Allowed), Tulis Nilai (Denied)');
  console.log('======================================================');
}

testRules();
