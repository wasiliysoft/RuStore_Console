package ru.wasiliysoft.rustoreconsole

import android.content.Context
import android.content.SharedPreferences

class PrefHelper private constructor(private val sp: SharedPreferences) {
    companion object {
        private const val PREF_TOKEN = "PREF_TOKEN"

        private var INSTANCE: PrefHelper? = null
        fun get(context: Context): PrefHelper {
            if (INSTANCE == null) {
                INSTANCE = PrefHelper(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
            }
            return INSTANCE!!
        }
    }

    var token: String
        get() = sp.getString(PREF_TOKEN, "") ?: ""
//        get() = ""
set(value) = sp.edit().putString(PREF_TOKEN, value).apply()
}