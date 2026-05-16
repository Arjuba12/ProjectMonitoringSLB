package com.example.monitoringappslb.kepsek;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.SiswaRekapItem;
import com.example.monitoringappslb.model.response.ApiModels.SiswaRekapResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RekapSekolahActivity extends BaseKepsekActivity {

    private static final String[] HEADERS = {
            "Kelas", "Jml Siswa", "Hadir Rata", "Kognitif", "Sosial",
            "Motorik", "Komunikasi", "Bina Diri", "Status"
    };

    private ApiService apiService;
    private TableLayout tableRekap;
    private TextView tvStatus, tvPeriode;
    private final List<KelasRekap> currentRekap = new ArrayList<>();
    private int bulan;
    private int tahun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap_sekolah_kepsek);

        apiService = ApiClient.getService();
        tableRekap = findViewById(R.id.table_rekap_sekolah);
        tvStatus = findViewById(R.id.tv_rekap_status);
        tvPeriode = findViewById(R.id.tv_rekap_periode);

        setupNavigation();
        findViewById(R.id.btn_ekspor_excel).setOnClickListener(v -> exportToExcel());
        loadRekap();
    }

    private void loadRekap() {
        Calendar calendar = Calendar.getInstance();
        bulan = calendar.get(Calendar.MONTH) + 1;
        tahun = calendar.get(Calendar.YEAR);

        if (tvPeriode != null) {
            tvPeriode.setText("Rekap " + monthName(bulan) + " " + tahun);
        }
        setStatus("Memuat data rekap...", true);
        renderHeaderOnly();

        apiService.getSiswaRekap(null, bulan, tahun).enqueue(new Callback<SiswaRekapResponse>() {
            @Override
            public void onResponse(Call<SiswaRekapResponse> call, Response<SiswaRekapResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    setStatus("Gagal memuat data rekap", true);
                    Toast.makeText(RekapSekolahActivity.this, "Gagal memuat data rekap", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindRekap(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaRekapResponse> call, Throwable t) {
                setStatus("Tidak bisa terhubung ke server", true);
                Toast.makeText(RekapSekolahActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindRekap(List<SiswaRekapItem> siswaList) {
        currentRekap.clear();
        renderHeaderOnly();

        if (siswaList == null || siswaList.isEmpty()) {
            setStatus("Belum ada data rekap bulan ini", true);
            addEmptyRow("Belum ada data rekap");
            return;
        }

        Map<String, KelasRekap> grouped = new LinkedHashMap<>();
        for (SiswaRekapItem siswa : siswaList) {
            String kelas = orDash(siswa.getNamaKelas());
            KelasRekap rekap = grouped.get(kelas);
            if (rekap == null) {
                rekap = new KelasRekap(kelas);
                grouped.put(kelas, rekap);
            }
            rekap.add(siswa);
        }

        currentRekap.addAll(grouped.values());
        if (currentRekap.isEmpty()) {
            setStatus("Belum ada data rekap bulan ini", true);
            addEmptyRow("Belum ada data rekap");
            return;
        }

        setStatus("", false);
        for (KelasRekap rekap : currentRekap) {
            tableRekap.addView(createDataRow(rekap));
        }
    }

    private void renderHeaderOnly() {
        if (tableRekap == null) return;
        tableRekap.removeAllViews();

        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
        for (String header : HEADERS) {
            TextView cell = createCell(header, true);
            cell.setTextColor(Color.parseColor("#94A3B8"));
            cell.setTypeface(null, Typeface.BOLD);
            row.addView(cell);
        }
        tableRekap.addView(row);

        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#F1F5F9"));
        tableRekap.addView(line, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private TableRow createDataRow(KelasRekap rekap) {
        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
        row.addView(createCell(rekap.namaKelas, false));
        row.addView(createCell(String.valueOf(rekap.jumlahSiswa), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalHadir, rekap.countHadir)), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalKognitif, rekap.countKognitif)), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalSosial, rekap.countSosial)), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalMotorik, rekap.countMotorik)), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalKomunikasi, rekap.countKomunikasi)), true));
        row.addView(createMetricCell(formatPercent(rekap.avg(rekap.totalBinaDiri, rekap.countBinaDiri)), true));
        row.addView(createStatusCell(rekap.status()));
        return row;
    }

    private void addEmptyRow(String message) {
        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
        row.addView(createCell(message, false));
        tableRekap.addView(row);
    }

    private TextView createMetricCell(String text, boolean center) {
        TextView tv = createCell(text, center);
        if (!"-".equals(text)) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(metricColor(parsePercent(text)));
        }
        return tv;
    }

    private TextView createStatusCell(String status) {
        TextView tv = createCell(status, true);
        tv.setTypeface(null, Typeface.BOLD);
        if ("Baik".equals(status)) {
            tv.setTextColor(Color.parseColor("#166534"));
        } else if ("Cukup".equals(status)) {
            tv.setTextColor(Color.parseColor("#E67E22"));
        } else if ("Perlu perhatian".equals(status)) {
            tv.setTextColor(Color.parseColor("#EF4444"));
        }
        return tv;
    }

    private TextView createCell(String text, boolean center) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(Color.parseColor("#334155"));
        tv.setPadding(dp(14), dp(12), dp(14), dp(12));
        if (center) tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private void exportToExcel() {
        if (currentRekap.isEmpty()) {
            Toast.makeText(this, "Belum ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rekap Sekolah");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            headerRow.createCell(i).setCellValue(HEADERS[i]);
        }

        int rowNum = 1;
        for (KelasRekap rekap : currentRekap) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rekap.namaKelas);
            row.createCell(1).setCellValue(rekap.jumlahSiswa);
            row.createCell(2).setCellValue(formatPercent(rekap.avg(rekap.totalHadir, rekap.countHadir)));
            row.createCell(3).setCellValue(formatPercent(rekap.avg(rekap.totalKognitif, rekap.countKognitif)));
            row.createCell(4).setCellValue(formatPercent(rekap.avg(rekap.totalSosial, rekap.countSosial)));
            row.createCell(5).setCellValue(formatPercent(rekap.avg(rekap.totalMotorik, rekap.countMotorik)));
            row.createCell(6).setCellValue(formatPercent(rekap.avg(rekap.totalKomunikasi, rekap.countKomunikasi)));
            row.createCell(7).setCellValue(formatPercent(rekap.avg(rekap.totalBinaDiri, rekap.countBinaDiri)));
            row.createCell(8).setCellValue(rekap.status());
        }

        for (int i = 0; i < HEADERS.length; i++) {
            sheet.setColumnWidth(i, 4200);
        }

        try {
            File file = new File(getExternalFilesDir(null),
                    "Rekap_Sekolah_" + tahun + "_" + String.format(Locale.US, "%02d", bulan) + ".xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            Toast.makeText(this, "Excel disimpan: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Gagal ekspor excel", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPercent(Double value) {
        if (value == null) return "-";
        return Math.round(value) + "%";
    }

    private int parsePercent(String text) {
        try {
            return Integer.parseInt(text.replace("%", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int metricColor(int value) {
        if (value >= 75) return Color.parseColor("#166534");
        if (value >= 60) return Color.parseColor("#E67E22");
        return Color.parseColor("#EF4444");
    }

    private String orDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private void setStatus(String text, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(text);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String monthName(int month) {
        String[] names = {
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        return names[Math.max(0, Math.min(month - 1, names.length - 1))];
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override protected DrawerLayout getDrawerLayout() { return findViewById(R.id.drawer_layout_rekap_sekolah); }
    @Override protected NavigationView getNavigationView() { return findViewById(R.id.nav_view); }
    @Override protected BottomNavigationView getBottomNavigationView() { return findViewById(R.id.bottom_navigation); }
    @Override protected int getSelfNavDrawerItemId() { return R.id.nav_rekap; }
    @Override protected int getSelfBottomNavItemId() { return R.id.nav_rekap; }

    private static class KelasRekap {
        final String namaKelas;
        int jumlahSiswa;
        double totalHadir, totalKognitif, totalSosial, totalMotorik, totalKomunikasi, totalBinaDiri;
        int countHadir, countKognitif, countSosial, countMotorik, countKomunikasi, countBinaDiri;

        KelasRekap(String namaKelas) {
            this.namaKelas = namaKelas;
        }

        void add(SiswaRekapItem siswa) {
            jumlahSiswa++;
            addHadir(siswa.getHadirPersen());
            totalKognitif += addValue(siswa.getKognitif(), true);
            totalSosial += addValue(siswa.getSosial(), false);
            totalMotorik += addMotorik(siswa.getMotorik());
            totalKomunikasi += addKomunikasi(siswa.getKomunikasi());
            totalBinaDiri += addBinaDiri(siswa.getBinaDiri());
        }

        private void addHadir(Double value) {
            if (value == null) return;
            totalHadir += value;
            countHadir++;
        }

        private double addValue(Double value, boolean kognitif) {
            if (value == null) return 0;
            if (kognitif) countKognitif++; else countSosial++;
            return value;
        }

        private double addMotorik(Double value) {
            if (value == null) return 0;
            countMotorik++;
            return value;
        }

        private double addKomunikasi(Double value) {
            if (value == null) return 0;
            countKomunikasi++;
            return value;
        }

        private double addBinaDiri(Double value) {
            if (value == null) return 0;
            countBinaDiri++;
            return value;
        }

        Double avg(double total, int count) {
            return count == 0 ? null : total / count;
        }

        String status() {
            double total = 0;
            int count = 0;
            Double[] values = {
                    avg(totalKognitif, countKognitif),
                    avg(totalSosial, countSosial),
                    avg(totalMotorik, countMotorik),
                    avg(totalKomunikasi, countKomunikasi),
                    avg(totalBinaDiri, countBinaDiri)
            };
            for (Double value : values) {
                if (value != null) {
                    total += value;
                    count++;
                }
            }
            if (count == 0) return "-";
            double average = total / count;
            if (average >= 75) return "Baik";
            if (average >= 60) return "Cukup";
            return "Perlu perhatian";
        }
    }
}
