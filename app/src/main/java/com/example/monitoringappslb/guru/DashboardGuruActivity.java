package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.*;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardGuruActivity extends BaseGuruActivity {

    private ApiService apiService;
    private SessionManager session;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_home;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_guru);

        apiService = ApiClient.getService();
        session    = new SessionManager(this);

        setupNavigation();

        // Tampilkan nama guru di header drawer
        NavigationView navigationView = getNavigationView();
        if (navigationView != null && navigationView.getHeaderCount() > 0) {
            View header = navigationView.getHeaderView(0);
            TextView tvNama = header.findViewById(R.id.tv_nama_guru);
            if (tvNama != null) tvNama.setText(session.getUserNama());
        }

        setupActions();
        renderLoadingState();
        loadDashboard();
    }

    private void setupActions() {
        View statTotalSiswa = findViewById(R.id.stat_total_siswa);
        if (statTotalSiswa != null) {
            statTotalSiswa.setOnClickListener(v ->
                startActivity(new Intent(this, DaftarSiswaActivity.class)));
        }

        View statInputHarian = findViewById(R.id.stat_input_harian);
        if (statInputHarian != null) {
            statInputHarian.setOnClickListener(v ->
                startActivity(new Intent(this, InputPerkembanganActivity.class)));
        }

        View statPerluPerhatian = findViewById(R.id.stat_perlu_perhatian);
        if (statPerluPerhatian != null) {
            statPerluPerhatian.setOnClickListener(v ->
                startActivity(new Intent(this, DaftarSiswaActivity.class)));
        }

        View statPesanMasuk = findViewById(R.id.stat_pesan_masuk);
        if (statPesanMasuk != null) {
            statPesanMasuk.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
        }
    }

    private void loadDashboard() {
        apiService.getDashboardGuru().enqueue(new Callback<DashboardGuruResponse>() {
            @Override
            public void onResponse(Call<DashboardGuruResponse> call, Response<DashboardGuruResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    renderDashboardError();
                    Toast.makeText(DashboardGuruActivity.this, "Gagal memuat dashboard", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindDashboard(response.body().getData());
            }

            @Override
            public void onFailure(Call<DashboardGuruResponse> call, Throwable t) {
                renderDashboardError();
                if (!isFinishing()) {
                    Toast.makeText(DashboardGuruActivity.this, "Tidak bisa memuat dashboard: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderLoadingState() {
        TextView tvTotal = findViewById(R.id.tv_total_siswa);
        TextView tvInput = findViewById(R.id.tv_input_progress);
        TextView tvPerlu = findViewById(R.id.tv_perlu_perhatian);
        TextView tvPesan = findViewById(R.id.tv_pesan_masuk);

        if (tvTotal != null) tvTotal.setText("-");
        if (tvInput != null) tvInput.setText("-");
        if (tvPerlu != null) tvPerlu.setText("-");
        if (tvPesan != null) tvPesan.setText("-");

        LinearLayout kelas = findViewById(R.id.container_kelas_saya);
        LinearLayout tugas = findViewById(R.id.container_tugas_pending);
        LinearLayout perhatian = findViewById(R.id.container_perlu_perhatian);
        LinearLayout notifikasi = findViewById(R.id.container_notifikasi);

        replaceContainerText(kelas, "Memuat kelas...");
        replaceContainerText(tugas, "Memuat tugas pending...");
        replaceContainerText(perhatian, "Memuat siswa perlu perhatian...");
        replaceContainerText(notifikasi, "Memuat notifikasi...");
        bindEmptyKegiatan("Memuat kegiatan...", "-", "-");
    }

    private void renderDashboardError() {
        LinearLayout kelas = findViewById(R.id.container_kelas_saya);
        LinearLayout tugas = findViewById(R.id.container_tugas_pending);
        LinearLayout perhatian = findViewById(R.id.container_perlu_perhatian);
        LinearLayout notifikasi = findViewById(R.id.container_notifikasi);

        replaceContainerText(kelas, "Dashboard belum bisa dimuat");
        replaceContainerText(tugas, "Cek koneksi atau restart backend");
        replaceContainerText(perhatian, "Data belum tersedia");
        replaceContainerText(notifikasi, "Data belum tersedia");
        bindEmptyKegiatan("Kegiatan belum bisa dimuat", "Cek koneksi atau restart backend", "-");
    }

    private void replaceContainerText(LinearLayout container, String text) {
        if (container == null) return;
        container.removeAllViews();
        container.addView(createPaddedText(text, "#7F8C8D", false));
    }

    private void bindDashboard(DashboardGuruData data) {
        if (data == null) return;

        updateKelasSaya(data);

        TextView tvTotal = findViewById(R.id.tv_total_siswa);
        if (tvTotal != null) {
            int total = 0;
            if (data.getKelas() != null) {
                for (KelasGuruItem k : data.getKelas()) total += k.getJmlSiswa();
            }
            tvTotal.setText(String.valueOf(total));
        }

        TextView tvPesan = findViewById(R.id.tv_pesan_masuk);
        if (tvPesan != null) tvPesan.setText(String.valueOf(data.getPesanMasuk()));

        TextView tvPerlu = findViewById(R.id.tv_perlu_perhatian);
        if (tvPerlu != null) {
            int count = data.getPerluPerhatian() != null ? data.getPerluPerhatian().size() : 0;
            tvPerlu.setText(String.valueOf(count));
        }

        if (data.getProgressInput() != null) {
            TextView tvInput = findViewById(R.id.tv_input_progress);
            if (tvInput != null) {
                tvInput.setText(data.getProgressInput().getSudahInput()
                    + "/" + data.getProgressInput().getTotalSiswa());
            }
        }

        updateTugasPending(data);
        updateKegiatan(data);
        updatePerluPerhatian(data);
        updateNotifikasi(data);
    }

    private void updateKelasSaya(DashboardGuruData data) {
        LinearLayout container = findViewById(R.id.container_kelas_saya);
        if (container == null) return;
        container.removeAllViews();

        if (data.getKelas() == null || data.getKelas().isEmpty()) {
            TextView empty = createSmallText("Tidak ada kelas yang ditugaskan", "#7F8C8D", false);
            container.addView(empty);
            return;
        }

        for (KelasGuruItem kelas : data.getKelas()) {
            Button button = new Button(this);
            button.setText(kelas.getNamaKelas());
            button.setTextSize(11);
            button.setAllCaps(false);
            button.setTextColor(Color.WHITE);
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2C3E50")));
            button.setOnClickListener(v -> startActivity(new Intent(this, DaftarSiswaActivity.class)));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(36), 1f);
            params.setMarginEnd(dpToPx(8));
            container.addView(button, params);
        }
    }

    private void updateTugasPending(DashboardGuruData data) {
        LinearLayout container = findViewById(R.id.container_tugas_pending);
        if (container == null) return;
        container.removeAllViews();

        if (data.getTugasPending() == null || data.getTugasPending().isEmpty()) {
            container.addView(createSmallText("Tidak ada tugas pending", "#7F8C8D", false));
            return;
        }

        for (DashboardTaskItem task : data.getTugasPending()) {
            LinearLayout row = createTwoLineRow(
                    valueOrDash(task.getJudul()),
                    valueOrDash(task.getDeskripsi()),
                    "#2C3E50",
                    "#95A5A6"
            );
            row.setOnClickListener(v -> navigateByType(task.getTipe()));
            container.addView(row);
        }
    }

    private void updateKegiatan(DashboardGuruData data) {
        if (data.getKegiatan() == null || data.getKegiatan().isEmpty()) {
            bindEmptyKegiatan("Belum ada kegiatan", "Tidak ada kegiatan berlangsung atau terdekat", "-");
            return;
        }

        KegiatanItem kegiatan = data.getKegiatan().get(0);
        TextView tvJudul = findViewById(R.id.tv_kegiatan_judul);
        TextView tvMeta = findViewById(R.id.tv_kegiatan_meta);
        TextView tvDeskripsi = findViewById(R.id.tv_kegiatan_deskripsi);
        ImageView imgBanner = findViewById(R.id.img_kegiatan_banner);
        View container = findViewById(R.id.container_kegiatan);

        if (tvJudul != null) tvJudul.setText(valueOrDash(kegiatan.getJudul()));
        if (tvMeta != null) tvMeta.setText(buildKegiatanMeta(kegiatan));
        if (tvDeskripsi != null) tvDeskripsi.setText(valueOrDash(kegiatan.getDeskripsi()));
        if (container != null) {
            container.setOnClickListener(v -> Toast.makeText(
                    this,
                    buildKegiatanMeta(kegiatan),
                    Toast.LENGTH_SHORT
            ).show());
        }

        if (imgBanner != null) {
            imgBanner.setVisibility(View.GONE);
            loadBannerImage(kegiatan.getBannerUrl(), imgBanner);
        }
    }

    private void bindEmptyKegiatan(String title, String meta, String description) {
        TextView tvJudul = findViewById(R.id.tv_kegiatan_judul);
        TextView tvMeta = findViewById(R.id.tv_kegiatan_meta);
        TextView tvDeskripsi = findViewById(R.id.tv_kegiatan_deskripsi);
        ImageView imgBanner = findViewById(R.id.img_kegiatan_banner);

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

    private void updatePerluPerhatian(DashboardGuruData data) {
        if (data.getPerluPerhatian() == null) return;
        LinearLayout container = findViewById(R.id.container_perlu_perhatian);
        if (container == null) return;
        container.removeAllViews();
        if (data.getPerluPerhatian().isEmpty()) {
            container.addView(createPaddedText("Tidak ada siswa perlu perhatian", "#7F8C8D", false));
            return;
        }
        int count = 0;
        for (SiswaItem s : data.getPerluPerhatian()) {
            if (count >= 3) break;
            
            android.widget.RelativeLayout row = new android.widget.RelativeLayout(this);
            row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            
            android.widget.LinearLayout info = new android.widget.LinearLayout(this);
            info.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_START);
            
            TextView tvNama = new TextView(this);
            tvNama.setText(s.getNama());
            tvNama.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNama.setTextSize(13);
            
            TextView tvInfo = new TextView(this);
            tvInfo.setText(s.getKebutuhanKhusus() != null ? s.getKebutuhanKhusus() : "Perlu perhatian");
            tvInfo.setTextColor(android.graphics.Color.parseColor("#E74C3C"));
            tvInfo.setTextSize(10);
            
            info.addView(tvNama);
            info.addView(tvInfo);
            row.addView(info, lp);
            
            android.widget.Button btn = new android.widget.Button(this);
            btn.setText("Detail");
            btn.setTextSize(10);
            btn.setAllCaps(false);
            android.widget.RelativeLayout.LayoutParams btnLp = new android.widget.RelativeLayout.LayoutParams(dpToPx(70), dpToPx(32));
            btnLp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_END);
            btnLp.addRule(android.widget.RelativeLayout.CENTER_VERTICAL);
            
            final int siswaId = s.getId();
            btn.setOnClickListener(v -> {
                Intent i = new Intent(this, DetailSiswaActivity.class);
                i.putExtra("SISWA_ID", siswaId);
                startActivity(i);
            });

            row.addView(btn, btnLp);
            container.addView(row);
            count++;
        }
    }

    private void updateNotifikasi(DashboardGuruData data) {
        LinearLayout container = findViewById(R.id.container_notifikasi);
        if (container == null) return;
        container.removeAllViews();

        if (data.getNotifikasi() == null || data.getNotifikasi().isEmpty()) {
            container.addView(createPaddedText("Tidak ada notifikasi baru", "#7F8C8D", false));
            return;
        }

        for (DashboardNotificationItem item : data.getNotifikasi()) {
            LinearLayout row = createTwoLineRow(
                    valueOrDash(item.getJudul()),
                    valueOrDash(item.getDeskripsi()),
                    "#2C3E50",
                    "#95A5A6"
            );
            row.setOnClickListener(v -> navigateByType(item.getTipe()));
            container.addView(row);
        }
    }

    private LinearLayout createTwoLineRow(String title, String subtitle, String titleColor, String subtitleColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        row.setClickable(true);
        row.setFocusable(true);

        TextView tvTitle = createSmallText(title, titleColor, true);
        TextView tvSubtitle = createSmallText(subtitle, subtitleColor, false);
        tvSubtitle.setPadding(0, dpToPx(2), 0, dpToPx(6));

        row.addView(tvTitle);
        row.addView(tvSubtitle);
        return row;
    }

    private TextView createPaddedText(String text, String color, boolean bold) {
        TextView tv = createSmallText(text, color, bold);
        tv.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        return tv;
    }

    private TextView createSmallText(String text, String color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor(color));
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void navigateByType(String type) {
        if ("input_perkembangan".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, InputPerkembanganActivity.class));
        } else if ("pesan".equalsIgnoreCase(type) || "konsultasi".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, ChatActivity.class));
        } else if ("siswa".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, DaftarSiswaActivity.class));
        } else if ("pengumuman".equalsIgnoreCase(type)) {
            startActivity(new Intent(this, KirimPengumumanActivity.class));
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
