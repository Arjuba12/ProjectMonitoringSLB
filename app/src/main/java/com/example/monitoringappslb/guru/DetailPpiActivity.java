package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Ppi;

public class DetailPpiActivity extends BaseGuruActivity {

    private TextView tvName, tvTargetAkademik, tvTargetPerilaku, tvTargetSosial, tvTargetMotorik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ppi);

        initViews();
        
        // Setup Navigation
        setupBottomNav(-1);
        setupNavigationDrawer(findViewById(R.id.drawer_layout));

        String studentName = getIntent().getStringExtra("STUDENT_NAME");
        if (studentName != null) {
            loadPpiData(studentName);
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_detail_ppi_name);
        tvTargetAkademik = findViewById(R.id.tv_target_akademik);
        tvTargetPerilaku = findViewById(R.id.tv_target_perilaku);
        tvTargetSosial = findViewById(R.id.tv_target_sosial);
        tvTargetMotorik = findViewById(R.id.tv_target_motorik);
    }

    private void loadPpiData(String studentName) {
        Ppi ppi = DummyDatabase.getPpiByStudentName(studentName);
        if (ppi != null) {
            tvName.setText("Detail PPI: " + ppi.getStudentName());
            tvTargetAkademik.setText(ppi.getTargetAkademik());
            tvTargetPerilaku.setText(ppi.getTargetPerilaku());
            tvTargetSosial.setText(ppi.getTargetSosial());
            tvTargetMotorik.setText(ppi.getTargetMotorik());
        }
    }
}