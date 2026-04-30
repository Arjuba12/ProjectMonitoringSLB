package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Siswa;
import java.util.List;

public class DashboardGuruActivity extends BaseGuruActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_guru);

        DrawerLayout dl = findViewById(R.id.drawer_layout);

        // Inisialisasi Bottom Navigation
        setupBottomNav(R.id.nav_home);

        // Setup Hamburger Menu & Side Navigation
        setupNavigationDrawer(dl);

        // Update Stats from Database
        updateDashboardStats();

        // Setup Klik Button Detail Siswa
        setupClickListeners();
    }

    private void updateDashboardStats() {
        List<Siswa> listSiswa = DummyDatabase.getSiswaList();
        
        // Update Total Siswa
        TextView tvTotalSiswa = findViewById(R.id.tv_total_siswa);
        if (tvTotalSiswa != null) {
            tvTotalSiswa.setText(String.valueOf(listSiswa.size()));
        }

        // Dynamic "Siswa Perlu Perhatian" (Misal yang kognitif/motorik < 60)
        LinearLayout container = findViewById(R.id.container_perlu_perhatian);
        if (container != null) {
            container.removeAllViews();
            int count = 0;
            for (Siswa s : listSiswa) {
                int kognitif = Integer.parseInt(s.getStatsKognitif());
                int motorik = Integer.parseInt(s.getStatsMotorik());
                
                if (kognitif < 65 || motorik < 65) {
                    addPerhatianRow(container, s);
                    count++;
                }
                if (count >= 3) break; // Limit 3 di dashboard
            }
        }
    }

    private void addPerhatianRow(LinearLayout container, Siswa s) {
        RelativeLayout row = new RelativeLayout(this);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        // Info Layout
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout.LayoutParams infoParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        infoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        
        TextView tvNama = new TextView(this);
        tvNama.setText(s.getNama());
        tvNama.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNama.setTextColor(android.graphics.Color.parseColor("#2C3E50"));
        tvNama.setTextSize(13);
        
        TextView tvDesc = new TextView(this);
        tvDesc.setText("Kognitif: " + s.getStatsKognitif() + ", Motorik: " + s.getStatsMotorik());
        tvDesc.setTextColor(android.graphics.Color.parseColor("#E74C3C"));
        tvDesc.setTextSize(10);

        info.addView(tvNama);
        info.addView(tvDesc);
        row.addView(info, infoParams);

        // Button
        Button btn = new Button(this, null, 0, com.google.android.material.R.style.Widget_MaterialComponents_Button_UnelevatedButton);
        btn.setText("Detail");
        btn.setTextSize(10);
        btn.setAllCaps(false);
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F2F4F7")));
        btn.setTextColor(android.graphics.Color.parseColor("#2C3E50"));
        
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                dpToPx(70), dpToPx(32));
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
        
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetailSiswaActivity.class);
            intent.putExtra("SISWA_ID", s.getId());
            startActivity(intent);
        });

        row.addView(btn, btnParams);
        container.addView(row);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(android.graphics.Color.parseColor("#F2F4F7"));
        container.addView(divider);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void setupClickListeners() {
        // Stats Row - Bisa diarahkan ke aktivitas terkait jika perlu
        View statTotalSiswa = findViewById(R.id.stat_total_siswa);
        if (statTotalSiswa != null) {
            statTotalSiswa.setOnClickListener(v -> {
                startActivity(new Intent(this, DaftarSiswaActivity.class));
            });
        }
    }
}