package ru.wasiliysoft.rustoreconsole.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

// Интересное решение вынести в утилсы, но дата-слой чуть больше подходит
// Но это сугубо имхота
// javadoc, кстати, это круто. Но не стоит им слишком злоупотреблять, потому что он не всегда нужен
// Тут он просто для примера
/**
 * Класс-хелпер для работы с SharedPreferences
 */
class PrefHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_APP_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val LOG_TAG = "PrefHelper"
        private const val PREF_APP_FILE_NAME = "prefs"
        private const val PREF_TOKEN = "PREF_TOKEN"
        private const val PREF_JSON_APP_LIST = "PREF_JSON_APP_LIST"

        // deprecated for who?
        // Серьёзно, оно никуда не шарится и используется только внутри префов
        // а ещё у тебя команда это ты сам, так что...
        // ну и тут стоит заметить, что лучше указывать причину деприкейта
        // хотя бы в javadoc
        // пример
        /**
         * Используется другая переменная для обозначения appID
         */
        @Deprecated("Used another variable for appID")
        private const val PREF_APP_IDS = "PREF_APP_IDS"

        private var instance: PrefHelper? = null

        /**
         * Инициализация PrefsHelper
         * @param context необходим для создания SharedPreferences
         * Необходима инициализация до использования метода getInstance()
         */
        fun initPreferences(context: Context) {
            if (instance == null) {
                instance = PrefHelper(context)
            }
        }

        /**
         * Метод для получения существующего инстанса PrefsHelper
         * Вызывается строго после инициализации, иначе IllegalArgumentException
         */
        fun getInstance(): PrefHelper = requireNotNull(instance) {
            "PrefHelper instance isn't create"
        }
    }

    //Стоит рассмотреть вариант prefManager -> prefsRepo -> VM
    //Сейчас можно в любом месте изменить переменную в итоге мы можем ожидать не тот результат, который нам нужен
    @Deprecated(
        message = "Use functions for access preferences and repo for access data",
        replaceWith = ReplaceWith("getPrefs(), editPrefs()"),
        level = DeprecationLevel.WARNING
    )
    var token: String
        get() = prefs.getString(PREF_TOKEN, "") ?: ""
        // get() = "" // for manual test on empty value
        set(value) = prefs.edit().putString(PREF_TOKEN, value).apply()

    // Уже лучше, но это уже не зона ответственности PrefHelper'а
    // PrefHelper должен отвечать только за использование SharedPreferences
    // и не должен хранить в себе ничего лишнего
    @Deprecated(
        message = "Use functions for access preferences and repo for access data",
        replaceWith = ReplaceWith("getPrefs(), editPrefs()"),
        level = DeprecationLevel.WARNING
    )
    var jsonAppListResp: String
        get() = prefs.getString(PREF_JSON_APP_LIST, "") ?: ""
        // get() = "" // for manual test on empty value
        set(value) {
            Log.d(LOG_TAG, "update app list in cache")
            prefs.edit().putString(PREF_JSON_APP_LIST, value).apply()
        }

    // Ответственность - мы знаем о знаем только то, что должны
    // к сожалению, для получения мы должны собрать все префы
    // поэтому тут должна быть прослойка в виде апи для конкретного типа
    fun getPrefs(): SharedPreferences = prefs

    // А вот для изменения всё просто - мы получаем Editor
    // И больше ничего не надо, так как мы пришли изменять что-то, а не читать
    fun editPrefs(): SharedPreferences.Editor = prefs.edit()
}