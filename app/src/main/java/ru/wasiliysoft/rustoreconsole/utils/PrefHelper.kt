package ru.wasiliysoft.rustoreconsole.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_APP_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val LOG_TAG = "PrefHelper"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_APP_IDS = "PREF_APP_IDS"
        private const val PREF_APP_FILE_NAME = "prefs"

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
        // get() = ""
        set(value) = prefs.edit().putString(PREF_TOKEN, value).apply()

    //стоит рассмотреть вариант хранить в json'е
    //потому что любое неверное движение и у тебя не то, что ты хотел
    //но стоит учитывать, что обфусикация (если рассмотреть json) может сожрать названия переменных :D
    var appIdList: List<Long>
        get(): List<Long> {
            val str = prefs.getString(PREF_APP_IDS, "") ?: ""
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
            prefs.edit().putString(PREF_APP_IDS, str.toString()).apply()
        }
}