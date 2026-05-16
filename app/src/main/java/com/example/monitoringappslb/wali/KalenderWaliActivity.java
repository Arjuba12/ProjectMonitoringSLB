package com.example.monitoringappslb.wali;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanItem;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KalenderWaliActivity extends BaseWaliActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private ApiService apiService;
    private TextView tvJadwalTitle;
    private LinearLayout containerJadwal;
    private final List<KegiatanItem> kegiatanBulanIni = new ArrayList<>();
    private int loadedMonth;
    private int loadedYear;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kalender_wali);

        drawerLayout = findViewById(R.id.drawer_layout_wali);
        navigationView = findViewById(R.id.nav_view_wali);
        bottomNavigationView = findViewById(R.id.bottom_navigation_wali);
        apiService = ApiClient.getService();
        tvJadwalTitle = findViewById(R.id.tv_jadwal_title);
        containerJadwal = findViewById(R.id.container_jadwal_kegiatan);

        setupNavigation();
        setupCalendar();

        Calendar cal = Calendar.getInstance();
        loadedMonth = cal.get(Calendar.MONTH) + 1;
        loadedYear = cal.get(Calendar.YEAR);
        selectedDate = formatDateKey(loadedYear, loadedMonth, cal.get(Calendar.DAY_OF_MONTH));
        loadKegiatan(loadedMonth, loadedYear);
    }

    private void setupCalendar() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            int pickedMonth = month + 1;
            selectedDate = formatDateKey(year, pickedMonth, dayOfMonth);
            if (pickedMonth != loadedMonth || year != loadedYear) {
                loadedMonth = pickedMonth;
                loadedYear = year;
                loadKegiatan(loadedMonth, loadedYear);
            } else {
                renderSelectedDate();
            }
        });
    }

    private void loadKegiatan(int bulan, int tahun) {
        setTitleText("Memuat jadwal...");
        showMessage("Memuat kegiatan");

        apiService.getKegiatan(bulan, tahun).enqueue(new Callback<KegiatanListResponse>() {
            @Override
            public void onResponse(Call<KegiatanListResponse> call, Response<KegiatanListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    setTitleText("Jadwal Kegiatan");
                    showMessage("Gagal memuat jadwal");
                    return;
                }

                kegiatanBulanIni.clear();
                if (response.body().getData() != null) {
                    kegiatanBulanIni.addAll(response.body().getData());
                }
                renderSelectedDate();
            }

            @Override
            public void onFailure(Call<KegiatanListResponse> call, Throwable t) {
                Toast.makeText(KalenderWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                setTitleText("Jadwal Kegiatan");
                showMessage("Tidak bisa memuat jadwal");
            }
        });
    }

    private void renderSelectedDate() {
        List<KegiatanItem> filtered = new ArrayList<>();
        for (KegiatanItem item : kegiatanBulanIni) {
            if (selectedDate.equals(normalizeDate(item.getTanggal()))) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            setTitleText("Jadwal " + readableDate(selectedDate));
            renderList(filtered, "Belum ada kegiatan pada tanggal yang dipilih");
        } else {
            setTitleText("Jadwal " + readableDate(selectedDate));
            renderList(filtered, "Belum ada kegiatan");
        }
    }

    private void renderList(List<KegiatanItem> data, String emptyMessage) {
        if (containerJadwal == null) return;
        containerJadwal.removeAllViews();

        if (data == null || data.isEmpty()) {
            showMessage(emptyMessage);
            return;
        }

        for (KegiatanItem item : data) {
            containerJadwal.addView(createKegiatanCard(item));
        }
    }

    private View createKegiatanCard(KegiatanItem item) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardElevation(0);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        View stripe = new View(this);
        LinearLayout.LayoutParams stripeParams = new LinearLayout.LayoutParams(dpToPx(4), LinearLayout.LayoutParams.MATCH_PARENT);
        stripe.setLayoutParams(stripeParams);
        stripe.setBackgroundColor(Color.parseColor(colorForType(item.getTipe())));
        row.addView(stripe);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        contentParams.setMargins(dpToPx(12), 0, 0, 0);
        content.setLayoutParams(contentParams);

        TextView title = createText(valueOrDash(item.getJudul()), "#1E293B", 14, true);
        TextView meta = createText(buildMeta(item), "#64748B", 12, false);
        meta.setPadding(0, dpToPx(4), 0, 0);
        content.addView(title);
        content.addView(meta);

        if (item.getDeskripsi() != null && !item.getDeskripsi().trim().isEmpty()) {
            TextView desc = createText(item.getDeskripsi(), "#475569", 12, false);
            desc.setPadding(0, dpToPx(8), 0, 0);
            content.addView(desc);
        }

        row.addView(content);
        card.addView(row);
        return card;
    }

    private TextView createText(String text, String color, int sizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(sizeSp);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void showMessage(String message) {
        if (containerJadwal == null) return;
        containerJadwal.removeAllViews();
        TextView tv = createText(message, "#64748B", 13, false);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dpToPx(16), dpToPx(18), dpToPx(16), dpToPx(18));
        containerJadwal.addView(tv);
    }

    private String buildMeta(KegiatanItem item) {
        StringBuilder meta = new StringBuilder();
        meta.append(readableDate(normalizeDate(item.getTanggal())));
        if (item.getWaktuMulai() != null && !item.getWaktuMulai().trim().isEmpty()) {
            meta.append(" | ").append(trimTime(item.getWaktuMulai()));
            if (item.getWaktuSelesai() != null && !item.getWaktuSelesai().trim().isEmpty()) {
                meta.append("-").append(trimTime(item.getWaktuSelesai()));
            }
            meta.append(" WIB");
        }
        if (item.getLokasi() != null && !item.getLokasi().trim().isEmpty()) {
            meta.append(" | ").append(item.getLokasi());
        }
        if (item.getTipe() != null && !item.getTipe().trim().isEmpty()) {
            meta.append(" | ").append(item.getTipe());
        }
        return meta.toString();
    }

    private String normalizeDate(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String clean = value.replace("T", " ");
        return clean.length() >= 10 ? clean.substring(0, 10) : clean;
    }

    private String readableDate(String dateKey) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            input.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            output.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
            return output.format(input.parse(dateKey));
        } catch (Exception ignored) {
            return valueOrDash(dateKey);
        }
    }

    private String monthTitle(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        SimpleDateFormat output = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
        output.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        return output.format(cal.getTime());
    }

    private String trimTime(String value) {
        return value != null && value.length() >= 5 ? value.substring(0, 5) : valueOrDash(value);
    }

    private String colorForType(String tipe) {
        if (tipe == null) return "#3498DB";
        String clean = tipe.toLowerCase(Locale.US);
        if (clean.contains("konsultasi")) return "#3498DB";
        if (clean.contains("rapor")) return "#9B59B6";
        if (clean.contains("acara")) return "#2ECC71";
        return "#F59E0B";
    }

    private String formatDateKey(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    private void setTitleText(String text) {
        if (tvJadwalTitle != null) tvJadwalTitle.setText(text);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String valueOrDash(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    @Override
    protected NavigationView getNavigationView() {
        return navigationView;
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_kalender;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_kalender;
    }
}
