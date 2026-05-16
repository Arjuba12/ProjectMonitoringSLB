package com.example.monitoringappslb.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.monitoringappslb.util.AvatarUtils;
import java.util.List;

public class DaftarSiswaAdapter extends RecyclerView.Adapter<DaftarSiswaAdapter.ViewHolder> {

    private List<Siswa> siswaList;
    private Context context;
    private static final String TAG = "DaftarSiswaAdapter";

    public DaftarSiswaAdapter(List<Siswa> siswaList, Context context) {
        this.siswaList = siswaList;
        this.context = context;
        Log.d(TAG, "Adapter initialized with " + (siswaList != null ? siswaList.size() : 0) + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_siswa_daftar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (siswaList == null || position >= siswaList.size()) {
            Log.w(TAG, "onBindViewHolder: data null or out of bounds at " + position);
            return;
        }

        Siswa siswa = siswaList.get(position);
        if (siswa == null) {
            Log.w(TAG, "onBindViewHolder: siswa at " + position + " is null");
            return;
        }

        Log.d(TAG, "Binding siswa: " + siswa.getNama() + " at position " + position);

        if (holder.tvNama != null) {
            holder.tvNama.setText(siswa.getNama() != null ? siswa.getNama() : "-");
        }

        AvatarUtils.applyInitialAvatar(holder.tvInitials, siswa.getNama(), siswa.getNisn());

        if (holder.tvDetail != null) {
            holder.tvDetail.setText("NISN: " + (siswa.getNisn() != null ? siswa.getNisn() : "-"));
        }

        if (holder.btnDetail != null) {
            holder.btnDetail.setOnClickListener(v -> {
                if (siswa.getId() != null) {
                    Intent intent = new Intent(context, DetailSiswaActivity.class);
                    intent.putExtra("SISWA_ID", siswa.getId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        int count = (siswaList != null) ? siswaList.size() : 0;
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitials, tvNama, tvDetail;
        Button btnDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_siswa_initials);
            tvNama = itemView.findViewById(R.id.tv_nama_siswa);
            tvDetail = itemView.findViewById(R.id.tv_nisn);
            btnDetail = itemView.findViewById(R.id.btn_detail_siswa);

            if (tvDetail == null) {
                Log.e(TAG, "ViewHolder: tvDetail (tv_nisn) not found in layout!");
            }
        }
    }
}
