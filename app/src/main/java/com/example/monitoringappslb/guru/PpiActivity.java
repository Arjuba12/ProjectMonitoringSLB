package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.adapter.PpiAdapter;
import com.example.monitoringappslb.data.DummyDatabase;
import com.example.monitoringappslb.model.Ppi;
import java.util.List;

public class PpiActivity extends BaseGuruActivity implements PpiAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PpiAdapter adapter;
    private List<Ppi> ppiList;
    private TextView tvDetailTitle, tvTargetAkademik, tvTargetPerilaku, tvTargetSosial, tvTargetMotorik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppi);

        DrawerLayout dl = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.rv_ppi_list);
        View btnTambahPpi = findViewById(R.id.btn_tambah_ppi);

        if (btnTambahPpi != null) {
            btnTambahPpi.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, BuatPpiActivity.class);
                startActivity(intent);
            });
        }

        // Inisialisasi Bottom Navigation
        setupBottomNav(-1);

        // Setup Hamburger Menu & Side Navigation
        setupNavigationDrawer(dl);

        // Load Dummy Data
        loadDummyData();

        // Setup RecyclerView
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PpiAdapter(ppiList, this);
            recyclerView.setAdapter(adapter);
        }

        initDetailViews();
        
        // Show default detail (first student)
        if (!ppiList.isEmpty()) {
            updateDetailPpi(ppiList.get(0));
        }
    }

    private void initDetailViews() {
        tvDetailTitle = findViewById(R.id.tv_detail_ppi_title);
        tvTargetAkademik = findViewById(R.id.tv_target_akademik);
        tvTargetPerilaku = findViewById(R.id.tv_target_perilaku);
        tvTargetSosial = findViewById(R.id.tv_target_sosial);
        tvTargetMotorik = findViewById(R.id.tv_target_motorik);
    }

    @Override
    public void onItemClick(Ppi ppi) {
        updateDetailPpi(ppi);
    }

    private void updateDetailPpi(Ppi ppi) {
        if (tvDetailTitle != null) tvDetailTitle.setText("Detail PPI - " + ppi.getStudentName());
        if (tvTargetAkademik != null) tvTargetAkademik.setText(ppi.getTargetAkademik());
        if (tvTargetPerilaku != null) tvTargetPerilaku.setText(ppi.getTargetPerilaku());
        if (tvTargetSosial != null) tvTargetSosial.setText(ppi.getTargetSosial());
        if (tvTargetMotorik != null) tvTargetMotorik.setText(ppi.getTargetMotorik());
    }

    private void loadDummyData() {
        ppiList = DummyDatabase.getPpiList();
    }
}