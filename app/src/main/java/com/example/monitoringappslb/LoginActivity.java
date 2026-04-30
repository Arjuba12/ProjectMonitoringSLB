package com.example.monitoringappslb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monitoringappslb.guru.DashboardGuruActivity;
import com.example.monitoringappslb.wali.DashboardWaliActivity;
import com.google.android.material.chip.ChipGroup;

public class LoginActivity extends AppCompatActivity {

    private TextView tvPortalTitle, tvLoginHeader, tvLoginSubheader, tvFooter;
    private EditText etIdentifier, etPassword;
    private Button btnLogin;
    private ChipGroup cgRoles;
    
    // 0: Guru, 1: Kepsek, 2: Wali, 3: Admin
    private int currentRole = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupRoleSwitcher();
        
        // Setup initial role (default Guru)
        updateUIForRole(0);

        btnLogin.setOnClickListener(v -> {
            handleLogin();
        });
    }

    private void initViews() {
        tvPortalTitle = findViewById(R.id.tv_portal_title);
        tvLoginHeader = findViewById(R.id.tv_login_header);
        tvLoginSubheader = findViewById(R.id.tv_login_subheader);
        tvFooter = findViewById(R.id.tv_footer);
        etIdentifier = findViewById(R.id.et_identifier);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        cgRoles = findViewById(R.id.cg_roles);
    }

    private void setupRoleSwitcher() {
        cgRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_guru) currentRole = 0;
            else if (checkedId == R.id.chip_kepsek) currentRole = 1;
            else if (checkedId == R.id.chip_wali) currentRole = 2;
            else if (checkedId == R.id.chip_admin) currentRole = 3;
            
            updateUIForRole(currentRole);
        });
    }

    private void handleLogin() {
        String id = etIdentifier.getText().toString();
        String pass = etPassword.getText().toString();

        if (id.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic login sesuai role
        if (currentRole == 0) {
            startActivity(new Intent(this, DashboardGuruActivity.class));
        } else if (currentRole == 2) {
            startActivity(new Intent(this, com.example.monitoringappslb.wali.DashboardWaliActivity.class));
        } else {
            Toast.makeText(this, "Akses " + tvPortalTitle.getText() + " sedang disiapkan", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIForRole(int role) {
        switch (role) {
            case 0: // Guru
                tvPortalTitle.setText("MONITORING SLB");
                tvLoginHeader.setText("Portal Guru");
                tvLoginSubheader.setText("Masuk dengan NIP atau Email institusi");
                tvFooter.setText("Belum memiliki akun? Hubungi Admin");
                btnLogin.setText("Masuk ke Sistem");
                break;
            case 1: // Kepsek
                tvPortalTitle.setText("MONITORING SLB");
                tvLoginHeader.setText("Portal Kepala Sekolah");
                tvLoginSubheader.setText("Masuk untuk memantau aktivitas sekolah");
                tvFooter.setText("Belum memiliki akun? Hubungi Admin");
                btnLogin.setText("Masuk ke Sistem");
                break;
            case 2: // Wali Murid
                tvPortalTitle.setText("MONITORING SLB");
                tvLoginHeader.setText("Portal Wali Murid");
                tvLoginSubheader.setText("Pantau perkembangan belajar anak Anda");
                tvFooter.setText("Baru pertama kali? Aktivasi Akun");
                btnLogin.setText("Masuk ke Portal");
                break;
            case 3: // Admin
                tvPortalTitle.setText("MONITORING SLB");
                tvLoginHeader.setText("Administrator");
                tvLoginSubheader.setText("Panel kendali sistem monitoring");
                tvFooter.setText("Akses terbatas untuk petugas");
                btnLogin.setText("Masuk Admin");
                break;
        }
    }
}
