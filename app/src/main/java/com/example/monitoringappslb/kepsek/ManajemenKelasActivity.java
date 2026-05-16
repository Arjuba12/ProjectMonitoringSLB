package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.Toast;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ManajemenKelasActivity extends BaseKepsekActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manajemen_kelas_kepsek);

        setupNavigation();

        findViewById(R.id.btn_tambah_kelas).setOnClickListener(v -> {
            Toast.makeText(this, "Fitur tambah kelas akan segera hadir", Toast.LENGTH_SHORT).show();
        });
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
