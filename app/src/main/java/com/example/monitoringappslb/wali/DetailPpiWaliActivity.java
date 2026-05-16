package com.example.monitoringappslb.wali;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganItem;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganListResponse;
import com.example.monitoringappslb.model.response.ApiModels.PpiItem;
import com.example.monitoringappslb.model.response.ApiModels.PpiListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPpiWaliActivity extends BaseWaliActivity {

    private TextView tvName, tvTargetAkademik, tvTargetPerilaku, tvTargetSosial, tvTargetMotorik;
    private TableLayout tableRiwayat;
    private ApiService apiService;
    private SessionManager session;
    private int currentSiswaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ppi_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);

        initViews();
        setupWaliMenus();
        setupNavigation();
        loadLatestPpi();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_ppi_name);
        tvTargetAkademik = findViewById(R.id.tv_target_akademik);
        tvTargetPerilaku = findViewById(R.id.tv_target_perilaku);
        tvTargetSosial = findViewById(R.id.tv_target_sosial);
        tvTargetMotorik = findViewById(R.id.tv_target_motorik);
        tableRiwayat = findViewById(R.id.table_riwayat_ppi);
    }

    private void setupWaliMenus() {
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView != null) {
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.drawer_menu_wali);
            if (navView.getHeaderCount() > 0) {
                navView.removeHeaderView(navView.getHeaderView(0));
            }
            navView.inflateHeaderView(R.layout.nav_header_wali);
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.getMenu().clear();
            bottomNav.inflateMenu(R.menu.bottom_nav_menu_wali);
        }
    }

    private void loadLatestPpi() {
        currentSiswaId = session.getSiswaId();
        if (currentSiswaId == -1) {
            showEmptyState("ID siswa tidak ditemukan. Buka dashboard atau login ulang.");
            return;
        }

        apiService.getPpiSiswa(currentSiswaId).enqueue(new Callback<PpiListResponse>() {
            @Override
            public void onResponse(Call<PpiListResponse> call, Response<PpiListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showEmptyState("Gagal memuat PPI");
                    return;
                }

                List<PpiItem> data = response.body().getData();
                if (data == null || data.isEmpty()) {
                    showEmptyState("Belum ada PPI untuk siswa ini");
                    return;
                }

                bindPpi(data.get(0));
            }

            @Override
            public void onFailure(Call<PpiListResponse> call, Throwable t) {
                Toast.makeText(DetailPpiWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showEmptyState("Gagal memuat PPI");
            }
        });
    }

    private void bindPpi(PpiItem ppi) {
        currentSiswaId = ppi.getSiswaId() > 0 ? ppi.getSiswaId() : currentSiswaId;

        tvName.setText("Detail PPI: " + valueOrDash(ppi.getNamaSiswa()));
        tvTargetAkademik.setText(valueOrDash(ppi.getTargetAkademik()));
        tvTargetPerilaku.setText(valueOrDash(ppi.getTargetBinaDiri()));
        tvTargetSosial.setText(valueOrDash(ppi.getTargetKomunikasi()));
        tvTargetMotorik.setText(valueOrDash(ppi.getTargetMotorik()));

        loadRiwayatPerkembangan();
    }

    private void loadRiwayatPerkembangan() {
        if (currentSiswaId <= 0) {
            showRiwayatEmpty("Data siswa tidak ditemukan");
            return;
        }

        apiService.getPerkembanganSiswa(currentSiswaId, null).enqueue(new Callback<PerkembanganListResponse>() {
            @Override
            public void onResponse(Call<PerkembanganListResponse> call, Response<PerkembanganListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showRiwayatEmpty("Gagal memuat progres");
                    return;
                }

                List<PerkembanganItem> data = response.body().getData();
                if (data == null || data.isEmpty()) {
                    showRiwayatEmpty("Belum ada input perkembangan");
                    return;
                }

                populateRiwayat(data);
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {
                showRiwayatEmpty("Tidak bisa memuat progres");
            }
        });
    }

    private void populateRiwayat(List<PerkembanganItem> data) {
        clearRiwayatRows();
        int limit = Math.min(data.size(), 10);
        for (int i = 0; i < limit; i++) {
            PerkembanganItem item = data.get(i);
            addRiwayatRow(
                    formatDate(item.getTanggal()),
                    valueOrDash(item.getAspekNama()),
                    item.getCapaian() + "%"
            );
        }
    }

    private void showRiwayatEmpty(String message) {
        clearRiwayatRows();
        addRiwayatRow(message, "-", "-");
    }

    private void clearRiwayatRows() {
        if (tableRiwayat == null) return;
        while (tableRiwayat.getChildCount() > 1) {
            tableRiwayat.removeViewAt(1);
        }
    }

    private void addRiwayatRow(String tanggal, String aspek, String nilai) {
        if (tableRiwayat == null) return;

        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
        row.setPadding(2, 2, 2, 2);

        TextView tvTanggal = createCell(tanggal, "#333333", Gravity.START);
        tvTanggal.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvTanggal);

        TextView tvAspek = createCell(aspek, "#7F8C8D", Gravity.CENTER);
        row.addView(tvAspek);

        TextView tvNilai = createCell(nilai, "#2E7D32", Gravity.CENTER);
        tvNilai.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvNilai);

        tableRiwayat.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F2F4F7"));
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        tableRiwayat.addView(divider);
    }

    private TextView createCell(String text, String color, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(Color.parseColor(color));
        tv.setGravity(gravity);
        tv.setPadding(14, 14, 14, 14);
        return tv;
    }

    private void showEmptyState(String message) {
        tvName.setText(message);
        tvTargetAkademik.setText("-");
        tvTargetPerilaku.setText("-");
        tvTargetSosial.setText("-");
        tvTargetMotorik.setText("-");
        showRiwayatEmpty("-");
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String formatDate(String value) {
        return DateTimeUtils.formatDate(value);
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
        return findViewById(R.id.bottom_navigation_wali);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_ppi;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_perkembangan;
    }
}
