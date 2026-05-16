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
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganRingkasanData;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganRingkasanHistory;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganRingkasanResponse;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganRingkasanTrend;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
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
        String[] items = new String[]{"5 bulan terakhir"};
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

        apiService.getRingkasanPerkembanganSiswa(siswaId, 5).enqueue(new Callback<PerkembanganRingkasanResponse>() {
            @Override
            public void onResponse(Call<PerkembanganRingkasanResponse> call, Response<PerkembanganRingkasanResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()
                        || response.body().getData() == null) {
                    showTableMessage("Gagal memuat perkembangan");
                    return;
                }

                renderRingkasan(response.body().getData());
            }

            @Override
            public void onFailure(Call<PerkembanganRingkasanResponse> call, Throwable t) {
                Toast.makeText(PerkembanganWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showTableMessage("Tidak bisa memuat perkembangan");
            }
        });
    }

    private void renderRingkasan(PerkembanganRingkasanData data) {
        List<PerkembanganRingkasanHistory> history = data.getHistory();
        List<PerkembanganRingkasanTrend> trends = data.getTrend();
        if ((history == null || history.isEmpty()) && (trends == null || trends.isEmpty())) {
            resetChart();
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        List<String> monthKeys = collectMonthKeys(history);
        Map<String, String> monthLabels = collectMonthLabels(history);
        renderChart(history, monthKeys, monthLabels);
        renderTable(history, trends, monthKeys);
    }

    private List<String> collectMonthKeys(List<PerkembanganRingkasanHistory> history) {
        LinkedHashMap<String, Boolean> keys = new LinkedHashMap<>();
        if (history != null) {
            for (PerkembanganRingkasanHistory item : history) {
                if (item.getBulanKey() != null) keys.put(item.getBulanKey(), true);
            }
        }
        return new ArrayList<>(keys.keySet());
    }

    private Map<String, String> collectMonthLabels(List<PerkembanganRingkasanHistory> history) {
        Map<String, String> labels = new LinkedHashMap<>();
        if (history != null) {
            for (PerkembanganRingkasanHistory item : history) {
                if (item.getBulanKey() != null) {
                    labels.put(item.getBulanKey(), formatMonthLabel(item.getBulanLabel(), item.getBulanKey()));
                }
            }
        }
        return labels;
    }

    private void renderChart(List<PerkembanganRingkasanHistory> history, List<String> monthKeys, Map<String, String> monthLabels) {
        Map<String, double[]> averages = new LinkedHashMap<>();
        if (history != null) {
            for (PerkembanganRingkasanHistory item : history) {
                if (item.getBulanKey() == null || item.getRataRata() == null) continue;
                double[] state = averages.get(item.getBulanKey());
                if (state == null) {
                    state = new double[]{0, 0};
                    averages.put(item.getBulanKey(), state);
                }
                state[0] += item.getRataRata();
                state[1] += 1;
            }
        }

        for (int i = 0; i < barIds.length; i++) {
            int value = 0;
            String label = "-";
            if (i < monthKeys.size()) {
                String key = monthKeys.get(i);
                double[] state = averages.get(key);
                if (state != null && state[1] > 0) value = (int) Math.round(state[0] / state[1]);
                label = monthLabels.containsKey(key) ? monthLabels.get(key) : "-";
            }
            setBarHeight(findViewById(barIds[i]), value);
            TextView tvLabel = findViewById(labelIds[i]);
            if (tvLabel != null) tvLabel.setText(label);
            TextView headerLabel = findViewById(headerLabelIds[i]);
            if (headerLabel != null) headerLabel.setText(label);
        }
    }

    private void renderTable(List<PerkembanganRingkasanHistory> history,
                             List<PerkembanganRingkasanTrend> trends,
                             List<String> monthKeys) {
        clearRows();

        Map<String, Map<String, Integer>> valuesByAspect = new LinkedHashMap<>();
        Map<String, String> aspectNames = new LinkedHashMap<>();
        if (history != null) {
            for (PerkembanganRingkasanHistory item : history) {
                if (item.getKode() == null || item.getBulanKey() == null || item.getRataRata() == null) continue;
                aspectNames.put(item.getKode(), valueOrDash(item.getAspek()));
                Map<String, Integer> values = valuesByAspect.get(item.getKode());
                if (values == null) {
                    values = new LinkedHashMap<>();
                    valuesByAspect.put(item.getKode(), values);
                }
                values.put(item.getBulanKey(), (int) Math.round(item.getRataRata()));
            }
        }

        if (trends != null) {
            for (PerkembanganRingkasanTrend trend : trends) {
                if (trend.getKode() != null) aspectNames.put(trend.getKode(), valueOrDash(trend.getNama()));
            }
        }

        if (aspectNames.isEmpty()) {
            showTableMessage("Belum ada data perkembangan");
            return;
        }

        for (Map.Entry<String, String> aspect : aspectNames.entrySet()) {
            addAspectRow(
                    aspect.getValue(),
                    valuesByAspect.get(aspect.getKey()),
                    monthKeys,
                    getTrendText(trends, aspect.getKey())
            );
        }
    }

    private String getTrendText(List<PerkembanganRingkasanTrend> trends, String kode) {
        if (trends == null) return "-";
        for (PerkembanganRingkasanTrend trend : trends) {
            if (kode.equals(trend.getKode())) return valueOrDash(trend.getTrend());
        }
        return "-";
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

    private String formatMonthLabel(String label, String key) {
        if (label != null && !label.trim().isEmpty()) {
            String lower = label.toLowerCase(Locale.US);
            switch (lower) {
                case "jan": return "Jan";
                case "feb": return "Feb";
                case "mar": return "Mar";
                case "apr": return "Apr";
                case "may": return "Mei";
                case "jun": return "Jun";
                case "jul": return "Jul";
                case "aug": return "Agu";
                case "sep": return "Sep";
                case "oct": return "Okt";
                case "nov": return "Nov";
                case "dec": return "Des";
                default: return label;
            }
        }
        if (key != null && key.length() >= 7) return key.substring(5, 7);
        return "-";
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
