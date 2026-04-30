package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;

import com.example.monitoringappslb.data.DummyDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class InputPerkembanganActivity extends BaseGuruActivity {

    private Spinner spinnerSiswa, spinnerAspek;
    private EditText etCapaian, etCatatan;
    private TextView tvTanggal;
    private Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_perkembangan);

        // Inisialisasi Navigasi dari BaseGuruActivity
        DrawerLayout dl = findViewById(R.id.drawer_layout);
        if (dl != null) {
            setupNavigationDrawer(dl);
        }
        setupBottomNav(-1);

        // Inisialisasi View
        initViews();

        // Set Tanggal Hari Ini
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvTanggal.setText(currentDate);

        // Load Data Dummy ke Spinner
        setupSpinners();

        // Handle Klik Tombol Simpan
        btnSimpan.setOnClickListener(v -> {
            simpanData();
        });
    }

    private void initViews() {
        spinnerSiswa = findViewById(R.id.spinnerSiswa);
        spinnerAspek = findViewById(R.id.spinnerAspek);
        etCapaian = findViewById(R.id.etCapaian);
        etCatatan = findViewById(R.id.etCatatan);
        tvTanggal = findViewById(R.id.tvTanggal);
        btnSimpan = findViewById(R.id.btnSimpan);
    }

    private void setupSpinners() {
        String[] listSiswa = DummyDatabase.getNamaSiswaArray();

        ArrayAdapter<String> adapterSiswa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listSiswa);
        adapterSiswa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSiswa.setAdapter(adapterSiswa);

        ArrayList<String> listAspek = new ArrayList<>();
        listAspek.add("-- Pilih Aspek --");
        listAspek.add("Kognitif");
        listAspek.add("Motorik Halus");
        listAspek.add("Motorik Kasar");
        listAspek.add("Bina Diri");
        listAspek.add("Sosial Emosional");

        ArrayAdapter<String> adapterAspek = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listAspek);
        adapterAspek.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAspek.setAdapter(adapterAspek);
    }

    private void simpanData() {
        String siswa = spinnerSiswa.getSelectedItem().toString();
        String aspek = spinnerAspek.getSelectedItem().toString();
        String capaian = etCapaian.getText().toString();

        if (siswa.equals("-- Pilih Siswa --") || aspek.equals("-- Pilih Aspek --") || capaian.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Berhasil menyimpan perkembangan " + siswa, Toast.LENGTH_LONG).show();

        etCapaian.setText("");
        etCatatan.setText("");
        spinnerSiswa.setSelection(0);
        spinnerAspek.setSelection(0);
    }
}