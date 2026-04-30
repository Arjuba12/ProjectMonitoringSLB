package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseGuruActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;
    protected DrawerLayout drawerLayout;

    protected void setupBottomNav(int selectedItemId) {
        bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            if (selectedItemId != -1) {
                bottomNav.setSelectedItemId(selectedItemId);
            } else {
                // Unselect all items if -1 is passed
                int size = bottomNav.getMenu().size();
                for (int i = 0; i < size; i++) {
                    bottomNav.getMenu().getItem(i).setCheckable(false);
                    bottomNav.getMenu().getItem(i).setChecked(false);
                    bottomNav.getMenu().getItem(i).setCheckable(true);
                }
            }

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (selectedItemId != -1 && id == selectedItemId) return true;

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
                    if (selectedItemId != -1) finish();
                    return true;
                }
                return false;
            });
        }
    }

    protected void setupNavigationDrawer(DrawerLayout drawer) {
        this.drawerLayout = drawer;
        View btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null && drawerLayout != null) {
            btnMenu.setOnClickListener(v -> {
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null && drawerLayout != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_absensi) {
                    intent = new Intent(this, AbsensiActivity.class);
                } else if (id == R.id.nav_ppi) {
                    intent = new Intent(this, PpiActivity.class);
                } else if (id == R.id.nav_laporan) {
                    intent = new Intent(this, LaporanKelasActivity.class);
                } else if (id == R.id.nav_siswa) {
                    intent = new Intent(this, DaftarSiswaActivity.class);
                } else if (id == R.id.nav_logout) {
                    // Handle logout
                    finishAffinity();
                    return true;
                }

                if (intent != null) {
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // We don't finish() here if we want to allow back navigation to dashboard
                    // but for school app consistency, often we do. Let's not finish() for drawer.
                }
                return true;
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}