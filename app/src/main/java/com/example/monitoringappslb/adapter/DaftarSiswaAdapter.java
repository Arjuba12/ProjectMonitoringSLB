package com.example.monitoringappslb.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.guru.DetailSiswaActivity;
import com.example.monitoringappslb.model.Siswa;
import java.util.List;

public class DaftarSiswaAdapter extends RecyclerView.Adapter<DaftarSiswaAdapter.ViewHolder> {

    private List<Siswa> siswaList;
    private Context context;

    public DaftarSiswaAdapter(List<Siswa> siswaList, Context context) {
        this.siswaList = siswaList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_siswa_daftar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Siswa siswa = siswaList.get(position);
        holder.tvNama.setText(siswa.getNama());
        holder.tvDetail.setText("NISN: " + siswa.getNisn());

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailSiswaActivity.class);
            intent.putExtra("SISWA_ID", siswa.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return siswaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDetail;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_nama_siswa);
            tvDetail = itemView.findViewById(R.id.tv_detail_siswa);
            btnDetail = itemView.findViewById(R.id.btn_detail_siswa);
        }
    }
}