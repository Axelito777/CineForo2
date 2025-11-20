package com.example.registroapp.Presentation.ViewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Domain.Model.Favorite
import com.example.registroapp.Domain.usecase.Favorite.GetFavoritesUseCase
import com.example.registroapp.Domain.usecase.Favorite.RemoveFavoriteUseCase
import com.example.registroapp.Utils.Resource
import kotlinx.coroutines.launch

data class FavoritosState(
    val favorites: List<Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritosViewModel(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : ViewModel() {

    private val _state = mutableStateOf(FavoritosState())
    val state: State<FavoritosState> = _state

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getFavoritesUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        favorites = result.data ?: emptyList(),
                        isLoading = false
                    )
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

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            when (removeFavoriteUseCase(movieId)) {
                is Resource.Success -> {
                    // Recargar la lista
                    loadFavorites()
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = "Error al eliminar favorito"
                    )
                }
                is Resource.Loading -> {
                    // No hacer nada
                }
            }
        }
    }
}