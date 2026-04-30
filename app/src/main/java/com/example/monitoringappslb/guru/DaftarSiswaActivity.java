package com.example.monitoringappslb.guru;

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
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Siswa;
import java.util.List;

public class DaftarSiswaActivity extends BaseGuruActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_siswa);

        // Inisialisasi Navigasi Side Drawer
        DrawerLayout dl = findViewById(R.id.drawer_layout);
        if (dl != null) {
            setupNavigationDrawer(dl);
        }
        
        // Inisialisasi Bottom Navigation
        setupBottomNav(-1);

        // Setup RecyclerView Daftar Siswa
        RecyclerView recyclerView = findViewById(R.id.rv_daftar_siswa);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            DaftarSiswaAdapter adapter = new DaftarSiswaAdapter(DummyDatabase.getSiswaList(), this);
            recyclerView.setAdapter(adapter);
        }

        // Setup Tabel Rekapitulasi PPI
        setupTableRekap();

        // Feedback Dummy
        Toast.makeText(this, "Memuat daftar siswa...", Toast.LENGTH_SHORT).show();
    }

    private void setupTableRekap() {
        TableLayout tableLayout = findViewById(R.id.table_rekap_ppi);
        if (tableLayout == null) return;

        List<Siswa> listSiswa = DummyDatabase.getSiswaList();
        
        for (Siswa siswa : listSiswa) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(android.graphics.Color.WHITE);

            row.addView(createCell(siswa.getNama(), false));
            row.addView(createCell(siswa.getStatsKehadiran(), true));
            row.addView(createCell(siswa.getStatsKognitif(), true));
            row.addView(createCell(siswa.getStatsMotorik(), true));
            row.addView(createCell(siswa.getStatsKomunikasi(), true));
            row.addView(createCell(siswa.getStatsBinaDiri(), true));

            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean center) {
        TextView tv = new TextView(this);
        tv.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(android.graphics.Color.parseColor("#333333"));
        if (center) {
            tv.setGravity(Gravity.CENTER);
        }
        return tv;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}