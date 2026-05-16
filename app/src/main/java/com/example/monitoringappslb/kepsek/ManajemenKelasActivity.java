package com.example.monitoringappslb.kepsek;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManajemenKelasActivity extends BaseKepsekActivity {

    private ApiService apiService;
    private LinearLayout containerKelas;
    private TextView tvKelasStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manajemen_kelas_kepsek);

        apiService = ApiClient.getService();
        containerKelas = findViewById(R.id.container_kelas_kepsek);
        tvKelasStatus = findViewById(R.id.tv_kelas_status);

        setupNavigation();
        loadKelas();
    }

    private void loadKelas() {
        setStatus("Memuat data kelas...", true);
        apiService.getKelas(null).enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setStatus("Gagal memuat data kelas", true);
                    Toast.makeText(ManajemenKelasActivity.this, "Gagal memuat data kelas", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<KelasItem> kelasList = response.body().getData();
                bindKelas(kelasList);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                setStatus("Tidak bisa terhubung ke server", true);
                Toast.makeText(ManajemenKelasActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindKelas(List<KelasItem> kelasList) {
        containerKelas.removeAllViews();
        if (kelasList == null || kelasList.isEmpty()) {
            setStatus("Belum ada data kelas", true);
            return;
        }

        setStatus("", false);
        for (KelasItem kelas : kelasList) {
            addKelasCard(kelas);
        }
    }

    private void addKelasCard(KelasItem kelas) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundResource(R.drawable.bg_rounded_white_border);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvNama = createText(orDash(kelas.getNamaKelas()), "#1E293B", 15, true);
        TextView tvJumlah = createBadge(kelas.getJmlSiswa() + " siswa");
        topRow.addView(tvNama, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(tvJumlah);

        TextView tvMeta = createText(
                orDash(kelas.getTingkatNama()) + " | " + orDash(kelas.getTahunAjaran()),
                "#64748B",
                12,
                false
        );
        tvMeta.setPadding(0, dp(4), 0, dp(10));

        card.addView(topRow);
        card.addView(tvMeta);
        card.addView(createInfoRow("Wali kelas", orDash(kelas.getNamaWaliKelas())));
        card.addView(createInfoRow("Kapasitas", kelas.getKapasitas() > 0
                ? kelas.getJmlSiswa() + "/" + kelas.getKapasitas() + " siswa"
                : kelas.getJmlSiswa() + " siswa"));
        card.addView(createInfoRow("Status", isActive(kelas) ? "Aktif" : "Nonaktif"));

        containerKelas.addView(card, cardParams);
    }

    private LinearLayout createInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(3), 0, dp(3));

        TextView tvLabel = createText(label, "#94A3B8", 12, false);
        TextView tvValue = createText(value, "#334155", 12, true);
        tvValue.setGravity(android.view.Gravity.END);

        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(tvValue, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private TextView createBadge(String text) {
        TextView tv = createText(text, "#166534", 12, true);
        tv.setPadding(dp(10), dp(4), dp(10), dp(4));
        tv.setBackgroundResource(R.drawable.bg_status_active);
        return tv;
    }

    private TextView createText(String text, String color, int sizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(sizeSp);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void setStatus(String text, boolean visible) {
        if (tvKelasStatus == null) return;
        tvKelasStatus.setText(text);
        tvKelasStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean isActive(KelasItem kelas) {
        return kelas.isAktif() == null || kelas.isAktif() == 1;
    }

    private String orDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_manajemen_kelas);
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
        return R.id.nav_manajemen;
    }
}
