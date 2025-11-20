package com.example.registroapp.Data.Repository

import com.example.registroapp.Data.Remote.Api.TmdbApi
import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Domain.repository.MovieRepository
import com.example.registroapp.Utils.Constants
import com.example.registroapp.Utils.Resource
import com.example.registroapp.Utils.toMovie

class MovieRepositoryImpl(
    private val api: TmdbApi
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): Resource<List<Movie>> {
        return try {
            val response = api.getPopularMovies(
                apiKey = Constants.API_KEY,
                language = Constants.LANGUAGE,
                page = page
            )

            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toMovie() } ?: emptyList()
                Resource.Success(movies)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun getNowPlayingMovies(page: Int): Resource<List<Movie>> {
        return try {
            val response = api.getNowPlayingMovies(
                apiKey = Constants.API_KEY,
                language = Constants.LANGUAGE,
                page = page
            )

            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toMovie() } ?: emptyList()
                Resource.Success(movies)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun getUpcomingMovies(page: Int): Resource<List<Movie>> {
        return try {
            val response = api.getUpcomingMovies(
                apiKey = Constants.API_KEY,
                language = Constants.LANGUAGE,
                page = page
            )

            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toMovie() } ?: emptyList()
                Resource.Success(movies)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun getMovieDetails(movieId: Int): Resource<Movie> {
        return try {
            val response = api.getMovieDetails(
                movieId = movieId,
                apiKey = Constants.API_KEY,
                language = Constants.LANGUAGE
            )

            if (response.isSuccessful) {
                val movie = response.body()?.toMovie()
                if (movie != null) {
                    Resource.Success(movie)
                } else {
                    Resource.Error("Película no encontrada")
                }
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    override suspend fun searchMovies(query: String, page: Int): Resource<List<Movie>> {
        return try {
            val response = api.searchMovies(
                apiKey = Constants.API_KEY,
                query = query,
                language = Constants.LANGUAGE,
                page = page
            )

            if (response.isSuccessful) {
                val movies = response.body()?.results?.map { it.toMovie() } ?: emptyList()
                Resource.Success(movies)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}