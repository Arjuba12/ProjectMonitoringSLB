package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaItem;
import com.example.monitoringappslb.model.response.ApiModels.SiswaListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuatPpiActivity extends BaseGuruActivity {

    private Spinner spinnerSiswa;
    private EditText etPotensi, etHambatan, etTargetAkademik, etTargetMotorik, etTargetKomunikasi, etTargetBinaDiri;
    private Button btnSimpan, btnBatal;
    private ApiService apiService;
    private List<SiswaItem> siswaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_ppi);

        apiService = ApiClient.getService();

        spinnerSiswa = findViewById(R.id.spinnerSiswa);
        etPotensi = findViewById(R.id.et_potensi);
        etHambatan = findViewById(R.id.et_hambatan);
        etTargetAkademik = findViewById(R.id.et_target_akademik);
        etTargetMotorik = findViewById(R.id.et_target_motorik);
        etTargetKomunikasi = findViewById(R.id.et_target_komunikasi);
        etTargetBinaDiri = findViewById(R.id.et_target_bina_diri);
        btnSimpan = findViewById(R.id.btn_simpan_ppi);
        btnBatal = findViewById(R.id.btn_batal_ppi);

        loadSiswa();

        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> simpanPpi());
        }

        if (btnBatal != null) {
            btnBatal.setOnClickListener(v -> finish());
        }
    }

    private void loadSiswa() {
        apiService.getSiswa(null, null, null).enqueue(new Callback<SiswaListResponse>() {
            @Override
            public void onResponse(Call<SiswaListResponse> call, Response<SiswaListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    siswaList = response.body().getData();
                    List<String> namaSiswa = new ArrayList<>();
                    namaSiswa.add("-- Pilih Siswa --");
                    for (SiswaItem s : siswaList) {
                        namaSiswa.add(s.getNama());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(BuatPpiActivity.this, android.R.layout.simple_spinner_item, namaSiswa);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSiswa.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<SiswaListResponse> call, Throwable t) {
                Toast.makeText(BuatPpiActivity.this, "Gagal memuat data siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void simpanPpi() {
        int selectedPos = spinnerSiswa.getSelectedItemPosition();
        if (selectedPos <= 0) {
            Toast.makeText(this, "Pilih siswa terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        SiswaItem selectedSiswa = siswaList.get(selectedPos - 1);

        Map<String, Object> params = new HashMap<>();
        params.put("siswa_id", selectedSiswa.getId());
        params.put("semester", "1");
        params.put("tahun_ajaran", selectedSiswa.getTahunAjaran() != null
                ? selectedSiswa.getTahunAjaran()
                : "2024/2025");
        params.put("potensi", etPotensi.getText().toString());
        params.put("hambatan", etHambatan.getText().toString());
        params.put("target_akademik", etTargetAkademik.getText().toString());
        params.put("target_motorik", etTargetMotorik.getText().toString());
        params.put("target_komunikasi", etTargetKomunikasi.getText().toString());
        params.put("target_bina_diri", etTargetBinaDiri.getText().toString());

        apiService.buatPpi(params).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BuatPpiActivity.this, "Program Pembelajaran Individual berhasil disimpan", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(BuatPpiActivity.this, "Gagal menyimpan Program Pembelajaran Individual", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(BuatPpiActivity.this, "Kesalahan jaringan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return null; // Not using drawer here or provide one if layout has it
    }

    @Override
    protected NavigationView getNavigationView() {
        return null;
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return null;
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return -1;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }
}
