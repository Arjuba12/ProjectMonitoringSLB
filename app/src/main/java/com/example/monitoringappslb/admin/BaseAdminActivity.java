package com.example.monitoringappslb.admin;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
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

public abstract class BaseAdminActivity extends AppCompatActivity {

    protected abstract DrawerLayout getDrawerLayout();
    protected abstract NavigationView getNavigationView();
    protected abstract BottomNavigationView getBottomNavigationView();
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
                if (drawer == null) return;
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void setupDrawer() {
        NavigationView navigationView = getNavigationView();
        if (navigationView == null) return;

        bindDrawerHeader(navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_admin_logout) {
                logout();
                return true;
            }
            if (item.getItemId() == R.id.nav_admin_siswa) {
                startActivity(new Intent(this, SiswaAdminActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_admin_kelas) {
                Intent intent = new Intent(this, MasterDataAdminActivity.class);
                intent.putExtra(MasterDataAdminActivity.EXTRA_MODE, MasterDataAdminActivity.MODE_KELAS);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_admin_user) {
                Intent intent = new Intent(this, MasterDataAdminActivity.class);
                intent.putExtra(MasterDataAdminActivity.EXTRA_MODE, MasterDataAdminActivity.MODE_USER);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_admin_laporan) {
                startActivity(new Intent(this, LaporanAdminActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = getBottomNavigationView();
        if (bottomNav == null) return;

        int selfId = getSelfBottomNavItemId();
        if (selfId != -1) {
            bottomNav.setSelectedItemId(selfId);
        } else {
            int size = bottomNav.getMenu().size();
            for (int i = 0; i < size; i++) {
                bottomNav.getMenu().getItem(i).setCheckable(false);
                bottomNav.getMenu().getItem(i).setChecked(false);
                bottomNav.getMenu().getItem(i).setCheckable(true);
            }
        }
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selfId) return true;

            Class<?> target = null;
            if (id == R.id.nav_admin_home) {
                target = DashboardAdminActivity.class;
            } else if (id == R.id.nav_admin_kegiatan) {
                target = KegiatanAdminActivity.class;
            } else if (id == R.id.nav_admin_pengumuman) {
                target = PengumumanAdminActivity.class;
            } else if (id == R.id.nav_admin_profile) {
                target = ProfileAdminActivity.class;
            }

            if (target == null) return false;
            startActivity(new Intent(this, target));
            overridePendingTransition(0, 0);
            return true;
        });
    }

    private void bindToolbarUser() {
        SessionManager session = new SessionManager(this);
        TextView tvName = findViewById(R.id.tv_toolbar_admin_name);
        TextView tvInitials = findViewById(R.id.tv_toolbar_admin_initials);

        if (tvName != null) {
            String name = session.getUserNama();
            tvName.setText(name == null || name.trim().isEmpty() ? "Admin" : name);
        }
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
    }

    private void bindDrawerHeader(NavigationView navigationView) {
        if (navigationView.getHeaderCount() == 0) return;

        SessionManager session = new SessionManager(this);
        View header = navigationView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.tv_admin_name);
        TextView tvRole = header.findViewById(R.id.tv_admin_role);
        TextView tvInitials = header.findViewById(R.id.tv_admin_initials);

        if (tvName != null) {
            String name = session.getUserNama();
            tvName.setText(name == null || name.trim().isEmpty() ? "Administrator" : name);
        }
        if (tvRole != null) tvRole.setText("Monitoring dan Master Data");
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
    }

    protected void logout() {
        new SessionManager(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    protected DrawerLayout findAdminDrawer() {
        View root = findViewById(android.R.id.content);
        return findDrawerInTree(root);
    }

    private DrawerLayout findDrawerInTree(View view) {
        if (view instanceof DrawerLayout) {
            return (DrawerLayout) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }

        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            DrawerLayout drawer = findDrawerInTree(group.getChildAt(i));
            if (drawer != null) return drawer;
        }
        return null;
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
