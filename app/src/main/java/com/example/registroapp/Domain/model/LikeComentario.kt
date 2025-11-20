package com.example.registroapp.Domain.model

data class LikeComentario(
    val id: String,
    val userId: String,
    val comentarioId: String,
    val tipo: TipoLike, // "like" o "dislike"
    val createdAt: String
)

enum class TipoLike {
    LIKE,
    DISLIKE
}