package com.app.shoppy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShoppyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
