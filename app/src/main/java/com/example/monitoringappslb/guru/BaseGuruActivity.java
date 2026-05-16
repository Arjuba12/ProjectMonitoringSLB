package com.example.monitoringappslb.guru;

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

public abstract class BaseGuruActivity extends AppCompatActivity {

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
        bindToolbarProfile();

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

                if (id == R.id.nav_absensi) {
                    startActivity(new Intent(this, AbsensiActivity.class));
                } else if (id == R.id.nav_ppi) {
                    startActivity(new Intent(this, PpiActivity.class));
                } else if (id == R.id.nav_laporan) {
                    startActivity(new Intent(this, LaporanKelasActivity.class));
                } else if (id == R.id.nav_input_perkembangan) {
                    startActivity(new Intent(this, InputPerkembanganActivity.class));
                } else if (id == R.id.nav_siswa) {
                    startActivity(new Intent(this, DaftarSiswaActivity.class));
                } else if (id == R.id.nav_logout) {
                    logout();
                    return true;
                }

                getDrawerLayout().closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void bindToolbarProfile() {
        SessionManager session = new SessionManager(this);
        String name = session.getUserNama();
        String email = session.getUserEmail();

        TextView tvName = findViewById(R.id.tv_toolbar_user_name);
        TextView tvInitials = findViewById(R.id.tv_toolbar_initials);

        if (tvName != null) {
            tvName.setText(valueOrDash(name));
        }
        AvatarUtils.applyInitialAvatar(tvInitials, name, email);
    }

    private void bindDrawerHeader(NavigationView navigationView) {
        View header = navigationView.getHeaderView(0);
        if (header == null) return;

        SessionManager session = new SessionManager(this);
        String name = session.getUserNama();
        String email = session.getUserEmail();

        TextView tvInitials = header.findViewById(R.id.tv_nav_initials);
        TextView tvName = header.findViewById(R.id.tv_nama_guru);
        TextView tvRole = header.findViewById(R.id.tv_nav_role);

        if (tvInitials != null) {
            AvatarUtils.applyInitialAvatar(tvInitials, name, email);
        }
        if (tvName != null) {
            tvName.setText(valueOrDash(name));
        }
        if (tvRole != null) {
            tvRole.setText("Guru");
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = getBottomNavigationView();
        if (bottomNav != null) {
            int selfId = getSelfBottomNavItemId();
            if (selfId != -1) {
                bottomNav.setSelectedItemId(selfId);
            } else {
                // Unselect all if -1
                int size = bottomNav.getMenu().size();
                for (int i = 0; i < size; i++) {
                    bottomNav.getMenu().getItem(i).setCheckable(false);
                    bottomNav.getMenu().getItem(i).setChecked(false);
                    bottomNav.getMenu().getItem(i).setCheckable(true);
                }
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == getSelfBottomNavItemId()) return true;

                Intent intent = null;
                if (id == R.id.nav_home) {
                    intent = new Intent(this, DashboardGuruActivity.class);
                } else if (id == R.id.nav_chat) {
                    intent = new Intent(this, ChatActivity.class);
                } else if (id == R.id.nav_announcement) {
                    intent = new Intent(this, KirimPengumumanActivity.class);
                } else if (id == R.id.nav_profile) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    protected void logout() {
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
