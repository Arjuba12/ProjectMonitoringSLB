package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.Toast;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RekapSekolahActivity extends BaseKepsekActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap_sekolah_kepsek);

        setupNavigation();

        findViewById(R.id.btn_ekspor_excel).setOnClickListener(v -> exportToExcel());
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_rekap_sekolah);
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
        return R.id.nav_rekap;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_rekap;
    }

    private void exportToExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rekap Sekolah");

        String[] headers = {"Kelas", "Jml Siswa", "Hadir Rata", "Kognitif", "Sosial", "Motorik", "Komunikasi", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Dummy data based on UI
        Object[][] data = {
                {"VI-A", 8, "91%", "72%", "68%", "65%", "70%", "Baik"},
                {"VI-B", 8, "91%", "72%", "68%", "65%", "70%", "Cukup"},
                {"VII-A", 8, "91%", "72%", "68%", "65%", "70%", "Baik"}
        };

        int rowNum = 1;
        for (Object[] rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.length; i++) {
                if (rowData[i] instanceof String) {
                    row.createCell(i).setCellValue((String) rowData[i]);
                } else if (rowData[i] instanceof Integer) {
                    row.createCell(i).setCellValue((Integer) rowData[i]);
                }
            }
        }

        try {
            File file = new File(getExternalFilesDir(null), "Rekap_Sekolah.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            Toast.makeText(this, "Excel berhasil disimpan di: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal ekspor excel", Toast.LENGTH_SHORT).show();
        }
    }
}
