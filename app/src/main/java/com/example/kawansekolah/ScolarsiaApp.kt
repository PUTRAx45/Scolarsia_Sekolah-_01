package com.example.kawansekolah

import android.app.Application
import android.util.Log
import com.example.kawansekolah.utils.DevHost
import com.example.kawansekolah.utils.NotificationHelper

class ScolarsiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val savedHost = DevHost.getSavedDynamicHost(this)
        Log.i("ScolarsiaNet", "🚀 ScolarsiaApp.onCreate() dipanggil. Host dibaca sinkron dari SharedPreferences: '$savedHost'")
        try {
            FirebaseEmulatorHelper.setupEmulators(context = this, useEmulator = BuildConfig.USE_EMULATOR)
        } catch (e: Exception) {
            Log.e("ScolarsiaNet", "❌ Gagal mengonfigurasi Firebase Emulator pada Application.onCreate(): ${e.message}", e)
        }
        NotificationHelper.createNotificationChannel(this)
    }
}
