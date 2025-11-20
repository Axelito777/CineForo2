package com.example.registroapp.Presentation.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.usecase.Comentarios.AddComentarioUseCase
import com.example.registroapp.Domain.usecase.Comentarios.GetComentariosUseCase
import com.example.registroapp.Domain.usecase.Comentarios.ToggleLikeUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComentariosViewModel(
    private val movieId: Int,
    private val getComentariosUseCase: GetComentariosUseCase,
    private val addComentarioUseCase: AddComentarioUseCase,
    private val toggleLikeUseCase: ToggleLikeUseCase
) : ViewModel() {

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        cargarComentarios()
    }

    fun cargarComentarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""

            getComentariosUseCase(movieId, userId)
                .onSuccess { comentariosList ->
                    _comentarios.value = comentariosList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Error al cargar comentarios"
                }

            _isLoading.value = false
        }
    }

    fun agregarComentario(contenido: String) {
        viewModelScope.launch {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch
            val userNombre = SupabaseClient.client.auth.currentUserOrNull()?.email?.substringBefore("@") ?: "Usuario"

            addComentarioUseCase(
                userId = userId,
                movieId = movieId,
                contenido = contenido,
                userNombre = userNombre
            ).onSuccess {
                cargarComentarios()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Error al agregar comentario"
            }
        }
    }

    fun darLike(comentarioId: String) {
        viewModelScope.launch {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

            toggleLikeUseCase(comentarioId, userId, "like")
                .onSuccess {
                    _comentarios.value = _comentarios.value.map { comentario ->
                        if (comentario.id == comentarioId) {
                            comentario.copy(
                                likes = if (comentario.userLikeStatus == "like") comentario.likes - 1 else comentario.likes + 1,
                                dislikes = if (comentario.userLikeStatus == "dislike") comentario.dislikes - 1 else comentario.dislikes,
                                userLikeStatus = if (comentario.userLikeStatus == "like") null else "like"
                            )
                        } else {
                            comentario
                        }
                    }
                }
        }
    }

    fun darDislike(comentarioId: String) {
        viewModelScope.launch {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return@launch

            toggleLikeUseCase(comentarioId, userId, "dislike")
                .onSuccess {
                    _comentarios.value = _comentarios.value.map { comentario ->
                        if (comentario.id == comentarioId) {
                            comentario.copy(
                                likes = if (comentario.userLikeStatus == "like") comentario.likes - 1 else comentario.likes,
                                dislikes = if (comentario.userLikeStatus == "dislike") comentario.dislikes - 1 else comentario.dislikes + 1,
                                userLikeStatus = if (comentario.userLikeStatus == "dislike") null else "dislike"
                            )
                        } else {
                            comentario
                        }
                    }
                }
        }
    }
}