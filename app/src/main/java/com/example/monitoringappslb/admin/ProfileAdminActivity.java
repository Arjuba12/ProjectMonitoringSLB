package com.example.monitoringappslb.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.MeResponse;
import com.example.monitoringappslb.model.response.UserResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAdminActivity extends BaseAdminActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);
        apiService = ApiClient.getService();
        setupNavigation();
        displayLoading();
        loadProfile();

        View btnLogout = findViewById(R.id.btn_logout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logout());
    }

    private void displayLoading() {
        setupInfoItem(findViewById(R.id.info_nama), "Nama Lengkap", "Memuat...");
        setupInfoItem(findViewById(R.id.info_email), "Email", "-");
        setupInfoItem(findViewById(R.id.info_role), "Role", "Admin");
    }

    private void loadProfile() {
        apiService.getMe().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    Toast.makeText(ProfileAdminActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindProfile(response.body().getData());
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(ProfileAdminActivity.this, "Tidak bisa memuat profil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfile(UserResponse user) {
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvRole = findViewById(R.id.tv_profile_role);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        if (tvName != null) tvName.setText(valueOrDash(user.getNama()));
        if (tvRole != null) tvRole.setText("Admin Sistem");
        AvatarUtils.applyInitialAvatar(tvInitials, user.getNama(), user.getEmail());

        setupInfoItem(findViewById(R.id.info_nama), "Nama Lengkap", valueOrDash(user.getNama()));
        setupInfoItem(findViewById(R.id.info_email), "Email", valueOrDash(user.getEmail()));
        setupInfoItem(findViewById(R.id.info_role), "Role", valueOrDash(user.getRole()));
    }

    private void setupInfoItem(View view, String label, String value) {
        if (view == null) return;
        TextView tvLabel = view.findViewById(R.id.tv_label);
        TextView tvValue = view.findViewById(R.id.tv_value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_profile; }
}
