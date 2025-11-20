package com.example.registroapp.Domain.usecase

import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Domain.repository.MovieRepository
import com.example.registroapp.Utils.Resource

class GetMovieDetailsUseCase(
    private val repository: MovieRepository
) {
    suspend operator fun invoke(movieId: Int): Resource<Movie> {
        return repository.getMovieDetails(movieId)
    }
}