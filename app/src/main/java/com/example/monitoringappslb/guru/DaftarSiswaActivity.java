
package com.example.monitoringappslb.guru;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.adapter.DaftarSiswaAdapter;
import com.example.monitoringappslb.model.Siswa;
import com.example.monitoringappslb.model.response.ApiModels.SiswaItem;
import com.example.monitoringappslb.model.response.ApiModels.SiswaListResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaRekapItem;
import com.example.monitoringappslb.model.response.ApiModels.SiswaRekapResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarSiswaActivity extends BaseGuruActivity {

    private RecyclerView recyclerView;
    private DaftarSiswaAdapter adapter;
    private final List<Siswa> siswaList = new ArrayList<>();

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_siswa);

        setupNavigation();

        apiService = ApiClient.getService();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.rv_daftar_siswa);

        if (recyclerView != null) {

            recyclerView.setLayoutManager(
                    new LinearLayoutManager(this)
            );

            adapter = new DaftarSiswaAdapter(
                    siswaList,
                    this
            );

            recyclerView.setAdapter(adapter);
        }

        Toast.makeText(
                this,
                "Memuat daftar siswa...",
                Toast.LENGTH_SHORT
        ).show();

        loadSiswa();
    }

    /**
     * Load data siswa dari API
     */
    private void loadSiswa() {

        // is_aktif = 1 karena database tinyint(1)
        apiService.getSiswa(
                null,
                1,
                null
        ).enqueue(new Callback<SiswaListResponse>() {

            @Override
            public void onResponse(
                    Call<SiswaListResponse> call,
                    Response<SiswaListResponse> response
            ) {

                // Debug response
                android.util.Log.d(
                        "RAW_JSON",
                        new Gson().toJson(response.body())
                );

                if (!response.isSuccessful()
                        || response.body() == null
                        || !response.body().isSuccess()) {

                    Toast.makeText(
                            DaftarSiswaActivity.this,
                            "Gagal memuat data siswa",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                List<SiswaItem> apiList =
                        response.body().getData();

                siswaList.clear();

                if (apiList != null) {

                    for (SiswaItem s : apiList) {

                        Siswa siswa = new Siswa(
                                String.valueOf(s.getId()),
                                s.getNama(),
                                s.getNisn() != null
                                        ? s.getNisn()
                                        : "-"
                        );

                        // Dummy sementara untuk tampilan
                        siswa.setStatusAbsensi("H");

                        siswaList.add(siswa);
                    }
                }

                // Refresh RecyclerView
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                loadRekapSiswa();

                Toast.makeText(
                        DaftarSiswaActivity.this,
                        siswaList.size() + " siswa ditemukan",
                        Toast.LENGTH_LONG
                ).show();
            }
            @Override
            public void onFailure(
                    Call<SiswaListResponse> call,
                    Throwable t
            ) {

                t.printStackTrace();

                android.util.Log.e(
                        "FULL_ERROR",
                        android.util.Log.getStackTraceString(t)
                );

                Toast.makeText(
                        DaftarSiswaActivity.this,
                        t.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }

        });
    }

    /**
     * Load rekap PPI dan kehadiran bulan berjalan
     */
    private void loadRekapSiswa() {
        Calendar calendar = Calendar.getInstance();
        int bulan = calendar.get(Calendar.MONTH) + 1;
        int tahun = calendar.get(Calendar.YEAR);

        apiService.getSiswaRekap(null, bulan, tahun).enqueue(new Callback<SiswaRekapResponse>() {
            @Override
            public void onResponse(Call<SiswaRekapResponse> call, Response<SiswaRekapResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    renderEmptyRekap("Gagal memuat rekap");
                    return;
                }

                setupTableRekap(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaRekapResponse> call, Throwable t) {
                renderEmptyRekap("Tidak bisa memuat rekap");
            }
        });
    }

    /**
     * Setup tabel rekap sesuai data API
     */
    private void setupTableRekap(List<SiswaRekapItem> rekapList) {

        TableLayout tableLayout =
                findViewById(R.id.table_rekap_ppi);

        if (tableLayout == null) return;

        // Hapus semua row kecuali header
        int childCount = tableLayout.getChildCount();

        if (childCount > 1) {
            tableLayout.removeViews(
                    1,
                    childCount - 1
            );
        }

        if (rekapList == null || rekapList.isEmpty()) {
            renderEmptyRekap("Belum ada data rekap");
            return;
        }

        for (SiswaRekapItem siswa : rekapList) {

            TableRow row = new TableRow(this);

            row.setBackgroundColor(Color.WHITE);

            // Nama Siswa
            row.addView(createCell(
                    siswa.getNama(),
                    false
            ));

            // Hadir
            row.addView(createCell(
                    formatPercent(siswa.getHadirPersen()),
                    true
            ));

            // Kognitif
            row.addView(createCell(
                    formatStatus(siswa.getKognitifStatus(), siswa.getKognitif()),
                    true
            ));

            // Motorik
            row.addView(createCell(
                    formatStatus(siswa.getMotorikStatus(), siswa.getMotorik()),
                    true
            ));

            // Komunikasi
            row.addView(createCell(
                    formatStatus(siswa.getKomunikasiStatus(), siswa.getKomunikasi()),
                    true
            ));

            // Bina Diri
            row.addView(createCell(
                    formatStatus(siswa.getBinaDiriStatus(), siswa.getBinaDiri()),
                    true
            ));

            tableLayout.addView(row);
        }
    }

    private void renderEmptyRekap(String message) {
        TableLayout tableLayout = findViewById(R.id.table_rekap_ppi);
        if (tableLayout == null) return;

        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1);
        }

        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
        row.addView(createCell(message, false));
        tableLayout.addView(row);
    }

    private String formatPercent(Double value) {
        if (value == null) return "-";
        return Math.round(value) + "%";
    }

    private String formatStatus(String status, Double value) {
        if (status == null || status.trim().isEmpty() || "-".equals(status.trim())) {
            return "-";
        }
        if (value == null) {
            return status;
        }
        return status + " (" + Math.round(value) + "%)";
    }

    /**
     * Membuat cell tabel
     */
    private TextView createCell(
            String text,
            boolean center
    ) {

        TextView tv = new TextView(this);

        tv.setPadding(
                dpToPx(14),
                dpToPx(14),
                dpToPx(14),
                dpToPx(14)
        );

        tv.setText(text);

        tv.setTextSize(12);

        tv.setTextColor(
                Color.parseColor("#333333")
        );

        if (center) {
            tv.setGravity(Gravity.CENTER);
        }

        return tv;
    }

    /**
     * Convert dp ke px
     */
    private int dpToPx(int dp) {

        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
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
        return R.id.nav_siswa;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }
}
