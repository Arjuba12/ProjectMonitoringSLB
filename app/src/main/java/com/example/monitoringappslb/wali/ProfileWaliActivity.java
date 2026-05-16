package com.example.monitoringappslb.wali;

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
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileWaliActivity extends BaseWaliActivity {
    private ApiService apiService;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);

        setupNavigation();
        displayLoading();
        loadProfileData();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null) loadProfileData();
    }

    private void displayLoading() {
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvRole = findViewById(R.id.tv_profile_role);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        if (tvName != null) tvName.setText("Memuat...");
        if (tvRole != null) tvRole.setText("Orang Tua / Wali Murid");
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());

        setupInfoRow(findViewById(R.id.row_nik), "Nama Lengkap", "Memuat...");
        setupInfoRow(findViewById(R.id.row_phone), "No. Telepon", "-");
        setupInfoRow(findViewById(R.id.row_email), "Email", "-");
        setupInfoRow(findViewById(R.id.row_address), "Hubungan", "-");
        setupInfoRow(findViewById(R.id.row_child_name), "Anak", "-");
    }

    private void loadProfileData() {
        apiService.getMe().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess() || response.body().getData() == null) {
                    Toast.makeText(ProfileWaliActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                    bindSessionFallback();
                    return;
                }

                bindProfile(response.body().getData());
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(ProfileWaliActivity.this, "Tidak bisa memuat profil", Toast.LENGTH_SHORT).show();
                bindSessionFallback();
            }
        });
    }

    private void bindProfile(UserResponse user) {
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvRole = findViewById(R.id.tv_profile_role);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        String child = buildChildText(user.getNamaSiswa(), user.getNamaKelas());
        if (tvName != null) tvName.setText(valueOrDash(user.getNama()));
        if (tvRole != null) tvRole.setText("Wali Murid" + ("-".equals(child) ? "" : " | " + child));
        AvatarUtils.applyInitialAvatar(tvInitials, user.getNama(), user.getEmail());

        if (user.getSiswaId() != null) session.saveSiswaId(user.getSiswaId());

        setupInfoRow(findViewById(R.id.row_nik), "Nama Lengkap", valueOrDash(user.getNama()));
        setupInfoRow(findViewById(R.id.row_phone), "No. Telepon", valueOrDash(user.getNoHp()));
        setupInfoRow(findViewById(R.id.row_email), "Email", valueOrDash(user.getEmail()));
        setupInfoRow(findViewById(R.id.row_address), "Hubungan", valueOrDash(user.getHubungan()));
        setupInfoRow(findViewById(R.id.row_child_name), "Anak", child);
    }

    private void bindSessionFallback() {
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        if (tvName != null) tvName.setText(valueOrDash(session.getUserNama()));
        AvatarUtils.applyInitialAvatar(tvInitials, session.getUserNama(), session.getUserEmail());
        setupInfoRow(findViewById(R.id.row_nik), "Nama Lengkap", valueOrDash(session.getUserNama()));
        setupInfoRow(findViewById(R.id.row_email), "Email", valueOrDash(session.getUserEmail()));
    }

    private void setupActions() {
        TextView btnLogout = findViewById(R.id.btn_logout_profile);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void setupInfoRow(View row, String label, String value) {
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.label);
        TextView tvValue = row.findViewById(R.id.value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(valueOrDash(value));
    }

    private String buildChildText(String namaSiswa, String namaKelas) {
        String child = valueOrDash(namaSiswa);
        if ("-".equals(child)) return "-";
        String kelas = valueOrDash(namaKelas);
        return "-".equals(kelas) ? child : child + " (" + kelas + ")";
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_wali);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view_wali);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation_wali);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_profile;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_profile;
    }
}
