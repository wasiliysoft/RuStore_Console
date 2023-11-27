package ru.wasiliysoft.rustoreconsole.data.prefs

interface StringPreferences {

    fun getData(key: String, defaultValue: String?): String

    fun setData(key: String, value: String)
}

class StringPreferencesImpl : StringPreferences {
    override fun getData(key: String, defaultValue: String?): String {
        return PrefHelper.getInstance().getPrefs().getString(key, defaultValue) ?: ""
    }

    override fun setData(key: String, value: String) {
        PrefHelper.getInstance().editPrefs().putString(key, value).apply()
    }
}