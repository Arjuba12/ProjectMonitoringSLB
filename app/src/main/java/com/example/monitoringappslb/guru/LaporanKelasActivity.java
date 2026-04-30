package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.RekapKehadiran;

import java.util.List;

public class LaporanKelasActivity extends BaseGuruActivity {

    private Spinner spinnerMonth, spinnerClass;
    private TableLayout tableAttendance;
    private LinearLayout containerGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_kelas);

        // UI Setup
        setupBottomNav(-1);
        setupNavigationDrawer(findViewById(R.id.drawer_layout));

        initViews();
        setupSpinners();
        loadAttendanceData();
        setupChart();

        findViewById(R.id.btn_save).setOnClickListener(v -> 
            Toast.makeText(this, "Catatan naratif berhasil disimpan", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnExportPdf).setOnClickListener(v ->
            Toast.makeText(this, "Mengekspor laporan ke PDF...", Toast.LENGTH_SHORT).show()
        );
    }

    private void initViews() {
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerClass = findViewById(R.id.spinner_class);
        tableAttendance = findViewById(R.id.table_attendance);
        containerGraph = findViewById(R.id.container_graph);
    }

    private void setupSpinners() {
        String[] months = {"November 2024", "Oktober 2024", "September 2024"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        String[] classes = {"Kelas VII-A", "Kelas VII-B", "Kelas VIII-A"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);
    }

    private void loadAttendanceData() {
        List<RekapKehadiran> rekapList = DummyDatabase.getRekapKehadiranList();
        
        for (RekapKehadiran r : rekapList) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 0, 0, 8);

            TextView tvName = createTableCell(r.getNamaSiswa(), false);
            TextView tvHadir = createTableCell(String.valueOf(r.getHadir()), true);
            TextView tvSakit = createTableCell(String.valueOf(r.getSakit()), true);
            TextView tvIzin = createTableCell(String.valueOf(r.getIzin()), true);
            TextView tvAlpha = createTableCell(String.valueOf(r.getAlpha()), true);

            row.addView(tvName);
            row.addView(tvHadir);
            row.addView(tvSakit);
            row.addView(tvIzin);
            row.addView(tvAlpha);

            tableAttendance.addView(row);
            
            // Add separator line
            View line = new View(this);
            line.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1));
            line.setBackgroundColor(getResources().getColor(R.color.gray_light, getTheme()));
            tableAttendance.addView(line);
        }
    }

    private void setupChart() {
        int[] stats = DummyDatabase.getRataRataKelas();
        containerGraph.removeAllViews();
        
        for (int stat : stats) {
            // Container for each bar and its value text
            LinearLayout barContainer = new LinearLayout(this);
            barContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            barContainer.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            barContainer.setOrientation(LinearLayout.VERTICAL);

            // Percentage Text above the bar
            TextView tvValue = new TextView(this);
            tvValue.setText(stat + "%");
            tvValue.setTextSize(9);
            tvValue.setTextColor(0xFF7F8C8D);
            tvValue.setGravity(android.view.Gravity.CENTER);
            tvValue.setPadding(0, 0, 0, 4);
            barContainer.addView(tvValue);

            View bar = new View(this);
            // Scale height (stat is 0-100, max height ~100dp)
            int heightPx = (int) (stat * getResources().getDisplayMetrics().density);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (28 * getResources().getDisplayMetrics().density), // bar width 28dp
                    heightPx
            );
            bar.setLayoutParams(params);
            
            // Rounded bar with color based on performance
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setCornerRadii(new float[]{15, 15, 15, 15, 0, 0, 0, 0}); // Top corners rounded
            
            if (stat >= 80) shape.setColor(0xFF2E7D32); // Green
            else if (stat >= 60) shape.setColor(0xFFE67E22); // Orange
            else shape.setColor(0xFFC0392B); // Red
            
            bar.setBackground(shape);
            
            barContainer.addView(bar);
            containerGraph.addView(barContainer);
        }
    }

    private TextView createTableCell(String text, boolean center) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        tv.setPadding(0, 12, 0, 12);
        
        if (center) {
            tv.setGravity(android.view.Gravity.CENTER);
            // Use DP for width to ensure consistency
            int widthPx = (int) (50 * getResources().getDisplayMetrics().density);
            tv.setLayoutParams(new TableRow.LayoutParams(widthPx, TableRow.LayoutParams.WRAP_CONTENT));
        } else {
            // For the name column, allow it to wrap/stretch as needed by TableLayout
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        }
        return tv;
    }
}