package ru.wasiliysoft.rustoreconsole.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_APP_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val LOG_TAG = "PrefHelper"
        private const val PREF_APP_FILE_NAME = "prefs"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_JSON_APP_LIST = "PREF_JSON_APP_LIST"

        /**
         * Хранит имя маршрута для стартовой вкладки на домашнем экране
         */
        const val PREF_HOME_START_TAB_ROUTE = "PREF_HOME_START_TAB_ROUTE"

        @Deprecated(
            message = "Used in old versions",
            replaceWith = ReplaceWith("PREF_JSON_APP_LIST"),
            level = DeprecationLevel.ERROR
        )
        private const val PREF_APP_IDS = "PREF_APP_IDS"

        private var instance: PrefHelper? = null

        fun initPreferences(context: Context) {
            if (instance == null) {
                instance = PrefHelper(context)
            }
        }

        fun getInstance(): PrefHelper = requireNotNull(instance) {
            "PrefHelper instance isn't create"
        }
    }

    //Стоит рассмотреть вариант prefManager -> prefsRepo -> VM
    //Сейчас можно в любом месте изменить переменную в итоге мы можем ожидать не тот результат, который нам нужен
    var token: String
        get() = prefs.getString(PREF_TOKEN, "") ?: ""
        // get() = "" // for manual test on empty value
        set(value) = prefs.edit().putString(PREF_TOKEN, value).apply()

    var jsonAppListResp: String
        get() = prefs.getString(PREF_JSON_APP_LIST, "") ?: ""
        // get() = "" // for manual test on empty value
        set(value) {
            Log.d(LOG_TAG, "update app list in cache")
            prefs.edit().putString(PREF_JSON_APP_LIST, value).apply()
        }

    fun getPrefs(): SharedPreferences = prefs

    fun editPrefs(): SharedPreferences.Editor = prefs.edit()
}