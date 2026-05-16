package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.LaporanItem;
import com.example.monitoringappslb.model.response.ApiModels.LaporanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganItem;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganListResponse;
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

public class LaporanWaliActivity extends BaseWaliActivity {
    private ApiService apiService;
    private SessionManager session;
    private TableLayout tableLaporan;
    private LinearLayout containerLaporanPeriodik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);
        tableLaporan = findViewById(R.id.table_laporan_guru);
        containerLaporanPeriodik = findViewById(R.id.container_laporan_periodik);

        setupNavigation();
        setupActions();
        loadCatatanGuru();
        loadLaporanPeriodik();
    }

    private void setupActions() {
        View btnRefresh = findViewById(R.id.btn_refresh_laporan_wali);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                loadCatatanGuru();
                loadLaporanPeriodik();
            });
        }
    }

    private void loadLaporanPeriodik() {
        showPeriodikMessage("Memuat laporan periodik...");
        apiService.getLaporan("Kelas").enqueue(new Callback<LaporanListResponse>() {
            @Override
            public void onResponse(Call<LaporanListResponse> call, Response<LaporanListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showPeriodikMessage("Gagal memuat laporan periodik");
                    return;
                }

                List<LaporanItem> data = response.body().getData();
                if (data == null || data.isEmpty()) {
                    showPeriodikMessage("Belum ada laporan periodik tersimpan");
                    return;
                }

                renderLaporanPeriodik(data);
            }

            @Override
            public void onFailure(Call<LaporanListResponse> call, Throwable t) {
                showPeriodikMessage("Tidak bisa memuat laporan periodik");
            }
        });
    }

    private void renderLaporanPeriodik(List<LaporanItem> data) {
        if (containerLaporanPeriodik == null) return;
        containerLaporanPeriodik.removeAllViews();
        for (LaporanItem item : data) {
            if (item.getFilePath() == null || item.getFilePath().trim().isEmpty()) continue;
            containerLaporanPeriodik.addView(createLaporanCard(item));
        }
        if (containerLaporanPeriodik.getChildCount() == 0) {
            showPeriodikMessage("Belum ada laporan periodik tersimpan");
        }
    }

    private View createLaporanCard(LaporanItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(params);
        card.setBackgroundResource(R.drawable.bg_rounded_white_border);

        TextView title = createPlainText(valueOrDash(item.getJudul()), "#1E293B", 14, true);
        TextView meta = createPlainText(
                valueOrDash(item.getPeriode()) + " | " + valueOrDash(item.getNamaKelas()) +
                        " | Oleh: " + valueOrDash(item.getNamaPembuat()),
                "#64748B",
                12,
                false
        );
        meta.setPadding(0, dpToPx(4), 0, dpToPx(10));

        TextView action = createPlainText("Unduh / Lihat PDF", "#2563EB", 13, true);
        action.setGravity(Gravity.CENTER);
        action.setPadding(0, dpToPx(8), 0, dpToPx(8));
        action.setBackgroundResource(R.drawable.bg_rounded_white_border);

        View.OnClickListener openListener = v -> openPdf(item.getFilePath());
        card.setOnClickListener(openListener);
        action.setOnClickListener(openListener);

        card.addView(title);
        card.addView(meta);
        card.addView(action);
        return card;
    }

    private void showPeriodikMessage(String message) {
        if (containerLaporanPeriodik == null) return;
        containerLaporanPeriodik.removeAllViews();
        TextView tv = createPlainText(message, "#94A3B8", 13, false);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dpToPx(8), 0, dpToPx(8));
        containerLaporanPeriodik.addView(tv);
    }

    private void openPdf(String url) {
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "File laporan tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim()));
        startActivity(intent);
    }

    private void loadCatatanGuru() {
        int siswaId = session.getSiswaId();
        if (siswaId == -1) {
            showTableMessage("ID siswa tidak ditemukan. Buka dashboard atau login ulang.");
            return;
        }

        apiService.getPerkembanganSiswa(siswaId, null).enqueue(new Callback<PerkembanganListResponse>() {
            @Override
            public void onResponse(Call<PerkembanganListResponse> call, Response<PerkembanganListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showTableMessage("Gagal memuat catatan guru");
                    return;
                }

                List<PerkembanganItem> data = response.body().getData();
                if (data == null || data.isEmpty()) {
                    showTableMessage("Belum ada catatan guru");
                    return;
                }

                populateTable(data);
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {
                Toast.makeText(LaporanWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showTableMessage("Tidak bisa memuat catatan guru");
            }
        });
    }

    private void populateTable(List<PerkembanganItem> data) {
        clearRows();
        int limit = Math.min(data.size(), 20);
        for (int i = 0; i < limit; i++) {
            PerkembanganItem item = data.get(i);
            if (item.getCatatan() == null || item.getCatatan().trim().isEmpty()) continue;
            addRow(
                    formatDate(item.getTanggal()),
                    valueOrDash(item.getNamaGuru()),
                    valueOrDash(item.getAspekNama()),
                    valueOrDash(item.getCatatan())
            );
        }

        if (tableLaporan != null && tableLaporan.getChildCount() <= 1) {
            showTableMessage("Belum ada catatan guru");
        }
    }

    private void showTableMessage(String message) {
        clearRows();
        addRow(message, "-", "-", "-");
    }

    private void clearRows() {
        if (tableLaporan == null) return;
        while (tableLaporan.getChildCount() > 1) {
            tableLaporan.removeViewAt(1);
        }
    }

    private void addRow(String tanggal, String guru, String aspek, String catatan) {
        if (tableLaporan == null) return;

        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);

        row.addView(createCell(tanggal, "#1E293B", 90));
        row.addView(createCell(guru, "#64748B", 110));
        row.addView(createCell(aspek, "#FFFFFF", 95, true));
        row.addView(createCell(catatan, "#64748B", 220));

        tableLaporan.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        tableLaporan.addView(divider);
    }

    private TextView createCell(String text, String color, int widthDp) {
        return createCell(text, color, widthDp, false);
    }

    private TextView createCell(String text, String color, int widthDp, boolean badge) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(badge ? 10 : 12);
        tv.setTextColor(Color.parseColor(color));
        tv.setPadding(10, 6, 10, 6);
        tv.setMaxWidth(dpToPx(widthDp));
        tv.setMinWidth(dpToPx(widthDp));
        if (badge) {
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.bg_badge_aspek);
            tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3498DB")));
        }
        return tv;
    }

    private TextView createPlainText(String text, String color, int sizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(Color.parseColor(color));
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String valueOrDash(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }

    private String formatDate(String value) {
        return DateTimeUtils.formatDate(value);
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_laporan);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view_wali);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation_wali);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_laporan;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_perkembangan;
    }
}
