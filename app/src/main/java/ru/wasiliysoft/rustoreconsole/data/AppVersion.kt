package ru.wasiliysoft.rustoreconsole.data

import android.util.Log
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

data class AppVersionShort(
    val versionId: Long,
    val appVersionStatus: String
)

fun Response<ResponseBody>.unwrapLastVersion(): AppVersionShort? {
    // 1. Проверяем, успешен ли запрос и есть ли тело
    val bodyString = if (this.isSuccessful) this.body()?.string() else null
    if (bodyString.isNullOrEmpty()) return null

    val resultList = mutableListOf<AppVersionShort>()

    try {
        // 2. Спускаемся по дереву без создания DTO-классов
        val rootNode = JSONObject(bodyString)
        val bodyNode = rootNode.optJSONObject("body")
        val contentArray = bodyNode?.optJSONArray("content")
        if (contentArray == null) {
            Log.e("unwrapLastVersion", "contentArray is null, $rootNode")
            return null
        }

        // 3. Вытаскиваем нужные строки из элементов массива
        for (i in 0 until contentArray.length()) {
            val element = contentArray.getJSONObject(i)
            resultList.add(
                AppVersionShort(
                    versionId = element.optLong("versionId"),
                    appVersionStatus = element.optString("appVersionStatus")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return resultList.maxByOrNull { it.versionId }
}

