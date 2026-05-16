package com.example.monitoringappslb;

import android.app.Application;
import com.example.monitoringappslb.network.ApiClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inisialisasi ApiClient sekali saat app pertama kali dibuka
        ApiClient.init(this);
    }
}