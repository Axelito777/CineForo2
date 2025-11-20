package com.example.registroapp.Domain.usecase.Foro

import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.repository.ComentarioRepository
import com.example.registroapp.Utils.Resource
import io.github.jan.supabase.gotrue.auth

class GetComentariosTemaUseCase(
    private val repository: ComentarioRepository
) {
    suspend operator fun invoke(temaId: String): Resource<List<Comentario>> {
        return try {
            val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
            repository.obtenerComentariosTema(temaId, userId).fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error al obtener comentarios") }
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}