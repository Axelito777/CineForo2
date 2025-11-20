package com.example.registroapp.Domain.usecase

import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Domain.repository.MovieRepository
import com.example.registroapp.Utils.Resource

class GetPopularMoviesUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(page: Int = 1): Resource<List<Movie>> {
        return repository.getPopularMovies(page)
    }
}