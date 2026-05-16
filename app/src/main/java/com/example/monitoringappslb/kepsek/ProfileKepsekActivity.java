package com.example.monitoringappslb.kepsek;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileKepsekActivity extends BaseKepsekActivity {

    private DatabaseReference mDatabase;
    private TextView tvNama, tvNIP, tvEmail, tvPhone, tvPangkat, tvPendidikan, tvAlamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_kepsek);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        tvNama = findViewById(R.id.tv_nama_kepsek);
        tvNIP = findViewById(R.id.tv_nip_kepsek);
        tvEmail = findViewById(R.id.tv_email_kepsek);
        tvPhone = findViewById(R.id.tv_phone_kepsek);
        tvPangkat = findViewById(R.id.tv_pangkat_kepsek);
        tvPendidikan = findViewById(R.id.tv_pendidikan_kepsek);
        tvAlamat = findViewById(R.id.tv_alamat_kepsek);

        // Setup Navigation
        setupNavigation();

        fetchProfileData();
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_profile);
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
        return R.id.nav_profile;
    }

    private void fetchProfileData() {
        // Since we don't have Auth yet, we'll fetch a placeholder or the first principal found
        // In a real app, this would use FirebaseAuth.getInstance().getCurrentUser().getUid()
        mDatabase.child("users").orderByChild("role").equalTo(1).limitToFirst(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String nama = userSnapshot.child("nama").getValue(String.class);
                            String nip = userSnapshot.child("nip").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);
                            String phone = userSnapshot.child("phone").getValue(String.class);
                            String pangkat = userSnapshot.child("pangkat").getValue(String.class);
                            String pendidikan = userSnapshot.child("pendidikan").getValue(String.class);
                            String alamat = userSnapshot.child("alamat").getValue(String.class);

                            if (nama != null) tvNama.setText(nama);
                            if (nip != null) tvNIP.setText(getString(R.string.nip_format, nip));
                            if (email != null) tvEmail.setText(email);
                            if (phone != null) tvPhone.setText(phone);
                            if (pangkat != null) tvPangkat.setText(pangkat);
                            if (pendidikan != null) tvPendidikan.setText(pendidikan);
                            if (alamat != null) tvAlamat.setText(alamat);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }
}
