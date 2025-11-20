package com.example.registroapp.Domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoritoSupabase(
    val id: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("movie_id")
    val movieId: Int,
    @SerialName("movie_title")
    val movieTitle: String,
    @SerialName("movie_poster")
    val moviePoster: String?,
    @SerialName("movie_rating")
    val movieRating: Double
)