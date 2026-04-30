package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Siswa;

public class DetailSiswaActivity extends BaseGuruActivity {

    private TextView tvNamaProfil, tvKelasNisn, tvTempatLahir, tvTanggalLahir, tvJenisKelamin, tvAlamat, tvNamaAyah, tvNamaIbu, 
            tvStatsKehadiran, tvStatsKognitif, tvStatsSosial, tvStatsMotorik, tvStatsKomunikasi, tvStatsBinaDiri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_siswa);

        initViews();

        String siswaId = getIntent().getStringExtra("SISWA_ID");
        if (siswaId != null) {
            loadSiswaData(siswaId);
        }

        setupButtons();
        setupBottomNav(-1);
        setupNavigationDrawer(findViewById(R.id.drawer_layout));
    }

    private void setupButtons() {
        findViewById(R.id.btn_lihat_ppi).setOnClickListener(v -> {
            String siswaId = getIntent().getStringExtra("SISWA_ID");
            if (siswaId != null) {
                Siswa s = DummyDatabase.getSiswaById(siswaId);
                Intent intent = new Intent(this, DetailPpiActivity.class);
                intent.putExtra("STUDENT_NAME", s.getNama());
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        tvNamaProfil = findViewById(R.id.tv_nama_profil);
        tvKelasNisn = findViewById(R.id.tv_kelas_nisn);
        tvTempatLahir = findViewById(R.id.tv_tempat_lahir);
        tvTanggalLahir = findViewById(R.id.tv_tanggal_lahir);
        tvJenisKelamin = findViewById(R.id.tv_jenis_kelamin);
        tvAlamat = findViewById(R.id.tv_alamat);
        tvNamaAyah = findViewById(R.id.tv_nama_ayah);
        tvNamaIbu = findViewById(R.id.tv_nama_ibu);
        tvStatsKehadiran = findViewById(R.id.tv_stats_kehadiran);
        tvStatsKognitif = findViewById(R.id.tv_stats_kognitif);
        tvStatsSosial = findViewById(R.id.tv_stats_sosial);
        tvStatsMotorik = findViewById(R.id.tv_stats_motorik);
        tvStatsKomunikasi = findViewById(R.id.tv_stats_komunikasi);
        tvStatsBinaDiri = findViewById(R.id.tv_stats_binadiri);
    }

    private void loadSiswaData(String id) {
        Siswa siswa = DummyDatabase.getSiswaById(id);
        if (siswa != null) {
            tvNamaProfil.setText(siswa.getNama());
            tvKelasNisn.setText(siswa.getKelas() + " | NISN: " + siswa.getNisn());
            tvTempatLahir.setText(siswa.getTempatLahir());
            tvTanggalLahir.setText(siswa.getTanggalLahir());
            tvJenisKelamin.setText(siswa.getJenisKelamin());
            tvAlamat.setText(siswa.getAlamat());
            tvNamaAyah.setText(siswa.getNamaAyah());
            tvNamaIbu.setText(siswa.getNamaIbu());
            tvStatsKehadiran.setText(siswa.getStatsKehadiran());
            tvStatsKognitif.setText(siswa.getStatsKognitif());
            tvStatsSosial.setText(siswa.getStatsSosial());
            tvStatsMotorik.setText(siswa.getStatsMotorik());
            tvStatsKomunikasi.setText(siswa.getStatsKomunikasi());
            tvStatsBinaDiri.setText(siswa.getStatsBinaDiri());
        }
    }
}