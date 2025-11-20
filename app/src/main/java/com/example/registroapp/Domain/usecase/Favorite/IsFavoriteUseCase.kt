package com.example.registroapp.Domain.usecase.Favorite

import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource

class IsFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(movieId: Int): Resource<Boolean> {
        return favoriteRepository.isFavorite(movieId)
    }
}