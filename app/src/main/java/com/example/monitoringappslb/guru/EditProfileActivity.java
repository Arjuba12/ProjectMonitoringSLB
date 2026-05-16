package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.MeResponse;
import com.example.monitoringappslb.model.response.UserResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends BaseGuruActivity {

    private EditText etNama, etNip, etEmail, etNoHp, etSpesialisasi;
    private ApiService apiService;
    private String currentSpesialisasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        setupNavigation();
        apiService = ApiClient.getService();

        initViews();
        loadCurrentData();

        findViewById(R.id.btn_simpan).setOnClickListener(v -> {
            saveData();
        });
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
        return -1;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }

    private void initViews() {
        etNama = findViewById(R.id.et_nama);
        etNip = findViewById(R.id.et_nip);
        etEmail = findViewById(R.id.et_email);
        etNoHp = findViewById(R.id.et_no_hp);
        etSpesialisasi = findViewById(R.id.et_kelas);
    }

    private void loadCurrentData() {
        apiService.getMe().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess() || response.body().getData() == null) {
                    Toast.makeText(EditProfileActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                    return;
                }

                bindForm(response.body().getData());
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Tidak bisa memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindForm(UserResponse user) {
        currentSpesialisasi = user.getSpesialisasi();
        etNama.setText(valueOrEmpty(user.getNama()));
        etNip.setText(valueOrEmpty(user.getNip()));
        etEmail.setText(valueOrEmpty(user.getEmail()));
        etNoHp.setText(valueOrEmpty(user.getNoHp()));
        etSpesialisasi.setText(valueOrEmpty(user.getSpesialisasi()));
    }

    private void saveData() {
        String nama = etNama.getText().toString().trim();
        String nip = etNip.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String noHp = etNoHp.getText().toString().trim();
        String spesialisasi = etSpesialisasi.getText().toString().trim();

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nama", nama);
        body.put("email", email);
        body.put("no_hp", noHp);
        body.put("nip", nip);
        body.put("spesialisasi", spesialisasi.isEmpty() ? currentSpesialisasi : spesialisasi);

        findViewById(R.id.btn_simpan).setEnabled(false);

        apiService.updateProfile(body).enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                findViewById(R.id.btn_simpan).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EditProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Gagal menyimpan profil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                findViewById(R.id.btn_simpan).setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
