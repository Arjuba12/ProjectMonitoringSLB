package com.example.monitoringappslb.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
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
import com.example.monitoringappslb.model.response.ApiModels.KegiatanItem;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KegiatanAdminActivity extends BaseAdminActivity {
    private ApiService apiService;
    private LinearLayout containerKegiatan;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kegiatan_admin);

        apiService = ApiClient.getService();
        containerKegiatan = findViewById(R.id.container_admin_kegiatan);
        tvStatus = findViewById(R.id.tv_admin_kegiatan_status);

        View btnTambah = findViewById(R.id.btn_tambah_kegiatan_admin);
        if (btnTambah != null) {
            btnTambah.setOnClickListener(v -> showTambahKegiatanDialog());
        }

        setupNavigation();
        loadKegiatan();
    }

    private void loadKegiatan() {
        Calendar calendar = Calendar.getInstance();
        apiService.getKegiatan(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
                .enqueue(new Callback<KegiatanListResponse>() {
                    @Override
                    public void onResponse(Call<KegiatanListResponse> call, Response<KegiatanListResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            setStatus("Gagal memuat kegiatan", true);
                            return;
                        }
                        renderKegiatan(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<KegiatanListResponse> call, Throwable t) {
                        setStatus("Tidak bisa terhubung ke server", true);
                        Toast.makeText(KegiatanAdminActivity.this, "Gagal memuat kegiatan", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderKegiatan(List<KegiatanItem> list) {
        containerKegiatan.removeAllViews();
        if (list == null || list.isEmpty()) {
            setStatus("Belum ada kegiatan bulan ini", true);
            return;
        }

        setStatus("", false);
        for (KegiatanItem item : list) {
            addKegiatanCard(item);
        }
    }

    private void addKegiatanCard(KegiatanItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_rounded_white_border);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));

        addText(card, valueOrDash(item.getJudul()), "#1E293B", 15, true);
        addText(card, buildMeta(item), "#64748B", 12, false);
        addText(card, valueOrDash(item.getDeskripsi()), "#334155", 13, false);
        containerKegiatan.addView(card, params);
    }

    private String buildMeta(KegiatanItem item) {
        List<String> parts = new ArrayList<>();

        String tanggal = DateTimeUtils.formatDate(item.getTanggal());
        if (!"-".equals(tanggal)) {
            parts.add(tanggal);
        }

        String jam = buildTimeRange(item.getWaktuMulai(), item.getWaktuSelesai());
        if (!jam.isEmpty()) {
            parts.add(jam);
        }

        String lokasi = valueOrDash(item.getLokasi());
        if (!"-".equals(lokasi)) {
            parts.add(lokasi);
        }

        String tipe = valueOrDash(item.getTipe());
        if (!"-".equals(tipe)) {
            parts.add(tipe);
        }

        return parts.isEmpty() ? "-" : join(parts, " | ");
    }

    private String buildTimeRange(String mulai, String selesai) {
        String start = normalizeTime(mulai);
        String end = normalizeTime(selesai);
        if (start.isEmpty() && end.isEmpty()) return "";
        if (start.isEmpty()) return end;
        if (end.isEmpty()) return start;
        return start + " - " + end;
    }

    private String normalizeTime(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String clean = value.trim();
        if (clean.length() >= 5) return clean.substring(0, 5);
        return clean;
    }

    private String join(List<String> parts, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            if (builder.length() > 0) builder.append(delimiter);
            builder.append(part.trim());
        }
        return builder.length() == 0 ? "-" : builder.toString();
    }

    private void showTambahKegiatanDialog() {
        LinearLayout form = buildKegiatanForm();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Tambah Kegiatan")
                .setView(wrapScrollable(form))
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Map<String, Object> body = readKegiatanForm(form);
            if (body == null) return;
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            createKegiatan(body, dialog);
        }));

        dialog.show();
    }

    private LinearLayout buildKegiatanForm() {
        LinearLayout container = createFormContainer();

        EditText etJudul = createInput("Judul kegiatan", InputType.TYPE_CLASS_TEXT);
        etJudul.setTag("judul");

        EditText etDeskripsi = createMultilineInput("Deskripsi kegiatan");
        etDeskripsi.setTag("deskripsi");

        EditText etTanggal = createInput("yyyy-MM-dd", InputType.TYPE_CLASS_DATETIME);
        etTanggal.setTag("tanggal");
        etTanggal.setFocusable(false);
        etTanggal.setClickable(true);

        EditText etMulai = createInput("HH:mm", InputType.TYPE_CLASS_DATETIME);
        etMulai.setTag("waktu_mulai");
        etMulai.setFocusable(false);
        etMulai.setClickable(true);

        EditText etSelesai = createInput("HH:mm", InputType.TYPE_CLASS_DATETIME);
        etSelesai.setTag("waktu_selesai");
        etSelesai.setFocusable(false);
        etSelesai.setClickable(true);

        EditText etLokasi = createInput("Lokasi kegiatan", InputType.TYPE_CLASS_TEXT);
        etLokasi.setTag("lokasi");

        Spinner spTipe = createSpinner(new String[]{"kegiatan", "jadwal", "pengumuman"}, 0);
        spTipe.setTag("tipe");

        EditText etBanner = createInput("https://...", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        etBanner.setTag("banner_url");

        Calendar now = Calendar.getInstance();
        etTanggal.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.getTime()));
        etMulai.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.getTime()));
        Calendar later = Calendar.getInstance();
        later.add(Calendar.HOUR_OF_DAY, 1);
        etSelesai.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(later.getTime()));

        etTanggal.setOnClickListener(v -> showDatePicker(etTanggal));
        etMulai.setOnClickListener(v -> showTimePicker(etMulai));
        etSelesai.setOnClickListener(v -> showTimePicker(etSelesai));

        addField(container, "Judul *", etJudul, "Contoh: Pentas seni");
        addField(container, "Deskripsi", etDeskripsi, "Isi singkat kegiatan.");
        addField(container, "Tanggal *", etTanggal, "Format backend: yyyy-MM-dd");
        addField(container, "Waktu Mulai", etMulai, "Format backend: HH:mm");
        addField(container, "Waktu Selesai", etSelesai, "Format backend: HH:mm");
        addField(container, "Lokasi", etLokasi, "Contoh: Aula sekolah");
        addField(container, "Tipe", spTipe, "Gunakan nilai yang dipakai backend.");
        addField(container, "Banner URL", etBanner, "Opsional.");

        return container;
    }

    private Map<String, Object> readKegiatanForm(LinearLayout container) {
        String judul = getTaggedText(container, "judul");
        String deskripsi = getTaggedText(container, "deskripsi");
        String tanggal = getTaggedText(container, "tanggal");
        String waktuMulai = getTaggedText(container, "waktu_mulai");
        String waktuSelesai = getTaggedText(container, "waktu_selesai");
        String lokasi = getTaggedText(container, "lokasi");
        String bannerUrl = getTaggedText(container, "banner_url");
        String tipe = getSelectedText((Spinner) container.findViewWithTag("tipe"));

        if (judul.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Judul dan tanggal wajib diisi", Toast.LENGTH_SHORT).show();
            return null;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("judul", judul);
        body.put("deskripsi", deskripsi.isEmpty() ? null : deskripsi);
        body.put("tanggal", tanggal);
        body.put("waktu_mulai", waktuMulai.isEmpty() ? null : waktuMulai);
        body.put("waktu_selesai", waktuSelesai.isEmpty() ? null : waktuSelesai);
        body.put("lokasi", lokasi.isEmpty() ? null : lokasi);
        body.put("tipe", tipe.isEmpty() ? "kegiatan" : tipe.toLowerCase(Locale.US));
        body.put("banner_url", bannerUrl.isEmpty() ? null : bannerUrl);
        return body;
    }

    private void createKegiatan(Map<String, Object> body, AlertDialog dialog) {
        apiService.createKegiatan(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(KegiatanAdminActivity.this, "Kegiatan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadKegiatan();
                } else {
                    Toast.makeText(KegiatanAdminActivity.this, "Gagal menambah kegiatan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(KegiatanAdminActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> target.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        dialog.show();
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
        tvLabel.setTextColor(Color.parseColor("#1E293B"));
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

    private EditText createMultilineInput(String hint) {
        EditText input = createInput(hint, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        return input;
    }

    private Spinner createSpinner(String[] values, int selectedIndex) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.parseColor("#0F172A"));
                    ((TextView) view).setTextSize(14);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
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

    private String getTaggedText(LinearLayout container, String tag) {
        View view = container.findViewWithTag(tag);
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString().trim();
        }
        return "";
    }

    private String getSelectedText(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItem() == null) return "";
        return spinner.getSelectedItem().toString().trim();
    }

    private void addText(LinearLayout parent, String text, String color, int size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, 0, 0, dp(4));
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findAdminDrawer(); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_admin_kegiatan; }
}
