package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
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

    private TextView tvTotalSiswa, tvTotalGuru;
    private ApiService apiService;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_kepsek);

        apiService = ApiClient.getService();
        session    = new SessionManager(this);

        tvTotalSiswa = findViewById(R.id.tv_total_siswa);
        tvTotalGuru  = findViewById(R.id.tv_total_guru);

        setupNavigation();
        loadDashboard();
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

                if (tvTotalSiswa != null)
                    tvTotalSiswa.setText(String.valueOf(data.getTotalSiswa()));

                if (tvTotalGuru != null)
                    tvTotalGuru.setText(String.valueOf(data.getTotalGuru()));

                // Kehadiran rata-rata
                TextView tvKehadiran = findViewById(R.id.tv_kehadiran_rata);
                if (tvKehadiran != null)
                    tvKehadiran.setText((int) data.getKehadiranRata() + "%");

                // Capaian rata-rata
                TextView tvCapaian = findViewById(R.id.tv_capaian_rata);
                if (tvCapaian != null)
                    tvCapaian.setText((int) data.getCapaianRata() + "%");
            }

            @Override
            public void onFailure(Call<DashboardKepsekResponse> call, Throwable t) {
                Toast.makeText(DashboardKepsekActivity.this,
                    "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_dashboard); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return -1; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_home; }
}
