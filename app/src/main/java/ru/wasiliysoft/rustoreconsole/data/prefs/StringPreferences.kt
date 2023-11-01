package ru.wasiliysoft.rustoreconsole.data.prefs

interface StringPreferences {

    fun getData(key: String, defaultValue: String?): String

    fun setData(key: String, value: String)
}