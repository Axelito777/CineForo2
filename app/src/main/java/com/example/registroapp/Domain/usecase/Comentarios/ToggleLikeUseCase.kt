package com.example.registroapp.Domain.usecase.Comentarios

import com.example.registroapp.Domain.repository.ComentarioRepository

class ToggleLikeUseCase(private val repository: ComentarioRepository) {
    suspend operator fun invoke(comentarioId: String, userId: String, tipo: String): Result<Unit> {
        return repository.toggleLike(comentarioId, userId, tipo)
    }
}