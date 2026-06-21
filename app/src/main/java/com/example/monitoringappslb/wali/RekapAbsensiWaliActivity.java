package com.example.monitoringappslb.wali;

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

import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RekapAbsensiWaliActivity extends BaseWaliActivity {

    private ApiService apiService;
    private SessionManager session;
    private TextView tvHadir, tvSakit, tvIzin, tvAlpha, tvPeriode, tvRingkasan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap_absensi_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);

        initViews();
        setupNavigation();
        loadRekap();
    }

    private void initViews() {
        tvHadir = findViewById(R.id.tv_count_hadir);
        tvSakit = findViewById(R.id.tv_count_sakit);
        tvIzin  = findViewById(R.id.tv_count_izin);
        tvAlpha = findViewById(R.id.tv_count_alpha);
        tvPeriode = findViewById(R.id.tv_absensi_periode);
        tvRingkasan = findViewById(R.id.tv_absensi_ringkasan);
    }

    private void loadRekap() {
        Calendar cal = Calendar.getInstance();
        int bulan = cal.get(Calendar.MONTH) + 1;
        int tahun = cal.get(Calendar.YEAR);
        if (tvPeriode != null) {
            tvPeriode.setText("Rekap Presensi " + getMonthName(bulan) + " " + tahun);
        }
        
        int siswaId = session.getSiswaId();
        
        if (siswaId == -1) {
            setCounts(0, 0, 0, 0);
            setSummary("ID siswa tidak ditemukan. Buka dashboard atau login ulang.");
            Toast.makeText(this, "ID Siswa tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getRekapAbsensiSiswa(siswaId, bulan, tahun).enqueue(new Callback<AbsensiSiswaRekapResponse>() {
            @Override
            public void onResponse(Call<AbsensiSiswaRekapResponse> call, Response<AbsensiSiswaRekapResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AbsensiSiswaRekapData data = response.body().getData();
                    if (data != null && data.getRekap() != null) {
                        AbsensiRekapBulanan rekap = data.getRekap();
                        bindRekap(rekap);
                    } else {
                        setCounts(0, 0, 0, 0);
                        setSummary("Belum ada presensi pada bulan ini.");
                    }
                } else {
                    setCounts(0, 0, 0, 0);
                    setSummary("Gagal memuat rekap presensi.");
                }
            }

            @Override
            public void onFailure(Call<AbsensiSiswaRekapResponse> call, Throwable t) {
                Toast.makeText(RekapAbsensiWaliActivity.this, "Gagal memuat rekap presensi", Toast.LENGTH_SHORT).show();
                setSummary("Tidak bisa terhubung ke server.");
            }
        });
    }

    private void bindRekap(AbsensiRekapBulanan rekap) {
        setCounts(rekap.getHadir(), rekap.getSakit(), rekap.getIzin(), rekap.getAlpha());

        int total = rekap.getTotalHari();
        if (total <= 0) {
            total = rekap.getHadir() + rekap.getSakit() + rekap.getIzin() + rekap.getAlpha();
        }
        int persenHadir = total > 0 ? (rekap.getHadir() * 100) / total : 0;

        if (total > 0) {
            setSummary(rekap.getHadir() + " dari " + total + " hari hadir (" + persenHadir + "%).");
        } else {
            setSummary("Belum ada presensi pada bulan ini.");
        }
    }

    private void setCounts(int hadir, int sakit, int izin, int alpha) {
        if (tvHadir != null) tvHadir.setText(String.valueOf(hadir));
        if (tvSakit != null) tvSakit.setText(String.valueOf(sakit));
        if (tvIzin != null) tvIzin.setText(String.valueOf(izin));
        if (tvAlpha != null) tvAlpha.setText(String.valueOf(alpha));
    }

    private void setSummary(String text) {
        if (tvRingkasan != null) tvRingkasan.setText(text);
    }

    private String getMonthName(int month) {
        String[] months = {
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        if (month < 1 || month > 12) return "-";
        return months[month - 1];
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_wali); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view_wali); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation_wali); }
    @Override protected int getSelfNavDrawerItemId() { return R.id.nav_wali_rekap_absensi; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_wali_home; }
}
