package ru.wasiliysoft.rustoreconsole.data.prefs

import ru.wasiliysoft.rustoreconsole.utils.PrefHelper

// Пример 2 в 1 - разделение ответственности и инверсия зависимостей
// PrefHelper - утилита, которую мы берём для доступа к чему-либо
// а StringPreferences - уже для доступа к определённому типу (String)
class StringPreferencesImpl : StringPreferences {

    override fun getData(key: String, defaultValue: String?): String {
        return PrefHelper.getInstance().getPrefs().getString(key, defaultValue) ?: ""
    }

    override fun setData(key: String, value: String) {
        PrefHelper.getInstance().editPrefs().putString(key, value).apply()
    }
}