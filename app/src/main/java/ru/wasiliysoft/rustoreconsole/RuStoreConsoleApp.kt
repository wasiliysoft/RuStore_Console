package ru.wasiliysoft.rustoreconsole

import android.app.Application
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper

class RuStoreConsoleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Не особо нормально, так как этот метод выполняется не только при запуске приложения,
        // но и при получении data push, нажатии на виджет и т.п.
        PrefHelper.initPreferences(applicationContext)
    }
}