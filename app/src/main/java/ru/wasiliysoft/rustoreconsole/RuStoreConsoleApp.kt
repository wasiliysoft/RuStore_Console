package ru.wasiliysoft.rustoreconsole

import android.app.ActivityManager
import android.app.Application
import android.os.Process
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper

class RuStoreConsoleApp : Application() {
    private val API_KEY = "020d4bfa-00f7-4d39-baf6-7f666b119ca0"

    /**
     * Этот метод выполняется не только при запуске приложения,
     * но и при получении data push, нажатии на виджет и т.п.
     */
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            if (!BuildConfig.DEBUG) {
                // Creating an extended library configuration.
                val config = AppMetricaConfig.newConfigBuilder(API_KEY).build()
                // Initializing the AppMetrica SDK.
                AppMetrica.activate(this, config)
            }

            PrefHelper.initPreferences(applicationContext)
        }
    }

    // your package name is the same with your main process name
    // https://stackoverflow.com/questions/6954027/detecting-if-youre-in-the-main-process-or-the-remote-service-process-in-applica/36256085#36256085
    private fun isMainProcess(): Boolean {
        return packageName == getProcName()
    }

    // you can use this method to get current process name, you will get
    // name like "com.package.name"(main process name) or "com.package.name:remote"
    private fun getProcName(): String? {
        val mypid = Process.myPid()
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.runningAppProcesses.firstOrNull { it.pid == mypid }?.processName
    }
}