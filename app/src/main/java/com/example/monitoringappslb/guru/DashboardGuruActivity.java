package com.example.monitoringappslb.guru;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.monitoringappslb.R;

public class DashboardGuruActivity extends BaseGuruActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_guru);

        // Inisialisasi Bottom Navigation dari BaseActivity
        setupBottomNav(R.id.nav_home);
    }
}