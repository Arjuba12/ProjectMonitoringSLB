package com.example.monitoringappslb.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.AdminUserItem;
import com.example.monitoringappslb.model.response.ApiModels.AdminUserListResponse;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.TingkatItem;
import com.example.monitoringappslb.model.response.ApiModels.TingkatListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MasterDataAdminActivity extends BaseAdminActivity {
    public static final String EXTRA_MODE = "admin_master_mode";
    public static final String MODE_KELAS = "kelas";
    public static final String MODE_USER = "user";

    private ApiService apiService;
    private LinearLayout containerKelas, containerUsers;
    private TextView tvStatus, tvUserSummary;
    private List<TingkatItem> tingkatList = new ArrayList<>();
    private List<AdminUserItem> guruList = new ArrayList<>();
    private String mode = MODE_KELAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_data_admin);

        apiService = ApiClient.getService();
        containerKelas = findViewById(R.id.container_admin_kelas);
        containerUsers = findViewById(R.id.container_admin_users);
        tvStatus = findViewById(R.id.tv_admin_master_status);
        tvUserSummary = findViewById(R.id.tv_admin_user_summary);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (!MODE_USER.equals(mode)) mode = MODE_KELAS;

        setupNavigation();
        setupModeUi();

        View btnTambahKelas = findViewById(R.id.btn_tambah_kelas_admin);
        if (btnTambahKelas != null) {
            btnTambahKelas.setOnClickListener(v -> showTambahKelasDialog());
        }
        View btnTambahUser = findViewById(R.id.btn_tambah_user_admin);
        if (btnTambahUser != null) {
            btnTambahUser.setOnClickListener(v -> showTambahUserDialog());
        }

        if (MODE_KELAS.equals(mode)) {
            loadKelas();
        } else {
            loadUsers();
        }
        loadTingkat();
        if (MODE_KELAS.equals(mode)) {
            loadGuruOptions();
        }
    }

    private void setupModeUi() {
        TextView title = findViewById(R.id.tv_admin_master_title);
        View cardKelas = findViewById(R.id.card_admin_kelas);
        View cardUsers = findViewById(R.id.card_admin_users);

        if (MODE_USER.equals(mode)) {
            if (title != null) title.setText("User");
            if (cardKelas != null) cardKelas.setVisibility(View.GONE);
            if (cardUsers != null) cardUsers.setVisibility(View.VISIBLE);
            setStatus("Memuat data user...", true);
        } else {
            if (title != null) title.setText("Kelas");
            if (cardKelas != null) cardKelas.setVisibility(View.VISIBLE);
            if (cardUsers != null) cardUsers.setVisibility(View.GONE);
            setStatus("Memuat data kelas...", true);
        }
    }

    private void loadKelas() {
        setStatus("Memuat data kelas...", true);
        apiService.getKelas(null).enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderKelas(null);
                    setStatus("Gagal memuat data kelas", true);
                    return;
                }
                renderKelas(response.body().getData());
                setStatus("", false);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                renderKelas(null);
                setStatus("Tidak bisa terhubung ke server", true);
                Toast.makeText(MasterDataAdminActivity.this, "Gagal memuat kelas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        apiService.getUsers(null, null, null).enqueue(new Callback<AdminUserListResponse>() {
            @Override
            public void onResponse(Call<AdminUserListResponse> call, Response<AdminUserListResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderUsers(null);
                    return;
                }
                renderUsers(response.body().getData());
            }

            @Override
            public void onFailure(Call<AdminUserListResponse> call, Throwable t) {
                renderUsers(null);
            }
        });
    }

    private void loadTingkat() {
        apiService.getTingkat().enqueue(new Callback<TingkatListResponse>() {
            @Override
            public void onResponse(Call<TingkatListResponse> call, Response<TingkatListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    tingkatList = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<TingkatListResponse> call, Throwable t) {
                tingkatList = new ArrayList<>();
            }
        });
    }

    private void loadGuruOptions() {
        apiService.getUsers("guru", 1, null).enqueue(new Callback<AdminUserListResponse>() {
            @Override
            public void onResponse(Call<AdminUserListResponse> call, Response<AdminUserListResponse> response) {
                guruList = response.isSuccessful() && response.body() != null && response.body().getData() != null
                        ? response.body().getData()
                        : new ArrayList<>();
            }

            @Override
            public void onFailure(Call<AdminUserListResponse> call, Throwable t) {
                guruList = new ArrayList<>();
            }
        });
    }

    private void renderKelas(List<KelasItem> list) {
        if (containerKelas == null) return;
        containerKelas.removeAllViews();
        if (list == null || list.isEmpty()) {
            addText(containerKelas, "Belum ada data kelas", "#64748B", 13, false);
            return;
        }

        for (KelasItem item : list) {
            String subtitle = item.getJmlSiswa() + " siswa | Wali kelas: " + valueOrDash(item.getNamaWaliKelas());
            if (item.isAktif() != null && item.isAktif() == 0) {
                subtitle += " | Nonaktif";
            }
            addKelasRow(item, subtitle);
        }
    }

    private void addKelasRow(KelasItem item, String subtitle) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        addText(row, valueOrDash(item.getNamaKelas()), "#1E293B", 14, true);
        addText(row, subtitle, isInactive(item.isAktif()) ? "#EF4444" : "#64748B", 12, false);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);
        actions.addView(createSmallButton("Edit", "#1E293B", v -> showEditKelasDialog(item)));
        actions.addView(createSmallButton(isInactive(item.isAktif()) ? "Aktifkan" : "Nonaktif", "#EF4444",
                v -> confirmToggleKelas(item)));
        row.addView(actions);

        containerKelas.addView(row);
        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        containerKelas.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void renderUsers(List<AdminUserItem> list) {
        if (containerUsers == null) return;
        containerUsers.removeAllViews();
        if (list == null || list.isEmpty()) {
            if (tvUserSummary != null) tvUserSummary.setText("Belum ada data user");
            addText(containerUsers, "Data user belum bisa dimuat", "#64748B", 13, false);
            return;
        }

        int admin = 0, kepsek = 0, guru = 0, wali = 0;
        for (AdminUserItem user : list) {
            if (user.isAktif() != null && user.isAktif() == 0) continue;
            String role = user.getRole();
            if ("admin".equals(role)) admin++;
            else if ("kepsek".equals(role)) kepsek++;
            else if ("guru".equals(role)) guru++;
            else if ("wali".equals(role)) wali++;
        }

        if (tvUserSummary != null) {
            tvUserSummary.setText(admin + " admin | " + kepsek + " kepsek | " + guru + " guru | " + wali + " wali");
        }

        for (AdminUserItem user : list) {
            addUserRow(user);
        }
    }

    private void addUserRow(AdminUserItem user) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        String subtitle = roleLabel(user.getRole()) + " | " + valueOrDash(user.getEmail());
        if ("guru".equals(user.getRole()) && user.getKelasMengajar() != null && !user.getKelasMengajar().trim().isEmpty()) {
            subtitle += " | " + user.getKelasMengajar();
        }
        if (isInactive(user.isAktif())) {
            subtitle += " | Nonaktif";
        }

        addText(row, valueOrDash(user.getNama()), "#1E293B", 14, true);
        addText(row, subtitle, isInactive(user.isAktif()) ? "#EF4444" : "#64748B", 12, false);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, 0);
        actions.addView(createSmallButton("Edit", "#1E293B", v -> showEditUserDialog(user)));
        actions.addView(createSmallButton("Reset", "#64748B", v -> showResetPasswordDialog(user)));
        actions.addView(createSmallButton(isInactive(user.isAktif()) ? "Aktifkan" : "Nonaktif", "#EF4444",
                v -> confirmToggleUser(user)));
        row.addView(actions);

        containerUsers.addView(row);
        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        containerUsers.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private MaterialButton createSmallButton(String text, String color, View.OnClickListener listener) {
        MaterialButton button = new MaterialButton(this);
        button.setText(text);
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

    private void showTambahKelasDialog() {
        LinearLayout container = buildKelasForm(null, true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tambah Kelas")
                .setView(wrapScrollable(container))
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readKelasForm(container, true);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            createKelas(body, dialog);
        }));

        dialog.show();
    }

    private LinearLayout buildKelasForm(KelasItem item, boolean createMode) {
        LinearLayout container = createFormContainer();

        EditText etNama = createInput("VII-A", InputType.TYPE_CLASS_TEXT);
        etNama.setTag("nama_kelas");
        EditText etTahun = createInput("2024/2025", InputType.TYPE_CLASS_TEXT);
        etTahun.setTag("tahun_ajaran");
        EditText etKapasitas = createInput("10", InputType.TYPE_CLASS_NUMBER);
        etKapasitas.setTag("kapasitas");
        Spinner spTingkat = createSpinner(getTingkatLabels(), 0);
        spTingkat.setTag("tingkat_id");
        Spinner spStatus = createSpinner(new String[]{"Aktif", "Nonaktif"}, 0);
        spStatus.setTag("is_aktif");
        Spinner spWaliKelas = createSpinner(getGuruLabels(), 0);
        spWaliKelas.setTag("wali_kelas_guru_id");

        etTahun.setEnabled(createMode);
        spTingkat.setEnabled(createMode);

        if (item != null) {
            etNama.setText(valueOrBlank(item.getNamaKelas()));
            etTahun.setText(valueOrBlank(item.getTahunAjaran()));
            etKapasitas.setText(String.valueOf(item.getKapasitas()));
            selectTingkat(spTingkat, item.getTingkatId());
            selectGuru(spWaliKelas, item.getGuruId());
            spStatus.setSelection(isInactive(item.isAktif()) ? 1 : 0);
        } else {
            etTahun.setText("2024/2025");
            etKapasitas.setText("10");
        }

        addField(container, "Nama Kelas *", etNama, "Contoh: VII-A");
        addField(container, "Tingkat *", spTingkat, createMode ? "Sama seperti admin panel web." : "Tingkat tidak diubah dari aplikasi Android.");
        addField(container, "Tahun Ajaran", etTahun, "Format: 2024/2025");
        addField(container, "Kapasitas", etKapasitas, "Jumlah maksimal siswa.");
        addField(container, "Wali Kelas", spWaliKelas, "Pilih guru aktif yang menjadi wali kelas.");
        if (!createMode) {
            addField(container, "Status", spStatus, null);
        }
        return container;
    }

    private Map<String, Object> readKelasForm(LinearLayout container, boolean createMode) {
        String nama = getTaggedText(container, "nama_kelas");
        String tahun = getTaggedText(container, "tahun_ajaran");
        String kapasitasText = getTaggedText(container, "kapasitas");
        int tingkatId = getSelectedTingkatId((Spinner) container.findViewWithTag("tingkat_id"));

        if (nama.isEmpty() || (createMode && tingkatId == 0)) {
            Toast.makeText(this, "Nama kelas dan tingkat wajib diisi", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (tahun.isEmpty()) tahun = "2024/2025";
        if (kapasitasText.isEmpty()) kapasitasText = "10";

        Integer kapasitas = parsePositiveNumber(kapasitasText, "Kapasitas harus berupa angka");
        if (kapasitas == null) return null;

        Map<String, Object> body = new HashMap<>();
        body.put("nama_kelas", nama);
        body.put("kapasitas", kapasitas);
        body.put("wali_kelas_guru_id",
                getSelectedGuruId((Spinner) container.findViewWithTag("wali_kelas_guru_id")));
        if (createMode) {
            body.put("tingkat_id", tingkatId);
            body.put("tahun_ajaran", tahun);
        } else {
            Spinner spStatus = (Spinner) container.findViewWithTag("is_aktif");
            body.put("is_aktif", spStatus != null && spStatus.getSelectedItemPosition() == 1 ? 0 : 1);
        }
        return body;
    }

    private EditText createInput(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setTextSize(14);
        input.setTextColor(Color.parseColor("#0F172A"));
        input.setHintTextColor(Color.parseColor("#94A3B8"));
        input.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        input.setPadding(0, dp(10), 0, dp(10));
        return input;
    }

    private void createKelas(Map<String, Object> body, AlertDialog dialog) {
        apiService.createKelas(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MasterDataAdminActivity.this, "Kelas berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadKelas();
                    if (MODE_USER.equals(mode)) loadUsers();
                } else {
                    Toast.makeText(MasterDataAdminActivity.this, "Gagal menambah kelas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditKelasDialog(KelasItem item) {
        LinearLayout container = buildKelasForm(item, false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Kelas")
                .setView(wrapScrollable(container))
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readKelasForm(container, false);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            updateKelas(item.getId(), body, dialog);
        }));

        dialog.show();
    }

    private void updateKelas(int id, Map<String, Object> body, AlertDialog dialog) {
        apiService.updateKelas(id, body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MasterDataAdminActivity.this, "Kelas berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadKelas();
                } else {
                    Toast.makeText(MasterDataAdminActivity.this, "Gagal memperbarui kelas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmToggleKelas(KelasItem item) {
        boolean inactive = isInactive(item.isAktif());
        new AlertDialog.Builder(this)
                .setTitle(inactive ? "Aktifkan kelas?" : "Nonaktifkan kelas?")
                .setMessage(valueOrDash(item.getNamaKelas()))
                .setNegativeButton("Batal", null)
                .setPositiveButton(inactive ? "Aktifkan" : "Nonaktifkan", (dialog, which) -> toggleKelasActive(item))
                .show();
    }

    private void toggleKelasActive(KelasItem item) {
        int nextStatus = isInactive(item.isAktif()) ? 1 : 0;
        Map<String, Object> body = new HashMap<>();
        body.put("nama_kelas", item.getNamaKelas());
        body.put("kapasitas", item.getKapasitas());
        body.put("is_aktif", nextStatus);

        apiService.updateKelas(item.getId(), body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MasterDataAdminActivity.this,
                            nextStatus == 1 ? "Kelas diaktifkan" : "Kelas dinonaktifkan",
                            Toast.LENGTH_SHORT).show();
                    loadKelas();
                } else {
                    Toast.makeText(MasterDataAdminActivity.this, "Gagal mengubah status kelas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTambahUserDialog() {
        LinearLayout container = buildUserForm(null, true);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tambah User")
                .setView(wrapScrollable(container))
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readUserForm(container, true);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            apiService.createUser(body).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(MasterDataAdminActivity.this, "User berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUsers();
                    } else {
                        Toast.makeText(MasterDataAdminActivity.this, "Gagal menambah user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private void showEditUserDialog(AdminUserItem user) {
        LinearLayout container = buildUserForm(user, false);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(wrapScrollable(container))
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readUserForm(container, false);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            apiService.updateUser(user.getId(), body).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(MasterDataAdminActivity.this, "User berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUsers();
                    } else {
                        Toast.makeText(MasterDataAdminActivity.this, "Gagal memperbarui user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            });
        }));

        dialog.show();
    }

    private LinearLayout buildUserForm(AdminUserItem user, boolean createMode) {
        LinearLayout container = createFormContainer();

        EditText etNama = createInput("Nama lengkap", InputType.TYPE_CLASS_TEXT);
        etNama.setTag("nama");
        EditText etEmail = createInput("nama@email.com", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setTag("email");
        EditText etPassword = createInput("Min. 8 karakter", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassword.setTag("password");
        Spinner spRole = createSpinner(new String[]{"Guru", "Wali", "Kepsek", "Admin"}, 0);
        spRole.setTag("role");
        EditText etNoHp = createInput("08xx-xxxx-xxxx", InputType.TYPE_CLASS_PHONE);
        etNoHp.setTag("no_hp");
        EditText etNip = createInput("NIP guru", InputType.TYPE_CLASS_TEXT);
        etNip.setTag("nip");
        Spinner spSpesialisasi = createSpinner(new String[]{"Guru Kelas", "Terapis", "Guru Mapel"}, 0);
        spSpesialisasi.setTag("spesialisasi");
        Spinner spStatus = createSpinner(new String[]{"Aktif", "Nonaktif"}, 0);
        spStatus.setTag("is_aktif");

        LinearLayout nipGroup = createFieldGroup("NIP", etNip, "Khusus akun guru.");
        nipGroup.setTag("nip_group");
        LinearLayout spesialisasiGroup = createFieldGroup("Spesialisasi", spSpesialisasi, "Khusus akun guru.");
        spesialisasiGroup.setTag("spesialisasi_group");

        if (user != null) {
            etNama.setText(valueOrBlank(user.getNama()));
            etEmail.setText(valueOrBlank(user.getEmail()));
            etEmail.setEnabled(false);
            spRole.setSelection(roleIndex(user.getRole()));
            spRole.setEnabled(false);
            etNoHp.setText(valueOrBlank(user.getNoHp()));
            etNip.setText(valueOrBlank(user.getNip()));
            spSpesialisasi.setSelection(spesialisasiIndex(user.getSpesialisasi()));
            spStatus.setSelection(isInactive(user.isAktif()) ? 1 : 0);
        }

        addField(container, "Nama Lengkap *", etNama, null);
        addField(container, "Email *", etEmail, createMode ? null : "Email tidak diubah saat edit.");
        if (createMode) addField(container, "Password *", etPassword, "Min. 8 karakter.");
        addField(container, "Role *", spRole, createMode ? "Sama seperti admin panel web." : "Role tidak diubah saat edit.");
        addField(container, "No. HP", etNoHp, null);
        container.addView(nipGroup);
        container.addView(spesialisasiGroup);
        if (!createMode) addField(container, "Status", spStatus, null);

        updateGuruFieldsVisibility(spRole, nipGroup, spesialisasiGroup);
        spRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGuruFieldsVisibility(spRole, nipGroup, spesialisasiGroup);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateGuruFieldsVisibility(spRole, nipGroup, spesialisasiGroup);
            }
        });
        return container;
    }

    private Map<String, Object> readUserForm(LinearLayout container, boolean createMode) {
        String nama = getTaggedText(container, "nama");
        String email = getTaggedText(container, "email");
        String password = getTaggedText(container, "password");
        String role = getSelectedRole((Spinner) container.findViewWithTag("role"));
        String noHp = getTaggedText(container, "no_hp");
        String nip = getTaggedText(container, "nip");
        String spesialisasi = getSelectedText((Spinner) container.findViewWithTag("spesialisasi"));

        if (nama.isEmpty() || role.isEmpty() || (createMode && (email.isEmpty() || password.isEmpty()))) {
            Toast.makeText(this, "Nama, role, email, dan password wajib diisi", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (createMode && password.length() < 8) {
            Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("nama", nama);
        body.put("role", role);
        body.put("no_hp", noHp.isEmpty() ? null : noHp);
        body.put("nip", "guru".equals(role) && !nip.isEmpty() ? nip : null);
        body.put("spesialisasi", "guru".equals(role) && !spesialisasi.isEmpty() ? spesialisasi : null);
        if (createMode) {
            body.put("email", email);
            body.put("password", password);
        } else {
            Spinner spStatus = (Spinner) container.findViewWithTag("is_aktif");
            body.put("is_aktif", spStatus != null && spStatus.getSelectedItemPosition() == 1 ? 0 : 1);
        }
        return body;
    }

    private String getTaggedText(LinearLayout container, String tag) {
        View view = container.findViewWithTag(tag);
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString().trim();
        }
        return "";
    }

    private LinearLayout createFormContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(8), dp(20), 0);
        return container;
    }

    private ScrollView wrapScrollable(LinearLayout container) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container);
        return scrollView;
    }

    private void addField(LinearLayout parent, String label, View input, String hint) {
        parent.addView(createFieldGroup(label, input, hint));
    }

    private LinearLayout createFieldGroup(String label, View input, String hint) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setPadding(0, 0, 0, dp(12));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.WHITE);
        tvLabel.setTextSize(12);
        tvLabel.setTypeface(null, Typeface.BOLD);
        group.addView(tvLabel);

        group.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                input instanceof Spinner ? dp(44) : LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        if (hint != null && !hint.trim().isEmpty()) {
            TextView tvHint = new TextView(this);
            tvHint.setText(hint);
            tvHint.setTextColor(Color.parseColor("#94A3B8"));
            tvHint.setTextSize(11);
            tvHint.setPadding(0, dp(2), 0, 0);
            group.addView(tvHint);
        }

        return group;
    }

    private Spinner createSpinner(String[] values, int selectedIndex) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#0F172A"));
                    ((TextView) view).setTextSize(14);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#0F172A"));
                    ((TextView) view).setTextSize(14);
                    view.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        if (selectedIndex >= 0 && selectedIndex < values.length) {
            spinner.setSelection(selectedIndex);
        }
        return spinner;
    }

    private String[] getTingkatLabels() {
        if (tingkatList == null || tingkatList.isEmpty()) {
            return new String[]{"Pilih tingkat..."};
        }
        String[] labels = new String[tingkatList.size() + 1];
        labels[0] = "Pilih tingkat...";
        for (int i = 0; i < tingkatList.size(); i++) {
            TingkatItem item = tingkatList.get(i);
            String keterangan = valueOrBlank(item.getKeterangan());
            labels[i + 1] = valueOrDash(item.getNama()) + (keterangan.isEmpty() ? "" : " - " + keterangan);
        }
        return labels;
    }

    private String[] getGuruLabels() {
        String[] labels = new String[guruList.size() + 1];
        labels[0] = "-- Belum ditentukan --";
        for (int i = 0; i < guruList.size(); i++) {
            AdminUserItem guru = guruList.get(i);
            String nip = valueOrBlank(guru.getNip());
            labels[i + 1] = valueOrDash(guru.getNama()) + (nip.isEmpty() ? "" : " | " + nip);
        }
        return labels;
    }

    private Integer getSelectedGuruId(Spinner spinner) {
        if (spinner == null || guruList.isEmpty()) return null;
        int index = spinner.getSelectedItemPosition() - 1;
        if (index < 0 || index >= guruList.size()) return null;
        return guruList.get(index).getGuruId();
    }

    private void selectGuru(Spinner spinner, Integer guruId) {
        if (spinner == null || guruId == null) return;
        for (int i = 0; i < guruList.size(); i++) {
            if (guruId.equals(guruList.get(i).getGuruId())) {
                spinner.setSelection(i + 1);
                return;
            }
        }
    }

    private int getSelectedTingkatId(Spinner spinner) {
        if (spinner == null || tingkatList == null || tingkatList.isEmpty()) return 0;
        int index = spinner.getSelectedItemPosition() - 1;
        if (index < 0 || index >= tingkatList.size()) return 0;
        return tingkatList.get(index).getId();
    }

    private void selectTingkat(Spinner spinner, int tingkatId) {
        if (spinner == null || tingkatList == null) return;
        for (int i = 0; i < tingkatList.size(); i++) {
            if (tingkatList.get(i).getId() == tingkatId) {
                spinner.setSelection(i + 1);
                return;
            }
        }
    }

    private String getSelectedText(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItem() == null) return "";
        return spinner.getSelectedItem().toString().trim();
    }

    private String getSelectedRole(Spinner spinner) {
        String label = getSelectedText(spinner).toLowerCase();
        if ("guru".equals(label)) return "guru";
        if ("wali".equals(label)) return "wali";
        if ("kepsek".equals(label)) return "kepsek";
        if ("admin".equals(label)) return "admin";
        return "";
    }

    private int roleIndex(String role) {
        if ("wali".equals(role)) return 1;
        if ("kepsek".equals(role)) return 2;
        if ("admin".equals(role)) return 3;
        return 0;
    }

    private int spesialisasiIndex(String value) {
        if ("Terapis".equals(value)) return 1;
        if ("Guru Mapel".equals(value)) return 2;
        return 0;
    }

    private void updateGuruFieldsVisibility(Spinner roleSpinner, LinearLayout nipGroup, LinearLayout spesialisasiGroup) {
        boolean isGuru = "guru".equals(getSelectedRole(roleSpinner));
        nipGroup.setVisibility(isGuru ? View.VISIBLE : View.GONE);
        spesialisasiGroup.setVisibility(isGuru ? View.VISIBLE : View.GONE);
    }

    private void showResetPasswordDialog(AdminUserItem user) {
        EditText input = createInput("Password baru", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int padding = dp(20);
        input.setPadding(padding, dp(8), padding, 0);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(input)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Reset", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = input.getText().toString().trim();
            if (password.length() < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, String> body = new HashMap<>();
            body.put("password_baru", password);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            apiService.resetUserPassword(user.getId(), body).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(MasterDataAdminActivity.this, "Password berhasil direset", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MasterDataAdminActivity.this, "Gagal reset password", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            });
        }));
        dialog.show();
    }

    private void confirmToggleUser(AdminUserItem user) {
        boolean inactive = isInactive(user.isAktif());
        new AlertDialog.Builder(this)
                .setTitle(inactive ? "Aktifkan user?" : "Nonaktifkan user?")
                .setMessage(valueOrDash(user.getNama()))
                .setNegativeButton("Batal", null)
                .setPositiveButton(inactive ? "Aktifkan" : "Nonaktifkan", (dialog, which) -> toggleUserActive(user))
                .show();
    }

    private void toggleUserActive(AdminUserItem user) {
        int nextStatus = isInactive(user.isAktif()) ? 1 : 0;
        Map<String, Object> body = new HashMap<>();
        body.put("nama", user.getNama());
        body.put("no_hp", user.getNoHp());
        body.put("is_aktif", nextStatus);
        body.put("nip", user.getNip());
        body.put("spesialisasi", user.getSpesialisasi());

        apiService.updateUser(user.getId(), body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(MasterDataAdminActivity.this,
                            nextStatus == 1 ? "User diaktifkan" : "User dinonaktifkan",
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(MasterDataAdminActivity.this, "Gagal mengubah status user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(MasterDataAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Integer parsePositiveNumber(String value, String message) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException e) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Integer parseStatusAktif(String value) {
        String normalized = value == null || value.trim().isEmpty() ? "1" : value.trim();
        if (!"0".equals(normalized) && !"1".equals(normalized)) {
            Toast.makeText(this, "Status aktif hanya boleh 1 atau 0", Toast.LENGTH_SHORT).show();
            return null;
        }
        return Integer.parseInt(normalized);
    }

    private boolean isInactive(Integer value) {
        return value != null && value == 0;
    }

    private void addRow(LinearLayout parent, String title, String subtitle) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        addText(row, title, "#1E293B", 14, true);
        addText(row, subtitle, "#64748B", 12, false);
        parent.addView(row);

        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        parent.addView(line, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void addText(LinearLayout parent, String text, String color, int size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        parent.addView(tv);
    }

    private void setStatus(String text, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(text);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String roleLabel(String role) {
        if ("admin".equals(role)) return "Admin";
        if ("kepsek".equals(role)) return "Kepsek";
        if ("guru".equals(role)) return "Guru";
        if ("wali".equals(role)) return "Wali";
        return valueOrDash(role);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return -1; }
}
