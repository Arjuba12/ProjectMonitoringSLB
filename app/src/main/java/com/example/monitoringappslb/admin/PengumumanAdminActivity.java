package com.example.monitoringappslb.admin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class PengumumanAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengumuman_admin);
        setupNavigation();
        setPageText("Pengumuman", "Kirim pengumuman umum atau per kelas. Halaman ini dipisah dari pengumuman guru dan kepsek.");
    }

    private void setPageText(String title, String description) {
        TextView tvTitle = findViewById(R.id.tv_admin_page_title);
        TextView tvDescription = findViewById(R.id.tv_admin_page_description);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvDescription != null) tvDescription.setText(description);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_pengumuman; }
}
