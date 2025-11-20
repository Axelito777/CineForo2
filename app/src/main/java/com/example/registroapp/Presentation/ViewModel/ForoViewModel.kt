package com.example.registroapp.Presentation.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.usecase.Foro.CreateForoTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.GetForoTemasUseCase
import com.example.registroapp.Domain.usecase.Foro.ToggleLikeTemaUseCase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForoUiState(
    val temas: List<ForoTema> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreatingTema: Boolean = false,
    val likingTemaIds: Set<String> = emptySet()  // ✅ NUEVO: Track de temas siendo likeados
)

class ForoViewModel(
    private val getForoTemasUseCase: GetForoTemasUseCase,
    private val createForoTemaUseCase: CreateForoTemaUseCase,
    private val toggleLikeTemaUseCase: ToggleLikeTemaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForoUiState())
    val uiState: StateFlow<ForoUiState> = _uiState.asStateFlow()

    init {
        loadTemas()
    }

    fun loadTemas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getForoTemasUseCase().fold(
                onSuccess = { temas ->
                    // Log para verificar los likes
                    temas.forEach { tema ->
                        android.util.Log.d("ForoVM", "Tema: ${tema.titulo}, Likes: ${tema.likes}")
                    }
                    _uiState.value = _uiState.value.copy(
                        temas = temas,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun createTema(titulo: String, descripcion: String, categoria: String) {
        if (titulo.isBlank() || descripcion.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El título y descripción no pueden estar vacíos"
            )
            return
        }

        viewModelScope.launch {
            Log.d("ForoViewModel", "📝 Creando tema: $titulo")
            _uiState.value = _uiState.value.copy(isCreatingTema = true, error = null)

            val userId = try {
                SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Usuario no autenticado")
            } catch (e: Exception) {
                Log.e("ForoViewModel", "❌ Error de autenticación: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isCreatingTema = false,
                    error = "Error de autenticación"
                )
                return@launch
            }

            Log.d("ForoViewModel", "👤 Usuario autenticado: $userId")

            createForoTemaUseCase(userId, titulo, descripcion, categoria).fold(
                onSuccess = { nuevoTema ->
                    Log.d("ForoViewModel", "✅ Tema creado: ${nuevoTema.id}")
                    _uiState.value = _uiState.value.copy(
                        temas = listOf(nuevoTema) + _uiState.value.temas,
                        isCreatingTema = false
                    )
                },
                onFailure = { error ->
                    Log.e("ForoViewModel", "❌ Error al crear tema: ${error.message}")
                    Log.e("ForoViewModel", "Stack trace: ${error.stackTraceToString()}")
                    _uiState.value = _uiState.value.copy(
                        isCreatingTema = false,
                        error = error.message ?: "Error al crear tema"
                    )
                }
            )
        }
    }

    fun toggleLikeTema(temaId: String) {
        // ✅ BLOQUEAR si ya se está procesando este tema
        if (_uiState.value.likingTemaIds.contains(temaId)) {
            Log.d("ForoViewModel", "⚠️ Ya se está procesando like para tema: $temaId")
            return
        }

        viewModelScope.launch {
            Log.d("ForoViewModel", "👍 Toggle like para tema: $temaId")

            // ✅ MARCAR como procesando INMEDIATAMENTE
            _uiState.value = _uiState.value.copy(
                likingTemaIds = _uiState.value.likingTemaIds + temaId
            )

            // Obtener el userId actual
            val userId = try {
                SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Usuario no autenticado")
            } catch (e: Exception) {
                Log.e("ForoViewModel", "❌ Error de autenticación: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Debes estar autenticado para dar like",
                    likingTemaIds = _uiState.value.likingTemaIds - temaId
                )
                return@launch
            }

            Log.d("ForoViewModel", "👤 Usuario autenticado: $userId")

            // Llamar al UseCase con userId
            toggleLikeTemaUseCase(temaId, userId).fold(
                onSuccess = { agregado ->
                    Log.d("ForoViewModel", "✅ Like ${if (agregado) "agregado" else "eliminado"}")

                    // ✅ ACTUALIZAR correctamente según si se agregó o quitó
                    val updatedTemas = _uiState.value.temas.map { tema ->
                        if (tema.id == temaId) {
                            val nuevoLikes = if (agregado) {
                                tema.likes + 1
                            } else {
                                maxOf(0, tema.likes - 1)
                            }
                            tema.copy(likes = nuevoLikes)
                        } else {
                            tema
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        temas = updatedTemas,
                        likingTemaIds = _uiState.value.likingTemaIds - temaId
                    )
                },
                onFailure = { error ->
                    Log.e("ForoViewModel", "❌ Error al dar like: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Error al dar like",
                        likingTemaIds = _uiState.value.likingTemaIds - temaId
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}