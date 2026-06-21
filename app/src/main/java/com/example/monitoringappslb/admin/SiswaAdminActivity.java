package com.example.monitoringappslb.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.AbsensiRekap;
import com.example.monitoringappslb.model.response.ApiModels.AdminUserItem;
import com.example.monitoringappslb.model.response.ApiModels.AdminUserListResponse;
import com.example.monitoringappslb.model.response.ApiModels.AspekCapaian;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetail;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetailResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaItem;
import com.example.monitoringappslb.model.response.ApiModels.SiswaListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SiswaAdminActivity extends BaseAdminActivity {
    private static final String DELETE_CONFIRMATION = "Hapus Data Ini";

    private ApiService apiService;
    private TextView tvStatus;
    private LinearLayout containerSiswa;
    private Button btnRefresh, btnTambah;
    private List<KelasItem> kelasList = new ArrayList<>();
    private List<AdminUserItem> waliList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siswa_admin);

        apiService = ApiClient.getService();
        tvStatus = findViewById(R.id.tv_siswa_admin_status);
        containerSiswa = findViewById(R.id.container_siswa_admin);
        btnRefresh = findViewById(R.id.btn_refresh_siswa_admin);
        btnTambah = findViewById(R.id.btn_tambah_siswa_admin);

        setupNavigation();
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> loadSiswa());
        }
        if (btnTambah != null) {
            btnTambah.setOnClickListener(v -> showSiswaDialog(null));
        }
        loadKelas();
        loadWali();
        loadSiswa();
    }

    private void loadKelas() {
        apiService.getKelas(null).enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                kelasList = response.isSuccessful() && response.body() != null && response.body().getData() != null
                        ? response.body().getData()
                        : new ArrayList<>();
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                kelasList = new ArrayList<>();
            }
        });
    }

    private void loadWali() {
        apiService.getUsers("wali", 1, null).enqueue(new Callback<AdminUserListResponse>() {
            @Override
            public void onResponse(Call<AdminUserListResponse> call, Response<AdminUserListResponse> response) {
                waliList = response.isSuccessful() && response.body() != null && response.body().getData() != null
                        ? response.body().getData()
                        : new ArrayList<>();
            }

            @Override
            public void onFailure(Call<AdminUserListResponse> call, Throwable t) {
                waliList = new ArrayList<>();
            }
        });
    }

    private void loadSiswa() {
        showStatus("Memuat data siswa...", true);
        if (containerSiswa != null) containerSiswa.removeAllViews();

        apiService.getSiswa(null, null, null).enqueue(new Callback<SiswaListResponse>() {
            @Override
            public void onResponse(Call<SiswaListResponse> call, Response<SiswaListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showStatus("Gagal memuat data siswa", true);
                    return;
                }
                renderSiswa(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaListResponse> call, Throwable t) {
                showStatus("Tidak bisa terhubung ke server", true);
                Toast.makeText(SiswaAdminActivity.this, "Gagal memuat siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSiswa(List<SiswaItem> data) {
        if (containerSiswa == null) return;
        containerSiswa.removeAllViews();

        if (data == null || data.isEmpty()) {
            showStatus("Belum ada data siswa", true);
            return;
        }

        int aktif = 0;
        for (SiswaItem item : data) {
            if (!isInactive(item.isAktif())) aktif++;
            containerSiswa.addView(createSiswaCard(item));
            View line = new View(this);
            line.setBackgroundColor(Color.parseColor("#F1F5F9"));
            containerSiswa.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        }
        showStatus(data.size() + " siswa | " + aktif + " aktif | " + (data.size() - aktif) + " nonaktif", true);
    }

    private View createSiswaCard(SiswaItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, dp(10), 0, dp(10));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        card.setLayoutParams(params);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleGroup = new LinearLayout(this);
        titleGroup.setOrientation(LinearLayout.VERTICAL);
        titleGroup.addView(text(valueOrDash(item.getNama()), "#1E293B", 15, true));
        titleGroup.addView(text("NISN: " + valueOrDash(item.getNisn()), "#64748B", 12, false));

        TextView badge = text(isInactive(item.isAktif()) ? "Nonaktif" : "Aktif", "#FFFFFF", 11, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(4), dp(10), dp(4));
        badge.setBackgroundResource(isInactive(item.isAktif()) ? R.drawable.bg_badge : R.drawable.bg_pill_primary);

        top.addView(titleGroup, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        top.addView(badge);
        card.addView(top);

        card.addView(text("Kelas: " + valueOrDash(item.getNamaKelas()) + " | Tahun: " + valueOrDash(item.getTahunAjaran()), "#334155", 12, false));
        card.addView(text("Wali: " + valueOrDash(item.getNamaWali()), "#334155", 12, false));
        card.addView(text("Kebutuhan: " + valueOrDash(item.getKebutuhanKhusus()), "#64748B", 12, false));

        if (item.getAlphaBulanIni() > 0) {
            card.addView(text("Alpha 30 hari: " + item.getAlphaBulanIni(), "#EF4444", 12, true));
        }

        card.setOnClickListener(v -> loadDetail(item.getId()));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(0, dp(8), 0, 0);

        LinearLayout primaryActions = new LinearLayout(this);
        primaryActions.setOrientation(LinearLayout.HORIZONTAL);
        primaryActions.addView(smallButton("Detail", "#64748B", v -> loadDetail(item.getId())));
        primaryActions.addView(smallButton("Edit", "#1E293B", v -> showSiswaDialog(item)));

        LinearLayout destructiveActions = new LinearLayout(this);
        destructiveActions.setOrientation(LinearLayout.HORIZONTAL);
        destructiveActions.setPadding(0, dp(6), 0, 0);
        destructiveActions.addView(smallButton(isInactive(item.isAktif()) ? "Aktifkan" : "Nonaktifkan", "#F59E0B",
                v -> confirmToggleStatus(item)));
        destructiveActions.addView(smallButton("Hapus", "#DC2626", v -> confirmDelete(item)));

        actions.addView(primaryActions);
        actions.addView(destructiveActions);
        card.addView(actions);
        return card;
    }

    private MaterialButton smallButton(String label, String color, View.OnClickListener listener) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setTextSize(11);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(color)));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(10), 0, dp(10), 0);
        button.setOnClickListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(32)
        );
        params.setMargins(0, 0, dp(8), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void loadDetail(int siswaId) {
        apiService.getSiswaById(siswaId).enqueue(new Callback<SiswaDetailResponse>() {
            @Override
            public void onResponse(Call<SiswaDetailResponse> call, Response<SiswaDetailResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    Toast.makeText(SiswaAdminActivity.this, "Detail siswa tidak tersedia", Toast.LENGTH_SHORT).show();
                    return;
                }
                showDetailDialog(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaDetailResponse> call, Throwable t) {
                Toast.makeText(SiswaAdminActivity.this, "Tidak bisa memuat detail siswa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDetailDialog(SiswaDetail item) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(4), dp(6), dp(4), dp(2));

        addRow(content, "NISN", item.getNisn());
        addRow(content, "Kelas", item.getNamaKelas());
        addRow(content, "Tahun ajaran", item.getTahunAjaran());
        addRow(content, "Tanggal lahir", item.getTglLahir());
        addRow(content, "Jenis kelamin", item.getJenisKelamin());
        addRow(content, "Alamat", item.getAlamat());
        addRow(content, "Kebutuhan khusus", item.getKebutuhanKhusus());
        addRow(content, "Wali", item.getNamaWali());

        if (item.getAspek() != null && !item.getAspek().isEmpty()) {
            content.addView(text("Capaian bulan ini", "#1E293B", 14, true));
            for (AspekCapaian aspek : item.getAspek()) {
                String nilai = aspek.getRataRata() == null ? "-" : String.valueOf(aspek.getRataRata());
                addRow(content, valueOrDash(aspek.getNama()), nilai);
            }
        }

        if (item.getAbsensiRekap() != null && !item.getAbsensiRekap().isEmpty()) {
            content.addView(text("Presensi bulan ini", "#1E293B", 14, true));
            for (AbsensiRekap absensi : item.getAbsensiRekap()) {
                addRow(content, valueOrDash(absensi.getStatus()), String.valueOf(absensi.getJumlah()));
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(valueOrDash(item.getNama()))
                .setView(content)
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void showSiswaDialog(SiswaItem item) {
        boolean createMode = item == null;
        LinearLayout form = buildSiswaForm(item);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(createMode ? "Tambah Siswa" : "Edit Siswa")
                .setView(form)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readSiswaForm(form, createMode, item);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            if (createMode) {
                createSiswa(body, dialog);
            } else {
                updateSiswa(item.getId(), body, dialog);
            }
        }));

        dialog.show();
    }

    private LinearLayout buildSiswaForm(SiswaItem item) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(4), dp(4), dp(4), dp(2));

        EditText etNisn = input("NISN", InputType.TYPE_CLASS_TEXT);
        etNisn.setTag("nisn");
        EditText etNama = input("Nama lengkap", InputType.TYPE_CLASS_TEXT);
        etNama.setTag("nama");
        EditText etTanggal = input("yyyy-MM-dd", InputType.TYPE_CLASS_DATETIME);
        etTanggal.setTag("tgl_lahir");
        Spinner spGender = spinner(new String[]{"Laki-laki", "Perempuan"}, 0);
        spGender.setTag("jenis_kelamin");
        EditText etAlamat = input("Alamat", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etAlamat.setTag("alamat");
        etAlamat.setSingleLine(false);
        etAlamat.setMinLines(2);
        EditText etKebutuhan = input("Kebutuhan khusus", InputType.TYPE_CLASS_TEXT);
        etKebutuhan.setTag("kebutuhan_khusus");
        EditText etTahunMasuk = input("Tahun masuk", InputType.TYPE_CLASS_NUMBER);
        etTahunMasuk.setTag("tahun_masuk");
        Spinner spKelas = spinner(kelasLabels(), 0);
        spKelas.setTag("kelas_id");
        Spinner spWali = spinner(waliLabels(), 0);
        spWali.setTag("wali_user_id");
        Spinner spStatus = spinner(new String[]{"Aktif", "Nonaktif"}, 0);
        spStatus.setTag("is_aktif");

        if (item != null) {
            etNisn.setText(valueOrBlank(item.getNisn()));
            etNama.setText(valueOrBlank(item.getNama()));
            etTanggal.setText(normalizeDate(item.getTglLahir()));
            spGender.setSelection("P".equals(item.getJenisKelamin()) || "Perempuan".equalsIgnoreCase(item.getJenisKelamin()) ? 1 : 0);
            etAlamat.setText(valueOrBlank(item.getAlamat()));
            etKebutuhan.setText(valueOrBlank(item.getKebutuhanKhusus()));
            etTahunMasuk.setText(item.getTahunMasuk() == null ? "" : String.valueOf(item.getTahunMasuk()));
            selectKelas(spKelas, item.getKelasId());
            spStatus.setSelection(isInactive(item.isAktif()) ? 1 : 0);
        }

        addField(form, "NISN *", etNisn);
        addField(form, "Nama *", etNama);
        addField(form, "Tanggal Lahir *", etTanggal);
        addField(form, "Jenis Kelamin *", spGender);
        addField(form, "Kelas", spKelas);
        addField(form, "Wali", spWali);
        addField(form, "Tahun Masuk", etTahunMasuk);
        addField(form, "Kebutuhan Khusus", etKebutuhan);
        addField(form, "Alamat", etAlamat);
        if (item != null) addField(form, "Status", spStatus);
        return form;
    }

    private Map<String, Object> readSiswaForm(LinearLayout form, boolean createMode, SiswaItem currentItem) {
        String nisn = taggedText(form, "nisn");
        String nama = taggedText(form, "nama");
        String tanggal = taggedText(form, "tgl_lahir");
        String alamat = taggedText(form, "alamat");
        String kebutuhan = taggedText(form, "kebutuhan_khusus");
        String tahunMasuk = taggedText(form, "tahun_masuk");

        if (nisn.isEmpty() || nama.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "NISN, nama, dan tanggal lahir wajib diisi", Toast.LENGTH_SHORT).show();
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nisn", nisn);
        body.put("nama", nama);
        body.put("tgl_lahir", tanggal);
        body.put("jenis_kelamin", ((Spinner) form.findViewWithTag("jenis_kelamin")).getSelectedItemPosition() == 1 ? "P" : "L");
        body.put("alamat", alamat.isEmpty() ? null : alamat);
        body.put("kebutuhan_khusus", kebutuhan.isEmpty() ? null : kebutuhan);
        body.put("kelas_id", selectedKelasId((Spinner) form.findViewWithTag("kelas_id")));
        body.put("wali_user_id", selectedWaliId((Spinner) form.findViewWithTag("wali_user_id")));
        body.put("hubungan", "Wali");
        body.put("tahun_masuk", tahunMasuk.isEmpty() ? null : tahunMasuk);
        if (!createMode) {
            Spinner spStatus = (Spinner) form.findViewWithTag("is_aktif");
            body.put("is_aktif", spStatus != null && spStatus.getSelectedItemPosition() == 1 ? 0 : 1);
        } else if (currentItem == null) {
            body.put("is_aktif", 1);
        }
        return body;
    }

    private void createSiswa(Map<String, Object> body, AlertDialog dialog) {
        apiService.createSiswa(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SiswaAdminActivity.this, "Siswa berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadSiswa();
                } else {
                    Toast.makeText(SiswaAdminActivity.this, "Gagal menambah siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(SiswaAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSiswa(int id, Map<String, Object> body, AlertDialog dialog) {
        apiService.updateSiswa(id, body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SiswaAdminActivity.this, "Siswa berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadSiswa();
                } else {
                    Toast.makeText(SiswaAdminActivity.this, "Gagal memperbarui siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(SiswaAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(SiswaItem item) {
        showPermanentDeleteDialog(
                "Hapus siswa permanen?",
                valueOrDash(item.getNama()) + " beserta data presensi, perkembangan, dan Program Pembelajaran Individual akan dihapus permanen.",
                () -> deleteSiswa(item.getId())
        );
    }

    private void confirmToggleStatus(SiswaItem item) {
        boolean inactive = isInactive(item.isAktif());
        new AlertDialog.Builder(this)
                .setTitle(inactive ? "Aktifkan siswa?" : "Nonaktifkan siswa?")
                .setMessage(valueOrDash(item.getNama()))
                .setNegativeButton("Batal", null)
                .setPositiveButton(inactive ? "Aktifkan" : "Nonaktifkan",
                        (dialog, which) -> updateSiswaStatus(item, inactive ? 1 : 0))
                .show();
    }

    private void updateSiswaStatus(SiswaItem item, int status) {
        Map<String, Object> body = new HashMap<>();
        body.put("nisn", item.getNisn());
        body.put("nama", item.getNama());
        body.put("tgl_lahir", normalizeDate(item.getTglLahir()));
        body.put("jenis_kelamin", item.getJenisKelamin());
        body.put("alamat", item.getAlamat());
        body.put("kebutuhan_khusus", item.getKebutuhanKhusus());
        body.put("kelas_id", item.getKelasId());
        body.put("tahun_masuk", item.getTahunMasuk());
        body.put("is_aktif", status);
        updateSiswaWithoutDialog(item.getId(), body, status == 1 ? "Siswa diaktifkan" : "Siswa dinonaktifkan");
    }

    private void showPermanentDeleteDialog(String title, String warning, Runnable onConfirmed) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), 0);
        content.addView(text(warning, "#B91C1C", 13, false));
        content.addView(text("Ketik \"" + DELETE_CONFIRMATION + "\" untuk melanjutkan.", "#334155", 12, true));

        EditText confirmation = input(DELETE_CONFIRMATION, InputType.TYPE_CLASS_TEXT);
        content.addView(confirmation);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(content)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Hapus Permanen", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#DC2626"));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!DELETE_CONFIRMATION.equals(confirmation.getText().toString())) return;
                dialog.dismiss();
                onConfirmed.run();
            });
        });
        confirmation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (dialog.isShowing()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(DELETE_CONFIRMATION.contentEquals(s));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        dialog.show();
    }

    private void deleteSiswa(int id) {
        apiService.deleteSiswa(id).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SiswaAdminActivity.this, "Siswa dihapus permanen", Toast.LENGTH_SHORT).show();
                    loadSiswa();
                } else {
                    Toast.makeText(SiswaAdminActivity.this, "Gagal menghapus siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(SiswaAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSiswaWithoutDialog(int id, Map<String, Object> body, String message) {
        apiService.updateSiswa(id, body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SiswaAdminActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadSiswa();
                } else {
                    Toast.makeText(SiswaAdminActivity.this, "Gagal mengubah status siswa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(SiswaAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addField(LinearLayout parent, String label, View input) {
        parent.addView(text(label, "#334155", 12, true));
        parent.addView(input);
    }

    private EditText input(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setTextColor(Color.parseColor("#0F172A"));
        input.setHintTextColor(Color.parseColor("#94A3B8"));
        input.setPadding(0, dp(8), 0, dp(8));
        return input;
    }

    private Spinner spinner(String[] values, int selectedIndex) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (selectedIndex >= 0 && selectedIndex < values.length) spinner.setSelection(selectedIndex);
        return spinner;
    }

    private String[] kelasLabels() {
        String[] labels = new String[kelasList.size() + 1];
        labels[0] = "-- Tanpa kelas --";
        for (int i = 0; i < kelasList.size(); i++) labels[i + 1] = valueOrDash(kelasList.get(i).getNamaKelas());
        return labels;
    }

    private String[] waliLabels() {
        String[] labels = new String[waliList.size() + 1];
        labels[0] = "-- Tanpa wali --";
        for (int i = 0; i < waliList.size(); i++) labels[i + 1] = valueOrDash(waliList.get(i).getNama());
        return labels;
    }

    private Integer selectedKelasId(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItemPosition() <= 0) return null;
        return kelasList.get(spinner.getSelectedItemPosition() - 1).getId();
    }

    private Integer selectedWaliId(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItemPosition() <= 0) return null;
        return waliList.get(spinner.getSelectedItemPosition() - 1).getId();
    }

    private void selectKelas(Spinner spinner, Integer kelasId) {
        if (spinner == null || kelasId == null) return;
        for (int i = 0; i < kelasList.size(); i++) {
            if (kelasList.get(i).getId() == kelasId) {
                spinner.setSelection(i + 1);
                return;
            }
        }
    }

    private String taggedText(LinearLayout form, String tag) {
        View view = form.findViewWithTag(tag);
        return view instanceof EditText ? ((EditText) view).getText().toString().trim() : "";
    }

    private String normalizeDate(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() >= 10 ? trimmed.substring(0, 10) : trimmed;
    }

    private void addRow(LinearLayout parent, String label, String value) {
        TextView tv = text(label + ": " + valueOrDash(value), "#334155", 12, false);
        tv.setPadding(0, dp(3), 0, dp(3));
        parent.addView(tv);
    }

    private TextView text(String value, String color, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(sp);
        tv.setPadding(0, dp(2), 0, dp(2));
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void showStatus(String message, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(message);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private boolean isInactive(Integer value) {
        return value != null && value == 0;
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_admin_siswa); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return -1; }
}
