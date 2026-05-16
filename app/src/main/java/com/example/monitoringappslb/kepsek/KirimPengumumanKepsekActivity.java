package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KirimPengumumanKepsekActivity extends BaseKepsekActivity {

    private EditText etJudul, etPengumuman;
    private Button btnKirim;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_pengumuman_kepsek);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setupNavigation();

        // Inisialisasi View
        etJudul = findViewById(R.id.et_judul);
        etPengumuman = findViewById(R.id.et_isi);
        btnKirim = findViewById(R.id.btn_kirim);

        if (btnKirim != null) {
            btnKirim.setOnClickListener(v -> {
                kirimPengumuman();
            });
        }
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_pengumuman);
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
        return -1;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_announcement;
    }

    private void kirimPengumuman() {
        if (etJudul == null || etPengumuman == null) return;

        String judul = etJudul.getText().toString().trim();
        String pesan = etPengumuman.getText().toString().trim();

        if (judul.isEmpty()) {
            Toast.makeText(this, "Judul tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pesan.isEmpty()) {
            Toast.makeText(this, "Isi pengumuman tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        String pushId = mDatabase.child("announcements").push().getKey();
        if (pushId == null) return;

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("id", pushId);
        announcement.put("title", judul);
        announcement.put("message", pesan);
        announcement.put("date", date);
        announcement.put("sender", "Kepala Sekolah");
        announcement.put("target", "all");

        mDatabase.child("announcements").child(pushId).setValue(announcement)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pengumuman berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    etJudul.setText("");
                    etPengumuman.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengirim pengumuman: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
