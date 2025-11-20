
package com.example.registroapp.Domain.usecase

import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource

class AddFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(
        movieId: Int,
        movieTitle: String,
        posterPath: String?,
        rating: Double
    ): Resource<Unit> {
        return favoriteRepository.addFavorite(movieId, movieTitle, posterPath, rating)
    }
}