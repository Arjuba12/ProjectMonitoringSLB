package com.example.monitoringappslb.guru;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.PesanItem;
import com.example.monitoringappslb.model.response.ApiModels.PesanListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.util.AvatarUtils;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseGuruActivity {
    private static final long CONTACT_REFRESH_INTERVAL_MS = 5000L;
    private TextView tvStatus;
    private ChatContactAdapter adapter;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private Call<PesanListResponse> contactsCall;
    private boolean isActive;
    private boolean isLoadingContacts;
    private final List<PesanItem> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setupNavigation();
        tvStatus = findViewById(R.id.tv_chat_status);
        RecyclerView rvContacts = findViewById(R.id.rv_chat_contacts);
        adapter = new ChatContactAdapter(contacts, contact -> {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("USER_ID", contact.getId());
            intent.putExtra("USER_NAME", safe(contact.getNama()));
            intent.putExtra("STUDENT_NAME", safe(contact.getNamaSiswa()));
            startActivity(intent);
        });
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadContacts(false);
                if (isActive) {
                    refreshHandler.postDelayed(this, CONTACT_REFRESH_INTERVAL_MS);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        loadContacts(true);
        refreshHandler.postDelayed(refreshRunnable, CONTACT_REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        isLoadingContacts = false;
        refreshHandler.removeCallbacks(refreshRunnable);
        if (contactsCall != null) contactsCall.cancel();
    }

    private void loadContacts(boolean showLoading) {
        if (isLoadingContacts) return;
        isLoadingContacts = true;
        if (showLoading && contacts.isEmpty()) showStatus("Memuat kontak...", true);
        contactsCall = ApiClient.getService().getKontak();
        contactsCall.enqueue(new Callback<PesanListResponse>() {
            @Override
            public void onResponse(Call<PesanListResponse> call, Response<PesanListResponse> response) {
                isLoadingContacts = false;
                contactsCall = null;
                if (!isActive) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    if (showLoading || contacts.isEmpty()) showStatus("Kontak belum bisa dimuat", true);
                    return;
                }

                contacts.clear();
                if (response.body().getData() != null) {
                    contacts.addAll(response.body().getData());
                }
                adapter.notifyDataSetChanged();
                showStatus(contacts.isEmpty() ? "Belum ada wali murid yang bisa dihubungi" : "", contacts.isEmpty());
            }

            @Override
            public void onFailure(Call<PesanListResponse> call, Throwable t) {
                isLoadingContacts = false;
                contactsCall = null;
                if (call.isCanceled() || !isActive) return;
                if (showLoading || contacts.isEmpty()) {
                    showStatus("Gagal memuat kontak: " + t.getMessage(), true);
                }
            }
        });
    }

    private void showStatus(String text, boolean visible) {
        if (tvStatus == null) return;
        tvStatus.setText(text);
        tvStatus.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String formatTime(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        return DateTimeUtils.formatTime(value);
    }

    private static class ChatContactAdapter extends RecyclerView.Adapter<ChatContactAdapter.ViewHolder> {
        interface Listener {
            void onClick(PesanItem contact);
        }

        private final List<PesanItem> items;
        private final Listener listener;

        ChatContactAdapter(List<PesanItem> items, Listener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_contact, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PesanItem item = items.get(position);
            AvatarUtils.applyInitialAvatar(holder.tvInitials, item.getNama(), item.getNama());
            holder.tvName.setText(safe(item.getNama()));
            if ("wali".equalsIgnoreCase(item.getRole())) {
                holder.tvStudent.setText(item.getNamaSiswa() == null || item.getNamaSiswa().trim().isEmpty()
                        ? "Wali murid"
                        : "Wali: " + item.getNamaSiswa());
            } else {
                holder.tvStudent.setText(roleLabel(item.getRole()));
            }
            holder.tvLastMessage.setText(item.getLastMessage() == null || item.getLastMessage().trim().isEmpty()
                    ? "Belum ada percakapan"
                    : item.getLastMessage());
            holder.tvTime.setText(formatTime(item.getLastMessageAt()));

            int unread = item.getUnread();
            holder.tvUnread.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
            holder.tvUnread.setText(unread > 99 ? "99+" : String.valueOf(unread));
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInitials, tvName, tvStudent, tvLastMessage, tvTime, tvUnread;

            ViewHolder(View itemView) {
                super(itemView);
                tvInitials = itemView.findViewById(R.id.tv_contact_initials);
                tvName = itemView.findViewById(R.id.tv_contact_name);
                tvStudent = itemView.findViewById(R.id.tv_contact_student);
                tvLastMessage = itemView.findViewById(R.id.tv_contact_last_message);
                tvTime = itemView.findViewById(R.id.tv_contact_time);
                tvUnread = itemView.findViewById(R.id.tv_contact_unread);
            }
        }
    }

    private static String roleLabel(String role) {
        if ("kepsek".equalsIgnoreCase(role)) return "Kepala sekolah";
        if ("admin".equalsIgnoreCase(role)) return "Admin";
        if ("guru".equalsIgnoreCase(role)) return "Guru";
        return "Kontak";
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
        return -1;
    }

    @Override
    protected int getSelfBottomNavItemId() {
        return R.id.nav_chat;
    }
}
