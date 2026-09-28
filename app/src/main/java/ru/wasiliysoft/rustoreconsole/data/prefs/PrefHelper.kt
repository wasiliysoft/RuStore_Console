package ru.wasiliysoft.rustoreconsole.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import ru.wasiliysoft.rustoreconsole.utils.CryptoManager

class PrefHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_APP_FILE_NAME, Context.MODE_PRIVATE)
    private val cryptoManager = CryptoManager()

    companion object {
        private const val LOG_TAG = "PrefHelper"
        private const val PREF_APP_FILE_NAME = "prefs"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_TOKEN_ENCRYPTED = "PREF_TOKEN_ENCRYPTED"
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

    var token: String
        get() {
            val stored = prefs.getString(PREF_TOKEN, "") ?: ""
            if (stored.isEmpty()) return ""

            val isEncrypted = prefs.getBoolean(PREF_TOKEN_ENCRYPTED, false)
            if (!isEncrypted) {
                // Явная миграция старого формата — один раз
                token = stored // пройдёт через setter → зашифрует и выставит флаг
                return stored
            }

            return try {
                cryptoManager.decrypt(stored)
            } catch (e: Exception) {
                // Данные повреждены/подменены/устарели из-за нового отпечатка и т.п.
                // НЕ пытаемся расшифровать
                Log.e(LOG_TAG, "Token decryption failed", e)
                clearToken()
                ""
            }
        }
        set(value) {
            if (value.isEmpty()) {
                clearToken()
                return
            }
            val encrypted = cryptoManager.encrypt(value)
            prefs.edit {
                putString(PREF_TOKEN, encrypted)
                putBoolean(PREF_TOKEN_ENCRYPTED, true)
            }
        }

    private fun clearToken() {
        prefs.edit {
            remove(PREF_TOKEN)
            remove(PREF_TOKEN_ENCRYPTED)
        }
    }

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