package ru.wasiliysoft.rustoreconsole

import android.app.Application

class RuStoreConsoleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PrefHelper.initPreferences(applicationContext)
    }
}