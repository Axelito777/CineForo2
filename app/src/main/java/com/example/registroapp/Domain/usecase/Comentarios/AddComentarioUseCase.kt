package com.example.registroapp.Domain.usecase.Comentarios

import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.repository.ComentarioRepository

class AddComentarioUseCase(private val repository: ComentarioRepository) {
    suspend operator fun invoke(
        userId: String,
        movieId: Int? = null,
        temaId: String? = null,
        contenido: String,
        userNombre: String
    ): Result<Comentario> {
        return repository.agregarComentario(userId, movieId, temaId, contenido, userNombre)
    }
}