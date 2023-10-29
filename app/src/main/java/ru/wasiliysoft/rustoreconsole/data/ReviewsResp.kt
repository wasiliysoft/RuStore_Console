package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

data class ReviewsResp(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: Reviews
)

data class Reviews(
    @SerializedName("content")
    val reviews: List<UserReview>
)

data class UserReview(
    @SerializedName("commentId")
    val commentId: Long,
    @SerializedName("appRating")
    val appRating: Int,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("commentDate")
    val commentDate: String,
    @SerializedName("commentText")
    val commentText: String,
    @SerializedName("likeCounter")
    val likeCounter: Int,
    @SerializedName("dislikeCounter")
    val dislikeCounter: Int,
    @SerializedName("deviceInfo")
    val deviceInfo: String?,
    @SerializedName("devResponse")
    val devResponse: List<DeveloperComment>?
)

data class DeveloperComment(
    @SerializedName("id")
    val id: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("date")
    val date: String
)