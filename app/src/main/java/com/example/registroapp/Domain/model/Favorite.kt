package com.example.registroapp.Domain.Model

data class Favorite(
    val id: String,
    val userId: String,
    val movieId: Int,
    val movieTitle: String,
    val moviePosterPath: String?,
    val movieRating: Double,
    val addedAt: String
)