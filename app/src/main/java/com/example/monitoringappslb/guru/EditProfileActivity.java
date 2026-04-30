package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import com.example.monitoringappslb.R;

public class EditProfileActivity extends BaseGuruActivity {

    private EditText etNama, etNip, etEmail, etKelas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Setup Navigation
        setupBottomNav(-1);
        setupNavigationDrawer(findViewById(R.id.drawer_layout));

        initViews();
        loadCurrentData();

        findViewById(R.id.btn_simpan).setOnClickListener(v -> {
            saveData();
        });
    }

    private void initViews() {
        etNama = findViewById(R.id.et_nama);
        etNip = findViewById(R.id.et_nip);
        etEmail = findViewById(R.id.et_email);
        etKelas = findViewById(R.id.et_kelas);
    }

    private void loadCurrentData() {
        // Dummy data loading
        etNama.setText("Arjuna Bimantara, S.Pd.");
        etNip.setText("19950821 202012 1 001");
        etEmail.setText("arjuna.b@sekolah.id");
        etKelas.setText("Kelas VII-A (Tunanetra)");
    }

    private void saveData() {
        String nama = etNama.getText().toString();
        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic simulasi simpan
        Toast.makeText(this, "Perubahan profil berhasil disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
}