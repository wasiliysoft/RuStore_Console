package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

data class AppListResp(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: AppList
)

data class AppList(
    @SerializedName("content")
    val list: List<AppInfo>,
)

data class AppInfo(
    @SerializedName("appId")
    val appId: Long,
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("appName")
    val appName: String,
    @SerializedName("iconUrl")
    val iconUrl: String,
    @SerializedName("appStatus")
    val appStatus: String,
    @SerializedName("versionName")
    private val versionNameNullable: String?,
    @SerializedName("versionCode")
    val versionCode: Int,
    @SerializedName("appVerUpdatedAt")
    val appVerUpdatedAt: String,
    @SerializedName("paid")
    val paid: Boolean,
    @SerializedName("activePrice")
    val activePrice: Int,
    val lastVersion: AppVersionShort? = null
) {
    val versionName get() = versionNameNullable ?: "-"

    /**
     * Обогащение модели информацией о статусе последней версии
     */
    fun withLastVersionInfo(lastVersion: AppVersionShort): AppInfo {
        return copy(lastVersion = lastVersion)
    }

    companion object {
        fun demo(appId: Long = 1) = AppInfo(
            appId = appId,
            packageName = "packageName.preview",
            appName = "Preview Application name",
            iconUrl = "iconUrl",
            appStatus = "PUBLISHED",
            versionNameNullable = "v44.4.56",
            versionCode = 4,
            appVerUpdatedAt = "appVerUpdatedAt",
            paid = true,
            activePrice = 950
        )
    }
}