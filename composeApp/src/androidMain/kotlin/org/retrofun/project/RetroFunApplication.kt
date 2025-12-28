package org.retrofun.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.retrofun.project.di.initKoin

class RetroFunApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@RetroFunApplication)
        }
    }
}
