package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    private val commentDateStr: String,
    @SerializedName("commentText")
    val commentText: String,
    @SerializedName("likeCounter")
    val likeCounter: Int,
    @SerializedName("dislikeCounter")
    val dislikeCounter: Int,
    @SerializedName("deviceInfo")
    val deviceInfo: DeviceInfo?,
    @SerializedName("devResponse")
    val devResponse: List<DeveloperComment>?
) {
    val commentDate: LocalDateTime
        get() = LocalDateTime.parse(
            commentDateStr.take(19).replace(' ', 'T'),
            DateTimeFormatter.ISO_DATE_TIME
        )

    companion object {
        fun demo(commentId: Long = 5) = UserReview(
            commentId = commentId,
            appRating = 5,
            firstName = "firstName",
            commentDateStr = "2023-07-20 19:09:45.045",
            commentText = "commentTextcommentTextcommentText commentText",
            likeCounter = 8,
            dislikeCounter = 3,
            deviceInfo = null,
            devResponse = List(5) { DeveloperComment.demo(commentId) }
        )
    }
}

data class DeviceInfo(
    @SerializedName("id")
    val id: String?,
    @SerializedName("firmwareVersion")
    val firmwareVersion: String?,
    @SerializedName("model")
    val model: String?,
    @SerializedName("manufacturer")
    val manufacturer: String?,
)

data class DeveloperComment(
    @SerializedName("id")
    val id: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("date")
    private val dateStr: String
) {
    val date: LocalDateTime
        get() = LocalDateTime.parse(
            dateStr.take(19).replace(' ', 'T'),
            DateTimeFormatter.ISO_DATE_TIME
        )

    companion object {
        fun demo(id: Long = 5) = DeveloperComment(
            id = id,
            status = "comment status",
            dateStr = "2023-07-20 19:09:45.045",
            text = "Dev comment Dev comment Dev comment",
        )
    }
}