package com.example.monitoringappslb.guru;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.KelasItem;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.LaporanKelasData;
import com.example.monitoringappslb.model.response.ApiModels.LaporanKelasResponse;
import com.example.monitoringappslb.model.response.ApiModels.LaporanKelasSiswa;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaporanKelasActivity extends BaseGuruActivity {

    private Spinner spinnerMonth;
    private Spinner spinnerClass;
    private TableLayout tableAttendance;
    private LinearLayout containerGraph;
    private EditText etNarrativeNote;
    private ApiService apiService;
    private final List<KelasItem> kelasList = new ArrayList<>();
    private final List<MonthOption> monthOptions = new ArrayList<>();
    private int selectedKelasId = -1;
    private MonthOption selectedMonth;
    private LaporanKelasData currentReportData;
    private int[] currentAverages = new int[]{0, 0, 0, 0, 0};
    private final ExecutorService printExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_kelas);

        apiService = ApiClient.getService();
        setupNavigation();
        initViews();
        setupSpinners();
        setupActions();
        loadKelas();
    }

    private void initViews() {
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerClass = findViewById(R.id.spinner_class);
        tableAttendance = findViewById(R.id.table_attendance);
        containerGraph = findViewById(R.id.container_graph);
        etNarrativeNote = findViewById(R.id.et_narrative_note);
    }

    private void setupSpinners() {
        setupMonthOptions();
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getMonthLabels()
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
        selectedMonth = monthOptions.get(0);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = monthOptions.get(position);
                if (selectedKelasId != -1) {
                    loadLaporanKelas();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupActions() {
        View btnSave = findViewById(R.id.btn_save);
        if (btnSave != null) {
            btnSave.setOnClickListener(v ->
                    Toast.makeText(this, "Catatan disimpan untuk laporan yang akan dicetak", Toast.LENGTH_SHORT).show());
        }

        View btnExportPdf = findViewById(R.id.btnExportPdf);
        if (btnExportPdf != null) {
            btnExportPdf.setOnClickListener(v -> generateLaporan());
        }

        View btnPreview = findViewById(R.id.btn_preview);
        if (btnPreview != null) {
            btnPreview.setOnClickListener(v -> generateLaporan());
        }
    }

    private void loadKelas() {
        apiService.getKelasSaya().enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null || response.body().getData().isEmpty()) {
                    Toast.makeText(LaporanKelasActivity.this, "Tidak ada kelas yang ditugaskan", Toast.LENGTH_SHORT).show();
                    renderEmptyTable();
                    setupChart(new int[]{0, 0, 0, 0, 0});
                    return;
                }

                kelasList.clear();
                kelasList.addAll(response.body().getData());
                setupClassSpinner();
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                Toast.makeText(LaporanKelasActivity.this, "Tidak bisa memuat kelas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClassSpinner() {
        List<String> labels = new ArrayList<>();
        for (KelasItem kelas : kelasList) {
            labels.add(kelas.getNamaKelas());
        }

        ArrayAdapter<String> classAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedKelasId = kelasList.get(position).getId();
                loadLaporanKelas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadLaporanKelas() {
        if (selectedKelasId == -1 || selectedMonth == null) return;

        apiService.getLaporanKelas(selectedKelasId, selectedMonth.month, selectedMonth.year)
                .enqueue(new Callback<LaporanKelasResponse>() {
                    @Override
                    public void onResponse(Call<LaporanKelasResponse> call, Response<LaporanKelasResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || !response.body().isSuccess() || response.body().getData() == null) {
                            Toast.makeText(LaporanKelasActivity.this, "Gagal memuat laporan kelas", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        bindLaporan(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<LaporanKelasResponse> call, Throwable t) {
                        Toast.makeText(LaporanKelasActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindLaporan(LaporanKelasData data) {
        currentReportData = data;
        List<LaporanKelasSiswa> siswaList = data.getSiswa();
        if (siswaList == null || siswaList.isEmpty()) {
            renderEmptyTable();
            currentAverages = new int[]{0, 0, 0, 0, 0};
            setupChart(currentAverages);
            if (etNarrativeNote != null) {
                etNarrativeNote.setText(valueOrDash(data.getCatatanNaratif()));
            }
            return;
        }

        renderAttendanceTable(siswaList);
        currentAverages = calculateAverageStats(siswaList);
        setupChart(currentAverages);
        if (etNarrativeNote != null) {
            etNarrativeNote.setText(valueOrDash(data.getCatatanNaratif()));
        }
    }

    private void renderAttendanceTable(List<LaporanKelasSiswa> siswaList) {
        clearAttendanceRows();

        for (LaporanKelasSiswa siswa : siswaList) {
            TableRow row = new TableRow(this);
            row.setPadding(0, 0, 0, 8);

            row.addView(createTableCell(valueOrDash(siswa.getNama()), false));
            row.addView(createTableCell(String.valueOf(siswa.getHadir()), true));
            row.addView(createTableCell(String.valueOf(siswa.getSakit()), true));
            row.addView(createTableCell(String.valueOf(siswa.getIzin()), true));
            row.addView(createTableCell(String.valueOf(siswa.getAlpha()), true));

            tableAttendance.addView(row);

            View line = new View(this);
            line.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            line.setBackgroundColor(getResources().getColor(R.color.gray_light, getTheme()));
            tableAttendance.addView(line);
        }
    }

    private void renderEmptyTable() {
        clearAttendanceRows();
        TableRow row = new TableRow(this);
        TextView tv = createTableCell("Belum ada data laporan", false);
        tv.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        row.addView(tv);
        tableAttendance.addView(row);
    }

    private void clearAttendanceRows() {
        int childCount = tableAttendance.getChildCount();
        if (childCount > 2) {
            tableAttendance.removeViews(2, childCount - 2);
        }
    }

    private int[] calculateAverageStats(List<LaporanKelasSiswa> siswaList) {
        double[] totals = new double[5];
        int[] counts = new int[5];

        for (LaporanKelasSiswa siswa : siswaList) {
            addStat(totals, counts, 0, siswa.getKognitif());
            addStat(totals, counts, 1, siswa.getSosial());
            addStat(totals, counts, 2, siswa.getMotorik());
            addStat(totals, counts, 3, siswa.getKomunikasi());
            addStat(totals, counts, 4, siswa.getBinaDiri());
        }

        int[] averages = new int[5];
        for (int i = 0; i < averages.length; i++) {
            averages[i] = counts[i] == 0 ? 0 : (int) Math.round(totals[i] / counts[i]);
        }
        return averages;
    }

    private void addStat(double[] totals, int[] counts, int index, Double value) {
        if (value == null) return;
        totals[index] += value;
        counts[index]++;
    }

    private void setupChart(int[] stats) {
        containerGraph.removeAllViews();

        for (int stat : stats) {
            LinearLayout barContainer = new LinearLayout(this);
            barContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            ));
            barContainer.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            barContainer.setOrientation(LinearLayout.VERTICAL);

            TextView tvValue = new TextView(this);
            tvValue.setText(stat + "%");
            tvValue.setTextSize(9);
            tvValue.setTextColor(0xFF7F8C8D);
            tvValue.setGravity(android.view.Gravity.CENTER);
            tvValue.setPadding(0, 0, 0, 4);
            barContainer.addView(tvValue);

            View bar = new View(this);
            int heightPx = (int) (stat * getResources().getDisplayMetrics().density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (28 * getResources().getDisplayMetrics().density),
                    heightPx
            );
            bar.setLayoutParams(params);

            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();
            shape.setCornerRadii(new float[]{15, 15, 15, 15, 0, 0, 0, 0});

            if (stat >= 80) {
                shape.setColor(0xFF2E7D32);
            } else if (stat >= 60) {
                shape.setColor(0xFFE67E22);
            } else {
                shape.setColor(0xFFC0392B);
            }

            bar.setBackground(shape);

            barContainer.addView(bar);
            containerGraph.addView(barContainer);
        }
    }

    private TextView createTableCell(String text, boolean center) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(11);
        tv.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        tv.setPadding(0, 12, 0, 12);

        if (center) {
            tv.setGravity(android.view.Gravity.CENTER);
            int widthPx = (int) (50 * getResources().getDisplayMetrics().density);
            tv.setLayoutParams(new TableRow.LayoutParams(widthPx, TableRow.LayoutParams.WRAP_CONTENT));
        } else {
            tv.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
        }
        return tv;
    }

    private void generateLaporan() {
        if (selectedKelasId == -1 || selectedMonth == null) {
            Toast.makeText(this, "Pilih kelas terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("tipe", "Kelas");
        body.put("periode", selectedMonth.label);
        body.put("kelas_id", selectedKelasId);
        body.put("tahun_ajaran", getSelectedTahunAjaran());

        apiService.generateLaporan(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    printCurrentReport();
                } else {
                    Toast.makeText(LaporanKelasActivity.this, "Gagal membuat laporan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(LaporanKelasActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void printCurrentReport() {
        if (currentReportData == null) {
            Toast.makeText(this, "Data laporan belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Menyiapkan laporan...", Toast.LENGTH_SHORT).show();
        printExecutor.execute(() -> {
            try {
                File pdf = createReportPdf();
                runOnUiThread(() -> printPdfFile(pdf));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        LaporanKelasActivity.this,
                        "Gagal menyiapkan PDF: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
            }
        });
    }

    private File createReportPdf() throws IOException {
        KelasItem kelas = currentReportData.getKelas();
        List<LaporanKelasSiswa> siswa = currentReportData.getSiswa();
        String kelasName = kelas != null ? valueOrDash(kelas.getNamaKelas()) : "-";
        String tahunAjaran = getSelectedTahunAjaran();
        String catatan = etNarrativeNote != null
                ? etNarrativeNote.getText().toString()
                : currentReportData.getCatatanNaratif();

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
        title.setColor(Color.parseColor("#2C3E50"));
        title.setTextSize(20);
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.parseColor("#1F2933"));
        text.setTextSize(10);

        Paint bold = new Paint(text);
        bold.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.parseColor("#D9E2EC"));
        line.setStrokeWidth(1);

        int y = 42;
        canvas.drawText("Laporan Perkembangan Kelas", 40, y, title);
        y += 24;
        canvas.drawText("Kelas: " + kelasName, 40, y, text);
        canvas.drawText("Periode: " + selectedMonth.label, 210, y, text);
        canvas.drawText("Tahun Ajaran: " + tahunAjaran, 390, y, text);
        y += 16;
        canvas.drawLine(40, y, 555, y, line);

        y += 26;
        String[] labels = {"Kognitif", "Sosial", "Motorik", "Komunikasi", "Bina Diri"};
        for (int i = 0; i < labels.length; i++) {
            int x = 40 + (i * 103);
            canvas.drawRect(x, y, x + 90, y + 48, line);
            canvas.drawText(labels[i], x + 8, y + 18, text);
            canvas.drawText(currentAverages[i] + "%", x + 8, y + 38, bold);
        }

        y += 78;
        canvas.drawText("Rekap Kehadiran dan Capaian Siswa", 40, y, bold);
        y += 14;
        drawTableHeader(canvas, y, bold);
        y += 20;

        if (siswa == null || siswa.isEmpty()) {
            canvas.drawText("Belum ada data laporan", 44, y + 14, text);
            y += 28;
        } else {
            for (LaporanKelasSiswa item : siswa) {
                if (y > 700) break;
                drawStudentRow(canvas, y, item, text, line);
                y += 22;
            }
        }

        y += 24;
        canvas.drawText("Catatan Naratif", 40, y, bold);
        y += 16;
        y = drawWrappedText(canvas, valueOrDash(catatan), 40, y, 515, text, 14);

        y = Math.max(y + 42, 710);
        canvas.drawText("Guru Kelas", 415, y, text);
        canvas.drawLine(385, y + 58, 535, y + 58, line);

        document.finishPage(page);

        File file = new File(getCacheDir(), "laporan_kelas.pdf");
        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
        } finally {
            document.close();
        }
        return file;
    }

    private void drawTableHeader(Canvas canvas, int y, Paint paint) {
        canvas.drawText("Siswa", 44, y, paint);
        canvas.drawText("H", 190, y, paint);
        canvas.drawText("S", 220, y, paint);
        canvas.drawText("I", 250, y, paint);
        canvas.drawText("A", 280, y, paint);
        canvas.drawText("Kog", 320, y, paint);
        canvas.drawText("Sos", 365, y, paint);
        canvas.drawText("Mot", 410, y, paint);
        canvas.drawText("Kom", 455, y, paint);
        canvas.drawText("BD", 510, y, paint);
    }

    private void drawStudentRow(Canvas canvas, int y, LaporanKelasSiswa item, Paint text, Paint line) {
        canvas.drawLine(40, y - 10, 555, y - 10, line);
        canvas.drawText(valueOrDash(item.getNama()), 44, y + 5, text);
        canvas.drawText(String.valueOf(item.getHadir()), 190, y + 5, text);
        canvas.drawText(String.valueOf(item.getSakit()), 220, y + 5, text);
        canvas.drawText(String.valueOf(item.getIzin()), 250, y + 5, text);
        canvas.drawText(String.valueOf(item.getAlpha()), 280, y + 5, text);
        canvas.drawText(percent(item.getKognitif()), 320, y + 5, text);
        canvas.drawText(percent(item.getSosial()), 365, y + 5, text);
        canvas.drawText(percent(item.getMotorik()), 410, y + 5, text);
        canvas.drawText(percent(item.getKomunikasi()), 455, y + 5, text);
        canvas.drawText(percent(item.getBinaDiri()), 510, y + 5, text);
    }

    private int drawWrappedText(Canvas canvas, String value, int x, int y, int maxWidth, Paint paint, int lineHeight) {
        String[] words = value.split("\\s+");
        StringBuilder lineBuilder = new StringBuilder();
        for (String word : words) {
            String candidate = lineBuilder.length() == 0 ? word : lineBuilder + " " + word;
            if (paint.measureText(candidate) > maxWidth) {
                canvas.drawText(lineBuilder.toString(), x, y, paint);
                y += lineHeight;
                lineBuilder = new StringBuilder(word);
            } else {
                lineBuilder = new StringBuilder(candidate);
            }
        }
        if (lineBuilder.length() > 0) {
            canvas.drawText(lineBuilder.toString(), x, y, paint);
            y += lineHeight;
        }
        return y;
    }

    private String percent(Double value) {
        return value == null ? "-" : Math.round(value) + "%";
    }

    private void printPdfFile(File file) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            Toast.makeText(this, "Layanan print tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String kelas = currentReportData.getKelas() != null
                ? currentReportData.getKelas().getNamaKelas()
                : "Kelas";
        String jobName = "Laporan_" + kelas + "_" + selectedMonth.label.replace(" ", "_");
        printManager.print(jobName, new PdfFilePrintAdapter(file, jobName), new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build());
    }

    private static class PdfFilePrintAdapter extends PrintDocumentAdapter {
        private final File file;
        private final String jobName;

        PdfFilePrintAdapter(File file, String jobName) {
            this.file = file;
            this.jobName = jobName;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal, LayoutResultCallback callback,
                             Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }
            PrintDocumentInfo info = new PrintDocumentInfo.Builder(jobName + ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                            CancellationSignal cancellationSignal, WriteResultCallback callback) {
            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        return;
                    }
                    out.write(buffer, 0, len);
                }
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (IOException e) {
                callback.onWriteFailed(e.getMessage());
            }
        }
    }

    private String getSelectedTahunAjaran() {
        if (currentReportData != null && currentReportData.getKelas() != null
                && currentReportData.getKelas().getTahunAjaran() != null) {
            return currentReportData.getKelas().getTahunAjaran();
        }
        for (KelasItem kelas : kelasList) {
            if (kelas.getId() == selectedKelasId && kelas.getTahunAjaran() != null) {
                return kelas.getTahunAjaran();
            }
        }
        return "-";
    }

    private void setupMonthOptions() {
        monthOptions.clear();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            monthOptions.add(new MonthOption(
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR),
                    calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("id", "ID"))
                            + " " + calendar.get(Calendar.YEAR)
            ));
            calendar.add(Calendar.MONTH, -1);
        }
    }

    private List<String> getMonthLabels() {
        List<String> labels = new ArrayList<>();
        for (MonthOption option : monthOptions) {
            labels.add(option.label);
        }
        return labels;
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private static class MonthOption {
        final int month;
        final int year;
        final String label;

        MonthOption(int month, int year, String label) {
            this.month = month;
            this.year = year;
            this.label = label;
        }
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout);
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
        return R.id.nav_laporan;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }
}
