package com.example.monitoringappslb.data;

import com.example.monitoringappslb.model.Ppi;
import com.example.monitoringappslb.model.RekapKehadiran;
import com.example.monitoringappslb.model.Siswa;
import java.util.ArrayList;
import java.util.List;

public class DummyDatabase {

    // 1. Data Master Siswa
    public static List<Siswa> getSiswaList() {
        List<Siswa> list = new ArrayList<>();
        list.add(new Siswa("1", "Andi Rizky", "009283741", "Jakarta", "12 Mei 2012", "Laki-laki", "Jl. Melati No. 45, Tebet", "Bambang Rizky", "Sari Indah", "95%", "85", "80", "70", "80", "75"));
        list.add(new Siswa("2", "Budi Santoso", "009283742", "Bandung", "05 Juni 2011", "Laki-laki", "Jl. Mawar No. 12, Bekasi", "Supardi", "Siti Aminah", "88%", "60", "70", "55", "70", "65"));
        list.add(new Siswa("3", "Citra Lestari", "009283743", "Surabaya", "20 Maret 2013", "Perempuan", "Perum Indah Blok C", "Herman", "Lilis", "92%", "90", "95", "85", "90", "88"));
        list.add(new Siswa("4", "Dedi Kurniawan", "009283744", "Jakarta", "15 Agustus 2012", "Laki-laki", "Jl. Elang No. 7", "Wawan", "Dewi", "85%", "75", "70", "65", "75", "72"));
        list.add(new Siswa("5", "Eka Putri", "009283745", "Jakarta", "01 Januari 2012", "Perempuan", "Jl. Kenanga No. 22", "Budi", "Ani", "98%", "95", "98", "92", "95", "96"));
        list.add(new Siswa("6", "Gita Permata", "009283746", "Malang", "10 September 2012", "Perempuan", "Jl. Anggrek No. 5", "Andi", "Ratna", "90%", "80", "85", "78", "82", "80"));
        return list;
    }
    
    public static Siswa getSiswaById(String id) {
        for (Siswa s : getSiswaList()) {
            if (s.getId().equals(id)) return s;
        }
        return getSiswaList().get(0);
    }

    // 2. Data Program PPI
    public static List<Ppi> getPpiList() {
        List<Ppi> list = new ArrayList<>();
        list.add(new Ppi("Andi Rizky", "VII/1", "Mandiri Toileting", 75, "Aktif", 
                "Mengenal angka 1-10", "Mengurangi tantrum", "Bermain bergantian", "Memegang alat tulis"));
        list.add(new Ppi("Budi Santoso", "VII/1", "Mengenal Huruf Vokal", 90, "Selesai", 
                "Membaca suku kata", "Duduk tenang 10 menit", "Menjawab sapaan", "Mewarnai bidang"));
        list.add(new Ppi("Citra Lestari", "VII/1", "Mengucap Salam", 60, "Aktif", 
                "Menulis nama sendiri", "Mengikuti antrian", "Berbagi mainan", "Mengancing baju"));
        list.add(new Ppi("Dedi Kurniawan", "VII/1", "Posisi Pensil", 30, "Aktif", 
                "Mengenal warna", "Merespon instruksi", "Menatap lawan bicara", "Melempar bola"));
        list.add(new Ppi("Gita Permata", "VII/1", "Kontak Mata 5 Detik", 45, "Aktif", 
                "Meniru bunyi huruf", "Menunggu giliran", "Meminta izin", "Berjalan lurus"));
        list.add(new Ppi("Eka Putri", "VII/1", "Mengenal Angka 1-10", 100, "Selesai", 
                "Berhitung 1-20", "Sabar menunggu", "Bekerja kelompok", "Melompat satu kaki"));
        return list;
    }

    public static Ppi getPpiByStudentName(String name) {
        for (Ppi p : getPpiList()) {
            if (p.getStudentName().equals(name)) return p;
        }
        return getPpiList().get(0);
    }

    // 3. Data untuk Spinner Nama Siswa (String array)
    public static String[] getNamaSiswaArray() {
        List<Siswa> siswaList = getSiswaList();
        String[] namaArr = new String[siswaList.size() + 1];
        namaArr[0] = "-- Pilih Siswa --";
        for (int i = 0; i < siswaList.size(); i++) {
            namaArr[i+1] = siswaList.get(i).getNama();
        }
        return namaArr;
    }

    // 4. Data Rekap Kehadiran
    public static List<RekapKehadiran> getRekapKehadiranList() {
        List<RekapKehadiran> list = new ArrayList<>();
        list.add(new RekapKehadiran("Andi Rizky", 20, 1, 1, 0));
        list.add(new RekapKehadiran("Budi Santoso", 18, 2, 2, 0));
        list.add(new RekapKehadiran("Citra Lestari", 22, 0, 0, 0));
        list.add(new RekapKehadiran("Dedi Kurniawan", 15, 3, 2, 2));
        list.add(new RekapKehadiran("Eka Putri", 21, 1, 0, 0));
        list.add(new RekapKehadiran("Gita Permata", 19, 1, 2, 0));
        return list;
    }

    // 5. Data Grafik Rata-rata Kelas (Persentase)
    public static int[] getRataRataKelas() {
        return new int[]{85, 78, 82, 75, 88}; // Kognitif, Sosial, Motorik, Komunikasi, Bina Diri
    }
}