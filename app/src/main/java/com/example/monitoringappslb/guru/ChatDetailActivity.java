package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;

public class ChatDetailActivity extends BaseGuruActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Inisialisasi Bottom Navigation (Tetap aktif di detail jika sesuai desain)
        setupBottomNav(R.id.nav_chat);

        // Setup Hamburger Menu
        setupNavigationDrawer(drawerLayout);

        // Back Button Logic
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Side Navigation Logic
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                // Biarkan Base atau Activity lain menangani jika perlu
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }
}