package com.example.registroapp.Domain.repository

import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Utils.Resource

interface MovieRepository {
    suspend fun getPopularMovies(page: Int = 1): Resource<List<Movie>>
    suspend fun getNowPlayingMovies(page: Int = 1): Resource<List<Movie>>
    suspend fun getUpcomingMovies(page: Int = 1): Resource<List<Movie>>
    suspend fun getMovieDetails(movieId: Int): Resource<Movie>
    suspend fun searchMovies(query: String, page: Int = 1): Resource<List<Movie>>
}