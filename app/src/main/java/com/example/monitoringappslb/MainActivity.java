package com.example.monitoringappslb;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monitoringappslb.guru.DashboardGuruActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Langsung arahkan ke Dashboard Guru untuk keperluan testing
        Intent intent = new Intent(this, DashboardGuruActivity.class);
        startActivity(intent);
        finish(); // Menutup MainActivity agar tidak bisa kembali ke sini
    }
}