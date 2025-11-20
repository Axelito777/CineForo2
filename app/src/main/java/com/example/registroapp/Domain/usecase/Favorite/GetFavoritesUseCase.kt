package com.example.registroapp.Domain.usecase.Favorite

import com.example.registroapp.Domain.Model.Favorite
import com.example.registroapp.Domain.Repository.FavoriteRepository
import com.example.registroapp.Utils.Resource

class GetFavoritesUseCase(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(): Resource<List<Favorite>> {
        return repository.getFavorites()
    }
}