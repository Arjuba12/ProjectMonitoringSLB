package com.example.monitoringappslb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.Siswa;
import java.util.List;

public class AbsensiAdapter extends RecyclerView.Adapter<AbsensiAdapter.ViewHolder> {

    private List<Siswa> siswaList;
    private OnStatusChangeListener listener;

    public interface OnStatusChangeListener {
        void onStatusChanged();
    }

    public AbsensiAdapter(List<Siswa> siswaList, OnStatusChangeListener listener) {
        this.siswaList = siswaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_absensi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Siswa siswa = siswaList.get(position);
        holder.tvNama.setText(siswa.getNama());
        holder.tvNisn.setText("NISN: " + siswa.getNisn());

        // Reset listener to avoid triggering it when setting checked status
        holder.rgStatus.setOnCheckedChangeListener(null);

        switch (siswa.getStatusAbsensi()) {
            case "H": holder.rbHadir.setChecked(true); break;
            case "S": holder.rbSakit.setChecked(true); break;
            case "I": holder.rbIzin.setChecked(true); break;
            case "A": holder.rbAlpa.setChecked(true); break;
        }

        holder.rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_hadir) siswa.setStatusAbsensi("H");
            else if (checkedId == R.id.rb_sakit) siswa.setStatusAbsensi("S");
            else if (checkedId == R.id.rb_izin) siswa.setStatusAbsensi("I");
            else if (checkedId == R.id.rb_alpa) siswa.setStatusAbsensi("A");
            
            if (listener != null) {
                listener.onStatusChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return siswaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvNisn;
        RadioGroup rgStatus;
        RadioButton rbHadir, rbSakit, rbIzin, rbAlpa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_nama_absensi);
            tvNisn = itemView.findViewById(R.id.tv_nisn_absensi);
            rgStatus = itemView.findViewById(R.id.rg_status_absensi);
            rbHadir = itemView.findViewById(R.id.rb_hadir);
            rbSakit = itemView.findViewById(R.id.rb_sakit);
            rbIzin = itemView.findViewById(R.id.rb_izin);
            rbAlpa = itemView.findViewById(R.id.rb_alpa);
        }
    }
}