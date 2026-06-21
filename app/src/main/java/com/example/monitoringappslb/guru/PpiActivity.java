package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.monitoringappslb.R;
import com.example.monitoringappslb.adapter.PpiAdapter;
import com.example.monitoringappslb.model.Ppi;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.PpiDetailResponse;
import com.example.monitoringappslb.model.response.ApiModels.PpiItem;
import com.example.monitoringappslb.model.response.ApiModels.PpiListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PpiActivity extends BaseGuruActivity implements PpiAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PpiAdapter adapter;
    private final List<Ppi> ppiList = new ArrayList<>();
    private TextView tvDetailTitle;
    private TextView tvTargetAkademik;
    private TextView tvTargetPerilaku;
    private TextView tvTargetSosial;
    private TextView tvTargetMotorik;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppi);

        apiService = ApiClient.getService();
        recyclerView = findViewById(R.id.rv_ppi_list);
        View btnTambahPpi = findViewById(R.id.btn_tambah_ppi);

        if (btnTambahPpi != null) {
            btnTambahPpi.setOnClickListener(v ->
                    startActivity(new Intent(this, BuatPpiActivity.class)));
        }

        setupNavigation();
        initDetailViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null && adapter != null) {
            loadPpiData();
        }
    }

    private void initDetailViews() {
        tvDetailTitle = findViewById(R.id.tv_detail_ppi_title);
        tvTargetAkademik = findViewById(R.id.tv_target_akademik);
        tvTargetPerilaku = findViewById(R.id.tv_target_perilaku);
        tvTargetSosial = findViewById(R.id.tv_target_sosial);
        tvTargetMotorik = findViewById(R.id.tv_target_motorik);
    }

    private void setupRecyclerView() {
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PpiAdapter(ppiList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void loadPpiData() {
        apiService.getKelasSaya().enqueue(new Callback<KelasListResponse>() {
            @Override
            public void onResponse(Call<KelasListResponse> call, Response<KelasListResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getData() == null || response.body().getData().isEmpty()) {
                    showEmptyState("Belum ada kelas");
                    return;
                }

                int kelasId = response.body().getData().get(0).getId();
                loadPpiKelas(kelasId);
            }

            @Override
            public void onFailure(Call<KelasListResponse> call, Throwable t) {
                showEmptyState("Tidak bisa memuat kelas");
            }
        });
    }

    private void loadPpiKelas(int kelasId) {
        apiService.getPpiKelas(kelasId).enqueue(new Callback<PpiListResponse>() {
            @Override
            public void onResponse(Call<PpiListResponse> call, Response<PpiListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(PpiActivity.this, "Gagal memuat Program Pembelajaran Individual", Toast.LENGTH_SHORT).show();
                    showEmptyState("Gagal memuat Program Pembelajaran Individual");
                    return;
                }

                bindPpiList(response.body().getData());
            }

            @Override
            public void onFailure(Call<PpiListResponse> call, Throwable t) {
                Toast.makeText(PpiActivity.this, "Tidak bisa terhubung ke server", Toast.LENGTH_SHORT).show();
                showEmptyState("Tidak bisa memuat Program Pembelajaran Individual");
            }
        });
    }

    private void bindPpiList(List<PpiItem> apiList) {
        ppiList.clear();

        if (apiList != null) {
            for (PpiItem item : apiList) {
                Ppi ppi = new Ppi(
                        item.getId(),
                        valueOrDash(item.getNamaSiswa()),
                        "Smt " + valueOrDash(item.getSemester()),
                        valueOrDash(item.getTargetUtama()),
                        calculateProgress(item),
                        valueOrDash(item.getStatus()),
                        valueOrDash(item.getPotensi()),
                        valueOrDash(item.getHambatan()),
                        valueOrDash(item.getTargetAkademik()),
                        valueOrDash(item.getTargetBinaDiri()),
                        valueOrDash(item.getTargetKomunikasi()),
                        valueOrDash(item.getTargetMotorik())
                );
                ppi.setSiswaId(item.getSiswaId());
                ppiList.add(ppi);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        if (ppiList.isEmpty()) {
            showEmptyState("Belum ada Program Pembelajaran Individual");
        } else {
            updateDetailPpi(ppiList.get(0));
            loadPpiDetail(ppiList.get(0).getId());
        }
    }

    private void loadPpiDetail(int ppiId) {
        apiService.getPpiById(ppiId).enqueue(new Callback<PpiDetailResponse>() {
            @Override
            public void onResponse(Call<PpiDetailResponse> call, Response<PpiDetailResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess() || response.body().getData() == null) {
                    return;
                }

                updateDetailPpi(response.body().getData());
            }

            @Override
            public void onFailure(Call<PpiDetailResponse> call, Throwable t) {
                Toast.makeText(PpiActivity.this, "Gagal memuat detail Program Pembelajaran Individual", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateProgress(PpiItem item) {
        if (item.getTotalTarget() <= 0) return 0;
        return (item.getTercapai() * 100) / item.getTotalTarget();
    }

    private void showEmptyState(String message) {
        ppiList.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
        if (tvDetailTitle != null) tvDetailTitle.setText(message);
        if (tvTargetAkademik != null) tvTargetAkademik.setText("-");
        if (tvTargetPerilaku != null) tvTargetPerilaku.setText("-");
        if (tvTargetSosial != null) tvTargetSosial.setText("-");
        if (tvTargetMotorik != null) tvTargetMotorik.setText("-");
    }

    @Override
    public void onItemClick(Ppi ppi) {
        updateDetailPpi(ppi);
        loadPpiDetail(ppi.getId());
    }

    private void updateDetailPpi(Ppi ppi) {
        if (tvDetailTitle != null) {
            tvDetailTitle.setText("Detail Program Pembelajaran Individual - " + ppi.getStudentName());
        }
        if (tvTargetAkademik != null) {
            tvTargetAkademik.setText(ppi.getTargetAkademik());
        }
        if (tvTargetPerilaku != null) {
            tvTargetPerilaku.setText(ppi.getTargetPerilaku());
        }
        if (tvTargetSosial != null) {
            tvTargetSosial.setText(ppi.getTargetSosial());
        }
        if (tvTargetMotorik != null) {
            tvTargetMotorik.setText(ppi.getTargetMotorik());
        }
    }

    private void updateDetailPpi(PpiItem ppi) {
        if (tvDetailTitle != null) {
            tvDetailTitle.setText("Detail Program Pembelajaran Individual - " + valueOrDash(ppi.getNamaSiswa()));
        }
        if (tvTargetAkademik != null) {
            tvTargetAkademik.setText(valueOrDash(ppi.getTargetAkademik()));
        }
        if (tvTargetPerilaku != null) {
            tvTargetPerilaku.setText(valueOrDash(ppi.getTargetBinaDiri()));
        }
        if (tvTargetSosial != null) {
            tvTargetSosial.setText(valueOrDash(ppi.getTargetKomunikasi()));
        }
        if (tvTargetMotorik != null) {
            tvTargetMotorik.setText(valueOrDash(ppi.getTargetMotorik()));
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
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
        return R.id.nav_ppi;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return -1;
    }
}
