package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

data class AuthTokenResp(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: Map<String, String>
)


