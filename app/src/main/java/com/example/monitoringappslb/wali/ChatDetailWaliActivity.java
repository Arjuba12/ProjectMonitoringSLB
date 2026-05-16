package com.example.monitoringappslb.wali;

import android.os.Bundle;
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
import com.example.monitoringappslb.network.SessionManager;
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

public class ChatDetailWaliActivity extends BaseWaliActivity {
    private int userId;
    private String userName;
    private TextView tvTitle;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatMessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SessionManager session;
    private final List<PesanItem> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail_wali);

        setupNavigation();
        session = new SessionManager(this);

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
                title += " - Guru " + studentName;
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

        if (userId <= 0) {
            Toast.makeText(this, "Kontak tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessages();
    }

    private void loadMessages() {
        ApiClient.getService().getPercakapan(userId).enqueue(new Callback<PesanListResponse>() {
            @Override
            public void onResponse(Call<PesanListResponse> call, Response<PesanListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(ChatDetailWaliActivity.this, "Percakapan belum bisa dimuat", Toast.LENGTH_SHORT).show();
                    return;
                }

                messages.clear();
                if (response.body().getData() != null) {
                    messages.addAll(response.body().getData());
                }
                adapter.notifyDataSetChanged();
                scrollToBottom();
            }

            @Override
            public void onFailure(Call<PesanListResponse> call, Throwable t) {
                Toast.makeText(ChatDetailWaliActivity.this, "Gagal memuat chat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        int siswaId = session.getSiswaId();
        if (siswaId > 0) body.put("siswa_id", siswaId);

        setSending(true);
        ApiClient.getService().kirimPesan(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                setSending(false);
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(ChatDetailWaliActivity.this, "Pesan gagal dikirim", Toast.LENGTH_SHORT).show();
                    return;
                }

                etMessage.setText("");
                loadMessages();
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                setSending(false);
                Toast.makeText(ChatDetailWaliActivity.this, "Gagal mengirim pesan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        return findViewById(R.id.drawer_layout_wali);
    }

    @Override
    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view_wali);
    }

    @Override
    protected BottomNavigationView getBottomNavigationView() {
        return null;
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
