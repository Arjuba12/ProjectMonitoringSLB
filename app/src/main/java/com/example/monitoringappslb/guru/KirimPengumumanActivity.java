package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
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

public class KirimPengumumanActivity extends BaseGuruActivity {

    private EditText etJudul, etPengumuman;
    private Spinner spinnerPenerima;
    private Button btnKirim;
    private ApiService apiService;
    private final List<KelasItem> kelasList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_pengumuman);

        setupNavigation();
        apiService = ApiClient.getService();

        etJudul = findViewById(R.id.etJudul);
        etPengumuman = findViewById(R.id.etPengumuman);
        spinnerPenerima = findViewById(R.id.spinnerPenerima);
        btnKirim = findViewById(R.id.btnKirim);

        loadKelasTujuan();

        if (btnKirim != null) {
            btnKirim.setOnClickListener(v -> {
                kirimPengumuman();
            });
        }
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
        return R.id.nav_announcement;
    }

    private void loadKelasTujuan() {
        if (btnKirim != null) btnKirim.setEnabled(false);

        apiService.getKelasSaya().enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null || response.body().getData().isEmpty()) {
                    Toast.makeText(KirimPengumumanActivity.this,
                            "Tidak ada kelas yang dapat dikirim pengumuman", Toast.LENGTH_SHORT).show();
                    return;
                }

                kelasList.clear();
                kelasList.addAll(response.body().getData());

                List<String> labels = new ArrayList<>();
                for (KelasItem kelas : kelasList) {
                    labels.add("Wali murid " + kelas.getNamaKelas());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        KirimPengumumanActivity.this,
                        android.R.layout.simple_spinner_item,
                        labels
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPenerima.setAdapter(adapter);

                if (btnKirim != null) btnKirim.setEnabled(true);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                Toast.makeText(KirimPengumumanActivity.this,
                        "Gagal memuat kelas tujuan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void kirimPengumuman() {
        if (etJudul == null || etPengumuman == null) return;

        int selectedPosition = spinnerPenerima != null ? spinnerPenerima.getSelectedItemPosition() : -1;
        String judul = etJudul.getText().toString().trim();
        String pesan = etPengumuman.getText().toString().trim();

        if (selectedPosition < 0 || selectedPosition >= kelasList.size()) {
            Toast.makeText(this, "Pilih kelas tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (judul.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pesan.isEmpty()) {
            Toast.makeText(this, "Isi pengumuman tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        KelasItem kelas = kelasList.get(selectedPosition);
        Map<String, Object> body = new HashMap<>();
        body.put("judul", judul);
        body.put("isi", pesan);
        body.put("target_role", "wali");
        body.put("kelas_id", kelas.getId());
        body.put("status", "Terkirim");

        btnKirim.setEnabled(false);
        btnKirim.setText("Mengirim...");

        apiService.kirimPengumuman(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                btnKirim.setEnabled(true);
                btnKirim.setText("Kirim Pengumuman");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(KirimPengumumanActivity.this,
                            "Pengumuman berhasil dikirim ke wali murid " + kelas.getNamaKelas(),
                            Toast.LENGTH_LONG).show();
                    etJudul.setText("");
                    etPengumuman.setText("");
                } else {
                    Toast.makeText(KirimPengumumanActivity.this,
                            "Gagal mengirim pengumuman", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                btnKirim.setEnabled(true);
                btnKirim.setText("Kirim Pengumuman");
                Toast.makeText(KirimPengumumanActivity.this,
                        "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
