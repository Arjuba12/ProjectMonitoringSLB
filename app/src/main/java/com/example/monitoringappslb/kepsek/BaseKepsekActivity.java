package com.example.monitoringappslb.kepsek;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.LoginActivity;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.AvatarUtils;
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
        bindToolbarUser();

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

    private void bindToolbarUser() {
        SessionManager session = new SessionManager(this);
        TextView tvName = findViewById(R.id.tv_toolbar_kepsek_name);
        TextView tvInitials = findViewById(R.id.tv_toolbar_kepsek_initials);

        if (tvName != null) {
            String name = session.getUserNama();
            tvName.setText(name == null || name.trim().isEmpty() ? "Kepsek" : name);
        }
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
    }

    private void setupDrawer() {
        NavigationView navigationView = getNavigationView();
        if (navigationView != null) {
            bindDrawerHeader(navigationView);
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

    private void bindDrawerHeader(NavigationView navigationView) {
        if (navigationView.getHeaderCount() == 0) return;

        SessionManager session = new SessionManager(this);
        View header = navigationView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.tv_kepsek_name);
        TextView tvRole = header.findViewById(R.id.tv_kepsek_role);
        TextView tvInitials = header.findViewById(R.id.tv_kepsek_initials);

        if (tvName != null) {
            String name = session.getUserNama();
            tvName.setText(name == null || name.trim().isEmpty() ? "Kepala Sekolah" : name);
        }
        if (tvRole != null) {
            tvRole.setText("Kepala Sekolah SLB");
        }
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
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
