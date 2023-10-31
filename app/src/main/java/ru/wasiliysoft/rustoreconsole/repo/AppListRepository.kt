package ru.wasiliysoft.rustoreconsole.repo

import com.google.gson.Gson
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.AppListResp
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper


object AppListRepository {
    private const val LOG_TAG = "AppListRepository"
    private val gson by lazy { Gson() }
    private val api by lazy { RetrofitClient.api }
    private val ph by lazy { PrefHelper.getInstance() }

    fun getApps(): List<AppInfo>? = fromJSON(ph.jsonAppListResp)?.body?.list

    suspend fun getAppsForce(): List<AppInfo> {
        val url = "https://backapi.rustore.ru/applicationData/retrieveUserApps"
        val rawBody = api.getRetrieveUserApps(url).string()
        toStorage(rawBody)
        return getApps()!!
    }

    private fun fromJSON(json: String): AppListResp? {
        if (json.isEmpty()) return null
        try {
            return gson.fromJson(json, AppListResp::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun toStorage(json: String) {
        ph.jsonAppListResp = json
    }
}