package com.example.monitoringappslb.wali;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ProfileWaliActivity extends BaseWaliActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_wali);

        setupNavigation();
        setupProfileData();
        
        // Setup PPI details if needed, though they are currently static in XML
    }

    private void setupProfileData() {
        // Set NISN
        View rowNisn = findViewById(R.id.row_nisn);
        ((android.widget.TextView) rowNisn.findViewById(R.id.label)).setText("NISN");
        ((android.widget.TextView) rowNisn.findViewById(R.id.value)).setText("1234567890");

        // Set Tgl Lahir
        View rowTgl = findViewById(R.id.row_tgl_lahir);
        ((android.widget.TextView) rowTgl.findViewById(R.id.label)).setText("Tgl Lahir");
        ((android.widget.TextView) rowTgl.findViewById(R.id.value)).setText("14 Maret 2011");

        // Set Guru Kelas
        View rowGuru = findViewById(R.id.row_guru);
        ((android.widget.TextView) rowGuru.findViewById(R.id.label)).setText("Guru Kelas");
        ((android.widget.TextView) rowGuru.findViewById(R.id.value)).setText("Bu Hartini Sri R.");

        // Set Tahun Masuk
        View rowTahun = findViewById(R.id.row_tahun);
        ((android.widget.TextView) rowTahun.findViewById(R.id.label)).setText("Tahun Masuk");
        ((android.widget.TextView) rowTahun.findViewById(R.id.value)).setText("2019");

        // Set Wali
        View rowWali = findViewById(R.id.row_wali);
        ((android.widget.TextView) rowWali.findViewById(R.id.label)).setText("Wali");
        ((android.widget.TextView) rowWali.findViewById(R.id.value)).setText("Bu Sari Murni");
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_wali);
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
        return R.id.nav_wali_profile;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_profile;
    }
}