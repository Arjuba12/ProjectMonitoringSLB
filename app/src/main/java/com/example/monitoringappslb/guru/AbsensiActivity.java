package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.adapter.AbsensiAdapter;
import com.example.monitoringappslb.model.Siswa;
import com.example.monitoringappslb.model.response.ApiModels.*;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AbsensiActivity extends BaseGuruActivity {

    private RecyclerView recyclerView;
    private AbsensiAdapter adapter;
    private List<Siswa> siswaList = new ArrayList<>();
    private TextView tvSummaryHadir, tvSummaryIzin, tvSummaryAlpa;
    private android.app.ProgressDialog progressDialog;
    private ApiService apiService;
    private int kelasId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absensi);

        setupNavigation();
        apiService = ApiClient.getService();

        recyclerView   = findViewById(R.id.rv_absensi_siswa);
        tvSummaryHadir = findViewById(R.id.tv_summary_hadir);
        tvSummaryIzin  = findViewById(R.id.tv_summary_izin);
        tvSummaryAlpa  = findViewById(R.id.tv_summary_alpa);
        Button btnSimpan = findViewById(R.id.btn_simpan_absensi);

        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Menyimpan absensi...");
        progressDialog.setCancelable(false);

        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> simpanAbsensi());
        }

        // Load kelas guru → lalu load siswa
        loadKelas();
    }

    private void loadKelas() {
        apiService.getKelasSaya().enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null
                        || response.body().getData().isEmpty()) {
                    Toast.makeText(AbsensiActivity.this,
                        "Tidak ada kelas yang ditugaskan", Toast.LENGTH_SHORT).show();
                    return;
                }
                kelasId = response.body().getData().get(0).getId();
                checkExistingAbsensi(kelasId);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                Toast.makeText(AbsensiActivity.this,
                    "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                t.printStackTrace(); Log.e( "ABSENSI_ERROR", Log.getStackTraceString(t) );
                Toast.makeText( AbsensiActivity.this, t.toString(), Toast.LENGTH_LONG ).show();
            }
        });
    }

    private void checkExistingAbsensi(int kelasId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        apiService.getAbsensiKelas(kelasId, today).enqueue(new Callback<AbsensiListResponse>() {
            @Override
            public void onResponse(Call<AbsensiListResponse> call, Response<AbsensiListResponse> response) {
                // Jika sukses dan ada data, berarti sudah pernah absen hari ini
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    loadSiswaWithStatus(kelasId, response.body().getData());
                } else {
                    // Jika belum ada data absensi hari ini, load siswa normal (default hadir)
                    loadSiswa(kelasId);
                }
            }

            @Override
            public void onFailure(Call<AbsensiListResponse> call, Throwable t) {
                // Jika gagal koneksi, tetap coba load daftar siswa agar aplikasi tidak stuck
                loadSiswa(kelasId);
            }
        });
    }

    private void loadSiswaWithStatus(int kelasId, List<AbsensiRekapBulanan> existingAbsensi) {
        apiService.getSiswa(kelasId, 1, null).enqueue(new Callback<SiswaListResponse>() {
            @Override
            public void onResponse(Call<SiswaListResponse> call, Response<SiswaListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                siswaList.clear();
                for (SiswaItem s : response.body().getData()) {
                    Siswa siswa = new Siswa(
                            String.valueOf(s.getId()),
                            s.getNama(),
                            s.getNisn() != null ? s.getNisn() : ""
                    );
                    
                    // Cari status absensi siswa ini di data existing
                    String status = "H"; // Default
                    for (AbsensiRekapBulanan exist : existingAbsensi) {
                        if (exist.getId() == s.getId()) {
                            if ("Sakit".equalsIgnoreCase(exist.getStatus())) status = "S";
                            else if ("Izin".equalsIgnoreCase(exist.getStatus())) status = "I";
                            else if ("Alpha".equalsIgnoreCase(exist.getStatus()) || "Alpa".equalsIgnoreCase(exist.getStatus())) status = "A";
                            else status = "H";
                            break;
                        }
                    }
                    
                    siswa.setStatusAbsensi(status);
                    siswaList.add(siswa);
                }

                initRecyclerView();
                Toast.makeText(AbsensiActivity.this, "Menampilkan absensi yang sudah tersimpan", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<SiswaListResponse> call, Throwable t) {
                Toast.makeText(AbsensiActivity.this, "Gagal sinkronisasi data siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSiswa(int kelasId) {
        apiService.getSiswa(kelasId, 1, null).enqueue(new Callback<SiswaListResponse>() {
            @Override
            public void onResponse(Call<SiswaListResponse> call, Response<SiswaListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                siswaList.clear();
                for (SiswaItem s : response.body().getData()) {
                    Siswa siswa = new Siswa(
                        String.valueOf(s.getId()),
                        s.getNama(),
                        s.getNisn() != null ? s.getNisn() : ""
                    );
                    siswa.setStatusAbsensi("H"); // default Hadir
                    siswaList.add(siswa);
                }

                initRecyclerView();
            }

            @Override
            public void onFailure(Call<SiswaListResponse> call, Throwable t) {
                Toast.makeText(AbsensiActivity.this,
                    "Gagal memuat daftar siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() {
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(AbsensiActivity.this));
            adapter = new AbsensiAdapter(siswaList, AbsensiActivity.this::updateSummary);
            recyclerView.setAdapter(adapter);
        }
        updateSummary();
    }

    private void simpanAbsensi() {
        if (kelasId == -1 || siswaList.isEmpty()) {
            Toast.makeText(this, "Data belum siap, tunggu sebentar", Toast.LENGTH_SHORT).show();
            return;
        }

        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        List<Map<String, Object>> absensiList = new ArrayList<>();
        for (Siswa s : siswaList) {
            Map<String, Object> item = new HashMap<>();
            item.put("siswa_id", Integer.parseInt(s.getId()));
            String status;
            switch (s.getStatusAbsensi()) {
                case "S":  status = "Sakit"; break;
                case "I":  status = "Izin";  break;
                case "A":  status = "Alpha"; break;
                default:   status = "Hadir"; break;
            }
            item.put("status", status);
            absensiList.add(item);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("kelas_id", kelasId);
        body.put("tanggal", tanggal);
        body.put("absensi_list", absensiList);

        progressDialog.show();
        apiService.inputAbsensi(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AbsensiActivity.this,
                        "✅ Absensi hari ini berhasil disimpan!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Gagal menyimpan absensi";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.code();
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(AbsensiActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AbsensiActivity.this,
                    "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary() {
        int hadir = 0, izinSakit = 0, alpa = 0;
        for (Siswa s : siswaList) {
            switch (s.getStatusAbsensi()) {
                case "H": hadir++; break;
                case "S": case "I": izinSakit++; break;
                case "A": alpa++; break;
            }
        }
        if (tvSummaryHadir != null) tvSummaryHadir.setText(String.valueOf(hadir));
        if (tvSummaryIzin  != null) tvSummaryIzin.setText(String.valueOf(izinSakit));
        if (tvSummaryAlpa  != null) tvSummaryAlpa.setText(String.valueOf(alpa));
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return R.id.nav_absensi; }
    @Override protected int getSelfBottomNavItemId() { return -1; }
}
