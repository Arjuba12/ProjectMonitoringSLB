package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class InputHarianListActivity extends BaseGuruActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_harian_list);

        setupNavigation();

        Button btnInputBaru = findViewById(R.id.btn_input_baru);
        if (btnInputBaru != null) {
            btnInputBaru.setOnClickListener(v -> {
                startActivity(new Intent(this, InputPerkembanganActivity.class));
            });
        }
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_input_perkembangan;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }
}
