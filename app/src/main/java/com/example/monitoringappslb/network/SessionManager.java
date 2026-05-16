package com.example.monitoringappslb.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.monitoringappslb.model.response.UserResponse;

public class SessionManager {

    public static final String PREF_NAME = "SLBSession";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAMA = "user_nama";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_FOTO = "user_foto";
    public static final String KEY_SISWA_ID = "siswa_id";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /** Simpan session setelah login berhasil */
    public void saveSession(String token, UserResponse user) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAMA, user.getNama());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_FOTO, user.getFoto());
        if (user.getSiswaId() != null) {
            editor.putInt(KEY_SISWA_ID, user.getSiswaId());
        }
        editor.apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public int getUserId() { return prefs.getInt(KEY_USER_ID, -1); }
    public String getUserNama() { return prefs.getString(KEY_USER_NAMA, ""); }
    public String getUserEmail() { return prefs.getString(KEY_USER_EMAIL, ""); }
    public String getUserRole() { return prefs.getString(KEY_USER_ROLE, ""); }
    public String getUserFoto() { return prefs.getString(KEY_USER_FOTO, null); }
    public int getSiswaId() { return prefs.getInt(KEY_SISWA_ID, -1); }
    public boolean isLoggedIn() { return getToken() != null; }

    public void saveSiswaId(int siswaId) {
        editor.putInt(KEY_SISWA_ID, siswaId);
        editor.apply();
    }

    /** Hapus session (logout) */
    public void logout() {
        editor.clear();
        editor.apply();
        ApiClient.reset();
    }
}
