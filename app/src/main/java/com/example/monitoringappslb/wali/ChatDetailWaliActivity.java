package com.example.monitoringappslb.wali;

import android.os.Bundle;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.monitoringappslb.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class ChatDetailWaliActivity extends BaseWaliActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail_wali);

        setupNavigation();
        
        String name = getIntent().getStringExtra("CHAT_NAME");
        if (name != null) {
            TextView tvChatWith = findViewById(R.id.tv_chat_with);
            if (tvChatWith != null) {
                tvChatWith.setText(name);
            }
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
        return null; // No bottom nav in detail chat
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