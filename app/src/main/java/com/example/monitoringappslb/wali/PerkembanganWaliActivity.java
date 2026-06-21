package com.example.monitoringappslb.wali;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganItem;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerkembanganWaliActivity extends BaseWaliActivity {
    private ApiService apiService;
    private SessionManager session;
    private TableLayout tableDetail;
    private final int[] barIds = {
            R.id.bar_aug, R.id.bar_sep, R.id.bar_okt, R.id.bar_nov, R.id.bar_des
    };
    private final int[] labelIds = {
            R.id.label_month_1, R.id.label_month_2, R.id.label_month_3, R.id.label_month_4, R.id.label_month_5
    };
    private final int[] headerLabelIds = {
            R.id.header_month_1, R.id.header_month_2, R.id.header_month_3, R.id.header_month_4, R.id.header_month_5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perkembangan_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);
        tableDetail = findViewById(R.id.table_detail_perkembangan);

        setupNavigation();
        setupSemesterSpinner();
        resetChart();
        loadRingkasanPerkembangan();
    }

    private void setupSemesterSpinner() {
        Spinner spinner = findViewById(R.id.spinner_semester);
        String[] items = new String[]{"5 input terakhir"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadRingkasanPerkembangan() {
        int siswaId = session.getSiswaId();
        if (siswaId == -1) {
            showTableMessage("ID siswa tidak ditemukan. Buka dashboard atau login ulang.");
            return;
        }

        apiService.getPerkembanganSiswa(siswaId, null).enqueue(new Callback<PerkembanganListResponse>() {
            @Override
            public void onResponse(Call<PerkembanganListResponse> call, Response<PerkembanganListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showTableMessage("Gagal memuat perkembangan");
                    return;
                }

                renderInputTerakhir(response.body().getData());
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {
                Toast.makeText(PerkembanganWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showTableMessage("Tidak bisa memuat perkembangan");
            }
        });
    }

    private void renderInputTerakhir(List<PerkembanganItem> data) {
        if (data == null || data.isEmpty()) {
            resetChart();
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        List<PerkembanganItem> latest = new ArrayList<>();
        int limit = Math.min(data.size(), 5);
        for (int i = 0; i < limit; i++) {
            latest.add(data.get(i));
        }
        Collections.reverse(latest);

        List<String> inputKeys = new ArrayList<>();
        Map<String, String> inputLabels = new LinkedHashMap<>();
        for (int i = 0; i < latest.size(); i++) {
            String key = String.valueOf(i);
            inputKeys.add(key);
            inputLabels.put(key, buildInputLabel(latest.get(i), i + 1));
        }

        renderChart(latest, inputLabels);
        renderTable(latest, inputKeys);
    }

    private void renderChart(List<PerkembanganItem> inputs, Map<String, String> inputLabels) {
        for (int i = 0; i < barIds.length; i++) {
            int value = 0;
            String label = "-";
            if (i < inputs.size()) {
                value = inputs.get(i).getCapaian();
                label = inputLabels.get(String.valueOf(i));
            }
            setBarHeight(findViewById(barIds[i]), value);
            TextView tvLabel = findViewById(labelIds[i]);
            if (tvLabel != null) tvLabel.setText(label);
            TextView headerLabel = findViewById(headerLabelIds[i]);
            if (headerLabel != null) headerLabel.setText(label);
        }
    }

    private void renderTable(List<PerkembanganItem> inputs, List<String> inputKeys) {
        clearRows();

        Map<String, Map<String, Integer>> valuesByAspect = new LinkedHashMap<>();
        Map<String, String> aspectNames = new LinkedHashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            PerkembanganItem item = inputs.get(i);
            String kode = item.getAspekKode() != null ? item.getAspekKode() : String.valueOf(item.getAspekId());
            aspectNames.put(kode, valueOrDash(item.getAspekNama()));
            Map<String, Integer> values = valuesByAspect.get(kode);
            if (values == null) {
                values = new LinkedHashMap<>();
                valuesByAspect.put(kode, values);
            }
            values.put(String.valueOf(i), item.getCapaian());
        }

        if (aspectNames.isEmpty()) {
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        for (Map.Entry<String, String> aspect : aspectNames.entrySet()) {
            addAspectRow(
                    aspect.getValue(),
                    valuesByAspect.get(aspect.getKey()),
                    inputKeys,
                    getTrendText(valuesByAspect.get(aspect.getKey()), inputKeys)
            );
        }
    }

    private String getTrendText(Map<String, Integer> values, List<String> inputKeys) {
        if (values == null || inputKeys == null || inputKeys.size() < 2) return "-";
        Integer first = null;
        Integer last = null;
        for (String key : inputKeys) {
            Integer value = values.get(key);
            if (value == null) continue;
            if (first == null) first = value;
            last = value;
        }
        if (first == null || last == null || first.equals(last)) return "Stabil";
        return last > first ? "Naik" : "Turun";
    }

    private void addAspectRow(String aspek, Map<String, Integer> values, List<String> monthKeys, String trend) {
        if (tableDetail == null) return;

        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 10, 0, 10);
        row.addView(createCell(aspek, "#2C3E50", 95, false, false));

        for (int i = 0; i < 5; i++) {
            String text = "-";
            if (i < monthKeys.size() && values != null && values.containsKey(monthKeys.get(i))) {
                text = values.get(monthKeys.get(i)) + "%";
            }
            row.addView(createCell(text, "#7F8C8D", 54, true, false));
        }

        row.addView(createCell(trend, trendColor(trend), 86, true, true));
        tableDetail.addView(row);
        addDivider();
    }

    private TextView createCell(String text, String color, int widthDp, boolean center, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(bold ? 10 : 11);
        tv.setPadding(8, 6, 8, 6);
        tv.setMinWidth(dpToPx(widthDp));
        tv.setMaxWidth(dpToPx(widthDp));
        tv.setGravity(center ? Gravity.CENTER : Gravity.START);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void showTableMessage(String message) {
        clearRows();
        addAspectRow(message, null, new ArrayList<>(), "-");
    }

    private void clearRows() {
        if (tableDetail == null) return;
        while (tableDetail.getChildCount() > 2) {
            tableDetail.removeViewAt(2);
        }
    }

    private void addDivider() {
        if (tableDetail == null) return;
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#ECF0F1"));
        divider.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        tableDetail.addView(divider);
    }

    private void resetChart() {
        for (int i = 0; i < barIds.length; i++) {
            setBarHeight(findViewById(barIds[i]), 0);
            TextView label = findViewById(labelIds[i]);
            if (label != null) label.setText("-");
            TextView headerLabel = findViewById(headerLabelIds[i]);
            if (headerLabel != null) headerLabel.setText("-");
        }
    }

    private void setBarHeight(View bar, int percentage) {
        if (bar == null) return;

        int maxHeightPx = (int) (180 * getResources().getDisplayMetrics().density);
        int targetHeight = (maxHeightPx * Math.max(0, Math.min(100, percentage))) / 100;

        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = targetHeight;
        bar.setLayoutParams(params);
    }

    private String buildInputLabel(PerkembanganItem item, int index) {
        String date = DateTimeUtils.formatDate(item.getTanggal());
        if (date == null || date.trim().isEmpty() || "-".equals(date)) {
            return "Input " + index;
        }
        return date;
    }

    private String trendColor(String trend) {
        if (trend == null) return "#64748B";
        String clean = trend.toLowerCase(Locale.US);
        if (clean.contains("naik")) return "#2E7D32";
        if (clean.contains("lambat")) return "#C62828";
        if (clean.contains("stabil")) return "#1565C0";
        return "#64748B";
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private String valueOrDash(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_perkembangan);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view_wali);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation_wali);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_perkembangan;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_perkembangan;
    }
}
