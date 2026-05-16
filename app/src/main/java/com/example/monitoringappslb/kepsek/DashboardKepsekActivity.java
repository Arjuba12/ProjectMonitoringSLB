package com.example.monitoringappslb.kepsek;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class DashboardKepsekActivity extends BaseKepsekActivity {

    private TextView tvTotalSiswa, tvTotalGuru, tvTotalTerapis, tvKehadiranRata, tvCapaianRata;
    private LinearLayout containerStatusSiswa, containerCapaianKelas;
    private ApiService apiService;

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

        setupNavigation();
        setupActions();
        loadDashboard();
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_dashboard); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return -1; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_home; }
}
