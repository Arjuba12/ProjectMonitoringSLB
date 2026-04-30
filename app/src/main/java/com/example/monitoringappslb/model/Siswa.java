package com.example.monitoringappslb.model;

public class Siswa {
    private String id;
    private String nama;
    private String nisn;
    private String statusAbsensi; // "H", "S", "I", "A"
    private String tempatLahir;
    private String tanggalLahir;
    private String jenisKelamin;
    private String alamat;
    private String namaAyah;
    private String namaIbu;
    private String kelas;
    
    // Performance stats
    private String statsKehadiran;
    private String statsKognitif;
    private String statsSosial;
    
    // Additional performance stats for Table
    private String statsMotorik;
    private String statsKomunikasi;
    private String statsBinaDiri;

    public Siswa(String id, String nama, String nisn) {
        this.id = id;
        this.nama = nama;
        this.nisn = nisn;
        this.statusAbsensi = "H"; // Default hadir
        this.kelas = "VII-A"; // Default
    }

    public Siswa(String id, String nama, String nisn, String tempatLahir, String tanggalLahir, String jenisKelamin, String alamat, String namaAyah, String namaIbu, 
                 String statsKehadiran, String statsKognitif, String statsSosial, String statsMotorik, String statsKomunikasi, String statsBinaDiri) {
        this.id = id;
        this.nama = nama;
        this.nisn = nisn;
        this.tempatLahir = tempatLahir;
        this.tanggalLahir = tanggalLahir;
        this.jenisKelamin = jenisKelamin;
        this.alamat = alamat;
        this.namaAyah = namaAyah;
        this.namaIbu = namaIbu;
        this.statsKehadiran = statsKehadiran;
        this.statsKognitif = statsKognitif;
        this.statsSosial = statsSosial;
        this.statsMotorik = statsMotorik;
        this.statsKomunikasi = statsKomunikasi;
        this.statsBinaDiri = statsBinaDiri;
        this.statusAbsensi = "H";
        this.kelas = "VII-A";
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getNisn() { return nisn; }
    public String getStatusAbsensi() { return statusAbsensi; }
    public void setStatusAbsensi(String statusAbsensi) { this.statusAbsensi = statusAbsensi; }
    
    public String getTempatLahir() { return tempatLahir; }
    public String getTanggalLahir() { return tanggalLahir; }
    public String getJenisKelamin() { return jenisKelamin; }
    public String getAlamat() { return alamat; }
    public String getNamaAyah() { return namaAyah; }
    public String getNamaIbu() { return namaIbu; }
    public String getKelas() { return kelas; }
    public String getStatsKehadiran() { return statsKehadiran; }
    public String getStatsKognitif() { return statsKognitif; }
    public String getStatsSosial() { return statsSosial; }
    public String getStatsMotorik() { return statsMotorik; }
    public String getStatsKomunikasi() { return statsKomunikasi; }
    public String getStatsBinaDiri() { return statsBinaDiri; }
}