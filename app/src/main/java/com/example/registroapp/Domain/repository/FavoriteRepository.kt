package com.example.registroapp.Domain.Repository

import com.example.registroapp.Domain.Model.Favorite
import com.example.registroapp.Utils.Resource

interface FavoriteRepository {
    suspend fun addFavorite(
        movieId: Int,
        movieTitle: String,
        posterPath: String?,
        rating: Double
    ): Resource<Unit>

    suspend fun removeFavorite(movieId: Int): Resource<Unit>

    suspend fun getFavorites(): Resource<List<Favorite>>

    suspend fun isFavorite(movieId: Int): Resource<Boolean>
}