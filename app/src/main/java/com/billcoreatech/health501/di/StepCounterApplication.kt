package com.billcoreatech.health501.di

import android.app.Application
import android.util.Log
import androidx.activity.viewModels
import com.billcoreatech.health501.data.HealthConnectManager
import com.billcoreatech.health501.viewmodels.HealthConnectViewModel
import dagger.hilt.android.HiltAndroidApp
import kotlin.getValue

@HiltAndroidApp
class StepCounterApplication : Application() {

    internal val healthConnectManager: HealthConnectManager by lazy {
        HealthConnectManager(this)
    }
    override fun onCreate() {
        super.onCreate()
        Log.e("", "StepCounterApplication onCreate .......................................")
        healthConnectManager.checkAvailability()
    }
}