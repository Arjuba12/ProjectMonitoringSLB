package com.example.monitoringappslb.wali;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class PerkembanganWaliActivity extends BaseWaliActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perkembangan_wali);

        setupNavigation();
        setupSemesterSpinner();
        loadDummyChartData();
    }

    private void loadDummyChartData() {
        // Data dummy: Persentase perkembangan (75, 78, 82, 88, 90) sesuai tabel
        int[] data = {75, 78, 82, 88, 90}; // Agu, Sep, Okt, Nov, Des
        
        View barAug = findViewById(R.id.bar_aug);
        View barSep = findViewById(R.id.bar_sep);
        View barOkt = findViewById(R.id.bar_okt);
        View barNov = findViewById(R.id.bar_nov);
        View barDes = findViewById(R.id.bar_des);

        // Set height bars secara dinamis berdasarkan data dummy (konversi ke DP)
        setBarHeight(barAug, data[0]);
        setBarHeight(barSep, data[1]);
        setBarHeight(barOkt, data[2]);
        setBarHeight(barNov, data[3]);
        setBarHeight(barDes, data[4]);
    }

    private void setBarHeight(View bar, int percentage) {
        if (bar == null) return;
        
        // Tinggi maksimal area chart adalah sekitar 180dp
        int maxHeightPx = (int) (180 * getResources().getDisplayMetrics().density);
        int targetHeight = (maxHeightPx * percentage) / 100;

        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = targetHeight;
        bar.setLayoutParams(params);
    }

    private void setupSemesterSpinner() {
        Spinner spinner = findViewById(R.id.spinner_semester);
        String[] items = new String[]{"Semester 1 - 2025/2026", "Semester 2 - 2024/2025"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_perkembangan);
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
        return R.id.nav_wali_perkembangan;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_perkembangan; // Dari Bottom Nav
    }
}