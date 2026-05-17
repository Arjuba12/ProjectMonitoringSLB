package com.example.monitoringappslb.guru;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.monitoringappslb.R;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.PesanItem;
import com.example.monitoringappslb.model.response.ApiModels.PesanListResponse;
import com.example.monitoringappslb.network.ApiClient;
import com.example.monitoringappslb.util.DateTimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends BaseGuruActivity {
    private static final long MESSAGE_REFRESH_INTERVAL_MS = 3000L;
    private int userId;
    private String userName;
    private TextView tvTitle;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatMessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private Call<PesanListResponse> messagesCall;
    private boolean isActive;
    private boolean isLoadingMessages;
    private final List<PesanItem> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        setupNavigation();

        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        String studentName = getIntent().getStringExtra("STUDENT_NAME");
        ImageButton btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tv_chat_title);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        RecyclerView rvMessages = findViewById(R.id.rv_chat_messages);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvTitle != null) {
            String title = safe(userName);
            if (studentName != null && !studentName.trim().isEmpty() && !"-".equals(studentName)) {
                title += " - Wali " + studentName;
            }
            tvTitle.setText(title);
        }

        layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new ChatMessageAdapter(messages, userId);
        rvMessages.setAdapter(adapter);

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }

        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages(false);
                if (isActive) {
                    refreshHandler.postDelayed(this, MESSAGE_REFRESH_INTERVAL_MS);
                }
            }
        };

        if (userId <= 0) {
            Toast.makeText(this, "Kontak tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        if (userId > 0) {
            loadMessages(true);
            refreshHandler.postDelayed(refreshRunnable, MESSAGE_REFRESH_INTERVAL_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        isLoadingMessages = false;
        refreshHandler.removeCallbacks(refreshRunnable);
        if (messagesCall != null) messagesCall.cancel();
    }

    private void loadMessages(boolean showErrors) {
        if (isLoadingMessages) return;
        isLoadingMessages = true;
        messagesCall = ApiClient.getService().getPercakapan(userId);
        messagesCall.enqueue(new Callback<PesanListResponse>() {
            @Override
            public void onResponse(Call<PesanListResponse> call, Response<PesanListResponse> response) {
                isLoadingMessages = false;
                messagesCall = null;
                if (!isActive) return;
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    if (showErrors) {
                        Toast.makeText(ChatDetailActivity.this, "Percakapan belum bisa dimuat", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                boolean shouldScroll = isNearBottom();
                messages.clear();
                if (response.body().getData() != null) {
                    messages.addAll(response.body().getData());
                }
                adapter.notifyDataSetChanged();
                if (showErrors || shouldScroll) scrollToBottom();
            }

            @Override
            public void onFailure(Call<PesanListResponse> call, Throwable t) {
                isLoadingMessages = false;
                messagesCall = null;
                if (call.isCanceled() || !isActive) return;
                if (showErrors) {
                    Toast.makeText(ChatDetailActivity.this, "Gagal memuat chat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendMessage() {
        String isi = etMessage == null ? "" : etMessage.getText().toString().trim();
        if (isi.isEmpty()) {
            Toast.makeText(this, "Pesan belum diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("penerima_id", userId);
        body.put("isi", isi);

        setSending(true);
        ApiClient.getService().kirimPesan(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                setSending(false);
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(ChatDetailActivity.this, "Pesan gagal dikirim", Toast.LENGTH_SHORT).show();
                    return;
                }

                etMessage.setText("");
                loadMessages(true);
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                setSending(false);
                Toast.makeText(ChatDetailActivity.this, "Gagal mengirim pesan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSending(boolean sending) {
        if (btnSend != null) btnSend.setEnabled(!sending);
        if (etMessage != null) etMessage.setEnabled(!sending);
    }

    private void scrollToBottom() {
        if (layoutManager != null && !messages.isEmpty()) {
            layoutManager.scrollToPosition(messages.size() - 1);
        }
    }

    private boolean isNearBottom() {
        if (layoutManager == null || messages.isEmpty()) return true;
        return layoutManager.findLastVisibleItemPosition() >= messages.size() - 2;
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static String formatTime(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        return DateTimeUtils.formatTime(value);
    }

    private static class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
        private static final int TYPE_SENT = 1;
        private static final int TYPE_RECEIVED = 2;
        private final List<PesanItem> items;
        private final int otherUserId;

        ChatMessageAdapter(List<PesanItem> items, int otherUserId) {
            this.items = items;
            this.otherUserId = otherUserId;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getPengirimId() == otherUserId ? TYPE_RECEIVED : TYPE_SENT;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layout = viewType == TYPE_SENT ? R.layout.item_chat_sender : R.layout.item_chat_receiver;
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new ViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PesanItem item = items.get(position);
            holder.tvMessage.setText(safe(item.getIsi()));
            holder.tvTime.setText(formatTime(item.getCreatedAt()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvTime;

            ViewHolder(View itemView, int viewType) {
                super(itemView);
                if (viewType == TYPE_SENT) {
                    tvMessage = itemView.findViewById(R.id.tv_message_sender);
                    tvTime = itemView.findViewById(R.id.tv_time_sender);
                } else {
                    tvMessage = itemView.findViewById(R.id.tv_message_receiver);
                    tvTime = itemView.findViewById(R.id.tv_time_receiver);
                }
            }
        }
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
