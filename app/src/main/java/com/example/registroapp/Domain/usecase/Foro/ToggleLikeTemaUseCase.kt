package com.example.registroapp.Domain.usecase.Foro

import com.example.registroapp.Domain.repository.ForoRepository

class ToggleLikeTemaUseCase(
    private val repository: ForoRepository
) {
    suspend operator fun invoke(temaId: String, userId: String): Result<Boolean> {
        return try {
            // Verificar si ya existe el like
            val yaExiste = repository.verificarLike(temaId, userId).getOrNull() ?: false

            if (yaExiste) {
                // Si existe, quitarlo
                repository.quitarLike(temaId, userId)
                Result.success(false) // Retorna false = like removido
            } else {
                // Si no existe, agregarlo
                repository.darLike(temaId, userId)
                Result.success(true) // Retorna true = like agregado
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}