package es.unizar.mii.tmdad.chatapp.dto

data class ErrorResponse(
    val status: Int,
    val code: Int? = null,
    val title: String?
)