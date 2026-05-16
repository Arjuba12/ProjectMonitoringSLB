package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.*;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputPerkembanganActivity extends BaseGuruActivity {

    private Spinner spinnerSiswa;
    private EditText etCatatan;
    private TextView tvTanggal;
    private Button btnSimpan;
    private LinearLayout containerAspekInput;
    private TableLayout tableRiwayat;

    private ApiService apiService;
    private List<SiswaItem> siswaList = new ArrayList<>();
    private List<AspekItem> aspekList = new ArrayList<>();
    private Map<Integer, EditText> capaianInputs = new HashMap<>();
    private List<PerkembanganItem> riwayatTerakhir = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_perkembangan);

        setupNavigation();
        apiService = ApiClient.getService();

        spinnerSiswa = findViewById(R.id.spinnerSiswa);
        etCatatan    = findViewById(R.id.etCatatan);
        tvTanggal    = findViewById(R.id.tvTanggal);
        btnSimpan    = findViewById(R.id.btnSimpan);
        containerAspekInput = findViewById(R.id.container_aspek_input);
        tableRiwayat = findViewById(R.id.table_riwayat_perkembangan);

        tvTanggal.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        renderEmptyHistory("Pilih siswa untuk melihat riwayat.");

        loadSiswa();
        loadAspek();

        spinnerSiswa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0 || position > siswaList.size()) {
                    clearCapaianInputs();
                    renderEmptyHistory("Pilih siswa untuk melihat riwayat.");
                    return;
                }

                loadRiwayatSiswa(siswaList.get(position - 1).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                renderEmptyHistory("Pilih siswa untuk melihat riwayat.");
            }
        });

        btnSimpan.setOnClickListener(v -> simpanData());
    }

    private void loadSiswa() {
        apiService.getSiswa(null, 1, null).enqueue(new Callback<SiswaListResponse>() {
            @Override
            public void onResponse(Call<SiswaListResponse> call, Response<SiswaListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                siswaList = response.body().getData();

                List<String> namaList = new ArrayList<>();
                namaList.add("-- Pilih Siswa --");
                for (SiswaItem s : siswaList) namaList.add(s.getNama());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    InputPerkembanganActivity.this,
                    android.R.layout.simple_spinner_item, namaList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSiswa.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<SiswaListResponse> call, Throwable t) {
                Toast.makeText(InputPerkembanganActivity.this,
                    "Gagal memuat daftar siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAspek() {
        apiService.getAspek().enqueue(new Callback<AspekListResponse>() {
            @Override
            public void onResponse(Call<AspekListResponse> call, Response<AspekListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                aspekList = response.body().getData();
                renderAspekInputs();
            }

            @Override
            public void onFailure(Call<AspekListResponse> call, Throwable t) {
                Toast.makeText(InputPerkembanganActivity.this,
                    "Gagal memuat aspek perkembangan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderAspekInputs() {
        if (containerAspekInput == null) return;

        containerAspekInput.removeAllViews();
        capaianInputs.clear();

        for (AspekItem aspek : aspekList) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dpToPx(6), 0, dpToPx(6));

            TextView label = new TextView(this);
            label.setText(aspek.getNama());
            label.setTextColor(Color.parseColor("#2C3E50"));
            label.setTextSize(12);
            label.setTypeface(null, Typeface.BOLD);
            row.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("0-100");
            input.setTextSize(12);
            input.setGravity(Gravity.CENTER);
            input.setSingleLine(true);
            input.setBackgroundResource(R.drawable.bg_rounded_gray);
            input.setPadding(dpToPx(8), 0, dpToPx(8), 0);
            row.addView(input, new LinearLayout.LayoutParams(dpToPx(96), dpToPx(44)));

            containerAspekInput.addView(row);
            capaianInputs.put(aspek.getId(), input);
        }

        populateTodayInputs(riwayatTerakhir);
    }

    private void loadRiwayatSiswa(int siswaId) {
        renderEmptyHistory("Memuat riwayat...");
        apiService.getPerkembanganSiswa(siswaId, null).enqueue(new Callback<PerkembanganListResponse>() {
            @Override
            public void onResponse(Call<PerkembanganListResponse> call, Response<PerkembanganListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderEmptyHistory("Riwayat belum bisa dimuat.");
                    return;
                }

                renderRiwayat(response.body().getData());
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {
                renderEmptyHistory("Tidak bisa terhubung ke server.");
            }
        });
    }

    private void renderRiwayat(List<PerkembanganItem> data) {
        clearHistoryRows();
        riwayatTerakhir = data != null ? data : new ArrayList<>();
        if (data == null || data.isEmpty()) {
            clearCapaianInputs();
            addHistoryRow("-", "Belum ada data", "-", Color.parseColor("#7F8C8D"));
            return;
        }

        int limit = Math.min(data.size(), 10);
        for (int i = 0; i < limit; i++) {
            PerkembanganItem item = data.get(i);
            addHistoryRow(
                    formatTanggal(item.getTanggal()),
                    safeText(item.getAspekNama()),
                    item.getCapaian() + "%",
                    getCapaianColor(item.getCapaian())
            );
        }
        populateTodayInputs(data);
    }

    private void renderEmptyHistory(String message) {
        riwayatTerakhir = new ArrayList<>();
        clearHistoryRows();
        addHistoryRow("-", message, "-", Color.parseColor("#7F8C8D"));
    }

    private void clearHistoryRows() {
        if (tableRiwayat == null) return;
        while (tableRiwayat.getChildCount() > 1) {
            tableRiwayat.removeViewAt(1);
        }
    }

    private void addHistoryRow(String tanggal, String aspek, String nilai, int nilaiColor) {
        if (tableRiwayat == null) return;

        TableRow row = new TableRow(this);
        row.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
        row.addView(createCell(tanggal, false, Gravity.START, Color.parseColor("#333333")));
        row.addView(createCell(aspek, false, Gravity.CENTER, Color.parseColor("#7F8C8D")));
        row.addView(createCell(nilai, true, Gravity.CENTER, nilaiColor));
        tableRiwayat.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F2F4F7"));
        tableRiwayat.addView(divider, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
        ));
    }

    private TextView createCell(String text, boolean bold, int gravity, int color) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setTextColor(color);
        cell.setTextSize(12);
        cell.setGravity(gravity);
        cell.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        cell.setMinWidth(dpToPx(120));
        if (bold) cell.setTypeface(null, Typeface.BOLD);
        return cell;
    }

    private String formatTanggal(String tanggal) {
        if (tanggal == null || tanggal.trim().isEmpty()) return "-";
        return DateTimeUtils.formatDate(tanggal);
    }

    private String safeText(String value) {
        if (value == null || value.trim().isEmpty()) return "-";
        return value.trim();
    }

    private void populateTodayInputs(List<PerkembanganItem> data) {
        clearCapaianInputs();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (data == null) return;

        for (PerkembanganItem item : data) {
            if (!today.equals(DateTimeUtils.dateKey(item.getTanggal()))) continue;

            EditText input = capaianInputs.get(item.getAspekId());
            if (input != null) {
                input.setText(String.valueOf(item.getCapaian()));
            }
        }
    }

    private void clearCapaianInputs() {
        for (EditText input : capaianInputs.values()) {
            input.setText("");
        }
    }

    private int getCapaianColor(int capaian) {
        if (capaian >= 80) return Color.parseColor("#2E7D32");
        if (capaian >= 60) return Color.parseColor("#F57C00");
        return Color.parseColor("#C62828");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void simpanData() {
        int siswaPos = spinnerSiswa.getSelectedItemPosition();

        if (siswaPos == 0) {
            Toast.makeText(this, "Pilih siswa terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (aspekList.isEmpty() || capaianInputs.isEmpty()) {
            Toast.makeText(this, "Aspek perkembangan belum siap", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> aspekPayload = new ArrayList<>();
        String catatan = etCatatan.getText().toString().trim();

        for (AspekItem aspek : aspekList) {
            EditText input = capaianInputs.get(aspek.getId());
            String capaianStr = input != null ? input.getText().toString().trim() : "";

            if (capaianStr.isEmpty()) {
                Toast.makeText(this, "Lengkapi nilai semua aspek", Toast.LENGTH_SHORT).show();
                return;
            }

            int capaian;
            try {
                capaian = Integer.parseInt(capaianStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Capaian harus berupa angka", Toast.LENGTH_SHORT).show();
                return;
            }

            if (capaian < 0 || capaian > 100) {
                Toast.makeText(this, "Capaian harus antara 0-100", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("aspek_id", aspek.getId());
            item.put("capaian", capaian);
            item.put("catatan", catatan);
            aspekPayload.add(item);
        }

        if (aspekPayload.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        SiswaItem siswa = siswaList.get(siswaPos - 1);
        String tanggal  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> body = new HashMap<>();
        body.put("siswa_id", siswa.getId());
        body.put("tanggal", tanggal);
        body.put("aspek_list", aspekPayload);

        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        apiService.inputPerkembanganBatch(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(InputPerkembanganActivity.this,
                        "Perkembangan " + siswa.getNama() + " berhasil disimpan!",
                        Toast.LENGTH_LONG).show();
                    etCatatan.setText("");
                    loadRiwayatSiswa(siswa.getId());
                } else {
                    Toast.makeText(InputPerkembanganActivity.this,
                        "Gagal menyimpan, coba lagi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                Toast.makeText(InputPerkembanganActivity.this,
                    "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return R.id.nav_input_perkembangan; }
    @Override protected int getSelfBottomNavItemId() { return -1; }
}
