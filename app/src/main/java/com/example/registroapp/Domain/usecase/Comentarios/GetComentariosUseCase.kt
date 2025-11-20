package com.example.registroapp.Domain.usecase.Comentarios

import com.example.registroapp.Domain.model.Comentario
import com.example.registroapp.Domain.repository.ComentarioRepository

class GetComentariosUseCase(private val repository: ComentarioRepository) {
    suspend operator fun invoke(movieId: Int, userId: String): Result<List<Comentario>> {
        return repository.obtenerComentariosPelicula(movieId, userId)
    }
}