const admin = require('firebase-admin');

// 1. Verifikasi Environment Variable Emulator (Keamanan: Mencegah Akses Firebase Produksi)
const authHost = process.env.FIREBASE_AUTH_EMULATOR_HOST;
const firestoreHost = process.env.FIRESTORE_EMULATOR_HOST;

if (!authHost || !firestoreHost) {
  console.error('❌ ERROR KRITIS: Hentikan eksekusi!');
  console.error('FIREBASE_AUTH_EMULATOR_HOST dan FIRESTORE_EMULATOR_HOST harus di-set pada environment variable.');
  console.error('Contoh: $env:FIREBASE_AUTH_EMULATOR_HOST="127.0.0.1:9099"; $env:FIRESTORE_EMULATOR_HOST="127.0.0.1:8081"');
  process.exit(1);
}

const projectId = process.env.GCLOUD_PROJECT || 'demo-kawansekolah';

if (!admin.apps.length) {
  admin.initializeApp({ projectId });
}

const auth = admin.auth();
const db = admin.firestore();

// 2. Data Seed Akun Demo (Selaras dengan DemoAccounts.kt & seed_postgres.sql)
const DEMO_USERS = [
  {
    uid: 'admin_01',
    email: 'admin12@scolarsia.ac.id',
    password: '1a2d3m4i5n',
    displayName: 'Arif Setiawan, S.Kom.',
    role: 'ADMIN',
    roles: ['ADMIN'],
    isSuperAdmin: true,
    nis: '',
    noHp: '08123456789'
  },
  {
    uid: 'guru_01',
    email: 'guru12@scolarsia.ac.id',
    password: '1g2u3r4u',
    displayName: 'Budi Santoso, S.Pd.',
    role: 'GURU',
    roles: ['GURU', 'WALI_KELAS'],
    isSuperAdmin: false,
    nis: '',
    noHp: '08123456788',
    classesTaught: ['9-A', '9-B']
  },
  {
    uid: 'wali_01',
    email: 'walikelas12@scolarsia.ac.id',
    password: '1w2a3l4i',
    displayName: 'Siti Aminah, M.Pd.',
    role: 'WALI_KELAS',
    roles: ['WALI_KELAS', 'GURU'],
    isSuperAdmin: false,
    nis: '',
    noHp: '08123456787',
    classesTaught: ['9-A']
  },
  {
    uid: 'siswa_01',
    email: 'siswa01@scolarsia.ac.id',
    password: '1s2i3w4a',
    displayName: 'Ahmad Budi',
    role: 'SISWA',
    roles: ['SISWA'],
    isSuperAdmin: false,
    nis: '09231',
    noHp: '08123456786',
    grade: 9,
    className: '9-A'
  },
  {
    uid: 'ortu_01',
    email: 'ortu_siswa01@scolarsia.ac.id',
    password: '1o2r3a4n5g',
    displayName: 'Bapak Ahmad',
    role: 'ORANG_TUA',
    roles: ['ORANG_TUA'],
    isSuperAdmin: false,
    nis: '',
    noHp: '08123456785',
    studentIds: ['siswa_01']
  }
];

async function runSeed() {
  console.log('======================================================');
  console.log(`🔥 [Firebase Seed] Menghubungkan ke Emulator (${projectId})`);
  console.log(`   AUTH HOST      : ${authHost}`);
  console.log(`   FIRESTORE HOST : ${firestoreHost}`);
  console.log('======================================================');

  let processedUsersCount = 0;
  let processedDocsCount = 0;

  // Step A: Seed Firebase Auth & Claims (Idempotent)
  for (const user of DEMO_USERS) {
    try {
      try {
        const existingByEmail = await auth.getUserByEmail(user.email);
        if (existingByEmail.uid !== user.uid) {
          await auth.deleteUser(existingByEmail.uid);
        }
      } catch (_) {}

      try {
        await auth.getUser(user.uid);
        await auth.updateUser(user.uid, {
          email: user.email,
          password: user.password,
          displayName: user.displayName,
          disabled: false
        });
      } catch (err) {
        if (err.code === 'auth/user-not-found') {
          await auth.createUser({
            uid: user.uid,
            email: user.email,
            password: user.password,
            displayName: user.displayName,
            disabled: false
          });
        } else {
          throw err;
        }
      }

      await auth.setCustomUserClaims(user.uid, {
        roles: user.roles,
        role: user.role.toLowerCase(),
        isSuperAdmin: user.isSuperAdmin
      });

      processedUsersCount++;
    } catch (e) {
      console.error(`❌ Gagal seed Auth user ${user.email}:`, e.message);
    }
  }

  // Step B: Seed Firestore Collections (Idempotent merge: true)
  const batch = db.batch();

  // 1. Koleksi 'sekolah'
  const sekolahRef = db.collection('sekolah').doc('sekolah_01');
  batch.set(sekolahRef, {
    id: 'sekolah_01',
    nama: 'SMP Scolarsia 1',
    alamat: 'Jl. Pendidikan No. 123',
    adminUid: 'admin_01',
    createdAt: Date.now()
  }, { merge: true });
  processedDocsCount++;

  // 2. Koleksi 'pengguna' dan 'users'
  for (const user of DEMO_USERS) {
    const userDocData = {
      uid: user.uid,
      sekolahId: 'sekolah_01',
      nama: user.displayName,
      namaLengkap: user.displayName,
      email: user.email,
      role: user.role,
      roles: user.roles,
      isSuperAdmin: user.isSuperAdmin,
      nis: user.nis || '',
      noHp: user.noHp || '',
      aktif: true,
      wajibGantiSandi: false,
      schoolLevel: 'SMP',
      grade: user.grade || 9,
      className: user.className || '9-A',
      studentIds: user.studentIds || [],
      teacherId: user.role === 'GURU' || user.role === 'WALI_KELAS' ? user.uid : ''
    };

    batch.set(db.collection('pengguna').doc(user.uid), userDocData, { merge: true });
    batch.set(db.collection('users').doc(user.uid), userDocData, { merge: true });
    processedDocsCount += 2;
  }

  // 3. Koleksi 'tahunAjaran'
  batch.set(db.collection('tahunAjaran').doc('ta_2026'), {
    id: 'ta_2026',
    sekolahId: 'sekolah_01',
    nama: '2026/2027',
    aktif: true
  }, { merge: true });
  processedDocsCount++;

  // 4. Koleksi 'kelas'
  batch.set(db.collection('kelas').doc('kelas_9a'), {
    id: 'kelas_9a',
    sekolahId: 'sekolah_01',
    jenjang: 'SMP',
    nama: '9-A',
    waliKelasUid: 'wali_01'
  }, { merge: true });
  batch.set(db.collection('kelas').doc('kelas_9b'), {
    id: 'kelas_9b',
    sekolahId: 'sekolah_01',
    jenjang: 'SMP',
    nama: '9-B',
    waliKelasUid: 'guru_01'
  }, { merge: true });
  processedDocsCount += 2;

  // 5. Koleksi 'mapel'
  const mapelList = [
    { id: 'mapel_matematika', nama: 'Matematika' },
    { id: 'mapel_bindo', nama: 'Bahasa Indonesia' },
    { id: 'mapel_fisika', nama: 'Fisika' },
    { id: 'mapel_biologi', nama: 'Biologi' },
    { id: 'mapel_sejarah', nama: 'Sejarah' }
  ];
  for (const m of mapelList) {
    batch.set(db.collection('mapel').doc(m.id), {
      id: m.id,
      sekolahId: 'sekolah_01',
      nama: m.nama,
      jenjang: 'SMP'
    }, { merge: true });
    processedDocsCount++;
  }

  // 6. Koleksi 'subMateri'
  const subMateriList = [
    { id: 'sub_aritmatika', mapelId: 'mapel_matematika', nama: 'Aritmatika' },
    { id: 'sub_aljabar', mapelId: 'mapel_matematika', nama: 'Aljabar' },
    { id: 'sub_pitagoras', mapelId: 'mapel_matematika', nama: 'Pitagoras' },
    { id: 'sub_lho', mapelId: 'mapel_bindo', nama: 'Teks LHO' },
    { id: 'sub_puisi', mapelId: 'mapel_bindo', nama: 'Puisi & Majas' },
    { id: 'sub_kinematika', mapelId: 'mapel_fisika', nama: 'Kinematika Gerak' },
    { id: 'sub_newton', mapelId: 'mapel_fisika', nama: 'Hukum Newton' },
    { id: 'sub_sel', mapelId: 'mapel_biologi', nama: 'Struktur Sel' },
    { id: 'sub_genetik', mapelId: 'mapel_biologi', nama: 'Genetik & Hereditas' },
    { id: 'sub_hindu', mapelId: 'mapel_sejarah', nama: 'Kerajaan Hindu-Buddha' }
  ];
  for (const sm of subMateriList) {
    batch.set(db.collection('subMateri').doc(sm.id), sm, { merge: true });
    processedDocsCount++;
  }

  // 7. Koleksi 'periode' (Konfigurasi Bobot Nilai)
  batch.set(db.collection('periode').doc('periode_ganjil'), {
    id: 'periode_ganjil',
    sekolahId: 'sekolah_01',
    nama: 'Semester Ganjil 2026/2027',
    aktif: true,
    bobotTugas: 30.0,
    bobotUts: 30.0,
    bobotUas: 40.0
  }, { merge: true });
  processedDocsCount++;

  // 8. Koleksi 'nilai'
  const nilaiList = [
    { id: 'nil_01', mapelId: 'mapel_matematika', tugas: 90.0, uts: 88.0, uas: 92.0, nilaiAkhir: 90.2 },
    { id: 'nil_02', mapelId: 'mapel_bindo', tugas: 85.0, uts: 85.0, uas: 90.0, nilaiAkhir: 87.0 },
    { id: 'nil_03', mapelId: 'mapel_fisika', tugas: 80.0, uts: 75.0, uas: 85.0, nilaiAkhir: 80.5 },
    { id: 'nil_04', mapelId: 'mapel_biologi', tugas: 88.0, uts: 90.0, uas: 86.0, nilaiAkhir: 87.8 },
    { id: 'nil_05', mapelId: 'mapel_sejarah', tugas: 95.0, uts: 92.0, uas: 94.0, nilaiAkhir: 93.7 }
  ];
  for (const n of nilaiList) {
    batch.set(db.collection('nilai').doc(n.id), {
      id: n.id,
      siswaUid: 'siswa_01',
      kelasId: 'kelas_9a',
      mapelId: n.mapelId,
      periodeId: 'periode_ganjil',
      tugas: n.tugas,
      uts: n.uts,
      uas: n.uas,
      nilaiAkhir: n.nilaiAkhir
    }, { merge: true });
    processedDocsCount++;
  }

  // 9. Koleksi 'relasiOrangTua'
  batch.set(db.collection('relasiOrangTua').doc('relasi_01'), {
    id: 'relasi_01',
    orangTuaUid: 'ortu_01',
    siswaUid: 'siswa_01'
  }, { merge: true });
  processedDocsCount++;

  // 10. Koleksi 'notifikasi'
  const notifikasiList = [
    { id: 'notif_01', targetUid: 'siswa_01', judul: 'Nilai Matematika Diperbarui', pesan: 'Nilai tugas dan ujian semester ganjil telah diinput oleh Budi Santoso, S.Pd.', dibaca: false },
    { id: 'notif_02', targetUid: 'ortu_01', judul: 'Laporan Akademik Anak', pesan: 'Laporan nilai semester ganjil Ahmad Budi telah diperbarui oleh sekolah.', dibaca: false },
    { id: 'notif_03', targetUid: 'guru_01', judul: 'Pengingat Input Nilai', pesan: 'Batas waktu penginputan nilai semester ganjil adalah 30 Oktober 2026.', dibaca: false }
  ];
  for (const notif of notifikasiList) {
    batch.set(db.collection('notifikasi').doc(notif.id), {
      ...notif,
      timestamp: Date.now()
    }, { merge: true });
    processedDocsCount++;
  }

  // 11. Koleksi 'teaching_profiles'
  batch.set(db.collection('teaching_profiles').doc('guru_01'), {
    teacherId: 'guru_01',
    fullName: 'Budi Santoso, S.Pd.',
    schoolName: 'SMP Scolarsia 1',
    schoolLevel: 'SMP',
    classesTaught: ['9-A', '9-B'],
    profileComplete: true
  }, { merge: true });
  batch.set(db.collection('teaching_profiles').doc('wali_01'), {
    teacherId: 'wali_01',
    fullName: 'Siti Aminah, M.Pd.',
    schoolName: 'SMP Scolarsia 1',
    schoolLevel: 'SMP',
    classesTaught: ['9-A'],
    profileComplete: true
  }, { merge: true });
  processedDocsCount += 2;

  // 12. Koleksi 'subjects' & 'topics' & 'grades' & 'insights' (AcademicRepository)
  const subjectsEntityList = [
    { id: 'subj_matematika_9', teacherId: 'guru_01', name: 'Matematika', schoolLevel: 'SMP', grade: 9 },
    { id: 'subj_bindo_9', teacherId: 'guru_01', name: 'Bahasa Indonesia', schoolLevel: 'SMP', grade: 9 },
    { id: 'subj_fisika_9', teacherId: 'guru_01', name: 'Fisika', schoolLevel: 'SMP', grade: 9 },
    { id: 'subj_biologi_9', teacherId: 'guru_01', name: 'Biologi', schoolLevel: 'SMP', grade: 9 },
    { id: 'subj_sejarah_9', teacherId: 'guru_01', name: 'Sejarah', schoolLevel: 'SMP', grade: 9 }
  ];
  for (const s of subjectsEntityList) {
    batch.set(db.collection('subjects').doc(s.id), s, { merge: true });
    processedDocsCount++;
  }

  const topicsEntityList = [
    { id: 'top_aritmatika', teacherId: 'guru_01', subjectId: 'subj_matematika_9', name: 'Aritmatika', weight: 1.0 },
    { id: 'top_geometri', teacherId: 'guru_01', subjectId: 'subj_matematika_9', name: 'Geometri', weight: 2.0 },
    { id: 'top_pitagoras', teacherId: 'guru_01', subjectId: 'subj_matematika_9', name: 'Pitagoras', weight: 1.5 },
    { id: 'top_lho', teacherId: 'guru_01', subjectId: 'subj_bindo_9', name: 'Teks LHO', weight: 1.0 },
    { id: 'top_puisi', teacherId: 'guru_01', subjectId: 'subj_bindo_9', name: 'Puisi & Majas', weight: 1.0 }
  ];
  for (const t of topicsEntityList) {
    batch.set(db.collection('topics').doc(t.id), t, { merge: true });
    processedDocsCount++;
  }

  const gradesEntityList = [
    { id: 'g1', studentId: 'siswa_01', teacherId: 'guru_01', subjectId: 'subj_matematika_9', subjectName: 'Matematika', topicId: 'top_aritmatika', topicName: 'Aritmatika', score: 90.0, createdAt: Date.now() },
    { id: 'g2', studentId: 'siswa_01', teacherId: 'guru_01', subjectId: 'subj_matematika_9', subjectName: 'Matematika', topicId: 'top_geometri', topicName: 'Geometri', score: 90.0, createdAt: Date.now() },
    { id: 'g3', studentId: 'siswa_01', teacherId: 'guru_01', subjectId: 'subj_matematika_9', subjectName: 'Matematika', topicId: 'top_pitagoras', topicName: 'Pitagoras', score: 70.0, createdAt: Date.now() }
  ];
  for (const g of gradesEntityList) {
    batch.set(db.collection('grades').doc(g.id), g, { merge: true });
    processedDocsCount++;
  }

  batch.set(db.collection('insights').doc('siswa_01_subj_matematika_9'), {
    id: 'siswa_01_subj_matematika_9',
    studentId: 'siswa_01',
    subjectId: 'subj_matematika_9',
    subjectName: 'Matematika',
    finalScore: 83.3,
    weakestTopic: 'Pitagoras',
    strongestTopic: 'Aritmatika & Geometri',
    recommendation: 'Siswa Ahmad Budi menunjukkan penguasaan yang sangat baik pada Aritmatika dan Geometri (skor 90). Namun, pemahaman pada materi Pitagoras masih perlu ditingkatkan (skor 70). Disarankan melakukan latihan tambahan pada rumus segitiga siku-siku dan tripel Pitagoras.',
    generatedAt: Date.now()
  }, { merge: true });
  processedDocsCount++;

  await batch.commit();

  console.log('======================================================');
  console.log(`✅ [Firebase Seed Sukses] Selesai Memproses:`);
  console.log(`   - Akun Auth Diproses     : ${processedUsersCount} user`);
  console.log(`   - Dokumen Firestore Set  : ${processedDocsCount} dokumen`);
  console.log('======================================================');
}

runSeed().catch(e => {
  console.error('❌ Gagal menjalankan Firebase Seed:', e);
  process.exit(1);
});
