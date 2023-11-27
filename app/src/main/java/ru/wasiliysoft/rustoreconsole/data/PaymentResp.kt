package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

data class PaymentResp(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: Map<String, Map<String, Map<String, Stats>>>
)


data class Stats(
    @SerializedName("dailyStats")
    val dailyStats: Int,
    @SerializedName("weeklyStats")
    val weeklyStats: Int,
    @SerializedName("monthlyStats")
    val monthlyStats: Int,
    @SerializedName("totalStats")
    val totalStats: Int,
)
