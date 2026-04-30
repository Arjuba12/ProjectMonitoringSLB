package com.example.monitoringappslb.wali;

import android.os.Bundle;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

public class KalenderWaliActivity extends BaseWaliActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalender_wali);

        drawerLayout = findViewById(R.id.drawer_layout_wali);
        navigationView = findViewById(R.id.nav_view_wali);
        bottomNavigationView = findViewById(R.id.bottom_navigation_wali);

        setupNavigation();
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    protected NavigationView getNavigationView() {
        return navigationView;
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_kalender;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_kalender;
    }
}