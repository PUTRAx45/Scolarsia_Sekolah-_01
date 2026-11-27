-- Script Seed PostgreSQL untuk Scolarsia (Idempotent)
-- Target: localhost:5434 | Database: postgres | Schema: public

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Tabel Sekolah
CREATE TABLE IF NOT EXISTS sekolah (
    id VARCHAR(50) PRIMARY KEY,
    nama VARCHAR(255) NOT NULL,
    alamat TEXT,
    admin_uid VARCHAR(100),
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)
);

-- 2. Tabel Users / Pengguna
CREATE TABLE IF NOT EXISTS users (
    uid VARCHAR(100) PRIMARY KEY,
    sekolah_id VARCHAR(50) REFERENCES sekolah(id) ON DELETE SET NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nama VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'SISWA',
    roles TEXT[] DEFAULT '{}',
    is_super_admin BOOLEAN DEFAULT FALSE,
    nis VARCHAR(50),
    no_hp VARCHAR(50),
    aktif BOOLEAN DEFAULT TRUE,
    wajib_ganti_sandi BOOLEAN DEFAULT FALSE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)
);

-- 3. Tabel Kelas
CREATE TABLE IF NOT EXISTS kelas (
    id VARCHAR(50) PRIMARY KEY,
    sekolah_id VARCHAR(50) REFERENCES sekolah(id) ON DELETE CASCADE,
    jenjang VARCHAR(20) DEFAULT 'SMP',
    nama VARCHAR(50) NOT NULL,
    wali_kelas_uid VARCHAR(100) REFERENCES users(uid) ON DELETE SET NULL
);

-- 4. Tabel Mapel (Mata Pelajaran)
CREATE TABLE IF NOT EXISTS mapel (
    id VARCHAR(50) PRIMARY KEY,
    sekolah_id VARCHAR(50) REFERENCES sekolah(id) ON DELETE CASCADE,
    nama VARCHAR(100) NOT NULL,
    category VARCHAR(50) DEFAULT 'Eksakta',
    jenjang VARCHAR(20) DEFAULT 'SMP',
    grade INT DEFAULT 9
);

-- 5. Tabel Sub Materi / Modul
CREATE TABLE IF NOT EXISTS sub_materi (
    id VARCHAR(50) PRIMARY KEY,
    mapel_id VARCHAR(50) REFERENCES mapel(id) ON DELETE CASCADE,
    nama VARCHAR(255) NOT NULL,
    weight NUMERIC(5,2) DEFAULT 1.0,
    duration VARCHAR(50) DEFAULT '45 Menit'
);

-- 6. Tabel Tugas
CREATE TABLE IF NOT EXISTS tugas (
    id VARCHAR(50) PRIMARY KEY,
    subject_name VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    deadline VARCHAR(100) NOT NULL,
    is_checked BOOLEAN DEFAULT FALSE
);

-- 7. Tabel Periode & Konfigurasi Bobot Nilai
CREATE TABLE IF NOT EXISTS periode (
    id VARCHAR(50) PRIMARY KEY,
    sekolah_id VARCHAR(50) REFERENCES sekolah(id) ON DELETE CASCADE,
    nama VARCHAR(100) NOT NULL,
    aktif BOOLEAN DEFAULT TRUE,
    bobot_tugas NUMERIC(5,2) DEFAULT 30.00,
    bobot_uts NUMERIC(5,2) DEFAULT 30.00,
    bobot_uas NUMERIC(5,2) DEFAULT 40.00
);

-- 8. Tabel Nilai
CREATE TABLE IF NOT EXISTS nilai (
    id VARCHAR(50) PRIMARY KEY,
    siswa_uid VARCHAR(100) REFERENCES users(uid) ON DELETE CASCADE,
    kelas_id VARCHAR(50) REFERENCES kelas(id) ON DELETE CASCADE,
    mapel_id VARCHAR(50) REFERENCES mapel(id) ON DELETE CASCADE,
    periode_id VARCHAR(50) REFERENCES periode(id) ON DELETE CASCADE,
    tugas NUMERIC(5,2) DEFAULT 0.00,
    uts NUMERIC(5,2) DEFAULT 0.00,
    uas NUMERIC(5,2) DEFAULT 0.00,
    nilai_akhir NUMERIC(5,2)
);

-- 9. Tabel Relasi Orang Tua & Siswa
CREATE TABLE IF NOT EXISTS relasi_orang_tua (
    id VARCHAR(50) PRIMARY KEY,
    orang_tua_uid VARCHAR(100) REFERENCES users(uid) ON DELETE CASCADE,
    siswa_uid VARCHAR(100) REFERENCES users(uid) ON DELETE CASCADE,
    CONSTRAINT unique_relasi_ortu_siswa UNIQUE (orang_tua_uid, siswa_uid)
);

-- 10. Tabel Notifikasi
CREATE TABLE IF NOT EXISTS notifikasi (
    id VARCHAR(50) PRIMARY KEY,
    target_uid VARCHAR(100) REFERENCES users(uid) ON DELETE CASCADE,
    judul VARCHAR(255) NOT NULL,
    pesan TEXT NOT NULL,
    dibaca BOOLEAN DEFAULT FALSE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)
);

-- ==========================================
-- DATA SEED (IDEMPOTENT VIA ON CONFLICT)
-- ==========================================

-- Seed Sekolah
INSERT INTO sekolah (id, nama, alamat, admin_uid)
VALUES ('sekolah_01', 'SMP Scolarsia 1', 'Jl. Pendidikan No. 123', 'admin_01')
ON CONFLICT (id) DO UPDATE SET
    nama = EXCLUDED.nama,
    alamat = EXCLUDED.alamat,
    admin_uid = EXCLUDED.admin_uid;

-- Seed Users / Pengguna (Kredensial dari DemoAccounts.kt)
INSERT INTO users (uid, sekolah_id, email, password_hash, nama, role, roles, is_super_admin, nis, no_hp, aktif)
VALUES
('admin_01', 'sekolah_01', 'admin12@scolarsia.ac.id', crypt('1a2d3m4i5n', gen_salt('bf')), 'Arif Setiawan, S.Kom.', 'ADMIN', ARRAY['ADMIN'], TRUE, '', '08123456789', TRUE),
('guru_01', 'sekolah_01', 'guru12@scolarsia.ac.id', crypt('1g2u3r4u', gen_salt('bf')), 'Budi Santoso, S.Pd.', 'GURU', ARRAY['GURU', 'WALI_KELAS'], FALSE, '', '08123456788', TRUE),
('wali_01', 'sekolah_01', 'walikelas12@scolarsia.ac.id', crypt('1w2a3l4i', gen_salt('bf')), 'Siti Aminah, M.Pd.', 'WALI_KELAS', ARRAY['WALI_KELAS', 'GURU'], FALSE, '', '08123456787', TRUE),
('siswa_01', 'sekolah_01', 'siswa01@scolarsia.ac.id', crypt('1s2i3w4a', gen_salt('bf')), 'Ahmad Budi', 'SISWA', ARRAY['SISWA'], FALSE, '09231', '08123456786', TRUE),
('ortu_01', 'sekolah_01', 'ortu_siswa01@scolarsia.ac.id', crypt('1o2r3a4n5g', gen_salt('bf')), 'Bapak Ahmad', 'ORANG_TUA', ARRAY['ORANG_TUA'], FALSE, '', '08123456785', TRUE)
ON CONFLICT (uid) DO UPDATE SET
    sekolah_id = EXCLUDED.sekolah_id,
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    nama = EXCLUDED.nama,
    role = EXCLUDED.role,
    roles = EXCLUDED.roles,
    is_super_admin = EXCLUDED.is_super_admin,
    nis = EXCLUDED.nis,
    no_hp = EXCLUDED.no_hp,
    aktif = EXCLUDED.aktif;

-- Update sekolah admin_uid FK
UPDATE sekolah SET admin_uid = 'admin_01' WHERE id = 'sekolah_01';

-- Seed Kelas
INSERT INTO kelas (id, sekolah_id, jenjang, nama, wali_kelas_uid)
VALUES
('kelas_9a', 'sekolah_01', 'SMP', '9-A', 'wali_01'),
('kelas_9b', 'sekolah_01', 'SMP', '9-B', 'guru_01')
ON CONFLICT (id) DO UPDATE SET
    sekolah_id = EXCLUDED.sekolah_id,
    jenjang = EXCLUDED.jenjang,
    nama = EXCLUDED.nama,
    wali_kelas_uid = EXCLUDED.wali_kelas_uid;

-- Seed Mapel (5 Mata Pelajaran)
INSERT INTO mapel (id, sekolah_id, nama, category, jenjang, grade)
VALUES
('mapel_matematika', 'sekolah_01', 'Matematika', 'Eksakta', 'SMP', 9),
('mapel_bindo', 'sekolah_01', 'Bahasa Indonesia', 'Bahasa', 'SMP', 9),
('mapel_fisika', 'sekolah_01', 'Fisika', 'Eksakta', 'SMP', 9),
('mapel_biologi', 'sekolah_01', 'Biologi', 'Eksakta', 'SMP', 9),
('mapel_sejarah', 'sekolah_01', 'Sejarah', 'Sosial', 'SMP', 9)
ON CONFLICT (id) DO UPDATE SET
    nama = EXCLUDED.nama,
    category = EXCLUDED.category,
    jenjang = EXCLUDED.jenjang,
    grade = EXCLUDED.grade;

-- Seed Sub Materi / Modul
INSERT INTO sub_materi (id, mapel_id, nama, weight, duration)
VALUES
('sub_aritmatika', 'mapel_matematika', 'Aritmatika', 1.0, '45 Menit'),
('sub_aljabar', 'mapel_matematika', 'Aljabar', 1.5, '60 Menit'),
('sub_pitagoras', 'mapel_matematika', 'Pitagoras', 1.5, '50 Menit'),
('sub_lho', 'mapel_bindo', 'Teks LHO', 1.0, '40 Menit'),
('sub_puisi', 'mapel_bindo', 'Puisi & Majas', 1.0, '40 Menit'),
('sub_kinematika', 'mapel_fisika', 'Kinematika Gerak', 1.0, '50 Menit'),
('sub_newton', 'mapel_fisika', 'Hukum Newton', 1.5, '60 Menit'),
('sub_sel', 'mapel_biologi', 'Struktur Sel', 1.0, '50 Menit'),
('sub_genetik', 'mapel_biologi', 'Genetik & Hereditas', 2.0, '65 Menit'),
('sub_hindu', 'mapel_sejarah', 'Kerajaan Hindu-Buddha', 1.0, '50 Menit')
ON CONFLICT (id) DO UPDATE SET
    mapel_id = EXCLUDED.mapel_id,
    nama = EXCLUDED.nama,
    weight = EXCLUDED.weight,
    duration = EXCLUDED.duration;

-- Seed Tugas
INSERT INTO tugas (id, subject_name, title, deadline, is_checked)
VALUES
('task_01', 'Matematika', 'Latihan Soal Aljabar Bab 3', 'Besok, 23:59', FALSE),
('task_02', 'Bahasa Indonesia', 'Menulis Essay Lingkungan Hidup', '20 Okt 2025', FALSE),
('task_03', 'Ilmu Pengetahuan Alam', 'Praktikum Fotosintesis', '22 Okt 2025', FALSE),
('task_04', 'Matematika', 'Quiz Trigonometri & Vektor', '25 Okt 2025', FALSE),
('task_05', 'Fisika', 'Laporan Praktikum Hukum Newton', '28 Okt 2025', FALSE)
ON CONFLICT (id) DO UPDATE SET
    subject_name = EXCLUDED.subject_name,
    title = EXCLUDED.title,
    deadline = EXCLUDED.deadline,
    is_checked = EXCLUDED.is_checked;

-- Seed Periode & Konfigurasi Bobot Nilai
INSERT INTO periode (id, sekolah_id, nama, aktif, bobot_tugas, bobot_uts, bobot_uas)
VALUES ('periode_ganjil', 'sekolah_01', 'Semester Ganjil 2026/2027', TRUE, 30.00, 30.00, 40.00)
ON CONFLICT (id) DO UPDATE SET
    nama = EXCLUDED.nama,
    aktif = EXCLUDED.aktif,
    bobot_tugas = EXCLUDED.bobot_tugas,
    bobot_uts = EXCLUDED.bobot_uts,
    bobot_uas = EXCLUDED.bobot_uas;

-- Seed Nilai
INSERT INTO nilai (id, siswa_uid, kelas_id, mapel_id, periode_id, tugas, uts, uas, nilai_akhir)
VALUES
('nil_01', 'siswa_01', 'kelas_9a', 'mapel_matematika', 'periode_ganjil', 90.00, 88.00, 92.00, 90.20),
('nil_02', 'siswa_01', 'kelas_9a', 'mapel_bindo', 'periode_ganjil', 85.00, 85.00, 90.00, 87.00),
('nil_03', 'siswa_01', 'kelas_9a', 'mapel_fisika', 'periode_ganjil', 80.00, 75.00, 85.00, 80.50),
('nil_04', 'siswa_01', 'kelas_9a', 'mapel_biologi', 'periode_ganjil', 88.00, 90.00, 86.00, 87.80),
('nil_05', 'siswa_01', 'kelas_9a', 'mapel_sejarah', 'periode_ganjil', 95.00, 92.00, 94.00, 93.70)
ON CONFLICT (id) DO UPDATE SET
    siswa_uid = EXCLUDED.siswa_uid,
    kelas_id = EXCLUDED.kelas_id,
    mapel_id = EXCLUDED.mapel_id,
    periode_id = EXCLUDED.periode_id,
    tugas = EXCLUDED.tugas,
    uts = EXCLUDED.uts,
    uas = EXCLUDED.uas,
    nilai_akhir = EXCLUDED.nilai_akhir;

-- Seed Relasi Orang Tua & Siswa
INSERT INTO relasi_orang_tua (id, orang_tua_uid, siswa_uid)
VALUES ('relasi_01', 'ortu_01', 'siswa_01')
ON CONFLICT (id) DO UPDATE SET
    orang_tua_uid = EXCLUDED.orang_tua_uid,
    siswa_uid = EXCLUDED.siswa_uid;

-- Seed Notifikasi
INSERT INTO notifikasi (id, target_uid, judul, pesan, dibaca)
VALUES
('notif_01', 'siswa_01', 'Nilai Matematika Diperbarui', 'Nilai tugas dan ujian semester ganjil telah diinput oleh Budi Santoso, S.Pd.', FALSE),
('notif_02', 'ortu_01', 'Laporan Akademik Anak', 'Laporan nilai semester ganjil Ahmad Budi telah diperbarui oleh sekolah.', FALSE),
('notif_03', 'guru_01', 'Pengingat Input Nilai', 'Batas waktu penginputan nilai semester ganjil adalah 30 Oktober 2026.', FALSE)
ON CONFLICT (id) DO UPDATE SET
    target_uid = EXCLUDED.target_uid,
    judul = EXCLUDED.judul,
    pesan = EXCLUDED.pesan,
    dibaca = EXCLUDED.dibaca;

COMMIT;
