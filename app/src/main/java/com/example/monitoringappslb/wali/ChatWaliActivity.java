package com.example.monitoringappslb.wali;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.guru.ChatDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ChatWaliActivity extends BaseWaliActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_wali);

        setupNavigation();
        setupChatListData();
    }

    private void setupChatListData() {
        // Chat 1
        View chat1 = findViewById(R.id.chat_1);
        if (chat1 != null) {
            String name = "Bu Hartini (Wali Kelas)";
            ((TextView) chat1.findViewById(R.id.chat_name)).setText(name);
            ((TextView) chat1.findViewById(R.id.chat_last_message)).setText("Andi hari ini tampak lebih ceria...");
            ((TextView) chat1.findViewById(R.id.chat_time)).setText("10.30");
            
            chat1.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatDetailWaliActivity.class);
                intent.putExtra("CHAT_NAME", name);
                startActivity(intent);
            });
        }

        // Chat 2
        View chat2 = findViewById(R.id.chat_2);
        if (chat2 != null) {
            String name = "Pak Bambang (Guru Olahraga)";
            ((TextView) chat2.findViewById(R.id.chat_name)).setText(name);
            ((TextView) chat2.findViewById(R.id.chat_last_message)).setText("Besok ada kegiatan renang ya Pak.");
            ((TextView) chat2.findViewById(R.id.chat_time)).setText("Kemarin");

            chat2.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatDetailWaliActivity.class);
                intent.putExtra("CHAT_NAME", name);
                startActivity(intent);
            });
        }

        // Chat 3
        View chat3 = findViewById(R.id.chat_3);
        if (chat3 != null) {
            String name = "Ibu Sri (Guru Agama)";
            ((TextView) chat3.findViewById(R.id.chat_name)).setText(name);
            ((TextView) chat3.findViewById(R.id.chat_last_message)).setText("Tugas hafalan sudah selesai.");
            ((TextView) chat3.findViewById(R.id.chat_time)).setText("Senin");

            chat3.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatDetailWaliActivity.class);
                intent.putExtra("CHAT_NAME", name);
                startActivity(intent);
            });
        }
    }

    @Override
    protected DrawerLayout getDrawerLayout() {
        return findViewById(R.id.drawer_layout_wali);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view_wali);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return findViewById(R.id.bottom_navigation_wali);
    }

    @Override
    protected int getSelfNavDrawerItemId() {
        return R.id.nav_wali_chat;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_wali_chat;
    }
}