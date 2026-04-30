package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;

public class KirimPengumumanActivity extends BaseGuruActivity {

    private EditText etJudul, etPengumuman;
    private Button btnKirim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_pengumuman);

        // Inisialisasi Navigasi
        setupBottomNav(R.id.nav_announcement);
        DrawerLayout dl = findViewById(R.id.drawer_layout);
        if (dl != null) {
            setupNavigationDrawer(dl);
        }

        // Inisialisasi View
        etJudul = findViewById(R.id.etJudul);
        etPengumuman = findViewById(R.id.etPengumuman);
        btnKirim = findViewById(R.id.btnKirim);

        if (btnKirim != null) {
            btnKirim.setOnClickListener(v -> {
                kirimPengumuman();
            });
        }
    }

    private void kirimPengumuman() {
        if (etJudul == null || etPengumuman == null) return;

        String judul = etJudul.getText().toString().trim();
        String pesan = etPengumuman.getText().toString().trim();

        if (judul.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pesan.isEmpty()) {
            Toast.makeText(this, "Isi pengumuman tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic Dummy: Simulasi kirim ke Firebase
        Toast.makeText(this, "Pengumuman '" + judul + "' berhasil dikirim!", Toast.LENGTH_LONG).show();
        etJudul.setText("");
        etPengumuman.setText("");
    }
}