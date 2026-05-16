package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.AvatarUtils;
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
        TextView tvNamaWali = findViewById(R.id.tv_nama_wali);
        TextView tvInitials = findViewById(R.id.tv_toolbar_wali_initials);

        if (tvNamaWali != null) {
            tvNamaWali.setText(session.getUserNama());
        }
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
    }

    private void setupDrawer() {
        NavigationView navigationView = getNavigationView();
        if (navigationView != null) {
            bindDrawerHeader(navigationView);
            navigationView.setCheckedItem(getSelfNavDrawerItemId());
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfNavDrawerItemId()) {
                    getDrawerLayout().closeDrawer(GravityCompat.START);
                    return true;
                }

                if (id == R.id.nav_wali_dashboard) {
                    startActivity(new Intent(this, DashboardWaliActivity.class));
                } else if (id == R.id.nav_wali_biodata) {
                    startActivity(new Intent(this, BiodataSiswaWaliActivity.class));
                } else if (id == R.id.nav_wali_ppi) {
                    startActivity(new Intent(this, DetailPpiWaliActivity.class));
                } else if (id == R.id.nav_wali_profile) {
                    startActivity(new Intent(this, ProfileWaliActivity.class));
                } else if (id == R.id.nav_wali_perkembangan) {
                    startActivity(new Intent(this, PerkembanganWaliActivity.class));
                } else if (id == R.id.nav_wali_rekap_absensi) {
                    startActivity(new Intent(this, RekapAbsensiWaliActivity.class));
                } else if (id == R.id.nav_wali_laporan) {
                    startActivity(new Intent(this, LaporanWaliActivity.class));
                } else if (id == R.id.nav_wali_kalender) {
                    startActivity(new Intent(this, KalenderWaliActivity.class));
                } else if (id == R.id.nav_wali_chat) {
                    startActivity(new Intent(this, ChatWaliActivity.class));
                } else if (id == R.id.nav_wali_logout) {
                    logout();
                }

                getDrawerLayout().closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void bindDrawerHeader(NavigationView navigationView) {
        if (navigationView.getHeaderCount() == 0) return;

        SessionManager session = new SessionManager(this);
        View header = navigationView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.tv_wali_name);
        TextView tvChild = header.findViewById(R.id.tv_wali_child);
        TextView tvInitials = header.findViewById(R.id.tv_wali_initials);

        if (tvName != null) {
            tvName.setText(session.getUserNama());
        }
        if (tvChild != null) {
            tvChild.setText("Wali murid");
        }
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
    }

    protected void logout() {
        new SessionManager(this).logout();
        Intent intent = new Intent(this, com.example.monitoringappslb.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
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
                } else if (id == R.id.nav_wali_biodata) {
                    startActivity(new Intent(this, BiodataSiswaWaliActivity.class));
                    return true;
                } else if (id == R.id.nav_wali_ppi) {
                    startActivity(new Intent(this, DetailPpiWaliActivity.class));
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
