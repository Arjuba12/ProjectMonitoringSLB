package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.navigation.NavigationView;
import androidx.cardview.widget.CardView;

public class ChatActivity extends BaseGuruActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        CardView chatCard1 = findViewById(R.id.card_chat_1);

        // Inisialisasi Bottom Navigation
        setupBottomNav(R.id.nav_chat);

        // Setup Hamburger Menu
        setupNavigationDrawer(drawerLayout);

        // Open Detail on Click
        if (chatCard1 != null) {
            chatCard1.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatDetailActivity.class);
                startActivity(intent);
            });
        }
    }
}