package com.example.registroapp.Domain.usecase.Foro

import com.example.registroapp.Domain.model.ForoTema
import com.example.registroapp.Domain.repository.ForoRepository

class GetForoTemasUseCase(
    private val repository: ForoRepository
) {
    suspend operator fun invoke(): Result<List<ForoTema>> {
        return repository.obtenerTemas() // ⚠️ NOTA: es "obtenerTemas", no "getTemas"
    }
}