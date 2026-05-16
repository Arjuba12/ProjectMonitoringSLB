package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.MeResponse;
import com.example.monitoringappslb.model.response.UserResponse;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseGuruActivity {

    private ApiService apiService;
    private String kelasSummary = "-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupNavigation();
        apiService = ApiClient.getService();

        displayLoading();
        loadProfileData();

        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_change_password).setOnClickListener(v -> showChangePasswordDialog());

        findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
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
        return R.id.nav_profile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null) {
            loadProfileData();
        }
    }

    private void displayLoading() {
        setupInfoItem(findViewById(R.id.info_nama), "Nama Lengkap", "Memuat...");
        setupInfoItem(findViewById(R.id.info_nip), "NIP", "-");
        setupInfoItem(findViewById(R.id.info_email), "Email", "-");
        setupInfoItem(findViewById(R.id.info_kelas), "Kelas Diampu", "-");
    }

    private void loadProfileData() {
        apiService.getMe().enqueue(new Callback<MeResponse>() {
            @Override
            public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess() || response.body().getData() == null) {
                    Toast.makeText(ProfileActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                    return;
                }

                bindProfile(response.body().getData());
            }

            @Override
            public void onFailure(Call<MeResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Tidak bisa memuat profil", Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getKelasSaya().enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                kelasSummary = buildKelasSummary(response.body().getData());
                setupInfoItem(findViewById(R.id.info_kelas), "Kelas Diampu", kelasSummary);

                TextView tvProfileRole = findViewById(R.id.tv_profile_role);
                if (tvProfileRole != null) tvProfileRole.setText("Guru | " + kelasSummary);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {}
        });
    }

    private void bindProfile(UserResponse user) {
        TextView tvProfileName = findViewById(R.id.tv_profile_name);
        TextView tvProfileRole = findViewById(R.id.tv_profile_role);
        TextView tvInitials = findViewById(R.id.tv_profile_initials);

        if (tvProfileName != null) tvProfileName.setText(valueOrDash(user.getNama()));
        if (tvProfileRole != null) tvProfileRole.setText("Guru | " + kelasSummary);
        AvatarUtils.applyInitialAvatar(tvInitials, user.getNama(), user.getEmail());

        setupInfoItem(findViewById(R.id.info_nama), "Nama Lengkap", valueOrDash(user.getNama()));
        setupInfoItem(findViewById(R.id.info_nip), "NIP", valueOrDash(user.getNip()));
        setupInfoItem(findViewById(R.id.info_email), "Email", valueOrDash(user.getEmail()));
        setupInfoItem(findViewById(R.id.info_kelas), "Kelas Diampu", kelasSummary);
    }

    private void showChangePasswordDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 8, padding, 0);

        EditText etOldPassword = createPasswordInput("Password lama");
        EditText etNewPassword = createPasswordInput("Password baru");
        EditText etConfirmPassword = createPasswordInput("Konfirmasi password baru");
        container.addView(etOldPassword);
        container.addView(etNewPassword);
        container.addView(etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ubah Kata Sandi")
                .setView(container)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            changePassword(oldPassword, newPassword, dialog);
        }));

        dialog.show();
    }

    private EditText createPasswordInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setPadding(0, 12, 0, 12);
        return input;
    }

    private void changePassword(String oldPassword, String newPassword, AlertDialog dialog) {
        Map<String, String> body = new HashMap<>();
        body.put("password_lama", oldPassword);
        body.put("password_baru", newPassword);

        apiService.changePassword(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ProfileActivity.this, "Gagal mengubah kata sandi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildKelasSummary(List<KelasItem> kelasList) {
        if (kelasList == null || kelasList.isEmpty()) return "-";

        StringBuilder builder = new StringBuilder();
        for (KelasItem kelas : kelasList) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(kelas.getNamaKelas());
        }
        return builder.toString();
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

}
