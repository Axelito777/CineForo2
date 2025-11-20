package com.example.registroapp.Presentation.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Domain.model.Movie
import com.example.registroapp.Domain.usecase.AddFavoriteUseCase
import com.example.registroapp.Domain.usecase.Favorite.IsFavoriteUseCase
import com.example.registroapp.Domain.usecase.Favorite.RemoveFavoriteUseCase
import com.example.registroapp.Domain.usecase.GetMovieDetailsUseCase
import com.example.registroapp.Utils.Resource
import kotlinx.coroutines.launch

data class MovieDetailState(
    val movie: Movie? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val favoriteLoading: Boolean = false
)

class MovieDetailViewModel(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase
) : ViewModel() {

    private val _state = mutableStateOf(MovieDetailState())
    val state: State<MovieDetailState> = _state

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getMovieDetailsUseCase(movieId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        movie = result.data,
                        isLoading = false
                    )
                    checkIfFavorite(movieId)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun checkIfFavorite(movieId: Int) {
        viewModelScope.launch {
            when (val result = isFavoriteUseCase(movieId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isFavorite = result.data ?: false)
                }
                else -> {
                    // No hacer nada si hay error al verificar
                }
            }
        }
    }

    fun toggleFavorite() {
        val movie = _state.value.movie ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(favoriteLoading = true)

            val result = if (_state.value.isFavorite) {
                removeFavoriteUseCase(movie.id)
            } else {
                addFavoriteUseCase(
                    movieId = movie.id,
                    movieTitle = movie.title,
                    posterPath = movie.posterPath,
                    rating = movie.voteAverage
                )
            }

            // 👇 Cambia .fold() por when()
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isFavorite = !_state.value.isFavorite,
                        favoriteLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        favoriteLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // No hacer nada, ya está en loading
                }
            }
        }
    }
}





