package com.example.registroapp.Domain.Model

import com.google.gson.annotations.SerializedName

data class Favorite(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("movieId")
    val movieId: Int,

    @SerializedName("movieTitle")
    val movieTitle: String,

    @SerializedName("moviePoster")
    val moviePosterPath: String?,

    @SerializedName("movieRating")
    val movieRating: Double,

    @SerializedName("createdAt")
    val addedAt: String
)