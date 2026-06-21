package com.example.monitoringappslb.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.AspekListResponse;
import com.example.monitoringappslb.model.response.ApiModels.DashboardKepsekResponse;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAdminActivity extends BaseAdminActivity {
    private ApiService apiService;
    private LinearLayout containerActivityLog, containerSystemStatus;
    private boolean dashboardOk, kelasOk, aspekOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);
        apiService = ApiClient.getService();
        containerActivityLog = findViewById(R.id.container_activity_log);
        containerSystemStatus = findViewById(R.id.container_system_status);
        setupNavigation();
        setupStats();
        setupInitialMonitoring();
        loadStats();
    }

    private void setupStats() {
        setupStat(findViewById(R.id.stat_siswa), "Siswa", "--", "Master data siswa");
        setupStat(findViewById(R.id.stat_guru), "Guru/Wali", "--", "Akun pengguna");
        setupStat(findViewById(R.id.stat_kelas), "Kelas", "--", "Kelas aktif");
        setupStat(findViewById(R.id.stat_aspek), "Aspek Program Pembelajaran Individual", "--", "Aspek perkembangan");
    }

    private void setupInitialMonitoring() {
        if (containerActivityLog != null) {
            containerActivityLog.removeAllViews();
            addLog("Dashboard admin dibuka", "Menyiapkan data monitoring dan master data.", "#64748B");
            addLog("Sinkronisasi dimulai", "Mengambil data siswa, guru, kelas, dan aspek Program Pembelajaran Individual.", "#64748B");
        }
        updateSystemStatus();
    }

    private void loadStats() {
        apiService.getDashboardKepsek().enqueue(new Callback<DashboardKepsekResponse>() {
            @Override
            public void onResponse(Call<DashboardKepsekResponse> call, Response<DashboardKepsekResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    dashboardOk = false;
                    addLog("Dashboard gagal dimuat", "Endpoint ringkasan sekolah belum mengembalikan data.", "#EF4444");
                    updateSystemStatus();
                    return;
                }
                dashboardOk = true;
                setupStat(findViewById(R.id.stat_siswa), "Siswa", String.valueOf(response.body().getData().getTotalSiswa()), "Siswa aktif");
                setupStat(findViewById(R.id.stat_guru), "Guru/Wali", String.valueOf(response.body().getData().getTotalGuru()), "Tenaga pendidik");
                addLog("Ringkasan sekolah diperbarui", "Data siswa dan guru berhasil dimuat.", "#166534");
                updateSystemStatus();
            }

            @Override
            public void onFailure(Call<DashboardKepsekResponse> call, Throwable t) {
                dashboardOk = false;
                addLog("Koneksi dashboard gagal", "Periksa backend atau alamat API.", "#EF4444");
                updateSystemStatus();
                Toast.makeText(DashboardAdminActivity.this, "Dashboard belum bisa dimuat", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getKelas(null).enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                int count = response.body() != null && response.body().getData() != null ? response.body().getData().size() : 0;
                kelasOk = response.isSuccessful() && response.body() != null;
                setupStat(findViewById(R.id.stat_kelas), "Kelas", String.valueOf(count), "Kelas terdaftar");
                addLog("Master kelas tersinkron", count + " kelas terbaca dari server.", kelasOk ? "#166534" : "#EF4444");
                updateSystemStatus();
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                kelasOk = false;
                addLog("Master kelas gagal dimuat", "Data kelas belum bisa diambil.", "#EF4444");
                updateSystemStatus();
            }
        });

        apiService.getAspek().enqueue(new Callback<AspekListResponse>() {
            @Override
            public void onResponse(Call<AspekListResponse> call, Response<AspekListResponse> response) {
                int count = response.body() != null && response.body().getData() != null ? response.body().getData().size() : 0;
                aspekOk = response.isSuccessful() && response.body() != null;
                setupStat(findViewById(R.id.stat_aspek), "Aspek Program Pembelajaran Individual", String.valueOf(count), "Aspek perkembangan");
                addLog("Aspek Program Pembelajaran Individual tersinkron", count + " aspek perkembangan aktif.", aspekOk ? "#166534" : "#EF4444");
                updateSystemStatus();
            }

            @Override
            public void onFailure(Call<AspekListResponse> call, Throwable t) {
                aspekOk = false;
                addLog("Aspek Program Pembelajaran Individual gagal dimuat", "Data aspek belum bisa diambil.", "#EF4444");
                updateSystemStatus();
            }
        });
    }

    private void setupStat(View card, String label, String value, String caption) {
        if (card == null) return;
        TextView tvLabel = card.findViewById(R.id.tv_stat_label);
        TextView tvValue = card.findViewById(R.id.tv_stat_value);
        TextView tvCaption = card.findViewById(R.id.tv_stat_caption);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
        if (tvCaption != null) tvCaption.setText(caption);
    }

    private void updateSystemStatus() {
        if (containerSystemStatus == null) return;
        containerSystemStatus.removeAllViews();
        addStatus("API Dashboard", dashboardOk ? "Terhubung" : "Menunggu", dashboardOk ? "#166534" : "#E67E22");
        addStatus("Master Kelas", kelasOk ? "Terhubung" : "Menunggu", kelasOk ? "#166534" : "#E67E22");
        addStatus("Aspek Program Pembelajaran Individual", aspekOk ? "Terhubung" : "Menunggu", aspekOk ? "#166534" : "#E67E22");
        addStatus("Mode Admin", "Monitoring + master data", "#1E293B");
    }

    private void addLog(String title, String subtitle, String color) {
        if (containerActivityLog == null) return;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView dot = createText("•", color, 20, true);
        dot.setGravity(android.view.Gravity.TOP);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        addText(content, title, "#1E293B", 13, true);
        addText(content, subtitle, "#64748B", 12, false);

        row.addView(dot, new LinearLayout.LayoutParams(dp(18), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(content, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        containerActivityLog.addView(row, 0);
    }

    private void addStatus(String label, String value, String color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, dp(7));

        TextView tvLabel = createText(label, "#334155", 13, false);
        TextView tvValue = createText(value, color, 13, true);
        tvValue.setGravity(android.view.Gravity.END);
        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvValue, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        containerSystemStatus.addView(row);
    }

    private void addText(LinearLayout parent, String text, String color, int size, boolean bold) {
        parent.addView(createText(text, color, size, bold));
    }

    private TextView createText(String text, String color, int size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_home; }
}
