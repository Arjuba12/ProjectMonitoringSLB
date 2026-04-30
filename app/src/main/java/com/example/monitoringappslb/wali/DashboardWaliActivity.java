package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class DashboardWaliActivity extends BaseWaliActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_wali);

        setupNavigation();
        
        // Handle clicks for cards on dashboard if needed
        findViewById(R.id.card_perkembangan).setOnClickListener(v -> {
            startActivity(new Intent(this, PerkembanganWaliActivity.class));
        });
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
        return R.id.nav_wali_dashboard;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_home;
    }
}