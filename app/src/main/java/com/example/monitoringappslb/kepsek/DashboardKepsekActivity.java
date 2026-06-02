package com.example.monitoringappslb.kepsek;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardKepsekActivity extends BaseKepsekActivity {

    private TextView tvTotalSiswa, tvTotalGuru, tvTotalTerapis, tvKehadiranRata, tvCapaianRata;
    private TextView tvKinerjaStatus, tvKinerjaPeriode;
    private LinearLayout containerStatusSiswa, containerCapaianKelas, containerKinerjaGuru;
    private ApiService apiService;
    private int bulan, tahun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_kepsek);

        apiService = ApiClient.getService();

        tvTotalSiswa = findViewById(R.id.tv_total_siswa);
        tvTotalGuru  = findViewById(R.id.tv_total_guru);
        tvTotalTerapis = findViewById(R.id.tv_total_terapis);
        tvKehadiranRata = findViewById(R.id.tv_kehadiran_rata);
        tvCapaianRata = findViewById(R.id.tv_capaian_rata);
        containerStatusSiswa = findViewById(R.id.container_status_siswa);
        containerCapaianKelas = findViewById(R.id.container_capaian_kelas);
        containerKinerjaGuru = findViewById(R.id.container_kinerja_guru);
        tvKinerjaStatus = findViewById(R.id.tv_kinerja_status);
        tvKinerjaPeriode = findViewById(R.id.tv_kinerja_periode);

        Calendar calendar = Calendar.getInstance();
        bulan = calendar.get(Calendar.MONTH) + 1;
        tahun = calendar.get(Calendar.YEAR);
        if (tvKinerjaPeriode != null) {
            tvKinerjaPeriode.setText("Pantau input perkembangan guru " + monthName(bulan) + " " + tahun + ".");
        }

        setupNavigation();
        setupActions();
        loadDashboard();
        loadKinerjaGuru();
    }

    private void setupActions() {
        View btnRekap = findViewById(R.id.btn_lihat_rekap);
        if (btnRekap != null) {
            btnRekap.setOnClickListener(v -> startActivity(new Intent(this, RekapSekolahActivity.class)));
        }
        View btnPengumuman = findViewById(R.id.btn_kirim_pengumuman);
        if (btnPengumuman != null) {
            btnPengumuman.setOnClickListener(v -> startActivity(new Intent(this, KirimPengumumanKepsekActivity.class)));
        }
    }

    private void loadDashboard() {
        apiService.getDashboardKepsek().enqueue(new Callback<DashboardKepsekResponse>() {
            @Override
            public void onResponse(Call<DashboardKepsekResponse> call, Response<DashboardKepsekResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(DashboardKepsekActivity.this,
                        "Gagal memuat dashboard", Toast.LENGTH_SHORT).show();
                    return;
                }

                DashboardKepsekData data = response.body().getData();
                if (data == null) return;

                if (tvTotalSiswa != null) tvTotalSiswa.setText(String.valueOf(data.getTotalSiswa()));
                if (tvTotalGuru != null) tvTotalGuru.setText(String.valueOf(data.getTotalGuru()));
                if (tvTotalTerapis != null) tvTotalTerapis.setText("Terapis: " + data.getTotalTerapis());
                if (tvKehadiranRata != null) tvKehadiranRata.setText(percent(data.getKehadiranRata()));
                if (tvCapaianRata != null) tvCapaianRata.setText(percent(data.getCapaianRata()));
                bindStatusSiswa(data);
                bindCapaianKelas(data);
            }

            @Override
            public void onFailure(Call<DashboardKepsekResponse> call, Throwable t) {
                Toast.makeText(DashboardKepsekActivity.this,
                    "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadKinerjaGuru() {
        setKinerjaStatus("Memuat kinerja guru...", true);
        if (containerKinerjaGuru != null) containerKinerjaGuru.removeAllViews();

        apiService.getKinerjaGuru(bulan, tahun).enqueue(new Callback<GuruKinerjaListResponse>() {
            @Override
            public void onResponse(Call<GuruKinerjaListResponse> call, Response<GuruKinerjaListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setKinerjaStatus("Gagal memuat kinerja guru", true);
                    Toast.makeText(DashboardKepsekActivity.this, "Gagal memuat kinerja guru", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindKinerjaGuru(response.body().getData());
            }

            @Override
            public void onFailure(Call<GuruKinerjaListResponse> call, Throwable t) {
                setKinerjaStatus("Tidak bisa terhubung ke server", true);
                Toast.makeText(DashboardKepsekActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindKinerjaGuru(List<GuruKinerjaItem> list) {
        if (containerKinerjaGuru == null) return;
        containerKinerjaGuru.removeAllViews();

        if (list == null || list.isEmpty()) {
            setKinerjaStatus("Belum ada data kinerja guru", true);
            return;
        }

        int perluPerhatian = 0;
        for (GuruKinerjaItem item : list) {
            if ("Perlu Perhatian".equalsIgnoreCase(item.getStatus())) perluPerhatian++;
            addKinerjaRow(item);
        }

        setKinerjaStatus(
                list.size() + " guru dipantau" + (perluPerhatian > 0 ? " | " + perluPerhatian + " perlu perhatian" : ""),
                true
        );
    }

    private void addKinerjaRow(GuruKinerjaItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvName = createText(valueOrDash(item.getNamaGuru()), "#1E293B", 14, true);
        TextView tvPercent = createText(percentText(item.getPersenTepatWaktu()), statusColor(item.getStatus()), 14, true);
        topRow.addView(tvName, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(tvPercent);
        row.addView(topRow);

        addText(row, valueOrDash(item.getKelas()), "#64748B", 12, false);
        addText(row, item.getTotalInput() + " input | " + item.getTotalSiswa() + " siswa | " + valueOrDash(item.getStatus()),
                statusColor(item.getStatus()), 12, true);
        addText(row, "Input terakhir: " + formatDate(item.getInputTerakhir()), "#64748B", 12, false);

        if (!"Baik".equalsIgnoreCase(item.getStatus())) {
            MaterialButton reminderButton = new MaterialButton(this);
            reminderButton.setText("Kirim Pengingat");
            reminderButton.setTextSize(11);
            reminderButton.setTextColor(Color.WHITE);
            reminderButton.setAllCaps(false);
            reminderButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E293B")));
            reminderButton.setInsetTop(0);
            reminderButton.setInsetBottom(0);
            reminderButton.setMinHeight(0);
            reminderButton.setMinimumHeight(0);
            reminderButton.setPadding(dp(12), 0, dp(12), 0);
            reminderButton.setOnClickListener(v -> confirmKirimPengingat(item));

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(32)
            );
            buttonParams.setMargins(0, dp(8), 0, 0);
            row.addView(reminderButton, buttonParams);
        }

        containerKinerjaGuru.addView(row);
        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        containerKinerjaGuru.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void confirmKirimPengingat(GuruKinerjaItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Kirim pengingat?")
                .setMessage("Pengingat akan dikirim ke " + valueOrDash(item.getNamaGuru()) + ".")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Kirim", (dialog, which) -> kirimPengingat(item))
                .show();
    }

    private void kirimPengingat(GuruKinerjaItem item) {
        if (item.getUserId() <= 0) {
            Toast.makeText(this, "Data penerima guru tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String periode = monthName(bulan) + " " + tahun;
        String isi = "Mohon lengkapi input perkembangan siswa untuk periode " + periode
                + ". Saat ini kinerja input tercatat " + percentText(item.getPersenTepatWaktu())
                + " dengan status " + valueOrDash(item.getStatus()) + ".";

        Map<String, Object> body = new HashMap<>();
        body.put("penerima_id", item.getUserId());
        body.put("subjek", "Pengingat Input Perkembangan");
        body.put("isi", isi);

        apiService.kirimPesan(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DashboardKepsekActivity.this, "Pengingat terkirim", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardKepsekActivity.this, "Gagal mengirim pengingat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(DashboardKepsekActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStatusSiswa(DashboardKepsekData data) {
        if (containerStatusSiswa == null) return;
        containerStatusSiswa.removeAllViews();
        StatusSiswa status = data.getStatusSiswa();
        if (status == null) {
            addText(containerStatusSiswa, "Belum ada data status siswa", "#64748B", 13, false);
            return;
        }
        int total = Math.max(data.getTotalSiswa(), 1);
        addStatusRow("Berkembang baik", status.getBerkembangBaik(), total, "#166534");
        addStatusRow("Cukup berkembang", status.getCukupBerkembang(), total, "#E67E22");
        addStatusRow("Perlu intervensi", status.getPerluIntervensi(), total, "#EF4444");
    }

    private void addStatusRow(String label, int count, int total, String color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 0, 0, dp(12));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        TextView title = createText(label, "#1E293B", 13, false);
        TextView value = createText(count + " siswa", "#64748B", 13, true);
        top.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(value);

        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(Math.round(count * 100f / total));
        bar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(color)));
        bar.setPadding(0, dp(6), 0, 0);

        row.addView(top);
        row.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(10)));
        containerStatusSiswa.addView(row);
    }

    private void bindCapaianKelas(DashboardKepsekData data) {
        if (containerCapaianKelas == null) return;
        containerCapaianKelas.removeAllViews();
        if (data.getCapaianPerKelas() == null || data.getCapaianPerKelas().isEmpty()) {
            addText(containerCapaianKelas, "Belum ada data capaian kelas", "#64748B", 13, false);
            return;
        }
        for (KelasCapaian kelas : data.getCapaianPerKelas()) {
            addKelasRow(kelas);
        }
    }

    private void addKelasRow(KelasCapaian kelas) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView name = createText(kelas.getNamaKelas(), "#1E293B", 14, true);
        int score = kelas.getRataRata() == null ? 0 : (int) Math.round(kelas.getRataRata());
        TextView value = createText(score + "%", score >= 75 ? "#166534" : score >= 60 ? "#E67E22" : "#EF4444", 14, true);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(value);
        containerCapaianKelas.addView(row);

        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        containerCapaianKelas.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void addText(LinearLayout parent, String text, String color, int sizeSp, boolean bold) {
        parent.addView(createText(text, color, sizeSp, bold));
    }

    private TextView createText(String text, String color, int sizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(sizeSp);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private String percent(double value) {
        return Math.round(value) + "%";
    }

    private String percentText(Integer value) {
        return (value == null ? 0 : value) + "%";
    }

    private String statusColor(String status) {
        if ("Baik".equalsIgnoreCase(status)) return "#166534";
        if ("Cukup".equalsIgnoreCase(status)) return "#E67E22";
        return "#EF4444";
    }

    private String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) return "-";
        return DateTimeUtils.formatDate(value);
    }

    private void setKinerjaStatus(String text, boolean visible) {
        if (tvKinerjaStatus == null) return;
        tvKinerjaStatus.setText(text);
        tvKinerjaStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String monthName(int month) {
        String[] names = {
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        return names[Math.max(0, Math.min(month - 1, names.length - 1))];
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_dashboard); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return -1; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_home; }
}
