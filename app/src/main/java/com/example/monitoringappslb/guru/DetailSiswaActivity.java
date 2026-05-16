package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.AbsensiRekap;
import com.example.monitoringappslb.model.response.ApiModels.AspekCapaian;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetail;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetailResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.example.monitoringappslb.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailSiswaActivity extends BaseGuruActivity {

    private TextView tvNamaProfil, tvKelasNisn, tvTempatLahir, tvTanggalLahir, tvJenisKelamin, tvAlamat, tvNamaAyah, tvNamaIbu, 
            tvStatsKehadiran, tvStatsKognitif, tvStatsSosial, tvStatsMotorik, tvStatsKomunikasi, tvStatsBinaDiri,
            tvSiswaInitials;
    private ApiService apiService;
    private int siswaId = -1;
    private String siswaNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_siswa);

        apiService = ApiClient.getService();
        siswaId = readSiswaId();

        initViews();

        if (siswaId != -1) {
            loadSiswaData(siswaId);
        } else {
            Toast.makeText(this, "ID siswa tidak ditemukan", Toast.LENGTH_SHORT).show();
        }

        setupButtons();
        setupNavigation();
    }

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
        return -1;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }

    private void setupButtons() {
        findViewById(R.id.btn_lihat_ppi).setOnClickListener(v -> {
            if (siswaId != -1 && siswaNama != null) {
                Intent intent = new Intent(this, DetailPpiActivity.class);
                intent.putExtra("SISWA_ID", siswaId);
                intent.putExtra("STUDENT_NAME", siswaNama);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Data siswa belum siap", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tvNamaProfil = findViewById(R.id.tv_nama_profil);
        tvKelasNisn = findViewById(R.id.tv_kelas_nisn);
        tvTempatLahir = findViewById(R.id.tv_tempat_lahir);
        tvTanggalLahir = findViewById(R.id.tv_tanggal_lahir);
        tvJenisKelamin = findViewById(R.id.tv_jenis_kelamin);
        tvAlamat = findViewById(R.id.tv_alamat);
        tvNamaAyah = findViewById(R.id.tv_nama_ayah);
        tvNamaIbu = findViewById(R.id.tv_nama_ibu);
        tvStatsKehadiran = findViewById(R.id.tv_stats_kehadiran);
        tvStatsKognitif = findViewById(R.id.tv_stats_kognitif);
        tvStatsSosial = findViewById(R.id.tv_stats_sosial);
        tvStatsMotorik = findViewById(R.id.tv_stats_motorik);
        tvStatsKomunikasi = findViewById(R.id.tv_stats_komunikasi);
        tvStatsBinaDiri = findViewById(R.id.tv_stats_binadiri);
        tvSiswaInitials = findViewById(R.id.tv_siswa_detail_initials);
    }

    private int readSiswaId() {
        if (getIntent() == null || !getIntent().hasExtra("SISWA_ID")) return -1;

        Object value = getIntent().getExtras().get("SISWA_ID");
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    private void loadSiswaData(int id) {
        apiService.getSiswaById(id).enqueue(new Callback<SiswaDetailResponse>() {
            @Override
            public void onResponse(Call<SiswaDetailResponse> call, Response<SiswaDetailResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess() || response.body().getData() == null) {
                    Toast.makeText(DetailSiswaActivity.this, "Gagal memuat detail siswa", Toast.LENGTH_SHORT).show();
                    return;
                }

                bindSiswaData(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaDetailResponse> call, Throwable t) {
                Toast.makeText(DetailSiswaActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindSiswaData(SiswaDetail siswa) {
        siswaNama = siswa.getNama();

        tvNamaProfil.setText(valueOrDash(siswa.getNama()));
        AvatarUtils.applyInitialAvatar(tvSiswaInitials, siswa.getNama(), siswa.getNisn());
        tvKelasNisn.setText(valueOrDash(siswa.getNamaKelas()) + " | NISN: " + valueOrDash(siswa.getNisn()));
        tvTempatLahir.setText("-");
        tvTanggalLahir.setText(formatDate(siswa.getTglLahir()));
        tvJenisKelamin.setText(formatGender(siswa.getJenisKelamin()));
        tvAlamat.setText(valueOrDash(siswa.getAlamat()));
        tvNamaAyah.setText("-");
        tvNamaIbu.setText(valueOrDash(siswa.getNamaWali()));

        bindKehadiran(siswa);
        bindAspekStats(siswa);
    }

    private void bindKehadiran(SiswaDetail siswa) {
        if (siswa.getAbsensiRekap() == null || siswa.getAbsensiRekap().isEmpty()) {
            tvStatsKehadiran.setText("-");
            return;
        }

        int hadir = 0;
        int total = 0;
        for (AbsensiRekap item : siswa.getAbsensiRekap()) {
            String status = item.getStatus();
            if ("H".equalsIgnoreCase(status) || "Hadir".equalsIgnoreCase(status)) {
                hadir += item.getJumlah();
            }
            total += item.getJumlah();
        }

        tvStatsKehadiran.setText(total > 0 ? ((hadir * 100) / total) + "%" : "-");
    }

    private void bindAspekStats(SiswaDetail siswa) {
        tvStatsKognitif.setText("-");
        tvStatsSosial.setText("-");
        tvStatsMotorik.setText("-");
        tvStatsKomunikasi.setText("-");
        tvStatsBinaDiri.setText("-");

        if (siswa.getAspek() == null) return;

        for (AspekCapaian aspek : siswa.getAspek()) {
            String value = aspek.getRataRata() != null ? aspek.getRataRata().intValue() + "%" : "-";
            String kode = aspek.getKode() != null ? aspek.getKode().toLowerCase() : "";
            String nama = aspek.getNama() != null ? aspek.getNama().toLowerCase() : "";

            if ("kognitif".equals(kode) || nama.contains("kognitif")) {
                tvStatsKognitif.setText(value);
            } else if ("sosial".equals(kode) || nama.contains("sosial")) {
                tvStatsSosial.setText(value);
            } else if ("motorik".equals(kode) || nama.contains("motorik")) {
                tvStatsMotorik.setText(value);
            } else if ("komunikasi".equals(kode) || nama.contains("komunikasi")) {
                tvStatsKomunikasi.setText(value);
            } else if ("bina_diri".equals(kode) || nama.contains("bina")) {
                tvStatsBinaDiri.setText(value);
            }
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String formatGender(String value) {
        if ("L".equalsIgnoreCase(value)) return "Laki-laki";
        if ("P".equalsIgnoreCase(value)) return "Perempuan";
        return valueOrDash(value);
    }

    private String formatDate(String value) {
        return DateTimeUtils.formatDate(value);
    }
}
