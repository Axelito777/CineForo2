package com.example.registroapp.Domain.usecase.Favorite

import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource

class RemoveFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(movieId: Int): Resource<Unit> {
        return favoriteRepository.removeFavorite(movieId)
    }
}