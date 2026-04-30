package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.monitoringappslb.R;

public class ProfileActivity extends BaseGuruActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup Navigation
        setupBottomNav(R.id.nav_profile);
        setupNavigationDrawer(findViewById(R.id.drawer_layout));

        // Load Data
        displayProfileData();

        // Buttons
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_change_password).setOnClickListener(v ->
            Toast.makeText(this, "Fitur Ubah Kata Sandi akan segera hadir", Toast.LENGTH_SHORT).show()
        );
    }

    private void displayProfileData() {
        // Find Views from updated layout
        TextView tvProfileName = findViewById(R.id.tv_profile_name);
        TextView tvProfileRole = findViewById(R.id.tv_profile_role);

        if (tvProfileName != null) tvProfileName.setText("Arjuna Bimantara, S.Pd.");
        if (tvProfileRole != null) tvProfileRole.setText("Guru Wali Kelas VII-A");

        setupInfoItem(findViewById(R.id.info_nama), "Nama Lengkap", "Arjuna Bimantara, S.Pd.");
        setupInfoItem(findViewById(R.id.info_nip), "NIP", "19950821 202012 1 001");
        setupInfoItem(findViewById(R.id.info_email), "Email", "arjuna.b@sekolah.id");
        setupInfoItem(findViewById(R.id.info_kelas), "Wali Kelas", "Kelas VII-A (Tunanetra)");
    }

    private void setupInfoItem(View view, String label, String value) {
        TextView tvLabel = view.findViewById(R.id.tv_label);
        TextView tvValue = view.findViewById(R.id.tv_value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
    }
}