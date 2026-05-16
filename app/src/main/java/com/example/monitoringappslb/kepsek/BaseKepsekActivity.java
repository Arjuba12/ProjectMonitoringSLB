package com.example.monitoringappslb.kepsek;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.LoginActivity;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseKepsekActivity extends AppCompatActivity {

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
                DrawerLayout drawer = getDrawerLayout();
                if (drawer != null) {
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    } else {
                        drawer.openDrawer(GravityCompat.START);
                    }
                }
            });
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = getNavigationView();
        if (navigationView != null) {
            int selfId = getSelfNavDrawerItemId();
            if (selfId != -1) {
                navigationView.setCheckedItem(selfId);
            }
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfNavDrawerItemId()) {
                    getDrawerLayout().closeDrawer(GravityCompat.START);
                    return true;
                }

                if (id == R.id.nav_logout) {
                    logout();
                    return true;
                }
                
                boolean navigated = navigateTo(id);
                if (navigated) {
                    getDrawerLayout().closeDrawer(GravityCompat.START);
                }
                return navigated;
            });
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = getBottomNavigationView();
        if (bottomNav != null) {
            int selfId = getSelfBottomNavItemId();
            if (selfId != -1) {
                bottomNav.setSelectedItemId(selfId);
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfBottomNavItemId()) return true;
                return navigateTo(id);
            });
        }
    }

    private boolean navigateTo(int id) {
        Class<?> targetActivity = null;

        if (id == R.id.nav_home) {
            targetActivity = DashboardKepsekActivity.class;
        } else if (id == R.id.nav_manajemen) {
            targetActivity = ManajemenKelasActivity.class;
        } else if (id == R.id.nav_rekap) {
            targetActivity = RekapSekolahActivity.class;
        } else if (id == R.id.nav_announcement) {
            targetActivity = KirimPengumumanKepsekActivity.class;
        } else if (id == R.id.nav_profile) {
            targetActivity = ProfileKepsekActivity.class;
        }

        if (targetActivity != null) {
            if (this.getClass().equals(targetActivity)) {
                return true;
            }
            Intent intent = new Intent(this, targetActivity);
            startActivity(intent);
            overridePendingTransition(0, 0);
            return true;
        }
        return false;
    }

    private void logout() {
        new SessionManager(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
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
