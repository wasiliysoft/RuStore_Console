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
    val versionName: String,
    @SerializedName("versionCode")
    val versionCode: Int,
    @SerializedName("appVerUpdatedAt")
    val appVerUpdatedAt: String,
    @SerializedName("paid")
    val paid: Boolean,
    @SerializedName("activePrice")
    val activePrice: Int
)