package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.monitoringappslb.R;

public class BuatPpiActivity extends BaseGuruActivity {

    private Spinner spinnerSiswa;
    private Button btnSimpan, btnBatal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_ppi);

        spinnerSiswa = findViewById(R.id.spinnerSiswa);
        btnSimpan = findViewById(R.id.btn_simpan_ppi);
        btnBatal = findViewById(R.id.btn_batal_ppi);

        // Setup Spinner Dummy Data
        String[] siswa = {"Pilih Siswa", "Andi Rizky", "Gita Permata", "Budi Santoso"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, siswa);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerSiswa != null) {
            spinnerSiswa.setAdapter(adapter);
        }

        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> {
                Toast.makeText(this, "PPI Berhasil Disimpan", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> finish());
        }
    }
}