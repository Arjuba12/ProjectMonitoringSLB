package com.example.monitoringappslb.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiModels {

    // ─── Base Response ────────────────────────────────────────
    public static class BaseResponse {
        @SerializedName("success") private boolean success;
        @SerializedName("message") private String message;
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // ─── Login ────────────────────────────────────────────────
    public static class LoginData {
        @SerializedName("token") private String token;
        @SerializedName("user")  private UserResponse user;
        public String getToken() { return token; }
        public UserResponse getUser() { return user; }
    }

    public static class LoginResponse extends BaseResponse {
        @SerializedName("data") private LoginData data;
        public LoginData getData() { return data; }
    }

    // ─── Siswa ────────────────────────────────────────────────
    public static class SiswaItem {
        @SerializedName("id")               private int id;
        @SerializedName("nisn")             private String nisn;
        @SerializedName("nama")             private String nama;
        @SerializedName("tgl_lahir")        private String tglLahir;
        @SerializedName("jenis_kelamin")    private String jenisKelamin;
        @SerializedName("alamat")           private String alamat;
        @SerializedName("kebutuhan_khusus") private String kebutuhanKhusus;
        @SerializedName("kelas_id")         private Integer kelasId;
        @SerializedName("nama_kelas")       private String namaKelas;
        @SerializedName("tahun_ajaran")     private String tahunAjaran;
        @SerializedName("tahun_masuk")      private Integer tahunMasuk;
        @SerializedName("is_aktif")         private Integer isAktif;
        @SerializedName("nama_wali")        private String namaWali;
        @SerializedName("alpha_bulan_ini")  private int alphaBulanIni;

        @SerializedName("target_akademik")
        private String targetAkademik;

        @SerializedName("target_motorik")
        private String targetMotorik;

        public int getId() { return id; }
        public String getNisn() { return nisn; }
        public String getNama() { return nama; }
        public String getTglLahir() { return tglLahir; }
        public String getJenisKelamin() { return jenisKelamin; }
        public String getAlamat() { return alamat; }
        public String getKebutuhanKhusus() { return kebutuhanKhusus; }
        public Integer getKelasId() { return kelasId; }
        public String getNamaKelas() { return namaKelas; }
        public String getTahunAjaran() { return tahunAjaran; }
        public Integer getTahunMasuk() { return tahunMasuk; }
        public Integer isAktif() { return isAktif; }
        public String getNamaWali() { return namaWali; }
        public int getAlphaBulanIni() { return alphaBulanIni; }

        public String getTargetAkademik() {
            return targetAkademik;
        }

        public String getTargetMotorik() {
            return targetMotorik;
        }
    }

    public static class SiswaListResponse extends BaseResponse {
        @SerializedName("data") private List<SiswaItem> data;
        public List<SiswaItem> getData() { return data; }
    }

    public static class SiswaDetailResponse extends BaseResponse {
        @SerializedName("data") private SiswaDetail data;
        public SiswaDetail getData() { return data; }
    }

    public static class SiswaRekapItem {
        @SerializedName("id") private int id;
        @SerializedName("nisn") private String nisn;
        @SerializedName("nama") private String nama;
        @SerializedName("kelas_id") private Integer kelasId;
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("hadir_persen") private Double hadirPersen;
        @SerializedName("kognitif") private Double kognitif;
        @SerializedName("sosial") private Double sosial;
        @SerializedName("motorik") private Double motorik;
        @SerializedName("komunikasi") private Double komunikasi;
        @SerializedName("bina_diri") private Double binaDiri;
        @SerializedName("kognitif_status") private String kognitifStatus;
        @SerializedName("sosial_status") private String sosialStatus;
        @SerializedName("motorik_status") private String motorikStatus;
        @SerializedName("komunikasi_status") private String komunikasiStatus;
        @SerializedName("bina_diri_status") private String binaDiriStatus;

        public int getId() { return id; }
        public String getNisn() { return nisn; }
        public String getNama() { return nama; }
        public Integer getKelasId() { return kelasId; }
        public String getNamaKelas() { return namaKelas; }
        public Double getHadirPersen() { return hadirPersen; }
        public Double getKognitif() { return kognitif; }
        public Double getSosial() { return sosial; }
        public Double getMotorik() { return motorik; }
        public Double getKomunikasi() { return komunikasi; }
        public Double getBinaDiri() { return binaDiri; }
        public String getKognitifStatus() { return kognitifStatus; }
        public String getSosialStatus() { return sosialStatus; }
        public String getMotorikStatus() { return motorikStatus; }
        public String getKomunikasiStatus() { return komunikasiStatus; }
        public String getBinaDiriStatus() { return binaDiriStatus; }
    }

    public static class SiswaRekapResponse extends BaseResponse {
        @SerializedName("data") private List<SiswaRekapItem> data;
        public List<SiswaRekapItem> getData() { return data; }
    }

    public static class SiswaDetail extends SiswaItem {
        @SerializedName("aspek") private List<AspekCapaian> aspek;
        @SerializedName("absensi_bulan_ini") private List<AbsensiRekap> absensiRekap;
        public List<AspekCapaian> getAspek() { return aspek; }
        public List<AbsensiRekap> getAbsensiRekap() { return absensiRekap; }
    }

    public static class AspekCapaian {
        @SerializedName("nama")      private String nama;
        @SerializedName("kode")      private String kode;
        @SerializedName("rata_rata") private Double rataRata;
        public String getNama() { return nama; }
        public String getKode() { return kode; }
        public Double getRataRata() { return rataRata; }
    }

    // ─── Kelas ────────────────────────────────────────────────
    public static class KelasItem {
        @SerializedName("id")              private int id;
        @SerializedName("nama_kelas")      private String namaKelas;
        @SerializedName("tingkat_id")      private int tingkatId;
        @SerializedName("tingkat_nama")    private String tingkatNama;
        @SerializedName("tahun_ajaran")    private String tahunAjaran;
        @SerializedName("kapasitas")       private int kapasitas;
        @SerializedName("jml_siswa")       private int jmlSiswa;
        @SerializedName("nama_wali_kelas") private String namaWaliKelas;
        @SerializedName("is_aktif")        private Integer isAktif;
        @SerializedName("is_wali_kelas")   private Integer isWaliKelas;
        @SerializedName("input_hari_ini")  private int inputHariIni;

        public int getId() { return id; }
        public String getNamaKelas() { return namaKelas; }
        public int getTingkatId() { return tingkatId; }
        public String getTingkatNama() { return tingkatNama; }
        public String getTahunAjaran() { return tahunAjaran; }
        public int getKapasitas() { return kapasitas; }
        public int getJmlSiswa() { return jmlSiswa; }
        public String getNamaWaliKelas() { return namaWaliKelas; }
        public Integer isAktif() { return isAktif; }
        public Integer isWaliKelas() { return isWaliKelas; }
        public int getInputHariIni() { return inputHariIni; }
    }

    public static class KelasListResponse extends BaseResponse {
        @SerializedName("data") private List<KelasItem> data;
        public List<KelasItem> getData() { return data; }
    }

    public static class TingkatItem {
        @SerializedName("id") private int id;
        @SerializedName("nama") private String nama;
        @SerializedName("keterangan") private String keterangan;

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getKeterangan() { return keterangan; }
    }

    public static class TingkatListResponse extends BaseResponse {
        @SerializedName("data") private List<TingkatItem> data;
        public List<TingkatItem> getData() { return data; }
    }

    // ─── Aspek Perkembangan ───────────────────────────────────
    public static class AspekItem {
        @SerializedName("id")    private int id;
        @SerializedName("nama")  private String nama;
        @SerializedName("kode")  private String kode;
        @SerializedName("bobot") private int bobot;
        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getKode() { return kode; }
        public int getBobot() { return bobot; }
    }

    public static class AspekListResponse extends BaseResponse {
        @SerializedName("data") private List<AspekItem> data;
        public List<AspekItem> getData() { return data; }
    }

    // ─── Perkembangan ─────────────────────────────────────────
    public static class AdminUserItem {
        @SerializedName("id") private int id;
        @SerializedName("nama") private String nama;
        @SerializedName("email") private String email;
        @SerializedName("role") private String role;
        @SerializedName("no_hp") private String noHp;
        @SerializedName("is_aktif") private Integer isAktif;
        @SerializedName("nip") private String nip;
        @SerializedName("spesialisasi") private String spesialisasi;
        @SerializedName("kelas_mengajar") private String kelasMengajar;

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getNoHp() { return noHp; }
        public Integer isAktif() { return isAktif; }
        public String getNip() { return nip; }
        public String getSpesialisasi() { return spesialisasi; }
        public String getKelasMengajar() { return kelasMengajar; }
    }

    public static class AdminUserListResponse extends BaseResponse {
        @SerializedName("data") private List<AdminUserItem> data;
        public List<AdminUserItem> getData() { return data; }
    }

    public static class PerkembanganItem {
        @SerializedName("id")         private int id;
        @SerializedName("siswa_id")   private int siswaId;
        @SerializedName("aspek_id")   private int aspekId;
        @SerializedName("aspek_nama") private String aspekNama;
        @SerializedName("aspek_kode") private String aspekKode;
        @SerializedName("capaian")    private int capaian;
        @SerializedName("catatan")    private String catatan;
        @SerializedName("tanggal")    private String tanggal;
        @SerializedName("nama_guru")  private String namaGuru;
        @SerializedName("target_utama") private String targetUtama;
        public int getId() { return id; }
        public int getSiswaId() { return siswaId; }
        public int getAspekId() { return aspekId; }
        public String getAspekNama() { return aspekNama; }
        public String getAspekKode() { return aspekKode; }
        public int getCapaian() { return capaian; }
        public String getCatatan() { return catatan; }
        public String getTanggal() { return tanggal; }
        public String getNamaGuru() { return namaGuru; }
    }

    public static class PerkembanganListResponse extends BaseResponse {
        @SerializedName("data") private List<PerkembanganItem> data;
        public List<PerkembanganItem> getData() { return data; }
    }

    // ─── Absensi ──────────────────────────────────────────────
    public static class PerkembanganRingkasanHistory {
        @SerializedName("bulan_label") private String bulanLabel;
        @SerializedName("bulan_key") private String bulanKey;
        @SerializedName("aspek") private String aspek;
        @SerializedName("kode") private String kode;
        @SerializedName("rata_rata") private Double rataRata;

        public String getBulanLabel() { return bulanLabel; }
        public String getBulanKey() { return bulanKey; }
        public String getAspek() { return aspek; }
        public String getKode() { return kode; }
        public Double getRataRata() { return rataRata; }
    }

    public static class PerkembanganRingkasanTrend {
        @SerializedName("nama") private String nama;
        @SerializedName("kode") private String kode;
        @SerializedName("bulan_ini") private Double bulanIni;
        @SerializedName("bulan_lalu") private Double bulanLalu;
        @SerializedName("trend") private String trend;

        public String getNama() { return nama; }
        public String getKode() { return kode; }
        public Double getBulanIni() { return bulanIni; }
        public Double getBulanLalu() { return bulanLalu; }
        public String getTrend() { return trend; }
    }

    public static class PerkembanganRingkasanData {
        @SerializedName("history") private List<PerkembanganRingkasanHistory> history;
        @SerializedName("trend") private List<PerkembanganRingkasanTrend> trend;

        public List<PerkembanganRingkasanHistory> getHistory() { return history; }
        public List<PerkembanganRingkasanTrend> getTrend() { return trend; }
    }

    public static class PerkembanganRingkasanResponse extends BaseResponse {
        @SerializedName("data") private PerkembanganRingkasanData data;
        public PerkembanganRingkasanData getData() { return data; }
    }

    public static class AbsensiRekap {
        @SerializedName("status")  private String status;
        @SerializedName("jumlah")  private int jumlah;
        public String getStatus() { return status; }
        public int getJumlah() { return jumlah; }
    }

    public static class AbsensiRekapBulanan {
        @SerializedName("id")         private int id;
        @SerializedName("nama")       private String nama;
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("hadir")      private int hadir;
        @SerializedName("sakit")      private int sakit;
        @SerializedName("izin")       private int izin;
        @SerializedName("alpha")      private int alpha;
        @SerializedName("total_hari") private int totalHari;
        @SerializedName("status")     private String status;

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getNamaKelas() { return namaKelas; }
        public int getHadir() { return hadir; }
        public int getSakit() { return sakit; }
        public int getIzin() { return izin; }
        public int getAlpha() { return alpha; }
        public int getTotalHari() { return totalHari; }
        public String getStatus() { return status; }
    }

    public static class AbsensiListResponse extends BaseResponse {
        @SerializedName("data") private List<AbsensiRekapBulanan> data;
        public List<AbsensiRekapBulanan> getData() { return data; }
    }

    public static class AbsensiSiswaRekapData {
        @SerializedName("rekap") private AbsensiRekapBulanan rekap;
        public AbsensiRekapBulanan getRekap() { return rekap; }
    }

    public static class AbsensiSiswaRekapResponse extends BaseResponse {
        @SerializedName("data") private AbsensiSiswaRekapData data;
        public AbsensiSiswaRekapData getData() { return data; }
    }

    // PPI
    public static class PpiDetailItem {
        @SerializedName("id") private int id;
        @SerializedName("ppi_id") private int ppiId;
        @SerializedName("aspek_id") private int aspekId;
        @SerializedName("aspek_nama") private String aspekNama;
        @SerializedName("kode") private String kode;
        @SerializedName("target") private String target;
        @SerializedName("progress") private int progress;
        @SerializedName("status") private String status;
        @SerializedName("catatan") private String catatan;

        public int getId() { return id; }
        public int getPpiId() { return ppiId; }
        public int getAspekId() { return aspekId; }
        public String getAspekNama() { return aspekNama; }
        public String getKode() { return kode; }
        public String getTarget() { return target; }
        public int getProgress() { return progress; }
        public String getStatus() { return status; }
        public String getCatatan() { return catatan; }
    }

    public static class PpiItem {
        @SerializedName("id") private int id;
        @SerializedName("siswa_id") private int siswaId;
        @SerializedName("nama_siswa") private String namaSiswa;
        @SerializedName("nama_guru") private String namaGuru;
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("kebutuhan_khusus") private String kebutuhanKhusus;
        @SerializedName("semester") private String semester;
        @SerializedName("tahun_ajaran") private String tahunAjaran;
        @SerializedName("status") private String status;
        @SerializedName("target_utama") private String targetUtama;
        @SerializedName("potensi") private String potensi;
        @SerializedName("hambatan") private String hambatan;
        @SerializedName("target_akademik") private String targetAkademik;
        @SerializedName("target_motorik") private String targetMotorik;
        @SerializedName("target_komunikasi") private String targetKomunikasi;
        @SerializedName("target_bina_diri") private String targetBinaDiri;
        @SerializedName("target_sosial") private String targetSosial;
        @SerializedName("target_perilaku") private String targetPerilaku;
        @SerializedName("total_target") private int totalTarget;
        @SerializedName("tercapai") private int tercapai;
        @SerializedName("detail") private List<PpiDetailItem> detail;

        public int getId() { return id; }
        public int getSiswaId() { return siswaId; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getNamaGuru() { return namaGuru; }
        public String getNamaKelas() { return namaKelas; }
        public String getKebutuhanKhusus() { return kebutuhanKhusus; }
        public String getSemester() { return semester; }
        public String getTahunAjaran() { return tahunAjaran; }
        public String getStatus() { return status; }
        public String getTargetUtama() { return targetUtama; }
        public String getPotensi() { return potensi; }
        public String getHambatan() { return hambatan; }
        public String getTargetAkademik() { return targetAkademik; }
        public String getTargetMotorik() { return targetMotorik; }
        public String getTargetKomunikasi() { return targetKomunikasi != null ? targetKomunikasi : targetSosial; }
        public String getTargetBinaDiri() { return targetBinaDiri != null ? targetBinaDiri : targetPerilaku; }
        public String getTargetSosial() { return getTargetKomunikasi(); }
        public String getTargetPerilaku() { return getTargetBinaDiri(); }
        public int getTotalTarget() { return totalTarget; }
        public int getTercapai() { return tercapai; }
        public List<PpiDetailItem> getDetail() { return detail; }
    }

    public static class PpiListResponse extends BaseResponse {
        @SerializedName("data") private List<PpiItem> data;
        public List<PpiItem> getData() { return data; }
    }

    public static class PpiDetailResponse extends BaseResponse {
        @SerializedName("data") private PpiItem data;
        public PpiItem getData() { return data; }
    }

    // Laporan Kelas
    public static class LaporanKelasSiswa {
        @SerializedName("id") private int id;
        @SerializedName("nama") private String nama;
        @SerializedName("kognitif") private Double kognitif;
        @SerializedName("sosial") private Double sosial;
        @SerializedName("motorik") private Double motorik;
        @SerializedName("komunikasi") private Double komunikasi;
        @SerializedName("bina_diri") private Double binaDiri;
        @SerializedName("hadir") private int hadir;
        @SerializedName("sakit") private int sakit;
        @SerializedName("izin") private int izin;
        @SerializedName("alpha") private int alpha;

        public int getId() { return id; }
        public String getNama() { return nama; }
        public Double getKognitif() { return kognitif; }
        public Double getSosial() { return sosial; }
        public Double getMotorik() { return motorik; }
        public Double getKomunikasi() { return komunikasi; }
        public Double getBinaDiri() { return binaDiri; }
        public int getHadir() { return hadir; }
        public int getSakit() { return sakit; }
        public int getIzin() { return izin; }
        public int getAlpha() { return alpha; }
    }

    public static class LaporanKelasData {
        @SerializedName("kelas") private KelasItem kelas;
        @SerializedName("siswa") private List<LaporanKelasSiswa> siswa;
        @SerializedName("catatan_naratif") private String catatanNaratif;
        @SerializedName("periode") private String periode;

        public KelasItem getKelas() { return kelas; }
        public List<LaporanKelasSiswa> getSiswa() { return siswa; }
        public String getCatatanNaratif() { return catatanNaratif; }
        public String getPeriode() { return periode; }
    }

    public static class LaporanKelasResponse extends BaseResponse {
        @SerializedName("data") private LaporanKelasData data;
        public LaporanKelasData getData() { return data; }
    }

    public static class LaporanItem {
        @SerializedName("id") private int id;
        @SerializedName("judul") private String judul;
        @SerializedName("tipe") private String tipe;
        @SerializedName("periode") private String periode;
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("nama_pembuat") private String namaPembuat;
        @SerializedName("role_pembuat") private String rolePembuat;
        @SerializedName("file_path") private String filePath;
        @SerializedName("total_siswa") private Integer totalSiswa;
        @SerializedName("total_kelas") private Integer totalKelas;
        @SerializedName("status") private String status;
        @SerializedName("created_at") private String createdAt;
        public int getId() { return id; }
        public String getJudul() { return judul; }
        public String getTipe() { return tipe; }
        public String getPeriode() { return periode; }
        public String getNamaKelas() { return namaKelas; }
        public String getNamaPembuat() { return namaPembuat; }
        public String getRolePembuat() { return rolePembuat; }
        public String getFilePath() { return filePath; }
        public Integer getTotalSiswa() { return totalSiswa; }
        public Integer getTotalKelas() { return totalKelas; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class LaporanListResponse extends BaseResponse {
        @SerializedName("data") private List<LaporanItem> data;
        public List<LaporanItem> getData() { return data; }
    }

    // ─── Dashboard Guru ───────────────────────────────────────
    public static class KelasGuruItem {
        @SerializedName("id")             private int id;
        @SerializedName("nama_kelas")     private String namaKelas;
        @SerializedName("is_wali_kelas")  private Integer isWaliKelas;
        @SerializedName("jml_siswa")      private int jmlSiswa;
        @SerializedName("input_hari_ini") private int inputHariIni;
        public int getId() { return id; }
        public String getNamaKelas() { return namaKelas; }
        public boolean isWaliKelas() { return isWaliKelas != null && isWaliKelas == 1; }
        public int getJmlSiswa() { return jmlSiswa; }
        public int getInputHariIni() { return inputHariIni; }
    }

    public static class ProgressInput {
        @SerializedName("total_siswa")  private int totalSiswa;
        @SerializedName("sudah_input")  private int sudahInput;
        public int getTotalSiswa() { return totalSiswa; }
        public int getSudahInput() { return sudahInput; }
    }

    public static class DashboardTaskItem {
        @SerializedName("tipe")      private String tipe;
        @SerializedName("judul")     private String judul;
        @SerializedName("deskripsi") private String deskripsi;
        public String getTipe() { return tipe; }
        public String getJudul() { return judul; }
        public String getDeskripsi() { return deskripsi; }
    }

    public static class DashboardNotificationItem {
        @SerializedName("tipe")      private String tipe;
        @SerializedName("judul")     private String judul;
        @SerializedName("deskripsi") private String deskripsi;
        @SerializedName("tanggal")   private String tanggal;
        public String getTipe() { return tipe; }
        public String getJudul() { return judul; }
        public String getDeskripsi() { return deskripsi; }
        public String getTanggal() { return tanggal; }
    }

    public static class KegiatanItem {
        @SerializedName("id")            private int id;
        @SerializedName("judul")         private String judul;
        @SerializedName("deskripsi")     private String deskripsi;
        @SerializedName("tanggal")       private String tanggal;
        @SerializedName("waktu_mulai")   private String waktuMulai;
        @SerializedName("waktu_selesai") private String waktuSelesai;
        @SerializedName("lokasi")        private String lokasi;
        @SerializedName("tipe")          private String tipe;
        @SerializedName("banner_url")    private String bannerUrl;
        public int getId() { return id; }
        public String getJudul() { return judul; }
        public String getDeskripsi() { return deskripsi; }
        public String getTanggal() { return tanggal; }
        public String getWaktuMulai() { return waktuMulai; }
        public String getWaktuSelesai() { return waktuSelesai; }
        public String getLokasi() { return lokasi; }
        public String getTipe() { return tipe; }
        public String getBannerUrl() { return bannerUrl; }
    }

    public static class KegiatanListResponse extends BaseResponse {
        @SerializedName("data") private List<KegiatanItem> data;
        public List<KegiatanItem> getData() { return data; }
    }

    public static class KegiatanBannerUploadData {
        @SerializedName("filename") private String filename;
        @SerializedName("path") private String path;
        @SerializedName("url") private String url;
        public String getFilename() { return filename; }
        public String getPath() { return path; }
        public String getUrl() { return url; }
    }

    public static class KegiatanBannerUploadResponse extends BaseResponse {
        @SerializedName("data") private KegiatanBannerUploadData data;
        public KegiatanBannerUploadData getData() { return data; }
    }

    public static class DashboardGuruData {
        @SerializedName("kelas")                 private List<KelasGuruItem> kelas;
        @SerializedName("siswa_perlu_perhatian") private List<SiswaItem> perluPerhatian;
        @SerializedName("pesan_masuk")           private int pesanMasuk;
        @SerializedName("progress_input")        private ProgressInput progressInput;
        @SerializedName("tugas_pending")         private List<DashboardTaskItem> tugasPending;
        @SerializedName("kegiatan")              private List<KegiatanItem> kegiatan;
        @SerializedName("notifikasi")            private List<DashboardNotificationItem> notifikasi;
        public List<KelasGuruItem> getKelas() { return kelas; }
        public List<SiswaItem> getPerluPerhatian() { return perluPerhatian; }
        public int getPesanMasuk() { return pesanMasuk; }
        public ProgressInput getProgressInput() { return progressInput; }
        public List<DashboardTaskItem> getTugasPending() { return tugasPending; }
        public List<KegiatanItem> getKegiatan() { return kegiatan; }
        public List<DashboardNotificationItem> getNotifikasi() { return notifikasi; }
    }

    public static class DashboardGuruResponse extends BaseResponse {
        @SerializedName("data") private DashboardGuruData data;
        public DashboardGuruData getData() { return data; }
    }

    // ─── Dashboard Kepsek ─────────────────────────────────────
    public static class DashboardWaliAnak {
        @SerializedName("id") private int id;
        @SerializedName("nama") private String nama;
        @SerializedName("nisn") private String nisn;
        @SerializedName("foto") private String foto;
        @SerializedName("kebutuhan_khusus") private String kebutuhanKhusus;
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("hubungan") private String hubungan;
        @SerializedName("capaian_rata") private Double capaianRata;
        @SerializedName("hadir_bulan") private int hadirBulan;
        @SerializedName("total_hari") private int totalHari;
        @SerializedName("kehadiran_hari_ini") private String kehadiranHariIni;
        @SerializedName("status") private String status;

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getNisn() { return nisn; }
        public String getFoto() { return foto; }
        public String getKebutuhanKhusus() { return kebutuhanKhusus; }
        public String getNamaKelas() { return namaKelas; }
        public String getHubungan() { return hubungan; }
        public Double getCapaianRata() { return capaianRata; }
        public int getHadirBulan() { return hadirBulan; }
        public int getTotalHari() { return totalHari; }
        public String getKehadiranHariIni() { return kehadiranHariIni; }
        public String getStatus() { return status; }
    }

    public static class DashboardWaliCatatan {
        @SerializedName("catatan") private String catatan;
        @SerializedName("tanggal") private String tanggal;
        @SerializedName("aspek") private String aspek;
        @SerializedName("nama_guru") private String namaGuru;

        public String getCatatan() { return catatan; }
        public String getTanggal() { return tanggal; }
        public String getAspek() { return aspek; }
        public String getNamaGuru() { return namaGuru; }
    }

    public static class DashboardWaliData {
        @SerializedName("anak") private List<DashboardWaliAnak> anak;
        @SerializedName("catatan_terbaru") private List<DashboardWaliCatatan> catatanTerbaru;
        @SerializedName("notifikasi") private List<DashboardNotificationItem> notifikasi;
        @SerializedName("kegiatan") private List<KegiatanItem> kegiatan;

        public List<DashboardWaliAnak> getAnak() { return anak; }
        public List<DashboardWaliCatatan> getCatatanTerbaru() { return catatanTerbaru; }
        public List<DashboardNotificationItem> getNotifikasi() { return notifikasi; }
        public List<KegiatanItem> getKegiatan() { return kegiatan; }
    }

    public static class DashboardWaliResponse extends BaseResponse {
        @SerializedName("data") private DashboardWaliData data;
        public DashboardWaliData getData() { return data; }
    }

    public static class KelasCapaian {
        @SerializedName("nama_kelas") private String namaKelas;
        @SerializedName("rata_rata")  private Double rataRata;
        public String getNamaKelas() { return namaKelas; }
        public Double getRataRata() { return rataRata; }
    }

    public static class StatusSiswa {
        @SerializedName("berkembang_baik")   private int berkembangBaik;
        @SerializedName("cukup_berkembang")  private int cukupBerkembang;
        @SerializedName("perlu_intervensi")  private int perluIntervensi;
        public int getBerkembangBaik() { return berkembangBaik; }
        public int getCukupBerkembang() { return cukupBerkembang; }
        public int getPerluIntervensi() { return perluIntervensi; }
    }

    public static class DashboardKepsekData {
        @SerializedName("total_siswa")       private int totalSiswa;
        @SerializedName("total_guru")        private int totalGuru;
        @SerializedName("total_terapis")     private int totalTerapis;
        @SerializedName("kehadiran_rata")    private double kehadiranRata;
        @SerializedName("capaian_rata")      private double capaianRata;
        @SerializedName("status_siswa")      private StatusSiswa statusSiswa;
        @SerializedName("capaian_per_kelas") private List<KelasCapaian> capaianPerKelas;

        public int getTotalSiswa() { return totalSiswa; }
        public int getTotalGuru() { return totalGuru; }
        public int getTotalTerapis() { return totalTerapis; }
        public double getKehadiranRata() { return kehadiranRata; }
        public double getCapaianRata() { return capaianRata; }
        public StatusSiswa getStatusSiswa() { return statusSiswa; }
        public List<KelasCapaian> getCapaianPerKelas() { return capaianPerKelas; }
    }

    public static class DashboardKepsekResponse extends BaseResponse {
        @SerializedName("data") private DashboardKepsekData data;
        public DashboardKepsekData getData() { return data; }
    }

    // ─── Pesan ────────────────────────────────────────────────
    public static class PesanItem {
        @SerializedName("id")            private int id;
        @SerializedName("pengirim_id")   private int pengirimId;
        @SerializedName("penerima_id")   private int penerimaId;
        @SerializedName("isi")           private String isi;
        @SerializedName("subjek")        private String subjek;
        @SerializedName("judul")         private String judul;
        @SerializedName("target_role")   private String targetRole;
        @SerializedName("status")        private String status;
        @SerializedName("is_dibaca")     private Integer isDibaca;
        @SerializedName("created_at")    private String createdAt;
        @SerializedName("nama_pengirim") private String namaPengirim;
        @SerializedName("nama_siswa")    private String namaSiswa;
        @SerializedName("nama")          private String nama;
        @SerializedName("role")          private String role;
        @SerializedName("foto")          private String foto;
        @SerializedName("unread")        private int unread;
        @SerializedName("last_message")  private String lastMessage;
        @SerializedName("last_message_at") private String lastMessageAt;

        public int getId() { return id; }
        public int getPengirimId() { return pengirimId; }
        public int getPenerimaId() { return penerimaId; }
        public String getIsi() { return isi; }
        public String getSubjek() { return subjek; }
        public String getJudul() { return judul; }
        public String getTargetRole() { return targetRole; }
        public String getStatus() { return status; }
        public boolean isDibaca() { return isDibaca != null && isDibaca == 1; }
        public String getCreatedAt() { return createdAt; }
        public String getNamaPengirim() { return namaPengirim; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getNama() { return nama; }
        public String getRole() { return role; }
        public String getFoto() { return foto; }
        public int getUnread() { return unread; }
        public String getLastMessage() { return lastMessage; }
        public String getLastMessageAt() { return lastMessageAt; }
    }

    public static class PesanListResponse extends BaseResponse {
        @SerializedName("data")   private List<PesanItem> data;
        @SerializedName("unread") private int unread;
        public List<PesanItem> getData() { return data; }
        public int getUnread() { return unread; }
    }

    // ─── Generic message ──────────────────────────────────────
    public static class MessageResponse extends BaseResponse {
        // hanya butuh success + message dari BaseResponse
    }
}
