package com.example.monitoringappslb.wali;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    private static final String MODE_INPUTS = "5 input terakhir";
    private static final String MODE_MONTHS = "5 bulan terakhir";
    private static final String YEAR_ALL = "Semua tahun";

    private ApiService apiService;
    private SessionManager session;
    private TableLayout tableDetail;
    private Spinner spinnerMode, spinnerYear;
    private List<PerkembanganItem> allPerkembangan = new ArrayList<>();
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
        setupGraphFilters();
        resetChart();
        loadRingkasanPerkembangan();
    }

    private void setupGraphFilters() {
        spinnerMode = findViewById(R.id.spinner_semester);
        spinnerYear = findViewById(R.id.spinner_tahun);

        String[] items = new String[]{MODE_INPUTS, MODE_MONTHS};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerMode != null) spinnerMode.setAdapter(adapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{YEAR_ALL});
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerYear != null) spinnerYear.setAdapter(yearAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderSelectedGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        if (spinnerMode != null) spinnerMode.setOnItemSelectedListener(listener);
        if (spinnerYear != null) spinnerYear.setOnItemSelectedListener(listener);
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

                allPerkembangan = response.body().getData();
                setupYearOptions(allPerkembangan);
                renderSelectedGraph();
            }

            @Override
            public void onFailure(Call<PerkembanganListResponse> call, Throwable t) {
                Toast.makeText(PerkembanganWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showTableMessage("Tidak bisa memuat perkembangan");
            }
        });
    }

    private void setupYearOptions(List<PerkembanganItem> data) {
        if (spinnerYear == null) return;

        List<String> years = new ArrayList<>();
        years.add(YEAR_ALL);
        if (data != null) {
            for (PerkembanganItem item : data) {
                String year = extractYear(item.getTanggal());
                if (!year.isEmpty() && !years.contains(year)) years.add(year);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);
    }

    private void renderSelectedGraph() {
        if (allPerkembangan == null || allPerkembangan.isEmpty()) {
            resetChart();
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        List<PerkembanganItem> filtered = filterByYear(allPerkembangan);
        if (MODE_MONTHS.equals(selectedMode())) {
            renderBulanTerakhir(filtered);
        } else {
            renderInputTerakhir(filtered);
        }
    }

    private List<PerkembanganItem> filterByYear(List<PerkembanganItem> data) {
        String year = selectedYear();
        if (YEAR_ALL.equals(year)) return data;

        List<PerkembanganItem> filtered = new ArrayList<>();
        for (PerkembanganItem item : data) {
            if (year.equals(extractYear(item.getTanggal()))) filtered.add(item);
        }
        return filtered;
    }

    private String selectedMode() {
        if (spinnerMode == null || spinnerMode.getSelectedItem() == null) return MODE_INPUTS;
        return spinnerMode.getSelectedItem().toString();
    }

    private String selectedYear() {
        if (spinnerYear == null || spinnerYear.getSelectedItem() == null) return YEAR_ALL;
        return spinnerYear.getSelectedItem().toString();
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

    private void renderBulanTerakhir(List<PerkembanganItem> data) {
        if (data == null || data.isEmpty()) {
            resetChart();
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        List<String> monthKeys = collectLatestMonthKeys(data);
        Map<String, String> labels = new LinkedHashMap<>();
        Map<String, double[]> totalByMonth = new LinkedHashMap<>();
        Map<String, Map<String, double[]>> totalByAspect = new LinkedHashMap<>();
        Map<String, String> aspectNames = new LinkedHashMap<>();

        for (PerkembanganItem item : data) {
            String monthKey = extractMonthKey(item.getTanggal());
            if (!monthKeys.contains(monthKey)) continue;

            labels.put(monthKey, formatMonthLabel(monthKey));
            addAverageState(totalByMonth, monthKey, item.getCapaian());

            String aspectKey = item.getAspekKode() != null ? item.getAspekKode() : String.valueOf(item.getAspekId());
            aspectNames.put(aspectKey, valueOrDash(item.getAspekNama()));
            Map<String, double[]> values = totalByAspect.get(aspectKey);
            if (values == null) {
                values = new LinkedHashMap<>();
                totalByAspect.put(aspectKey, values);
            }
            addAverageState(values, monthKey, item.getCapaian());
        }

        renderMonthlyChart(monthKeys, labels, totalByMonth);
        renderMonthlyTable(monthKeys, totalByAspect, aspectNames);
    }

    private List<String> collectLatestMonthKeys(List<PerkembanganItem> data) {
        List<String> keys = new ArrayList<>();
        for (PerkembanganItem item : data) {
            String key = extractMonthKey(item.getTanggal());
            if (!key.isEmpty() && !keys.contains(key)) keys.add(key);
            if (keys.size() == 5) break;
        }
        Collections.reverse(keys);
        return keys;
    }

    private void addAverageState(Map<String, double[]> target, String key, int value) {
        double[] state = target.get(key);
        if (state == null) {
            state = new double[]{0, 0};
            target.put(key, state);
        }
        state[0] += value;
        state[1] += 1;
    }

    private void renderMonthlyChart(List<String> monthKeys, Map<String, String> labels, Map<String, double[]> totalByMonth) {
        for (int i = 0; i < barIds.length; i++) {
            int value = 0;
            String label = "-";
            if (i < monthKeys.size()) {
                String key = monthKeys.get(i);
                double[] state = totalByMonth.get(key);
                if (state != null && state[1] > 0) value = (int) Math.round(state[0] / state[1]);
                label = labels.containsKey(key) ? labels.get(key) : "-";
            }
            setBarHeight(findViewById(barIds[i]), value);
            TextView tvLabel = findViewById(labelIds[i]);
            if (tvLabel != null) tvLabel.setText(label);
            TextView headerLabel = findViewById(headerLabelIds[i]);
            if (headerLabel != null) headerLabel.setText(label);
        }
    }

    private void renderMonthlyTable(List<String> monthKeys,
                                    Map<String, Map<String, double[]>> totalByAspect,
                                    Map<String, String> aspectNames) {
        clearRows();

        if (aspectNames.isEmpty()) {
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        for (Map.Entry<String, String> aspect : aspectNames.entrySet()) {
            Map<String, Integer> averages = new LinkedHashMap<>();
            Map<String, double[]> values = totalByAspect.get(aspect.getKey());
            if (values != null) {
                for (String key : monthKeys) {
                    double[] state = values.get(key);
                    if (state != null && state[1] > 0) {
                        averages.put(key, (int) Math.round(state[0] / state[1]));
                    }
                }
            }
            addAspectRow(aspect.getValue(), averages, monthKeys, getTrendText(averages, monthKeys));
        }
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

    private String extractYear(String value) {
        String key = DateTimeUtils.dateKey(value);
        return key.length() >= 4 ? key.substring(0, 4) : "";
    }

    private String extractMonthKey(String value) {
        String key = DateTimeUtils.dateKey(value);
        return key.length() >= 7 ? key.substring(0, 7) : "";
    }

    private String formatMonthLabel(String monthKey) {
        if (monthKey == null || monthKey.length() < 7) return "-";
        String month = monthKey.substring(5, 7);
        switch (month) {
            case "01": return "Jan";
            case "02": return "Feb";
            case "03": return "Mar";
            case "04": return "Apr";
            case "05": return "Mei";
            case "06": return "Jun";
            case "07": return "Jul";
            case "08": return "Agu";
            case "09": return "Sep";
            case "10": return "Okt";
            case "11": return "Nov";
            case "12": return "Des";
            default: return month;
        }
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
