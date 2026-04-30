package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.guru.ChatActivity;
import com.example.monitoringappslb.guru.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseWaliActivity extends AppCompatActivity {

    protected abstract DrawerLayout getDrawerLayout();
    protected abstract NavigationView getNavigationView();
    protected abstract BottomNavigationView getBottomNavigationView();
    protected abstract int getSelfNavDrawerItemId();
    protected abstract int getSelfBottomNavItemId();

    protected void setupNavigation() {
        setupDrawer();
        setupBottomNav();
        setupToolbar();
    }

    private void setupToolbar() {
        View btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getDrawerLayout() != null) {
                    getDrawerLayout().openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = getNavigationView();
        if (navigationView != null) {
            navigationView.setCheckedItem(getSelfNavDrawerItemId());
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfNavDrawerItemId()) {
                    getDrawerLayout().closeDrawer(GravityCompat.START);
                    return true;
                }

                if (id == R.id.nav_wali_dashboard) {
                    startActivity(new Intent(this, DashboardWaliActivity.class));
                } else if (id == R.id.nav_wali_perkembangan) {
                    startActivity(new Intent(this, PerkembanganWaliActivity.class));
                } else if (id == R.id.nav_wali_laporan) {
                    startActivity(new Intent(this, LaporanWaliActivity.class));
                } else if (id == R.id.nav_wali_kalender) {
                    startActivity(new Intent(this, KalenderWaliActivity.class));
                } else if (id == R.id.nav_wali_chat) {
                    startActivity(new Intent(this, ChatWaliActivity.class));
                } else if (id == R.id.nav_wali_profile) {
                    startActivity(new Intent(this, ProfileWaliActivity.class));
                } else if (id == R.id.nav_wali_logout) {
                    finishAffinity();
                }

                getDrawerLayout().closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = getBottomNavigationView();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(getSelfBottomNavItemId());
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfBottomNavItemId()) return true;

                if (id == R.id.nav_wali_home) {
                    startActivity(new Intent(this, DashboardWaliActivity.class));
                    return true;
                } else if (id == R.id.nav_wali_perkembangan) {
                    startActivity(new Intent(this, PerkembanganWaliActivity.class));
                    return true;
                } else if (id == R.id.nav_wali_kalender) {
                    startActivity(new Intent(this, KalenderWaliActivity.class));
                    return true;
                } else if (id == R.id.nav_wali_chat) {
                    startActivity(new Intent(this, ChatWaliActivity.class));
                    return true;
                } else if (id == R.id.nav_wali_profile) {
                    startActivity(new Intent(this, ProfileWaliActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = getDrawerLayout();
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}