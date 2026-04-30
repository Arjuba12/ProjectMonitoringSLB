package com.example.monitoringappslb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.Ppi;
import java.util.List;

public class PpiAdapter extends RecyclerView.Adapter<PpiAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Ppi ppi);
    }

    private List<Ppi> ppiList;
    private OnItemClickListener listener;

    public PpiAdapter(List<Ppi> ppiList, OnItemClickListener listener) {
        this.ppiList = ppiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ppi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ppi ppi = ppiList.get(position);
        holder.tvNama.setText(ppi.getStudentName());
        holder.tvSmt.setText(ppi.getSemester());
        holder.tvTarget.setText(ppi.getMainTarget());
        holder.progressBar.setProgress(ppi.getProgress());
        holder.tvStatus.setText(ppi.getStatus());
        
        if (ppi.getProgress() > 70) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF2E7D32));
        } else if (ppi.getProgress() > 30) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFE67E22));
        } else {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFC0392B));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(ppi);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ppiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvSmt, tvTarget, tvStatus;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_ppi_nama);
            tvSmt = itemView.findViewById(R.id.tv_ppi_smt);
            tvTarget = itemView.findViewById(R.id.tv_ppi_target);
            progressBar = itemView.findViewById(R.id.pb_ppi_progress);
            tvStatus = itemView.findViewById(R.id.tv_ppi_status);
        }
    }
}