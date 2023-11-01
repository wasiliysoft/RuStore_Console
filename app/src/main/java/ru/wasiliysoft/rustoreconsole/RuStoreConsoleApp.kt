package ru.wasiliysoft.rustoreconsole

import android.app.Application
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper

class RuStoreConsoleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Не особо нормально, так как этот метод выполняется не только при запуске приложения,
        // но и при получении data push, нажатии на виджет и т.п.
        PrefHelper.initPreferences(applicationContext)
        // Xак для любознательных -> activity lifecycle callbacks
        // но это будет сложный хак и, так как префы - легковесная штука, можно пока что инциализировать каждый раз
        // тем более это позволяет сразу же иметь необходимые данные во всех местах
    }
}