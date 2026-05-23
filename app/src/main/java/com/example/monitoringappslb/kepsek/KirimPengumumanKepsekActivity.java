package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.PesanItem;
import com.example.monitoringappslb.model.response.ApiModels.PesanListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KirimPengumumanKepsekActivity extends BaseKepsekActivity {

    private EditText etJudul, etPengumuman;
    private Button btnKirim, btnDraft;
    private LinearLayout layoutPengumumanSebelumnya;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_pengumuman_kepsek);

        apiService = ApiClient.getService();

        setupNavigation();

        etJudul = findViewById(R.id.et_judul);
        etPengumuman = findViewById(R.id.et_isi);
        btnKirim = findViewById(R.id.btn_kirim);
        btnDraft = findViewById(R.id.btn_draft);
        layoutPengumumanSebelumnya = findViewById(R.id.layout_pengumuman_sebelumnya);

        loadPengumumanSebelumnya();

        if (btnKirim != null) {
            btnKirim.setOnClickListener(v -> kirimPengumuman("Terkirim"));
        }

        if (btnDraft != null) {
            btnDraft.setOnClickListener(v -> kirimPengumuman("Draft"));
        }
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_pengumuman);
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

    private void kirimPengumuman(String status) {
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

        Map<String, Object> body = new HashMap<>();
        body.put("judul", judul);
        body.put("isi", pesan);
        body.put("target_role", "semua");
        body.put("kelas_id", null);
        body.put("status", status);

        setSendingState(true, status);

        apiService.kirimPengumuman(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                setSendingState(false, status);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(KirimPengumumanKepsekActivity.this,
                            status.equals("Draft") ? "Draft pengumuman berhasil disimpan" : "Pengumuman berhasil dikirim",
                            Toast.LENGTH_SHORT).show();
                    etJudul.setText("");
                    etPengumuman.setText("");
                    loadPengumumanSebelumnya();
                } else {
                    Toast.makeText(KirimPengumumanKepsekActivity.this,
                            "Gagal menyimpan pengumuman", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                setSendingState(false, status);
                Toast.makeText(KirimPengumumanKepsekActivity.this,
                        "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSendingState(boolean sending, String status) {
        if (btnKirim != null) {
            btnKirim.setEnabled(!sending);
            btnKirim.setText(sending && !"Draft".equals(status) ? "Mengirim..." : "Kirim sekarang");
        }
        if (btnDraft != null) {
            btnDraft.setEnabled(!sending);
            btnDraft.setText(sending && "Draft".equals(status) ? "Menyimpan..." : "Simpan Draft");
        }
    }

    private void loadPengumumanSebelumnya() {
        if (layoutPengumumanSebelumnya == null) return;

        showHistoryMessage("Memuat riwayat pengumuman...", null);
        apiService.getPengumuman().enqueue(new Callback<PesanListResponse>() {
            @Override
            public void onResponse(Call<PesanListResponse> call, Response<PesanListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showHistoryMessage("Riwayat tidak dapat dimuat", "Periksa koneksi atau coba lagi nanti.");
                    return;
                }
                bindPengumumanSebelumnya(response.body().getData());
            }

            @Override
            public void onFailure(Call<PesanListResponse> call, Throwable t) {
                showHistoryMessage("Riwayat tidak dapat dimuat", "Tidak bisa terhubung ke server.");
            }
        });
    }

    private void bindPengumumanSebelumnya(List<PesanItem> items) {
        layoutPengumumanSebelumnya.removeAllViews();

        if (items == null || items.isEmpty()) {
            showHistoryMessage("Belum ada data riwayat", "Setelah pengumuman dikirim, riwayat akan muncul di bagian ini.");
            return;
        }

        int shown = 0;
        for (PesanItem item : items) {
            String title = firstNonEmpty(item.getJudul(), item.getSubjek(), "Pengumuman");
            String body = firstNonEmpty(item.getIsi(), "-");
            String meta = DateTimeUtils.formatDateTime(item.getCreatedAt())
                    + " | " + targetLabel(item.getTargetRole());

            TextView tvTitle = createHistoryText(title, 14, true, "#1E293B");
            TextView tvBody = createHistoryText(body, 12, false, "#475569");
            TextView tvMeta = createHistoryText(meta, 11, false, "#64748B");

            if (shown > 0) {
                ViewDivider divider = new ViewDivider(this);
                layoutPengumumanSebelumnya.addView(divider);
            }
            layoutPengumumanSebelumnya.addView(tvTitle);
            layoutPengumumanSebelumnya.addView(tvBody);
            layoutPengumumanSebelumnya.addView(tvMeta);

            shown++;
            if (shown >= 5) break;
        }
    }

    private TextView createHistoryText(String text, int sp, boolean bold, String color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(android.graphics.Color.parseColor(color));
        tv.setPadding(0, 2, 0, 2);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private void showHistoryMessage(String title, String subtitle) {
        layoutPengumumanSebelumnya.removeAllViews();
        layoutPengumumanSebelumnya.addView(createHistoryText(title, 13, true, "#1E293B"));
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            layoutPengumumanSebelumnya.addView(createHistoryText(subtitle, 12, false, "#64748B"));
        }
    }

    private String targetLabel(String targetRole) {
        if ("guru".equalsIgnoreCase(targetRole)) return "Guru";
        if ("wali".equalsIgnoreCase(targetRole)) return "Wali murid";
        if ("kepsek".equalsIgnoreCase(targetRole)) return "Kepsek";
        return "Semua";
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private static class ViewDivider extends android.view.View {
        ViewDivider(android.content.Context context) {
            super(context);
            setBackgroundColor(android.graphics.Color.parseColor("#CBD5E1"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            );
            params.setMargins(0, 10, 0, 10);
            setLayoutParams(params);
        }
    }
}
