package com.example.registroapp.Domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Comentario(
    val id: String,
    val userId: String,
    val movieId: Int? = null,
    val temaId: String? = null,
    val contenido: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val createdAt: String,
    val userNombre: String? = null,
    val userLikeStatus: String? = null
)

@Serializable
data class ComentarioSupabase(
    @SerialName("id") val id: String,
    @SerialName("user_id") val user_id: String,
    @SerialName("movie_id") val movie_id: Int? = null,
    @SerialName("tema_id") val tema_id: String? = null,
    @SerialName("contenido") val contenido: String,
    @SerialName("likes") val likes: Int = 0,
    @SerialName("dislikes") val dislikes: Int = 0,
    @SerialName("created_at") val created_at: String,
    @SerialName("user_nombre") val user_nombre: String? = null
) {
    fun toDomain(userLikeStatus: String? = null) = Comentario(
        id = id,
        userId = user_id,
        movieId = movie_id,
        temaId = tema_id,
        contenido = contenido,
        likes = likes,
        dislikes = dislikes,
        createdAt = created_at,
        userNombre = user_nombre,
        userLikeStatus = userLikeStatus
    )
}

@Serializable
data class NuevoComentario(
    @SerialName("user_id") val user_id: String,
    @SerialName("movie_id") val movie_id: Int? = null,
    @SerialName("tema_id") val tema_id: String? = null,
    @SerialName("contenido") val contenido: String,
    @SerialName("user_nombre") val user_nombre: String
)