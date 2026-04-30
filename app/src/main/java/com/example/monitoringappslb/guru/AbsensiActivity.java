package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.adapter.AbsensiAdapter;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Siswa;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class AbsensiActivity extends BaseGuruActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private AbsensiAdapter adapter;
    private List<Siswa> siswaList;
    private TextView tvSummaryHadir, tvSummaryIzin, tvSummaryAlpa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absensi);

        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.rv_absensi_siswa);
        tvSummaryHadir = findViewById(R.id.tv_summary_hadir);
        tvSummaryIzin = findViewById(R.id.tv_summary_izin);
        tvSummaryAlpa = findViewById(R.id.tv_summary_alpa);
        Button btnSimpan = findViewById(R.id.btn_simpan_absensi);

        // Inisialisasi Bottom Navigation
        setupBottomNav(-1);

        // Setup Hamburger Menu
        setupNavigationDrawer(drawerLayout);

        // Load Dummy Data
        loadDummyData();

        // Setup RecyclerView
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AbsensiAdapter(siswaList, this::updateSummary);
            recyclerView.setAdapter(adapter);
        }

        updateSummary();

        // Save Button Logic
        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> {
                Toast.makeText(this, "Absensi hari ini berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void loadDummyData() {
        siswaList = DummyDatabase.getSiswaList();
    }

    private void updateSummary() {
        int hadir = 0;
        int izinSakit = 0;
        int alpa = 0;

        for (Siswa s : siswaList) {
            if (s.getStatusAbsensi().equals("H")) hadir++;
            else if (s.getStatusAbsensi().equals("S") || s.getStatusAbsensi().equals("I")) izinSakit++;
            else if (s.getStatusAbsensi().equals("A")) alpa++;
        }

        tvSummaryHadir.setText(String.valueOf(hadir));
        tvSummaryIzin.setText(String.valueOf(izinSakit));
        tvSummaryAlpa.setText(String.valueOf(alpa));
    }
}