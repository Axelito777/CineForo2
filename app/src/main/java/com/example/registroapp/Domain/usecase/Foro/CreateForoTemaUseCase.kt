package com.example.registroapp.Domain.usecase.Foro

import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.repository.ForoRepository

class CreateForoTemaUseCase(
    private val repository: ForoRepository
) {
    suspend operator fun invoke(
        userId: String,
        titulo: String,
        descripcion: String,
        categoria: String
    ): Result<ForoTema> {
        return repository.crearTema(userId, titulo, descripcion, categoria)
    }
}