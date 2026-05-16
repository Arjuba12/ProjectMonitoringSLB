package com.example.monitoringappslb.wali;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetail;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetailResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.example.monitoringappslb.network.SessionManager;
import com.example.monitoringappslb.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BiodataSiswaWaliActivity extends BaseWaliActivity {
    private ApiService apiService;
    private SessionManager session;
    private TextView tvInitials, tvName, tvClass, tvNeed, tvAddress, tvGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biodata_siswa_wali);

        apiService = ApiClient.getService();
        session = new SessionManager(this);

        setupNavigation();
        initViews();
        setupLoadingState();
        loadStudentData();
    }

    private void initViews() {
        tvInitials = findViewById(R.id.tv_student_initials);
        tvName = findViewById(R.id.tv_student_name);
        tvClass = findViewById(R.id.tv_student_class);
        tvNeed = findViewById(R.id.tv_student_need);
        tvAddress = findViewById(R.id.tv_student_address);
        tvGender = findViewById(R.id.tv_student_gender);
    }

    private void setupLoadingState() {
        if (tvName != null) tvName.setText("Memuat...");
        if (tvClass != null) tvClass.setText("-");
        if (tvNeed != null) tvNeed.setText("-");
        if (tvAddress != null) tvAddress.setText("-");
        if (tvGender != null) tvGender.setText("-");

        setRow(R.id.row_nisn, "NISN", "-");
        setRow(R.id.row_tgl_lahir, "Tgl Lahir", "-");
        setRow(R.id.row_guru, "Tahun Ajaran", "-");
        setRow(R.id.row_tahun, "Tahun Masuk", "-");
        setRow(R.id.row_wali, "Wali", "-");
    }

    private void loadStudentData() {
        int siswaId = session.getSiswaId();
        if (siswaId == -1) {
            Toast.makeText(this, "ID siswa tidak ditemukan. Buka dashboard atau login ulang.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getSiswaById(siswaId).enqueue(new Callback<SiswaDetailResponse>() {
            @Override
            public void onResponse(Call<SiswaDetailResponse> call, Response<SiswaDetailResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    Toast.makeText(BiodataSiswaWaliActivity.this, "Gagal memuat biodata siswa", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindStudent(response.body().getData());
            }

            @Override
            public void onFailure(Call<SiswaDetailResponse> call, Throwable t) {
                Toast.makeText(BiodataSiswaWaliActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindStudent(SiswaDetail detail) {
        AvatarUtils.applyInitialAvatar(tvInitials, detail.getNama(), String.valueOf(detail.getId()));

        if (tvName != null) tvName.setText(valueOrDash(detail.getNama()));
        if (tvClass != null) tvClass.setText(buildClassText(detail));
        if (tvNeed != null) tvNeed.setText(valueOrDash(detail.getKebutuhanKhusus()));
        if (tvAddress != null) tvAddress.setText(valueOrDash(detail.getAlamat()));
        if (tvGender != null) tvGender.setText(formatGender(detail.getJenisKelamin()));

        setRow(R.id.row_nisn, "NISN", valueOrDash(detail.getNisn()));
        setRow(R.id.row_tgl_lahir, "Tgl Lahir", valueOrDash(detail.getTglLahir()));
        setRow(R.id.row_guru, "Tahun Ajaran", valueOrDash(detail.getTahunAjaran()));
        setRow(R.id.row_tahun, "Tahun Masuk", detail.getTahunMasuk() != null ? String.valueOf(detail.getTahunMasuk()) : "-");
        setRow(R.id.row_wali, "Wali", valueOrDash(detail.getNamaWali()));
    }

    private void setRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.label);
        TextView tvValue = row.findViewById(R.id.value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(valueOrDash(value));
    }

    private String buildClassText(SiswaDetail detail) {
        String kelas = valueOrDash(detail.getNamaKelas());
        String tahunAjaran = detail.getTahunAjaran();
        if (tahunAjaran == null || tahunAjaran.trim().isEmpty()) return kelas;
        return kelas + " - " + tahunAjaran;
    }

    private String formatGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) return "-";
        if ("L".equalsIgnoreCase(gender)) return "Laki-laki";
        if ("P".equalsIgnoreCase(gender)) return "Perempuan";
        return gender;
    }

    private String valueOrDash(String value) {
        return value != null && !value.trim().isEmpty() ? value : "-";
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_wali);
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
        return R.id.nav_wali_biodata;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_home;
    }
}
