package com.example.registroapp.Presentation.ViewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.usecase.Foro.AddComentarioTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.GetComentariosTemaUseCase
import com.example.registroapp.Domain.usecase.Foro.GetForoTemasUseCase
import com.example.registroapp.Domain.usecase.Foro.ToggleLikeTemaUseCase
import com.example.registroapp.Utils.Resource
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

data class TemaDetailUiState(
    val tema: ForoTema? = null,
    val comentarios: List<Comentario> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isTogglingLike: Boolean = false,  // ✅ NUEVO: Evitar múltiples clicks
    val error: String? = null
)

class TemaDetailViewModel(
    private val temaId: String,
    private val getForoTemasUseCase: GetForoTemasUseCase,
    private val getComentariosTemaUseCase: GetComentariosTemaUseCase,
    private val addComentarioTemaUseCase: AddComentarioTemaUseCase,
    private val toggleLikeTemaUseCase: ToggleLikeTemaUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(TemaDetailUiState())
    val uiState: State<TemaDetailUiState> = _uiState

    init {
        loadTemaDetail()
        loadComentarios()
    }

    private fun loadTemaDetail() {
        viewModelScope.launch {
            Log.d("TemaDetailVM", "📖 Cargando tema: $temaId")
            _uiState.value = _uiState.value.copy(isLoading = true)

            getForoTemasUseCase().fold(
                onSuccess = { temas ->
                    val tema = temas.find { it.id == temaId }
                    if (tema != null) {
                        Log.d("TemaDetailVM", "✅ Tema encontrado: ${tema.titulo}")
                        _uiState.value = _uiState.value.copy(
                            tema = tema,
                            isLoading = false
                        )
                    } else {
                        Log.e("TemaDetailVM", "❌ Tema no encontrado")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Tema no encontrado"
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("TemaDetailVM", "❌ Error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al cargar tema"
                    )
                }
            )
        }
    }

    fun loadComentarios() {
        viewModelScope.launch {
            Log.d("TemaDetailVM", "💬 Cargando comentarios del tema: $temaId")

            val result = getComentariosTemaUseCase(temaId)
            when (result) {
                is Resource.Success -> {
                    Log.d("TemaDetailVM", "✅ Comentarios cargados: ${result.data?.size ?: 0}")
                    _uiState.value = _uiState.value.copy(
                        comentarios = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("TemaDetailVM", "❌ Error al cargar comentarios: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // No hacer nada, ya manejamos el estado de carga
                }
            }
        }
    }

    fun addComentario(contenido: String) {
        if (contenido.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El comentario no puede estar vacío"
            )
            return
        }

        viewModelScope.launch {
            Log.d("TemaDetailVM", "📝 Agregando comentario al tema: $temaId")
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

            val result = addComentarioTemaUseCase(temaId, contenido)
            when (result) {
                is Resource.Success -> {
                    Log.d("TemaDetailVM", "✅ Comentario agregado exitosamente")
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    // Recargar comentarios
                    loadComentarios()
                    // Actualizar contador del tema
                    _uiState.value.tema?.let { tema ->
                        _uiState.value = _uiState.value.copy(
                            tema = tema.copy(numComentarios = tema.numComentarios + 1)
                        )
                    }
                }
                is Resource.Error -> {
                    Log.e("TemaDetailVM", "❌ Error al agregar comentario: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.message ?: "Error al agregar comentario"
                    )
                }
                is Resource.Loading -> {
                    // Ya manejado
                }
            }
        }
    }

    fun toggleLike() {
        // ✅ EVITAR MÚLTIPLES CLICKS MIENTRAS SE PROCESA
        if (_uiState.value.isTogglingLike) {
            Log.d("TemaDetailVM", "⚠️ Ya se está procesando un like, ignorando...")
            return
        }

        viewModelScope.launch {
            // ✅ MARCAR INMEDIATAMENTE COMO PROCESANDO
            _uiState.value = _uiState.value.copy(isTogglingLike = true)

            val userId = try {
                SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Usuario no autenticado")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes estar autenticado para dar like",
                    isTogglingLike = false
                )
                return@launch
            }

            Log.d("TemaDetailVM", "👍 Toggle like en tema: $temaId")

            _uiState.value.tema?.let { tema ->
                toggleLikeTemaUseCase(temaId, userId).fold(
                    onSuccess = { agregado ->
                        Log.d("TemaDetailVM", "✅ Like ${if (agregado) "agregado" else "eliminado"}")
                        val nuevoLikes = if (agregado) tema.likes + 1 else maxOf(0, tema.likes - 1)
                        _uiState.value = _uiState.value.copy(
                            tema = tema.copy(likes = nuevoLikes),
                            isTogglingLike = false  // ✅ Permitir nuevos clicks
                        )
                    },
                    onFailure = { error ->
                        Log.e("TemaDetailVM", "❌ Error al dar like: ${error.message}")
                        _uiState.value = _uiState.value.copy(
                            error = "Error al actualizar like",
                            isTogglingLike = false  // ✅ Permitir reintentar
                        )
                    }
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(isTogglingLike = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}