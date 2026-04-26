package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseGuruActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;

    protected void setupBottomNav(int selectedItemId) {
        bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(selectedItemId);
            bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    
                    if (id == selectedItemId) return true;

                    if (id == R.id.nav_home) {
                        startActivity(new Intent(BaseGuruActivity.this, DashboardGuruActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_chat) {
                        startActivity(new Intent(BaseGuruActivity.this, ChatActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_notif) {
                        startActivity(new Intent(BaseGuruActivity.this, KirimPengumumanActivity.class));
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        // Handle Profile
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}