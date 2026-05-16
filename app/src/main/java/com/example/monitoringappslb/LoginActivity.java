package com.example.monitoringappslb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.monitoringappslb.guru.DashboardGuruActivity;
import com.example.monitoringappslb.kepsek.DashboardKepsekActivity;
import com.example.monitoringappslb.model.response.ApiModels.*;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.wali.DashboardWaliActivity;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextView tvPortalTitle, tvLoginHeader, tvLoginSubheader, tvFooter;
    private EditText etIdentifier, etPassword;
    private Button btnLogin;
    private ChipGroup cgRoles;
    private ProgressBar progressBar;

    private int currentRole = 0; // 0=Guru, 1=Kepsek, 2=Wali, 3=Admin
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init API
        ApiClient.init(getApplicationContext());
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        // Kalau sudah login, langsung ke dashboard
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard(sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
        setupRoleSwitcher();
        updateUIForRole(0);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void initViews() {
        tvPortalTitle    = findViewById(R.id.tv_portal_title);
        tvLoginHeader    = findViewById(R.id.tv_login_header);
        tvLoginSubheader = findViewById(R.id.tv_login_subheader);
        tvFooter         = findViewById(R.id.tv_footer);
        etIdentifier     = findViewById(R.id.et_identifier);
        etPassword       = findViewById(R.id.et_password);
        btnLogin         = findViewById(R.id.btn_login);
        cgRoles          = findViewById(R.id.cg_roles);
        progressBar      = findViewById(R.id.progress_bar); // Tambahkan ProgressBar di layout
    }

    private void setupRoleSwitcher() {
        cgRoles.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chip_guru)   currentRole = 0;
            else if (id == R.id.chip_kepsek) currentRole = 1;
            else if (id == R.id.chip_wali)   currentRole = 2;
            else if (id == R.id.chip_admin)  currentRole = 3;
            updateUIForRole(currentRole);
        });
    }

    private void handleLogin() {
        String email    = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap isi email dan password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        apiService.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse res = response.body();

                    // Simpan session
                    sessionManager.saveSession(res.getData().getToken(), res.getData().getUser());

                    Toast.makeText(LoginActivity.this,
                            "Selamat datang, " + res.getData().getUser().getNama() + "!",
                            Toast.LENGTH_SHORT).show();

                    // Navigasi sesuai role dari server (bukan dari chip)
                    navigateToDashboard(res.getData().getUser().getRole());
                } else {
                    String msg = "Login gagal";
                    try {
                        // Coba baca pesan error dari server
                        if (response.errorBody() != null) {
                            msg = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Tidak bisa terhubung ke server.\nPastikan backend sudah jalan.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "kepsek":
            case "admin":
                intent = new Intent(this, DashboardKepsekActivity.class);
                break;
            case "wali":
                intent = new Intent(this, DashboardWaliActivity.class);
                break;
            default: // "guru"
                intent = new Intent(this, DashboardGuruActivity.class);
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setText(loading ? "Masuk..." : "Masuk ke Sistem");
    }

    private void updateUIForRole(int role) {
        switch (role) {
            case 0:
                tvLoginHeader.setText("Portal Guru");
                tvLoginSubheader.setText("Masuk dengan Email institusi");
                break;
            case 1:
                tvLoginHeader.setText("Portal Kepala Sekolah");
                tvLoginSubheader.setText("Masuk untuk memantau aktivitas sekolah");
                break;
            case 2:
                tvLoginHeader.setText("Portal Wali Murid");
                tvLoginSubheader.setText("Pantau perkembangan belajar anak Anda");
                break;
            case 3:
                tvLoginHeader.setText("Administrator");
                tvLoginSubheader.setText("Panel kendali sistem monitoring");
                break;
        }
    }
}
