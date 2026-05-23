package com.example.monitoringappslb.admin;

import android.content.Intent;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.LaporanItem;
import com.example.monitoringappslb.model.response.ApiModels.LaporanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaporanAdminActivity extends BaseAdminActivity {
    private ApiService apiService;
    private Button btnRefresh;
    private TextView tvStatus;
    private LinearLayout containerLaporan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_admin);

        apiService = ApiClient.getService();
        btnRefresh = findViewById(R.id.btn_refresh_laporan_admin);
        tvStatus = findViewById(R.id.tv_laporan_admin_status);
        containerLaporan = findViewById(R.id.container_laporan_admin);

        setupNavigation();
        if (btnRefresh != null) btnRefresh.setOnClickListener(v -> loadLaporan());
        loadLaporan();
    }

    private void loadLaporan() {
        showStatus("Memuat laporan guru dan kepsek...", true);
        if (containerLaporan != null) containerLaporan.removeAllViews();

        apiService.getLaporan(null).enqueue(new Callback<LaporanListResponse>() {
            @Override
            public void onResponse(Call<LaporanListResponse> call, Response<LaporanListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showStatus("Gagal memuat laporan", true);
                    return;
                }
                renderLaporan(response.body().getData());
            }

            @Override
            public void onFailure(Call<LaporanListResponse> call, Throwable t) {
                showStatus("Tidak bisa terhubung ke server", true);
            }
        });
    }

    private void renderLaporan(List<LaporanItem> data) {
        if (containerLaporan == null) return;
        containerLaporan.removeAllViews();

        if (data == null || data.isEmpty()) {
            showStatus("Belum ada laporan dari guru atau kepsek", true);
            return;
        }

        int shown = 0;
        for (LaporanItem item : data) {
            String role = item.getRolePembuat();
            if (role != null && !"guru".equalsIgnoreCase(role) && !"kepsek".equalsIgnoreCase(role)) {
                continue;
            }
            containerLaporan.addView(createLaporanCard(item));
            shown++;
        }

        showStatus(shown == 0 ? "Belum ada laporan dari guru atau kepsek" : "", shown == 0);
    }

    private View createLaporanCard(LaporanItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(params);
        card.setBackgroundResource(R.drawable.bg_rounded_white_border);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = text(valueOrDash(item.getJudul()), "#1E293B", 14, true);
        TextView badge = text(roleLabel(item.getRolePembuat()), "#FFFFFF", 11, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        badge.setBackgroundResource(R.drawable.bg_pill_primary);

        top.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(badge);
        card.addView(top);

        String meta = valueOrDash(item.getTipe()) + " | " + valueOrDash(item.getPeriode())
                + " | " + valueOrDash(item.getNamaKelas());
        card.addView(text(meta, "#64748B", 12, false));
        card.addView(text("Dibuat oleh: " + valueOrDash(item.getNamaPembuat())
                + " | " + DateTimeUtils.formatDateTime(item.getCreatedAt()), "#64748B", 12, false));
        card.addView(text("Siswa: " + numberOrDash(item.getTotalSiswa())
                + " | Kelas: " + numberOrDash(item.getTotalKelas())
                + " | Status: " + valueOrDash(item.getStatus()), "#334155", 12, false));

        TextView action = text(hasFile(item) ? "Buka PDF" : "File PDF belum tersedia",
                hasFile(item) ? "#2563EB" : "#94A3B8", 13, true);
        action.setGravity(Gravity.CENTER);
        action.setPadding(0, dp(10), 0, dp(4));
        card.addView(action);

        if (hasFile(item)) {
            View.OnClickListener openListener = v -> openPdf(item.getFilePath());
            action.setOnClickListener(openListener);
            card.setOnClickListener(openListener);
        }

        TextView delete = text("Hapus laporan", "#EF4444", 13, true);
        delete.setGravity(Gravity.CENTER);
        delete.setPadding(0, dp(8), 0, dp(2));
        delete.setOnClickListener(v -> confirmDelete(item));
        card.addView(delete);
        return card;
    }

    private void confirmDelete(LaporanItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus laporan?")
                .setMessage(valueOrDash(item.getJudul()) + " akan dihapus dari daftar laporan.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus", (dialog, which) -> deleteLaporan(item.getId()))
                .show();
    }

    private void deleteLaporan(int id) {
        apiService.deleteLaporan(id).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(LaporanAdminActivity.this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadLaporan();
                } else {
                    Toast.makeText(LaporanAdminActivity.this, "Gagal menghapus laporan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(LaporanAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasFile(LaporanItem item) {
        return item.getFilePath() != null && !item.getFilePath().trim().isEmpty();
    }

    private void openPdf(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.trim())));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak bisa membuka file laporan", Toast.LENGTH_SHORT).show();
        }
    }

    private void showStatus(String message, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(message);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private TextView text(String value, String color, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(sp);
        tv.setPadding(0, dp(3), 0, dp(3));
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private String numberOrDash(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String roleLabel(String role) {
        if ("guru".equalsIgnoreCase(role)) return "Guru";
        if ("kepsek".equalsIgnoreCase(role)) return "Kepsek";
        return "Pembuat";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_admin_laporan); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_home; }
}
