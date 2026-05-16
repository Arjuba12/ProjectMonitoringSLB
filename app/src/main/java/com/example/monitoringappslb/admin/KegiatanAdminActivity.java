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
import com.example.monitoringappslb.model.response.ApiModels.KegiatanItem;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KegiatanAdminActivity extends BaseAdminActivity {
    private ApiService apiService;
    private LinearLayout containerKegiatan;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kegiatan_admin);
        apiService = ApiClient.getService();
        containerKegiatan = findViewById(R.id.container_admin_kegiatan);
        tvStatus = findViewById(R.id.tv_admin_kegiatan_status);
        setupNavigation();
        loadKegiatan();
    }

    private void loadKegiatan() {
        Calendar calendar = Calendar.getInstance();
        apiService.getKegiatan(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
                .enqueue(new Callback<KegiatanListResponse>() {
                    @Override
                    public void onResponse(Call<KegiatanListResponse> call, Response<KegiatanListResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            setStatus("Gagal memuat kegiatan", true);
                            return;
                        }
                        renderKegiatan(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<KegiatanListResponse> call, Throwable t) {
                        setStatus("Tidak bisa terhubung ke server", true);
                        Toast.makeText(KegiatanAdminActivity.this, "Gagal memuat kegiatan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderKegiatan(List<KegiatanItem> list) {
        containerKegiatan.removeAllViews();
        if (list == null || list.isEmpty()) {
            setStatus("Belum ada kegiatan bulan ini", true);
            return;
        }

        setStatus("", false);
        for (KegiatanItem item : list) {
            addKegiatanCard(item);
        }
    }

    private void addKegiatanCard(KegiatanItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_rounded_white_border);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));

        addText(card, valueOrDash(item.getJudul()), "#1E293B", 15, true);
        addText(card, DateTimeUtils.formatDate(item.getTanggal()) + " | " + valueOrDash(item.getLokasi()), "#64748B", 12, false);
        addText(card, valueOrDash(item.getDeskripsi()), "#334155", 13, false);
        containerKegiatan.addView(card, params);
    }

    private void addText(LinearLayout parent, String text, String color, int size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, 0, 0, dp(4));
        parent.addView(tv);
    }

    private void setStatus(String text, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(text);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_kegiatan; }
}
