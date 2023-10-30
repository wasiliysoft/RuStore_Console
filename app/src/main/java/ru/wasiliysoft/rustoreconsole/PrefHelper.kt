package ru.wasiliysoft.rustoreconsole

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefHelper private constructor(private val sp: SharedPreferences) {
    companion object {
        private const val LOG_TAG = "PrefHelper"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_APP_IDS = "PREF_APP_IDS"

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
        // get() = ""
        set(value) = sp.edit().putString(PREF_TOKEN, value).apply()

    var appIdList: List<Long>
        get(): List<Long> {
            val str = sp.getString(PREF_APP_IDS, "") ?: ""
            val list = mutableListOf<Long>()
            str.split(',').toList().forEach {
                if (it.isNotEmpty()) list.add(it.toLong())
            }
            return list
        }
        set(value) {
            Log.d(LOG_TAG, "update application id list")
            val str = StringBuilder()
            value.forEach {
                str.append(it).append(',')
            }
            sp.edit().putString(PREF_APP_IDS, str.toString()).apply()
        }
}