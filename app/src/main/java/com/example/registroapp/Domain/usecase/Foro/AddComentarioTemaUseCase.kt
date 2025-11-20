package com.example.registroapp.Domain.usecase.Foro

import com.example.registroapp.Di.SupabaseClient
import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.repository.ComentarioRepository
import com.example.registroapp.Utils.Resource
import io.github.jan.supabase.gotrue.auth

class AddComentarioTemaUseCase(
    private val repository: ComentarioRepository
) {
    suspend operator fun invoke(temaId: String, contenido: String): Resource<Comentario> {
        return try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                ?: return Resource.Error("Usuario no autenticado")

            val userId = currentUser.id
            val userNombre = currentUser.email?.substringBefore("@") ?: "Usuario"

            repository.agregarComentario(
                userId = userId,
                temaId = temaId,
                contenido = contenido,
                userNombre = userNombre
            ).fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error al agregar comentario") }
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }
}