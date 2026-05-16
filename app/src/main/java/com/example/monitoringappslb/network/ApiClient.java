package com.example.monitoringappslb.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // ⚠️ GANTI SESUAI IP KOMPUTER KAMU
    // Emulator Android  → "http://10.0.2.2:3000/"
    // HP fisik (USB)    → "http://192.168.1.10:3000/" (IP komputer kamu di jaringan lokal)
    // Cek IP: buka CMD → ketik "ipconfig" → lihat IPv4 Address

    public static final String BASE_URL = "http://10.0.2.2:3000/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging interceptor (tampil di Logcat)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            // Auth interceptor — otomatis attach token ke setiap request
            OkHttpClient client = new OkHttpClient.Builder()
                    // Interceptor untuk menghapus header 'Expect' jika ada
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .addHeader("Content-Type", "application/json");

                        // Ambil token dari SharedPreferences
                        if (appContext != null) {
                            SharedPreferences prefs = appContext.getSharedPreferences(
                                    SessionManager.PREF_NAME, Context.MODE_PRIVATE);
                            String token = prefs.getString(SessionManager.KEY_TOKEN, null);
                            if (token != null) {
                                builder.addHeader("Authorization", "Bearer " + token);
                            }
                        }

                        // Hapus header 'Expect' jika ada
                        builder.removeHeader("Expect");

                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Singleton ApiService agar semua endpoint siap dipakai di seluruh aplikasi
     */
    public static ApiService getService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    // Reset retrofit & ApiService (dipanggil saat logout)
    public static void reset() {
        retrofit = null;
        apiService = null;
    }

}
