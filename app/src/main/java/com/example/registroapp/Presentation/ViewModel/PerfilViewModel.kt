package com.example.registroapp.Presentation.ViewModel

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.registroapp.Data.Repository.AuthRepository
import com.example.registroapp.Domain.model.UsuarioSupabase
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

data class PerfilState(
    val usuario: UsuarioSupabase? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val estadisticas: Map<String, Int>? = null
)

class PerfilViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(PerfilState())
    val state: State<PerfilState> = _state

    init {
        cargarPerfil()
        cargarEstadisticas()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = authRepository.obtenerUsuarioActual()
            result.fold(
                onSuccess = { usuario ->
                    _state.value = _state.value.copy(
                        usuario = usuario,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun setEditing(editing: Boolean) {
        _state.value = _state.value.copy(
            isEditing = editing,
            error = null,
            successMessage = null
        )
    }

    fun actualizarPerfil(nombre: String, generoFavorito: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            val result = authRepository.actualizarPerfil(nombre, generoFavorito)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        isEditing = false,
                        successMessage = "Perfil actualizado correctamente"
                    )
                    cargarPerfil()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun cambiarPassword(passwordActual: String, passwordNueva: String, confirmarPassword: String) {
        if (passwordNueva != confirmarPassword) {
            _state.value = _state.value.copy(error = "Las contraseñas no coinciden")
            return
        }

        if (passwordNueva.length < 6) {
            _state.value = _state.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            val result = authRepository.cambiarPassword(passwordActual, passwordNueva)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        successMessage = "Contraseña actualizada correctamente"
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun subirFoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            // Convertir bitmap a base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            val result = authRepository.subirFotoPerfil(base64)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        successMessage = "Foto actualizada correctamente"
                    )
                    cargarPerfil()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = authRepository.cerrarSesion()
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error ->
                    _state.value = _state.value.copy(error = error.message)
                }
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            error = null,
            successMessage = null
        )
    }
    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val userId = com.example.registroapp.Di.SupabaseClient.client.auth.currentUserOrNull()?.id

                if (userId == null) {
                    _state.value = _state.value.copy(
                        estadisticas = mapOf("favoritos" to 0, "comentarios" to 0, "likes" to 0)
                    )
                    return@launch
                }

                var favoritosCount = 0
                var comentariosCount = 0
                var likesCount = 0

                // Favoritos
                try {
                    val favs = com.example.registroapp.Di.SupabaseClient.client
                        .from("favoritos")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                    favoritosCount = favs.size
                } catch (e: Exception) {
                    android.util.Log.e("PerfilVM", "Error favoritos: ${e.message}")
                }

                // Comentarios
                try {
                    val coms = com.example.registroapp.Di.SupabaseClient.client
                        .from("comentarios")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                    comentariosCount = coms.size
                } catch (e: Exception) {
                    android.util.Log.e("PerfilVM", "Error comentarios: ${e.message}")
                }

                // Likes de comentarios
                try {
                    val likesComentarios = com.example.registroapp.Di.SupabaseClient.client
                        .from("likes_comentarios")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                    likesCount += likesComentarios.size
                } catch (e: Exception) {
                    android.util.Log.e("PerfilVM", "Error likes_comentarios: ${e.message}")
                }

                // Likes de temas del foro
                try {
                    val likesTemas = com.example.registroapp.Di.SupabaseClient.client
                        .from("foro_tema_likes")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<kotlinx.serialization.json.JsonObject>()
                    likesCount += likesTemas.size
                } catch (e: Exception) {
                    android.util.Log.e("PerfilVM", "Error foro_tema_likes: ${e.message}")
                }

                _state.value = _state.value.copy(
                    estadisticas = mapOf(
                        "favoritos" to favoritosCount,
                        "comentarios" to comentariosCount,
                        "likes" to likesCount
                    )
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    estadisticas = mapOf("favoritos" to 0, "comentarios" to 0, "likes" to 0)
                )
            }
        }
    }
}