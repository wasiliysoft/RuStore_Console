package ru.wasiliysoft.rustoreconsole.utils

sealed class LoadingResult<out R> {
    data class Success<out T>(val data: T) : LoadingResult<T>()
    data class Error(val exception: Exception) : LoadingResult<Nothing>()
    data class Loading(val description: String) : LoadingResult<Nothing>()

    val <T> T.exhaustive: T
        get() = this

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            is Loading -> "Loading:$description"
        }
    }
}