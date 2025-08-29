package com.example.dinahvision.koin

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class ApplicationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ApplicationApp)
            modules(appModule)
        }
    }
}