package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.*;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.AvatarUtils;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardWaliActivity extends BaseWaliActivity {

    private ApiService apiService;
    private SessionManager session;
    
    private TextView tvAnakInitials, tvAnakNama, tvKelas, tvKehadiran, tvKehadiranRingkas, tvCapaian, tvStatus;
    private TextView tvKognitifVal, tvSosialVal, tvMotorikVal, tvKomunikasiVal, tvBinaDiriVal;
    private ProgressBar pbKognitif, pbSosial, pbMotorik, pbKomunikasi, pbBinaDiri;
    private LinearLayout containerCatatan, containerNotifikasi;
    private boolean kehadiranDashboardLoaded = false;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_wali);

        apiService = ApiClient.getService();
        session    = new SessionManager(this);

        initViews();
        setupNavigation();
        setupDashboardInteractions();
        
        // Tampilkan nama wali dari session
        TextView tvNama = findViewById(R.id.tv_nama_wali);
        if (tvNama != null) tvNama.setText(session.getUserNama());

        // Navigasi ke perkembangan
        if (findViewById(R.id.card_perkembangan) != null) {
            findViewById(R.id.card_perkembangan).setOnClickListener(v ->
                startActivity(new Intent(this, PerkembanganWaliActivity.class)));
        }

        loadDashboard();
        if (session.getSiswaId() != -1) {
            loadSiswaDetail(session.getSiswaId());
        }
    }

    private void initViews() {
        tvAnakInitials = findViewById(R.id.tv_anak_initials);
        tvAnakNama = findViewById(R.id.tv_anak_nama);
        tvKelas = findViewById(R.id.tv_nama_kelas);
        tvKehadiran = findViewById(R.id.tv_kehadiran_persen);
        tvKehadiranRingkas = findViewById(R.id.tv_kehadiran_ringkas);
        tvCapaian = findViewById(R.id.tv_capaian_rata);
        tvStatus = findViewById(R.id.tv_status_siswa);

        tvKognitifVal = findViewById(R.id.tv_stat_kognitif);
        tvSosialVal = findViewById(R.id.tv_stat_sosial);
        tvMotorikVal = findViewById(R.id.tv_stat_motorik);
        tvKomunikasiVal = findViewById(R.id.tv_stat_komunikasi);
        tvBinaDiriVal = findViewById(R.id.tv_stat_bina_diri);

        pbKognitif = findViewById(R.id.pb_stat_kognitif);
        pbSosial = findViewById(R.id.pb_stat_sosial);
        pbMotorik = findViewById(R.id.pb_stat_motorik);
        pbKomunikasi = findViewById(R.id.pb_stat_komunikasi);
        pbBinaDiri = findViewById(R.id.pb_stat_bina_diri);
        containerCatatan = findViewById(R.id.container_catatan_guru);
        containerNotifikasi = findViewById(R.id.container_notifikasi_wali);
    }

    private void setupDashboardInteractions() {
        setClick(R.id.card_anak_saya, BiodataSiswaWaliActivity.class);
        setClick(R.id.section_kehadiran, RekapAbsensiWaliActivity.class);
        setClick(R.id.section_capaian, PerkembanganWaliActivity.class);
        setClick(R.id.section_status, DetailPpiWaliActivity.class);
        setClick(R.id.card_perkembangan, PerkembanganWaliActivity.class);
        setClick(R.id.card_catatan_guru, LaporanWaliActivity.class);
        setClick(R.id.container_kegiatan_wali, KalenderWaliActivity.class);
    }

    private void setClick(int viewId, Class<?> destination) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> startActivity(new Intent(this, destination)));
        }
    }

    private void loadSiswaDetail() {
        int siswaId = session.getSiswaId();
        if (siswaId == -1) return;
        loadSiswaDetail(siswaId);
    }

    private void loadSiswaDetail(int siswaId) {
        apiService.getSiswaById(siswaId).enqueue(new Callback<SiswaDetailResponse>() {
            @Override
            public void onResponse(Call<SiswaDetailResponse> call, Response<SiswaDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SiswaDetail detail = response.body().getData();
                    updateSiswaUI(detail);
                }
            }

            @Override
            public void onFailure(Call<SiswaDetailResponse> call, Throwable t) {}
        });

        // Load Catatan Terbaru
        apiService.getPerkembanganSiswa(siswaId, null).enqueue(new Callback<PerkembanganListResponse>() {
            @Override
            public void onResponse(Call<PerkembanganListResponse> call, Response<PerkembanganListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCatatanUI(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {}
        });
    }

    private void updateCatatanUI(List<PerkembanganItem> data) {
        if (containerCatatan == null || data == null || data.isEmpty()) return;
        containerCatatan.removeAllViews();
        
        int limit = Math.min(data.size(), 5);
        for (int i = 0; i < limit; i++) {
            PerkembanganItem item = data.get(i);
            addCatatanItem(item);
        }
    }

    private void addCatatanItem(PerkembanganItem item) {
        View view = getLayoutInflater().inflate(R.layout.item_catatan_dashboard, containerCatatan, false);
        
        TextView tvJudul = view.findViewById(R.id.tv_catatan_judul);
        TextView tvIsi = view.findViewById(R.id.tv_catatan_isi);
        TextView tvMeta = view.findViewById(R.id.tv_catatan_meta);
        View stripe = view.findViewById(R.id.view_catatan_stripe);

        tvJudul.setText(item.getAspekNama());
        tvIsi.setText(item.getCatatan());
        tvMeta.setText(DateTimeUtils.formatDate(item.getTanggal()) + " | Oleh: " + item.getNamaGuru());
        
        // Warna stripe berdasarkan aspek atau random/static
        // if (item.getAspekKode().equals("KOG")) ... 

        view.setOnClickListener(v -> startActivity(new Intent(this, LaporanWaliActivity.class)));
        containerCatatan.addView(view);
    }

    private void updateCatatanDashboardUI(List<DashboardWaliCatatan> data) {
        if (containerCatatan == null || data == null || data.isEmpty()) return;
        containerCatatan.removeAllViews();

        int limit = Math.min(data.size(), 5);
        for (int i = 0; i < limit; i++) {
            addCatatanDashboardItem(data.get(i));
        }
    }

    private void addCatatanDashboardItem(DashboardWaliCatatan item) {
        View view = getLayoutInflater().inflate(R.layout.item_catatan_dashboard, containerCatatan, false);

        TextView tvJudul = view.findViewById(R.id.tv_catatan_judul);
        TextView tvIsi = view.findViewById(R.id.tv_catatan_isi);
        TextView tvMeta = view.findViewById(R.id.tv_catatan_meta);

        tvJudul.setText(item.getAspek() != null ? item.getAspek() : "Catatan");
        tvIsi.setText(item.getCatatan() != null ? item.getCatatan() : "-");
        tvMeta.setText(DateTimeUtils.formatDate(item.getTanggal()) + " | Oleh: " + item.getNamaGuru());

        view.setOnClickListener(v -> startActivity(new Intent(this, LaporanWaliActivity.class)));
        containerCatatan.addView(view);
    }

    private void updateNotifikasiUI(List<DashboardNotificationItem> data) {
        if (containerNotifikasi == null) return;
        containerNotifikasi.removeAllViews();

        if (data == null || data.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Belum ada notifikasi");
            empty.setTextColor(0xFFBDC3C7);
            empty.setTextSize(12);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, 16, 0, 16);
            containerNotifikasi.addView(empty);
            return;
        }

        int limit = Math.min(data.size(), 3);
        for (int i = 0; i < limit; i++) {
            addNotifikasiItem(data.get(i));
        }
    }

    private void updateKegiatanUI(List<KegiatanItem> data) {
        if (data == null || data.isEmpty()) {
            bindEmptyKegiatan("Belum ada kegiatan", "Tidak ada kegiatan berlangsung atau terdekat", "-");
            return;
        }

        KegiatanItem kegiatan = data.get(0);
        TextView tvJudul = findViewById(R.id.tv_kegiatan_wali_judul);
        TextView tvMeta = findViewById(R.id.tv_kegiatan_wali_meta);
        TextView tvDeskripsi = findViewById(R.id.tv_kegiatan_wali_deskripsi);
        ImageView imgBanner = findViewById(R.id.img_kegiatan_wali_banner);
        View container = findViewById(R.id.container_kegiatan_wali);

        if (tvJudul != null) tvJudul.setText(valueOrDash(kegiatan.getJudul()));
        if (tvMeta != null) tvMeta.setText(buildKegiatanMeta(kegiatan));
        if (tvDeskripsi != null) tvDeskripsi.setText(valueOrDash(kegiatan.getDeskripsi()));
        if (container != null) {
            container.setOnClickListener(v -> startActivity(new Intent(this, KalenderWaliActivity.class)));
        }
        if (imgBanner != null) {
            imgBanner.setVisibility(View.GONE);
            loadBannerImage(kegiatan.getBannerUrl(), imgBanner);
        }
    }

    private void bindEmptyKegiatan(String title, String meta, String description) {
        TextView tvJudul = findViewById(R.id.tv_kegiatan_wali_judul);
        TextView tvMeta = findViewById(R.id.tv_kegiatan_wali_meta);
        TextView tvDeskripsi = findViewById(R.id.tv_kegiatan_wali_deskripsi);
        ImageView imgBanner = findViewById(R.id.img_kegiatan_wali_banner);

        if (tvJudul != null) tvJudul.setText(title);
        if (tvMeta != null) tvMeta.setText(meta);
        if (tvDeskripsi != null) tvDeskripsi.setText(description);
        if (imgBanner != null) imgBanner.setVisibility(View.GONE);
    }

    private String buildKegiatanMeta(KegiatanItem kegiatan) {
        StringBuilder meta = new StringBuilder();
        meta.append(readableDate(normalizeDate(kegiatan.getTanggal())));
        if (kegiatan.getWaktuMulai() != null && !kegiatan.getWaktuMulai().trim().isEmpty()) {
            meta.append(" | ").append(trimTime(kegiatan.getWaktuMulai()));
            if (kegiatan.getWaktuSelesai() != null && !kegiatan.getWaktuSelesai().trim().isEmpty()) {
                meta.append("-").append(trimTime(kegiatan.getWaktuSelesai()));
            }
            meta.append(" WIB");
        }
        if (kegiatan.getLokasi() != null && !kegiatan.getLokasi().trim().isEmpty()) {
            meta.append(" | ").append(kegiatan.getLokasi());
        }
        return meta.toString();
    }

    private String normalizeDate(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String clean = value.replace("T", " ");
        return clean.length() >= 10 ? clean.substring(0, 10) : clean;
    }

    private String readableDate(String dateKey) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            input.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            output.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            return output.format(input.parse(dateKey));
        } catch (Exception ignored) {
            return valueOrDash(dateKey);
        }
    }

    private String trimTime(String value) {
        return value != null && value.length() >= 5 ? value.substring(0, 5) : valueOrDash(value);
    }

    private String valueOrDash(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }

    private void loadBannerImage(String url, ImageView target) {
        if (url == null || url.trim().isEmpty()) return;

        imageExecutor.execute(() -> {
            try {
                InputStream input = new URL(resolveAssetUrl(url)).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                runOnUiThread(() -> {
                    target.setImageBitmap(bitmap);
                    target.setVisibility(View.VISIBLE);
                });
            } catch (Exception ignored) {
                runOnUiThread(() -> target.setVisibility(View.GONE));
            }
        });
    }

    private String resolveAssetUrl(String url) {
        String cleanUrl = url.trim();
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            return cleanUrl;
        }
        String baseUrl = ApiClient.BASE_URL.endsWith("/")
                ? ApiClient.BASE_URL.substring(0, ApiClient.BASE_URL.length() - 1)
                : ApiClient.BASE_URL;
        return cleanUrl.startsWith("/") ? baseUrl + cleanUrl : baseUrl + "/" + cleanUrl;
    }

    private void addNotifikasiItem(DashboardNotificationItem item) {
        View view = getLayoutInflater().inflate(R.layout.item_notifikasi_wali, containerNotifikasi, false);

        TextView tvJudul = view.findViewById(R.id.tv_notifikasi_judul);
        TextView tvDeskripsi = view.findViewById(R.id.tv_notifikasi_deskripsi);
        TextView tvTanggal = view.findViewById(R.id.tv_notifikasi_tanggal);

        tvJudul.setText(item.getJudul() != null ? item.getJudul() : "Notifikasi");
        tvDeskripsi.setText(item.getDeskripsi() != null ? item.getDeskripsi() : "-");
        tvTanggal.setText(DateTimeUtils.formatDateTime(item.getTanggal()));
        view.setOnClickListener(v -> openNotifikasi(item));

        containerNotifikasi.addView(view);
    }

    private void openNotifikasi(DashboardNotificationItem item) {
        String tipe = item.getTipe() != null ? item.getTipe().toLowerCase() : "";

        if ("laporan".equals(tipe)) {
            startActivity(new Intent(this, LaporanWaliActivity.class));
        } else if ("pesan".equals(tipe) || "chat".equals(tipe)) {
            startActivity(new Intent(this, ChatWaliActivity.class));
        } else if ("pengumuman".equals(tipe)) {
            new AlertDialog.Builder(this)
                    .setTitle(item.getJudul() != null ? item.getJudul() : "Pengumuman")
                    .setMessage(item.getDeskripsi() != null ? item.getDeskripsi() : "-")
                    .setPositiveButton("Tutup", null)
                    .show();
        } else if ("kegiatan".equals(tipe) || "jadwal".equals(tipe)) {
            startActivity(new Intent(this, KalenderWaliActivity.class));
        } else {
            startActivity(new Intent(this, LaporanWaliActivity.class));
        }
    }

    private void updateSiswaUI(SiswaDetail detail) {
        if (detail == null) return;
        
        // Nama dan Kelas
        if (tvAnakNama != null) tvAnakNama.setText(detail.getNama());
        if (tvKelas != null) tvKelas.setText(detail.getNamaKelas() + " • " + detail.getKebutuhanKhusus());
        
        // Status
        if (tvStatus != null) tvStatus.setText( detail.isAktif() == 1 ? "Aktif" : "Non-Aktif" );

        // Aspek
        double totalRata = 0;
        int countAspek = 0;
        if (detail.getAspek() != null) {
            for (AspekCapaian aspek : detail.getAspek()) {
                int progress = aspek.getRataRata() != null ? aspek.getRataRata().intValue() : 0;
                if (aspek.getRataRata() != null) {
                    totalRata += aspek.getRataRata();
                    countAspek++;
                }
                String valText = progress + "%";
                
                String kode = aspek.getKode() != null ? aspek.getKode().toLowerCase() : "";
                if ("kog".equals(kode) || "kognitif".equals(kode)) {
                    if (tvKognitifVal != null) tvKognitifVal.setText(valText);
                    if (pbKognitif != null) pbKognitif.setProgress(progress);
                } else if ("sos".equals(kode) || "sosial".equals(kode)) {
                    if (tvSosialVal != null) tvSosialVal.setText(valText);
                    if (pbSosial != null) pbSosial.setProgress(progress);
                } else if ("mot".equals(kode) || "motorik".equals(kode)) {
                    if (tvMotorikVal != null) tvMotorikVal.setText(valText);
                    if (pbMotorik != null) pbMotorik.setProgress(progress);
                } else if ("kom".equals(kode) || "komunikasi".equals(kode)) {
                    if (tvKomunikasiVal != null) tvKomunikasiVal.setText(valText);
                    if (pbKomunikasi != null) pbKomunikasi.setProgress(progress);
                } else if ("bd".equals(kode) || "bina_diri".equals(kode)) {
                    if (tvBinaDiriVal != null) tvBinaDiriVal.setText(valText);
                    if (pbBinaDiri != null) pbBinaDiri.setProgress(progress);
                }
            }
        }
        
        if (countAspek > 0 && tvCapaian != null) {
            int rata = (int) (totalRata / countAspek);
            tvCapaian.setText(rata + "%");
        }
        
        // Fallback hanya dipakai kalau dashboard wali belum membawa status harian.
        if (!kehadiranDashboardLoaded && detail.getAbsensiRekap() != null && tvKehadiran != null) {
            int hadir = 0;
            int total = 0;
            for (AbsensiRekap r : detail.getAbsensiRekap()) {
                if ("H".equals(r.getStatus())) hadir = r.getJumlah();
                total += r.getJumlah();
            }
            if (total > 0) {
                int persen = (hadir * 100) / total;
                tvKehadiran.setText(persen + "%");
                if (tvKehadiranRingkas != null) {
                    tvKehadiranRingkas.setText(hadir + "/" + total + " hari bulan ini");
                }
            }
        }
    }

    private void updateKehadiranUI(DashboardWaliAnak anak) {
        if (anak == null) return;

        kehadiranDashboardLoaded = true;
        String statusHariIni = normalizeStatusAbsensi(anak.getKehadiranHariIni());
        if (tvKehadiran != null) {
            tvKehadiran.setText(statusHariIni);
        }
        if (tvKehadiranRingkas != null) {
            int total = anak.getTotalHari();
            if (total > 0) {
                tvKehadiranRingkas.setText(anak.getHadirBulan() + "/" + total + " hari bulan ini");
            } else {
                tvKehadiranRingkas.setText("Belum ada rekap bulan ini");
            }
        }
    }

    private String normalizeStatusAbsensi(String status) {
        if (status == null || status.trim().isEmpty()) return "Belum diinput";

        String value = status.trim();
        if ("H".equalsIgnoreCase(value) || "Hadir".equalsIgnoreCase(value)) return "Hadir";
        if ("S".equalsIgnoreCase(value) || "Sakit".equalsIgnoreCase(value)) return "Sakit";
        if ("I".equalsIgnoreCase(value) || "Izin".equalsIgnoreCase(value)) return "Izin";
        if ("A".equalsIgnoreCase(value) || "Alpha".equalsIgnoreCase(value)) return "Alpha";
        return value;
    }

    private void loadDashboard() {
        apiService.getDashboardWali().enqueue(new Callback<DashboardWaliResponse>() {
            @Override
            public void onResponse(Call<DashboardWaliResponse> call, Response<DashboardWaliResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DashboardWaliActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    return;
                }

                DashboardWaliData data = response.body().getData();
                if (data == null) return;

                if (data.getAnak() != null && !data.getAnak().isEmpty()) {
                    DashboardWaliAnak anak = data.getAnak().get(0);
                    session.saveSiswaId(anak.getId());

                    AvatarUtils.applyInitialAvatar(tvAnakInitials, anak.getNama(), String.valueOf(anak.getId()));
                    if (tvAnakNama != null) tvAnakNama.setText(anak.getNama());
                    if (tvKelas != null) {
                        String kebutuhan = anak.getKebutuhanKhusus() != null ? " - " + anak.getKebutuhanKhusus() : "";
                        tvKelas.setText(anak.getNamaKelas() + kebutuhan);
                    }
                    if (tvStatus != null) tvStatus.setText(anak.getStatus() != null ? anak.getStatus() : "-");
                    updateDrawerChild(anak);
                    if (tvCapaian != null) {
                        int capaian = anak.getCapaianRata() != null ? anak.getCapaianRata().intValue() : 0;
                        tvCapaian.setText(capaian + "%");
                    }
                    updateKehadiranUI(anak);

                    loadSiswaDetail(anak.getId());
                }

                TextView tvPesan = findViewById(R.id.tv_pesan_masuk);
                if (tvPesan != null) {
                    int totalNotifikasi = data.getNotifikasi() != null ? data.getNotifikasi().size() : 0;
                    tvPesan.setText(String.valueOf(totalNotifikasi));
                }

                updateCatatanDashboardUI(data.getCatatanTerbaru());
                updateNotifikasiUI(data.getNotifikasi());
                updateKegiatanUI(data.getKegiatan());
            }

            @Override
            public void onFailure(Call<DashboardWaliResponse> call, Throwable t) {
                Toast.makeText(DashboardWaliActivity.this,
                    "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        super.onDestroy();
    }

    private void updateDrawerChild(DashboardWaliAnak anak) {
        NavigationView navView = getNavigationView();
        if (navView == null || navView.getHeaderCount() == 0 || anak == null) return;

        View header = navView.getHeaderView(0);
        TextView tvChild = header.findViewById(R.id.tv_wali_child);
        if (tvChild != null) {
            String kelas = anak.getNamaKelas() != null ? " (" + anak.getNamaKelas() + ")" : "";
            tvChild.setText("Wali dari " + anak.getNama() + kelas);
        }
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_wali); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view_wali); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation_wali); }
    @Override protected int getSelfNavDrawerItemId() { return R.id.nav_wali_dashboard; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_wali_home; }
}
